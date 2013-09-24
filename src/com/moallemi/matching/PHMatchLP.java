package com.moallemi.matching;

import java.util.*;

import ilog.concert.*;
import ilog.cplex.*;

import com.moallemi.math.*;
import com.moallemi.util.data.*;

public class PHMatchLP {
  private ArrayList<Item> supplySide, demandSide;
  private RewardFunction rewardFun;

  IloCplex cplex;
  IloNumVar[][] piVar;

  IloRange[] supplyConst, demandConst;

  public PHMatchLP(CplexFactory factory,
                   ArrayList<Item> supplySide,
                   ArrayList<Item> demandSide,
                   RewardFunction rewardFun)
      throws IloException {
    if (supplySide.size() != demandSide.size()) {
      throw new RuntimeException("supply and demand sizes don't match");
    }
    //initialize
    this.supplySide = supplySide;
    this.demandSide = demandSide;
    this.rewardFun = rewardFun;
    cplex = factory.getCplex();

    int n = supplySide.size();
    double[] lb = new double[n];
    double[] ub = new double[n];
    for (int i = 0; i < n; i++) {
      lb[i] = 0.0;
      ub[i] = 1.0;
    }
    piVar = new IloNumVar[n][];
    for (int s = 0; s < n; s++) {
      piVar[s] = cplex.numVarArray(n, lb, ub);
    }

    IloLinearNumExpr obj = cplex.linearNumExpr();
    for (int s = 0; s < n; s++) {
      Item typeS = supplySide.get(s);
      for (int d = 0; d < n; d++) {
        Item typeD = demandSide.get(d);
        obj.addTerm(this.rewardFun.evaluate(typeS, typeD), piVar[s][d]);
      }
    }

    cplex.addMaximize(obj);

    double[] ones = new double[n];
    Arrays.fill(ones, 1.0);

    supplyConst = new IloRange[n];
    for (int s = 0; s < n; s++) {
      supplyConst[s] = cplex.addEq(cplex.scalProd(ones, piVar[s]), 1.0);
    }

    IloLinearNumExpr[] demandConstExpr = new IloLinearNumExpr[n];
    demandConst = new IloRange[n];
    for (int d = 0; d < n; d++) {
      demandConstExpr[d] = cplex.linearNumExpr();
      for (int s = 0; s < n; s++) {
        demandConstExpr[d].addTerm(1.0, piVar[s][d]);
      }
      demandConst[d] = cplex.addEq(demandConstExpr[d], 1.0);
    }
  }

  public boolean solve() throws IloException {
    boolean status = cplex.solve();
    System.out.println("objective = " + cplex.getObjValue());
    return status;
  }

  public ArrayList<Pair<Integer, Integer>> getMatchedPairInds() throws IloException {
    double thresh = 0.2;
    Pair<Integer, Integer> pair;
    ArrayList<Pair<Integer, Integer>> out = new ArrayList<Pair<Integer, Integer>>();
    for (int s = 0; s < supplySide.size(); s++) {
      for (int d = 0; d < demandSide.size(); d++) {
        double curPi = cplex.getValue(piVar[s][d]);
        if (curPi > thresh) {
          pair = new Pair<Integer, Integer>(((Integer) s), ((Integer) d));
          out.add(pair);
        }
      }
    }
    System.out.println("set of matched pairs is of the size" + out.size());
    return out;
  }

  public ArrayList<Pair<Item, Item>> getMatchedPairs() throws IloException {
    double thresh = 0.2;
    Pair<Item, Item> pair;
    ArrayList<Pair<Item, Item>> out = new ArrayList<Pair<Item, Item>>();
    for (int s = 0; s < supplySide.size(); s++) {
      for (int d = 0; d < demandSide.size(); d++) {
        double curPi = cplex.getValue(piVar[s][d]);
        if (curPi > thresh) {
          pair = new Pair<Item, Item>(supplySide.get(s), demandSide.get(d));
          out.add(pair);
        }
      }
    }
    System.out.println("set of matched pairs is of the size: " + out.size());
    return out;
  }

}
