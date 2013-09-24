package com.moallemi.iqswitch;

public interface FunctionCollection {
    public int size();
    public SeparableFunction getFunction(int i);
    public void evaluateAt(SwitchState state);
}