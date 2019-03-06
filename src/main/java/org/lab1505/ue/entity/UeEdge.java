package org.lab1505.ue.entity;

/**
 * Edges on which to perfom UE-Assignment must implement this interface
 */
public interface UeEdge{
    /**
     * Return recent volume.
     * 
     * @return recent volume
     */
    double getVolume();
    
    /**
     * Return auxiliary volume, only works in the procedure of UE-Assignment.
     * 
     * @return auxiliary volume
     */
    double getAuxVolume();

    /**
     * Set volume of the edge.
     */
    void setVolume(double volume);

    /**
     * Set auxiliary volume of the edge.
     */
    void setAuxVolume(double auxVolume);

    /**
     * Get recent traveltime of the edge.
     * 
     * @return recent traveltime of the edge
     */
    double getTraveltime();

    /**
     * Update traveltime of the edge
     */
    void updateTraveltime();

    /**
     * Get integral traveltime of the edge.
     * Usually traveltime is a function of volume, so is its integral function.
     */
    double getTraveltimeIntegral(double alpha);
}