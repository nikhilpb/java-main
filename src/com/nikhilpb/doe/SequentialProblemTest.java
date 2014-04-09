package com.nikhilpb.doe;

import org.junit.Test;

/**
 * Created by nikhilpb on 3/20/14.
 */
public class SequentialProblemTest {
    private static final double kTol = 1E-4;

    @Test
    public void baseTest() throws Exception {
        IdentityFunction ifun = new IdentityFunction();
        assert ifun.value(0.) == 0.;
        assert ifun.value(5.) == 5.;

        DiscretizedFunction disFunction = new DiscretizedFunction(10., 11);
        assert disFunction.value(4.5) == 0;

        double[] values = new double[11];
        values[4] = 4.;
        values[5] = 6.;
        disFunction.setValues(values);
        assert Math.abs(disFunction.value(4.25) - 4.5) < kTol;

    }
}
