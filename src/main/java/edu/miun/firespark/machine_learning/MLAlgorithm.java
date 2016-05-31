package edu.miun.firespark.machine_learning;

import org.apache.spark.ml.Model;
import org.apache.spark.sql.DataFrame;

/**
 * Created by Dwe on 2016-05-08.
 */
public interface MLAlgorithm {
    public void train(DataFrame input);
    public DataFrame predict(DataFrame input);
    public Model getModel();
}
