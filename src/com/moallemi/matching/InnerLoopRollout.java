package com.moallemi.matching;

import java.util.*;

import ilog.concert.*;

import com.moallemi.math.*;

public class InnerLoopRollout extends InnerLoop {
  private ItemFunction supplyValueFunction;
  private ArrayList<Item> tildeBeta;

  static final int MC_SAMPLES_COUNT = 1000;

  public InnerLoopRollout(CplexFactory factory,
                          ArrayList<Item> supplyTypes,
                          Random random,
                          int innerSampleCount,
                          OnlineMatchingModel model,
                          ItemFunction supplyValueFunction)
      throws IloException {
    this.supplyTypes = supplyTypes;
    this.innerSampleCount = innerSampleCount;
    this.model = model;
    this.supplyValueFunction = supplyValueFunction;
    long tildeSeed = random.nextLong();
    model.setDemRandomSeed(tildeSeed);
    tildeBeta = model.sampleDemandTypes(MC_SAMPLES_COUNT);

    this.cplex = factory.getCplex();
  }

  @Override
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
        double thisValue = rewardFun.evaluate(supplyTypes.get(i), thisDemand)
            - supplyValueFunction.evaluate(supplyTypes.get(i));
        if (refPolicy[t][i] < 0.5 && (thisValue > max)) {
          max = thisValue;
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

  protected double getJ(double[] state, Item betaT, int t) {
    int n = supplyTypes.size();
    double max = -Double.MAX_VALUE;
    for (int i = 0; i < n; i++) {
      if (state[i] < 0.5) {
        double jThis = model.getRewardFunction().evaluate(supplyTypes.get(i), betaT)
            - supplyValueFunction.evaluate(supplyTypes.get(i));
        if (jThis > max) {
          max = jThis;
        }
      }
    }
    return max;
  }

  protected double getEJ(double[] state, int t) {
    double meanJ = 0.0;
    for (int i = 0; i < MC_SAMPLES_COUNT; i++) {
      meanJ = (meanJ * i + getJ(state, tildeBeta.get(i), t)) / (i + 1);
    }
    return meanJ;
  }

  protected double[] getdJ(double[] state, Item betaT, int t) {
    int n = supplyTypes.size();
    double[] thisdJ = new double[n];
    Arrays.fill(thisdJ, 0.0);
    double max = -Double.MAX_VALUE;
    int maxInd = -1;
    double max2 = -Double.MAX_VALUE;
    int maxInd2 = -1;
    for (int i = 0; i < n; i++) {
      if (state[i] < 0.5) {
        double vThis = model.getRewardFunction().evaluate(supplyTypes.get(i), betaT)
            - supplyValueFunction.evaluate(supplyTypes.get(i));
        if (vThis > max) {
          max2 = max;
          maxInd2 = maxInd;
          max = vThis;
          maxInd = i;
        } else if (vThis > max2) {
          max2 = vThis;
          maxInd2 = i;
        }
      }
    }
    if (maxInd2 < 0) {
      max2 = 0.0;
    }
    thisdJ[maxInd] = max2 - max;
    for (int i = 0; i < n; i++) {
      if (state[i] > 0.5) {
        double vThis = model.getRewardFunction().evaluate(supplyTypes.get(i), betaT)
            - supplyValueFunction.evaluate(supplyTypes.get(i));
        if (vThis > max) {
          thisdJ[i] = max - vThis;
        }
      }
    }
    return thisdJ;
  }

  protected double[] getdEJ(double[] state, int t) {
    int n = supplyTypes.size();
    double[] thisdEJ = new double[n];
    Arrays.fill(thisdEJ, 0.0);
    for (int s = 0; s < MC_SAMPLES_COUNT; s++) {
      double[] thisdJ = getdJ(state, tildeBeta.get(s), t);
      for (int i = 0; i < n; i++) {
        thisdEJ[i] = (i * thisdEJ[i] + thisdJ[i]) / (i + 1);
      }
    }
    return thisdEJ;
  }
}
