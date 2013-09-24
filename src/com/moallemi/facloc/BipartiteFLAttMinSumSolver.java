package com.moallemi.facloc;

import java.util.Arrays;

public class BipartiteFLAttMinSumSolver extends BipartiteFLMinSumSolver {
    private double alphaInv;
    private double[] buildValue;

    public BipartiteFLAttMinSumSolver(FacilityLocationProblem problem,
                                      double damp) {
        super(problem, damp);
        if (cityCount != facilityCount) 
            throw new IllegalArgumentException("must have same cities "
                                               + "and facilities");
        alphaInv = 1.0 / ((double) facilityCount - 1.0);
        buildValue = new double [facilityCount];
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

    protected void computeOptimalActions() {
        for (int j = 0; j < facilityCount; j++) 
            buildValue[j] = getFacilityMarginal(j, 1);
        double lastObjective = Double.MAX_VALUE;
        Arrays.fill(optimalFacility, false);
        for (int cnt = 0; cnt < facilityCount; cnt++) {
            double minValue = Double.MAX_VALUE;
            int minFac = -1;

            for (int j = 0; j < facilityCount; j++) {
                if (!optimalFacility[j] && buildValue[j] < minValue) {
                    minValue = buildValue[j];
                    minFac = j;
                }
            }
            optimalFacility[minFac] = true;
            computeObjectiveValue();
//             System.out.println(objectiveValue + " " + getOptimalFacilitiesString());
            if (objectiveValue > lastObjective) {
                optimalFacility[minFac] = false;
                break;
            }
            lastObjective = objectiveValue;
        }
    }


//      protected void computeOptimalActions() {
//          double min = Double.MAX_VALUE;
//          int minFac = -1;
//          for (int j = 0; j < facilityCount; j++) {
//              double sum = problem.getConstructionCost(j);
//              for (int i = 0; i < cityCount; i++) {
//                  if (msgToFacility[i][j][1] >= Double.MAX_VALUE) {
//                      sum = Double.MAX_VALUE;
//                      break;
//                  }
//                  sum += msgToFacility[i][j][1];
//              }
//              buildValue[j] = sum;
//              if (sum < min) {
//                  min = sum;
//                  minFac = j;
//              }
//              System.out.println("fac " + (j+1) + ": " + sum);
//          }
//          Arrays.fill(optimalFacility, false);
//          optimalFacility[minFac] = true;
//          for (int i = 0; i < cityCount; i++) {
//              double minValue = Double.MAX_VALUE;
//              int minX = -1;
//              for (int xi = 0; xi < facilityCount; xi++) {
//                  double thisValue = 0.0;
//                  // single node potential at i
//                  thisValue += problem.getDistance(i, xi);
//                  // pairwise cost at (i,minFac) is always zero
//                  // sum incoming messages
//                  double sum = getSumMsgToCity(i, minFac, xi);
//                  if (sum >= Double.MAX_VALUE)
//                      continue;  // thisValue = Double.MAX_VALUE;
//                  thisValue += sum;

//                  if (thisValue < minValue) {
//                      minValue = thisValue;
//                      minX = xi;
//                  }
//                  System.out.println("city " + (i+1) + " fac " + (xi+1) + ": "
//                                      + thisValue);
//              }
//              if (minX < 0)
//                  throw new IllegalStateException("could not find optimal "
//                                                  + "facility");
//              optimalFacility[minX] = true;
//          }
//      }

//      protected void computeOptimalActions() {
//          TupleIterator ti = new TupleIterator(facilityCount, 2);
//          while (ti.hasNext()) {
//              int[] facStatus = ti.next();

//              for (int i = 0; i < cityCount; i++) {
//                  for (int j = 0; j < facilityCount; j++) {


    public boolean isGlobalOptimum() {
        int[] optFacilityAction = new int [facilityCount];
        for (int j = 0; j < facilityCount; j++) 
            optFacilityAction[j] = optimalFacility[j] ? 1 : 0;
        int[] optCityAction = new int [cityCount];
        for (int j = 0; j < cityCount; j++) {
            double min = Double.MAX_VALUE;
            int minX = 0;
            for (int xj = 0; xj < facilityCount; xj++) {
                if (optimalFacility[xj]) {
                    double sum = problem.getDistance(j, xj);
                    if (sum < min) {
                        min = sum;
                        minX = xj;
                    }
                }
            }
            optCityAction[j] = minX;
        }

        //dumpPolicyInfo();

        // check policy on messages to cities
        for (int j = 0; j < cityCount; j++) {
            for (int i = 0; i < facilityCount; i++) {
                double min = getTJFacilityToCity(i, 
                                                 j,
                                                 optFacilityAction[i],
                                                 optCityAction[j]);
                for (int xi = 0; xi < 2; xi++) {
                    double v = getTJFacilityToCity(i,
                                                   j,
                                                   xi,
                                                   optCityAction[j]);
                    if (v < min) 
                        return false;
                }
            }
        }

        // check policy on messages to facilities
        for (int j = 0; j < facilityCount; j++) {
            for (int i = 0; i < cityCount; i++) {
                double min = getTJCityToFacility(i, 
                                                 j,
                                                 optCityAction[i],
                                                 optFacilityAction[j]);
                for (int xi = 0; xi < facilityCount; xi++) {
                    double v = getTJCityToFacility(i,
                                                   j,
                                                   xi,
                                                   optFacilityAction[j]);
                    if (v < min) 
                        return false;
                }
            }
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
    
    // i a facility, j a city
    private double getTJFacilityToCity(int i, int j, int xi, int xj) {
        double v = 0.0;
        if (xi == 1)
            v += problem.getConstructionCost(i);
        if (xj == i && xi != 1)
            return Double.MAX_VALUE;
        double sum = getSumMsgToFacility(i, j, xi);
        if (sum >= Double.MAX_VALUE)
            return Double.MAX_VALUE;
        v += sum;
        return v;
    }


    // i a city, j a facility
    private double getTJCityToFacility(int i, int j, int xi, int xj) {
        double v = problem.getDistance(i, xi);
        if (xi == j && xj != 1)
            return Double.MAX_VALUE;
        double sum = getSumMsgToCity(i, j, xi);
        if (sum >= Double.MAX_VALUE)
            return Double.MAX_VALUE;
        v += sum;
        return v;
    }


    private void dumpPolicyInfo() {
        int[] optFacilityAction = new int [facilityCount];
        for (int j = 0; j < facilityCount; j++) 
            optFacilityAction[j] = optimalFacility[j] ? 1 : 0;
        int[] optCityAction = new int [cityCount];
        for (int j = 0; j < cityCount; j++) {
            double min = Double.MAX_VALUE;
            int minX = 0;
            for (int xj = 0; xj < facilityCount; xj++) {
                if (optimalFacility[xj]) {
                    double sum = problem.getDistance(j, xj);
                    if (sum < min) {
                        min = sum;
                        minX = xj;
                    }
                }
            }
            optCityAction[j] = minX;
        }

        // check policy on messages to cities
        for (int i = 0; i < facilityCount; i++) {
            for (int j = 0; j < cityCount; j++) {
                for (int xj = 0; xj < facilityCount; xj++) {
//                     if (xj != optCityAction[j])
//                         continue;

                    double min = Double.MAX_VALUE;
                    for (int xi = 0; xi < 2; xi++) {
                        double v = getTJFacilityToCity(i,
                                                       j,
                                                       xi,
                                                       xj);

                        if (v < min) 
                            min = v;
                    }

                    for (int xi = 0; xi < 2; xi++) {
                        double v = getTJFacilityToCity(i,
                                                       j,
                                                       xi,
                                                       xj);
                        System.out.println("des_c: " + (j+1) 
                                           + " xj: " + (xj+1)
                                           + " "
                                           + (optCityAction[j] == xj 
                                              ? "*" : "x")
                                           + " src_f: " + (i+1) 
                                           + " "
                                           + xi
                                           + " "
                                           + (optFacilityAction[i] == xi 
                                              ? "*" : "x")
                                           + " "
                                           + (v <= min ? "M " : "")
                                           + v);

                    }

                    System.out.println();
                }
            }
        }

        // check policy on messages to facilities
        for (int i = 0; i < cityCount; i++) {
            for (int j = 0; j < facilityCount; j++) {
                for (int xj = 0; xj < 2; xj++) {
//                     if (xj != optFacilityAction[j])
//                         continue;

                    double min = Double.MAX_VALUE;
                    for (int xi = 0; xi < facilityCount; xi++) {
                        double v = getTJCityToFacility(i,
                                                       j,
                                                       xi,
                                                       xj);
                        if (v < min)
                            min = v;
                    }

                    for (int xi = 0; xi < facilityCount; xi++) {
                        double v = getTJCityToFacility(i,
                                                       j,
                                                       xi,
                                                       xj);
                        
                        System.out.println("des_f: " + (j+1) 
                                           + " xj: " + (xj)
                                           + " "
                                           + (optFacilityAction[j] == xj 
                                              ? "*" : "x")
                                           + " src_c: " + (i+1) 
                                           + " "
                                           + (xi+1)
                                           + " "
                                           + (optCityAction[i] == xi 
                                              ? "*" : "x")
                                           + " "
                                           + (v <= min ? "M " : "")
                                           + v);
                    }

                    System.out.println();
                }
            }
        }
    }
        
}
   