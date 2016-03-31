package edu.miun.firespark.service;

import edu.miun.firespark.domain.SparkContextBean;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.AnalysisException;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.storage.StorageLevel;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

import static org.apache.spark.sql.functions.*;

/**
 * Created by Dwe on 2016-02-28.
 */

@Scope("singleton")
@Component
public class HarvestProductionReportRepository {

    private static String path;

    private SparkContextBean sparkContext;
    private DataFrame df;
    private DataFrame stems;
    private DataFrame worksites;
    private Long nrOfStems;

    @Autowired
    public HarvestProductionReportRepository(@Value("${location.hpr}") String url, SparkContextBean spc) {
        this.path = url;
        this.sparkContext = spc;
        System.out.println("URL: " + path);
        df = sparkContext.getSQLContext().read()
                .format("com.databricks.spark.xml")
                .option("rowTag", "HarvestedProduction")
                .load(path+"/*");
                //.load("D:/Skola/2016 VT Examensarbete Masternivå/SDC data/HPR filer/Raw/*");
        //df.schema().json();
        //StructType schema = StructType.fromJson();
        StructType schema = df.schema();

        /*df = sparkContext.getSQLContext().read()
                .format("com.databricks.spark.xml")
                .option("rowTag", "HarvestedProduction")
                .schema(schema)
                .load("file:///D:/hpr/*");
        df.printSchema();
        */
        /*
        try {
            stems = df.select(explode(df.col("Machine.Stem.StemKey"))).persist(StorageLevel.MEMORY_ONLY());
            // Cache current data
            //nrOfStems = sparkContext.getSQLContext().sql("SELECT COUNT(st) FROM HarvestedProduction LATERAL VIEW explode(Machine.Stem.StemKey) as st" ).first().getLong(0);
            nrOfStems = stems.count();
            //nrOfStems = df.select(explode(df.col("Machine.Stem.StemKey"))).count();
        } catch (Exception e){
            System.out.println("ERROR:" + e.getMessage());
        }*/

        try {
            worksites = df.filter(col("Machine.Stem.StemCoordinates").getItem(0).getField("Latitude").getField("#VALUE").isNotNull())
                            .select(col("Machine.ObjectDefinition.ObjectUserID.#VALUE"), col("Machine.Stem.StemCoordinates"), size(col("Machine.Stem")).as("Stem"))
                            .groupBy(col("#VALUE").getItem(0).as("id")).agg(first("StemCoordinates").getItem(0).getField("Latitude").getField("#VALUE").getItem(0).alias("latitude")
                            ,first("StemCoordinates").getItem(0).getField("Longitude").getField("#VALUE").getItem(0).alias("longitude"),
                            sum("Stem").as("stems"))
                            .persist(StorageLevel.MEMORY_ONLY());

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public Long getWorksitesCount(){
        return null;
    }

    public RDD<String> getAllWorksites(){
        return worksites.toJSON();
    }

    /*public List<String> getStream(){
        sparkContext.getStreamContext();
        JavaDStream<?> rdd = (JavaDStream<?>)worksites.rdd();
        List<String> worksites = new ArrayList<String>();
        return worksites;
    }*/

    public long getStemsCount(){
        long stemscnt = 0;
        JavaRDD<String> lines = sparkContext.getContext().textFile(path+"/*");
        stemscnt = lines.filter( s-> s.contains("<StemKey>")).count();
        return stemscnt;
    }


}
