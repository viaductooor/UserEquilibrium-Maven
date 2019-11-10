package org.lab1505.ue.entity;


public class DemandEdge{
    private double demand;

    public DemandEdge(double demand) {
        this.demand = demand;
    }

    public double getDemand() {
        return demand;
    }

    public void setDemand(double demand) {
        this.demand = demand;
    }

    @Override
    public String toString() {
        return "DemandEdge{" +
            "demand=" + demand +
            '}';
    }
}
