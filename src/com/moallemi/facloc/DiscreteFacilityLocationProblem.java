package com.moallemi.facloc;

import java.io.*;
import java.text.DecimalFormat;

import com.moallemi.math.graph.*;
import com.moallemi.minsum.*;

public class DiscreteFacilityLocationProblem 
    implements FacilityLocationProblem 
{
    private Graph graph;
    private AllPairsShortestPaths apsp;
    private double[] constructionCost;
    private DirectedEdgeSet directedEdgeSet;

    public DiscreteFacilityLocationProblem(Graph graph, 
                                           double[] constructionCost) {
        this.graph = graph;
        this.constructionCost = constructionCost;
        apsp = new AllPairsShortestPaths(graph);
        directedEdgeSet = new DirectedEdgeSet(graph);
    }

    public Graph getGraph() { return graph; }
    public AllPairsShortestPaths getAllPairsShortestPaths() {
        return apsp; 
    }
    public DirectedEdgeSet getDirectedEdgeSet() { return directedEdgeSet; }
    public double getConstructionCost(int i) { return constructionCost[i]; }
    public int getCityCount() { return graph.getNodeCount(); }
    public int getFacilityCount() { return graph.getNodeCount(); }
    public double getDistance(int i, int j) {
        return apsp.getDistance(i, j);
    }
    
    public double getAllFacilityCost() {
        double sum = 0.0;
        for (int i = 0; i < constructionCost.length; i++)
            sum += constructionCost[i];
        return sum;
    }


}
