package com.moallemi.iqswitch;

public class XLogXScalarFunction implements ScalarFunction {
    public double getValue(int x) {
        return x == 0 ? 0.0 : ((double) x) * Math.log(x);
    }
    public String toString() { return "xlogx"; }
}