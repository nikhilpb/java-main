package com.moallemi.maxcut;

import com.moallemi.math.graph.*;

public class MaxCutProblem {
    // underlying graph
    protected Graph graph;
    // indexed by [node][neighbor_index]
    protected double[][] edgeCost;

    public MaxCutProblem(Graph graph, double[] costByEdge) {
        this.graph = graph;
        int nodeCount = graph.getNodeCount();
        int edgeCount = graph.getEdgeCount();
        if (costByEdge.length != edgeCount)
            throw new IllegalArgumentException("bad edge count");
        
        edgeCost = new double [nodeCount][];
        for (int i = 0; i < nodeCount; i++)
            edgeCost[i] = new double [graph.getNodeDegree(i)];
        for (int e = 0; e < edgeCount; e++) {
            Edge edge = graph.getEdge(e);
            Node iNode = edge.getFirst();
            int i = graph.getNodeIndex(iNode);
            Node jNode = edge.getSecond();
            int j = graph.getNodeIndex(jNode);
            edgeCost[i][graph.getConnectedNodeOffset(iNode, jNode)] 
                = costByEdge[e];
            edgeCost[j][graph.getConnectedNodeOffset(jNode, iNode)] 
                = costByEdge[e];
        }
    }

        
    public Graph getGraph() { return graph; }
    public double getEdgeCost(int i, int jIndex) {
        return edgeCost[i][jIndex];
    }
}