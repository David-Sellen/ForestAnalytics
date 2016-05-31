package edu.miun.firespark.utilities;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;

/**
 * Created by Dwe on 2016-03-14.
 */

@Scope("singleton")
@Component
public class SparkContextBean {
    SparkConf sparkConf;
    JavaSparkContext sparkContext;
    SQLContext sqlContext;
    JavaStreamingContext streamContext;

    @Value("${spring.application.name}")
    private String appName;
    @Value("${spark.master}")
    private String sparkMaster;

    @PostConstruct
    public void init(){
        String[] jarPaths = new String[0];
        jarPaths = new String[]{getJarPath(com.databricks.spark.xml.XmlInputFormat.class),
                "file:/C:/Users/Dwe/IdeaProjects/FireSpark/target/firespark-app-1.0.jar"};

        //Class[] kryoClasses= {MachineLearningService.class};
        //System.setProperty("spark.execution.memory", "1g");
        this.sparkConf = new SparkConf().setAppName(appName)
                .setAppName(appName)
                .set("spark.local.ip", "192.168.1.232")
                .set("spark.executor.memory", "3700m")
                .set("spark.executor.cores", "4")
                .set("spark.driver.maxResultSize", "4000m")
                .setJars(jarPaths)
                .setMaster(sparkMaster); // set to sparkMaster or sparkLocal
        // Disable unnecessary logging.
        Logger.getLogger("org").setLevel(Level.ERROR);
        Logger.getLogger("akka").setLevel(Level.ERROR);


        sparkContext = new JavaSparkContext(sparkConf);
        sqlContext = new SQLContext(sparkContext);
        streamContext = new JavaStreamingContext( sparkContext, Durations.seconds(1));
    }

    public JavaSparkContext getContext(){
        return sparkContext;
    }

    public SQLContext getSQLContext(){ return sqlContext; }

    public JavaStreamingContext getStreamContext(){ return streamContext; }

    public static String getJarPath(Class c) {
        if(c == null) {
            return null;
        }

        try {
            ProtectionDomain protectionDomain = c.getProtectionDomain();
            CodeSource codeSource = protectionDomain.getCodeSource();
            URL location = codeSource.getLocation();
            return location.toString();
        } catch (Throwable e) {
            e.printStackTrace();
            return "JAR not found";
        }
    }

}
