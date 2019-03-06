package org.lab1505.ue.entity;

import java.util.Objects;
import java.util.function.Function;

import org.lab1505.ue.exception.SurchargePoolException;

public class UeLinkEdge extends LinkEdge implements UeEdge {
    private double volume;
    private double auxVolume;
    private double traveltime;
    private SurchargePool surchargePool;
    private static Function<UeLinkEdge, Double> traveltimeFunc;

    public UeLinkEdge(LinkEdge e){
        super(e.getFrom(), e.getTo(), e.getCapacity(), e.getLength(), e.getFtime(), e.getB(), e.getPower(),
                e.getSpeed(), e.getToll(), e.getType());
        try {
            this.surchargePool = new SurchargePool();
        } catch (SurchargePoolException ex) {
            ex.printStackTrace();
        }
        traveltimeFunc = createBprTraveltimeFunction();
        resetVolumeAndTraveltime();
    }

    public void setTraveltimeFunction(Function<UeLinkEdge, Double> func) {
        Objects.requireNonNull(traveltimeFunc);
        traveltimeFunc = func;
    }

    public static UeLinkEdge fromLinkEdge(LinkEdge e) {
        return new UeLinkEdge(e);
    }

    public double getRecentSurcharge(){
        return surchargePool.getRecentSurcharge();
    }

    public double getLastSurcharge(){
        return surchargePool.getLastSurcharge();
    }

    public void updateSurcharge(double sur){
        surchargePool.add(sur);
    }

    public double getSurchargeDiff(){
        return surchargePool.getSurchargeDiff();
    }

    @Override
    public double getVolume() {
        return volume;
    }

    @Override
    public double getAuxVolume() {
        return auxVolume;
    }

    @Override
    public void setVolume(double volume) {
        this.volume = volume;
    }

    @Override
    public void setAuxVolume(double auxVolume) {
        this.auxVolume = auxVolume;
    }

    @Override
    public double getTraveltime() {
        return traveltime;
    }

    @Override
    public void updateTraveltime() {
        traveltime = traveltimeFunc.apply(this) + getRecentSurcharge();
    }

    public void updateTraveltimeWithoutSurcharge(){
        traveltime = traveltimeFunc.apply(this) - getRecentSurcharge();
    }

    @Override
    public double getTraveltimeIntegral(double alpha) {
        double upper = volume + alpha * (auxVolume - volume);
        double surcharge = getRecentSurcharge();
        double C =  (0.03 * getFtime()) / Math.pow(getCapacity(), 4);
        double result = C * Math.pow(upper, 5) + (getFtime() + surcharge) * upper;
        return result;
    }

    /**
     * Set volume and auxVolume to 0 and update traveltime.
     */
    public void resetVolumeAndTraveltime(){
        volume = 0;
        auxVolume = 0;
        updateTraveltime();
    }

    /**
     * Create a BPR function to calculate traveltime.
     * 
     * @return BPR traveltime function.
     */
    private Function<UeLinkEdge, Double> createBprTraveltimeFunction() {
        return (edge) -> {
            double volume = edge.getVolume();
            double capacity = edge.getCapacity();
            double fftt = edge.getFtime();
            return ((Math.pow(volume / capacity, 4)) * 0.15 + 1) * fftt;
        };
    }


}