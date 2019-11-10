package org.lab1505.ue.alg.chongqing;

public class CMapEdge {
    public double averageTraveltime;
    public double length;
    public double traveltime;

    public CMapEdge(double averageTraveltime, double length) {
        this.averageTraveltime = averageTraveltime;
        this.length = length;
        this.traveltime = averageTraveltime;
    }

    public void refreshTraveltime() {

    }

    @Override
    public String toString() {
        return "CMapEdge{" +
                "averageTraveltime=" + averageTraveltime +
                ", length=" + length +
                ", traveltime=" + traveltime +
                '}';
    }
}
