package com.moallemi.iqswitch;

import com.moallemi.math.*;

public class MatchingPolicy {
    private SwitchModel model;
    private BipartiteMatcher matcher;
    private SeparableFunction valueFunction;
    private MatchingMatrix matrix;
    private SwitchAction action;
    private double value;

    public MatchingPolicy(SwitchModel model,
                          SeparableFunction valueFunction,
                          BipartiteMatcherFactory factory)
    {
        this.model = model;
        this.valueFunction = valueFunction;
        int switchSize = model.getSwitchSize();
        matcher = factory.newMatcher(switchSize);
        matrix = new MatchingMatrix(model);
    }

    public void setState(SwitchState state) {
        int switchSize = model.getSwitchSize();

        matrix.reset();
        valueFunction.addToMatrix(state,
                                  1.0,
                                  matrix);
        double[][] w = matrix.getWeights();

        // sanity check, remove for best performance
//         for (int src = 0; src < switchSize; src++) {
//             for (int dest = 0; dest < switchSize; dest++) {
//                 if (state.getQueueLength(src, dest) == 0) {
//                     if (w[src][dest] != 0.0)
//                         throw new IllegalStateException("bad weights");
//                 }
//                 else if (w[src][dest] >= 0.0)
//                     throw new IllegalStateException("bad weights");
//             }
//         }



        matcher.computeMin(w);
        int[] m = matcher.getMatchingDest();

        int[] m2 = new int [switchSize];
        for (int dest = 0; dest < switchSize; dest++) 
            m2[dest] = state.getQueueLength(m[dest], dest) > 0
                ? m[dest]
                : -1;
        action = new SwitchAction(m2);
        value = matcher.getMatchingWeight() + matrix.getOffset();
    }

    public SwitchAction getAction() { return action; }
    public double getValue() { return value; }

    public double getActionValue(SwitchAction action) {
        double value = matrix.getOffset();
        double[][] w = matrix.getWeights();
        int switchSize = model.getSwitchSize();
        for (int dest = 0; dest < switchSize; dest++) {
            int src = action.getSourceWorkedOn(dest);
            if (src >= 0)
                value += w[src][dest];
        }
        return value;
    }
        
    public boolean isActionOptimal(SwitchAction action,
                                   double tolerance) {
        return Math.abs(value - getActionValue(action)) < tolerance;
    }
}


        
    