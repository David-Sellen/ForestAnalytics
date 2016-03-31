package edu.miun.firespark.service;

import java.util.ArrayList;

/**
 * Created by Dwe on 2016-03-15.
 */
public class MachineLearningService {
    private ArrayList<String> algorithms;

    public MachineLearningService(){
        algorithms = new ArrayList<String>();
        algorithms.add("K-Means");
        algorithms.add("C4.5");
        algorithms.add("Linear Regression");
    }

    public ArrayList<String> getAlgorithms(){
        return algorithms;
    }

}
