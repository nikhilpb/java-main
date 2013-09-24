package com.moallemi.iqswitch;

public interface BasisSet {
    public int size();
    public String getFunctionName(int i);
    public void evaluate(SwitchState state, double[] out);
    public void addToMatrix(SwitchState state, 
                            MatchingMatrix[] matrix);
    public void addToMatrix(SwitchState state, 
                            double[] weights,
                            MatchingMatrix matrix);
}