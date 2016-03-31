import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;
import org.junit.Test;

import static org.apache.spark.sql.functions.*;

/**
 * Created by Dwe on 2016-03-03.
 */
public class SparkSQLCountStems {

    // Cluster settings
    static final String sparkMaster = "spark://10.211.55.101:7077";
    static final String hdfsRoot = "hdfs://10.211.55.101:50070";

    // Local settings
    static final String sparkLocal = "local[*]"; // Run locally on as many threads as possible
    static final String localDir = "file:///D:/Skola/2016 VT Examensarbete Masternivå/SDC data/HPR filer/Raw";

    @Test
    public void countStems(){
        SparkConf sparkConf = new SparkConf().setAppName("FireSpark")
                .setAppName("TestApp")
                .setMaster(sparkLocal); // set to sparkMaster or sparkLocal

        // Disable unnecessary logging.
        Logger.getLogger("org").setLevel(Level.OFF);
        Logger.getLogger("akka").setLevel(Level.OFF);

        JavaSparkContext sc = new JavaSparkContext(sparkConf);
        SQLContext sqlContext = new SQLContext(sc);
        DataFrame df = sqlContext.read()
                .format("com.databricks.spark.xml")
                .option("rowTag", "HarvestedProduction")
                .load("D:/hpr/*");

        df.registerTempTable("HarvestedProduction");

        DataFrame worksiteNames = df.select("Machine.ObjectDefinition.ObjectUserID", "Machine.Stem.StemCoordinates").
                groupBy("ObjectUserID").agg(first("StemCoordinates.Latitude.#VALUE").getItem(0).alias("Latitude"), first("StemCoordinates.Longitude.#VALUE").getItem(0).alias("Longitude"));
        worksiteNames.show();
        //sqlContext.sql("SELECT count(Machine.Stem.StemKey) AS Stems FROM HarvestedProduction").show();
        //StemAndSpecies.show();
        //int stems = (StemAndSpecies.collect()).length;

        //long stems = df.select("Machine.Stem").count();
        //System.out.println(stems);
        //df.show();

        //DataFrame df = sqlContext.read().text(localDir+"/gpx103-sdcgpx7364-358bb34d_da95_45aa_9e0d_78ee5917db4e-01.04-2015-12-07 1603.hpr");
        //df.printSchema();
        // System.out.println(df.toString());

        //System.out.println("Reading lines...");
        //JavaRDD<String> textFile = sc.textFile(localDir+"/*");
        //System.out.println("Lines: " + textFile.count());


        /* Time measuring
            long start = System.currentTimeMillis();
            long elapsed = System.currentTimeMillis()-start;
            long average = elapsed / 100;
            System.out.println("Elapsed time: " + average);
         */
        sc.stop();

    }
}
