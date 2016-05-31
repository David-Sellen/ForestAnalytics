package edu.miun.firespark.etl;

import edu.miun.firespark.messages.ETLQuery;
import edu.miun.firespark.utilities.SparkContextBean;
import edu.miun.firespark.utilities.Timer;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.types.StructType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.apache.spark.sql.functions.*;

/**
 * Created by Dwe on 2016-02-28.
 */

@Scope("singleton")
@Component
public class HarvestProductionReportRepository {

    private static String hprPath;
    private static String schemaName = "hpr_schema1";
    private static String schemaPath;
    private static String schemaFileExt = ".json";
    private StructType schema = null;

    private SparkContextBean sparkContext;
    private DataFrame df;
    private DataFrame worksites;

    public Boolean CREATE_SCHEMA_FILE = false;
    public Boolean USE_EXISISTING_SCHEMA_FILE = true;

    // Timer for measuring execution time
    private Timer timer = new Timer();

    @Autowired
    public HarvestProductionReportRepository(@Value("${location.hpr}") String hprPath,
                                             @Value("${storage.prefix}") String prefix,
                                             @Value("${location.schema.hpr}") String schemaPath,
                                             SparkContextBean scb) {
        this.hprPath = prefix+hprPath;
        this.schemaPath = schemaPath + "/" + schemaName + schemaFileExt;
        this.sparkContext = scb;

        if(CREATE_SCHEMA_FILE) {
            // Load HPR files to create a schema.
            df = sparkContext.getSQLContext().read()
                    .format("com.databricks.spark.xml")
                    .option("rowTag", "HarvestedProduction")
                    .option("charset", "iso-8859-1")
                    .option("treatEmptyValuesAsNulls", "true")
                    .load(this.hprPath+"/*");
            //df.setName("Raw HPR");

            try {
                schema = df.schema();
                if(schemaFileExt.equals(".obj")) {
                    FileOutputStream fout = new FileOutputStream(this.schemaPath );
                    ObjectOutputStream oos = new ObjectOutputStream(fout);
                    oos.writeObject(schema);
                    oos.close();
                } else if(schemaFileExt.equals(".json")){
                    FileWriter file = new FileWriter(this.schemaPath);
                    file.write(schema.json());
                    file.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(USE_EXISISTING_SCHEMA_FILE) {
            try {
                if(schemaFileExt.equals(".obj")) {
                    FileInputStream fin = new FileInputStream(this.schemaPath );
                    ObjectInputStream ois = new ObjectInputStream(fin);
                    schema = (StructType) ois.readObject();
                    ois.close();
                } else if(schemaFileExt.equals(".json")){
                    String json = new String(Files.readAllBytes(Paths.get(this.schemaPath)));
                    schema = (StructType)StructType.fromJson(json);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            schema = df.schema();
        }

        //schema.printTreeString();
        // Read HPR data that must contain information on stems and stem coordinates.
        df = sparkContext.getSQLContext().read()
                .format("com.databricks.spark.xml")
                .option("rowTag", "HarvestedProduction")
                .option("charset", "iso-8859-1")
                .option("treatEmptyValuesAsNulls", "true")
                .schema(schema)
                .load(this.hprPath+"/*")
                .filter(col("Machine.Stem.StemCoordinates").isNotNull())
                .filter(explode(col("Machine.Stem")).isNotNull())
                .filter(explode(col("Machine.SpeciesGroupDefinition")).isNotNull())
                .filter(col("Machine.ObjectDefinition.ObjectUserID.#VALUE").getItem(0).isNotNull())
                .filter(explode(col("Machine.Stem.StemCoordinates")).isNotNull());

        df = df.select(col("Machine.Stem"), col("Machine.ObjectDefinition"),  col("Machine.SpeciesGroupDefinition"))
        .filter(col("SpeciesGroupDefinition.BarkFunction").isNotNull())
        .filter(col("SpeciesGroupDefinition.SpeciesGroupKey").isNotNull())
        .filter(col("SpeciesGroupDefinition.ButtEndProfileExtrapolation").isNotNull())
        .filter(col("SpeciesGroupDefinition.DBHHeight").isNotNull())
        .filter(col("SpeciesGroupDefinition.Grades").isNotNull())
        .filter(col("SpeciesGroupDefinition.SpeciesGroupInfo").isNotNull())
        .filter(col("SpeciesGroupDefinition.SpeciesGroupKey").isNotNull())
        .filter(col("SpeciesGroupDefinition.SpeciesGroupModificationDate").isNotNull())
        .filter(col("SpeciesGroupDefinition.SpeciesGroupName").isNotNull())
        .filter(col("SpeciesGroupDefinition.SpeciesGroupPresentationOrder").isNotNull())
        .filter(col("SpeciesGroupDefinition.SpeciesGroupUserID").isNotNull())
        .filter(col("SpeciesGroupDefinition.SpeciesGroupVersion").isNotNull())
        .filter(col("SpeciesGroupDefinition.StemTypeDefinition").isNotNull())
                .select(col("Stem"), col("ObjectDefinition"),  col("SpeciesGroupDefinition")).filter(col("SpeciesGroupDefinition").isNotNull());
        // TODO: remove timer
        timer.start();
        df.cache().count();
        timer.stop();
        System.out.println("df.cache.count(): " + timer + " ns");

        try {
            worksites = df.select(col("ObjectDefinition.ObjectUserID").getItem(0).getField("#VALUE").as("ObjectUserID"), explode(col("Stem")).as("Stem")).distinct()
                    .filter(col("Stem.StemCoordinates").isNotNull())
                    .filter(col("Stem.SingleTreeProcessedStem.Log").getItem(0).getField("LogMeasurement").isNotNull())
                    .filter(col("ObjectUserID").isNotNull())
                    .filter(col("Stem.SingleTreeProcessedStem").isNotNull())
                    .groupBy(col("ObjectUserID").as("id"))
                    .agg(first("Stem.StemCoordinates.Latitude").getItem(0).getField("#VALUE").alias("latitude")
                            ,first("Stem.StemCoordinates.Longitude").getItem(0).getField("#VALUE").alias("longitude")
                            , count("Stem").as("stems")).cache();
            // TODO: remove timer
            timer.start();
            worksites.cache().count(); // Store worksites data in memory.
            timer.stop();
            System.out.println("worksites.cache.count(): " + timer + " ns");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public Long getWorksitesCount(){
        return null;
    }

    public RDD<String> getAllWorksites(){
        timer.start();
        RDD<String> json = worksites.toJSON();
        timer.stop();
        System.out.println("worksites.toJSON(): " + timer + " ns");
        return worksites.toJSON();
    }

    public DataFrame queryHPRData(ETLQuery ETLQuery){
        List<String> worksiteIDs = ETLQuery.getWorksites(); // Object array of worksite IDs

        // Filter data to only contain targeted worksites
        DataFrame results = df.select(col("ObjectDefinition.ObjectUserID.#VALUE").as("ObjectUserID"), explode(col("Stem")).as("Stem"), col("SpeciesGroupDefinition"))
                .distinct()
                //.select(explode(col("ObjectUserID")).as("ObjectUserID"), col("Stem"), col("SpeciesGroupDefinition"))
                .filter(col("Stem.StemCoordinates").getItem(0).isNotNull())
                .filter(col("ObjectUserID").isNotNull())
                .filter(col("Stem.SingleTreeProcessedStem").isNotNull())
                .filter(col("Stem.SingleTreeProcessedStem.Log").getItem(0).getField("LogMeasurement").isNotNull())
                .filter(col("ObjectUserID").getItem(0).isin(worksiteIDs.toArray()));

        // Parse selected data to columns
        List<Column> selCol = new ArrayList<>();
        selCol.add(col("Machine.ObjectDefinition.ObjectUserID.#VALUE"));
        selCol.add(col("Machine.Stem.StemCoordinates"));
        selCol.add(size(col("Machine.Stem")).as("Stem"));

        //TODO, use user defined queries
        DataFrame species = results.select(col("ObjectUserID")
                    , explode(col("SpeciesGroupDefinition")).as("SpeciesGroupDefinition"))
                .select(col("ObjectUserID").getItem(0).as("ID")
                        , col("SpeciesGroupDefinition.SpeciesGroupName")
                        , col("SpeciesGroupDefinition.SpeciesGroupKey").as("GroupKey"))
                .distinct();

        results = results.select(col("ObjectUserID").getItem(0).as("ObjectUserID")
                , col("Stem")
                , col("Stem.SingleTreeProcessedStem.DBH")
                , explode(col("Stem.SingleTreeProcessedStem.Log.LogVolume")).as("LogVolume")
                , col("Stem.SpeciesGroupKey"))
                .select(col("ObjectUserID"), col("Stem"), col("DBH"), explode(col("LogVolume")).as("LogVolume"), col("SpeciesGroupKey"))
                .where(col("LogVolume.@logVolumeCategory").equalTo("m3sub"))
                .groupBy(col("ObjectUserID").as("ObjectUserID"), col("Stem"))
                .agg(first("SpeciesGroupKey").as("SpeciesGroupKey")
                        , sum(col("Stem.SingleTreeProcessedStem.Log").getField("LogMeasurement").getField("LogLength").getItem(0)).as("Length")
                        , sum(col("LogVolume").getField("#VALUE")).as("Volume")
                        //, first(col("Log").getField("LogVolume").getField("@logVolumeCategory"))
                        , first("DBH").as("DBH"))
                .join(species, col("SpeciesGroupKey").equalTo(species.col("GroupKey"))
                        .and(col("ObjectUserID").equalTo(species.col("ID"))))
                .drop("SpeciesGroupKey")
                .drop("GroupKey")
                .drop("ObjectUserID")
                .drop("ID")
                .drop("Stem");

        return results;
    }
}
