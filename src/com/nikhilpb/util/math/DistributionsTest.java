package com.nikhilpb.util.math;

import Jama.Matrix;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/14/13
 * Time: 2:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class DistributionsTest {
    private static final int kSampleCount = 100000;
    private static final double kTol = 0.05;

    @Test
    public void testBase() throws Exception {
        double[][] meanVec = {{1.}, {0.}}, sigmaVec = {{5.,1.},{1.,1.}};
        Matrix mean = new Matrix(meanVec), sigma = new Matrix(sigmaVec);
        Distributions.GaussianVectorGen gen = new Distributions.GaussianVectorGen(mean, new PSDMatrix(sigma), 0l);
        double[] sample, sum = new double[2], sumsq = new double[2];
        double cov = 0.0;
        for (int t = 0; t < kSampleCount; ++t) {
            sample = gen.nextValue();
            sum[0] += sample[0]; sum[1] += sample[1];
            sumsq[0] += sample[0]*sample[0]; sumsq[1] += sample[1]*sample[1];
            cov += sample[0] * sample[1];
        }
        sum[0] = sum[0] / (double)kSampleCount; sum[1] = sum[1] / (double)kSampleCount;
        sumsq[0] = (sumsq[0] / (double)kSampleCount) - (sum[0]*sum[0]);
        sumsq[1] = (sumsq[1] / (double)kSampleCount) - (sum[1]*sum[1]);
        cov = (cov/ (double)kSampleCount) - (sum[0]*sum[1]);
        assert Math.abs(sum[0] - meanVec[0][0]) < kTol;
        assert Math.abs(sum[1] - meanVec[1][0]) < kTol;
        assert Math.abs(sumsq[0] - sigmaVec[0][0]) < kTol;
        assert Math.abs(sumsq[1] - sigmaVec[1][1]) < kTol;
        assert Math.abs(cov - sigmaVec[0][1]) < kTol;
    }
}
