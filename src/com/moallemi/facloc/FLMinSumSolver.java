package com.moallemi.facloc;

import com.moallemi.math.graph.*;
import com.moallemi.minsum.*;

public class FLMinSumSolver implements MinSumSolver {
    // value function
    // indexed by [edgeIndex(i,j)][y_j][x_j]
    // where y_j is a neighbor offset of j, and x_j is a distance
    private double[][][] value, nextValue;
    private DiscreteFacilityLocationProblem problem;
    private double bellmanError;
    private double objectiveValue;
    private int[] optimalDistance;
    private int[] optimalConnection;
    private int valueFunctionDim;
    private int maxValueCount;
    private int maxDist;
    private int thisMaxDist;

    public FLMinSumSolver(FacilityLocationProblem iProblem, int maxDist) {
        this.problem = (DiscreteFacilityLocationProblem) iProblem;
        this.maxDist = maxDist;

        Graph graph = problem.getGraph();
        DirectedEdgeSet des = problem.getDirectedEdgeSet();
        
        int directedEdgeCount = des.getDirectedEdgeCount();
        int nodeCount = graph.getNodeCount();
        value = new double [directedEdgeCount][][];
        nextValue = new double [directedEdgeCount][][];
        valueFunctionDim = 0;
        for (int ij = 0; ij < directedEdgeCount; ij++) {
            DirectedEdgeInfo edgeInfo = des.getDirectedEdge(ij);
            int j = edgeInfo.getSecondNodeIndex();
            int degree = graph.getNodeDegree(j);
            int maxPathLength = getAdjustedMaxPathLength(j);
            value[ij] = new double [degree+1][maxPathLength+1];
            nextValue[ij] = new double [degree+1][maxPathLength+1];
            valueFunctionDim += (degree+1)*(maxPathLength+1);
        }
        optimalDistance = new int [nodeCount];
        optimalConnection = new int [nodeCount];
    }

