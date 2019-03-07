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
import org.lab1505.ue.fileutil.FileDirectoryGenerator;

public final class ChangeDemand {
    private double uediff = 50;
    private int lockcount = 0;
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
     * Load method affects volume, traveltime and weight.
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
    public void changeDemand(double demandStep, double targetDiff) {
        init();
        int nSurchargeloop = 0;
        double recentDiff = Double.MAX_VALUE;

        do {
            ue.assign(uediff);
            loadAndChangeDemandIncrementally(demandStep);
            updateDemandBasedOnPercentage();
            updataMarginalCostAndSurcharge(++ nSurchargeloop);
            writeTripsAndNet("trips", "net");

            {
                ue.assign(uediff);
                int numUnqualified = 0;
                for(UeLinkEdge edge:mNet.edgeSet()){
                    if(edge.getVolume()>edge.getCapacity()){
                        edge.updateSurcharge(edge.getRecentSurcharge()*(1.05));
                        numUnqualified ++;
                    }
                }
                if(numUnqualified == 0) return;
            }

            recentDiff = getSuchargeDiff();
        } while (nSurchargeloop<10);
    }

    /**
     * Incrementally load demand until all demand-edges' cost greater than their original cost.
     * 
     * @param demandStep how much to load each time
     */
    private void loadAndChangeDemandIncrementally(double demandStep) {
        resetTrips();
        resetNet();
        int nLoadLoop = 0;
        HashMap<Integer, SingleSourcePaths<Integer, UeLinkEdge>> paths;
        paths = getUpdatedShortestPaths();
        do {
            nLoadLoop ++;
            for (ChangeDemandEdge edge : mTrips.edgeSet()) {
                if (edge.isLock() == false) {
                    if (edge.getCost() > edge.getOriginCost() || edge.getIncrePercentage() >= 10) {
                        edge.setLock(true);
                        lockcount++;
                    } else {
                        load(demandStep, edge, paths); //Affects volume, traveltime and weight
                    }
                }
            }

            paths = getUpdatedShortestPaths();
            for (ChangeDemandEdge edge : mTrips.edgeSet()) {
                updateCost(edge, paths);
            }

        } while (lockcount < mTrips.edgeSet().size());
        System.out.println("nLoadLoop:"+nLoadLoop);
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
     * Get shortest paths based on recent mNet.
     * 
     * @return paths
     */
    public HashMap<Integer,SingleSourcePaths<Integer,UeLinkEdge>> getUpdatedShortestPaths(){
        HashMap<Integer, SingleSourcePaths<Integer, UeLinkEdge>> paths = new HashMap<>();
        for (ChangeDemandEdge edge : mTrips.edgeSet()) {
            int o = edge.getOrigin();
            paths.put(o, dsp.getPaths(o));
        }
        return paths;
    }

    /**
     * Output the trips and the net.
     * 
     * @param tripsFilename filename of the trips
     * @param netFilename filename of the net
     */
    private void writeTripsAndNet(String tripsFilename,String netFilename) {
        CsvGraphWriter.writeTo(mTrips, ChangeDemandEdge.class,
                FileDirectoryGenerator.createFileAutoRename(tripsFilename, "csv"));
        CsvGraphWriter.writeTo(mNet, UeLinkEdge.class, FileDirectoryGenerator.createFileAutoRename(netFilename, "csv"));
    }

    /**
     * Clear every UeLinkEdge's volume and auxVolume in mNet.
     */
    private void resetNet() {
        for (UeLinkEdge edge : mNet.edgeSet()) {
            edge.setVolume(0);
            edge.setAuxVolume(0);
        }
    }

    /**
     * Reset the trips. Details are as follow:
     * <p>
     * 1. Set demand to originalDemand;
     * </p>
     * <p>
     * 2. Clear increPercentage.
     * </p>
     * <p>
     * 3. Set cost to 0.
     * </p>
     * <p>
     * 4. Unlocl every demand edge.
     * </p>
     */
    private void resetTrips() {
        for (ChangeDemandEdge edge : mTrips.edgeSet()) {
            edge.setDemand(edge.getOriginDemand());
            edge.setIncrePercentage(0.0);
            edge.setCost(0);
            edge.setLock(false);
        }
        lockcount = 0;

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
     * Get the trip's shortest path weight according to paths, and
     * pass it to {@link ChangeDemandEdge#setCost(double)}.
     * 
     * @param edge
     */
    public void updateCost(ChangeDemandEdge edge, HashMap<Integer, SingleSourcePaths<Integer, UeLinkEdge>> paths) {
        int origin = edge.getOrigin();
        int des = edge.getDestination();
        edge.setCost(paths.get(origin).getPath(des).getWeight());
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