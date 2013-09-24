package com.moallemi.facloc;

import java.util.Arrays;

public class FastSparseBipartiteFLMinSumSolver implements MinSumSolver {
    protected FacilityLocationProblem problem;

    protected int cityCount, facilityCount;

    // optimal actions
    protected boolean[] optimalFacility;
    protected int[] optFacilityAction;
    protected int[] optCityAction;

    protected double bellmanError, objectiveValue;
    protected double damp;

    protected double sumNorm;

    // indexed by [city][facility_adj_index][city_state]
    // here, city_state==0 means the city is pointing away from the facility
    // city_state==1 means the city is pointing towards the facility
    protected double[][][] cityIncomingMsg;
    protected double[][] citySumIncomingMsg;
    protected double[][][] nextCityIncomingMsg;
    // indexed by [facility][city_adj_index][facility_state]
    protected double[][][] facilityIncomingMsg;
    protected double[][] facilitySumIncomingMsg;
    protected double[][][] nextFacilityIncomingMsg;    

    // facilities connected to each city
    protected int[][] cityAdjFacility;
    protected int[][] cityAdjFacilityIndex;
    // cities connected to each facility
    protected int[][] facilityAdjCity;
    protected int[][] facilityAdjCityIndex;

    // this number should be big (used as a penalty)
    protected double bigCost;

    public FastSparseBipartiteFLMinSumSolver(FacilityLocationProblem problem,
                                             double damp) {
        this.problem = problem;
        this.damp = damp;
        cityCount = problem.getCityCount();
        facilityCount = problem.getFacilityCount();
        bigCost = problem.getAllFacilityCost();

        cityAdjFacility = new int [cityCount][];
        cityAdjFacilityIndex = new int [cityCount][facilityCount];
        int[] tmp = new int [facilityCount];
        for (int i = 0; i < cityCount; i++) {
            Arrays.fill(cityAdjFacilityIndex[i], -1);
            int cnt = 0;
            for (int j = 0; j < facilityCount; j++) {
                if (problem.getDistance(i,j) < Double.MAX_VALUE) {
                    tmp[cnt] = j;
                    cityAdjFacilityIndex[i][j] = cnt;
                    cnt++;
                }
            }
            cityAdjFacility[i] = new int [cnt];
            System.arraycopy(tmp, 0, cityAdjFacility[i], 0, cnt);
        }
        facilityAdjCity = new int [facilityCount][];
        facilityAdjCityIndex = new int [facilityCount][cityCount];
        tmp = new int [cityCount];
        for (int i = 0; i < facilityCount; i++) {
            Arrays.fill(facilityAdjCityIndex[i], -1);
            int cnt = 0;
            for (int j = 0; j < cityCount; j++) {
                if (problem.getDistance(j,i) < Double.MAX_VALUE) {
                    tmp[cnt] = j;
                    facilityAdjCityIndex[i][j] = cnt;
                    cnt++;
                }
            }
            facilityAdjCity[i] = new int [cnt];
            System.arraycopy(tmp, 0, facilityAdjCity[i], 0, cnt);
        }

        cityIncomingMsg = new double [cityCount][][];
        citySumIncomingMsg = new double [cityCount][];
        nextCityIncomingMsg = new double [cityCount][][];
        for (int j = 0; j < cityCount; j++) {
            int degree = cityAdjFacility[j].length;
            cityIncomingMsg[j] = new double [degree][2];
            citySumIncomingMsg[j] = new double [degree];
            nextCityIncomingMsg[j] = new double [degree][2];
        }
        facilityIncomingMsg = new double [facilityCount][][];
        facilitySumIncomingMsg = new double [facilityCount][2];
        nextFacilityIncomingMsg = new double [facilityCount][][];
        for (int j = 0; j < facilityCount; j++) {
            int degree = facilityAdjCity[j].length;
            facilityIncomingMsg[j] = new double [degree][2];
            nextFacilityIncomingMsg[j] = new double [degree][2];
        }

        optimalFacility = new boolean [facilityCount];
        optFacilityAction = new int [facilityCount];
        optCityAction = new int [facilityCount];

        computeSumIncomingMessages();
    }

