package edu.miun.firespark.web;

import edu.miun.firespark.service.HarvestProductionReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Dwe on 2016-03-15.
 */

@RestController
public class MachineLearningController {

        @Autowired
        private HarvestProductionReportRepository hprAdepter;

        /*@RequestMapping( value = "/machinelearning/algorithms", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<RDD<String>> getAlgorithms() {
            RDD<String> worksites =  hprAdepter.getAllWorksites();
            if(worksites.isEmpty()){
                return new ResponseEntity<RDD<String>>(HttpStatus.NO_CONTENT);//You many decide to return HttpStatus.NOT_FOUND
            }
            return new ResponseEntity<RDD<String>>(worksites, HttpStatus.OK);
        }*/
}
