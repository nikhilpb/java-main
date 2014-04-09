package com.nikhilpb.doe;

import java.io.PrintStream;

/**
 * Created by nikhilpb on 3/20/14.
 */
public interface OneDFunction {
    public double value(double x);

    public void printFn(PrintStream stream, double low, double high);
}
