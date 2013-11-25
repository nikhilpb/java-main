package com.nikhilpb.stopping;

import Jama.Matrix;
import com.nikhilpb.adp.*;
import com.nikhilpb.util.math.Distributions;
import com.nikhilpb.util.math.PSDMatrix;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/13/13
 * Time: 8:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class StoppingModel extends MarkovDecisionProcess {
    private final Distributions.GaussianVectorGen gaussianVectorGen;
    private final double[] initialValue;
    private final PSDMatrix covarMatrix;
    private final Matrix meanMatrix;
    private int timePeriods;

    public StoppingModel(Matrix meanMatrix,
                         PSDMatrix covarMatrix,
                         double[] initialValue,
                         int timePeriods,
                         RewardFunction rewardFunction,
                         long seed) {
        this.meanMatrix = meanMatrix;
        this.covarMatrix = covarMatrix;
        this.initialValue = initialValue;
        this.timePeriods = timePeriods;
        this.isInfHorizon = false;
        this.alpha = 1.0;
        this.rewardFunction = rewardFunction;
        gaussianVectorGen = new Distributions.GaussianVectorGen(meanMatrix, covarMatrix, seed);
    }

    public StateDistribution getDistribution(State state, Action action) {
        StoppingState stoppingState = (StoppingState)state;
        StoppingAction stoppingAction = (StoppingAction)action;
        if (stoppingState.time >= timePeriods  - 1 || stoppingAction == StoppingAction.STOP) {
            return null;
        }
        return new GaussianTransition(gaussianVectorGen, stoppingState.vector, stoppingState.time + 1);
    }

    public void reset(long seed) {
        gaussianVectorGen.resetSeed(seed);
    }

    public State getBaseState() {
        return (State) new StoppingState(initialValue, 0);
    }

    public Matrix getMeanMatrix() {
        return meanMatrix;
    }

    public PSDMatrix getCovarMatrix() {
        return covarMatrix;
    }

    public int getDimension() {
        return meanMatrix.getRowDimension();
    }

    public int getTimePeriods() {
        return timePeriods;
    }
}
