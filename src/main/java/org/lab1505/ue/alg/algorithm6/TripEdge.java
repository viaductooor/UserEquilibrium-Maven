package org.lab1505.ue.alg.algorithm6;

import org.lab1505.ue.fileutil.Ignore;

public class TripEdge{
    @Ignore
    public String init;
    @Ignore
    public String end;
    public double leastCost;
    public TripEdge(String init,String end){
        this.init = init;
        this.end = end;
        leastCost = -1;
    }
}