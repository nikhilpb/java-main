package com.moallemi.facloc;

import java.util.Arrays;

public class BipartiteFLTRWMinSumSolver extends BipartiteFLMinSumSolver {
    private double rho;

    public BipartiteFLTRWMinSumSolver(FacilityLocationProblem problem,
                                      double damp) {
        super(problem, damp);
        // weight of each edge in a uniform;y sampled random
        // tree for a complete bipartite graph
        rho = ((double) facilityCount + cityCount - 1.0)
            / ((double) facilityCount * cityCount);
    }

    protected double getSumMsgToFacility(int i, int j, int xi) {
        double sum = 0.0;
        for (int u = 0; u < j; u++) {
            if (msgToFacility[u][i][xi] >= Double.MAX_VALUE) 
                return Double.MAX_VALUE;
            sum += rho * msgToFacility[u][i][xi];
        }
        for (int u = j+1; u < cityCount; u++) {
            if (msgToFacility[u][i][xi] >= Double.MAX_VALUE) 
                return Double.MAX_VALUE;
            sum += rho * msgToFacility[u][i][xi];
        }
        // not sure below is the right thing to do
        if (msgToFacility[j][i][xi] >= Double.MAX_VALUE)
            return Double.MAX_VALUE;
        sum += (rho - 1.0) * msgToFacility[j][i][xi];
        return sum;
    }

    protected double getSumMsgToCity(int i, int j, int xi) {
        double sum = 0.0;
        for (int u = 0; u < j; u++) {
            if (msgToCity[u][i][xi] >= Double.MAX_VALUE) 
                return Double.MAX_VALUE;
            sum += rho * msgToCity[u][i][xi];
        }
        for (int u = j+1; u < facilityCount; u++) {
            if (msgToCity[u][i][xi] >= Double.MAX_VALUE) 
                return Double.MAX_VALUE;
            sum += rho * msgToCity[u][i][xi];
        }
        // not sure below is the right thing to do
        if (msgToCity[j][i][xi] >= Double.MAX_VALUE)
            return Double.MAX_VALUE;
        sum += (rho - 1.0) * msgToCity[j][i][xi];
        return sum;
    }

    protected void computeOptimalActions() {
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
            optimalFacility[j] = (minX == 1);
        }
    }

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
            for (int xj = 0; xj < facilityCount; xj++) {
                double sum = getCityMarginal(j, xj);
                if (sum < min) {
                    min = sum;
                    minX = xj;
                }
            }
            optCityAction[j] = minX;
        }

        for (int j = 0; j < cityCount; j++) {
            for (int i = 0; i < facilityCount; i++) {
                double min = getPairwiseMarginal(i, 
                                                 j,
                                                 optFacilityAction[i],
                                                 optCityAction[j]);
                for (int xi = 0; xi < 2; xi++) {
                    for (int xj = 0; xj < facilityCount; xj++) {
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
    
    // i is a facility, j a city
    private double getPairwiseMarginal(int i, int j, int xi, int xj) {
        double sum = 0.0;
        // single-node potential at i
        if (xi == 1)
            sum += problem.getConstructionCost(i);
        // single-node potential at j
        sum += problem.getDistance(j, xj);
        // pairwise potential
        if (xj == i && xi != 1) 
            return Double.MAX_VALUE;
        // incoming messages to i
        for (int u = 0; u < cityCount; u++) {
            if (msgToFacility[u][i][xi] >= Double.MAX_VALUE)
                return Double.MAX_VALUE;
            if (u == j)
                sum += (rho - 1.0) * msgToFacility[u][i][xi];
            else
                sum += rho * msgToFacility[u][i][xi];
        }
        // incoming messages to j
        for (int u = 0; u < facilityCount; u++) {
            if (msgToCity[u][j][xj] >= Double.MAX_VALUE)
                return Double.MAX_VALUE;
            if (u == i)
                sum += (rho - 1.0) * msgToCity[u][j][xj];
            else
                sum += rho * msgToCity[u][j][xj];
        }
        return sum;
    }

    private double getFacilityMarginal(int j, int xj) {
        double sum = 0.0;
        if (xj == 1)
            sum += problem.getConstructionCost(j);
        for (int i = 0; i < cityCount; i++) {
            if (msgToFacility[i][j][xj] >= Double.MAX_VALUE) {
                sum = Double.MAX_VALUE;
                break;
            }
            sum += rho * msgToFacility[i][j][xj];
        }
        return sum;
    }
    
    private double getCityMarginal(int j, int xj) {
        double sum = problem.getDistance(j, xj);
        for (int i = 0; i < facilityCount; i++) {
            if (msgToCity[i][j][xj] >= Double.MAX_VALUE) {
                sum = Double.MAX_VALUE;
                break;
            }
            sum += rho * msgToCity[i][j][xj];
        }
        return sum;
    }

}
   