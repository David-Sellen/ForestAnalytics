package edu.miun.firespark.machine_learning;

import edu.miun.firespark.utilities.SparkContextBean;
import org.apache.commons.lang.ArrayUtils;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.VectorUDT;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.catalyst.expressions.GenericRow;
import org.apache.spark.sql.types.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Dwe on 2016-03-15.
 */


@Scope("singleton")
@Component
public class MachineLearningService {
    private ArrayList<String> algorithms;

    @Autowired
    SparkContextBean spc;
    static String label;
    static int sgnIndex = -1;
    Broadcast<Integer> brLabelIndex;
    Broadcast<Integer> attrLength;
    Broadcast<String[]> brSpecies;

    String[] species = {"gran", "tall", "björk", "löv", "contorta"};

    public MachineLearningService(){
        algorithms = new ArrayList<String>();
        algorithms.add("K-Means");
        algorithms.add("C4.5");
    }

    public ArrayList<String> getAlgorithms(){
        return algorithms;
    }

    public DataFrame[] prepareTrainAndTestDF(DataFrame df, double split,int seed, String label){
        this.label = label;
        //Prepare dataset for running ML
        StructField[] fields = {new StructField("label", DataTypes.StringType, false, Metadata.empty()),
                                new StructField("features", new VectorUDT(), false, Metadata.empty())};
        StructType schema = new StructType(fields);

        int sgnIndex = Arrays.asList(df.columns()).indexOf(label);
        brLabelIndex = spc.getContext().broadcast(sgnIndex);
        attrLength = spc.getContext().broadcast((sgnIndex != -1) ? df.columns().length : df.columns().length-1);
        brSpecies = spc.getContext().broadcast(species);

        df.show();
        //Parse dataset to list of doubles and a label
        JavaRDD<Row> points = df.toJavaRDD().map(new ParseRow(brLabelIndex, attrLength, brSpecies));
        DataFrame dataset = spc.getSQLContext().createDataFrame(points, schema);

        //Split dataset to train and test
        double trainPercentage = split;
        double testPercentage = 1 - trainPercentage;
        DataFrame[] splits = dataset.randomSplit(new double[] {trainPercentage, testPercentage}, seed);

        return splits;
    }

    public DataFrame prepareAllTrainDF(DataFrame df, String label){
        this.label = label;
        //Prepare dataset for running ML
        StructField[] fields = {new StructField("label", DataTypes.StringType, false, Metadata.empty()),
                new StructField("features", new VectorUDT(), false, Metadata.empty())};
        StructType schema = new StructType(fields);

        int sgnIndex = Arrays.asList(df.columns()).indexOf(label);
        brLabelIndex = spc.getContext().broadcast(sgnIndex);
        attrLength = spc.getContext().broadcast((sgnIndex != -1) ? df.columns().length : df.columns().length-1);
        brSpecies = spc.getContext().broadcast(species);

        //Parse dataset to list of doubles and a label
        JavaRDD<Row> points = df.toJavaRDD().map(new ParseRow(brLabelIndex, attrLength, brSpecies));

        DataFrame dataset = spc.getSQLContext().createDataFrame(points, schema);

        return dataset;
    }

    private static final class ParseRow implements Function<Row, Row> {
        Broadcast<Integer> labelIndex;
        Broadcast<Integer> attrLength;
        Broadcast<String[]> speciesList;

        ParseRow(Broadcast brLabelIndex,Broadcast atLength, Broadcast brSpecies){
            labelIndex = brLabelIndex;
            attrLength = atLength;
            speciesList = brSpecies;
        }

        @Override
        public Row call(Row r) {
            //double[] point = new double[r.length()];
            List<Double> list = new ArrayList<>();
            String lab = "";

            for (int i = 0; i < r.length(); ++i) {
                if(i == labelIndex.value()) {
                    String lb = r.getString(i).toLowerCase();
                    for(String species : speciesList.value()){
                        if(lb.contains(species) || lb.equals(species)){
                            lab = species;
                            break;
                        } else {
                            lab = "unknown";
                        }
                    }
                }
                else if(r.get(i)instanceof Long){
                    list.add( new Long(r.getLong(i)).doubleValue() );
                } else if(r.get(i)instanceof Double){
                    list.add(r.getDouble(i));
                } else if(r.get(i) == null){
                    list.add(0.0);
                }
            }
            double[] point = ArrayUtils.toPrimitive(list.toArray(new Double[list.size()]));
            Vector[] points = {Vectors.dense(point)};
            Object[] values = {lab, Vectors.dense(point)};
            return new GenericRow(values);
        }
    }

}
