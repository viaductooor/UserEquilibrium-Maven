package org.lab1505.ue.alg.chongqing;

import org.lab1505.ue.entity.LinkEdge;

/**
 * Convert ChongqingMapEdge to LinkEdge (like Sioux Falls, Chicago etc.)
 */
public class ChongqingMapEdgeAdapter extends LinkEdge {

    public ChongqingMapEdgeAdapter(int from, int to, double capacity, double length, double ftime, double b, double power, double speed, double toll, int type) {
        super(from, to, capacity, length, ftime, b, power, speed, toll, type);
    }
}
