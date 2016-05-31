package edu.miun.firespark.messages;

import java.util.Map;

/**
 * Created by Dwe on 2016-04-11.
 */
public class MLQuery implements Message{
    private String algorithm;
    private double splitRatio;
    private String label;
    private int seed;
    private Map<String, Object> parameters;
    private String evaluation;

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public double getSplitRatio() {
        return splitRatio;
    }

    public void setSplitRatio(double splitRatio) {
        this.splitRatio = splitRatio;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getSeed() {
        return seed;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public String getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(String evaluation) {
        this.evaluation = evaluation;
    }

    @Override
    public String toString(){
        return "Algorithm: " + algorithm
                + "\nNr of parameters: " + parameters.size()
                + "\nEvaluation: " + evaluation;
    }
}
