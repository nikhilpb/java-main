package com.nikhilpb.pools;

import com.nikhilpb.util.CplexFactory;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/10/13
 * Time: 9:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class SsgdSolver {
  private final double eps, a, b;
  private final int sampleCount, checkPerSteps, simSteps;
  private final long simSeed;
  private double[] kappa;
  private final NodeFunctionSet basisSet;
  private final MatchingPoolsModel model;
  private Random random;

  public SsgdSolver(MatchingPoolsModel model,
                    NodeFunctionSet basisSet,
                    long sampleSeed,
                    Config config) {
    this.eps = config.epsConfig;
    this.a = config.aConfig;
    this.b = config.bConfig;
    this.checkPerSteps = config.checkPerStepsConfig;
    this.sampleCount = config.stepCountConfig;
    this.simSteps = config.simSteps;
    this.simSeed = config.simSeed;
    this.basisSet = basisSet;
    kappa = new double[this.basisSet.size()];
    Arrays.fill(kappa, 0.0);
    System.out.println();
    System.out.println("SALP with stochastic sub-gradient method");
    System.out.println();
    System.out.printf("solver parameters -\neps: %f\na: %f\nb: %f\n", eps, a, b);
    System.out.printf("sim steps: %d\nsim seed: %d", simSteps, simSeed);
    System.out.println();
    this.model = model;
    this.random = new Random(sampleSeed);
    System.out.printf("sampling parameters -\nseed: %d\n\n", sampleSeed);
  }

  public boolean solve() {
    double stepSize;
    double[] sg = new double[kappa.length];
    SampleInstance samplePath;
    double maxValue = 0.0;
    try {
      Evaluator evaluator = new Evaluator(model, simSteps, simSeed);
      for (int i = 0; i < sampleCount; ++ i) {
        stepSize = a / (b + (double) i);
        if (i % checkPerSteps == 0) {
          evaluator.setValueFunction(basisSet.getLinearCombination(kappa));
          double value = evaluator.evaluate();
          if (value >= maxValue) {
            maxValue = value;
          }
          System.out.println("sampled instance: " + i
                                     + ", step size: " + stepSize + ", value: "
                                     + value + ", max value: " + maxValue);
        }
        samplePath = samplePathMatched(random.nextLong());
        findSubgrad(samplePath, sg);
        // Minimize objective, subtract sub-gradient
        for (int j = 0; j < kappa.length; ++ j) {
          kappa[j] -= stepSize * sg[j];
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    return true;
  }

  protected void findSubgrad(SampleInstance samplePath,
                             double[] sg) {
    Arrays.fill(sg, 0.0);
    int tp = samplePath.getTimePeriods();
    ArrayList<Node> state;
    NodeRewardFunction nrf = new NodeRewardFunction(model.getNodeRewardFunction().getRf(),
                                                           basisSet.getLinearCombination(kappa),
                                                           1.0 - model.getDepartureRate());
    try {
      IloCplex cplex = new IloCplex();
      cplex.setOut(null); // no printing to stdout
      for (int t = 0; t <= tp; ++ t) {
        state = samplePath.getState(t);

        KidneyPoolsMatcher matcher = new KidneyPoolsMatcher(new CplexFactory(), state, nrf);
        matcher.solve();
        ArrayList<Node> matchedNodes = matcher.findMatchedNodes();
        SalpConstraint constraint = new SalpConstraint(state, matchedNodes, t == tp, model, basisSet);
        double[] coeffKappa;
        double rhs = matcher.value(model.getNodeRewardFunction());
        if (! constraint.satisfied(kappa, rhs)) {
          coeffKappa = constraint.getCoeff();
          for (int i = 0; i < sg.length; ++ i) {
            sg[i] -= (1. + eps) * coeffKappa[i];
          }
        }
        constraint = new SalpConstraint(state, samplePath.getMatches(t), (t == tp), model, basisSet);
        coeffKappa = constraint.getCoeff();
        for (int i = 0; i < sg.length; ++ i) {
          sg[i] += coeffKappa[i];
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected SampleInstance samplePathMatched(long seed) throws Exception {
    SampleInstance samplePath = new SampleInstance(model, seed);
    samplePath.sample();
    samplePath.match("greedy", new CplexFactory());
    return samplePath;
  }


  public static class Config {
    public double epsConfig, aConfig, bConfig;
    public int stepCountConfig, checkPerStepsConfig, simSteps;
    public long simSeed;
  }
}
