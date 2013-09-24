package com.moallemi.iqswitch;

public class LogScalarFunction implements ScalarFunction {
    public double getValue(int x) {
        return x == 0 ? 0.0 : Math.log(x);
    }
    public String toString() { return "log"; }
}