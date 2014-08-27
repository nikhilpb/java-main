package com.nikhilpb.adp;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/16/13
 * Time: 2:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class MonteCarloEval {
  private MarkovDecisionProcess mdp;
  private Policy policy;
  private RewardFunction rewardFunction;
  private Random random;

  public MonteCarloEval(MarkovDecisionProcess mdp, Policy policy, long seed) {
    this.mdp = mdp;
    this.policy = policy;
    this.rewardFunction = mdp.getRewardFunction();
    this.random = new Random(seed);
  }

  public SamplePath samplePath(long seed, int timePeriods) {
    mdp.reset(seed);
    SamplePath samplePath = new SamplePath();
    State curState = null;
    Action curAction = null;
    int time = 0;
    double alpha = mdp.getAlpha();
    do {
      if (time == 0) {
        curState = mdp.getBaseState();
      } else {
        StateDistribution distribution = mdp.getDistribution(curState, curAction);
        if (distribution == null) {
          break;
        }
        curState = distribution.nextSample();
      }
      curAction = policy.getAction(curState);
      samplePath.stateActions.add(new StateAction(curState, curAction));
      samplePath.reward += Math.pow(alpha, time)
                                   * rewardFunction.value(curState, curAction);
      time += 1;
    } while (time < timePeriods);
    return samplePath;
  }

  public ArrayList<SamplePath> getSamplePaths(int pathsCount, int timePeriods) {
    ArrayList<SamplePath> samplePaths = new ArrayList<SamplePath>();
    for (int i = 0; i < pathsCount; ++ i) {
      samplePaths.add(samplePath(random.nextLong(), timePeriods));
    }
    return samplePaths;
  }

  public MonteCarloResults eval(int pathsCount, int timePeriods) {
    ArrayList<SamplePath> samplePaths = getSamplePaths(pathsCount, timePeriods);
    double mean = 0., var = 0.;
    int totalStepCount = 0;
    for (SamplePath sp : samplePaths) {
      mean += sp.reward;
      var += sp.reward * sp.reward;
      totalStepCount += sp.stateActions.size();
    }
    mean = mean / pathsCount;
    var = var / pathsCount;
    var = var - (mean * mean);
    return new MonteCarloResults(mean, Math.sqrt(var), pathsCount, totalStepCount);
  }

  public static class MonteCarloResults {
    private double mcMean, mcStd, mcStdErr, meanStepCount;

    public MonteCarloResults(double mcMean, double mcStd, int sampleCount, int totalStepCount) {
      this.mcMean = mcMean;
      this.mcStd = mcStd;
      this.mcStdErr = mcStd / Math.sqrt((double) sampleCount);
      this.meanStepCount = totalStepCount / ((double) sampleCount);
    }

    public double getMean() {
      return mcMean;
    }

    public double getStd() {
      return mcStd;
    }

    public double getStdErr() {
      return mcStdErr;
    }

    @Override
    public String toString() {
      return "mean: " + mcMean + ",\n" +
                     "standard deviation: " + mcStd + ",\n" +
                     "standard error: " + mcStdErr + ",\n" +
                     "mean step count: " + meanStepCount + "\n";
    }
  }

}
