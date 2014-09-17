package com.nikhilpb.abtesting;

import com.nikhilpb.util.Pair;
import com.nikhilpb.util.math.Distributions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by nikhilpb on 9/16/14.
 */
public class ContinuationValueCollection {
  private int timePeriods;
  private double lambdaMax;
  private int discretePointsCount;
  private ArrayList<HashMap<Integer, OneDFunction>> functions;
  private ArrayList<Pair<Double, Double>> sampledPoints;

  public ContinuationValueCollection(int dimension,
                                     int timePeriods,
                                     double lambdaMax,
                                     int discretePointsCount,
                                     int simPointsCount,
                                     long seed) {
    this.timePeriods = timePeriods;
    this.lambdaMax = lambdaMax;
    this.discretePointsCount = discretePointsCount;

    functions = new ArrayList<HashMap<Integer, OneDFunction>>();
    for (int t = 0; t <= timePeriods; ++t) {
      functions.add(new HashMap<Integer, OneDFunction>());
    }

    System.out.println("Sampling points");
    sampledPoints = new ArrayList<Pair<Double, Double>>();
    Random random = new Random(seed);
    for (int i = 0; i < simPointsCount; ++i) {
      sampledPoints.add(new Pair<Double, Double>(random.nextGaussian(),
                                                 Distributions.nextChiSquared(random, dimension - 2)));
    }
  }

  public void evaluate() {
    for (int t = timePeriods; t >= 0; --t) {
      System.out.println("Evaluating continuation functions at time " + t);
      if (t == timePeriods) {
        terminalContFunction(functions.get(t), t);
        continue;
      }
      for (int i = -t; i <= t; i = i + 2) {
        OneDFunction plusFun = functions.get(t+1).get(i+1),
                     minusFun = functions.get(t+1).get(i-1);
        System.out.println("m = " + i);
        DiscretizedFunction df = new DiscretizedFunction(lambdaMax, discretePointsCount);
        double[] pts = df.points();
        for (int j = 0; j < pts.length; ++j) {
          df.setValue(j, evalMC(df.getPoint(j), plusFun, minusFun));
        }
        functions.get(t).put(i, df);
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

  private double evalMC(double lambda, OneDFunction plusFun, OneDFunction minusFun) {
    double estimate = 0.;
    for (Pair<Double, Double> p : sampledPoints) {
      double pValue = plusFun.value(Math.pow(Math.sqrt(lambda) + p.getFirst(), 2) + p.getSecond()),
             mValue = minusFun.value(Math.pow(Math.sqrt(lambda) - p.getFirst(), 2) + p.getSecond());
      estimate += Math.min(pValue, mValue);
    }
    return estimate / sampledPoints.size();
  }
}
