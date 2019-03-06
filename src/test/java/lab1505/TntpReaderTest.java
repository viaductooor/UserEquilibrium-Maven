package lab1505;

import org.jgrapht.graph.SimpleDirectedGraph;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.lab1505.ue.entity.DemandEdge;
import org.lab1505.ue.entity.LinkEdge;
import org.lab1505.ue.fileutil.TntpReader;

public class TntpReaderTest{
    
    @ParameterizedTest
    @ValueSource(strings = {TntpReader.SIOUXFALLS_NET,TntpReader.ANAHEIM_NET,TntpReader.WINNIPEG_ASYM_NET})
    public void readNet(String url){
        SimpleDirectedGraph<Integer,LinkEdge> graph = TntpReader.readNet(url);
        for(LinkEdge edge:graph.edgeSet()){
            System.out.println(edge);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {TntpReader.SIOUXFALLS_TRIP,TntpReader.ANAHEIM_TRIP,TntpReader.WINNIPEG_ASYM_TRIP})
    public void readTrips(String url){
        SimpleDirectedGraph<Integer,DemandEdge> graph = TntpReader.readTrips(url);
        for(DemandEdge edge:graph.edgeSet()){
            int source = graph.getEdgeSource(edge);
            int target = graph.getEdgeTarget(edge);
            System.out.println(source+","+target+":"+graph.getEdge(source, target));
        }
    }

}