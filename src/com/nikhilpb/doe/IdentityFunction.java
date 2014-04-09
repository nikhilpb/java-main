package com.nikhilpb.doe;

import java.io.PrintStream;

/**
 * Created by nikhilpb on 3/20/14.
 */
public class IdentityFunction implements OneDFunction {
    private static final int kPrintCount = 1000;

    @Override
    public double value(double x) {
        return x;
    }

    @Override
    public void printFn(PrintStream stream, double low, double high) {
        final double delta = (high - low) / kPrintCount;
        for (int i = 0; i < kPrintCount + 1; ++i) {
            stream.println((low + i * delta) + "," + (low + i * delta));
        }
    }
}
