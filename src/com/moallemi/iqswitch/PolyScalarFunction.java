package com.moallemi.iqswitch;

public class PolyScalarFunction implements ScalarFunction {
    private double power;

    public PolyScalarFunction(double power) {
        this.power = power;
    }

    public double getValue(int x) {
        return Math.pow(x, power);
    }

    public String toString() { return "x^" + power; }
}