package com.nikhilpb.matching;

import com.moallemi.util.data.Pair;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.Arrays;

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
                      Config config) {
        this.eps = config.epsConfig;
        this.a = config.aConfig;
        this.b = config.bConfig;
        this.checkPerSteps = config.checkPerStepsConfig;
        this.sampleCount = config.stepCountConfig;
        this.basisSetSupply = basisSetSupply;
        this.basisSetDemand = basisSetDemand;
        kappaSupply = new double[this.basisSetSupply.size()];
        Arrays.fill(kappaSupply, 0.0);
        kappaDemand = new double[this.basisSetDemand.size()];
        Arrays.fill(kappaDemand, 0.0);
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
            if (i % checkPerSteps == 0) {
                System.out.println("sampled instance: " + i
                        + ", step size: " + stepSize);
            }
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
        Arrays.fill(sgSupply, 0.0);
        Arrays.fill(sgDemand, 0.0);
        int tp = samplePath.getTimePeriods();
        ArrayList<Item> states, supItems = new ArrayList<Item>(), demItems = new ArrayList<Item>();
        ItemFunction supplyFunction = basisSetSupply.getLinearCombination(kappaSupply);
        ItemFunction demandFunction = basisSetDemand.getLinearCombination(kappaDemand);
        double thisEps;
        double qs = model.getSupplyDepartureRate(), qd = model.getDemandDepartureRate();
        try {
            IloCplex cplex = new IloCplex();
            cplex.setOut(null);
            for (int t = 0; t <= tp; ++t) {
                states = samplePath.getStates(t);
                supItems.clear(); demItems.clear();
                for (Item s : states) {
                    if (s.isSod() == 1) {
                        supItems.add(s);
                    } else {
                        demItems.add(s);
                    }
                }
                int supplySize = supItems.size();
                int demandSize = demItems.size();
                double[][] w = new double[supplySize][demandSize];
                for (int i = 0; i < supplySize; ++i) {
                    for (int j = 0; j < demandSize; ++j) {
                        w[i][j] = model.getRewardFunction().evaluate(supItems.get(i), demItems.get(j));
                        if ( t < tp) {
                            w[i][j] = w[i][j] - (1. - qs)*supplyFunction.evaluate(supItems.get(i))
                                              - (1. - qd)*demandFunction.evaluate(demItems.get(j));
                        }
                    }
                }
                AsymmetricMatcher matcher = new AsymmetricMatcher(w, cplex);
                if (!matcher.solve()) {
                    throw new RuntimeException("problem solving asymmetric matcher");
                }
                ArrayList<Pair<Integer, Integer>> pairs = matcher.getMatchedPairs();
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
        } catch (Exception e) {
            e.printStackTrace();
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
