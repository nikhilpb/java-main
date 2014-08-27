package com.nikhilpb.pools;

import com.nikhilpb.util.CplexFactory;
import ilog.cplex.IloCplex;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/10/13
 * Time: 10:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class Evaluator {
  private MatchingPoolsModel model;
  private NodeFunction valueFunction;
  private int runCount;
  private long simValueSeed;

  public Evaluator(MatchingPoolsModel model, int runCount, long simValueSeed) {
    this.model = model;
    this.runCount = runCount;
    this.simValueSeed = simValueSeed;
  }

  public void setValueFunction(NodeFunction vf) {
    this.valueFunction = vf;
  }

  public double evaluate() throws Exception {
    NodeRewardFunction nrf = new NodeRewardFunction(model.getNodeRewardFunction().getRf(),
                                                           valueFunction,
                                                           1 - model.getDepartureRate());
    Random svRandom = new Random(simValueSeed);
    double[] value = new double[runCount];
    SampleInstance inst;
    CplexFactory factory = new CplexFactory();
    IloCplex cplex = factory.getCplex();
    double mean = 0.0, std = 0.0, sterr = 0.0;
    for (int s = 0; s < runCount; s++) {
      inst = new SampleInstance(model, svRandom.nextLong());
      inst.sample();
      value[s] = inst.greedyMatch(factory, nrf);
      mean += value[s];
    }
    mean = mean / runCount;
    for (int s = 0; s < runCount; s++) {
      std += Math.pow((value[s] - mean), 2);
    }
    std = Math.pow(std / runCount, 0.5);
    sterr = std / Math.pow(runCount, 0.5);
    return mean;
  }
}
