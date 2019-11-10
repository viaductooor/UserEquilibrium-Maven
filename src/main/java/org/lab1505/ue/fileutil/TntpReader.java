package org.lab1505.ue.fileutil;

import org.jgrapht.graph.SimpleDirectedGraph;
import org.lab1505.ue.entity.DemandEdge;
import org.lab1505.ue.entity.LinkEdge;

import java.io.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * TntpReader is a class which can read tntp files (see
 * https://github.com/bstabler/TransportationNetworks) and transform them into
 * SimpleDirectedGraph.
 */
public class TntpReader {

    public static final String ANAHEIM_TRIP = "files/Anaheim_trips.tntp";
    public static final String ANAHEIM_NET = "files/Anaheim_net.tntp";
    public static final String CHICAGO_TRIP = "files/ChicagoRegional_trips.tntp";
    public static final String CHICAGO_NET = "files/ChicagoRegional_net.tntp";
    public static final String SIOUXFALLS_TRIP = "files/SiouxFalls_trips.tntp";
    public static final String SIOUXFALLS_NET = "files/SiouxFalls_net.tntp";
    public static final String WINNIPEG_ASYM_TRIP = "files/Winnipeg-Asym_trips.tntp";
    public static final String WINNIPEG_ASYM_NET = "files/Winnipeg-Asym_net.tntp";
    private static Logger logger = Logger.getLogger(TntpReader.class.getName());

    public static SimpleDirectedGraph<Integer, LinkEdge> readNet(InputStream inputStream) {
        SimpleDirectedGraph<Integer, LinkEdge> graph = new SimpleDirectedGraph<>(LinkEdge.class);
        String line = "";

        logger.info("Start reading net...");

        // read from net file
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            boolean isEdge = false;

            while ((line = reader.readLine()) != null & line != "") {
                if (isEdge == true) {
                    line = " " + line;
                    String[] items = line.split("\\s+");
                    int from = Integer.parseInt(items[1]);
                    int to = Integer.parseInt(items[2]);
                    double capacity = Double.parseDouble(items[3]);
                    double length = Double.parseDouble(items[4]);
                    double ftime = Double.parseDouble(items[5]);
                    double B = Double.parseDouble(items[6]);
                    double power = Double.parseDouble(items[7]);
                    double speed = Double.parseDouble(items[8]);
                    double toll = Double.parseDouble(items[9]);
                    int type = Integer.parseInt(items[10].substring(0, 1));
                    LinkEdge edge = new LinkEdge(from, to, capacity, length, ftime, B, power, speed, toll, type);
                    graph.addVertex(from);
                    graph.addVertex(to);
                    graph.addEdge(from, to, edge);
                }
                if (line.contains("~")) {
                    isEdge = true;
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            // last line was counted in, which is not supposed
        } catch (java.lang.NumberFormatException e) {
            e.printStackTrace();
        }

        logger.info("Reading net done: " + graph.edgeSet().size() + " links in total.");

        return graph;
    }

    /**
     * Read the tntp net from a file which is usually named by REGIONNAME_net.tntp.
     *
     * @param url the relative file location
     * @return net graph
     */
    public static SimpleDirectedGraph<Integer, LinkEdge> readNet(String url) {
        SimpleDirectedGraph<Integer, LinkEdge> graph = new SimpleDirectedGraph<>(LinkEdge.class);
        File netFile = new File(url);
        FileInputStream fis = null;
        BufferedReader reader = null;
        String line = "";

        logger.info("Start reading net...");

        // read from net file
        try {
            fis = new FileInputStream(netFile);
            reader = new BufferedReader(new InputStreamReader(fis));
            boolean isEdge = false;

            while ((line = reader.readLine()) != null & line != "") {
                if (isEdge == true) {
                    line = " " + line;
                    String[] items = line.split("\\s+");
                    int from = Integer.parseInt(items[1]);
                    int to = Integer.parseInt(items[2]);
                    double capacity = Double.parseDouble(items[3]);
                    double length = Double.parseDouble(items[4]);
                    double ftime = Double.parseDouble(items[5]);
                    double B = Double.parseDouble(items[6]);
                    double power = Double.parseDouble(items[7]);
                    double speed = Double.parseDouble(items[8]);
                    double toll = Double.parseDouble(items[9]);
                    int type = Integer.parseInt(items[10].substring(0, 1));
                    LinkEdge edge = new LinkEdge(from, to, capacity, length, ftime, B, power, speed, toll, type);
                    graph.addVertex(from);
                    graph.addVertex(to);
                    graph.addEdge(from, to, edge);
                }
                if (line.contains("~")) {
                    isEdge = true;
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            // last line was counted in, which is not supposed
        } catch (java.lang.NumberFormatException e) {
            e.printStackTrace();
        }

        logger.info("Reading net done: " + graph.edgeSet().size() + " links in total.");

        return graph;
    }

    /**
     * Read the tntp trips from a file which is usually named by REGIONNAME_trips.tntp.
     *
     * @param inputStream the relative file location
     */
    public static SimpleDirectedGraph<Integer, DemandEdge> readTrips(InputStream inputStream) {
        SimpleDirectedGraph<Integer, DemandEdge> trips = new SimpleDirectedGraph<>(DemandEdge.class);
        String line = "";
        Matcher m = null;

        logger.info("Start reading trips...");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            boolean isTrip = false;
            int origin = -1;
            int destination;
            double demand;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Origin")) {
                    isTrip = true;
                    Pattern pOrigin = Pattern.compile("Origin\\s+(\\d+)");
                    if ((m = pOrigin.matcher(line)).find()) {
                        origin = Integer.parseInt(m.group(1));
                    }
                } else if (isTrip) {
                    Pattern pItem = Pattern.compile("\\s*(\\d+)\\s*:\\s+(\\S+);");
                    m = pItem.matcher(line);
                    while (m.find()) {
                        destination = Integer.parseInt(m.group(1));
                        demand = Double.parseDouble(m.group(2));
                        trips.addVertex(origin);
                        trips.addVertex(destination);
                        if (origin != destination) {
                            trips.addEdge(origin, destination, new DemandEdge(demand));
                        }
                    }
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.info("Reading trips done, " + trips.edgeSet().size() + " trips in total.");

        return trips;
    }

    /**
     * Read the tntp trips from a file which is usually named by REGIONNAME_trips.tntp.
     *
     * @param url the relative file location
     */
    public static SimpleDirectedGraph<Integer, DemandEdge> readTrips(String url) {
        SimpleDirectedGraph<Integer, DemandEdge> trips = new SimpleDirectedGraph<>(DemandEdge.class);
        // file
        File tripFile = new File(url);
        // read from trip file
        FileInputStream fis = null;
        BufferedReader reader = null;
        String line = "";
        Matcher m = null;

        logger.info("Start reading trips...");

        try {
            fis = new FileInputStream(tripFile);
            reader = new BufferedReader(new InputStreamReader(fis));
            boolean isTrip = false;
            int origin = -1;
            int destination;
            double demand;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Origin")) {
                    isTrip = true;
                    Pattern pOrigin = Pattern.compile("Origin\\s+(\\d+)");
                    if ((m = pOrigin.matcher(line)).find()) {
                        origin = Integer.parseInt(m.group(1));
                    }
                } else if (isTrip) {
                    Pattern pItem = Pattern.compile("\\s*(\\d+)\\s*:\\s+(\\S+);");
                    m = pItem.matcher(line);
                    while (m.find()) {
                        destination = Integer.parseInt(m.group(1));
                        demand = Double.parseDouble(m.group(2));
                        trips.addVertex(origin);
                        trips.addVertex(destination);
                        if (origin != destination) {
                            trips.addEdge(origin, destination, new DemandEdge(demand));
                        }
                    }
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.info("Reading trips done, " + trips.edgeSet().size() + " trips in total.");

        return trips;
    }

    public static TripsInfo readTripsInfo() {
        return null;
    }

    public static NetInfo readNetInfo() {
        return null;
    }
}

class TripsInfo {
    private int numberOfZones;
    private double totalVolume;

    public TripsInfo(int numberOfZones, double totalVolume) {
        this.numberOfZones = numberOfZones;
        this.totalVolume = totalVolume;
    }

    public int getNumberOfZones() {
        return numberOfZones;
    }

    public void setNumberOfZones(int numberOfZones) {
        this.numberOfZones = numberOfZones;
    }

    public double getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(double totalVolume) {
        this.totalVolume = totalVolume;
    }
}

class NetInfo {
    private int numberOfZones;
    private int numberOfNodes;
    private int firstThruNode;
    private int numberOfLinks;

    public NetInfo(int numberOfZones, int numberOfNodes, int firstThruNode, int numberOfLinks) {
        this.numberOfZones = numberOfZones;
        this.numberOfNodes = numberOfNodes;
        this.firstThruNode = firstThruNode;
        this.numberOfLinks = numberOfLinks;
    }

    public int getNumberOfZones() {
        return numberOfZones;
    }

    public void setNumberOfZones(int numberOfZones) {
        this.numberOfZones = numberOfZones;
    }

    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    public void setNumberOfNodes(int numberOfNodes) {
        this.numberOfNodes = numberOfNodes;
    }

    public int getFirstThruNode() {
        return firstThruNode;
    }

    public void setFirstThruNode(int firstThruNode) {
        this.firstThruNode = firstThruNode;
    }

    public int getNumberOfLinks() {
        return numberOfLinks;
    }

    public void setNumberOfLinks(int numberOfLinks) {
        this.numberOfLinks = numberOfLinks;
    }
}
