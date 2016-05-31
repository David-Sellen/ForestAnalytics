package edu.miun.firespark.machine_learning;

import java.util.List;
import java.util.Map;

/**
 * Created by Dwe on 2016-04-25.
 */
public class ClusterData {
    int id;
    int instances;
    List<String> labels;
    Map<String, Integer> instancesWithLabel;
    double[] centroid;

    public ClusterData(int id, List<String> labels, int instances, Map<String, Integer> instancesWithLabel){
        this.id = id;
        this.instancesWithLabel = instancesWithLabel;
        this.labels = labels;

        this.instances = instances;

        if(labels != null && !labels.isEmpty())
        for(String label : labels){
            if(instancesWithLabel != null && instancesWithLabel.containsKey(label)){
                int count = instancesWithLabel.get(label);
                this.instancesWithLabel.put(label, count);
            } else {
                this.instancesWithLabel.put(label, 0);
            }
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getInstances() {
        return instances;
    }

    public void setInstances(int instances) {
        this.instances = instances;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public Map<String, Integer> getInstancesWithLabel() {
        return instancesWithLabel;
    }

    public void setInstancesWithLabel(Map<String, Integer> instancesWithLabel) {
        this.instancesWithLabel = instancesWithLabel;
    }

    public double[] getCentroid() {
        return centroid;
    }

    public void setCentroid(double[] centroid) {
        this.centroid = centroid;
    }
}
