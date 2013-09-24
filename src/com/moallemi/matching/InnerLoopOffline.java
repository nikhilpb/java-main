package com.moallemi.matching;

import java.util.*;

import ilog.concert.*;
import ilog.cplex.*;

import com.moallemi.math.*;

public class InnerLoopOffline extends InnerLoop {

  private Random random;
  private ArrayList<ArrayList<Item>> tildeBeta;

  private IloCplex cplexInner;
  private IloNumVar[][] muVar;
  private IloRange[] supplyConst, demandConst;

  private double[] lb, ub;
  private double[] dJStored, dEJStored;

  static final int MC_SAMPLES_COUNT = 1000;

  public InnerLoopOffline(CplexFactory factory,
                          ArrayList<Item> supplyTypes,
                          Random random,
                          int innerSampleCount,
                          OnlineMatchingModel model)
      throws IloException {
    this.supplyTypes = supplyTypes;
    this.random = random;
    this.innerSampleCount = innerSampleCount;
    this.model = model;
    tildeBeta = new ArrayList<ArrayList<Item>>(MC_SAMPLES_COUNT);
    for (int i = 0; i < MC_SAMPLES_COUNT; i++) {
      long tildeSeed = this.random.nextLong();
      model.setDemRandomSeed(tildeSeed);
      tildeBeta.add(model.sampleDemandTypes(MC_SAMPLES_COUNT));
    }
    this.cplex = factory.getCplex();
    this.cplexInner = factory.getCplex();
    cplexInner.setOut(null);
    int n = supplyTypes.size();
    lb = new double[n];
    ub = new double[n];
    Arrays.fill(lb, 0.0);
    Arrays.fill(ub, 1.0);

  }

  protected double getJ(double[] state, Item betaT, int t)
      throws IloException {
    int n = supplyTypes.size();
    double meanJ = 0;
    dJStored = new double[n];
    Arrays.fill(dJStored, 0.0);

    ArrayList<Item> thisTildeBeta;
    for (int s = 0; s < MC_SAMPLES_COUNT; s++) {
      thisTildeBeta = tildeBeta.get(s);

      cplexInner.clearModel();
      muVar = new IloNumVar[n - t][];
      for (int tau = t; tau < n; tau++) {
        muVar[tau - t] = new IloNumVar[n];
      }

      demandConst = new IloRange[n - t];
      for (int tau = t; tau < n; tau++) {
        System.out.println(tau - t);
        demandConst[tau - t] = cplexInner.addLe(cplexInner.scalProd(ub, muVar[tau - t]), 1.0);
      }

      IloLinearNumExpr[] supplyConstExpr = new IloLinearNumExpr[n];
      supplyConst = new IloRange[n];
      for (int i = 0; i < n; i++) {
        supplyConstExpr[i] = cplexInner.linearNumExpr();
        for (int tau = t; tau < n; tau++) {
          supplyConstExpr[i].addTerm(1.0, muVar[tau - t][i]);
        }
        supplyConst[i] = cplex.addLe(supplyConstExpr[i], 1.0 - state[i]);
      }
      IloLinearNumExpr obj = cplex.linearNumExpr();
      for (int i = 0; i < n; i++) {
        Item typeS = supplyTypes.get(i);
        obj.addTerm(model.getRewardFunction().evaluate(typeS, betaT), muVar[0][i]);
        for (int tau = t + 1; tau < n; tau++) {
          Item typeD = thisTildeBeta.get(tau);
          obj.addTerm(model.getRewardFunction().evaluate(typeS, typeD), muVar[tau - t][i]);
        }
      }
      cplexInner.solve();
      double thisJ = cplexInner.getObjValue();
      double[] thisDual = cplexInner.getDuals(supplyConst);
      meanJ = (thisJ + meanJ * s) / (s + 1);
      for (int i = 0; i < n; i++) {
        dJStored[i] = (s * dJStored[i] - thisDual[i]) / (s + 1);
      }
    }
    return meanJ;
  }

  protected double getEJ(double[] state, int t)
      throws IloException {
    int n = supplyTypes.size();
    double meanJ = 0;
    dEJStored = new double[n];
    Arrays.fill(dEJStored, 0.0);

    ArrayList<Item> thisTildeBeta;
    for (int s = 0; s < MC_SAMPLES_COUNT; s++) {
      thisTildeBeta = tildeBeta.get(s);
      cplexInner.clearModel();
      muVar = new IloNumVar[n - t][];
      for (int tau = t; tau < n; tau++) {
        muVar[tau - t] = new IloNumVar[n];
      }

      demandConst = new IloRange[n - t];
      for (int tau = t; tau < n; tau++) {
        demandConst[tau - t] = cplexInner.addLe(cplexInner.scalProd(ub, muVar[tau - t]), 1.0);
      }

      IloLinearNumExpr[] supplyConstExpr = new IloLinearNumExpr[n];
      supplyConst = new IloRange[n];
      for (int i = 0; i < n; i++) {
        supplyConstExpr[i] = cplexInner.linearNumExpr();
        for (int tau = t; tau < n; tau++) {
          supplyConstExpr[i].addTerm(1.0, muVar[tau - t][i]);
        }
        supplyConst[i] = cplex.addLe(supplyConstExpr[i], 1.0 - state[i]);
      }
      IloLinearNumExpr obj = cplex.linearNumExpr();
      for (int i = 0; i < n; i++) {
        Item typeS = supplyTypes.get(i);
        for (int tau = t; tau < n; tau++) {
          Item typeD = thisTildeBeta.get(tau);
          obj.addTerm(model.getRewardFunction().evaluate(typeS, typeD), muVar[tau - t][i]);
        }
      }
      cplexInner.solve();
      double thisJ = cplexInner.getObjValue();
      double[] thisDual = cplexInner.getDuals(supplyConst);
      meanJ = (thisJ + meanJ * s) / (s + 1);
      for (int i = 0; i < n; i++) {
        dEJStored[i] = (s * dEJStored[i] - thisDual[i]) / (s + 1);
      }
    }
    return meanJ;
  }

  protected double[] getdJ(double[] state, Item betaT, int t) {
    return dJStored;
  }

  protected double[] getdEJ(double[] state, int t) {
    return dEJStored;
  }
}
