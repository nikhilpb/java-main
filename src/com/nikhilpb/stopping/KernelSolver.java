package com.nikhilpb.stopping;


import com.nikhilpb.adp.*;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/21/13
 * Time: 2:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class KernelSolver implements Solver {
    private final StoppingModel model;
    private final double gamma;
    private final StateKernel kernel;
    private ArrayList<ArrayList<StoppingState>> sampleStates;
    private ArrayList<Lambda> lambdas;
    private final int timePeriods;
    private final int sampleCount;

    public KernelSolver(StoppingModel model,
                        double gamma,
                        double bandWidth,
                        int sampleCount,
                        long sampleSeed) {
        this.model = model;
        this.gamma = gamma;
        this.kernel = new GaussianStateKernel(bandWidth);
        timePeriods = model.getTimePeriods();
        lambdas = new ArrayList<Lambda>();
        this.sampleCount = sampleCount;

        Policy policy = new Policy() {
            @Override
            public Action getAction(State state) { return StoppingAction.CONTINUE; }
        };

        System.out.println("sampling " + sampleCount + " sample paths");
        MonteCarloEval sampler = new MonteCarloEval(model, policy, model.getRewardFunction(), sampleSeed);
        ArrayList<SamplePath> samplePaths = sampler.getSamplePaths(sampleCount, timePeriods);
        sampleStates = new ArrayList<ArrayList<StoppingState>>();
        for (int t = 0; t < timePeriods; ++t) {
            sampleStates.add(new ArrayList<StoppingState>());
        }

        System.out.println("aggregating states");
        for (SamplePath sp : samplePaths) {
            for (int t = 0; t < sampleCount; ++t) {
                sampleStates.get(t).add((StoppingState)sp.stateActions.get(t).getState());
            }
        }

        System.out.println("initializing the dual variables");
        for (int t = 0; t < timePeriods; ++t) {
            lambdas.add(new Lambda(sampleStates.get(t)));
        }

        System.out.println("initializing the gradient");
        initializeGrad();
    }

    public double getGamma() {
        return gamma;
    }

    public StateKernel getKernel() {
        return kernel;
    }

    @Override
    public boolean solve() {
        return true;
    }

    @Override
    public StateFunction getValueEstimate() {
        return null;
    }

    // TODO
    private void initializeGrad() {

    }

    private double[] que0(int i) {
        return null;
    }
}
