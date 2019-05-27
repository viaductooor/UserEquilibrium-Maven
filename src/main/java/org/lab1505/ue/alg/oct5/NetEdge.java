package org.lab1505.ue.alg.oct5;

import org.lab1505.ue.entity.SurchargePool;

public class NetEdge {
     double otherVolume;
     double taxiVolume;
     double initialTraveltime;
     double traveltime;
     double length;
     int numLanes;
     SurchargePool surchargePool;

    public NetEdge(double length, double initialTraveltime, double othervolume) {
        this.otherVolume = othervolume;
        this.taxiVolume = 0;
        this.initialTraveltime = initialTraveltime;
        this.traveltime = initialTraveltime;
        this.surchargePool = new SurchargePool();
        this.length = length;
        this.numLanes = 1;
    }
}