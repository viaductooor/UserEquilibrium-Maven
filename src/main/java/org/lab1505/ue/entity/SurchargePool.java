package org.lab1505.ue.entity;

import java.util.LinkedList;

import org.lab1505.ue.exception.SurchargePoolException;

/**
 * SurchargePool is like a fix-sized queue, when its full, add one surcharge
 * will add it to the head of the queue and remove the last one. Otherwise add
 * it to the head only.
 */
public class SurchargePool {
    /**
     * Basic list that restores surcharges.
     */
    private LinkedList<Double> pool;
    private double surchargeRate;

    /**
     * Create a surcharge of a specific size.
     * 
     * @param size size of the surchargePool
     * @throws SurchargePoolException when size is too small
     */
    public SurchargePool(int size) throws SurchargePoolException {
        if (size < 1) {
            throw new SurchargePoolException("Size of surchargePool should be greater than 0.");
        }
        this.pool = new LinkedList<Double>();
        for (int i = 0; i < size; i++) {
            pool.add(0.0);
        }
        surchargeRate = 0;
    }

    /**
     * Create a surchargePool whose size is 2 by default.
     * 
     */
    public SurchargePool(){
        this.pool = new LinkedList<Double>();
        pool.add(0.0);
        pool.add(0.0);
        surchargeRate = 0;
    }

    /**
     * Add a new surcharge to the head of surchargePool.
     * 
     * @param surcharge surcharge to add
     */
    public void add(double surcharge) {
        pool.removeLast();
        pool.addFirst(surcharge*(1+surchargeRate));
    }

    /**
     * Get first surcharge of the surchargePool.
     * 
     * @return first (recent) surcharge
     */
    public double getRecentSurcharge() {
        return pool.getFirst();
    }

    /**
     * Get the second surcharge of surchargePool if its size > 1, otherwise get the first one.
     * 
     * @return last surcharge
     */
    public double getLastSurcharge() {

        if (pool.size() == 1) {
            return pool.getFirst();
        }
        return pool.get(1);
    }

    /**
     * Get the difference between recentSurcharge and lastSurcharge.
     * 
     * @return abs(firstSurcharge-lastSurcharge)
     */
    public double getSurchargeDiff() {
        return Math.abs(this.getRecentSurcharge() - this.getLastSurcharge());
    }

    /**
     * Get the size of the surchargePool.
     * 
     * @return size of the surchargePool.
     */
    public int size() {
        return pool.size();
    }

    @Override
    public String toString(){
        return pool.toString()+","+surchargeRate;
    }

    public void setSurchargeRate(double rate){
        this.surchargeRate = rate;
    }

    public double getSurchargeRate(){
        return surchargeRate;
    }
}