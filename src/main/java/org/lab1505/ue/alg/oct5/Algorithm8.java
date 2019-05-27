package org.lab1505.ue.alg.oct5;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.opencsv.CSVReader;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.lab1505.ue.fileutil.CsvGraphWriter;
import org.lab1505.ue.fileutil.FileDirectoryGenerator;

public class Algorithm8 {

    public static final String NET_5 = "files/oct5/new/links_5pm.csv";
    public static final String NET_8 = "files/oct5/new/links_8am.csv";
    public static final String TRIPS_5 = "files/oct5/trips_5pm.csv";
    public static final String TRIPS_8 = "files/oct5/trips_8am.csv";

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
    public static SimpleDirectedWeightedGraph<String, NetEdge> readGraph(String url)
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
                        double otherVolume = Double.parseDouble(line[5]);
                        double initialTraveltime = Double.parseDouble(line[2]);
                        double length = Double.parseDouble(line[3]);
                        int numlanes = Integer.parseInt(line[4]);
                        if (otherVolume >= 0 && initialTraveltime >= 0 && length >= 0) {
                            NetEdge edge = new NetEdge(length, initialTraveltime, otherVolume);
                            edge.numLanes = numlanes;
                            graph.addEdge(initNode, endNode, edge);
                        }
                    } catch (NumberFormatException e) {
                        // Something went wrong with the argument of Double.parseDouble().
                        // Escape this edge if that exception happened.
                    } catch (IllegalArgumentException e2){
                        //A loop is formed, which is not supposed to happen in a simple graph
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

    static int getTotalLinks( SimpleDirectedGraph<String,TripEdge> trip){
        int sum = 0;
        for(TripEdge e:trip.edgeSet()){
            sum += e.numLinks;
        }
        return sum;
    }

    static double getTotalTaxiVolume(SimpleDirectedWeightedGraph<String,NetEdge> net){
        double sum = 0;
        for(NetEdge e:net.edgeSet()){
            sum += e.taxiVolume;
        }
        return sum;
    }

    static double getTotalCost(SimpleDirectedGraph<String,TripEdge> trip){
        double sum = 0;
        for(TripEdge e:trip.edgeSet()){
            sum += e.leastCost;
        }
        return sum;
    }

    public static void run(String linkurl,String tripurl,String prefix){
        try {
            SimpleDirectedWeightedGraph<String,NetEdge> net = readGraph(linkurl);
            SimpleDirectedGraph<String,TripEdge> trip = readTrips(tripurl);
            
            for(NetEdge e:net.edgeSet()){
                net.setEdgeWeight(e, e.initialTraveltime);
            }
            
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
                            edge.numLinks = path.getLength();
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

                System.out.println("Total links: "+getTotalLinks(trip));
                System.out.println("Total taxi volume: "+getTotalTaxiVolume(net));
                System.out.println("Total cost: "+getTotalCost(trip));

                CsvGraphWriter.writeTo(net, NetEdge.class, FileDirectoryGenerator.createFileAutoRename(prefix+"net", "csv"));
                CsvGraphWriter.writeTo(trip, TripEdge.class, FileDirectoryGenerator.createFileAutoRename(prefix+"trip", "csv"));
                n ++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}