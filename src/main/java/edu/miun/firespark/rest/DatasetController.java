package edu.miun.firespark.rest;

import edu.miun.firespark.messages.ETLQuery;
import edu.miun.firespark.machine_learning.Dataset;
import edu.miun.firespark.etl.DataExtractionService;
import edu.miun.firespark.etl.DatasetRepository;
import edu.miun.firespark.etl.HarvestProductionReportRepository;
import edu.miun.firespark.utilities.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

/**
 * Created by Dwe on 2016-03-29.
 */

@RestController
public class DatasetController {

    @Autowired
    private HarvestProductionReportRepository hprAdepter; //Adapters to datasources
    @Autowired
    DataExtractionService deService;        //Extract, transform and load from raw data
    @Autowired
    DatasetRepository datasetRepository;    //Dataset repository
    @Autowired
    private HttpSession httpSession;        //Client http session

    Dataset workingDataset;

    // Creates a dataset form raw data, load it into RAM and store it as a parquet file.
    @RequestMapping( value = "/dataset/create", method = RequestMethod.POST)
    public ResponseEntity<String> createDataset(@RequestBody ETLQuery etlQuery) {
        // If the dataset is already loaded, return
        workingDataset = (Dataset)httpSession.getAttribute("dataset");
        if(workingDataset != null){
            if( workingDataset.getName().equals(etlQuery.getName())){
                return new ResponseEntity<String>("{\"name\":" + workingDataset.getName() + "}", HttpStatus.OK);
            }
            workingDataset.getDf().unpersist(); // Release the dataset from cache.
        }

        // TODO: remove timer
        Timer timer = new Timer();
        timer.start();
        workingDataset = new Dataset(etlQuery.getName(), deService.createDataset(etlQuery));
        timer.stop();
        System.out.println("create.dataset.and.cache: " + timer + " ns");

        if(workingDataset.getDf() == null){
            return new ResponseEntity<String>(HttpStatus.NO_CONTENT);
        }

        //Save dataset to storage
        datasetRepository.saveDataset(workingDataset);

        //Set the dataset to the user session
        httpSession.setAttribute("dataset", workingDataset);
        String jsonResponse = "{\"name\":" + workingDataset.getName() +
                ", \"instances\":\"" + workingDataset.getInstances() + "}";

        return new ResponseEntity<String>("{\"name\":" + workingDataset.getName() + "}", HttpStatus.OK);
    }

    @RequestMapping( value = "/dataset/load/{name}", method = RequestMethod.POST)
    public ResponseEntity<Dataset> loadDataset(@PathVariable String name, @RequestBody String payload) {
        // If the dataset is already loaded, return
        workingDataset = (Dataset)httpSession.getAttribute("dataset");
        if(workingDataset != null){
            if( workingDataset.getName().equals(name)){
                return new ResponseEntity<Dataset>(workingDataset, HttpStatus.OK);
            }
            workingDataset.getDf().unpersist(); // Release the dataset from cache.
        }

        //Load a dataset by its name.
        System.out.println("Load dataset: " + name);
        workingDataset = datasetRepository.loadDataset(name);

        //Set the dataset to the user session
        httpSession.setAttribute("dataset", workingDataset);

        System.out.println("Loaded: \n" + workingDataset);
        if(workingDataset == null){
            return new ResponseEntity<Dataset>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<Dataset>(workingDataset, HttpStatus.OK);
    }

    //TODO: implement save method.
    /*
    @RequestMapping( value = "/dataset/save/{name}", method = RequestMethod.POST)
    public ResponseEntity<String> createDataset(@RequestBody ETLQuery query) {
        //Query raw data to create a new dataset
        workingDataset = deService.createDataset(query);
        if(workingDataset == null){
            return new ResponseEntity<String>(HttpStatus.NO_CONTENT);D
        }
        return new ResponseEntity<String>("{\"dataset\":" + query.getName() + "}", HttpStatus.OK);
    }*/

    //Get a list of stored datasets.
    @RequestMapping( value = "/dataset/list", method = RequestMethod.GET)
    public ResponseEntity<String[]> getAllDatasets() {
        String[] datasets = datasetRepository.getListOfDatasets();
        if(datasets == null ||datasets.length == 0){
            return new ResponseEntity<String[]>(HttpStatus.NO_CONTENT); //Return HttpStatus.NOT_FOUND
        }
        // Remove file extension form file names.
        for(int i=0; i<datasets.length; i++){
            datasets[i] = datasets[i].replace(".parquet", "");
        }
        return new ResponseEntity<String[]>(datasets, HttpStatus.OK);
    }
}
