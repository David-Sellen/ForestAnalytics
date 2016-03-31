package edu.miun.firespark.service;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Created by Dwe on 2016-03-30.
 */


public class DataExtractionService {

    // Adapters to datasources
    @Autowired
    private HarvestProductionReportRepository hprAdepter;


    public String createDataset(final Map<String, String> query){
        final String worksite = new String("worksite");
        final Map<String, String> input = query;

        switch(input.get("group")){
            case "worksite":
                System.out.println("WORKSITE CASE");
                return "{\"OK\":true}";
            case "stem":
                System.out.println("STEM CASE");
                return "{\"OK\":true}";
            case "log":
                System.out.println("LOG CASE");
                return "{\"OK\":true}";
            default:
                System.out.println("DEFAULT CASE");
                return "{\"ERR\":false}";
        }
    }


}
