package com.moallemi.facloc;

public class BipartiteFLMinSumSolver implements MinSumSolver {
    protected FacilityLocationProblem problem;

    protected int cityCount, facilityCount;

    // indexed by [facility][city][city_state]
    protected double[][][] msgToCity;
    protected double[][][] nextMsgToCity;
    // indexed by [city][facility][facility_state];
    protected double[][][] msgToFacility;
    protected double[][][] nextMsgToFacility;
    // optimal facilities to open
    protected boolean[] optimalFacility;
    protected double bellmanError, objectiveValue;
    protected double damp;

    protected double sumNorm;

    public BipartiteFLMinSumSolver(FacilityLocationProblem problem,
                                   double damp) {
        this.problem = problem;
        this.damp = damp;
        cityCount = problem.getCityCount();
        facilityCount = problem.getFacilityCount();
        msgToCity = new double [facilityCount][cityCount][facilityCount];
        nextMsgToCity = new double [facilityCount][cityCount][facilityCount];
        msgToFacility = new double [cityCount][facilityCount][2];
        nextMsgToFacility = new double [cityCount][facilityCount][2];
        optimalFacility = new boolean [facilityCount];
    }

    public double getBellmanError() { return bellmanError; }
    public double getObjectiveValue() { return objectiveValue; }
    public double getSumNorm() { return sumNorm; }
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

    protected double getSumMsgToFacility(int i, int j, int xi) {
        double sum = 0.0;
        for (int u = 0; u < j; u++) {
            if (msgToFacility[u][i][xi] >= Double.MAX_VALUE) 
                return Double.MAX_VALUE;
            sum += msgToFacility[u][i][xi];
        }
        for (int u = j+1; u < cityCount; u++) {
            if (msgToFacility[u][i][xi] >= Double.MAX_VALUE) 
                return Double.MAX_VALUE;
            sum += msgToFacility[u][i][xi];
        }
        return sum;
    }

    protected double getSumMsgToCity(int i, int j, int xi) {
        double sum = 0.0;
        for (int u = 0; u < j; u++) {
            if (msgToCity[u][i][xi] >= Double.MAX_VALUE) 
                return Double.MAX_VALUE;
            sum += msgToCity[u][i][xi];
        }
        for (int u = j+1; u < facilityCount; u++) {
            if (msgToCity[u][i][xi] >= Double.MAX_VALUE) 
                return Double.MAX_VALUE;
            sum += msgToCity[u][i][xi];
        }
        return sum;

    }

    protected void computeOptimalActions() {
        for (int j = 0; j < facilityCount; j++) {
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

    protected void computeObjectiveValue() {
        objectiveValue = 0.0;
        for (int j = 0; j < facilityCount; j++) {
            if (optimalFacility[j])
                objectiveValue += problem.getConstructionCost(j);
        }
        for (int i = 0; i < cityCount; i++) {
            double minDist = Double.MAX_VALUE;
            for (int j = 0; j < facilityCount; j++) {
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

    protected void normalizeMessages() {
        // normalize and compute bellman error
        bellmanError = 0.0;
        sumNorm = 0.0;
        for (int f = 0; f < facilityCount; f++) {
            for (int c = 0; c < cityCount; c++) {
                int i, j;
                double norm;

                i = f;
                j = c;
                norm = nextMsgToCity[i][j][0];
                if (norm >= Double.MAX_VALUE) 
                    continue;
                    //throw new IllegalStateException("unable to normalize");
                sumNorm += norm;
                for (int xj = 0; xj < facilityCount; xj++) {
                    if (nextMsgToCity[i][j][xj] < Double.MAX_VALUE) {
                        nextMsgToCity[i][j][xj] -= norm;
                        if (damp > 0.0) {
                            if (msgToCity[i][j][xj] >= Double.MAX_VALUE) 
                                nextMsgToCity[i][j][xj] = Double.MAX_VALUE;
                            else
                                nextMsgToCity[i][j][xj] =
                                    (1.0 - damp) * nextMsgToCity[i][j][xj]
                                    + damp * msgToCity[i][j][xj];
                        }
                        if (msgToCity[i][j][xj] < Double.MAX_VALUE) {
                            double dv = msgToCity[i][j][xj]
                                - nextMsgToCity[i][j][xj];
                            bellmanError += dv*dv;
                        }
                    }
                }
            }
        }
        for (int c = 0; c < cityCount; c++) {
            for (int f = 0; f < facilityCount; f++) {
                int i, j;
                double norm;

                i = c;
                j = f;
                norm = nextMsgToFacility[i][j][0];
                if (norm >= Double.MAX_VALUE) 
                    continue;
                    //throw new IllegalStateException("unable to normalize");
                sumNorm += norm;
                for (int xj = 0; xj < 2; xj++) {
                    if (nextMsgToFacility[i][j][xj] < Double.MAX_VALUE) {
                        nextMsgToFacility[i][j][xj] -= norm;
                        if (damp > 0.0) {
                            if (msgToFacility[i][j][xj] >= Double.MAX_VALUE)
                                nextMsgToFacility[i][j][xj] = Double.MAX_VALUE;
                            else
                                nextMsgToFacility[i][j][xj] =
                                    (1.0 - damp) * nextMsgToFacility[i][j][xj]
                                    + damp * msgToFacility[i][j][xj];
                        }
                        if (msgToFacility[i][j][xj] < Double.MAX_VALUE) {
                            double dv = msgToFacility[i][j][xj]
                                - nextMsgToFacility[i][j][xj];
                            bellmanError += dv*dv;
                        }
                    }
                }
            }
        }
        //System.out.println(sumNorm);
    }

    public void iterate() {
        updateMessages();

        normalizeMessages();

        // swap
        double[][][] tmp;
        tmp = msgToCity;
        msgToCity = nextMsgToCity;
        nextMsgToCity = tmp;
        tmp = msgToFacility;
        msgToFacility = nextMsgToFacility;
        nextMsgToFacility = tmp;

        computeOptimalActions();

        computeObjectiveValue();
    }

   
    public boolean isGlobalOptimum() { return false; }

}
   