package lab1505;

import java.io.IOException;

import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.junit.jupiter.api.Test;
import org.lab1505.ue.alg.algorithm6.NetEdge;
import org.lab1505.ue.fileutil.CsvGraphWriter;
import org.lab1505.ue.fileutil.FileDirectoryGenerator;

public class ReadFileTest {
    static final String INITIAL_TRAVELTIME = "files/algorithm6/traveltime.csv";
    static final String LENGTH = "files/algorithm6/length.csv";
    static final String OTHER_VOLUME = "files/algorithm6/othervolume.csv";

    @Test
    public void read() throws IOException {
        SimpleDirectedWeightedGraph<String,NetEdge> graph =  NetEdge.readGraphFromFiles(OTHER_VOLUME, INITIAL_TRAVELTIME, LENGTH);
        CsvGraphWriter.writeTo(graph, NetEdge.class, FileDirectoryGenerator.createDefaultFile("test.csv"));
    }
}