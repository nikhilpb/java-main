package com.nikhilpb.matching;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 9/22/13
 * Time: 8:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class SsgdSolver extends MatchingSolver {
    private double eps, a , b;
    private int sampleCount, checkPerSteps;
    private double[] kappaSupply, kappaDemand;
    ItemFunctionSet basisSetSupply, basisSetDemand;
    private int stepCount;

    public SsgdSolver(MatchingModel model,
                      ItemFunctionSet basisSetSupply,
                      ItemFunctionSet basisSetDemand,
                      long sampleSeed,
                      MatchingSolver.SamplingPolicy samplingPolicy,
                      int sampleCount,
                      Config config) {
        this.eps = config.epsConfig;
        this.a = config.aConfig;
        this.b = config.bConfig;
        this.checkPerSteps = config.checkPerStepsConfig;
        this.sampleCount = config.stepCountConfig;
        this.sampleCount = sampleCount;
        this.basisSetSupply = basisSetSupply;
        this.basisSetDemand = basisSetDemand;
        kappaSupply = new double[basisSetSupply.size()];
        kappaDemand = new double[basisSetDemand.size()];
        this.stepCount = 10;
        System.out.println();
        System.out.println("SALP with stochastic sub-gradient method");
        System.out.println();
        System.out.printf("solver parameters -\neps: %f\na: %f\nb: %f\n", eps, a, b);
        System.out.println();
        initParams(model, sampleSeed, samplingPolicy);
    }

    public boolean solve() {
        double stepSize;
        double[] sgSupply = new double[kappaSupply.length];
        double[] sgDemand = new double[kappaDemand.length];
        MatchingSamplePath samplePath;
        for (int i = 0; i < stepCount; ++i) {
            stepSize = a / (b + (double) i);
            System.out.println("sampled instance: " + i
                    + ", step size: " + stepSize);
            samplePath = samplePath(random.nextLong());
            findSubgrad(samplePath, sgSupply, sgDemand);
            // Minimize objective, subtract sub-gradient
            for (int j = 0; j < kappaSupply.length; ++j) {
                kappaSupply[j] -= stepSize * sgSupply[j];
            }
            for (int j = 0; j < kappaDemand.length; ++j) {
                kappaDemand[j] -= stepSize * sgDemand[j];
            }
        }
        return true;
    }

    protected void findSubgrad(MatchingSamplePath samplePath,
                               double[] sgSupply,
                               double[] sgDemand) {
        for (int i = 0; i < sgSupply.length; ++i) {
            sgSupply[i] = 0.0;
        }
        for (int i = 0; i < sgDemand.length; ++i) {
            sgDemand[i] = 0.0;
        }
        int tp = samplePath.getTimePeriods();
        ItemMatcher matcher;
        ArrayList<Item> states, supItems = new ArrayList<Item>(), demItems = new ArrayList<Item>();
        for (int t = 0; t < tp; ++t) {
            states = samplePath.getStates(t);
            supItems.clear(); demItems.clear();
            for (int i = 0; i < states.size(); ++i) {
                if (states.get(i).isSod() == 1) {
                    supItems.add(states.get(i));
                } else {
                    demItems.add(states.get(i));
                }
            }
            matcher = new ItemMatcher(supItems, demItems, model.getRewardFunction());
            matcher.solve();

        }
    }

    public ItemFunction getSupplyFunction() {
        return new ConstantItemFunction(0.0);
    }

    public ItemFunction getDemandFunction() {
       return new ConstantItemFunction(0.0);
    }

    public static class Config {
        public double epsConfig, aConfig, bConfig;
        public int stepCountConfig, checkPerStepsConfig;
    }
}
