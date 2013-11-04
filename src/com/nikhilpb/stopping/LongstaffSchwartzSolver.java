package com.nikhilpb.stopping;

import com.nikhilpb.adp.*;
import com.nikhilpb.util.math.Regression;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 11/3/13
 * Time: 2:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class LongstaffSchwartzSolver implements Solver {
    private StoppingModel model;
    private BasisSet basisSet;
    private int timePeriods;
    ArrayList<SamplePath> samplePaths;
    private ArrayList<ArrayList<StoppingState>> sampleStates;
    private double[][] coeffs;
    ArrayList<StateFunction> contValues;

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
        samplePaths = sampler.getSamplePaths(sampleCount, timePeriods);
    }

    @Override
    public boolean solve() {
        coeffs = new double[timePeriods][];
        contValues = new ArrayList<StateFunction>();
        RewardFunction rf = model.getRewardFunction();
        for (int t = timePeriods - 1; t >= 0; --t) {
            if (t == timePeriods - 1) {
                contValues.set(t, new ConstantStateFunction(0.));
                continue;
            }
            double[][] xData = new double[samplePaths.size()][];
            double[] yData = new double[samplePaths.size()];
            for (int s = 0; s < samplePaths.size(); ++s) {
                SamplePath sp = samplePaths.get(s);
                ArrayList<StateAction> states = sp.stateActions;
                State state = states.get(t).getState();
                xData[s] = basisSet.evaluate(state);
                for (int tt = t+1; tt < timePeriods; tt++) {
                    State state2 = states.get(tt).getState();
                    if (rf.value(state2, StoppingAction.STOP) > contValues.get(tt).value(state2)) {
                        yData[s] = rf.value(state2, StoppingAction.STOP);
                    }
                }
            }
            coeffs[t] = Regression.LinLeastSq(xData, yData);
            contValues.set(t, new LinCombStateFunction(coeffs[t], basisSet));
        }
        return true;
    }

    @Override
    public Policy getPolicy() {
        QFunction qFunction = new BasisQFunction(contValues);
        return new QFunctionPolicy(model, qFunction, model.getRewardFunction(), 1.);
    }
}
