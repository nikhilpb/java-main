package com.moallemi.iqswitch;

public class SymmetricQueueLengthScalarFunction
    implements SeparableFunction
{
    private SwitchModel model;
    private SeparableFunction[] components;
    private ScalarFunction f;

    public SymmetricQueueLengthScalarFunction(SwitchModel model,
                                              ScalarFunction f)
    {
        this.model = model;
        this.f = f;
        int switchSize = model.getSwitchSize();
        components = new SeparableFunction [switchSize*switchSize];
        int cnt = 0;
        for (int src = 0; src < switchSize; src++)
            for (int dest = 0; dest < switchSize; dest++)
                components[cnt++] =
                    new QueueLengthScalarFunction(model,
                                                  src,
                                                  dest,
                                                  f);
    }
    
    public double getValue(SwitchState state) {
        double sum = 0.0;
        for (int i = 0; i < components.length; i++)
            sum += components[i].getValue(state);
        return sum;
    }

    public String toString() {
        return "symqlen(" + f.toString() + ")";
    }
        

    public void addToMatrix(SwitchState state, 
                            double weight,
                            MatchingMatrix matrix) {
        for (int i = 0; i < components.length; i++)
            components[i].addToMatrix(state, weight, matrix);
    }
}