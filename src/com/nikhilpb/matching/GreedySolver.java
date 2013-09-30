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
                      long sampleSeed,
                      MatchingSolver.SamplingPolicy samplingPolicy) {
        System.out.println();
        System.out.println("creating a greedy solver");
        System.out.println();
        initParams(model, sampleSeed, samplingPolicy);
    }

    public boolean solve() {
        return true;
    }

    public ItemFunction getSupplyFunction() {
        return new ConstantItemFunction(10000.0);
    }

    public ItemFunction getDemandFunction() {
        return new ConstantItemFunction(10000.0);
    }
}
