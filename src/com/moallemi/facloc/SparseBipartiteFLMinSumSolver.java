package com.moallemi.facloc;

public class SparseBipartiteFLMinSumSolver implements MinSumSolver {
    protected FacilityLocationProblem problem;

    protected int cityCount, facilityCount;

    // optimal facilities to open
    protected boolean[] optimalFacility;
    protected double bellmanError, objectiveValue;
    protected double damp;

    protected double sumNorm;

    // indexed by [city][facility_adj_index][city_state]
    protected double[][][] cityIncomingMsg;
    protected double[][][] nextCityIncomingMsg;
    // indexed by [facility][city_adj_index][facility_state]
    protected double[][][] facilityIncomingMsg;
    protected double[][][] nextFacilityIncomingMsg;    

    // facilities connected to each city
    protected int[][] cityAdjFacility;
    // cities connected to each facility
    protected int[][] facilityAdjCity;

    public SparseBipartiteFLMinSumSolver(FacilityLocationProblem problem,
                                         double damp) {
        this.problem = problem;
        this.damp = damp;
        cityCount = problem.getCityCount();
        facilityCount = problem.getFacilityCount();

        cityAdjFacility = new int [cityCount][];
        int[] tmp = new int [facilityCount];
        for (int i = 0; i < cityCount; i++) {
            int cnt = 0;
            for (int j = 0; j < facilityCount; j++) {
                if (problem.getDistance(i,j) < Double.MAX_VALUE)
                    tmp[cnt++] = j;
            }
            cityAdjFacility[i] = new int [cnt];
            System.arraycopy(tmp, 0, cityAdjFacility[i], 0, cnt);
        }
        facilityAdjCity = new int [facilityCount][];
        tmp = new int [cityCount];
        for (int i = 0; i < facilityCount; i++) {
            int cnt = 0;
            for (int j = 0; j < cityCount; j++) {
                if (problem.getDistance(j,i) < Double.MAX_VALUE)
                    tmp[cnt++] = j;
            }
            facilityAdjCity[i] = new int [cnt];
            System.arraycopy(tmp, 0, facilityAdjCity[i], 0, cnt);
        }

        cityIncomingMsg = new double [cityCount][][];
        nextCityIncomingMsg = new double [cityCount][][];
        for (int j = 0; j < cityCount; j++) {
            int degree = cityAdjFacility[j].length;
            cityIncomingMsg[j] = new double [degree][degree];
            nextCityIncomingMsg[j] = new double [degree][degree];
        }
        facilityIncomingMsg = new double [facilityCount][][];
        nextFacilityIncomingMsg = new double [facilityCount][][];
        for (int j = 0; j < facilityCount; j++) {
            int degree = facilityAdjCity[j].length;
            facilityIncomingMsg[j] = new double [degree][2];
            nextFacilityIncomingMsg[j] = new double [degree][2];
        }
        optimalFacility = new boolean [facilityCount];
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

    // i is a city, j a facility
    protected double getSumMsgToCity(int i, int j, int xi) {
        double sum = 0.0;
        int degree = cityAdjFacility[i].length;
        for (int uIndex = 0; uIndex < degree; uIndex++) {
            if (cityAdjFacility[i][uIndex] == j)
                continue;
            if (cityIncomingMsg[i][uIndex][xi] >= Double.MAX_VALUE)
                return Double.MAX_VALUE;
            sum += cityIncomingMsg[i][uIndex][xi];
        }
        return sum;
    }

    // i is a facility, j a city
    protected double getSumMsgToFacility(int i, int j, int xi) {
        double sum = 0.0;
        int degree = facilityAdjCity[i].length;
        for (int uIndex = 0; uIndex < degree; uIndex++) {
            if (facilityAdjCity[i][uIndex] == j)
                continue;
            if (facilityIncomingMsg[i][uIndex][xi] >= Double.MAX_VALUE)
                return Double.MAX_VALUE;
            sum += facilityIncomingMsg[i][uIndex][xi];
        }
        return sum;
    }

    protected void computeOptimalActions() {
        for (int j = 0; j < facilityCount; j++) {
            double min = Double.MAX_VALUE;
            int minX = 0;
            int degree = facilityAdjCity[j].length;
            for (int xj = 0; xj < 2; xj++) {
                double sum = 0.0;
                if (xj == 1)
                    sum += problem.getConstructionCost(j);
                for (int iIndex = 0; iIndex < degree; iIndex++)  {
                    if (facilityIncomingMsg[j][iIndex][xj] 
                        >= Double.MAX_VALUE) {
                        sum = Double.MAX_VALUE;
                        break;
                    }
                    sum += facilityIncomingMsg[j][iIndex][xj];
                }
                if (sum < min) {
                    min = sum;
                    minX = xj;
                }
            }
            optimalFacility[j] = (minX == 1);
        }
    }

    protected void computeObjectiveValue() {
        // compute objective
        objectiveValue = 0.0;
        for (int j = 0; j < facilityCount; j++) {
            if (optimalFacility[j])
                objectiveValue += problem.getConstructionCost(j);
        }
        for (int i = 0; i < cityCount; i++) {
            int degree = cityAdjFacility[i].length;
            double minDist = Double.MAX_VALUE;
            for (int jIndex = 0; jIndex < degree; jIndex++) {
                int j = cityAdjFacility[i][jIndex];
                if (optimalFacility[j]) {
                    double d = problem.getDistance(i, j);
                    if (d < minDist)
                        minDist = d;
                }
            }
            if (minDist >= Double.MAX_VALUE) {
                objectiveValue = Double.MAX_VALUE;
                break;
            }
            objectiveValue += minDist;
        }

    }

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
                        if (cityAdjFacility[j][xj] == i && xi != 1) 
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
                        if (cityAdjFacility[i][xi] == j && xj != 1)
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

    public boolean isGlobalOptimum() { return false; }

}
   