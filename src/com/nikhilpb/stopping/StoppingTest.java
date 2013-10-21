package com.nikhilpb.stopping;

import Jama.Matrix;
import com.nikhilpb.adp.*;
import com.nikhilpb.util.math.PSDMatrix;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/16/13
 * Time: 5:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class StoppingTest {
    private static final int kTimePeriods = 5;
    private static final double[][] kMeanArray = {{1.}, {1.}}, kCovArray = {{1., .5},{.5, 1.}};
    private static final long kSeed = 123l;
    private static final double kTol = 1E-4;

    @Test
    public void baseTest() throws Exception {
        Matrix meanMatrix = new Matrix(kMeanArray);
        PSDMatrix covMatrix = new PSDMatrix(kCovArray);
        Policy policy = new Policy() {
            @Override
            public Action getAction(State state) {
                StoppingState stoppingState = (StoppingState)state;
                if (stoppingState.vector[0] + stoppingState.vector[1] >= 6.) {
                    return StoppingAction.STOP;
                }
                return StoppingAction.CONTINUE;
            }
        };
        StoppingModel model = new StoppingModel(meanMatrix, covMatrix, kTimePeriods, kSeed);
        RewardFunction rewardFunction = new RewardFunction() {
            @Override
            public double value(State state, Action action) { return 0.; }
        };
        MonteCarloEval eval = new MonteCarloEval(model, policy, rewardFunction, kSeed);
        SamplePath samplePath = eval.samplePath(kSeed, kTimePeriods);
        System.out.print(samplePath.toString());
    }

    @Test
    public void kernelTest() throws Exception {
        double[] stateVec1 = {0., 0.}, stateVec2 = {0., 1.};
        StoppingState sState1 = new StoppingState(stateVec1, 1),
                      sState2 = new StoppingState(stateVec2, 2);
        StateKernel kernel = new GaussianStateKernel(1.0);
        assert Math.abs(kernel.value(sState1, sState1) - 1.0) < kTol;
        assert Math.abs(kernel.value(sState1, sState2) - Math.exp(-1.)) < kTol;
    }
}
