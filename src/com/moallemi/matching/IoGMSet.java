package com.moallemi.matching;

import java.util.*;

import com.moallemi.math.CplexFactory;

import ilog.concert.*;

public class IoGMSet {
  private int timePeriods;
  private double ipParam;
  private long seed;
  private ArrayList<InstanceOfGeneralMatching> instances;
  private GeneralMatchingModel model;
  private Random random;

  public IoGMSet(GeneralMatchingModel model,
                 int timePeriods,
                 double ipParam,
                 long seed) {
    this.timePeriods = timePeriods;
    this.ipParam = ipParam;
    this.seed = seed;
    this.model = model;
    random = new Random(this.seed);
  }

  public void sample(int sampleCount) {
    instances = new ArrayList<InstanceOfGeneralMatching>(sampleCount);
    InstanceOfGeneralMatching thisInstance;
    for (int i = 0; i < sampleCount; i++) {
      thisInstance = new InstanceOfGeneralMatching(model,
          timePeriods,
          ipParam,
          random.nextLong());
      thisInstance.sample();
      instances.add(thisInstance);
    }
  }

  public void matchAll() {
    double oValue = 0.0;
    for (int i = 0; i < instances.size(); i++) {
      System.out.println("matching instance " + i);
      oValue += instances.get(i).match();
    }
    System.out.println("offline value is: " + (oValue / instances.size()));
  }

  public void sampleAndMatch(int sampleCount) {
    this.sample(sampleCount);
    this.matchAll();
  }

  public InstanceOfGeneralMatching get(int i) {
    if (i >= instances.size() || i < -1) {
      System.out.println("incorrect index");
      return null;
    } else {
      return instances.get(i);
    }
  }

  public int size() {
    return instances.size();
  }

  public double[] dualPolicyTest(ItemFunction sf,
                                 ItemFunction df,
                                 CplexFactory factory)
      throws IloException {
    double[] value = new double[instances.size()];
    for (int i = 0; i < instances.size(); i++) {
      value[i] = instances.get(i).dualPolicyTest(sf, df, factory);
    }
    return value;
  }

}