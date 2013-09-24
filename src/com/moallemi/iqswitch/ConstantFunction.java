package com.moallemi.iqswitch;

public class ConstantFunction implements SeparableFunction 
{
    public double getValue(SwitchState state) {
        return 1.0;
    }

    public String toString() {
        return "constant";
    }

    public void addToMatrix(SwitchState state, 
                            double weight,
                            MatchingMatrix matrix) {
        matrix.addOffset(weight);
    }
}