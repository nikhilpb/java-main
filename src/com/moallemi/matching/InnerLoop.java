package com.moallemi.matching;

import ilog.concert.*;
import ilog.cplex.*;

import java.util.*;

public abstract class InnerLoop {
  protected ArrayList<Item> demandTypes;
  protected OnlineMatchingModel model;
  protected ArrayList<Item> supplyTypes;
  protected int innerSampleCount;

  protected IloCplex cplex;
  protected IloNumVar[][] piVar;
  protected IloRange[] supplyConst, demandConst;

  private double[] J, EJ;
  private double[][] dJ, dEJ;

  private double[][] refPolicy;

  public double run()
      throws IloException {

    int n = supplyTypes.size();
    J = new double[n];
    dJ = new double[n][];
    EJ = new double[n];
    dEJ = new double[n][];

    refPolicy = getRefPolicy();

    for (int t = 0; t < n; t++) {
      J[t] = getJ(refPolicy[t], demandTypes.get(t), t);
      EJ[t] = getEJ(refPolicy[t], t);
      dJ[t] = getdJ(refPolicy[t], demandTypes.get(t), t);
      dEJ[t] = getdEJ(refPolicy[t], t);
    }

    loadLP();
    cplex.solve();

    return (cplex.getObjValue() - getC());
  }

  public void clear()
      throws IloException {
    cplex.clearModel();
  }

  abstract protected double getJ(double[] state, Item betaT, int t) throws IloException;

  abstract protected double getEJ(double[] state, int t) throws IloException;

  abstract protected double[] getdJ(double[] state, Item betaT, int t);

  abstract protected double[] getdEJ(double[] state, int t);

  public void setOmega(ArrayList<Item> newOmega) {
    this.demandTypes = newOmega;
  }

  private void loadLP()
      throws IloException {
    int n = supplyTypes.size();
    double[] lb = new double[n];
    double[] ub = new double[n];
    for (int i = 0; i < n; i++) {
      lb[i] = 0.0;
      ub[i] = 1.0;
    }
    piVar = new IloNumVar[n][];
    for (int t = 0; t < n; t++) {
      piVar[t] = cplex.numVarArray(n, lb, ub);
    }

    // adding constraints
    double[] ones = new double[n];
    Arrays.fill(ones, 1.0);

    supplyConst = new IloRange[n];
    for (int t = 0; t < n; t++) {
      supplyConst[t] = cplex.addEq(cplex.scalProd(ones, piVar[t]), 1.0);
    }

    IloLinearNumExpr[] demandConstExpr = new IloLinearNumExpr[n];
    demandConst = new IloRange[n];
    for (int i = 0; i < n; i++) {
      demandConstExpr[i] = cplex.linearNumExpr();
      for (int t = 0; t < n; t++) {
        demandConstExpr[i].addTerm(1.0, piVar[t][i]);
      }
      demandConst[i] = cplex.addEq(demandConstExpr[i], 1.0);
    }

    // adding objective
    IloLinearNumExpr obj = cplex.linearNumExpr();
    for (int i = 0; i < n; i++) {
      Item typeS = supplyTypes.get(i);
      for (int t = 0; t < n; t++) {
        Item typeD = demandTypes.get(t);
        obj.addTerm(model.getRewardFunction().evaluate(typeS, typeD), piVar[t][i]);
      }
    }
    // additional inner loop terms
    double[] coeffArray = new double[n];
    Arrays.fill(coeffArray, 0.0);
    for (int t = n - 2; t > -1; t--) {
      for (int i = 0; i < n; i++) {
        coeffArray[i] += dEJ[t + 1][i] - dJ[t + 1][i];
      }
      obj.addTerms(coeffArray, piVar[t]);
    }
    cplex.addMaximize(obj);
  }

  // by default the reference policy is the greedy policy
  protected double[][] getRefPolicy() {
    int n = supplyTypes.size();
    double[][] refPolicy = new double[n][];
    for (int i = 0; i < n; i++) {
      refPolicy[i] = new double[n];
      for (int j = 0; j < n; j++) {
        refPolicy[i][j] = 0.0;
      }
    }
    for (int t = 0; t < (n - 1); t++) {
      Item thisDemand = demandTypes.get(t);
      double max = -Double.MAX_VALUE;
      int maxInd = -1;
      RewardFunction rewardFun = model.getRewardFunction();
      for (int i = 0; i < n; i++) {
        if (refPolicy[t][i] < 0.5 && (rewardFun.evaluate(supplyTypes.get(i), thisDemand) > max)) {
          max = rewardFun.evaluate(supplyTypes.get(i), thisDemand);
          maxInd = i;
        }
      }
      for (int i = 0; i < n; i++) {
        if (i == maxInd) {
          refPolicy[t + 1][i] = 1;
        } else {
          refPolicy[t + 1][i] = refPolicy[t][i];
        }
      }
    }
    return refPolicy;
  }

  double getC() {
    double ret = 0.0;
    int n = supplyTypes.size();
    for (int t = 0; t < n; t++) {
      ret += J[t] - EJ[t];
      for (int i = 0; i < n; i++) {
        ret += refPolicy[t][i] * (dEJ[t][i] - dJ[t][i]);
      }
    }
    return ret;
  }
}
