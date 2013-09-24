package com.moallemi.iqswitch;

public class MaxWeightHeuristicPolicyFunction 
    implements SeparableFunction
{
    private SwitchModel model;
    private double power;

    public MaxWeightHeuristicPolicyFunction(SwitchModel model,
                                            double power)
    {
        this.model = model;
        this.power = power;
    }

    public double getValue(SwitchState state) {
        throw new UnsupportedOperationException();
    }

    public void addToMatrix(SwitchState state,
                            double weight,
                            MatchingMatrix matrix) {
        int switchSize = model.getSwitchSize();
        for (int src = 0; src < switchSize; src++) {
            for (int dest = 0; dest < switchSize; dest++) {
                int q = state.getQueueLength(src, dest);
                if (q > 0) 
                    matrix.addWeight(src, dest, 
                                     -weight * Math.pow(q, power));
            }
        }
    }
}

                                 
