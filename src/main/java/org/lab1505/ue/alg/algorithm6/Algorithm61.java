package org.lab1505.ue.alg.algorithm6;

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

public class Algorithm61 {
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

    public static double computeMarginalCost(double length,double volume,int numLanes){
        final double K = 0.033591;
        final double ratio = 3600.0/1760.0;
        double A = ratio*K*length;
        double B = volume/numLanes;
        double C = Math.pow((26.90674-K*volume/numLanes), 2);
        return A*B/C;
    }

    public static void run(String netUrl,String tripUrl,String volumeUrl){
        try {
            SimpleDirectedWeightedGraph<String,NetEdge> net = readGraph(netUrl,volumeUrl);
            SimpleDirectedGraph<String,TripEdge> trip = readTrips(tripUrl);
            int n = 1;
            while(n<11){
                //Calculate all surcharge and update weight of the edges
                for(NetEdge edge:net.edgeSet()){
                    double d = edge.length;
                    double traveltime = edge.traveltime;
                    double x = edge.otherVolume+edge.taxiVolume;
                    int numLanes = edge.numLanes;
                    double marginalCost = computeMarginalCost(d, x,numLanes);
                    double surcharge = (1.0/n)*marginalCost + (1-(1.0/n))*edge.surchargePool.getRecentSurcharge();
                    edge.surchargePool.add(surcharge);
                    double updatedTraveltime = surcharge + traveltime;
                    net.setEdgeWeight(edge, updatedTraveltime);
                    edge.traveltime = updatedTraveltime;
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

                //Update traveltime
                for(NetEdge edge:net.edgeSet()){
                    double taxiVolume = edge.taxiVolume;
                    double otherVolume = edge.otherVolume;
                    double length = edge.length;
                    double ratio = 3600.0/1760.0;
                    double numLanes = edge.numLanes;
                    double newTraveltime = numLanes*ratio*length/(26.90674-0.033591*(taxiVolume+otherVolume));
                    
                    if(newTraveltime<0){
                        newTraveltime = numLanes*ratio*length;
                    }

                    edge.traveltime = newTraveltime;
                }

                CsvGraphWriter.writeTo(net, NetEdge.class, FileDirectoryGenerator.createFileAutoRename("a6_1_net", "csv"));
                CsvGraphWriter.writeTo(trip, TripEdge.class, FileDirectoryGenerator.createFileAutoRename("a6_1_trip", "csv"));
                n ++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}