package com.moallemi.adp;

public class ConstantFunction implements StateFunction {
    private double value;
    
    public ConstantFunction(double value) {
        this.value = value;
    }

    public ConstantFunction() {
        this.value = 1.0;
    }

    public double getValue(State state) {
        return value;
    }

    public String toString() { return "constant"; }
}
