package org.lab1505.ue.entity;


import java.util.Objects;

public class LinkEdge {
    private int from;
    private int to;
    private double capacity;
    private double length;
    private double ftime;
    private double b;
    private double power;
    private double speed;
    private double toll;
    private int type;

    public LinkEdge(int from, int to, double capacity, double length, double ftime, double b, double power, double speed, double toll, int type) {
        this.from = from;
        this.to = to;
        this.capacity = capacity;
        this.length = length;
        this.ftime = ftime;
        this.b = b;
        this.power = power;
        this.speed = speed;
        this.toll = toll;
        this.type = type;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public double getCapacity() {
        return capacity;
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getFtime() {
        return ftime;
    }

    public void setFtime(double ftime) {
        this.ftime = ftime;
    }

    public double getB() {
        return b;
    }

    public void setB(double b) {
        this.b = b;
    }

    public double getPower() {
        return power;
    }

    public void setPower(double power) {
        this.power = power;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getToll() {
        return toll;
    }

    public void setToll(double toll) {
        this.toll = toll;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "LinkEdge{" +
            "from=" + from +
            ", to=" + to +
            ", capacity=" + capacity +
            ", length=" + length +
            ", ftime=" + ftime +
            ", b=" + b +
            ", power=" + power +
            ", speed=" + speed +
            ", toll=" + toll +
            ", type=" + type +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinkEdge linkEdge = (LinkEdge) o;
        return from == linkEdge.from &&
            to == linkEdge.to &&
            Double.compare(linkEdge.capacity, capacity) == 0 &&
            Double.compare(linkEdge.length, length) == 0 &&
            Double.compare(linkEdge.ftime, ftime) == 0 &&
            Double.compare(linkEdge.b, b) == 0 &&
            Double.compare(linkEdge.power, power) == 0 &&
            Double.compare(linkEdge.speed, speed) == 0 &&
            Double.compare(linkEdge.toll, toll) == 0 &&
            type == linkEdge.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, capacity, length, ftime, b, power, speed, toll, type);
    }
}
