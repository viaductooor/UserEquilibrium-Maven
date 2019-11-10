package org.lab1505.ue.alg.chongqing;


import com.opencsv.CSVReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ChongqingMapResolver {
    public static List<ChongqingMapEdge> readEdges(InputStream inputStream) {
        List<ChongqingMapEdge> list = new LinkedList<>();
        try (InputStreamReader ireader = new InputStreamReader(inputStream, "GB2312")) {
            CSVReader reader = new CSVReader(ireader);
            Iterator<String[]> iter = reader.iterator();
            iter.next();
            while (iter.hasNext()) {
                String[] items = iter.next();
                if (items.length == 17 && !items[14].equals("#N/A") && !items[15].equals("#N/A") && !items[16].equals("#N/A")) {
                    String type = items[2];
                    String oneway = items[5];
                    int startNode = Integer.parseInt(items[14]);
                    int endNode = Integer.parseInt(items[15]);
                    double length = Double.parseDouble(items[16]);
                    list.add(new ChongqingMapEdge(type, oneway, startNode, endNode, length));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<ChongqingMapNode> readNodes(InputStream inputStream) {
        List<ChongqingMapNode> list = new LinkedList<>();
        try (InputStreamReader ireader = new InputStreamReader(inputStream)) {
            CSVReader reader = new CSVReader(ireader);
            Iterator<String[]> iter = reader.iterator();
            iter.next();
            while (iter.hasNext()) {
                String[] items = iter.next();
                int id = Integer.parseInt(items[0]);
                double lon = Double.parseDouble(items[1]);
                double lat = Double.parseDouble(items[2]);
                list.add(new ChongqingMapNode(id, lat, lon));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static class ChongqingMapEdge {
        public String type;
        public String oneway;
        public int startNode;
        public int endNode;
        public double length;

        public ChongqingMapEdge(String type, String oneway, int startNode, int endNode, double length) {
            this.type = type;
            this.oneway = oneway;
            this.startNode = startNode;
            this.endNode = endNode;
            this.length = length;
        }

        @Override
        public String toString() {
            return "ChongqingMapEdge{" +
                    "type='" + type + '\'' +
                    ", oneway='" + oneway + '\'' +
                    ", startNode=" + startNode +
                    ", endNode=" + endNode +
                    ", length=" + length +
                    '}';
        }
    }

    public static class ChongqingMapNode {
        public int id;
        public double lat;
        public double lon;

        public ChongqingMapNode(int id, double lat, double lon) {
            this.id = id;
            this.lat = lat;
            this.lon = lon;
        }

        @Override
        public String toString() {
            return "ChongqingMapNode{" +
                    "id=" + id +
                    ", lat=" + lat +
                    ", lon=" + lon +
                    '}';
        }
    }
}
