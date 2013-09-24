package com.moallemi.facloc;

public class LocalOptimumTester {
    protected FacilityLocationProblem problem;
    protected int cityCount, facilityCount;
    
    // facilities connected to each city
    protected int[][] cityAdjFacility;

    protected boolean[] optimalFacility;
    protected int[] cityBestFacility;
    protected int[] cityBestFacility2;
    protected boolean infiniteCost;

    public LocalOptimumTester(FacilityLocationProblem problem) {
        this.problem = problem;
        cityCount = problem.getCityCount();
        facilityCount = problem.getFacilityCount();

       cityAdjFacility = new int [cityCount][];
       int[] tmp = new int [facilityCount];
       for (int c = 0; c < cityCount; c++) {
           int cnt = 0;
           for (int f = 0; f < facilityCount; f++) {
               if (problem.getDistance(c,f) < Double.MAX_VALUE)
                   tmp[cnt++] = f;
           }
           cityAdjFacility[c] = new int [cnt];
           System.arraycopy(tmp, 0, cityAdjFacility[c], 0, cnt);

           // sort based on distance to city
           for (int fIndex = 1; fIndex < cnt; fIndex++) {
               int f = cityAdjFacility[c][fIndex];
               double v = problem.getDistance(c, f);
               int kIndex = fIndex - 1;
               while (kIndex >= 0
                      && problem.getDistance(c, cityAdjFacility[c][kIndex]) 
                      > v) {
                   cityAdjFacility[c][kIndex+1] = cityAdjFacility[c][kIndex];
                   kIndex--;
               }
               cityAdjFacility[c][kIndex+1] = f;
           }
       }

       optimalFacility = new boolean [facilityCount];
       cityBestFacility = new int [cityCount];
       cityBestFacility2 = new int [cityCount];

    }

    public void setOptimalFacilities(boolean[] iOptimalFacility) {
        infiniteCost = false;
        System.arraycopy(iOptimalFacility, 0, optimalFacility, 0, 
                         facilityCount);

        for (int c = 0; c < cityCount; c++) {
            cityBestFacility[c] = -1;
            cityBestFacility2[c] = -1;
            for (int fIndex = 0; fIndex < cityAdjFacility[c].length; fIndex++) {
                if (optimalFacility[cityAdjFacility[c][fIndex]]) {
                    if (cityBestFacility[c] == -1) 
                        cityBestFacility[c] = cityAdjFacility[c][fIndex];
                    else {
                        cityBestFacility2[c] = cityAdjFacility[c][fIndex];
                        break;
                    }
                }
            }
            if (cityBestFacility[c] == -1) {
                infiniteCost = true;
                break;
            }
        }
    }

    // see if we can add any facility and improve the cost after
    // reassigning other cities
    public boolean isLocalOptimumAddFacility() {
        if (infiniteCost)
            return false;

        // loop through facilities which are closed
        for (int f = 0; f < facilityCount; f++) {
            if (!optimalFacility[f]) {
                double cost = problem.getConstructionCost(f);
                
                for (int c = 0; c < cityCount; c++) {
                    double vNew = problem.getDistance(c, f);
                    if (vNew < Double.MAX_VALUE) {
                        double vOld = 
                            problem.getDistance(c, cityBestFacility[c]);
                        if (vNew < vOld) 
                            cost += vNew - vOld;
                    }
                }
                if (cost < 0.0) 
                    return false;
            }
        }
        return true;

    }


    // see if we can add drop facility and improve the cost after
    // reassigning other cities
    public boolean isLocalOptimumDropFacility() {
        if (infiniteCost)
            return false;
        
        // loop through facilities which are open
        for (int f = 0; f < facilityCount; f++) {
            if (optimalFacility[f]) {
                double cost =  -problem.getConstructionCost(f);
                
                for (int c = 0; c < cityCount; c++) {
                    if (cityBestFacility[c] == f) {
                        if (cityBestFacility2[c] >= 0) {
                            cost += 
                                problem.getDistance(c, cityBestFacility2[c])
                                -
                                problem.getDistance(c, f);
                        }
                        else {
                            cost = Double.MAX_VALUE;
                            break;
                        }
                    }
                }

                if (cost < 0.0) 
                    return false;
            }
        }
        return true;
    }

    // see if we can add drop facility and add another and improve the
    // cost after reassigning other cities
    public boolean isLocalOptimumSwapFacility() {
        if (infiniteCost)
            return false;

        for (int fAdd = 0; fAdd < facilityCount; fAdd++) {
            if (optimalFacility[fAdd]) 
                continue;

            for (int fDrop = 0; fDrop < facilityCount; fDrop++) {
                if (!optimalFacility[fDrop])
                    continue;
                
                double cost = problem.getConstructionCost(fAdd)
                    - problem.getConstructionCost(fDrop);

                for (int c = 0; c < cityCount; c++) {
                    if (cityBestFacility[c] == fDrop) {
                        double vOld = problem.getDistance(c, fDrop);
                        double vNew = problem.getDistance(c, fAdd);
                        if (cityBestFacility2[c] >= 0) {
                            double vNew2 = 
                                problem.getDistance(c, cityBestFacility2[c]);
                            if (vNew2 < vNew)
                                vNew = vNew2;
                        }
                        if (vNew < Double.MAX_VALUE) {
                            cost += vNew - vOld;
                        }
                        else {
                            cost = Double.MAX_VALUE;
                            break;
                        }
                    }
                    else {
                        double vNew = problem.getDistance(c, fAdd);
                        if (vNew < Double.MAX_VALUE) {
                            double vOld = 
                                problem.getDistance(c, cityBestFacility[c]);
                            if (vNew < vOld) 
                                cost += vNew - vOld;
                        }
                    }
                }

                if (cost < 0.0)
                    return false;
            }
        }

        return true;
    }

    // see if we can add drop facility and add another and improve the
    // cost after reassigning other cities
    public boolean isLocalOptimumSwap2Facility() {
        if (infiniteCost)
            return false;

        for (int fAdd = 0; fAdd < facilityCount; fAdd++) {
            if (optimalFacility[fAdd]) 
                continue;

            for (int fDrop = 0; fDrop < facilityCount; fDrop++) {
                if (!optimalFacility[fDrop])
                    continue;
                
                double cost = problem.getConstructionCost(fAdd)
                    - problem.getConstructionCost(fDrop);

                for (int c = 0; c < cityCount; c++) {
                    if (cityBestFacility[c] == fDrop) {
                        double vOld = problem.getDistance(c, fDrop);
                        double vNew = problem.getDistance(c, fAdd);
                        if (vNew < Double.MAX_VALUE)
                            cost += vNew - vOld;
                        else {
                            cost = Double.MAX_VALUE;
                            break;
                        }
                    }
                }

                if (cost < 0.0)
                    return false;
            }
        }

        return true;
    }




}
        