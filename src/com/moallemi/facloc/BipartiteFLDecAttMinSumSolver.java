package com.moallemi.facloc;

import java.util.Arrays;

public class BipartiteFLDecAttMinSumSolver extends BipartiteFLMinSumSolver {
    private double alphaInv;
    private double[][] facilityBias;
    private boolean[] facilityDecimated;

    public BipartiteFLDecAttMinSumSolver(FacilityLocationProblem problem,
                                         double damp) {
        super(problem, damp);
        if (cityCount != facilityCount) 
            throw new IllegalArgumentException("must have same cities "
                                               + "and facilities");
        alphaInv = 1.0 / ((double) facilityCount - 1.0);
        facilityBias = new double [facilityCount][2];
        bellmanError = Double.MAX_VALUE;
        facilityDecimated = new boolean [facilityCount];
        Arrays.fill(facilityDecimated, false);
    }

    protected double getSumMsgToFacility(int i, int j, int xi) {
        double sum = super.getSumMsgToFacility(i, j, xi);
        if (sum >= Double.MAX_VALUE)
            return Double.MAX_VALUE;
        return sum * alphaInv;
    }

    protected double getSumMsgToCity(int i, int j, int xi) {
        double sum = super.getSumMsgToCity(i, j, xi);
        if (sum >= Double.MAX_VALUE)
            return Double.MAX_VALUE;
        return sum * alphaInv;
    }

    
    protected void updateMessages() {
        // apply the min-sum operator ...
        // ... to messages to cities
        for (int f = 0; f < facilityCount; f++) {
            for (int c = 0; c < cityCount; c++) {
                int i, j;

                i = f;
                j = c;
                for (int xj = 0; xj < facilityCount; xj++) {
                    double minValue = Double.MAX_VALUE;
                    int minX = -1;

                    for (int xi = 0; xi < 2; xi++) {
                        double thisValue = 0.0;
                        // bias at node i
                        if (facilityBias[i][xi] >= Double.MAX_VALUE) 
                            continue;
                        thisValue += facilityBias[i][xi];
                        // single node potential at i
                        if (xi == 1)
                            thisValue += problem.getConstructionCost(i);
                        // pairwise cost at (i,j)
                        if (xj == i && xi != 1) 
                            continue; // thisValue = Double.MAX_VALUE;
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
                    nextMsgToCity[i][j][xj] = minValue;
                }
            }
        }

        // apply the min-sum operator ...
        // ... to messages to facilities
        for (int c = 0; c < cityCount; c++) {
            for (int f = 0; f < facilityCount; f++) {
                int i, j;

                i = c;
                j = f;
                for (int xj = 0; xj < 2; xj++) {
                    double minValue = Double.MAX_VALUE;
                    int minX = -1;

                    for (int xi = 0; xi < facilityCount; xi++) {
                        double thisValue = 0.0;
                        // single node potential at i
                        thisValue += problem.getDistance(i, xi);
                        // pairwise cost at (i,j)
                        if (xi == j && xj != 1)
                            continue; // thisValue = Double.MAX_VALUE;
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
                    nextMsgToFacility[i][j][xj] = minValue;
                }
            }
        }
    }


    public void decimate() {
        // decimate!
        double max = 0.0;
        int maxJ = -1;
        int xj = -1;
        for (int j = 0; j < facilityCount; j++) {
            if (!facilityDecimated[j]) {
                double v = getFacilityMarginal(j, 1);
                double av = Math.abs(v);
                if (av > max) {
                    max = av;
                    maxJ = j;
                    xj = v < 0.0 ? 1 : 0;
                }
            }
        }
        if (maxJ >= 0) {
            facilityDecimated[maxJ] = true;
            facilityBias[maxJ][xj] = 0.0;
            facilityBias[maxJ][1-xj] = Double.MAX_VALUE;
        }
    }

    public boolean isFullyDecimated() {
        for (int j = 0; j < facilityCount; j++) {
            if (!facilityDecimated[j]) 
                return false;
        }
        return true;
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
            sum += msgToFacility[i][j][xj];
        }
        return sum;
    }

    protected void computeOptimalActions() {
        for (int j = 0; j < facilityCount; j++) {
            if (facilityDecimated[j]) 
                optimalFacility[j] = facilityBias[j][0] > facilityBias[j][1];
            else {
                double min = Double.MAX_VALUE;
                int minX = 0;
                for (int xj = 0; xj < 2; xj++) {
                    double sum = 0.0;
                    if (xj == 1)
                        sum += problem.getConstructionCost(j);
                    for (int i = 0; i < cityCount; i++) {
                        if (msgToFacility[i][j][xj] >= Double.MAX_VALUE) {
                            sum = Double.MAX_VALUE;
                            break;
                        }
                        sum += msgToFacility[i][j][xj];
                    }
                    if (sum < min) {
                        min = sum;
                        minX = xj;
                    }
                }
                optimalFacility[j] = (minX == 1);
            }
        }
    }

}
   