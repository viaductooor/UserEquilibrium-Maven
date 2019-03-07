package org.lab1505.ue.entity;

import org.lab1505.ue.fileutil.Ignore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeDemandEdge extends DemandEdge{
    private int origin;
	private int destination;
	private double originCost;
	private double cost;
	private double increPercentage;
	private double originDemand;
	private boolean lock;
    
    public ChangeDemandEdge(int origin, int dest, double demand){
        super(demand);
        this.origin = origin;
		this.destination = dest;
		init();
    }

    public ChangeDemandEdge(ChangeDemandEdge edge) {
        super(edge.getDemand());
        this.origin = edge.origin;
		this.destination = edge.destination;
		this.originDemand = edge.originDemand;
		this.cost = edge.cost;
		this.lock = edge.lock;
		this.increPercentage = edge.increPercentage;
	}

	public ChangeDemandEdge(DemandEdge edge,int source,int target){
		super( edge.getDemand());
		this.origin = source;
		this.destination = target;
		init();
	}

	private void init(){
		this.originDemand = getDemand();
		this.increPercentage = 0;
		this.lock = false;
		this.cost = 0;
		this.originCost = 0;
	}
}