package com.moallemi.matching;

import java.util.*;

import ilog.concert.*;
import ilog.cplex.*;

import com.moallemi.util.data.*;
import com.moallemi.math.*;

public class SalpSolverOnline implements MatchingSolver {
  private ArrayList<Pair<Item, Item>> sampledPairs;
  private ItemFunctionSet basisSetSupply, basisSetDemand;
  private RewardFunction rewardFun;
  private double eps;
  // cplex stuff
  IloCplex cplex;
  IloNumVar[] kappaOneVar, kappaTwoVar;
  IloNumVar[] sVar;
  IloRange[] salpConst;

  public SalpSolverOnline(CplexFactory factory,
                          ArrayList<Pair<Item, Item>> sampledPairs,
                          ItemFunctionSet basisSetSupply,
                          ItemFunctionSet basisSetDemand,
                          RewardFunction rewardFun,
                          double eps)
      throws IloException {

    this.sampledPairs = sampledPairs;
    this.basisSetSupply = basisSetSupply;
    this.basisSetDemand = basisSetDemand;
    this.rewardFun = rewardFun;
    this.eps = eps;
    cplex = factory.getCplex();
    //set up cplex stuff
    int basisCountSupply = basisSetSupply.size();
    int basisCountDemand = basisSetDemand.size();
    double[] lbS = new double[basisCountSupply];
    double[] ubS = new double[basisCountSupply];
    double[] lbD = new double[basisCountDemand];
    double[] ubD = new double[basisCountDemand];
    double[] objCoeffTwo = new double[basisCountDemand];
    double[] objCoeffOne = new double[basisCountSupply];
    for (int i = 0; i < basisCountSupply; i++) {
      lbS[i] = -Double.MAX_VALUE;
      ubS[i] = Double.MAX_VALUE;
      objCoeffOne[i] = 0.0;
    }
    for (int i = 0; i < basisCountDemand; i++) {
      lbD[i] = -Double.MAX_VALUE;
      ubD[i] = Double.MAX_VALUE;
      objCoeffTwo[i] = 0.0;
    }
    kappaOneVar = cplex.numVarArray(basisCountSupply, lbS, ubS);
    kappaTwoVar = cplex.numVarArray(basisCountDemand, lbD, ubD);

    IloLinearNumExpr obj = cplex.linearNumExpr();

    int nP = sampledPairs.size();
    double[] lbP = new double[nP];
    double[] ubP = new double[nP];
    Arrays.fill(lbP, 0.0);
    double ubPValue = Double.MAX_VALUE;
    if (this.eps > 1E8) {
      ubPValue = 0.0;
      System.out.println("Solving ALP");
    }
    Arrays.fill(ubP, ubPValue);
    sVar = cplex.numVarArray(nP, lbP, ubP);

    salpConst = new IloRange[nP];
    IloLinearNumExpr tempExp;
    for (int p = 0; p < nP; p++) {
      Item typeS = this.sampledPairs.get(p).getFirst();
      Item typeD = this.sampledPairs.get(p).getSecond();
      tempExp = cplex.scalProd(basisSetSupply.evaluate(typeS), kappaOneVar);
      tempExp.addTerms(basisSetDemand.evaluate(typeD), kappaTwoVar);
      tempExp.addTerm(1.0, sVar[p]);
      salpConst[p] = cplex.addGe(tempExp, this.rewardFun.evaluate(typeS, typeD));
      obj.add(tempExp);
    }
    double[] epsArray = new double[nP];
    if (this.eps <= 1E8) {
      Arrays.fill(epsArray, this.eps);
      obj.add(cplex.scalProd(epsArray, sVar));
    }
    cplex.addMinimize(obj);
  }

  public boolean solve() throws IloException {
    boolean status = cplex.solve();
    double rw = 0.0;
    System.out.println("objective = " + cplex.getObjValue());
    for (int p = 0; p < sampledPairs.size(); p++) {
      Item typeS = this.sampledPairs.get(p).getFirst();
      Item typeD = this.sampledPairs.get(p).getSecond();
      rw += this.rewardFun.evaluate(typeS, typeD);
    }
    System.out.println("total reward = " + rw);
    return status;
  }

  public ItemFunction getSupplyValue() throws IloException {
    double[] kappaOne = cplex.getValues(kappaOneVar);
    return basisSetSupply.getLinearCombination(kappaOne);
  }

  public ItemFunction getDemandValue() throws IloException {
    double[] kappaTwo = cplex.getValues(kappaTwoVar);
    return basisSetDemand.getLinearCombination(kappaTwo);
  }
}
