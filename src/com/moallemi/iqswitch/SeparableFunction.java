package com.moallemi.iqswitch;

public interface SeparableFunction extends Function {
    public void addToMatrix(SwitchState state,
                            double weight,
                            MatchingMatrix matrix);
}
