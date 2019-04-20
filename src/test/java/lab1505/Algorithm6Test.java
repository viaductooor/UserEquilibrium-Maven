package lab1505;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.junit.jupiter.api.Test;
import org.lab1505.ue.alg.algorithm6.Algorithm6;
import org.lab1505.ue.alg.algorithm6.NetEdge;
import org.lab1505.ue.alg.algorithm6.TripEdge;
import org.lab1505.ue.fileutil.CsvGraphWriter;
import org.lab1505.ue.fileutil.FileDirectoryGenerator;

public class Algorithm6Test {
    @Test
    public void readFile() throws FileNotFoundException, IOException {
        SimpleDirectedWeightedGraph<String,NetEdge> graph =  Algorithm6.readGraph(Algorithm6.NET_URL);
        CsvGraphWriter.writeTo(graph, NetEdge.class, FileDirectoryGenerator.createDefaultFile("outfile.csv"));
    }
    @Test
    public void readTrips() throws FileNotFoundException, IOException {
        SimpleDirectedGraph<String,TripEdge> graph =  Algorithm6.readTrips(Algorithm6.TRIP_URL);
        CsvGraphWriter.writeTo(graph, TripEdge.class, FileDirectoryGenerator.createDefaultFile("trips.csv"));
    }

    @Test
    public void total(){
        Algorithm6.run(Algorithm6.NET_URL,Algorithm6.TRIP_URL);
    }
}