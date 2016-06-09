# ForestAnalytics
ForestAnalytics is a system developed for analysing data from the forest industry i.e. files from the [StanForD 2010](http://www.skogforsk.se/english/projects/stanford/) standard. Built on cloud technologies such as Apache Spark and Hadoop, the system enables processing of large amounts of forest data. Currently the system supports the harvest production reports (.hpr files) form the StanForD standard. 

## Architecture
The system consists of a back-end and front-end. The back-end communicates with a computer cluster running Spark and Hadoop. The front-end is designed as a Single Page Application (SPA). Communication between front- and back-end is trough the back-ends REST API.

## Requirements
- Apache Spark 1.6.1
- Hadoop Common 2.2

## Features
- Local or distributed mode
- Load and filter HPR data
- Display harvest worksites with name and the number of harvested stems
- Extract stem data to create datasets
- Store and load datasets as Parquet files
- Apply machine learning methods (currently only supports K-Means||)
- Visualize machine learning results

## Try the system
To compile and run the system, first change the values in the [application.properties](src/main/resources/application.properties) file.
- `mode`: Set to local to run on a local machine or to distributed to connect to a computer cluster.
- `storage.prefix`: The prefix for the type of storage, i.e. for Hadoop, use "hdfs://[IP to Hadoop cluster]:8020" or for local storage use "file:///".
- `spark.master`: Set this to "Spark://[IP to Spark master node]:7077.
- `location.dataset`: The path to the datasets location.
- `location.hpr`: The path to the HPR files location.

When starting the system it will first run Spring Boot and its embedded Tomcat server then it will start Spark and load filtered HPR data into the Spark worker nodes RAM.
