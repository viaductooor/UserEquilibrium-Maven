package org.lab1505.ue.alg.chongqing;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lab1505.ue.alg.chongqing.ChongqingMapResolver.ChongqingMapEdge;
import static org.lab1505.ue.alg.chongqing.ChongqingMapResolver.ChongqingMapNode;
import static org.lab1505.ue.alg.chongqing.ChongqingTaxiGpsResolver.TaxiGpsRecord;

public class EmergencySystem {
    private SimpleDirectedGraph<Integer, CMapEdge> basicMap;
    private SimpleDirectedWeightedGraph<Integer, CMapEdge> lengthWeightedGraph;
    private SimpleDirectedWeightedGraph<Integer, CMapEdge> timeCostWeightedGraph;
    private EmergencyStations stations;

    public EmergencySystem(SimpleDirectedGraph<Integer, CMapEdge> basicMap,
                           EmergencyStations stations) {
        SimpleDirectedWeightedGraph<Integer, CMapEdge> lengthWeightedGraph = new SimpleDirectedWeightedGraph<>(CMapEdge.class);
        SimpleDirectedWeightedGraph<Integer, CMapEdge> timeCostWeightedGraph = new SimpleDirectedWeightedGraph<>(CMapEdge.class);
        for (CMapEdge e : basicMap.edgeSet()) {
            int start = basicMap.getEdgeSource(e);
            int end = basicMap.getEdgeTarget(e);
            lengthWeightedGraph.addVertex(start);
            lengthWeightedGraph.addVertex(end);
            lengthWeightedGraph.addEdge(start, end, e);
            lengthWeightedGraph.setEdgeWeight(e, e.length);
        }
        for (CMapEdge e : basicMap.edgeSet()) {
            int start = basicMap.getEdgeSource(e);
            int end = basicMap.getEdgeTarget(e);
            timeCostWeightedGraph.addVertex(start);
            timeCostWeightedGraph.addVertex(end);
            timeCostWeightedGraph.addEdge(start, end, e);
            timeCostWeightedGraph.setEdgeWeight(e, e.traveltime);
        }
        this.basicMap = basicMap;
        this.lengthWeightedGraph = lengthWeightedGraph;
        this.timeCostWeightedGraph = timeCostWeightedGraph;
        this.stations = stations;
    }

    /**
     * Compute the total distance cost of a path
     *
     * @param path
     * @return total distance
     */
    public static double totalDistance(GraphPath<Integer, CMapEdge> path) {
        double sum = 0;
        for (CMapEdge edge : path.getEdgeList()) {
            sum += edge.length;
        }
        return sum;
    }

    /**
     * Compute the total time cost of a path
     *
     * @param path
     * @return total time cost
     */
    public static double totalTimecost(GraphPath<Integer, CMapEdge> path) {
        double sum = 0;
        for (CMapEdge edge : path.getEdgeList()) {
            sum += edge.traveltime;
        }
        return sum;
    }

    /**
     * @param records
     * @param nodes
     * @param edges
     * @return
     */
    public static SimpleDirectedGraph<Integer, CMapEdge> readAvgSpeedLinkMap(
            List<TaxiGpsRecord> records, List<ChongqingMapNode> nodes, List<ChongqingMapEdge> edges) {

        SimpleDirectedGraph<Integer, CMapEdge> speedMap = new SimpleDirectedGraph<>(CMapEdge.class);
        Map<Integer, double[]> nodeSpeedMap = ChongqingTaxiGpsProcessor.getSpeedMap(records, nodes);
        for (ChongqingMapEdge e : edges) {
            // If we can't infer the average speed of the link, set it to a default low value
            double avgLinkSpeed = 10;
            if (nodeSpeedMap.containsKey(e.startNode) && nodeSpeedMap.containsKey(e.endNode)) {
                avgLinkSpeed = (nodeSpeedMap.get(e.startNode)[0] + nodeSpeedMap.get(e.endNode)[0]) / 2;
            }
            double traveltime = 3.6 * e.length / avgLinkSpeed;
            if (e.startNode != e.endNode) {
                // Simple graph cannot contain a loop
                speedMap.addVertex(e.startNode);
                speedMap.addVertex(e.endNode);
                speedMap.addEdge(e.startNode, e.endNode, new CMapEdge(traveltime, e.length));
                speedMap.addEdge(e.endNode, e.startNode, new CMapEdge(traveltime, e.length));
            }
        }
        return speedMap;
    }


