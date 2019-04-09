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

public class Algorithm6 {
    public static final String NET_URL = "files/algorithm6/lengthOthervolumeTraveltime.csv";
    public static final String TRIP_URL="files/algorithm6/odpair.csv";

    /**
     * Structure of the input csv file should be:
     * (source_vertex,target_vertex,otherVolume,initialTraveltime,length),
     * and the first line is the header which will be omitted during processing.
     * @param url
     * @return the graph
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static SimpleDirectedWeightedGraph<String, NetEdge> readGraph(String url) throws FileNotFoundException,IOException{
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
        return graph;
    }

    /**
     * 
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

    public static double computeMarginalCost(double length,double initialTraveltime,double volume){
        final double K = 0.033591;
        return K*length/Math.pow((initialTraveltime-K*volume), 2);
    }

    public static void run(){
        try {
            SimpleDirectedWeightedGraph<String,NetEdge> net = readGraph(NET_URL);
            SimpleDirectedGraph<String,TripEdge> trip = readTrips(TRIP_URL);
            int n = 1;
            while(n<6){
                //Calculate all surcharge and update weight of the edges
                for(NetEdge edge:net.edgeSet()){
                    double d = edge.length;
                    double t_0 = edge.initialTraveltime;
                    double x = edge.otherVolume+edge.taxiVolume;
                    double marginalCost = computeMarginalCost(d, t_0, x);
                    double surcharge = (1.0/n)*marginalCost + (1-(1.0/n))*edge.surchargePool.getRecentSurcharge();
                    edge.surchargePool.add(surcharge);
                    double updatedTraveltime = surcharge + t_0;
                    net.setEdgeWeight(edge, updatedTraveltime);
                    edge.traveltime = updatedTraveltime;
                }

                //Clear all the volume of taxi which will be loaded later
                for(NetEdge edge:net.edgeSet()){
                    edge.taxiVolume = 0;
                }

                //Find shortest paths of the trips
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
                CsvGraphWriter.writeTo(net, NetEdge.class, FileDirectoryGenerator.createFileAutoRename("a6_net", "csv"));
                CsvGraphWriter.writeTo(trip, TripEdge.class, FileDirectoryGenerator.createFileAutoRename("a6_trip", "csv"));
                n ++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}