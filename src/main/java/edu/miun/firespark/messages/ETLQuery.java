package edu.miun.firespark.messages;

import java.util.List;

/**
 * Created by Dwe on 2016-04-05.
 */
public class ETLQuery implements Message{
    private String name;
    private List<String> worksites;
    private List<String> features;
    private String group;
    private String type;

    public String getName() {
        return name;
    }
 
    public void setName(String name) {
        this.name = name;
    }

    public List<String> getWorksites() {
        return worksites;
    }

    public void setWorksites(List<String> worksites) {
        this.worksites = worksites;
    }

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString(){
        return "Dataset: " + name + "\nNr of worksites: " + worksites.size()
                + "\nGroup: " + group + "\nType: " + type
                + "\nWorksites: " + worksites + "\nFeatures: " + features;
    }

}
