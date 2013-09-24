package com.moallemi.facloc;

import ilog.concert.*;
import ilog.cplex.*;

import com.moallemi.math.CplexFactory;

public class CplexSolver implements FacilityLocationSolver {
    protected FacilityLocationProblem problem;
    protected IloCplex cplex;

    protected int cityCount, facilityCount;
     // facilities connected to each city
    protected int[][] cityAdjFacility;
    // cities connected to each facility
    protected int[][] facilityAdjCity;
   
    // optimal facilities to open
    protected boolean[] optimalFacility;
    protected double objectiveValue;
    protected boolean isOptimal;

    public CplexSolver(FacilityLocationProblem problem,
                       CplexFactory cplexFactory) throws IloException
    {
        this.problem = problem;
        this.cplex = cplexFactory.getCplex();
        cplex.setOut(null);

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

        optimalFacility = new boolean [facilityCount];
    }

    public boolean solve() throws IloException {
        
        IloIntVar[] fVar = cplex.boolVarArray(facilityCount);
        // add facility costs to objective
        double[] fCosts = new double [facilityCount];
        for (int i = 0; i < facilityCount; i++)
            fCosts[i] = problem.getConstructionCost(i);
        IloLinearNumExpr objective = cplex.scalProd(fCosts, fVar);
        for (int i = 0; i < cityCount; i++) {
            int degree = cityAdjFacility[i].length;
            IloIntVar[] xVar = cplex.boolVarArray(degree);

            // add constraint that we must be connected to some facility
            cplex.addGe(cplex.sum(xVar), 1.0);

            // add constraint that the facility must be open if we are
            // connected to it
            for (int jIndex = 0; jIndex < degree; jIndex++) 
                cplex.addGe(cplex.sum(fVar[cityAdjFacility[i][jIndex]],
                                      cplex.negative(xVar[jIndex])),
                            0.0);
                
            // add connection costs to objective
            double[] dCost = new double [degree];
            for (int jIndex = 0; jIndex < degree; jIndex++) 
                dCost[jIndex] = 
                    problem.getDistance(i, cityAdjFacility[i][jIndex]);
            objective.add(cplex.scalProd(dCost, xVar));
        }
        cplex.addMinimize(objective);

        boolean status = cplex.solve();
        isOptimal = cplex.getStatus() == IloCplex.Status.Optimal;
        objectiveValue  = cplex.getObjValue();
        for (int i = 0; i < facilityCount; i++)
            optimalFacility[i] = cplex.getValue(fVar[i]) > 0.0;

        return status;
    }
    

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
    public boolean isGlobalOptimum() { return isOptimal; }

}