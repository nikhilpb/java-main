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
}
