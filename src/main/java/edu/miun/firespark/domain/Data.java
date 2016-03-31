package edu.miun.firespark.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by Dwe on 2016-03-03.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Data implements Serializable {
    private String validTime;
    private String t; // Air temperature
    private String r; // Relative humidity

    public String getValidTime() {
        return validTime;
    }

    public void setValidTime(String validTime) {
        this.validTime = validTime;
    }

    public String getT() {
        return t;
    }

    public void setT(String t) {
        this.t = t;
    }

    public String getR() {
        return r;
    }

    public void setR(String r) {
        this.r = r;
    }
}
