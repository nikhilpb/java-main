package com.nikhilpb.stopping;


import com.nikhilpb.adp.*;
import com.nikhilpb.util.math.PSDMatrix;

import java.util.ArrayList;


/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/21/13
 * Time: 2:18 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class KernelSolver implements Solver {
    protected StoppingModel model;
    protected double gamma, kappa;
    protected GaussianStateKernel kernel;
    protected ArrayList<ArrayList<StoppingState>> sampleStates;
    protected ArrayList<Lambda> lambdas;
    protected QPColumnStore columnStore;
    protected int timePeriods;
    protected MeanGaussianKernel oneExp, twoExp;

    protected void init(StoppingModel model,
                        double gamma,
                        double kappa,
                        double bandWidth,
                        int sampleCount,
                        long sampleSeed) {
        this.model = model;
        oneExp = new MeanGaussianKernel(model.getCovarMatrix(), bandWidth);
        twoExp = new MeanGaussianKernel(PSDMatrix.times(model.getCovarMatrix(), 2.),
                                        bandWidth);
        this.gamma = gamma;
        this.kappa = kappa;
        this.kernel = new GaussianStateKernel(bandWidth);
        timePeriods = model.getTimePeriods();
        lambdas = new ArrayList<Lambda>();

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

        System.out.println("Computing the Q matrix");
        ColumnStoreArguments args = new ColumnStoreArguments();
        args.stoppingModel = model;
        args.oneExp = oneExp;
        args.twoExp = twoExp;
        args.kernel = kernel;
        args.stateList = sampleStates;
        columnStore = new CompleteQPStore();
        columnStore.initialize(args);
    }

    public double getGamma() {
        return gamma;
    }

    public StateKernel getKernel() {
        return kernel;
    }
}
