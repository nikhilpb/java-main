package com.moallemi.iqswitch;

import java.util.ArrayList;

public class RowSumFunctionCollection implements FunctionCollection {
    private SwitchModel model;
    private SwitchState state;
    private int src;
    private int rowSum;
    private SeparableFunction[] indicatorFunctions;
    private SeparableFunction[] scalarFunctions;
    private SeparableFunction[] allFunctions;

    public RowSumFunctionCollection(SwitchModel model, 
                                    int src, 
                                    int cutoff,
                                    ScalarFunction[] f)
    {
        this.model = model;
        this.src = src;
        ArrayList<SeparableFunction> list 
            = new ArrayList<SeparableFunction>();

        scalarFunctions = new SeparableFunction [f.length];
        for (int p = 0; p < f.length; p++) {
            scalarFunctions[p] = new RowSumScalarFunction(f[p]);
            list.add(scalarFunctions[p]);
        }

        indicatorFunctions = new SeparableFunction [cutoff+1];
        for (int length = 0; length <= cutoff; length++) {
            indicatorFunctions[length] = new RowSumFunction(length);
            list.add(indicatorFunctions[length]);
        }

        allFunctions = list.toArray(new SeparableFunction [0]);
    }

    private class RowSumFunction implements SeparableFunction {
        private int length;
        public RowSumFunction(int length) { this.length = length; }
        public double getValue(SwitchState state) {
            if (state != RowSumFunctionCollection.this.state) 
                throw new IllegalArgumentException("must initialize");
            return rowSum == length ? 1.0 : 0.0;
        }
        public void addToMatrix(SwitchState state,
                                double weight,
                                MatchingMatrix matrix) {
            if (state != RowSumFunctionCollection.this.state) 
                throw new IllegalArgumentException("must initialize");
            double p0 = model.getSourceArrivalProbability(src, 
                                                          length - rowSum);
            double p1 = model.getSourceArrivalProbability(src,
                                                          length - rowSum + 1);
            if (p0 != 0.0 || p1 != 0.0) {
                matrix.addOffset(weight * p0);
                int switchSize = model.getSwitchSize();
                double v = weight * (p1 - p0);
                for (int dest = 0; dest < switchSize; dest++) {
                    if (state.getQueueLength(src, dest) > 0)
                        matrix.addWeight(src, dest, v);
                }
            }
        }
        public String toString() {
            return "rowsum(" + src + ")[" + length + "]";
        }
    }

    private class RowSumScalarFunction implements SeparableFunction {
        private ScalarFunction f;
        public RowSumScalarFunction(ScalarFunction f) { this.f = f; }
        public double getValue(SwitchState state) {
            if (state != RowSumFunctionCollection.this.state) 
                throw new IllegalArgumentException("must initialize");
            return f.getValue(rowSum);
        }
        public void addToMatrix(SwitchState state,
                                double weight,
                                MatchingMatrix matrix) {
            if (state != RowSumFunctionCollection.this.state) 
                throw new IllegalArgumentException("must initialize");

            int switchSize = model.getSwitchSize();
            double offset = 0.0;
            for (int arrivals = 0; arrivals <= switchSize; arrivals++)
                offset += 
                    model.getSourceArrivalProbability(src, arrivals)
                    * f.getValue(rowSum + arrivals);
            matrix.addOffset(weight * offset);

            if (rowSum > 0) {
                double v = 0.0;
                for (int arrivals = 0; arrivals <= switchSize; arrivals++)
                    v += model.getSourceArrivalProbability(src, arrivals)
                        * (f.getValue(rowSum + arrivals - 1)
                           - f.getValue(rowSum + arrivals));
                v *= weight;
                for (int dest = 0; dest < switchSize; dest++) {
                    if (state.getQueueLength(src, dest) > 0)
                        matrix.addWeight(src, dest, v);
                }
            }
        }

        public String toString() {
            return "rowsum(" + f.toString() + ")[" + src + "]";
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
        rowSum = 0;
        for (int dest = 0; dest < switchSize; dest++) 
            rowSum += state.getQueueLength(src, dest);
    }
    
}
    