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
    private StoppingModel model;
    private final double gamma;
    private ArrayList<ArrayList<State>> sampleStates;
    private double[][] lambdaC, lambdaS;
    private double[][] gradC, gradS;

    public KernelSolver(StoppingModel model,
                        double gamma,
                        int sampleCount,
                        long sampleSeed) {
        this.model = model;
        this.gamma = gamma;

        Policy policy = new Policy() {
            @Override
            public Action getAction(State state) { return StoppingAction.CONTINUE; }
        };

        System.out.println("sampling " + sampleCount + " sample paths");
        MonteCarloEval sampler = new MonteCarloEval(model, policy, model.getRewardFunction(), sampleSeed);
        ArrayList<SamplePath> samplePaths = sampler.getSamplePaths(sampleCount, model.getTimePeriods());
        sampleStates = new ArrayList<ArrayList<State>>();
        lambdaC = new double[model.getTimePeriods()][];
        lambdaS = new double[model.getTimePeriods()][];
        gradC = new double[model.getTimePeriods()][];
        gradS = new double[model.getTimePeriods()][];
        for (int i = 0; i < model.getTimePeriods(); ++i) {
            sampleStates.add(new ArrayList<State>());
        }

        System.out.println("aggregating states");
        for (SamplePath sp : samplePaths) {
            for (int t = 0; t < sp.stateActions.size(); ++t) {
                sampleStates.get(t).add(sp.stateActions.get(t).getState());
            }
        }

        System.out.println("initializing the dual variables");
        for (int t = 0; t < model.getTimePeriods(); ++t) {
            int lmdSize = sampleStates.get(t).size();
            lambdaC[t] = new double[lmdSize];
            Arrays.fill(lambdaC[t], 1./sampleStates.size());
            lambdaS[t] = new double[lmdSize];
            Arrays.fill(lambdaS, 0.);

            gradC[t] = new double[lmdSize];
            gradS[t] = new double[lmdSize];
        }

        System.out.println("initializing the gradient");
        initializeGrad();
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
        // Gamma * \sum \lambda_{x,s} g_t(x) term
        for (int i = 0; i < gradS.length; ++i) {
            for (int j = 0; j < gradS[i].length; ++j) {
                gradS[i][j] = gamma * model.getRewardFunction().value(
                        sampleStates.get(i).get(j),
                        StoppingAction.STOP);
            }
        }

        // Q0 \lambda_0 + R_0 term
        for (int i = 0; i < gradS[0].length; ++i) {

        }
    }

    private double[] que0(int i) {

    }
}
