package com.moallemi.facloc;

import java.util.Arrays;

public class SparseBipartiteFLAttMinSumSolver 
    extends SparseBipartiteFLMinSumSolver 
{
    private double alphaInv;
    private double[] buildValue;

    public SparseBipartiteFLAttMinSumSolver(FacilityLocationProblem problem,
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
        double[] buildValue = new double [facilityCount];
        for (int j = 0; j < facilityCount; j++) {
            double sum = problem.getConstructionCost(j);
            int degree = facilityAdjCity[j].length;
            for (int iIndex = 0; iIndex < degree; iIndex++) {
                if (facilityIncomingMsg[j][iIndex][1] >= Double.MAX_VALUE) {
                    sum = Double.MAX_VALUE;
                    break;
                }
                sum += facilityIncomingMsg[j][iIndex][1];
            }
            buildValue[j] = sum;
            System.out.println("fac " + (j+1) + ": " + sum);
        }
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
            System.out.println(objectiveValue + " " 
                               + getOptimalFacilitiesString());
            if (objectiveValue > lastObjective) {
                optimalFacility[minFac] = false;
                break;
            }
            lastObjective = objectiveValue;
        }
    }

}
   