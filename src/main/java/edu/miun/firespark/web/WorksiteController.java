package edu.miun.firespark.web;

import edu.miun.firespark.service.HarvestProductionReportRepository;
import org.apache.spark.rdd.RDD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.json.JSONArray;

import java.util.*;

/**
 * Created by Dwe on 2016-03-03.
 */


@RestController
public class WorksiteController {

    @Autowired
    private HarvestProductionReportRepository hprAdepter;

    @RequestMapping( value = "/worksites/count", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getWorksitesCount() {
        Map<String, Object> map = new HashMap<String, Object>();
        long count = hprAdepter.getStemsCount();
        map.put("count", count);
        return new ResponseEntity<Map<String, Object>>(map, HttpStatus.OK);
    }

    @RequestMapping( value = "/worksites/listAll", method = RequestMethod.GET)
    public ResponseEntity<JSONArray> getWorksitesAndCoordinates() {
        RDD<String> worksites =  hprAdepter.getAllWorksites();

        String[] list = (String[])worksites.collect();//take(10);

        JSONArray mJSONArray = new JSONArray(Arrays.asList(list));
        if(list.length == 0){
            return new ResponseEntity<JSONArray>(HttpStatus.NO_CONTENT);// Return HttpStatus.NOT_FOUND
        }
        return new ResponseEntity<JSONArray>(mJSONArray, HttpStatus.OK);
    }
}
