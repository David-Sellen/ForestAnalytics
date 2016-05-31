package edu.miun.firespark.machine_learning;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.spark.sql.DataFrame;

import java.util.Arrays;

/**
 * Created by Dwe on 2016-03-03.
 */
public class Dataset {
    private String name;
    private long instances;
    //private List<Attribute> attributes;
    String[] attributes;
    private DataFrame df;

    public Dataset(String name, DataFrame df){
        this.name = name;
        this.df = df;
        this.instances = df.cache().count();
        this.attributes = this.df.columns();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getInstances() {
        return instances;
    }

    public void setInstances(long instances) {
        this.instances = instances;
    }

    public String[] getAttributes() {
        return attributes;
    }

    public void setAttributes(String[] attributes) {
        this.attributes = attributes;
    }

    @JsonIgnore
    public DataFrame getDf() {
        return df;
    }

    public void setDf(DataFrame df) {
        this.df = df;
    }

    @Override
    public String toString() {
        String string = "Name: " + name + "\nInstances: " + instances + "\nAttributes: " +  Arrays.toString(attributes);
        return string;
    }
}
