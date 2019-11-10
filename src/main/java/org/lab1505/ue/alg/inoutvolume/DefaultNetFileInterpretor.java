package org.lab1505.ue.alg.inoutvolume;

public class DefaultNetFileInterpretor implements NetFileInterpretor {
    @Override
    public double getOtherVolume(String[] line) {
        return Integer.parseInt(line[7]) * Double.parseDouble(line[9]);
    }

    @Override
    public int getNumLanes(String[] line) {
        return Integer.parseInt(line[7]);
    }

    @Override
    public double getLength(String[] line) {
        return Double.parseDouble(line[5]);
    }

    @Override
    public double getInitTraveltime(String[] line) {
        return Double.parseDouble(line[3]);
    }

    @Override
    public String getInitNode(String[] line) {
        return line[0];
    }

    @Override
    public String getEenNode(String[] line) {
        return line[1];
    }
}