    /**
     * Note: only applicable for Sioux Falls network
     * Reset the travel time of every edge in the Sioux Falls network, according to
     * {@url https://github.com/bstabler/TransportationNetworks/blob/master/SiouxFalls/SiouxFalls_flow.tntp}
     *
     * @param traveltimeUrl
     * @param graph
     */
    private static void coverSiouxFallsTraveltime(String traveltimeUrl, SimpleDirectedGraph<Integer, CMapEdge> graph) {
        try (BufferedReader reader = new BufferedReader(new FileReader(traveltimeUrl))) {
            reader.readLine();
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] items = line.split(" ");
                int start = Integer.parseInt(items[0].trim());
                int end = Integer.parseInt(items[1].trim());
                double traveltime = Double.parseDouble(items[3].trim());
                if (graph.containsEdge(start, end)) {
                    graph.getEdge(start, end).traveltime = traveltime;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Disabling several edges means setting their weight to a super big value.
     *
     * @param targetEdges the edges to disable, eg. [[1,2],[3,4]] means Link(1,2) and Link(3,4)
     */
    public void disableEdges(int[][] targetEdges) {
        for (int[] e : targetEdges) {
            CMapEdge edge = basicMap.getEdge(e[0], e[1]);
            if (edge != null) {
                lengthWeightedGraph.setEdgeWeight(edge, Double.MAX_VALUE);
                timeCostWeightedGraph.setEdgeWeight(edge, Double.MAX_VALUE);
            }
        }
    }

    /**
     * The opposite method of {@link #disableEdges}
     */
    public void recoverAllEdgeWeight() {
        for (CMapEdge e : basicMap.edgeSet()) {
            lengthWeightedGraph.setEdgeWeight(e, e.length);
        }
        for (CMapEdge e : basicMap.edgeSet()) {
            timeCostWeightedGraph.setEdgeWeight(e, e.traveltime);
        }
    }

    /**
     * Get paths from specific stations to the target location which are the least judging by distance
     *
     * @param stations
     * @param targetNodeId
     * @return paths
     */
    private Map<String, GraphPath<Integer, CMapEdge>> getShortestPathsByDistance(
            List<? extends BasicEmergencyStation> stations, int targetNodeId) {
        Map<String, GraphPath<Integer, CMapEdge>> routes = new HashMap<>();
        for (BasicEmergencyStation station : stations) {
            if (lengthWeightedGraph.containsVertex(station.nodeId) && lengthWeightedGraph.containsVertex(targetNodeId)) {
                DijkstraShortestPath<Integer, CMapEdge> shortestPath = new DijkstraShortestPath<>(lengthWeightedGraph);
                GraphPath<Integer, CMapEdge> path = shortestPath.getPath(station.nodeId, targetNodeId);
                if (path != null) {
                    routes.put(station.name, path);
                }
            }
        }
        return routes;
    }

    /**
     * Get paths from specific stations to the target location which are the least judging by time cost
     *
     * @param stations
     * @param targetNodeId
     * @return paths
     */
    private Map<String, GraphPath<Integer, CMapEdge>> getShortestPathsByTimeCost(
            List<? extends BasicEmergencyStation> stations, int targetNodeId) {
        Map<String, GraphPath<Integer, CMapEdge>> routes = new HashMap<>();
        for (BasicEmergencyStation station : stations) {
            if (timeCostWeightedGraph.containsVertex(station.nodeId) && timeCostWeightedGraph.containsVertex(targetNodeId)) {
                DijkstraShortestPath<Integer, CMapEdge> shortestPath = new DijkstraShortestPath<>(timeCostWeightedGraph);
                GraphPath<Integer, CMapEdge> path = shortestPath.getPath(station.nodeId, targetNodeId);
                if (path != null) {
                    routes.put(station.name, path);
                }
            }
        }
        return routes;
    }

    /**
     * Search paths from the police stations to the target node, according to minimum distance.
     *
     * @param targetNodeId
     * @return paths
     */
    public Map<String, GraphPath<Integer, CMapEdge>> searchNearestPoliceStationByDistance(int targetNodeId) {
        return getShortestPathsByDistance(stations.getPoliceStations(), targetNodeId);
    }

    /**
     * Search paths from the police stations to the target node, according to minimum time cost.
     *
     * @param targetNodeId
     * @return paths
     */
    public Map<String, GraphPath<Integer, CMapEdge>> searchNearestPoliceStationByTimecost(int targetNodeId) {
        return getShortestPathsByTimeCost(stations.getPoliceStations(), targetNodeId);
    }

    /**
     * Search paths from the hospitals to the target node, according to minimum distance.
     *
     * @param targetNodeId
     * @return paths
     */
    public Map<String, GraphPath<Integer, CMapEdge>> searchNearestHospitalByDistance(int targetNodeId) {
        return getShortestPathsByDistance(stations.getHospitals(), targetNodeId);
    }

    /**
     * Search paths from the hospitals to the target node, according to minimum time cost.
     *
     * @param targetNodeId
     * @return paths
     */
    public Map<String, GraphPath<Integer, CMapEdge>> searchNearestHospitalByTimecost(int targetNodeId) {
        return getShortestPathsByTimeCost(stations.getHospitals(), targetNodeId);
    }

    /**
     * Search paths from the fire alarm stations to the target node, according to minimum distance.
     *
     * @param targetNodeId
     * @return paths
     */
    public Map<String, GraphPath<Integer, CMapEdge>> searchNearestFireAlarmCenterByDistance(int targetNodeId) {
        return getShortestPathsByDistance(stations.getFireAlarmCenters(), targetNodeId);
    }

    /**
     * Search paths from the fire alarm stations to the target node, according to minimum time cost.
     *
     * @param targetNodeId
     * @return paths
     */
    public Map<String, GraphPath<Integer, CMapEdge>> searchNearestFireAlarmCenterByTimecost(int targetNodeId) {
        return getShortestPathsByTimeCost(stations.getFireAlarmCenters(), targetNodeId);
    }

    private void refreshAllTraveltime() {
        for (CMapEdge edge : basicMap.edgeSet()) {
            edge.refreshTraveltime();
        }
    }
}
