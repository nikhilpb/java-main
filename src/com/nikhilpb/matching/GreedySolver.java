package com.nikhilpb.matching;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 9/22/13
 * Time: 9:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class GreedySolver extends MatchingSolver {

    public GreedySolver(MatchingModel model,
                      double initPopulationParam,
                      int timePeriods,
                      long sampleSeed,
                      MatchingSolver.SamplingPolicy samplingPolicy) {
        System.out.println();
        System.out.println("creating a greedy solver");
        System.out.println();
        initParams(model, initPopulationParam, timePeriods, sampleSeed, samplingPolicy);
    }

    public boolean solve() {
        return true;
    }

    public ItemFunction getSupplyFunction() {
        return new ConstantItemFunction(0.0);
    }

    public ItemFunction getDemandFunction() {
        return new ConstantItemFunction(0.0);
    }
}
