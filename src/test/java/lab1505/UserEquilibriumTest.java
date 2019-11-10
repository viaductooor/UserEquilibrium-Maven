package lab1505;

import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.junit.jupiter.api.Test;
import org.lab1505.ue.alg.UserEquilibrium;
import org.lab1505.ue.entity.DemandEdge;
import org.lab1505.ue.entity.LinkEdge;
import org.lab1505.ue.entity.UeLinkEdge;
import org.lab1505.ue.fileutil.CsvGraphWriter;
import org.lab1505.ue.fileutil.FileDirectoryGenerator;
import org.lab1505.ue.fileutil.TntpReader;

public class UserEquilibriumTest{

    @Test
    public void assign(){
        SimpleDirectedGraph<Integer,LinkEdge> originalNet = TntpReader.readNet(TntpReader.SIOUXFALLS_NET);
        SimpleDirectedWeightedGraph<Integer,UeLinkEdge> net = UserEquilibrium.fromLinkEdgeGraph(originalNet);
        SimpleDirectedGraph<Integer,DemandEdge> trips = TntpReader.readTrips(TntpReader.SIOUXFALLS_TRIP);
        System.out.println("init");
        UserEquilibrium<Integer,DemandEdge,UeLinkEdge> ueq = new UserEquilibrium<>(net, trips);
        ueq.assign(5);
        System.out.println(ueq.getDiffList());
        CsvGraphWriter.writeTo(net, UeLinkEdge.class, FileDirectoryGenerator.createDefaultFile("ue.csv"));
    }
}
