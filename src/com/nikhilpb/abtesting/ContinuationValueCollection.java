package com.nikhilpb.abtesting;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by nikhilpb on 9/16/14.
 */
public class ContinuationValueCollection {
  private int dimension;
  private int timePeriods;
  private double lambdaMax;
  private int discretePointsCount;
  private int simPointsCount;
  private ArrayList<HashMap<Integer, OneDFunction>> functions;

  public ContinuationValueCollection(int dimension,
                                     int timePeriods,
                                     double lambdaMax,
                                     int discretePointsCount,
                                     int simPointsCount) {
    this.dimension = dimension;
    this.timePeriods = timePeriods;
    this.lambdaMax = lambdaMax;
    this.discretePointsCount = discretePointsCount;
    this.simPointsCount = simPointsCount;

    functions = new ArrayList<HashMap<Integer, OneDFunction>>();
    for (int t = 0; t <= timePeriods; ++t) {
      functions.add(new HashMap<Integer, OneDFunction>());
    }
  }

  public void evaluate() {
    for (int t = timePeriods; t >= 0; --t) {
      System.out.println("Evaluating continuation functions at time " + t);
      if (t == timePeriods) {
        terminalContFunction(functions.get(t), t);
        continue;
      }
    }
  }

  private void terminalContFunction(HashMap<Integer, OneDFunction> terminalFun, final int timePeriods) {
    terminalFun.clear();
    for (int i = -timePeriods; i <= timePeriods; i = i + 2) {
      terminalFun.put(i, new IdenPlusConst(i*i));
    }
  }

  public double value(final int tp, final int m, final double lambda) {
    if (tp < 0 || tp > timePeriods) {
      throw new RuntimeException("Time period out of bound");
    }
    HashMap<Integer, OneDFunction> thisFun = functions.get(tp);
    if (!thisFun.containsKey(m)) {
      throw new RuntimeException("No integer index " + m);
    }
    return thisFun.get(m).value(lambda);
  }
}
