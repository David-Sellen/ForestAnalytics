package edu.miun.firespark.domain;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.StreamingContext;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.Duration;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Created by Dwe on 2016-03-14.
 */

@Scope("singleton")
@Component
public class SparkContextBean {
    // Local settings
    static final String sparkLocal = "local[*]"; // Run locally on as many threads as possible
    SparkConf sparkConf;
    JavaSparkContext sparkContext;
    SQLContext sqlContext;
    JavaStreamingContext streamContext;

    @PostConstruct
    public void init(){
        // Init Spark context
        System.setProperty("spark.execution.memory", "1g");
        this.sparkConf = new SparkConf().setAppName("FireSpark")
                .setAppName("TestApp")
                .setMaster(sparkLocal); // set to sparkMaster or sparkLocal
        // Disable unnecessary logging.
        Logger.getLogger("org").setLevel(Level.OFF);
        Logger.getLogger("akka").setLevel(Level.OFF);

        sparkContext = new JavaSparkContext(sparkConf);
        sqlContext = new SQLContext(sparkContext);
        streamContext = new JavaStreamingContext( sparkContext, Durations.seconds(1));
    }

    public JavaSparkContext getContext(){
        return sparkContext;
    }

    public SQLContext getSQLContext(){ return sqlContext; }

    public JavaStreamingContext getStreamContext(){ return streamContext; }

}
