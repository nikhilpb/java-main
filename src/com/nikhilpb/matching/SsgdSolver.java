package com.nikhilpb.matching;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 9/22/13
 * Time: 8:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class SsgdSolver extends MatchingSolver {
    private double eps, a , b;
    private int sampleCount;

    public SsgdSolver(MatchingModel model,
                      long sampleSeed,
                      MatchingSolver.SamplingPolicy samplingPolicy,
                      int sampleCount,
                      double eps, double a, double b) {
        this.eps = eps;
        this.a = a;
        this.b = b;
        this.sampleCount = sampleCount;
        System.out.println();
        System.out.println("SALP with stochastic sub-gradient method");
        System.out.println();
        System.out.printf("solver parameters -\neps: %f\na: %f\nb: %f\n", eps, a, b);
        System.out.println();
        initParams(model, sampleSeed, samplingPolicy);
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
