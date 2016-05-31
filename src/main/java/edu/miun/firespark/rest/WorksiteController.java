package edu.miun.firespark.rest;

import edu.miun.firespark.etl.HarvestProductionReportRepository;
import org.apache.spark.rdd.RDD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.json.JSONArray;

import java.util.*;

/**
 * Created by Dwe on 2016-03-03.
 */

// Load information from raw data.

@RestController
public class WorksiteController {

    @Autowired
    private HarvestProductionReportRepository hprAdapter;

    @RequestMapping( value = "/worksites/listAll", method = RequestMethod.GET)
    public ResponseEntity<JSONArray> getWorksitesAndCoordinates() {
        RDD<String> worksites =  hprAdapter.getAllWorksites();

        String[] list = (String[])worksites.collect();//Get worksites data

        JSONArray mJSONArray = new JSONArray(Arrays.asList(list));
        if(list.length == 0){
            return new ResponseEntity<JSONArray>(HttpStatus.NO_CONTENT);// Return HttpStatus.NOT_FOUND
        }
        return new ResponseEntity<JSONArray>(mJSONArray, HttpStatus.OK);
    }
}
