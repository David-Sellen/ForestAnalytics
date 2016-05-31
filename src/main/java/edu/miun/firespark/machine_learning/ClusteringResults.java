package edu.miun.firespark.machine_learning;

import org.apache.spark.sql.Row;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Dwe on 2016-04-25.
 */
public class ClusteringResults implements MLResults{
    int k = 0;
    int instances = 0;
    List<String> labels = new ArrayList<>();
    boolean isLabeled = false;
    List<ClusterData> clusters = new ArrayList<>();
    int[] clusterInstances;

    public ClusteringResults(int k, Row[] clusterMatrix){
        this.k = k;
        clusterInstances = new int[k];
        if(clusterMatrix[0].size() == 3){
            isLabeled = true;
        }
        Map<Integer, Map<String, Integer>> clusterData = new HashMap<>();
        for(int i = 0; i<clusterMatrix.length; i++){
            String label = "";
            int clusterID;
            int count;

            if(isLabeled) {
                label = clusterMatrix[i].getString(0);
                clusterID = clusterMatrix[i].getInt(1);
                count = (int) clusterMatrix[i].getLong(2);

                if(!labels.contains(label))labels.add(label); //Get all unique labels
                // Count instaces of each label
                clusterData.putIfAbsent(clusterID, new HashMap<>());
                if(clusterData.get(clusterID).containsKey(label)){
                    int current = clusterData.get(clusterID).get(label);
                    clusterData.get(clusterID).put(label, count+current);
                } else {
                    clusterData.get(clusterID).put(label, count);
                }
            } else {
                clusterID = clusterMatrix[i].getInt(0);
                count = (int) clusterMatrix[i].getLong(1);
            }
            instances += count;
            clusterInstances[clusterID] += count;

        }
        for(int i=0; i<k; i++) {
            clusters.add(new ClusterData(i, labels, clusterInstances[i], clusterData.get(i)));
        }
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
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

    public List<ClusterData> getClusters() {
        return clusters;
    }

    public void setClusters(List<ClusterData> clusters) {
        this.clusters = clusters;
    }

    public ClusterData getCluster(int index){
        return clusters.get(index);
    }

}
