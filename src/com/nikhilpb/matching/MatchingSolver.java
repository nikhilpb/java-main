package com.nikhilpb.matching;

import com.nikhilpb.util.CplexFactory;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 9/22/13
 * Time: 7:53 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class MatchingSolver {

  protected MatchingModel model;
  protected SamplingPolicy samplingPolicy;
  Random random;

  protected void initParams(MatchingModel model,
                            long sampleSeed,
                            SamplingPolicy samplingPolicy) {
    this.model = model;
    this.random = new Random(sampleSeed);
    this.samplingPolicy = samplingPolicy;
    System.out.printf("sampling parameters -\npolicy: %s\nseed: %d\n\n",
                             stringFromSamplingPolicy(samplingPolicy), sampleSeed);
  }

  public abstract boolean solve();

  public abstract ItemFunction getSupplyFunction();

  public abstract ItemFunction getDemandFunction();

  public enum SolverType {
    SALP_SSGD, SALP_BATCHLP, GREEDY
  }

  public static SolverType solverTypeFromString(String solverType) throws RuntimeException {
    if (solverType.equals("ssgd")) {
      return SolverType.SALP_SSGD;
    } else if (solverType.equals("batch_lp")) {
      return SolverType.SALP_BATCHLP;
    } else if (solverType.equals("greedy")) {
      return SolverType.GREEDY;
    } else {
      throw new RuntimeException("Unknown solver type");
    }
  }

  public enum SamplingPolicy {
    OFFLINE, GREEDY, OPTIMISTIC
  }

  protected MatchingSamplePath samplePath() {
    return new MatchingSamplePath(model, random.nextLong());
  }

  protected MatchingSamplePath samplePath(long seed) {
    return new MatchingSamplePath(model, seed);
  }

  protected MatchingSamplePath samplePathMatched(long seed) throws Exception {
    MatchingSamplePath samplePath = new MatchingSamplePath(model, seed);
    samplePath.sample();
    switch (samplingPolicy) {
      case OFFLINE:
        samplePath.offlineMatch();
        break;
      case GREEDY:
        samplePath.dualPolicyMatch(new ConstantItemFunction(0.), new ConstantItemFunction(0.));
        break;
      case OPTIMISTIC:
        samplePath.dualPolicyMatch(getSupplyFunction(), getDemandFunction());
    }
    return samplePath;
  }

  public double evaluate(long sampleSeed) throws Exception {
    MatchingSamplePath samplePath = samplePath(sampleSeed);
    samplePath.sample();
    double reward = samplePath.dualPolicyEvaluate(getSupplyFunction(), getDemandFunction(), new CplexFactory());
    return reward;
  }

  public static SamplingPolicy samplingPolicyFromString(String typeName) throws RuntimeException {
    if (typeName.equals("offline")) {
      return SamplingPolicy.OFFLINE;
    } else if (typeName.equals("greedy")) {
      return SamplingPolicy.GREEDY;
    } else if (typeName.equals("optimistic")) {
      return SamplingPolicy.OPTIMISTIC;
    } else {
      throw new RuntimeException("Unknown match type");
    }
  }

  public static String stringFromSamplingPolicy(SamplingPolicy samplingPolicy) {
    switch (samplingPolicy) {
      case OFFLINE:
        return "offline";
      case GREEDY:
        return "greedy";
      case OPTIMISTIC:
        return "optimistic";
    }
    return "";
  }
}
