package edu.miun.firespark.domain;

/**
 * Created by Dwe on 2016-02-28.
 */


public class WeatherDAO {
    private static WeatherDAO singletonWA = new WeatherDAO();
    private String category = "mesan1g";
    private String version = "1";
    private String URL = "http://opendata-download-metfcst.smhi.se/api/category/"+category+"/version"+version+"/";
    private String test ="geotype/point/lon/16/lat/58/data.json";



    private int temp;

    private WeatherDAO(){
    }

    public static WeatherDAO getInstance(){
        if(singletonWA == null){
            singletonWA = new WeatherDAO();
        }
        return singletonWA;
    }


    public int getTemperature(int value){
        System.out.println(temp);
        int dayAvgHumidity = value;
        return dayAvgHumidity;
    }

    int getDayAvgHumidity(){
        int dayAvgHumidity = 0;
        return dayAvgHumidity;
    }

    int getWeekAvgHumidity(){
        int dayAvgHumidity = 0;
        return dayAvgHumidity;
    }

    int getMonthAvgHumidity(){
        int dayAvgHumidity = 0;
        return dayAvgHumidity;
    }

    int getYearAvgHumidity(){
        int dayAvgHumidity = 0;
        return dayAvgHumidity;
    }


}
