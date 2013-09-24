package com.moallemi.maxcut;

import java.util.Random;

import com.moallemi.math.graph.*;

public class MCMinSumSolver {
    protected Graph graph;
    protected MaxCutProblem problem;
    protected int nodeCount;

    // incoming messages
    // indexed by [j][jState][iIndex]
    protected double[][][] incomingMsg;
    protected double[][][] nextIncomingMsg;
    
    // map of neighbors, indexed by [j][iIndex]
    protected int[][] neighborMap;

    protected int[] optimalActions;
    protected double bellmanError, objectiveValue;

    protected double damp;

    protected static final double EPSILON = 0.001;
    protected double[] bias;

    public MCMinSumSolver(MaxCutProblem problem, 
                          double damp,
                          Random r) {
        this.problem = problem;
        this.damp = damp;

        graph = problem.getGraph();
        nodeCount = graph.getNodeCount();
        incomingMsg = new double [nodeCount][][];
        nextIncomingMsg = new double [nodeCount][][];
        neighborMap = new int [nodeCount][];
        optimalActions = new int [nodeCount];
        bias = new double [nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            Node iNode = graph.getNode(i);
            int degree = graph.getNodeDegree(i);
            incomingMsg[i] = new double [2][degree];
            nextIncomingMsg[i] = new double [2][degree];
            neighborMap[i] = new int [degree];
            bias[i] = EPSILON * (2.0 * r.nextDouble() - 1.0);
            for (int jIndex = 0; jIndex < degree; jIndex++) {
                Node jNode = graph.getConnectedNode(iNode, jIndex);
                neighborMap[i][jIndex] = graph.getNodeIndex(jNode);
            }
        }
        bias[0] = Double.MAX_VALUE;

    }


    public double getBellmanError() { return bellmanError; }
    public double getObjectiveValue() { return -objectiveValue; }
    public String getOptimalActions() { 
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < nodeCount; i++) {
            if (i > 0)
                sb.append(",");
            sb.append(optimalActions[i]);
        }
        return sb.toString();
    }
    public String getOptimalStats() {
        int zeroCount = 0;
        for (int i = 0; i < nodeCount; i++) {
            if (optimalActions[i] == 0)
                zeroCount++;
        }
        return zeroCount + "," + (nodeCount-zeroCount);
    }

    // get sum of messages to i not including j
    protected double getSumMessages(int i, int j, int xi) {
        double sum = 0.0;
        int degree = neighborMap[i].length;
        for (int uIndex = 0; uIndex < degree; uIndex++) {
            if (neighborMap[i][uIndex] != j) {
                if (incomingMsg[i][xi][uIndex] >= Double.MAX_VALUE)
                    return Double.MAX_VALUE;
                sum += incomingMsg[i][xi][uIndex];
            }
        }
        return sum;
    }

    protected void computeOptimalActions() {
        for (int j = 0; j < nodeCount; j++) {
            double min = Double.MAX_VALUE;
            int minX = -1;
            int degree = neighborMap[j].length;
            for (int xj = 0; xj < 2; xj++) {
                double sum = 0.0;
                if (xj == 1) {
                    if (bias[j] >= Double.MAX_VALUE)
                        continue;
                    sum += bias[j];
                }
                for (int iIndex = 0; iIndex < degree; iIndex++) {
                    if (incomingMsg[j][xj][iIndex] >= Double.MAX_VALUE) {
                        sum = Double.MAX_VALUE;
                        break;
                    }
                    sum += incomingMsg[j][xj][iIndex];
                }
                //System.out.println(j + " " + xj + " " + sum);
                if (sum < min) {
                    min = sum;
                    minX = xj;
                }
            }
            if (minX < 0)
                throw new IllegalStateException("could not compute "
                                                + "optimal action");
            //System.out.println(j + " " + minX + " ****");
            optimalActions[j] = minX;
        }
    }

    protected void computeObjectiveValue() {
        objectiveValue = 0.0;
        for (int j = 0; j < nodeCount; j++) {
            int degree = neighborMap[j].length;
            int xj = optimalActions[j];
            for (int iIndex = 0; iIndex < degree; iIndex++) {
                int i = neighborMap[j][iIndex];
                if (i < j && optimalActions[i] != xj)
                    objectiveValue += -problem.getEdgeCost(j, iIndex);
            }
        }
    }

    public void iterate() {
        // apply the min-sum operator
        for (int j = 0; j < nodeCount; j++) {
            int jDegree = neighborMap[j].length;
            for (int xj = 0; xj < 2; xj++) {
                for (int iIndex = 0; iIndex < jDegree; iIndex++) {
                    int i = neighborMap[j][iIndex];
                    double minValue = Double.MAX_VALUE;
                    double edgeCost = -problem.getEdgeCost(j, iIndex);
                    for (int xi = 0; xi < 2; xi++) {
                        double thisValue = 0.0;
                        // single node potential (bias)
                        if (xi == 1) {
                            if (bias[j] >= Double.MAX_VALUE)
                                continue;
                            thisValue += bias[i];
                        }
                        // pairwise cost at (i,j)
                        if (xi != xj)
                            thisValue += edgeCost;
                        // sum incoming messages
                        double incoming = getSumMessages(i, j, xi);
                        if (incoming >= Double.MAX_VALUE)
                            continue;
                        thisValue += incoming;
                        
                        if (thisValue < minValue)
                            minValue = thisValue;
                    }
                    nextIncomingMsg[j][xj][iIndex] = minValue;
                }
            }
        }

        // normalize and compute Bellman Error
        bellmanError = 0.0;
        for (int j = 0; j < nodeCount; j++) {
            int jDegree = neighborMap[j].length;
            for (int iIndex = 0; iIndex < jDegree; iIndex++) {
                double norm = nextIncomingMsg[j][0][iIndex];
                if (norm >= Double.MAX_VALUE)
                    throw new IllegalStateException("unable to normalize");
                for (int xj = 0; xj < 2; xj++) {
                    if (nextIncomingMsg[j][xj][iIndex] < Double.MAX_VALUE) {
                        nextIncomingMsg[j][xj][iIndex] -= norm;
                        if (damp > 0.0) {
                            if (incomingMsg[j][xj][iIndex] < Double.MAX_VALUE)
                                nextIncomingMsg[j][xj][iIndex] =
                                    (1.0 - damp) 
                                    * nextIncomingMsg[j][xj][iIndex]
                                    + 
                                    damp 
                                    * incomingMsg[j][xj][iIndex];
                            else
                                nextIncomingMsg[j][xj][iIndex] 
                                    = Double.MAX_VALUE;
                        }
                        if (incomingMsg[j][xj][iIndex] < Double.MAX_VALUE) {
                            double dv = incomingMsg[j][xj][iIndex] 
                                - nextIncomingMsg[j][xj][iIndex];
                            bellmanError += dv*dv;
                        }
                    }
                }
            }
        }

        // swap
        double[][][] tmp;
        tmp = incomingMsg;
        incomingMsg = nextIncomingMsg;
        nextIncomingMsg = tmp;

        computeOptimalActions();
        
        computeObjectiveValue();
    }
}

                            
                    
                
    