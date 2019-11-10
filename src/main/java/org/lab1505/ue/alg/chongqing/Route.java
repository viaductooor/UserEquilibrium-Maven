package org.lab1505.ue.alg.chongqing;

import java.util.List;

public class Route<T> {
    private List<T> route;
    private double length;
    private int numNodes;
    private double timeCost;

    public Route(List<T> route, double length, int numNodes, double timeCost) {
        this.route = route;
        this.length = length;
        this.numNodes = numNodes;
        this.timeCost = timeCost;
    }

    public Route(List<T> route) {
        this.route = route;
        this.numNodes = route.size();
        this.length = -1;
        this.timeCost = -1;
    }

    public List<T> getRoute() {
        return route;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public int getNumNodes() {
        return numNodes;
    }

    public double getTimeCost() {
        return timeCost;
    }

    public void setTimeCost(double timeCost) {
        this.timeCost = timeCost;
    }
}