    public double getSumNorm() { return Double.MAX_VALUE; }
    public double getBellmanError() { return bellmanError; }
    public double getObjectiveValue() { return objectiveValue; }
    public int getValueFunctionDim() { return valueFunctionDim; }
    public int getMaxValueCount() { return maxValueCount; }
    public int getMaximumDistance() { return thisMaxDist; }
    public String getOptimalFacilitiesString() { 
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < optimalDistance.length; i++) {
            if (optimalDistance[i] == 0) {
                if (sb.length() > 0)
                    sb.append(",");
                sb.append((i+1));
            }
        }
        return sb.toString();
    }
    public boolean[] getOptimalFacilities() {
        boolean[] optimalFacility = new boolean [optimalDistance.length];
        for (int i = 0; i < optimalDistance.length; i++) 
            optimalFacility[i] = (optimalDistance[i] == 0);
        return optimalFacility;
    }
    public int getAdjustedMaxPathLength(int i) {
        int d = problem.getAllPairsShortestPaths().getMaximumPathLength(i);
        return d >= maxDist ? d : maxDist;
    }

    public void iterate() {
        Graph graph = problem.getGraph();
        DirectedEdgeSet des = problem.getDirectedEdgeSet();

        int nodeCount = graph.getNodeCount();
        int directedEdgeCount = value.length;

        double damp = 0.1;

        // apply the min-sum operator
        for (int ij = 0; ij < directedEdgeCount; ij++) {
            int degree = value[ij].length;
            DirectedEdgeInfo edgeInfo = des.getDirectedEdge(ij);
            int i = edgeInfo.getFirstNodeIndex();
            int j = edgeInfo.getSecondNodeIndex();
            int iDegree = graph.getNodeDegree(i);
            int jDegree = graph.getNodeDegree(j);
            int iMaxPathLength = getAdjustedMaxPathLength(i);
            int jMaxPathLength = getAdjustedMaxPathLength(j);
            int msgCount = des.getConnectedEdgeDegree(ij);

            for (int yj = 0; yj <= jDegree; yj++) {
                for (int xj = 0; xj <= jMaxPathLength; xj++) {
                    double minValue = Double.MAX_VALUE;

                    for (int yi = 0; yi <= iDegree; yi++) {
                        for (int xi = 0; xi <= iMaxPathLength; xi++) {
                            double thisValue = 0.0;

                            // single node cost at i
                            if (yi == iDegree) {
                                if (xi != 0)
                                    continue; // thisValue = Double.MAX_VALUE;
                                thisValue 
                                    += problem.getConstructionCost(i);
                            }
                            else if (xi == 0)
                                continue; // thisValue = Double.MAX_VALUE;
                            else
                                thisValue += xi;

                            // pairwise cost (i,j)
                             if ((yi == edgeInfo.getSecondNodeOffset()
                                  && xi != xj + 1)
                                 || 
                                 (yj == edgeInfo.getFirstNodeOffset()
                                  && xj != xi + 1)) 
                                 continue; // thisValue = Double.MAX_VALUE;

                            // Sum incoming messages
                            for (int offset_ui = 0; offset_ui < msgCount; 
                                 offset_ui++) {
                                int ui = des.getConnectedEdgeIndex(ij, 
                                                                   offset_ui);
                                if (value[ui][yi][xi] >= Double.MAX_VALUE) {
                                    thisValue = Double.MAX_VALUE;
                                    break;
                                }
                                thisValue += value[ui][yi][xi];
                            }

                            if (thisValue < minValue)
                                minValue = thisValue;
                        }
                    }
                        
                    nextValue[ij][yj][xj] = minValue;
                }
            }
        }

        // normalize and computer bellman error
        bellmanError = 0.0;
        maxValueCount = 0;
        for (int ij = 0; ij < directedEdgeCount; ij++) {
            int degree = nextValue[ij].length;
            DirectedEdgeInfo edgeInfo = des.getDirectedEdge(ij);
            int j = edgeInfo.getSecondNodeIndex();
            int jDegree = graph.getNodeDegree(j);
            int jMaxPathLength = getAdjustedMaxPathLength(j);
            double norm = nextValue[ij][jDegree][0];
            if (norm >= Double.MAX_VALUE)
                throw new IllegalStateException("unable to normalize");

            for (int yj = 0; yj <= jDegree; yj++) {
                for (int xj = 0; xj <= jMaxPathLength; xj++) {
                    if (nextValue[ij][yj][xj] < Double.MAX_VALUE) {
                        nextValue[ij][yj][xj] -= norm;
                        if (value[ij][yj][xj] < Double.MAX_VALUE) {
                            double dv = value[ij][yj][xj] 
                                - nextValue[ij][yj][xj];
                            bellmanError += dv*dv;
                        }
                    }
                    else
                        maxValueCount++;
                }
            }
        }

        // compute optimal actions
        for (int j = 0; j < nodeCount; j++) {
            int jDegree = graph.getNodeDegree(j);
            int jMaxPathLength = getAdjustedMaxPathLength(j);

            double min = Double.MAX_VALUE;
            int minX = 0;
            int minY = jDegree;

            for (int yj = 0; yj <= jDegree; yj++) {
                for (int xj = 0; xj <= jMaxPathLength; xj++) {
                    double sum = 0.0;

                    if (yj == jDegree) {
                        if (xj != 0) 
                            continue; // sum = Double.MAX_VALUE
                        sum += problem.getConstructionCost(j);
                    }
                    else if (xj == 0)
                        continue; // sum = Double.MAX_VALUE
                    else 
                        sum += xj;

                    for (int k = 0; k < jDegree; k++) {
                        double v = 
                            nextValue[des.getIncomingEdgeIndex(j,k)][yj][xj];
                        if (v < Double.MAX_VALUE)
                            sum += v;
                        else {
                            sum = Double.MAX_VALUE;
                            break;
                        }
                    }

                    if (sum < min) {
                        min = sum;
                        minX = xj;
                        minY = yj;
                    }
                }
            }

            optimalConnection[j] = minY;
            optimalDistance[j] = minX;
        }


        // compute objective value
        objectiveValue = 0.0;
        outer:
        for (int j = 0; j < nodeCount; j++) {
            int yj = optimalConnection[j];
            int xj = optimalDistance[j];
            int jDegree = graph.getNodeDegree(j);

            if (yj == jDegree) {
                if (xj != 0) {
                    objectiveValue = Double.MAX_VALUE;
                    break;
                }
                objectiveValue += problem.getConstructionCost(j);
            }
            else {
                Node node = graph.getNode(j);
                Node other = graph.getConnectedNode(node, yj);
                int i = graph.getNodeIndex(other);
                int xi = optimalDistance[i];
                if (xj != xi + 1) {
                    objectiveValue = Double.MAX_VALUE;
                    break;
                }
            }
            objectiveValue += xj;
        }

        // compute maximum distance
        thisMaxDist = 0;
        for (int j = 0; j < nodeCount; j++)  {
            int xj = optimalDistance[j];
            if (xj > thisMaxDist)
                thisMaxDist = xj;
        }

        // swap
        double[][][] tmp;
        tmp = value;
        value = nextValue;
        nextValue = tmp;
    }

                                
    public boolean isGlobalOptimum() { return false; }
                        
                    
}
        