package org.lab1505.ue.alg;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.lab1505.ue.entity.ChangeDemandEdge;
import org.lab1505.ue.entity.DemandEdge;
import org.lab1505.ue.entity.UeLinkEdge;

public final class ChangeDemand {
    private double uediff = 50;
    private SimpleDirectedWeightedGraph<Integer, UeLinkEdge> mNet;
    private SimpleDirectedGraph<Integer, ChangeDemandEdge> mTrips;
    private DijkstraShortestPath<Integer, UeLinkEdge> dsp;

    public ChangeDemand(SimpleDirectedWeightedGraph<Integer, UeLinkEdge> net,
            SimpleDirectedGraph<Integer, DemandEdge> trips) {
        mNet = net;
        mTrips = new SimpleDirectedGraph<>(ChangeDemandEdge.class);
        for(Integer vertex:trips.vertexSet()){
            mTrips.addVertex(vertex);
        }
        for(DemandEdge edge:trips.edgeSet()){
            int source = trips.getEdgeSource(edge);
            int target = trips.getEdgeTarget(edge);
            ChangeDemandEdge cde = new ChangeDemandEdge(edge, source, target);
            mTrips.addEdge(source, target,cde);
        }

        this.dsp = new DijkstraShortestPath<>(mNet);
    }

    private void init() {
        UserEquilibrium<Integer, ChangeDemandEdge, UeLinkEdge> ue = new UserEquilibrium<>(mNet, mTrips);
        /**
         * set original total cost
         */
        ue.assign(uediff);
        updateAllWeight();
        for (ChangeDemandEdge trip : mTrips.edgeSet()) {
            int origin = trip.getOrigin();
            int destination = trip.getDestination();
            double cost = dsp.getPathWeight(origin, destination);
            trip.setOriginCost(cost);
        }
        
    }

    /**
     * compute marginal cost based volume, freeFlowTime and Capacity
     * 
     * @param volume
     * @param fftt
     * @param cap
     * @return
     */
    private double computeMarginalCost(double volume, double fftt, double cap) {
        double result = (double) (volume * fftt * (4 * Math.pow(volume, 3) * 0.15 / Math.pow(cap, 4)));
        return result;
    }

    /**
     * compute surcharge based on marginalCost and last surcharge
     * 
     * @param n
     * @param marginalCost
     * @param lastSurcharge
     * @return
     */
    public double computeSurcharge(int n, double marginalCost, double lastSurcharge) {
        double result = (1f / n) * marginalCost + (1 - (1f / n)) * lastSurcharge;
        return result;
    }

    public double computeTravelTime(double flow, double capacity, double freeFlowTime) {

        return ((Math.pow(flow / capacity, 4)) * 0.15 + 1) * freeFlowTime;
    }

    /**
     * 
     * @param list
     * @return
     */
    private double getSuchargeDiff() {
        double sum = 0;
        for (UeLinkEdge edge : mNet.edgeSet()) {
            sum += edge.getSurchargeDiff();
        }
        return sum;
    }

    /**
     * load n(percentage) of the demand,change flow and traveltime of related links
     * 
     * @param n
     * @param odp
     */
    private void load(double n, ChangeDemandEdge demande) {
        double demand = demande.getOriginDemand();
        double per = demande.getIncrePercentage();
        per = per + n;
        demande.setIncrePercentage(per);
        int origin = mTrips.getEdgeSource(demande);
        int des = mTrips.getEdgeTarget(demande);
        GraphPath<Integer, UeLinkEdge> path = dsp.getPath(origin, des);
        for (UeLinkEdge edge : path.getEdgeList()) {
            edge.setVolume(edge.getVolume() + n * demand);
            edge.updateTraveltime();
            updateWeight(edge);
        }
    }

