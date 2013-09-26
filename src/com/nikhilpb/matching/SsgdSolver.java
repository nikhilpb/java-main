package com.nikhilpb.matching;

import com.moallemi.util.data.Pair;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 9/22/13
 * Time: 8:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class SsgdSolver extends MatchingSolver {
    private final double eps, a , b;
    private final int sampleCount, checkPerSteps;
    private double[] kappaSupply, kappaDemand;
    private final ItemFunctionSet  basisSetSupply, basisSetDemand;

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
        this.basisSetSupply = basisSetSupply;
        this.basisSetDemand = basisSetDemand;
        kappaSupply = new double[this.basisSetSupply.size()];
        kappaDemand = new double[this.basisSetDemand.size()];
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
        for (int i = 0; i < sampleCount; ++i) {
            stepSize = a / (b + (double) i);
            System.out.println("sampled instance: " + i
                    + ", step size: " + stepSize);
            samplePath = samplePathMatched(random.nextLong());
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
        LinearCombinationItemFunction supplyFunction =
                (LinearCombinationItemFunction)basisSetSupply.getLinearCombination(kappaSupply);
        supplyFunction.scale(1.0 - model.supplyDepartureRate);
        LinearCombinationItemFunction demandFunction =
                (LinearCombinationItemFunction)basisSetDemand.getLinearCombination(kappaDemand);
        demandFunction.scale(1.0 - model.demandDepartureRate);
        double thisEps;
        for (int t = 0; t < tp; ++t) {
            states = samplePath.getStates(t);
            supItems.clear(); demItems.clear();
            for (Item s : states) {
                if (s.isSod() == 1) {
                    supItems.add(s);
                } else {
                    demItems.add(s);
                }
            }
            matcher = new ItemMatcher(supItems, demItems, model.getRewardFunction(),
                                      supplyFunction, demandFunction, t == tp);
            matcher.solve();
            ArrayList<Pair<Integer, Integer>> pairs = matcher.getMatchedPairInds();
            ArrayList<Pair<Item, Item>> matchedPairs = new ArrayList<Pair<Item, Item>>();
            for (Pair<Integer, Integer> p : pairs) {
                matchedPairs.add(new Pair<Item, Item>(supItems.get(p.getFirst()), demItems.get(p.getSecond())));
            }
            SalpConstraint constraint = new SalpConstraint(model, basisSetSupply, basisSetDemand,
                                                           states, matchedPairs, t == tp);
            if (!constraint.satisfied(kappaSupply, kappaDemand)) {
                thisEps = eps;
            } else {
                thisEps = 0.0;
            }
            double[] coeffKappaS = constraint.getKappa1Coeff();
            double[] coeddKappaD = constraint.getKappa2Coeff();
            for (int i = 0; i < sgSupply.length; ++i) {
                sgSupply[i] += (1.0 - thisEps) * coeffKappaS[i];
            }
            for (int j = 0; j < sgDemand.length; ++j) {
                sgDemand[j] += (1.0 - thisEps) * coeddKappaD[j];
            }
        }
    }

    public ItemFunction getSupplyFunction() {
        return basisSetSupply.getLinearCombination(kappaSupply);
    }

    public ItemFunction getDemandFunction() {
       return basisSetDemand.getLinearCombination(kappaDemand);
    }

    public static class Config {
        public double epsConfig, aConfig, bConfig;
        public int stepCountConfig, checkPerStepsConfig;
    }
}
