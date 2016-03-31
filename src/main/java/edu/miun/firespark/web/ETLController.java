package edu.miun.firespark.web;

import edu.miun.firespark.service.DataExtractionService;
import edu.miun.firespark.service.HarvestProductionReportRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by Dwe on 2016-03-29.
 */

@RestController
public class ETLController {

    // Adapters to datasources
    @Autowired
    private HarvestProductionReportRepository hprAdepter;

    @RequestMapping( value = "/dataset/create", method = RequestMethod.POST)
    public ResponseEntity<String> createDataset(@RequestBody Map<String, String> query) {
        // Parse
        String datasetID = generateDataset(query);
        if(datasetID.isEmpty()){
            return new ResponseEntity<String>(HttpStatus.NO_CONTENT);// Return HttpStatus.NOT_FOUND
        }
        return new ResponseEntity<String>(datasetID, HttpStatus.OK);
    }

    private String generateDataset(Map<String, String> query){

        return (new DataExtractionService()).createDataset(query);
    }
}
