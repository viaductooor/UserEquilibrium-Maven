package org.lab1505.ue.alg;

import java.util.HashMap;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.SingleSourcePaths;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.lab1505.ue.entity.ChangeDemandEdge;
import org.lab1505.ue.entity.DemandEdge;
import org.lab1505.ue.entity.UeLinkEdge;
import org.lab1505.ue.fileutil.CsvGraphWriter;

public final class ChangeDemand {
    private double uediff = 50;
    private SimpleDirectedWeightedGraph<Integer, UeLinkEdge> mNet;
    private SimpleDirectedGraph<Integer, ChangeDemandEdge> mTrips;
    private DijkstraShortestPath<Integer, UeLinkEdge> dsp;
    private UserEquilibrium<Integer, ChangeDemandEdge, UeLinkEdge> ue;

    public ChangeDemand(SimpleDirectedWeightedGraph<Integer, UeLinkEdge> net,
            SimpleDirectedGraph<Integer, DemandEdge> trips) {
        mNet = net;
        mTrips = new SimpleDirectedGraph<>(ChangeDemandEdge.class);
        for (Integer vertex : trips.vertexSet()) {
            mTrips.addVertex(vertex);
        }
        for (DemandEdge edge : trips.edgeSet()) {
            int source = trips.getEdgeSource(edge);
            int target = trips.getEdgeTarget(edge);
            ChangeDemandEdge cde = new ChangeDemandEdge(edge, source, target);
            mTrips.addEdge(source, target, cde);
        }

        this.dsp = new DijkstraShortestPath<>(mNet);
        this.ue = new UserEquilibrium<>(mNet, mTrips);
    }

    private void init() {
        /**
         * set original total cost
         */
        ue.assign(uediff);
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
    private double getMarginalCost(double volume, double fftt, double cap) {
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
     * load some, like 5% or 1% of original demand and update travel_time of every
     * link. then update the travel time of every link that composes the shrotest
     * path.
     * 
     * @param n       pecentage by which to add on the demand edge
     * @param demande the demand edge to load
     */
    private void load(double n, ChangeDemandEdge demande,
            HashMap<Integer, SingleSourcePaths<Integer, UeLinkEdge>> paths) {
        double demand = demande.getOriginDemand();
        double per = demande.getIncrePercentage();
        per = per + n;
        demande.setIncrePercentage(per);
        int origin = mTrips.getEdgeSource(demande);
        int des = mTrips.getEdgeTarget(demande);
        GraphPath<Integer, UeLinkEdge> path = paths.get(origin).getPath(des);
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
        int count1 = 0;
        int count2;
        int lockcount;
        double diff;

        do {
            count1++;
            ue.assign(uediff);

            count2 = 0;
            lockcount = 0;
            clearPercentage();
            clearLock();

            /**
             * clear the volume of every link which will be set later in the step "load"
             */
            ue.init();

            do {

                System.out.println("outter loop:" + count1 + "; inner loop: " + count2++);

                HashMap<Integer, SingleSourcePaths<Integer, UeLinkEdge>> paths = new HashMap<>();
                for (ChangeDemandEdge edge : mTrips.edgeSet()) {
                    int o = edge.getOrigin();
                    paths.put(o, dsp.getPaths(o));
                }

                for (ChangeDemandEdge edge : mTrips.edgeSet()) {
                    if (edge.isLock() == false) {
                        if (edge.getCost() > edge.getOriginCost() || edge.getIncrePercentage() >= 10) {
                            edge.setLock(true);
                            lockcount++;
                        } else {
                            load(demandStep, edge, paths);
                        }
                    }
                }

                for (ChangeDemandEdge edge : mTrips.edgeSet()) {
                    updateCost(edge,paths);
                }

            } while (lockcount < mTrips.edgeSet().size());

            updateDemandBasedOnPercentage();
            updataMarginalCostAndSurcharge(count1);

            CsvGraphWriter.writeTo(mTrips, ChangeDemandEdge.class, "trips.csv");
            CsvGraphWriter.writeTo(mNet, UeLinkEdge.class, "net.csv");

            // /**
            // * check each link travel time, if over congestion time: >t_0 (1+0.15). End
            // * while.
            // */
            // for (UeLinkEdge l : mNet.edgeSet()) {
            // if (l.getTraveltime() > 1.15 * l.getFtime()) {
            // // if congested
            // l.updateSurcharge(l.getRecentSurcharge() * 1.1);
            // }
            // }
            diff = getSuchargeDiff();
        } while (diff > surchargeDiff);

        for (UeLinkEdge edge : mNet.edgeSet()) {
            edge.updateTraveltimeWithoutSurcharge();
        }
    }

    /**
     * Set each ChangeDemandEdge's increPercentage to 0.
     */
    public void clearPercentage() {
        for (ChangeDemandEdge edge : mTrips.edgeSet()) {
            edge.setIncrePercentage(0.0);
        }
    }

    /**
     * Set each ChangeDemandEdge's lock to false.
     */
    public void clearLock() {
        for (ChangeDemandEdge edge : mTrips.edgeSet()) {
            edge.setLock(false);
        }
    }

    /**
     * update marginal cost and link surcharge
     * 
     * @param n iteration number
     */
    private void updataMarginalCostAndSurcharge(int n) {
        for (UeLinkEdge edge : mNet.edgeSet()) {
            double marginalCost = getMarginalCost(edge.getVolume(), edge.getFtime(), edge.getCapacity());
            double nowSurcharge = edge.getRecentSurcharge();
            double sur = computeSurcharge(n, marginalCost, nowSurcharge);
            edge.updateSurcharge(sur);
        }
    }

    /**
     * Update each demand edge's demand based on its increPercentage.
     */
    private void updateDemandBasedOnPercentage() {
        for (ChangeDemandEdge edge : mTrips.edgeSet()) {
            double demand = edge.getDemand();
            double percentage = edge.getIncrePercentage();
            edge.setDemand(demand * percentage);
        }
    }

    /**
     * Calculate the trip's shortest path first according to the present mNet, and
     * pass its total cost to {@link ChangeDemandEdge#setCost(double)}.
     * 
     * @param edge
     */
    public void updateCost(ChangeDemandEdge edge, HashMap<Integer,SingleSourcePaths<Integer,UeLinkEdge>> paths) {
        int origin = edge.getOrigin();
        int des = edge.getDestination();
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

    /**
     * Update an edge's weight according to its own
     * {@link UeLinkEdge#getTraveltime()}
     * 
     * @param edge the edge to update weight
     */
    public void updateWeight(UeLinkEdge edge) {
        int source = mNet.getEdgeSource(edge);
        int target = mNet.getEdgeTarget(edge);
        mNet.setEdgeWeight(source, target, edge.getTraveltime());
    }

    public SimpleDirectedGraph<Integer, ChangeDemandEdge> getTripsGraph() {
        return mTrips;
    }

    public SimpleDirectedGraph<Integer, UeLinkEdge> getNetGraph() {
        return mNet;
    }

}