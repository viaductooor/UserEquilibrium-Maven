package org.lab1505.ue.alg;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.lab1505.ue.entity.DemandEdge;
import org.lab1505.ue.entity.LinkEdge;
import org.lab1505.ue.entity.UeEdge;
import org.lab1505.ue.entity.UeLinkEdge;

import java.util.LinkedList;
import java.util.Set;

/**
 * Refer to User Equilibrium Assignment in Sheffi's Urban Transportation
 * Network.
 *
 * @param <T> type of vertex
 * @param <E> type of demand edge
 * @param <D> type of net edge
 */
public class UserEquilibrium<T, E extends DemandEdge, D extends UeEdge> {
	private SimpleDirectedWeightedGraph<T, D> mNet;
	private SimpleDirectedGraph<T, E> mTrips;
	private LinkedList<Double> diffChangeList;

	public UserEquilibrium(SimpleDirectedWeightedGraph<T, D> net, SimpleDirectedGraph<T, E> trips) {
		mNet = net;
		mTrips = trips;
		diffChangeList = new LinkedList<Double>();
	}

	/**
	 * Before performing the assignment, each UeEdge's volume and auxVolume should
	 * be cleared.
	 */
	public void init() {
		for (D edge : mNet.edgeSet()) {
			edge.setVolume(0);
			edge.setAuxVolume(0);
		}
	}

	/**
	 * All-or-nothing assignment. When given a trip(or an origin-destination pair)
	 * of any demand, we first get the shortest path of the trip. Then we add the
	 * demand as volume onto every single link which composes the shortest path.For
	 * example, if we have a trip (a,d) with demand of n, We first get the shortest
	 * path (a,c,d), then we need to add n to the volume of link(a,c) and link(c,d).
	 *
	 */
	private void allOrNothing() {

	    DijkstraShortestPath<T, D> dsp = new DijkstraShortestPath<>(mNet);
		clearAuxVolume();
		for (E edge : mTrips.edgeSet()) {
			T source = mTrips.getEdgeSource(edge);
			T target = mTrips.getEdgeTarget(edge);
			double demand = edge.getDemand();
            if (mNet.containsVertex(source) && mNet.containsVertex(target)) {
                GraphPath<T, D> path = dsp.getPath(source, target);
                if (path != null) {
                    for (D eg : path.getEdgeList()) {
                        eg.setAuxVolume(eg.getAuxVolume() + demand);
                    }
                }
            }

		}
	}

	/**
	 * Line search.Alpha is a parameter between 0 and 1. In this method, we get the
	 * optimal alpha which is going to minimize the total flow(by the step
	 * {@link #move}) by trying incrementally)
	 * @return
	 */
	private double lineSearch() {
		double alpha = 1.0;
		double minSum = Double.POSITIVE_INFINITY;
		Set<D> edges = mNet.edgeSet();
		for (float al = 0; al < 1.00; al += 0.01) {
			double sum = 0;
			for (UeEdge e : edges) {
				sum += e.getTraveltimeIntegral(al);
			}
			if (sum < minSum) {
				alpha = al;
				minSum = sum;
			}
		}
		return alpha;
	}

	/**
	 * Change volume of every link of the graph to decrease the total flow. The
	 * basis of alpha is in {@link #lineSearch}}
	 *
	 * @param alpha
	 */
	private void move(double alpha) {
		Set<D> edges = mNet.edgeSet();
		for (D e : edges) {
			double volume = e.getVolume() + alpha * (e.getAuxVolume() - e.getVolume());
			e.setVolume(volume);
		}
	}

	/**
	 * Set auxVolume(which is also known as y in the algorithm) of every link of the
	 * graph to zero.
	 *
	 */
	private void clearAuxVolume() {
		Set<D> edges = mNet.edgeSet();

		for (D e : edges) {
			e.setAuxVolume(0);
		}
	}

	/**
	 * Set flow of every link with the value of auxVolume. In some of the methods
	 * (eg. allOrNothing) we don't directly change the flow of every link, we
	 * firstly change auxVolume of them and then change flow when needed.
	 */
	private void y2x() {
		Set<D> edges = mNet.edgeSet();
		for (D e : edges) {
			e.setVolume(e.getAuxVolume());
		}
	}

	/**
	 * Update travel-time of every link according to volume, free-flow-travel-time,
	 * capacity etc.
	 */
	private void updateAllTraveltime() {
		Set<D> edges = mNet.edgeSet();
		for (D e : edges) {
			T source = mNet.getEdgeSource(e);
			T target = mNet.getEdgeTarget(e);
			e.updateTraveltime();
			mNet.setEdgeWeight(source, target, e.getTraveltime());
		}
	}

	/**
	 * Get the total flow of a graph composed by UeLinks.
	 *
	 * @return total volume
	 */
    public double getTotalVolume() {
		float sum = 0;
		Set<D> edges = mNet.edgeSet();
		for (D e : edges) {
			sum += e.getVolume();
		}
		return sum;
	}

	/**
	 * Change the graph of {@link LinkEdge} to graph of {@link UeLinkEdge}. When we perform
	 * a user-equilibrium assignment, we have to make sure the links of the graph
	 * have property volume, which is in {@link UeLinkEdge} but not in {@link LinkEdge}.
	 *
	 * @param originalGraph
	 */
	public static <T> SimpleDirectedWeightedGraph<T, UeLinkEdge> fromLinkEdgeGraph(Graph<T, LinkEdge> originalGraph) {
		SimpleDirectedWeightedGraph<T, UeLinkEdge> newGraph = new SimpleDirectedWeightedGraph<>(UeLinkEdge.class);
		Set<LinkEdge> originalEdges = originalGraph.edgeSet();
		Set<T> originalVetices = originalGraph.vertexSet();
		for (T vertex : originalVetices) {
			newGraph.addVertex(vertex);
		}
		for (LinkEdge e : originalEdges) {
			T source = originalGraph.getEdgeSource(e);
			T target = originalGraph.getEdgeTarget(e);
			UeLinkEdge ueedge = new UeLinkEdge(e);
			newGraph.addEdge(source, target, ueedge);
			newGraph.setEdgeWeight(source, target, ueedge.getTraveltime());
		}
		return newGraph;
	}

	/**
	 * Get total travel time of mNet.
	 *
	 * @return total travel time
	 */
	public double getTotalTravelTime() {
		float sum = 0;
		Set<D> edges = mNet.edgeSet();
		for (D e : edges) {
			sum += e.getTraveltime() * e.getVolume();
		}
		return sum;
	}

	public LinkedList<Double> getDiffList() {
		return this.diffChangeList;
	}

	/**
	 * Perform User Equilibrium assignment based on existing trips and net.
	 *
	 * @param targetDiff differece of surcharge between the recent operation and
	 *                   last operation, which is also a critical convergence criteria
	 */
    public boolean assign(double targetDiff) {
        init();
        // step 0
        allOrNothing();
        y2x();
        double alpha = 1.0;
        int targetIter = 20;
        int iter = 0;
        while (iter++ < 20) {
            updateAllTraveltime(); // step 1
            allOrNothing();// step 2
            alpha = lineSearch();// step 3
            double beforemove = getTotalVolume();
            move(alpha);
            double aftermove = getTotalVolume();
        }
        updateAllTraveltime();
        return true;
	}
}
