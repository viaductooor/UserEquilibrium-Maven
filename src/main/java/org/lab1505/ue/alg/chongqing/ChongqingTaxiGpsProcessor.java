package org.lab1505.ue.alg.chongqing;

import com.opencsv.CSVWriter;

import java.io.*;
import java.util.*;

import static org.lab1505.ue.alg.chongqing.ChongqingMapResolver.ChongqingMapNode;
import static org.lab1505.ue.alg.chongqing.ChongqingMapResolver.readNodes;
import static org.lab1505.ue.alg.chongqing.ChongqingTaxiGpsResolver.TaxiGpsRecord;
import static org.lab1505.ue.alg.chongqing.ChongqingTaxiGpsResolver.readTaxiRecords;

public class ChongqingTaxiGpsProcessor {

    public static List<CMapTrip> getTaxiTrips(List<TaxiGpsRecord> taxiGpsRecords, List<ChongqingMapNode> nodes) {
        taxiGpsRecords.sort(Comparator.comparing(record -> (record.car_id + record.date + record.time)));
        LinkedList<CMapTrip> trips = new LinkedList<>();
        HashMap<String, TaxiGpsRecord> taxipool = new HashMap<>();
        for (TaxiGpsRecord record : taxiGpsRecords) {
            String id = record.car_id;
            int state = record.state;
            if (state > 0) {
                if (!taxipool.containsKey(id)) {
                    // The beginning of another new trip
                    taxipool.put(id, record);
                }
            } else {
                if (taxipool.containsKey(id)) {
                    // The end of the previous trip
                    double startLat = taxipool.get(id).lat;
                    double startLon = taxipool.get(id).lon;
                    String startDate = taxipool.get(id).date;
                    String startTime = taxipool.get(id).time;
                    double endLat = record.lat;
                    double endLon = record.lon;
                    String endDate = record.date;
                    String endTime = record.time;

                    CMapTrip cMapTrip = new CMapTrip(startTime, endTime, startDate, endDate, -1, -1, startLat, startLon, endLat, endLon);

                    trips.add(cMapTrip);
                    taxipool.remove(id);
                }
            }
        }
        for (CMapTrip trip : trips) {
            int nearestStart = computeNearestNode(trip.startNodeLat, trip.startNodeLon, nodes);
            int nearestEnd = computeNearestNode(trip.endNodeLat, trip.endNodeLon, nodes);
            trip.startNodeId = nearestStart;
            trip.endNodeId = nearestEnd;
        }
        return trips;
    }

    /**
     * Get average speed of every node.
     *
     * @param taxiRecords original taxi gps data
     * @param nodes       basic nodes which are composed of id, latitude and longitude
     * @return average speed of every node
     */
    public static Map<Integer, double[]> getSpeedMap(List<TaxiGpsRecord> taxiRecords,
                                                     List<ChongqingMapNode> nodes) {

        // Create two node lists sorted by lat and lon respectively
        // The two lists will be used to match nearest nodes by biSearch Method
        ArrayList<ChongqingMapNode> nodesSortedByLat = new ArrayList<>();
        ArrayList<ChongqingMapNode> nodesSortedByLon = new ArrayList<>();
        nodesSortedByLat.addAll(nodes);
        nodesSortedByLon.addAll(nodes);
        nodesSortedByLat.sort((node1, node2) -> ((int) ((node1.lat - node2.lat) * 1000)));
        nodesSortedByLon.sort((node1, node2) -> ((int) ((node1.lon - node2.lon) * 1000)));

        // The second generic type contains two values: [total speed, count]
        Map<Integer, double[]> map = new HashMap<>();
        for (TaxiGpsRecord record : taxiRecords) {
//            int nearestNode = computeNearestNode(record.lat, record.lon, nodesSortedByLat, nodesSortedByLon);
            int nearestNode = computeNearestNode(record.lat, record.lon, nodes);
            if (map.containsKey(nearestNode)) {
                double[] v = map.get(nearestNode);
                v[0] = v[0] + record.speed;
                v[1] += 1;
            } else {
                map.put(nearestNode, new double[]{record.speed, 1});
            }
        }
        return map;
    }

    /**
     * Given a geo location (latitude,longitude), match (brute) the nearest node from the basic nodes.
     *
     * @param lat   latitude of the location
     * @param lon   longitude of the location
     * @param nodes
     * @return
     */
    public static int computeNearestNode(double lat, double lon, List<ChongqingMapNode> nodes) {
        double minDistance = Double.MAX_VALUE;
        int minNode = -1;
        for (ChongqingMapNode node : nodes) {
            double dlat = node.lat - lat;
            double dlon = node.lon - lon;
            double d = Math.pow(dlat, 2) + Math.pow(dlon, 2);
            if (d < minDistance) {
                minDistance = d;
                minNode = node.id;
            }
        }
        return minNode;
    }

    /**
     * (Test)Given a geo location (latitude,longitude), match (biSearch) the nearest node from the basic nodes.
     *
     * @param lat              latitude of the location
     * @param lon              longitude of the location
     * @param nodesSortedByLat nodes which are sorted by latitude
     * @param nodesSortedByLon nodes which are sorted by longitude
     * @return id of the nearest node
     */
    @Deprecated
    public static int computeNearestNode(double lat, double lon, ArrayList<ChongqingMapNode> nodesSortedByLat,
                                         ArrayList<ChongqingMapNode> nodesSortedByLon) {
        int lonNearest = biSearchLon(nodesSortedByLon, 0, nodesSortedByLon.size() - 1, lon);
        int latNearest = biSearchLat(nodesSortedByLat, 0, nodesSortedByLat.size() - 1, lat);
        ChongqingMapNode latNode = null;
        ChongqingMapNode lonNode = null;
        for (ChongqingMapNode node : nodesSortedByLat) {
            if (node.id == lonNearest) {
                lonNode = node;
            }
            if (node.id == latNearest) {
                latNode = node;
            }
            if (lonNode != null && latNode != null) {
                break;
            }
        }
        double latDistance = Math.pow(latNode.lon - lon, 2) + Math.pow(latNode.lat - lat, 2);
        double lonDistance = Math.pow(lonNode.lon - lon, 2) + Math.pow(lonNode.lat - lat, 2);
        if (latDistance <= lonDistance) {
            return latNode.id;
        } else {
            return lonNode.id;
        }
    }

