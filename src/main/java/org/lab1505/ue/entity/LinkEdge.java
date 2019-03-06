package org.lab1505.ue.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class LinkEdge{
    private int from;
    private int to;
    private double capacity;
    private double length;
    private double ftime;
    private double b;
    private double power;
    private double speed;
    private double toll;
    private  int type;
}
