package lab1505;

import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.junit.jupiter.api.Test;
import org.lab1505.ue.alg.ChangeDemand;
import org.lab1505.ue.alg.UserEquilibrium;
import org.lab1505.ue.entity.ChangeDemandEdge;
import org.lab1505.ue.entity.DemandEdge;
import org.lab1505.ue.entity.LinkEdge;
import org.lab1505.ue.entity.UeLinkEdge;
import org.lab1505.ue.fileutil.CsvGraphWriter;
import org.lab1505.ue.fileutil.TntpReader;

public class ChangeDemandTest {
    private SimpleDirectedWeightedGraph<Integer,UeLinkEdge> net;
    private SimpleDirectedGraph<Integer,DemandEdge> trips;

    @Test
    public void changeDemand(){
        SimpleDirectedGraph<Integer,LinkEdge> originalNet = TntpReader.readNet(TntpReader.SIOUXFALLS_NET);
        net = UserEquilibrium.fromLinkEdgeGraph(originalNet);
        trips = TntpReader.readTrips(TntpReader.SIOUXFALLS_TRIP);
        
        ChangeDemand cd = new ChangeDemand(net,trips);
        cd.changeDemand(0.01, 10);
        CsvGraphWriter.writeTo(cd.getTripsGraph(), ChangeDemandEdge.class, "changedemand.csv");
        
    }

}