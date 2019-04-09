package org.lab1505.ue.alg.algorithm6;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.opencsv.CSVReader;

import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.lab1505.ue.entity.SurchargePool;

public class NetEdge {
    double otherVolume;
    double taxiVolume;
    double initialTraveltime;
    double traveltime;
    double length;
    SurchargePool surchargePool;

    public NetEdge(double length, double initialTraveltime, double othervolume) {
        this.otherVolume = othervolume;
        this.taxiVolume = 0;
        this.initialTraveltime = initialTraveltime;
        this.traveltime = initialTraveltime;
        this.surchargePool = new SurchargePool();
        this.length = length;
    }

    public static SimpleDirectedWeightedGraph<String, NetEdge> readGraphFromFiles(String ovurl, String iturl,
            String lengthurl) throws IOException {
        SimpleDirectedWeightedGraph<String, NetEdge> graph = new SimpleDirectedWeightedGraph<>(NetEdge.class);

        File file = new File(ovurl);
        String abspath = file.getAbsolutePath();
        System.out.println(abspath);
        FileReader fileReader = new FileReader(file);
        CSVReader csvReader = new CSVReader(fileReader);
        csvReader.readNext();
        String[] line = null;
        while ((line = csvReader.readNext()) != null) {
            String initNode = line[0];
            String endNode = line[1];
            try{
                double otherVolume = Double.parseDouble(line[2]);
                graph.addVertex(initNode);
                graph.addVertex(endNode);
                graph.addEdge(initNode, endNode, new NetEdge(-1, -1, otherVolume));
            }catch(NumberFormatException e){
                e.printStackTrace();
            }
        }
        fileReader.close();
        csvReader.close();

        file = new File(iturl);
        fileReader = new FileReader(file);
        csvReader = new CSVReader(fileReader);
        csvReader.readNext();
        while ((line = csvReader.readNext()) != null) {
            String initNode = line[0];
            String endNode = line[1];
            try{
                double initialTraveltime = Double.parseDouble(line[2]);
                if (graph.containsEdge(initNode, endNode)) {
                    NetEdge e = graph.getEdge(initNode, endNode);
                    e.initialTraveltime = initialTraveltime;
                    e.traveltime = initialTraveltime;
                }
            }catch(NumberFormatException e){
                e.printStackTrace();
            }

        }
        fileReader.close();
        csvReader.close();

        file = new File(lengthurl);
        fileReader = new FileReader(file);
        csvReader = new CSVReader(fileReader);
        csvReader.readNext();
        while ((line = csvReader.readNext()) != null) {
            String initNode = line[0];
            String endNode = line[1];
            try{
                double length = Double.parseDouble(line[2]);
                if (graph.containsEdge(initNode, endNode)) {
                    NetEdge e = graph.getEdge(initNode, endNode);
                    e.length = length;
                }
            }catch(NumberFormatException e){
                e.printStackTrace();
            }

        }
        fileReader.close();
        csvReader.close();

        return graph;
    }
}