package com.nikhilpb.stopping;

import Jama.Matrix;
import com.nikhilpb.adp.*;
import com.nikhilpb.util.math.PSDMatrix;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/16/13
 * Time: 5:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class StoppingTest {
    private static final int kTimePeriods = 3;
    private static final double[][] kMeanArray = {{1.}, {1.}}, kCovArray = {{1., .5},{.5, 1.}};
    private static final double[] kInitValue = {0., 0.};
    private static final long kSeed = 123l;
    private static final double kTol = 1E-4;
    private static final StoppingModel model;

    static {
        Matrix meanMatrix = new Matrix(kMeanArray);
        PSDMatrix covMatrix = new PSDMatrix(kCovArray);
        RewardFunction rewardFunction = new RewardFunction() {
            @Override
            public double value(State state, Action action) { return 0.; }
        };
        model = new StoppingModel(meanMatrix, covMatrix, kInitValue, kTimePeriods, rewardFunction, kSeed);
    }

    @Test
    public void baseTest() throws Exception {
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
        MonteCarloEval eval = new MonteCarloEval(model, policy, kSeed);
        SamplePath samplePath = eval.samplePath(kSeed, kTimePeriods);
        System.out.print(samplePath.toString());
    }

    @Test
    public void kernelTest() throws Exception {
        double[] stateVec1 = {0., 0.}, stateVec2 = {0., 1.};
        StoppingState sState1 = new StoppingState(stateVec1, 1),
                      sState2 = new StoppingState(stateVec2, 2);
        StateKernel kernel = new GaussianStateKernel(1., 1.);
        assert Math.abs(kernel.value(sState1, sState1) - 1.) < kTol;
        assert Math.abs(kernel.value(sState1, sState2) - Math.exp(-.5)) < kTol;
    }

    @Test
    public void meanGaussianKernelTest() throws Exception {
        PSDMatrix sigma = new PSDMatrix(new Matrix(kCovArray));
        double bw = 2.;
        MeanGaussianKernel mgk = new MeanGaussianKernel(sigma, bw, 1.);
        double[] mean = {0., 0.};
        assert Math.abs(mgk.eval(mean) - 0.676123) < kTol; // computed separately
        double[] mean2 = {1., 0.};
        assert Math.abs(mgk.eval(mean2) - 0.569607) < kTol; // computed separately
    }

    private static boolean approxEqual(double val1, double val2) {
        return Math.abs(val1 - val2) < kTol;
    }
}
