package com.moallemi.math.graph;

import java.util.*;

/**
 * Compute the shortest path between all pairs. Uses the
 * Floyd-Warshall algorithm and runs in O(V^3) time.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.1 $, $Date: 2006-08-31 08:40:14 $
 */
public class AllPairsShortestPaths {
    private Graph graph;
    private int initialModCount;
    private int[][] distance;
    private int maxPathLength;
    private int[] maxPathLengths;
    private boolean isConnected;

    /**
     * Constructor.
     *
     * @param graph the graph
     */
    public AllPairsShortestPaths(Graph graph) {
        this.graph = graph;
        initialModCount = graph.getModificationCount();

        int nodeCount = graph.getNodeCount();
        int edgeCount = graph.getEdgeCount();
        distance = new int [nodeCount][nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            Arrays.fill(distance[i], Integer.MAX_VALUE);
            distance[i][i] = 0;
        }
        for (int e = 0; e < edgeCount; e++) {
            Edge edge = graph.getEdge(e);
            int i = graph.getNodeIndex(edge.getFirst());
            int j = graph.getNodeIndex(edge.getSecond());
            distance[i][j] = distance[j][i] = 1;
        }

        for (int k = 0; k < nodeCount; k++) {
            for (int i = 0; i < nodeCount; i++) {
                for (int j = 0; j < i; j++) {
                    if (distance[i][k] < Integer.MAX_VALUE
                        && distance[k][j] < Integer.MAX_VALUE
                        && distance[i][j] > distance[i][k] + distance[k][j]) {
                        distance[i][j] = distance[i][k] + distance[k][j];
                        distance[j][i] = distance[i][j];
                    }
                }
            }
        }

        maxPathLength = 0;
        maxPathLengths = new int [nodeCount];
        isConnected = true;
        for (int i = 0; i < nodeCount; i++) {
            maxPathLengths[i] = 0;
            for (int j = 0; j < nodeCount; j++) {
                if (distance[i][j] >= Integer.MAX_VALUE) 
                    isConnected = false;
                else if (distance[i][j] > maxPathLengths[i])
                    maxPathLengths[i] = distance[i][j];
            }
            if (maxPathLengths[i] > maxPathLength)
                maxPathLength = maxPathLengths[i];
        }
                    

    }

    /**
     * Get the length of the shortest path between 2 nodes.
     *
     * @param i the index of the first node
     * @param j the index of the second node
     * @return the distance, or <code>Integer.MAX_VALUE</code> if they
     * are not connected
     * @throws ConcurrentModificationException if the graph has been modified
     */
    public int getDistance(int i, int j) {
	if (initialModCount != graph.getModificationCount())
	    throw new ConcurrentModificationException();
        return distance[i][j];
    }

    /**
     * Get the length of the shortest path between 2 nodes.
     *
     * @param a the first node
     * @param b the second node
     * @return the distance, or <code>Integer.MAX_VALUE</code> if they
     * are not connected
     * @throws ConcurrentModificationException if the graph has been modified
     */
    public int getDistance(Node a, Node b) {
	if (initialModCount != graph.getModificationCount())
	    throw new ConcurrentModificationException();
        return distance[graph.getNodeIndex(a)][graph.getNodeIndex(b)];
    }

    /**
     * Get the maximum length of the shortest path between any two
     * connected nodes.
     *
     * @return the maximum path length
     */
    public int getMaximumPathLength() {
        return maxPathLength;
    }

    /**
     * Get the maximum length of the shortest path between a given
     * node an any other.
     *
     * @param i the node indes
     * @return the maximum path length
     */
    public int getMaximumPathLength(int i) {
        return maxPathLengths[i];
    }

    /**
     * Is the graph connected?
     *
     * @return <code>true</code> if the graph is connected,
     * <code>false</code> otherwise
     */
    public boolean isConnected() {
	return isConnected;
    }

}