    /**
     * Note should be taken that the nodes here ar sorted by lon ascend
     *
     * @param nodes nodes sorted by lon ascend
     * @param lon   target latitude to match
     * @return matched node id
     */
    @Deprecated
    private static int biSearchLon(ArrayList<ChongqingMapNode> nodes, int start, int end, double lon) {
        double firstLon = nodes.get(start).lon;
        double lastLon = nodes.get(end).lon;
        if (lon < firstLon) {
            return nodes.get(start).id;
        } else if (lon > lastLon) {
            return nodes.get(end).id;
        }
        if (end - start < 2) {
            if (lon - firstLon <= lastLon - lon) {
                return nodes.get(start).id;
            } else {
                return nodes.get(end).id;
            }
        } else {
            int mid = start + (end - start) / 2;
            if (lon < nodes.get(mid).lon) {
                return biSearchLon(nodes, start, mid, lon);
            } else {
                return biSearchLon(nodes, mid, end, lon);
            }
        }
    }

    /**
     * Note should be taken that the nodes here ar sorted by lat ascend
     *
     * @param nodes nodes sorted by lat ascend
     * @param lat   target latitude to match
     * @return matched node id
     */
    @Deprecated
    private static int biSearchLat(ArrayList<ChongqingMapNode> nodes, int start, int end, double lat) {
        double firstLat = nodes.get(start).lat;
        double lastLat = nodes.get(end).lat;
        if (lat < firstLat) {
            return nodes.get(start).id;
        } else if (lat > lastLat) {
            return nodes.get(end).id;
        }
        if (end - start < 2) {
            if (lat - firstLat <= lastLat - lat) {
                return nodes.get(start).id;
            } else {
                return nodes.get(end).id;
            }
        } else {
            int mid = start + (end - start) / 2;
            if (lat < nodes.get(mid).lat) {
                return biSearchLat(nodes, start, mid, lat);
            } else {
                return biSearchLat(nodes, mid, end, lat);
            }
        }
    }

    /**
     * Write average speed of every node into an OutputStream in the form of csv table.
     *
     * @param outputStream target output stream
     * @param speedMap     average speed of every node
     */
    public static void writeAsCsv(OutputStream outputStream, Map<Integer, double[]> speedMap) {
        CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(outputStream));
        csvWriter.writeNext(new String[]{"node_id", "avg_speed", "count"});
        for (Map.Entry<Integer, double[]> e : speedMap.entrySet()) {
            csvWriter.writeNext(new String[]{
                    String.valueOf(e.getKey()),
                    String.valueOf(e.getValue()[0] / e.getValue()[1]),
                    String.valueOf(e.getValue()[1])});
        }
    }

    /**
     * Overall procedure, compute average speed of every node, and write the result into target outputStream
     *
     * @param gpsStreams   input gps file streams
     * @param nodesStream  input nodes stream
     * @param outputStream target output stream
     */
    public static void computeAndWriteNodeAvgSpeed(InputStream[] gpsStreams, InputStream nodesStream, OutputStream outputStream) {
        List<TaxiGpsRecord> gpsRecords = readTaxiRecords(gpsStreams);
        List<ChongqingMapNode> chongqingMapNodes = readNodes(nodesStream);
        Map<Integer, double[]> speedMap = getSpeedMap(gpsRecords, chongqingMapNodes);
        writeAsCsv(outputStream, speedMap);
    }

    public static void writeTaxiTrips(OutputStream os, List<CMapTrip> trips) {

        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(os))) {
            writer.writeNext(new String[]{"startDate", "startTime", "endDate", "endTime",
                    "startNodeId", "endNodeId", "startNodeLat", "startNodeLon", "endNodeLat", "endNodeLon"});
            for (CMapTrip trip : trips) {
                writer.writeNext(new String[]{trip.startDate, trip.startTime, trip.endDate, trip.endTime,
                        trip.startNodeId + "", trip.endNodeId + "", trip.startNodeLat + "", trip.startNodeLon + "",
                        trip.endNodeLat + "", trip.endNodeLon + ""});
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        // Read taxi records from original files. This process is time-costing
        String[] urls = new String[]{
                "20170328_097.TXT",
                "20170328_099.TXT",
                "20170328_100.TXT",
                "20170328_101.TXT",
                "20170328_102.TXT",
                "20170328_103.TXT",
                "20170328_104.TXT",
                "20170328_105.TXT",
                "20170328_106.TXT",
                "20170328_107.TXT",
                "20170328_108.TXT"
        };
        InputStream[] inputStreams = new InputStream[urls.length];
        for (int i = 0; i < urls.length; i++) {
            inputStreams[i] = new FileInputStream("files/chongqing/gps/" + urls[i]);
        }

        List<TaxiGpsRecord> taxiGpsRecords = readTaxiRecords(inputStreams);
        List<ChongqingMapNode> chongqingMapNodes = readNodes(new FileInputStream("files/chongqing/TEST_NODES.csv"));
        List<CMapTrip> taxiTrips = getTaxiTrips(taxiGpsRecords, chongqingMapNodes);
        for (CMapTrip trip : taxiTrips) {
            System.out.println(trip);
        }
    }
}
