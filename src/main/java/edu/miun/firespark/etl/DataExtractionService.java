package edu.miun.firespark.etl;

import edu.miun.firespark.messages.ETLQuery;
import org.apache.spark.sql.DataFrame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Dwe on 2016-03-30.
 */

@Scope("singleton")
@Component
public class DataExtractionService {

    // Adapters to datasources
    @Autowired
    private HarvestProductionReportRepository hprAdapter;

    private Map<String, String> sourceEquivalent = new HashMap<>();

    @PostConstruct
    public void init(){
        sourceEquivalent.put("hprRoot", "HPR");
        sourceEquivalent.put("stem", "Machine.Stem.StemKey");
        sourceEquivalent.put("length", "Machine.Stem.SingleTreeProcessedStem");
        sourceEquivalent.put("coordinates", "Machine.Stem.StemCoordinates");
        sourceEquivalent.put("dbh", "Machine.Stem.SingleTreeProcessedStem.DBH");
    }

    public DataFrame createDataset(final ETLQuery ETLQuery){
        ETLQuery parsedQuery = parseQuery(ETLQuery);
        DataFrame results = null;

        switch(ETLQuery.getGroup()){
            // TODO Combine different data sources here... tree dimensions, weather, wood quality etc.
            case "worksite":
                System.out.println("ETL worksite data");
                results = hprAdapter.queryHPRData(parsedQuery);
                System.out.println("Created dataset: " + ETLQuery.getName());
                break;
            case "stem":
                System.out.println("ETL tree data");
                results = hprAdapter.queryHPRData(parsedQuery);
                System.out.println("Created dataset: " + ETLQuery.getName());
                break;
        }
        return results;
    }


    private ETLQuery parseQuery(ETLQuery ETLQuery){
        ETLQuery parsedETLQuery = ETLQuery;
        List<String> features = new ArrayList<>();
        for(String feature : ETLQuery.getFeatures()){
            if(sourceEquivalent.get(feature) != null){
                if(feature == sourceEquivalent.get("coordinates")){
                    features.add(sourceEquivalent.get(feature +".Latitude.#VALUE"));
                    features.add(sourceEquivalent.get(feature +".Longitude.#VALUE"));
                } else {
                    features.add(sourceEquivalent.get(feature));
                }
            }
        }
        parsedETLQuery.setFeatures(features);
        return ETLQuery;
    }

    private ETLQuery parseStemQuery(){
        return null;
    }

}
