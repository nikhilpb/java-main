package com.moallemi.matching;

import java.util.*;

import ilog.concert.*;
import ilog.cplex.*;

import com.moallemi.math.*;

public class DualAverageSolverOnline implements MatchingSolver {
  private ArrayList<Item> sampledSupplyTypes;
  private OnlineMatchingModel model;
  private long matchingSeed;
  private int matchingCount;
  private double[] lambda;
  // cplex stuff
  IloCplex cplex;
  IloNumVar[] lambdaSVar, lambdaDVar;
  IloRange[][] matchConst;

  public DualAverageSolverOnline(CplexFactory factory,
                                 OnlineMatchingModel model,
                                 ArrayList<Item> sampledSupplyTypes,
                                 int matchingCount,
                                 long matchingSeed)
      throws IloException {
    //initilize
    this.sampledSupplyTypes = sampledSupplyTypes;
    this.model = model;
    this.matchingCount = matchingCount;
    cplex = factory.getCplex();

  }

  public boolean solve() throws IloException {
    Random sampler = new Random(matchingSeed);
    int n = sampledSupplyTypes.size();
    double[] lb = new double[n];
    double[] ub = new double[n];
    Arrays.fill(lb, -Double.MAX_VALUE);
    Arrays.fill(ub, Double.MAX_VALUE);
    double[] objCoeff = new double[n];
    Arrays.fill(objCoeff, 1.0);
    ArrayList<Item> sampledDemandTypes;
    RewardFunction rewardFun = model.getRewardFunction();
    lambda = new double[n];
    Arrays.fill(lambda, 0.0);
    for (int i = 0; i < matchingCount; i++) {
      System.out.println("match run " + i);
      lambdaSVar = cplex.numVarArray(n, lb, ub);
      lambdaDVar = cplex.numVarArray(n, lb, ub);
      IloLinearNumExpr obj = cplex.scalProd(objCoeff, lambdaSVar);
      obj.addTerms(objCoeff, lambdaDVar);
      cplex.addMinimize(obj);
      long dSeed = sampler.nextLong();
      model.setDemRandomSeed(dSeed);
      sampledDemandTypes = model.sampleDemandTypes(n);
      for (int s = 0; s < n; s++) {
        for (int d = 0; d < n; d++) {
          cplex.addGe(cplex.sum(
              cplex.prod(1.0, lambdaSVar[s]),
              cplex.prod(1.0, lambdaDVar[d])),
              rewardFun.evaluate(
                  sampledSupplyTypes.get(s),
                  sampledDemandTypes.get(d)
              )
          );
        }
      }
      cplex.solve();
      System.out.println("matching model solved. objective value - " + cplex.getObjValue());
      double[] thisLambda = cplex.getValues(lambdaSVar);
      for (int j = 0; j < n; j++) {
        lambda[j] = (lambda[j] * i + thisLambda[j]) / (i + 1);
      }
      cplex.clearModel();
    }
    return true;
  }

  public ItemFunction getSupplyValue() throws IloException {
    return new SimpleItemFunction(sampledSupplyTypes, lambda);
  }

  public ItemFunction getDemandValue() throws IloException {
    return new ConstantItemFunction(0.0);
  }
}
