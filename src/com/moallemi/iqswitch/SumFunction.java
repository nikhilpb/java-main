package com.moallemi.iqswitch;

public class SumFunction implements SeparableFunction 
{
    private SeparableFunction[] functions;
    private String name;

    public SumFunction(SeparableFunction[] functions,
                       String name) 
    {
        this.functions = functions;
        this.name = name;
    }

    public double getValue(SwitchState state) {
        double sum = 0.0;
        for (int i = 0; i < functions.length; i++)
            sum += functions[i].getValue(state);
        return sum;
    }

    public void addToMatrix(SwitchState state,
                            double weight,
                            MatchingMatrix matrix) {
        for (int i = 0; i < functions.length; i++)
            functions[i].addToMatrix(state, weight, matrix);
    }

    public String toString() {
        return name;
    }
}
            
    