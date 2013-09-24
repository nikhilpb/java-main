package com.nikhilpb.matching;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 9/20/13
 * Time: 1:01 PM
 * To change this template use File | Settings | File Templates.
 */


// TODO: Test based on sampling

public class MultiIndependentDistTest extends TestCase {

    static final int kSampleCount = 100000;

    static final double kTol = 1E-2;

    @Test
    public void testBase() throws Exception {
        double[][] probs = {{0.1, 0.9}, {0.3, 0.2, 0.5}};
        MultiIndependentDist dist = new MultiIndependentDist(probs, new Random());
        assertEquals(probs[0][0], dist.getProb(0, 0));
        assertEquals(probs[1][2], dist.getProb(1, 2));
        Integer[] sample;
        int count1 = 0, count2 = 0, count3 = 0;
        for (int i = 0; i < kSampleCount; ++i) {
            sample = dist.nextSample();
            if (sample[0] == 0) {
                ++count1;
            }
            if (sample[1] == 2) {
                ++count2;
            }
            if (sample[1] == 0) {
                ++count3;
            }
        }
        double sampleCountD = (double) kSampleCount;
        double eps1 = ((double) count1) / sampleCountD,
               eps2 = ((double) count2) / sampleCountD,
               eps3 = ((double) count3) / sampleCountD;
        assertEquals(Math.abs(eps1 - 0.1) < kTol, true);
        assertEquals(Math.abs(eps2 - 0.5) < kTol, true);
        assertEquals(Math.abs(eps3 - 0.3) < kTol, true);
    }
}
