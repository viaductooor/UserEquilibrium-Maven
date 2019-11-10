package org.lab1505.ue.alg;

import org.jgrapht.Graph;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.lab1505.ue.entity.DemandEdge;
import org.lab1505.ue.entity.LinkEdge;
import org.lab1505.ue.entity.UeEdge;
import org.lab1505.ue.entity.UeLinkEdge;
import org.lab1505.ue.fileutil.CsvGraphWriter;
import org.lab1505.ue.fileutil.TntpReader;

import java.io.File;

public class Game {
    public double beta;
    public double omega;
    public double d;

    private SimpleDirectedGraph<Integer, LinkEdge> links;
    private SimpleDirectedGraph<Integer, DemandEdge> trips;

    private int linkNumber;
    private int n_iteration;
    private double v;

    public Game(SimpleDirectedGraph<Integer, LinkEdge> net, SimpleDirectedGraph<Integer, DemandEdge> trips) {
        this.links = net;
        this.trips = trips;
        this.linkNumber = net.edgeSet().size();
        this.omega = 0;
        this.d = 1;
        this.v = 0;
    }

    public static void main(String[] args) {
        SimpleDirectedGraph<Integer, LinkEdge> links = TntpReader.readNet(TntpReader.SIOUXFALLS_NET);
        SimpleDirectedGraph<Integer, DemandEdge> trips = TntpReader.readTrips(TntpReader.SIOUXFALLS_TRIP);
        Game game = new Game(links, trips);
        Graph<Integer, GameLink> res = game.run(10, 1, 1, 0.001);
        CsvGraphWriter.writeTo(res, GameLink.class, new File("output/game_res.csv"));
    }

    public SimpleDirectedWeightedGraph<Integer, GameLink> transGraph(Graph<Integer, LinkEdge> graph) {
        SimpleDirectedWeightedGraph<Integer, GameLink> newGraph = new SimpleDirectedWeightedGraph<>(GameLink.class);
        for (LinkEdge e : graph.edgeSet()) {
            Integer begin = graph.getEdgeSource(e);
            Integer end = graph.getEdgeTarget(e);
            newGraph.addVertex(begin);
            newGraph.addVertex(end);
            newGraph.addEdge(begin, end, new GameLink(e));
        }
        return newGraph;
    }

    public Graph<Integer, GameLink> run(double beta, double omega, double d, double delta) {
        SimpleDirectedWeightedGraph<Integer, GameLink> gameGraph = transGraph(links);
        UserEquilibrium ue = new UserEquilibrium(gameGraph, trips);
        double _prev = 0;
        double _v = 0;
        n_iteration = 1;


        for (GameLink l : gameGraph.edgeSet()) {
            l.rho = 1f / linkNumber;
            l.gamma = 0;
            l.c_normal = l.getFtime();
            l.c_fail = beta * linkNumber;
        }

        n_iteration = 2;

        do {
            _prev = v; // save the previous V
            _v = 0; // initiate the present V
            for (GameLink l : gameGraph.edgeSet()) {
                double _rho = l.rho;
                double _t = l.t;
                double _s = (1 - _rho) * l.c_normal + _rho * l.c_fail;
                _t = (1f / n_iteration) * _s + (1 - 1f / n_iteration) * _t;
                l.s = _s;
                l.t = _t;
            }

            ue.assign(50);

            double _totalflow = ue.getTotalVolume();
            double _sum = 0;
            for (GameLink l : gameGraph.edgeSet()) {
                _sum += l.rho * (l.t + omega) / d;
            }
            for (GameLink l : gameGraph.edgeSet()) {
                double _gamma = l.getVolume() / _totalflow;
                double _rho = ((_gamma * (l.t + omega)) / d) / _sum;
                l.gamma = _gamma;
                l.rho = _rho;
            }
            for (GameLink l : gameGraph.edgeSet()) {
                _v += l.rho * l.gamma * l.t;
            }
            v = _v;
            n_iteration++;

        } while (Math.abs(_prev - _v) > delta);
        return gameGraph;
    }

    public class GameLink extends UeLinkEdge implements UeEdge {
        public double rho;
        public double gamma;
        public double c_normal;
        public double c_fail;
        public double s;
        public double t;
        public double delta;

        public GameLink(LinkEdge e) {
            super(e);
            this.rho = 0;
            this.gamma = 0;
            this.c_normal = 0;
            this.c_fail = 0;
            this.s = 0;
            this.t = 0;
            this.delta = 0;
        }
    }
}
