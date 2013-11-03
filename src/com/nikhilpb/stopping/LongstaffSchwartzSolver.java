package com.nikhilpb.stopping;

import com.nikhilpb.adp.*;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 11/3/13
 * Time: 2:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class LongstaffSchwartzSolver {
    private StoppingModel model;
    private BasisSet basisSet;
    private int timePeriods;
    private ArrayList<ArrayList<StoppingState>> sampleStates;

    public LongstaffSchwartzSolver(StoppingModel model,
                                   BasisSet basisSet,
                                   long seed,
                                   int sampleCount) {
        this.model = model;
        this.basisSet = basisSet;

        Policy policy = new Policy() {
            @Override
            public Action getAction(State state) { return StoppingAction.CONTINUE; }
        };
        timePeriods = model.getTimePeriods();
        System.out.println("sampling " + sampleCount + " sample paths");
        MonteCarloEval sampler = new MonteCarloEval(model, policy, model.getRewardFunction(), seed);
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
    }

    public boolean solve() {
        return true;
    }

    public QFunction getQFunction() {
        return null;
    }
}
