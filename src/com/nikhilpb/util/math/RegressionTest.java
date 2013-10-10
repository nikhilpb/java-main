package com.nikhilpb.util.math;

import org.junit.Test;

import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/1/13
 * Time: 6:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class RegressionTest {
    private static final double kTol = 1E-4;
    @Test
    public void testBase() throws Exception {
        double[][] xData = new double[2][2];
        double[] yData = new double[2];
        xData[0][0] = 1.0; xData[0][1] = 1.0; xData[1][0] = 1.0; xData[1][1] = 2.;
        yData[0] = 1.; yData[1] = 2.0;
        double[] weights = Regression.LinLeastSq(xData, yData);
        assert weights.length == 2;
        assert Math.abs(weights[0] - 0.) < kTol;
        assert Math.abs(weights[1] - 1.) < kTol;
    }
}
