package lab1505;

import org.jgrapht.graph.SimpleDirectedGraph;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.lab1505.ue.entity.DemandEdge;
import org.lab1505.ue.entity.LinkEdge;
import org.lab1505.ue.fileutil.CsvGraphWriter;
import org.lab1505.ue.fileutil.TntpReader;

public class CsvGraphWriterTest {
    static final String prefix = "output/";
    static final String suffix = ".csv";
    
    @ParameterizedTest
    @ValueSource(strings = {TntpReader.SIOUXFALLS_NET,TntpReader.ANAHEIM_NET,TntpReader.WINNIPEG_ASYM_NET})
    public void writeNet(String url){
        SimpleDirectedGraph<Integer,LinkEdge> graph = TntpReader.readNet(url);

        CsvGraphWriter.writeTo(graph, LinkEdge.class, prefix+url+suffix);
    }

    @ParameterizedTest
    @ValueSource(strings = {TntpReader.SIOUXFALLS_TRIP,TntpReader.ANAHEIM_TRIP,TntpReader.WINNIPEG_ASYM_TRIP})
    public void writeTrips(String url){
        SimpleDirectedGraph<Integer,DemandEdge> graph = TntpReader.readTrips(url);
        CsvGraphWriter.writeTo(graph, DemandEdge.class, prefix+url+suffix);
    }
}