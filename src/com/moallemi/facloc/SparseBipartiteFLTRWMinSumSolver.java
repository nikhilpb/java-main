package com.moallemi.facloc;

import java.util.*;
import java.io.PrintStream;

import com.moallemi.math.graph.*;

public class SparseBipartiteFLTRWMinSumSolver 
    extends SparseBipartiteFLMinSumSolver 
{
    // indexed by [city][facility_adj_index]
    private double[][] rhoCityIncoming;
    // indexed by [facility][city_adj_index]
    private double[][] rhoFacilityIncoming;

    public SparseBipartiteFLTRWMinSumSolver(FacilityLocationProblem problem,
                                            double damp,
                                            int minTreeCount,
                                            Random r) {
        super(problem, damp);

        // construct a graph for the problem
        Graph graph = new Graph();
        graph.beginModification();
        Node[] cityNode = new Node [cityCount];
        for (int i = 0; i < cityCount; i++) {
            cityNode[i] = new Node();
            graph.addNode(cityNode[i]);
        }
        Node[] facilityNode = new Node [facilityCount];
        for (int i = 0; i < facilityCount; i++) {
            facilityNode[i] = new Node();
            graph.addNode(facilityNode[i]);
        }
        for (int j = 0; j < cityCount; j++) {
            int jDegree = cityAdjFacility[j].length;
            for (int iIndex = 0; iIndex < jDegree; iIndex++) {
                Edge edge = new Edge(cityNode[j],
                                     facilityNode[cityAdjFacility[j][iIndex]]);
                graph.addEdge(edge);
            }
        }
        graph.endModification();

        rhoCityIncoming = new double [cityCount][];
        for (int j = 0; j < cityCount; j++) 
            rhoCityIncoming[j] = new double [cityAdjFacility[j].length];
        rhoFacilityIncoming = new double [facilityCount][];
        for (int j = 0; j < facilityCount; j++) 
            rhoFacilityIncoming[j] = new double [facilityAdjCity[j].length];
        
        // sample spanning trees
        UniformSpanningTree ust = new UniformSpanningTree(graph, r);
        boolean done = false;
        int treeCount = 0;
        while (!done) {
            // sample a tree and update the edges
            Graph tree = ust.next();
            treeCount++;
            int treeEdgeCount = tree.getEdgeCount();
//             if (treeEdgeCount != cityCount + facilityCount - 1)
//                 throw new IllegalStateException("bad tree count: "
//                                                 + treeEdgeCount
//                                                 + " "
//                                                 + (cityCount+facilityCount-1)
//                                                 );
            for (int e = 0; e < treeEdgeCount; e++) {
                Edge edge = tree.getEdge(e);
                int n1 = graph.getNodeIndex(edge.getFirst());
                int n2 = graph.getNodeIndex(edge.getSecond());
                int c, f;
                if (n1 < cityCount) {
                    c = n1; f = n2 - cityCount;
                }
                else {
                    c = n2; f = n1 - cityCount;
                }
                for (int x = 0; x < cityAdjFacility[c].length; x++) {
                    if (cityAdjFacility[c][x] == f) 
                        rhoCityIncoming[c][x]++;
                }
                for (int x = 0; x < facilityAdjCity[f].length; x++) {
                    if (facilityAdjCity[f][x] == c)
                        rhoFacilityIncoming[f][x]++;
                }
            }

            if (treeCount >= minTreeCount) {
                // test to see if every edge is sampled at least once
                boolean allSampled = true;
                outer:
                for (int j = 0; j < cityCount; j++) {
                    int jDegree = cityAdjFacility[j].length;
                    for (int iIndex = 0; iIndex < jDegree; iIndex++) {
                        if (rhoCityIncoming[j][iIndex] <= 0.0) {
                            allSampled = false;
                            break outer;
                        }
                    }
                }
                if (allSampled)
                    done = true;
            }
        }


        for (int j = 0; j < cityCount; j++) {
            int jDegree = cityAdjFacility[j].length;
            for (int iIndex = 0; iIndex < jDegree; iIndex++) 
                rhoCityIncoming[j][iIndex] /= treeCount;
        }
        for (int j = 0; j < facilityCount; j++) {
            int jDegree = facilityAdjCity[j].length;
            for (int iIndex = 0; iIndex < jDegree; iIndex++) 
                rhoFacilityIncoming[j][iIndex] /= treeCount;
        }


        //System.out.println("tree samples: " + treeCount);
    }

        // i is a city, j a facility
    protected double getSumMsgToCity(int i, int j, int xi) {
        double sum = 0.0;
        int degree = cityAdjFacility[i].length;
        for (int uIndex = 0; uIndex < degree; uIndex++) {
            if (cityIncomingMsg[i][uIndex][xi] >= Double.MAX_VALUE)
                return Double.MAX_VALUE;
            if (cityAdjFacility[i][uIndex] == j)
                sum += (rhoCityIncoming[i][uIndex] - 1.0)
                    * cityIncomingMsg[i][uIndex][xi];
            else 
                sum += rhoCityIncoming[i][uIndex] 
                    * cityIncomingMsg[i][uIndex][xi];
        }
        return sum;
    }


    // i is a facility, j a city
    protected double getSumMsgToFacility(int i, int j, int xi) {
        double sum = 0.0;
        int degree = facilityAdjCity[i].length;
        for (int uIndex = 0; uIndex < degree; uIndex++) {
            if (facilityIncomingMsg[i][uIndex][xi] >= Double.MAX_VALUE)
                return Double.MAX_VALUE;
            if (facilityAdjCity[i][uIndex] == j) 
                sum += (rhoFacilityIncoming[i][uIndex] - 1.0)
                    * facilityIncomingMsg[i][uIndex][xi];                
            else
                sum += rhoFacilityIncoming[i][uIndex]
                    * facilityIncomingMsg[i][uIndex][xi];
        }
        return sum;
    }

    private static final double BIG = 1e6;

    public void iterate() {
        // apply the min-sum operator ...
        // ... to messages to cities
        for (int j = 0; j < cityCount; j++) {
            int jDegree = cityAdjFacility[j].length;
            for (int iIndex = 0; iIndex < jDegree; iIndex++) {
                int i = cityAdjFacility[j][iIndex];

                for (int xj = 0; xj < jDegree; xj++) {
                    double minValue = Double.MAX_VALUE;
                    int minX = -1;

                    for (int xi = 0; xi < 2; xi++) {
                        double thisValue = 0.0;
                        // single node potential at i
                        if (xi == 1)
                            thisValue += problem.getConstructionCost(i);
                        // pairwise cost at (i,j)
                        if (cityAdjFacility[j][xj] == i && xi != 1)  {
                            continue; // thisValue = Double.MAX_VALUE;
                            //thisValue += BIG;
                        }
                        // sum incoming messages
                        double sum = getSumMsgToFacility(i, j, xi);
                        if (sum >= Double.MAX_VALUE)
                            continue; // thisValue = Double.MAX_VALUE;
                        thisValue += sum;

                        if (thisValue < minValue) {
                            minValue = thisValue;
                            minX = xi;
                        }
                    }
                    nextCityIncomingMsg[j][iIndex][xj] = minValue;
                }
            }
        }

        // apply the min-sum operator ...
        // ... to messages to facilities
        for (int j = 0; j < facilityCount; j++) {
            int jDegree = facilityAdjCity[j].length;
            for (int iIndex = 0; iIndex < jDegree; iIndex++) {
                int i = facilityAdjCity[j][iIndex];
                int iDegree = cityAdjFacility[i].length;

                for (int xj = 0; xj < 2; xj++) {
                    double minValue = Double.MAX_VALUE;
                    int minX = -1;

                    for (int xi = 0; xi < iDegree; xi++) {
                        double thisValue = 0.0;
                        // single node potential at i
                        thisValue += 
                            problem.getDistance(i, cityAdjFacility[i][xi]);
                        // pairwise cost at (i,j)
                        if (cityAdjFacility[i][xi] == j && xj != 1) {
                            continue; // thisValue = Double.MAX_VALUE;
                            //thisValue += BIG;
                        }
                        // sum incoming messages
                        double sum = getSumMsgToCity(i, j, xi);
                        if (sum >= Double.MAX_VALUE)
                            continue; // thisValue = Double.MAX_VALUE;
                        thisValue += sum;

                        if (thisValue < minValue) {
                            minValue = thisValue;
                            minX = xi;
                        }
                    }
                    nextFacilityIncomingMsg[j][iIndex][xj] = minValue;
                }
            }
        }

        // normalize and compute bellman error
        bellmanError = 0.0;
        double sumNorm = 0.0;
        for (int j = 0; j < cityCount; j++) {
            int jDegree = cityAdjFacility[j].length;
            for (int iIndex = 0; iIndex < jDegree; iIndex++) {
                int i = cityAdjFacility[j][iIndex];
                double norm;

                norm = nextCityIncomingMsg[j][iIndex][0];
                if (norm >= Double.MAX_VALUE) 
                    throw new IllegalStateException("unable to normalize");
                sumNorm += norm;
                for (int xj = 0; xj < jDegree; xj++) {
                    if (nextCityIncomingMsg[j][iIndex][xj] < Double.MAX_VALUE) {
                        nextCityIncomingMsg[j][iIndex][xj] -= norm;
                        if (damp > 0.0) {
                            if (cityIncomingMsg[j][iIndex][xj] 
                                >= Double.MAX_VALUE) 
                                nextCityIncomingMsg[j][iIndex][xj] 
                                    = Double.MAX_VALUE;
                            else
                                nextCityIncomingMsg[j][iIndex][xj] =
                                    (1.0 - damp) 
                                    * nextCityIncomingMsg[j][iIndex][xj]
                                    + damp * cityIncomingMsg[j][iIndex][xj];
                        }
                        if (cityIncomingMsg[j][iIndex][xj] < Double.MAX_VALUE) {
                            double dv = cityIncomingMsg[j][iIndex][xj]
                                - nextCityIncomingMsg[j][iIndex][xj];
                            bellmanError += dv*dv;
                        }
                    }
                }
            }
        }
        for (int j = 0; j < facilityCount; j++) {
            int jDegree = facilityAdjCity[j].length;
            for (int iIndex = 0; iIndex < jDegree; iIndex++) {
                int i = facilityAdjCity[j][iIndex];
                double norm;

                norm = nextFacilityIncomingMsg[j][iIndex][0];
                if (norm >= Double.MAX_VALUE) 
                    throw new IllegalStateException("unable to normalize");
                sumNorm += norm;
                for (int xj = 0; xj < 2; xj++) {
                    if (nextFacilityIncomingMsg[j][iIndex][xj] 
                        < Double.MAX_VALUE) {
                        nextFacilityIncomingMsg[j][iIndex][xj] -= norm;
                        if (damp > 0.0) {
                            if (facilityIncomingMsg[j][iIndex][xj] 
                                >= Double.MAX_VALUE)
                                nextFacilityIncomingMsg[j][iIndex][xj] 
                                    = Double.MAX_VALUE;
                            else
                                nextFacilityIncomingMsg[j][iIndex][xj] =
                                    (1.0 - damp) 
                                    * nextFacilityIncomingMsg[j][iIndex][xj]
                                    + damp * facilityIncomingMsg[j][iIndex][xj];
                        }
                        if (facilityIncomingMsg[j][iIndex][xj]
                            < Double.MAX_VALUE) {
                            double dv = facilityIncomingMsg[j][iIndex][xj]
                                - nextFacilityIncomingMsg[j][iIndex][xj];
                            bellmanError += dv*dv;
                        }
                    }
                }
            }
        }
        //System.out.println(sumNorm);

        // swap
        double[][][] tmp;
        tmp = cityIncomingMsg;
        cityIncomingMsg = nextCityIncomingMsg;
        nextCityIncomingMsg = tmp;
        tmp = facilityIncomingMsg;
        facilityIncomingMsg = nextFacilityIncomingMsg;
        nextFacilityIncomingMsg = tmp;

        computeOptimalActions();

        computeObjectiveValue();
    }



    protected void computeOptimalActions() {
        for (int j = 0; j < facilityCount; j++) {
            double min = Double.MAX_VALUE;
            int minX = 1;
            for (int xj = 1; xj >= 0; xj--) {
                double sum = getFacilityMarginal(j, xj);
                if (sum < min) {
                    min = sum;
                    minX = xj;
                }
            }
            optimalFacility[j] = (minX == 1);
        }
        for (int j = 0; j < cityCount; j++) {
            boolean satisfied = false;
            int degree = cityAdjFacility[j].length;
            for (int iIndex = 0; iIndex < degree; iIndex++) {
                if (optimalFacility[cityAdjFacility[j][iIndex]]) {
                    satisfied = true;
                    break;
                }
            }
            if (!satisfied) {
                double min = Double.MAX_VALUE;
                int minX = 0;
                
                for (int xj = 0; xj < degree; xj++) {
                    double sum = getCityMarginal(j, xj);
                    if (sum < min) {
                        min = sum;
                        minX = xj;
                    }
                }
                optimalFacility[cityAdjFacility[j][minX]] = true;
            }
        }
    }

    public void dumpRho(PrintStream out) {
        for (int j = 0; j < cityCount; j++) {
            int jDegree = cityAdjFacility[j].length;
            for (int iIndex = 0; iIndex < jDegree; iIndex++) {
                int i = cityAdjFacility[j][iIndex];
                out.println((j+1) + " " + (i+1) + " " 
                            + rhoCityIncoming[j][iIndex]);
            }
        }
        for (int j = 0; j < facilityCount; j++) {
            int jDegree = facilityAdjCity[j].length;
            for (int iIndex = 0; iIndex < jDegree; iIndex++) {
                int i = facilityAdjCity[j][iIndex];
                out.println((j+1) + " " + (i+1) + " " 
                            + rhoFacilityIncoming[j][iIndex]);
            }
        }
    }
   

    private double getFacilityMarginal(int j, int xj) {
        double sum = 0.0;
        if (xj == 1)
            sum += problem.getConstructionCost(j);
        int degree = facilityAdjCity[j].length;
        for (int iIndex = 0; iIndex < degree; iIndex++)  {
            if (facilityIncomingMsg[j][iIndex][xj] 
                >= Double.MAX_VALUE) {
                sum = Double.MAX_VALUE;
                break;
            }
            sum += rhoFacilityIncoming[j][iIndex]
                * facilityIncomingMsg[j][iIndex][xj];
        }
        return sum;
    }

    private double getCityMarginal(int j, int xj) {
        double sum = problem.getDistance(j, xj);
        int degree = cityAdjFacility[j].length;
        for (int iIndex = 0; iIndex < degree; iIndex++) {
            if (cityIncomingMsg[j][iIndex][xj] >= Double.MAX_VALUE) {
                sum = Double.MAX_VALUE;
                break;
            }
            sum += rhoCityIncoming[j][iIndex] 
                * cityIncomingMsg[j][iIndex][xj];
        }
        return sum;
    }

    // i is a facility, j a city
    private double getPairwiseMarginal(int i, int j, int xi, int xj) {
        double sum = 0.0;
        // single-node potential at i
        if (xi == 1)
            sum += problem.getConstructionCost(i);
        // single-node potential at j
        sum += problem.getDistance(j, cityAdjFacility[j][xj]);
        // pairwise potential
        if (cityAdjFacility[j][xj] == i && xi != 1) {
            return Double.MAX_VALUE;
            //sum += BIG;
        }
        // incoming messages to i
        int iDegree = facilityAdjCity[i].length;
        for (int uIndex = 0; uIndex < iDegree; uIndex++) {
            if (facilityIncomingMsg[i][uIndex][xi] >= Double.MAX_VALUE)
                return Double.MAX_VALUE;
            if (facilityAdjCity[i][uIndex] == j)
                sum += (rhoFacilityIncoming[i][uIndex] - 1.0)
                    * facilityIncomingMsg[i][uIndex][xi];
            else
                sum += rhoFacilityIncoming[i][uIndex]
                    * facilityIncomingMsg[i][uIndex][xi];
        }
        // incoming messages to j
        int jDegree = cityAdjFacility[j].length;
        for (int uIndex = 0; uIndex < jDegree; uIndex++) {
            if (cityIncomingMsg[j][uIndex][xj] >= Double.MAX_VALUE) 
                return Double.MAX_VALUE;
            if (cityAdjFacility[j][uIndex] == i)
                sum += (rhoCityIncoming[j][uIndex] - 1.0)
                    * cityIncomingMsg[j][uIndex][xj];
            else
                sum += rhoCityIncoming[j][uIndex]
                    * cityIncomingMsg[j][uIndex][xj];
        }
        return sum;
    }
   
    // fix!!! only look over connected edges!
    public boolean isGlobalOptimum() {
        int[] optFacilityAction = new int [facilityCount];
        for (int j = 0; j < facilityCount; j++) {
            double min = Double.MAX_VALUE;
            int minX = 0;
            for (int xj = 0; xj < 2; xj++) {
                double sum = getFacilityMarginal(j, xj);
                if (sum < min) {
                    min = sum;
                    minX = xj;
                }
            }
            optFacilityAction[j] = minX;
        }
        int[] optCityAction = new int [cityCount];
        for (int j = 0; j < cityCount; j++) {
            double min = Double.MAX_VALUE;
            int minX = 0;
            int degree = cityAdjFacility[j].length;
            for (int xj = 0; xj < degree; xj++) {
                double sum = getCityMarginal(j, xj);
                if (sum < min) {
                    min = sum;
                    minX = xj;
                }
            }
            optCityAction[j] = minX;
        }

        for (int j = 0; j < cityCount; j++) {
            int jDegree = cityAdjFacility[j].length;
            for (int i = 0; i < facilityCount; i++) {
                double min = getPairwiseMarginal(i, 
                                                 j,
                                                 optFacilityAction[i],
                                                 optCityAction[j]);
                for (int xi = 0; xi < 2; xi++) {
                    for (int xj = 0; xj < jDegree; xj++) {
                        double v = getPairwiseMarginal(i,
                                                       j,
                                                       xi,
                                                       xj);
                        if (v < min) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
     
}
   