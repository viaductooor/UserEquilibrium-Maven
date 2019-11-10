package org.lab1505.ue.alg.chongqing;

public class CMapTrip {
    public String startTime;
    public String endTime;
    public String startDate;
    public String endDate;
    public Integer startNodeId;
    public Integer endNodeId;
    public double startNodeLat;
    public double startNodeLon;
    public double endNodeLat;
    public double endNodeLon;

    public CMapTrip(String startTime, String endTime, String startDate, String endDate,
                    Integer startNodeId, Integer endNodeId, double startNodeLat, double startNodeLon,
                    double endNodeLat, double endNodeLon) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.startDate = startDate;
        this.endDate = endDate;
        this.startNodeId = startNodeId;
        this.endNodeId = endNodeId;
        this.startNodeLat = startNodeLat;
        this.startNodeLon = startNodeLon;
        this.endNodeLat = endNodeLat;
        this.endNodeLon = endNodeLon;
    }

    @Override
    public String toString() {
        return "CMapTrip{" +
                "startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", startNodeId=" + startNodeId +
                ", endNodeId=" + endNodeId +
                ", startNodeLat=" + startNodeLat +
                ", startNodeLon=" + startNodeLon +
                ", endNodeLat=" + endNodeLat +
                ", endNodeLon=" + endNodeLon +
                '}';
    }
}
