package com.nikhilpb.matching;

import com.moallemi.math.CplexFactory;
import ilog.concert.IloException;

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
    protected double initPopulationParam;
    protected int timePeriods;
    protected SamplingPolicy samplingPolicy;
    Random random;

    protected void initParams(MatchingModel model,
                              double initPopulationParam,
                              int timePeriods,
                              long sampleSeed,
                              SamplingPolicy samplingPolicy) {
        this.model = model;
        this.initPopulationParam = initPopulationParam;
        this.timePeriods = timePeriods;
        this.random = new Random(sampleSeed);
        this.samplingPolicy = samplingPolicy;
        System.out.printf("sampling parameters -\npolicy: %s\ninit pop param: %f\ntime periods: %d\nseed: %d\n\n",
                          stringFromSamplingPolicy(samplingPolicy),initPopulationParam, timePeriods, sampleSeed);
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
        OFFLINE, GREEDY
    }

    protected MatchingSamplePath samplePath() {
        return new MatchingSamplePath(model, timePeriods, initPopulationParam, random.nextLong());
    }

    protected MatchingSamplePath samplePath(long seed) {
        return new MatchingSamplePath(model, timePeriods, initPopulationParam, seed);
    }

    public double evaluate(long sampleSeed) throws Exception {
        MatchingSamplePath samplePath = samplePath(sampleSeed);
        samplePath.sample();
        return samplePath.dualPolicyEvaluate(getSupplyFunction(), getDemandFunction());
    }

    public static SamplingPolicy samplingPolicyFromString(String typeName) throws RuntimeException {
        if (typeName.equals("offline")) {
            return SamplingPolicy.OFFLINE;
        } else if (typeName.equals("greedy")) {
            return SamplingPolicy.GREEDY;
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
        }
        return "";
    }
}
