package edu.miun.firespark.machine_learning;

import edu.miun.firespark.utilities.SparkContextBean;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.ml.Model;
import org.apache.spark.ml.clustering.KMeansModel;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.VectorUDT;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.io.IOException;

/**
 * Created by Dwe on 2016-03-15.
 */

public class KMeans implements MLAlgorithm {
    private org.apache.spark.ml.clustering.KMeans kMeans;
    private KMeansModel model;
    private int k;
    private int iterations;
    private int randomSeed;
    private Vector[] centroids;
    private SparkContextBean scb;

    public KMeans(SparkContextBean scb, int k, int iterations, int randomSeed, String initMode){
        this.scb = scb;
        this.k = k;
        this.iterations = iterations;
        this.randomSeed = randomSeed;
        kMeans = new org.apache.spark.ml.clustering.KMeans()
                .setK(k)
                .setMaxIter(iterations)
                .setSeed(randomSeed)
                .setInitMode(initMode);
        System.out.println("Running init mode:" + kMeans.getInitMode());
        System.out.println("Running init steps:" + kMeans.getInitSteps());
        model = null;
    }

    public Vector[] getCentroids(){
        if(model != null){
            centroids = model.clusterCenters();
        }
        return centroids;
    }

    @Override
    public void train(DataFrame trainData) {
        model = this.kMeans.fit(trainData.select("features"));
    }

    public void saveModel(String name){
        if(model != null){
            try {
                //TODO set path to storage.model
                model.write().overwrite().save("D:\\storage\\model\\"+name + ".pmml");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public DataFrame predict(DataFrame data) {
        // Features column must have index 1
        // Label column must have index 0
        final KMeansModel finalModel = model;
        JavaRDD<Row> predRDD = data.javaRDD().map(r -> RowFactory.create((Vector)r.get(1)
                                                , finalModel.predict((Vector)r.get(1))
                                                , r.getString(0)));
        StructField[] fields = {new StructField("features", new VectorUDT(), false, Metadata.empty()),
                                new StructField("prediction", DataTypes.IntegerType, false, Metadata.empty()),
                                new StructField("label", DataTypes.StringType, false, Metadata.empty())};
        StructType schema = new StructType(fields);
        DataFrame prediction = scb.getSQLContext().createDataFrame(predRDD, schema);
        return prediction;
    }

    @Override
    public Model getModel() {
        return model;
    }

    @Override
    public String toString() {
        return "K-Means";
    }
}
