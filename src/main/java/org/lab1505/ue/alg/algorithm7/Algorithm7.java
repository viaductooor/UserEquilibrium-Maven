package org.lab1505.ue.alg.algorithm7;

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

public class Algorithm7 {
    public static final String VOLUME_1 = "files/algorithm61/volume1.csv";
    public static final String VOLUME_2 = "files/algorithm61/Link Volume2.csv";
    public static final String NUM_LANES = "files/algorithm61/Number of Lanes.csv";

    /**
     * Structure of the input csv file should be:
     * (source_vertex,target_vertex,otherVolume,initialTraveltime,length),
     * and the first line is the header which will be omitted during processing.
     * @param url
     * @return the graph
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static SimpleDirectedWeightedGraph<String, NetEdge> readGraph(String url,String volumeUrl) throws FileNotFoundException,IOException{
        File file = new File(url);
        SimpleDirectedWeightedGraph<String,NetEdge> graph = new SimpleDirectedWeightedGraph<>(NetEdge.class);
        
        // Read length
        try(FileReader reader = new FileReader(file)){
            try(CSVReader r = new CSVReader(reader)){
                r.readNext();
                String[] line = null;
                while((line=r.readNext())!=null){
                    String initNode = line[0];
                    String endNode = line[1];
                    graph.addVertex(initNode);
                    graph.addVertex(endNode);
                    try{
                        double otherVolume = Double.parseDouble(line[2]);
                        double initialTraveltime = Double.parseDouble(line[3]);
                        double length = Double.parseDouble(line[4]);
                        if(otherVolume>=0&&initialTraveltime>=0&&length>=0){
                            NetEdge edge = new NetEdge(length,initialTraveltime,otherVolume);
                            graph.addEdge(initNode, endNode,edge);
                        }
                    }catch(NumberFormatException e){
                        // Something went wrong with the argument of Double.parseDouble().
                        // Escape this edge if that exception happened.
                    }
                }
            }
        }
        
        // Change other volume according to VOLUME_1 or VOLUME_2
        try(FileReader reader = new FileReader(volumeUrl)){
            try(CSVReader r = new CSVReader(reader)){
                r.readNext();
                String[] line = null;
                while((line=r.readNext())!=null){
                    String initNode = line[0];
                    String endNode = line[1];
                    try{
                        double otherVolume = Double.parseDouble(line[2]);
                        if(otherVolume>=0){
                            NetEdge edge = graph.getEdge(initNode, endNode);
                            if(edge!=null){
                                edge.otherVolume = otherVolume;
                            }
                        }
                    }catch(NumberFormatException e){
                        // Something went wrong with the argument of Double.parseDouble().
                        // Escape this edge if that exception happened.
                    }
                }

            }
        }

        //Change number of lanes according to NUM_LANES.
        try(FileReader reader = new FileReader(NUM_LANES);){
            try(CSVReader r = new CSVReader(reader)){
                r.readNext();
                String[] line = null;
                while((line=r.readNext())!=null){
                    String initNode = line[0];
                    String endNode = line[1];
                    try{
                        int numLanes = Integer.parseInt(line[2]);
                        if(numLanes>=0){
                            NetEdge edge = graph.getEdge(initNode, endNode);
                            if(edge!=null){
                                edge.numLanes = numLanes;
                            }
                        }
                    }catch(NumberFormatException e){
                        // Something went wrong with the argument of Double.parseDouble().
                        // Escape this edge if that exception happened.
                    }
                }
            }
        }

        // Update initialDensity, density
        for(NetEdge e:graph.edgeSet()){
            double k = (1760.0/3600)*e.otherVolume*e.traveltime/(e.length*e.numLanes);
            if(k>113){
                k = 113;
            }
            e.initialDensity = k;
            e.density = e.initialDensity;
        }
        
        return graph;
    }

    /**
     * Structure of the input csv file should be:
     * (source_vertex,target_vertex,...),
     * and the first line is the header which will be omitted during processing.
     * @param url 
     * @return the odpair graph
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static SimpleDirectedGraph<String,TripEdge> readTrips(String url) throws FileNotFoundException,IOException {
        SimpleDirectedGraph<String,TripEdge> graph = new SimpleDirectedGraph<>(TripEdge.class);
        File file = new File(url);
        try(FileReader reader = new FileReader(file)){
            try(CSVReader cr = new CSVReader(reader)){
                cr.readNext();
                String[] line = null;
                while((line=cr.readNext())!=null){
                    String initNode = line[0];
                    String endNode = line[1];
                    graph.addVertex(initNode);
                    graph.addVertex(endNode);
                    if(!initNode.equals(endNode)){
                        TripEdge edge = new TripEdge(initNode,endNode);
                        graph.addEdge(initNode, endNode,edge);
                    }
                }
            }
        }
        return graph;
    }

    public static double computeMarginalCost(double length,double density){
        double A = 8.51319*density*Math.pow(length,2);
        double B = Math.pow(114.38-density, 2)*1760;
        return A/B;
    }

    public static double computeDensity(double initialDensity,double taxiVolume, double length, int numLanes){
        double k =  initialDensity + taxiVolume*1760/(length*numLanes);
        if(k>113){
            return 113;
        }else{
            return k;
        }
    }

    public static double computeTraveltime(double length,double density){
        return (3600.0/1760)*(length/(27.48195-0.2402688*density));
    }

    public static void run(String netUrl,String tripUrl,String volumeUrl){
        try {
            SimpleDirectedWeightedGraph<String,NetEdge> net = readGraph(netUrl,volumeUrl);
            SimpleDirectedGraph<String,TripEdge> trip = readTrips(tripUrl);
            int n = 1;
            while(n<6){
                //Calculate all surcharge and update weight of the edges
                for(NetEdge edge:net.edgeSet()){
                    double d = edge.length;
                    double traveltime = edge.traveltime;
                    double density = edge.density;
                    double marginalCost = computeMarginalCost(d, density);
                    double surcharge = (1.0/n)*marginalCost + (1-(1.0/n))*edge.surchargePool.getRecentSurcharge();
                    edge.surchargePool.add(surcharge);
                    double cost = surcharge + traveltime;
                    net.setEdgeWeight(edge, cost);
                }

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

                //Update density & traveltime 
                for(NetEdge edge:net.edgeSet()){
                    double taxiVolume = edge.taxiVolume;
                    double length = edge.length;
                    double initialDensity = edge.initialDensity;
                    int numLanes = edge.numLanes;

                    double density = computeDensity(initialDensity, taxiVolume, length, numLanes);
                    edge.density = density;
                    
                    double newTraveltime = computeTraveltime(length, density);
                    
                    if(newTraveltime<0){
                        edge.traveltimeBeforeRounded = newTraveltime;
                        newTraveltime = length/0.01;
                        edge.traveltimeRounded = true;
                    }else{
                        edge.traveltimeRounded = false;
                        edge.traveltimeBeforeRounded = 0;
                    }
                    edge.traveltime = newTraveltime;
                }

                CsvGraphWriter.writeTo(net, NetEdge.class, FileDirectoryGenerator.createFileAutoRename("a7_net", "csv"));
                CsvGraphWriter.writeTo(trip, TripEdge.class, FileDirectoryGenerator.createFileAutoRename("a7_trip", "csv"));
                n ++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}