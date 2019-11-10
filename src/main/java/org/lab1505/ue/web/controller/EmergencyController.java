package org.lab1505.ue.web.controller;

import org.jgrapht.GraphPath;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.lab1505.ue.alg.chongqing.*;
import org.lab1505.ue.web.result.CodeMsg;
import org.lab1505.ue.web.result.Result;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class EmergencyController {
    @PostMapping("emergency/quicksearch")
    @ResponseBody
    public Result<String> quickSearch(
            @RequestParam(value = "gpsFiles[]", required = false) MultipartFile[] gpsRecordsFiles,
            @RequestParam(value = "nodeFile", required = false) MultipartFile nodeFile,
            @RequestParam(value = "edgeFile", required = false) MultipartFile edgeFile,
            @RequestParam(value = "stationFile", required = false) MultipartFile stationFile,
            @RequestParam(value = "targetNodeId", required = false) int targetNodeId,
            @RequestParam(value = "stationType", required = false, defaultValue = "hospital") String stationType) {

        if (gpsRecordsFiles == null || nodeFile == null || edgeFile == null || stationFile == null) {
            return Result.error(CodeMsg.ERROR);
        }
        if (gpsRecordsFiles.length < 1) {
            return Result.error(CodeMsg.ERROR);
        }

        InputStream nodeInputStream = null;
        InputStream edgeInputStream = null;
        InputStream stationInputStream = null;
        InputStream[] gpsRecordInputStreams = new InputStream[gpsRecordsFiles.length];

        Map<String, Map<String, GraphPath<Integer, CMapEdge>>> results = new HashMap<>();

        try {

            for (int i = 0; i < gpsRecordsFiles.length; i++) {
                gpsRecordInputStreams[i] = gpsRecordsFiles[i].getInputStream();
            }

            nodeInputStream = nodeFile.getInputStream();
            edgeInputStream = edgeFile.getInputStream();
            stationInputStream = stationFile.getInputStream();

            List<ChongqingTaxiGpsResolver.TaxiGpsRecord> records = ChongqingTaxiGpsResolver.readTaxiRecords(gpsRecordInputStreams);
            List<ChongqingMapResolver.ChongqingMapEdge> edges = ChongqingMapResolver.readEdges(edgeInputStream);
            List<ChongqingMapResolver.ChongqingMapNode> nodes = ChongqingMapResolver.readNodes(nodeInputStream);
            SimpleDirectedGraph<Integer, CMapEdge> speedGraph = EmergencySystem.readAvgSpeedLinkMap(records, nodes, edges);
            EmergencyStations stations = EmergencyStations.readFromInputStream(stationInputStream);
            EmergencySystem esystem = new EmergencySystem(speedGraph, stations);


            switch (stationType) {
                case "fire":
                    results.put("distance", esystem.searchNearestFireAlarmCenterByDistance(targetNodeId));
                    results.put("timecost", esystem.searchNearestFireAlarmCenterByTimecost(targetNodeId));
                    break;
                case "police":
                    results.put("distance", esystem.searchNearestPoliceStationByDistance(targetNodeId));
                    results.put("timecost", esystem.searchNearestPoliceStationByTimecost(targetNodeId));
                    break;
                case "hospital":
                    results.put("distance", esystem.searchNearestHospitalByDistance(targetNodeId));
                    results.put("timecost", esystem.searchNearestHospitalByTimecost(targetNodeId));
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(CodeMsg.ERROR);
        } finally {

            try {

                for (InputStream is : gpsRecordInputStreams) {
                    is.close();
                }

                nodeInputStream.close();
                edgeInputStream.close();
                stationInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String resultString = "";
        for (Map.Entry<String, Map<String, GraphPath<Integer, CMapEdge>>> e : results.entrySet()) {
            String searchType = e.getKey();
            for (Map.Entry<String, GraphPath<Integer, CMapEdge>> ee : e.getValue().entrySet()) {
                String stationName = ee.getKey();
                String path = ee.getValue().getVertexList().toString();
                resultString += "Search by " + searchType + ", from station " + stationName + ": " + path + "\n";
            }
        }

        return Result.success(resultString);
    }

    @PostMapping("emergency/test")
    @ResponseBody
    public Result<String> test(@RequestParam("stationType") String stationType,
                               @RequestParam("targetNodeId") int targetNodeId,
                               HttpServletResponse response) {

        System.out.println("Receiving a request");
        return Result.success("success");
    }

    private static class ResponsePath {
        String stationName;
        int stationNodeId;
        int[] nodeRoute;
        double totalDistance;
        double totalTimeCost;

        public ResponsePath(String stationName, int stationNodeId, int[] nodeRoute, double totalDistance, double totalTimeCost) {
            this.stationName = stationName;
            this.stationNodeId = stationNodeId;
            this.nodeRoute = nodeRoute;
            this.totalDistance = totalDistance;
            this.totalTimeCost = totalTimeCost;
        }
    }
}
