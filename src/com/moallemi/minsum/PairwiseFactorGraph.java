package com.moallemi.minsum;

import java.util.Arrays;

import com.moallemi.math.graph.*;

public class PairwiseFactorGraph {
    protected Graph graph;
    protected int nodeCount;
    protected int[][] nodeNeighborMap;
    protected int[][] nodeNeighborOffsetMap;
    protected int[] nodeDegree;

    public PairwiseFactorGraph(Graph graph) {
        this.graph = graph;
        nodeCount = graph.getNodeCount();
        nodeDegree = new int [nodeCount];
        nodeNeighborMap = new int [nodeCount][];
        nodeNeighborOffsetMap = new int [nodeCount][];
        for (int i = 0; i < nodeCount; i++) {
            Node node = graph.getNode(i);
            int degree = graph.getNodeDegree(i);
            nodeDegree[i] = degree;
            nodeNeighborMap[i] = new int [degree];
            nodeNeighborOffsetMap[i] = new int [degree];
            for (int idx = 0; idx < degree; idx++) {
                Node other = graph.getConnectedNode(node, idx);
                nodeNeighborMap[i][idx] = graph.getNodeIndex(other);
                nodeNeighborOffsetMap[i][idx] 
                    = graph.getConnectedNodeOffset(other, node);
            }
        }
    }

    public Graph getGraph() { return graph; }
    public int getNodeCount() { return nodeCount; }
    public int getNodeDegree(int i) { return nodeDegree[i]; }
    public int getNodeNeighbor(int i, int idx) {
        return nodeNeighborMap[i][idx];
    }
    public int getNodeNeighborOffset(int i, int idx) {
        return nodeNeighborOffsetMap[i][idx];
    }
}