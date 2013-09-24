package com.moallemi.iqswitch;

import java.util.ArrayList;

public class ColSumFunctionCollection implements FunctionCollection {
    private SwitchModel model;
    private SwitchState state;
    private int dest;
    private int colSum;
    private SeparableFunction[] indicatorFunctions;
    private SeparableFunction[] scalarFunctions;
    private SeparableFunction[] allFunctions;

    public ColSumFunctionCollection(SwitchModel model, 
                                    int dest, 
                                    int cutoff,
                                    ScalarFunction[] f) {
        this.model = model;
        this.dest = dest;
        ArrayList<SeparableFunction> list 
            = new ArrayList<SeparableFunction>();

        scalarFunctions = new SeparableFunction [f.length];
        for (int p = 0; p < f.length; p++) {
            scalarFunctions[p] = new ColSumScalarFunction(f[p]);
            list.add(scalarFunctions[p]);
        }

        indicatorFunctions = new SeparableFunction [cutoff+1];
        for (int length = 0; length <= cutoff; length++) {
            indicatorFunctions[length] = new ColSumFunction(length);
            list.add(indicatorFunctions[length]);
        }

        allFunctions = list.toArray(new SeparableFunction [0]);
    }

    private class ColSumFunction implements SeparableFunction {
        private int length;
        public ColSumFunction(int length) { this.length = length; }
        public double getValue(SwitchState state) {
            if (state != ColSumFunctionCollection.this.state) 
                throw new IllegalArgumentException("must initialize");
            return colSum == length ? 1.0 : 0.0;
        }
        public void addToMatrix(SwitchState state,
                                double weight,
                                MatchingMatrix matrix) {
            if (state != ColSumFunctionCollection.this.state) 
                throw new IllegalArgumentException("must initialize");
            double p0 = model.getDestArrivalProbability(dest, 
                                                        length - colSum);
            double p1 = model.getDestArrivalProbability(dest,
                                                        length - colSum + 1);
            if (p0 != 0.0 || p1 != 0.0) {
                matrix.addOffset(weight * p0);
                int switchSize = model.getSwitchSize();
                double v = weight * (p1 - p0);
                for (int src = 0; src < switchSize; src++) {
                    if (state.getQueueLength(src, dest) > 0)
                        matrix.addWeight(src, dest, v);
                }
            }
        }
        public String toString() {
            return "colsum(" + dest + ")[" + length + "]";
        }
    }

    private class ColSumScalarFunction implements SeparableFunction {
        private ScalarFunction f;
        public ColSumScalarFunction(ScalarFunction f) { this.f = f; }
        public double getValue(SwitchState state) {
            if (state != ColSumFunctionCollection.this.state) 
                throw new IllegalArgumentException("must initialize");
            return f.getValue(colSum);
        }
        public void addToMatrix(SwitchState state,
                                double weight,
                                MatchingMatrix matrix) {
            if (state != ColSumFunctionCollection.this.state) 
                throw new IllegalArgumentException("must initialize");

            int switchSize = model.getSwitchSize();
            double offset = 0.0;
            for (int arrivals = 0; arrivals <= switchSize; arrivals++)
                offset += 
                    model.getDestArrivalProbability(dest, arrivals)
                    * f.getValue(colSum + arrivals);
            matrix.addOffset(weight * offset);

            if (colSum > 0) {
                double v = 0.0;
                for (int arrivals = 0; arrivals <= switchSize; arrivals++)
                    v += model.getDestArrivalProbability(dest, arrivals)
                        * (f.getValue(colSum + arrivals - 1)
                           - f.getValue(colSum + arrivals));
                v *= weight;
                for (int src = 0; src < switchSize; src++) {
                    if (state.getQueueLength(src, dest) > 0)
                        matrix.addWeight(src, dest, v);
                }
            }
        }

        public String toString() {
            return "colsum(" + f.toString() + ")[" + dest + "]";
        }
    }


    public SeparableFunction getFunction(int i) {
        return allFunctions[i];
    }
    public int size() { return allFunctions.length; }
    public SeparableFunction getIndicatorFunction(int length) {
        return indicatorFunctions[length];
    }
    public int sizeIndicatorFunctions() { 
        return indicatorFunctions.length;
    }
    public SeparableFunction getScalarFunction(int p) {
        return scalarFunctions[p];
    }
    public int sizeScalarFunctions() {
        return scalarFunctions.length;
    }

    public void evaluateAt(SwitchState state) {
        this.state = state;
        int switchSize = model.getSwitchSize();
        colSum = 0;
        for (int src = 0; src < switchSize; src++) 
            colSum += state.getQueueLength(src, dest);
    }
    
}
    