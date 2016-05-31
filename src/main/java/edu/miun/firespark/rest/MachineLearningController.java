package edu.miun.firespark.rest;

import edu.miun.firespark.messages.MLQuery;
import edu.miun.firespark.utilities.SparkContextBean;
import edu.miun.firespark.machine_learning.ClusteringResults;
import edu.miun.firespark.machine_learning.Dataset;
import edu.miun.firespark.machine_learning.KMeans;
import edu.miun.firespark.machine_learning.MLAlgorithm;
import edu.miun.firespark.machine_learning.MachineLearningService;
import edu.miun.firespark.utilities.Timer;
import org.apache.log4j.Logger;
import edu.miun.firespark.etl.HarvestProductionReportRepository;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.*;

import static org.apache.spark.sql.functions.*;

/**
 * Created by Dwe on 2016-03-15.
 */

@RestController
public class MachineLearningController {

    @Autowired
    private MachineLearningService mlService;
    @Autowired
    private HarvestProductionReportRepository hprAdepter;
    @Autowired
    private HttpSession httpSession; //Client http session
    @Autowired
    SparkContextBean scb;
    @Value("${mode}")
    String mode;



    /* Steps for ml/run
     * 1. Receive and parse MLQuery, t.ex. algorithm K-means och K=2
     * 2. Build and prepare dataset
     * 3. Run algorithm
     * 4. Save model?
     * 5. Reply with results, i.e. cluster centroids, %corrent classified etc.
     */
    @RequestMapping( value = "/ml/run", method = RequestMethod.POST)
    public ResponseEntity<ClusteringResults> runAlgorithm(@RequestBody MLQuery query) {
        //Get session dataset, if null return error.
        Dataset workingDataset = (Dataset)httpSession.getAttribute("dataset");
        if(workingDataset == null){
            Logger.getLogger("org").error("ml/algorithms: Session dataset was null!");
            return new ResponseEntity<ClusteringResults>(HttpStatus.BAD_REQUEST);// Return HttpStatus.NOT_FOUND
        }

        //Declare variables
        MLAlgorithm mlAlgorithm;
        ClusteringResults clusteringResults = null;
        DataFrame train = null;
        DataFrame test = null;
        boolean useLabel = false;
        boolean useTrain = true;

        try {
            useTrain = (Boolean) query.getParameters().get("useTrain");
        } catch (Exception e){
            useTrain = false;
        }

        String labelCol = query.getLabel();
        int sgnIndex = Arrays.asList(workingDataset.getDf().columns()).indexOf(labelCol);
        if(sgnIndex != -1){
            // No labeled column will be used..
            useLabel = true;
        }

        if(!useTrain) {
            double trainPercentage = query.getSplitRatio();
            int seed = query.getSeed();
            if (seed == 0) {
                seed = new Random().nextInt(100000);
            }
            // Split dataset into test and train. Set label column.
            DataFrame[] data = mlService.prepareTrainAndTestDF(workingDataset.getDf(), trainPercentage, seed, labelCol);
            train = data[0];
            test = data[1];
            test.cache();
        } else {
            train = mlService.prepareAllTrainDF(workingDataset.getDf(), labelCol);
        }
        train.cache();

        /* Creation of CSV file containing the preprocessed data
        String header = "Length, Volume, DBH, SpeciesGroupName";
        List<String> lines = new ArrayList<>();
        lines.add(delimitor);
        lines.add(header);
        Row[] rows = train.collect();

        for(Row r : rows){
            Vector vec = (Vector)r.get(1);
            String line = vec.apply(0) + ", " + vec.apply(1) + ", " + vec.apply(2) + ", " + r.getString(0);
            lines.add(line);
        }

        try {
            Charset charset = Charset.forName("UTF-8");
            Files.write(Paths.get("D:\\hpr_data.csv"), lines, charset);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        switch (query.getAlgorithm()){
            case "kmeans":
                System.out.println("Running K-Means!");
                try {
                    int k;
                    if( query.getParameters().get("k") instanceof String){
                        k  = Integer.parseInt((String) query.getParameters().get("k"));
                    } else {
                        k  = (Integer) query.getParameters().get("k");
                    }

                    int iterations;
                    if( query.getParameters().get("iterations") instanceof String){
                        iterations  = Integer.parseInt((String) query.getParameters().get("iterations"));
                    } else {
                        iterations  = (Integer) query.getParameters().get("iterations");
                    }

                    int centroidSeed = (int) query.getParameters().get("centroidSeed");
                    if(mode.equals("local")) {
                        mlAlgorithm = new KMeans(scb, k, iterations, centroidSeed, "k-means||");
                    } else {
                        mlAlgorithm = new KMeans(scb, k, iterations, centroidSeed, "k-means||");
                    }

                    // TODO: remove timer
                    Timer timer = new Timer();
                    timer.start();
                    mlAlgorithm.train(train); // Train - build model
                    timer.stop();
                    System.out.println("allTrain.train: " + timer.elapsedTimeNano() + " ns");

                    Vector[] centroids = ((KMeans)mlAlgorithm).getCentroids(); // Get centroids produces during clustering.
                    DataFrame predictions;
                    DataFrame groupedInstances = null;

                    if(useTrain) { //Train - predict on model
                        predictions = mlAlgorithm.predict(train);
                        // TODO: remove timer and collectkrav har ställts på implementationen
                        timer.start();
                        predictions.collect();
                        timer.stop();
                        System.out.println("allTrain.predict: " + timer.elapsedTimeNano() + " ns");
                    } else {
                        predictions = mlAlgorithm.predict(test);
                        System.out.println("COLLECT");
                    }

                    //Get instances and label for each cluster
                    Row[] instancesInClusters; //Aggregated predictions of each cluster
                    if(useLabel){
                        DataFrame classToCluster = predictions.select("prediction", "label").groupBy(col("label"), col("prediction")).agg(count(col("prediction")).as("count"));
                        instancesInClusters = classToCluster.collect();
                    } else {
                        DataFrame clusterInstances = predictions.select("prediction").groupBy(col("prediction")).agg(count(col("prediction")).as("count"));
                        instancesInClusters = clusterInstances.collect();
                    }

                    clusteringResults = new ClusteringResults(k, instancesInClusters); // Create results
                    //Add centroid points to the results
                    for (int i=0; i<centroids.length; i++) {
                        clusteringResults.getCluster(i).setCentroid(centroids[i].toArray());
                    }

                } catch(Exception e){
                    e.printStackTrace();
                    Logger.getLogger("org").error("ml/algorithms: failed to parse K-Means data.");
                }
                train.unpersist();
                if(test != null) test.unpersist();
                break;
            default:
                Logger.getLogger("org").error("ml/algorithms: Failed to run algorithm.");
                break;
        }

        if(clusteringResults == null){
            return new ResponseEntity<ClusteringResults>(HttpStatus.NO_CONTENT);// Return HttpStatus.NOT_FOUND
        }
        return new ResponseEntity<ClusteringResults>(clusteringResults, HttpStatus.OK);
    }
}
