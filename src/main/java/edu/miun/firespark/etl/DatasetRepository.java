package edu.miun.firespark.etl;

import edu.miun.firespark.utilities.SparkContextBean;
import edu.miun.firespark.machine_learning.Dataset;
import edu.miun.firespark.utilities.Timer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.spark.sql.DataFrame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dwe on 2016-04-05.
 */

@Scope("singleton")
@Component
public class DatasetRepository {
    private static String path;
    private SparkContextBean sparkContext;

    // Timer for measuring execution time
    private Timer timer = new Timer();

    @Value("${storage.prefix}")
    String prefix;

    @Autowired
    public DatasetRepository(@Value("${location.dataset}") String url, SparkContextBean scb) {
        this.path = url;
        this.sparkContext = scb;
    }

    public String[] getListOfDatasets(){
        if(prefix.contains("hdfs://")){
            return listDatasetsHDFS();
        }
        return listDatasetsLocal();
    }

    // Store the dataset as a Parquet file
    public void saveDataset(Dataset dataset){
        // TODO: remove timer
        timer.start();
        dataset.getDf().write().parquet(prefix+path + "/" + dataset.getName() + ".parquet");
        timer.stop();
        System.out.println("save.dataset.parquet: " + timer + " ns");
    }

    public Dataset loadDataset(String name){
        // TODO: remove timer
        timer.start();
        DataFrame df = sparkContext.getSQLContext().read().parquet(prefix+path + "/" + name + ".parquet");
        Dataset dataset = new Dataset(name, df);
        timer.stop();
        System.out.println("load.dataset.parquet: " + timer + " ns");
        return dataset;
    }


    private String[] listDatasetsLocal(){
        File file = new File(path);
        String[] content = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
        return content;
    }

    private String[] listDatasetsHDFS(){
        FileSystem fs = null;
        List<String> content = new ArrayList<>();
        try {
            fs = FileSystem.get(new URI(prefix), new Configuration());
            FileStatus[] status = fs.listStatus(new Path(path));
            Path[] paths = FileUtil.stat2Paths(status);
            for (Path path : paths){
                content.add(path.getName());
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return content.toArray(new String[0]);
    }


}