    public double getBellmanError() { return bellmanError; }
    public double getObjectiveValue() { return objectiveValue; }
    public String getOptimalFacilitiesString() { 
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < optimalFacility.length; i++) {
            if (optimalFacility[i]) {
                if (sb.length() > 0)
                    sb.append(",");
                sb.append((i+1));
            }
        }
        return sb.toString();
    }
    public boolean[] getOptimalFacilities() { return optimalFacility; }
    public double getSumNorm() { return sumNorm; }

    protected void computeSumIncomingMessages() {
        // ... messages to cities
        for (int j = 0; j < cityCount; j++) {
            int jDegree = cityAdjFacility[j].length;

            double sumAll0 = 0.0;
            for (int iIndex = 0; iIndex < jDegree; iIndex++)
                sumAll0 += cityIncomingMsg[j][iIndex][0];

            for (int xj = 0; xj < jDegree; xj++) {
                int xjNode = cityAdjFacility[j][xj];
                // add local potential
                double sum = problem.getDistance(j, xjNode);
                // add incoming messages
                sum += sumAll0 
                    + cityIncomingMsg[j][xj][1]
                    - cityIncomingMsg[j][xj][0];

                citySumIncomingMsg[j][xj] = sum;
            }
        }
        // ... messages to facilities
        for (int j = 0; j < facilityCount; j++) {
            int jDegree = facilityAdjCity[j].length;
            for (int xj = 0; xj < 2; xj++) {
                double sum = 0.0;

                // add local potential
                if (xj == 1)
                    sum += problem.getConstructionCost(j);
                // add incoming messages
                for (int iIndex = 0; iIndex < jDegree; iIndex++) 
                    sum += facilityIncomingMsg[j][iIndex][xj];
                
                facilitySumIncomingMsg[j][xj] = sum;
            }
        }
    }

    protected void computeOptimalActions() {
        for (int j = 0; j < facilityCount; j++) {
            optimalFacility[j] = 
                facilitySumIncomingMsg[j][0] >= facilitySumIncomingMsg[j][1];
            optFacilityAction[j] = optimalFacility[j] ? 1 : 0;
        }
        for (int j = 0; j < cityCount; j++) {
            int jDegree = cityAdjFacility[j].length;
            double minValue = Double.MAX_VALUE;
            int minX = -1;
            for (int xj = 0; xj < jDegree; xj++) {
                if (citySumIncomingMsg[j][xj] < minValue) {
                    minValue = citySumIncomingMsg[j][xj];
                    minX = xj;
                }
//                 int i = cityAdjFacility[j][xj];
//                 if (optFacilityAction[i] == 1) {
//                     double c = problem.getDistance(j, i);
//                     if (c < minValue) {
//                         minValue = c;
//                         minX = xj;
//                     }
//                 }
            }
            optCityAction[j] = minX;
        }
    }

    protected void computeObjectiveValue() {
        // compute objective
        objectiveValue = 0.0;
        for (int j = 0; j < facilityCount; j++) {
            if (optFacilityAction[j] == 1)
                objectiveValue += problem.getConstructionCost(j);
        }
        for (int j = 0; j < cityCount; j++) {
            int xj = optCityAction[j];
            if (xj < 0) {
                objectiveValue = Double.MAX_VALUE;
                break;
            }
            int i = cityAdjFacility[j][xj];
            if (optFacilityAction[i] == 0) {
                objectiveValue = Double.MAX_VALUE;
                break;
            }
            objectiveValue += problem.getDistance(j, i);
        }
    }

    public void iterate() {
        // apply the min-sum operator ...
        // ... to messages to cities
        for (int j = 0; j < cityCount; j++) {
            int jDegree = cityAdjFacility[j].length;
            for (int iIndex = 0; iIndex < jDegree; iIndex++) {
                int i = cityAdjFacility[j][iIndex];
                int offset = facilityAdjCityIndex[i][j];
                for (int r_xj = 0; r_xj < 2; r_xj++) {
                    double minValue = Double.MAX_VALUE;
                    for (int xi = 0; xi < 2; xi++) {
                        // add incoming messages, local potential
                        double thisValue = facilitySumIncomingMsg[i][xi]
                            - facilityIncomingMsg[i][offset][xi];
                        // pairwise cost at (i,j)
                        if (r_xj == 1 && xi != 1) 
                            thisValue += bigCost;
                        if (thisValue < minValue)
                            minValue = thisValue;
                    }
                    nextCityIncomingMsg[j][iIndex][r_xj] = minValue;
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
                int offset = cityAdjFacilityIndex[i][j];
                for (int xj = 0; xj < 2; xj++) {
                    double minValue = Double.MAX_VALUE;
                    for (int xi = 0; xi < iDegree; xi++) {
                        // add incoming messages, local potential
                        int r_xi = xi == offset ? 1 : 0;
                        double thisValue = citySumIncomingMsg[i][xi]
                            - cityIncomingMsg[i][offset][r_xi];
                        // pairwise cost at (i,j)
                        if (r_xi == 1 && xj != 1)
                            thisValue += bigCost;
                        if (thisValue < minValue) 
                            minValue = thisValue;
                    }
                    nextFacilityIncomingMsg[j][iIndex][xj] = minValue;
                }
            }
        }

        // normalize and compute bellman error
        bellmanError = 0.0;
        sumNorm = 0.0;
        for (int j = 0; j < cityCount; j++) {
            int jDegree = cityAdjFacility[j].length;
            for (int iIndex = 0; iIndex < jDegree; iIndex++) {
                int i = cityAdjFacility[j][iIndex];
                double norm;
                norm = nextCityIncomingMsg[j][iIndex][0];
                sumNorm += norm;
                for (int r_xj = 0; r_xj < 2; r_xj++) {
                    nextCityIncomingMsg[j][iIndex][r_xj] -= norm;
                    if (damp > 0.0) 
                        nextCityIncomingMsg[j][iIndex][r_xj] =
                            (1.0 - damp) * nextCityIncomingMsg[j][iIndex][r_xj]
                            + damp * cityIncomingMsg[j][iIndex][r_xj];
                    double dv = cityIncomingMsg[j][iIndex][r_xj]
                        - nextCityIncomingMsg[j][iIndex][r_xj];
                    bellmanError += dv*dv;
                }
            }
        }
        for (int j = 0; j < facilityCount; j++) {
            int jDegree = facilityAdjCity[j].length;
            for (int iIndex = 0; iIndex < jDegree; iIndex++) {
                int i = facilityAdjCity[j][iIndex];
                double norm;
                norm = nextFacilityIncomingMsg[j][iIndex][0];
                sumNorm += norm;
                for (int xj = 0; xj < 2; xj++) {
                    nextFacilityIncomingMsg[j][iIndex][xj] -= norm;
                    if (damp > 0.0) 
                        nextFacilityIncomingMsg[j][iIndex][xj] =
                            (1.0 - damp) 
                            * nextFacilityIncomingMsg[j][iIndex][xj]
                            + damp * facilityIncomingMsg[j][iIndex][xj];
                    double dv = facilityIncomingMsg[j][iIndex][xj]
                        - nextFacilityIncomingMsg[j][iIndex][xj];
                    bellmanError += dv*dv;
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

        computeSumIncomingMessages();

        computeOptimalActions();

        computeObjectiveValue();
    }

    // i is a facility, j a city
    private double getPairwiseMarginal(int i, int j, int xi, int xj) {
        double sum = 0.0;
        int iOffset = cityAdjFacilityIndex[j][i];
        int jOffset = facilityAdjCityIndex[i][j];
        int r_xj = iOffset == xj ? 1 : 0;
        // pairwise potential
        if (r_xj == 1 && xi == 0)
            sum += bigCost;
        // incoming messages
        sum -= facilityIncomingMsg[i][jOffset][xi];
        sum -= cityIncomingMsg[j][iOffset][r_xj];
        return sum;
    }

    public boolean isGlobalOptimum() { 
        for (int j = 0; j < cityCount; j++) {
            int jDegree = cityAdjFacility[j].length;
            for (int iIndex = 0; iIndex < jDegree; iIndex++) {
                int i = cityAdjFacility[j][iIndex];

                double base = getPairwiseMarginal(i, 
                                                  j, 
                                                  optFacilityAction[i],
                                                  optCityAction[j]);

                for (int xj = 0; xj < jDegree; xj++) {
                    for (int xi = 0; xi < 2; xi++) {
                        double v = getPairwiseMarginal(i,
                                                       j,
                                                       xi,
                                                       xj);
                        if (v < base)
                            return false;
                    }
                }
            }
        }
        return true;
    }

}
   