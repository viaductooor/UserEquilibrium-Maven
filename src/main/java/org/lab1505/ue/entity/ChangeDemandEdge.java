package org.lab1505.ue.entity;

import org.jetbrains.annotations.NotNull;

public class ChangeDemandEdge extends DemandEdge{
    private int origin;
	private int destination;
	private double originCost;
	private double cost;
	private double incrementPercentage;
	private double originDemand;
	private boolean lock;

    public ChangeDemandEdge(int origin, int dest, double demand){
        super(demand);
        this.origin = origin;
		this.destination = dest;
		init();
    }

    public ChangeDemandEdge(@org.jetbrains.annotations.NotNull ChangeDemandEdge edge) {
        super(edge.getDemand());
        this.origin = edge.origin;
		this.destination = edge.destination;
		this.originDemand = edge.originDemand;
		this.cost = edge.cost;
		this.lock = edge.lock;
		this.incrementPercentage = edge.incrementPercentage;
	}

	public ChangeDemandEdge(@NotNull DemandEdge edge, int source, int target){
		super( edge.getDemand());
		this.origin = source;
		this.destination = target;
		init();
	}

	private void init(){
		this.originDemand = getDemand();
		this.incrementPercentage = 0;
		this.lock = false;
		this.cost = 0;
		this.originCost = 0;
	}

    public int getOrigin() {
        return origin;
    }

    public void setOrigin(int origin) {
        this.origin = origin;
    }

    public int getDestination() {
        return destination;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }

    public double getOriginCost() {
        return originCost;
    }

    public void setOriginCost(double originCost) {
        this.originCost = originCost;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public double getIncrementPercentage() {
        return incrementPercentage;
    }

    public void setIncrementPercentage(double incrementPercentage) {
        this.incrementPercentage = incrementPercentage;
    }

    public double getOriginDemand() {
        return originDemand;
    }

    public void setOriginDemand(double originDemand) {
        this.originDemand = originDemand;
    }

    public boolean isLock() {
        return lock;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }
}
