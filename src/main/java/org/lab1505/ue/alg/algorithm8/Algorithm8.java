package org.lab1505.ue.alg.algorithm8;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.opencsv.CSVReader;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.lab1505.ue.alg.algorithm6.TripEdge;
import org.lab1505.ue.fileutil.CsvGraphWriter;
import org.lab1505.ue.fileutil.FileDirectoryGenerator;

public class Algorithm8 {

    static final String VOLUME = "files/algorithm8/Volume.csv";
    static final String BASIC_NET = "files/algorithm8/lengthOthervolumeTraveltime.csv";
    static final String TRIP = "files/algorithm8/odpair.csv";

    /**
     * Structure of the input csv file should be:
     * (source_vertex,target_vertex,otherVolume,initialTraveltime,length), and the
     * first line is the header which will be omitted during processing.
     * 
     * @param url
     * @return the graph
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static SimpleDirectedWeightedGraph<String, NetEdge> readGraph(String url, String volumeUrl)
            throws FileNotFoundException, IOException {
        File file = new File(url);
        SimpleDirectedWeightedGraph<String, NetEdge> graph = new SimpleDirectedWeightedGraph<>(NetEdge.class);

        // Read length
        try (FileReader reader = new FileReader(file)) {
            try (CSVReader r = new CSVReader(reader)) {
                r.readNext();
                String[] line = null;
                while ((line = r.readNext()) != null) {
                    String initNode = line[0];
                    String endNode = line[1];
                    graph.addVertex(initNode);
                    graph.addVertex(endNode);
                    try {
                        double otherVolume = Double.parseDouble(line[2]);
                        double initialTraveltime = Double.parseDouble(line[3]);
                        double length = Double.parseDouble(line[4]);
                        if (otherVolume >= 0 && initialTraveltime >= 0 && length >= 0) {
                            NetEdge edge = new NetEdge(length, initialTraveltime, otherVolume);
                            graph.addEdge(initNode, endNode, edge);
                        }
                    } catch (NumberFormatException e) {
                        // Something went wrong with the argument of Double.parseDouble().
                        // Escape this edge if that exception happened.
                    }
                }
            }
        }

        // Change other volume and numLanes according to VOLUME
        try (FileReader reader = new FileReader(volumeUrl)) {
            try (CSVReader r = new CSVReader(reader)) {
                r.readNext();
                String[] line = null;
                while ((line = r.readNext()) != null) {
                    String initNode = line[0];
                    String endNode = line[1];
                    try {
                        double otherVolume = Double.parseDouble(line[3]);
                        int numLanes = Integer.parseInt(line[2]);
                        if (otherVolume >= 0) {
                            NetEdge edge = graph.getEdge(initNode, endNode);
                            if (edge != null) {
                                edge.otherVolume = otherVolume;
                                edge.numLanes = numLanes;
                            }
                        }
                    } catch (NumberFormatException e) {
                        // Something went wrong with the argument of Double.parseDouble().
                        // Escape this edge if that exception happened.
                    }
                }

            }
        }

        return graph;
    }

    /**
     * Structure of the input csv file should be: (source_vertex,target_vertex,...),
     * and the first line is the header which will be omitted during processing.
     * 
     * @param url
     * @return the odpair graph
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static SimpleDirectedGraph<String, TripEdge> readTrips(String url)
            throws FileNotFoundException, IOException {
        SimpleDirectedGraph<String, TripEdge> graph = new SimpleDirectedGraph<>(TripEdge.class);
        File file = new File(url);
        try (FileReader reader = new FileReader(file)) {
            try (CSVReader cr = new CSVReader(reader)) {
                cr.readNext();
                String[] line = null;
                while ((line = cr.readNext()) != null) {
                    String initNode = line[0];
                    String endNode = line[1];
                    graph.addVertex(initNode);
                    graph.addVertex(endNode);
                    if (!initNode.equals(endNode)) {
                        TripEdge edge = new TripEdge(initNode, endNode);
                        graph.addEdge(initNode, endNode, edge);
                    }
                }
            }
        }
        return graph;
    }

    static double computeTraveltime(double otherVolume, double taxiVolume, double numLanes, double length) {
        double volumeIn = computeVolumeIn(taxiVolume, otherVolume, numLanes);
        double traveltime;

        if (volumeIn < 785) {
            traveltime = 15 * length * (687.0 - Math.sqrt(157989 * 3 - 600 * volumeIn)) / (88 * volumeIn);
        } else {
            traveltime = 15 * length * Math.sqrt(150 * volumeIn - 116992.5) / (44 * 1570 - 44 * volumeIn);
        }
        return traveltime;
    }

    static double computeMarginalCost(double volumeIn, double length) {
        double marginalCost;
        if (volumeIn < 785) {
            double in1 = Math.sqrt(157989 - 200 * volumeIn - 229 * Math.sqrt(3));
            double in2 = 687 * in1 + 100 * volumeIn * Math.sqrt(3);
            double upper = 15 * length * in2;
            double lower = 88 * volumeIn * Math.sqrt(157989 - 200 * volumeIn);
            marginalCost = upper / lower;
        } else {
            double upper = 225 * length * volumeIn * (10 * volumeIn + 101);
            double lower = 88 * Math.pow(volumeIn - 1570, 2) * Math.sqrt(150 * volumeIn - 116992.5);
            marginalCost = upper / lower;
        }
        return marginalCost;
    }

    static double computeVolumeIn(double taxiVolume, double otherVolume, double numLanes) {
        double volumeIn = (taxiVolume + otherVolume) / numLanes;
        if (volumeIn > 1569) {
            volumeIn = 1569;
        }
        return volumeIn;
    }

    public static void run(){
        try {
            SimpleDirectedWeightedGraph<String,NetEdge> net = readGraph(BASIC_NET,VOLUME);
            SimpleDirectedGraph<String,TripEdge> trip = readTrips(TRIP);
            
            int n = 1;
            while(n<6){
                //Clear all the volume of taxi which will be loaded later
                for(NetEdge edge:net.edgeSet()){
                    edge.taxiVolume = 0;
                }

                //Find shortest paths of the trips, assign taxi volume
                DijkstraShortestPath<String,NetEdge> sp = new DijkstraShortestPath<>(net);
                for(TripEdge edge:trip.edgeSet()){
                    String o = edge.init;
                    String d = edge.end;
                    if(net.containsVertex(o)&&net.containsVertex(d)){
                        GraphPath<String,NetEdge> path = sp.getPath(edge.init, edge.end);
                        if(path!=null){
                            edge.leastCost = path.getWeight();
                            for(NetEdge e:path.getEdgeList()){
                                e.taxiVolume += 1;
                            }
                        }
                    }
                }
                
                //Update traveltime 
                for(NetEdge edge:net.edgeSet()){
                    double newTraveltime = computeTraveltime(edge.otherVolume, edge.taxiVolume, edge.numLanes,edge.length);
                    edge.traveltime = newTraveltime;
                }


                //Calculate all surcharge and update weight of the edges
                for(NetEdge edge:net.edgeSet()){
                    double volumeIn = computeVolumeIn(edge.taxiVolume, edge.otherVolume, edge.numLanes);
                    double marginalCost = computeMarginalCost(volumeIn, edge.length);
                    double surcharge = (1.0/n)*marginalCost + (1-(1.0/n))*edge.surchargePool.getRecentSurcharge();
                    edge.surchargePool.add(surcharge);
                    double cost = surcharge + edge.traveltime;
                    net.setEdgeWeight(edge, cost);
                }

                CsvGraphWriter.writeTo(net, NetEdge.class, FileDirectoryGenerator.createFileAutoRename("a8_net", "csv"));
                CsvGraphWriter.writeTo(trip, TripEdge.class, FileDirectoryGenerator.createFileAutoRename("a8_trip", "csv"));
                n ++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}