    /**
     * main method of this class
     */
    public void changeDemand(double demandStep, double surchargeDiff) {

        init();
        UserEquilibrium<Integer, ChangeDemandEdge, UeLinkEdge> ue = new UserEquilibrium<>(mNet, mTrips);
        int count1 = 0;
        int count2;
        int lockcount;
        double diff;

        do {
            count1++;
            ue.assign(uediff);
            updateAllWeight();

            /**
             * update marginal cost and link surcharge
             */
            for (UeLinkEdge edge : mNet.edgeSet()) {
                double marginalCost = computeMarginalCost(edge.getVolume(), edge.getFtime(), edge.getCapacity());
                double nowSurcharge = edge.getRecentSurcharge();
                double sur = computeSurcharge(count1, marginalCost, nowSurcharge);
                // sur *= 1.1f;
                edge.updateSurcharge(sur);
            }

            count2 = 0;
            lockcount = 0;
            clearPercentage();
            clearLock();

            /**
             * clear the volume of every link which will be set later in the step "load"
             */
            for (UeLinkEdge edge : mNet.edgeSet()) {
                edge.setVolume(0.0);
            }

            do {

                /**
                 * compute shortest path of all OD pairs
                 */
                count2++;
                System.out.println("outter loop:" + count1 + "; inner loop: " + count2);

                /**
                 * for each OD Pair, find shortest path if it's total cost < original cost then
                 * load 5% original demand(or something else)
                 */
                for (ChangeDemandEdge edge : mTrips.edgeSet()) {
                    int o = edge.getOrigin();
                    int d = edge.getDestination();
                    double totalCost = dsp.getPathWeight(o, d);
                    if (totalCost > edge.getOriginCost() || edge.getIncrePercentage() >= 1) {
                        edge.setLock(true);
                        lockcount++;
                    }
                    if (edge.isLock() == false) {
                        /**
                         * load some, like 5% or 1% of original demand and update travel_time of every
                         * link. then update the travel time of every link that composes the shrotest
                         * path.
                         */
                        load(demandStep, edge);
                    }
                }

                /**
                 * update the cost of all ODPairs based on the composing links
                 */
                for (ChangeDemandEdge edge : mTrips.edgeSet()) {
                    updateCost(edge);
                }

            } while (lockcount < mTrips.edgeSet().size());

            /**
             * update demand
             */
            for (ChangeDemandEdge edge : mTrips.edgeSet()) {
                double demand = edge.getDemand();
                double percentage = edge.getIncrePercentage();
                edge.setDemand(demand * percentage);
            }

            /**
             * check each link travel time, if over congestion time: >t_0 (1+0.15). End
             * while.
             */
            // for (UeLinkEdge l : mNet.edgeSet()) {
            //     if (l.getTraveltime() > 1.15 * l.getFtime()) {
            //         // if congested
            //         l.updateSurcharge(l.getRecentSurcharge() * 1.1);
            //     }
            // }
            diff = getSuchargeDiff();
            System.out.println("surcharge diff:" + diff);
        } while (diff > surchargeDiff);

        for (UeLinkEdge edge : mNet.edgeSet()) {
            edge.updateTraveltimeWithoutSurcharge();
        }
    }

    public void clearPercentage() {
        for (ChangeDemandEdge edge : mTrips.edgeSet()) {
            edge.setIncrePercentage(0.0);
        }
    }

    public void clearLock() {
        for (ChangeDemandEdge edge : mTrips.edgeSet()) {
            edge.setLock(false);
        }
    }

    /**
     * 
     * @param f
     * @param ll
     * @param ol
     */
    public void updateCost(ChangeDemandEdge edge) {
        int origin = edge.getOrigin();
        int des = edge.getDestination();
        DijkstraShortestPath<Integer, UeLinkEdge> dsp = new DijkstraShortestPath<>(mNet);
        edge.setCost(dsp.getPathWeight(origin, des));
    }

    /**
     * Update mNet's weight using the edges' traveltime.
     */
    public void updateAllWeight() {
        for (UeLinkEdge edge : mNet.edgeSet()) {
            Integer source = mNet.getEdgeSource(edge);
            Integer target = mNet.getEdgeTarget(edge);
            mNet.setEdgeWeight(source, target, edge.getTraveltime());
        }
    }

    public void updateWeight(UeLinkEdge edge){
        int source = mNet.getEdgeSource(edge);
        int target = mNet.getEdgeTarget(edge);
        mNet.setEdgeWeight(source, target, edge.getTraveltime());
    }

    public SimpleDirectedGraph<Integer,ChangeDemandEdge> getTripsGraph(){
        return mTrips;
    }

}