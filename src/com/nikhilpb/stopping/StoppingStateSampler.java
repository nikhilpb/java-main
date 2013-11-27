package com.nikhilpb.stopping;

import com.nikhilpb.adp.*;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 11/11/13
 * Time: 11:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class StoppingStateSampler {
    private StoppingModel model;
    private Policy policy;
    private ArrayList<SamplePath> samplePaths;
    private ArrayList<ArrayList<StoppingState>> states;
    private int timePeriods;

    public StoppingStateSampler(StoppingModel model) {
        this.model = model;
        policy =  new Policy() {
            @Override
            public Action getAction(State state) { return StoppingAction.CONTINUE; }
        };
        timePeriods = model.getTimePeriods();
    }

    public void sample(int sampleCount, long seed) {
        MonteCarloEval sampler = new MonteCarloEval(model, policy, seed);
        samplePaths = sampler.getSamplePaths(sampleCount, timePeriods);
        System.out.println(samplePaths.get(0).toString());
        states = new ArrayList<ArrayList<StoppingState>>();
        for (int t = 0; t < timePeriods; ++t) {
            states.add(new ArrayList<StoppingState>());
        }
        states.get(0).add((StoppingState)model.getBaseState());
        for (SamplePath sp : samplePaths) {
            for (int t = 1; t < timePeriods; ++t) {
                states.get(t).add((StoppingState)sp.stateActions.get(t).getState());
            }
        }
    }

    public ArrayList<StoppingState> getStates(int time) {
        return states.get(time);
    }
}
