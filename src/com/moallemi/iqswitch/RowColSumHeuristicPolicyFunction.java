package com.moallemi.iqswitch;

import java.util.Arrays;

public class RowColSumHeuristicPolicyFunction
    implements SeparableFunction
{
    private SwitchModel model;
    private double alpha;
    private int[] rowSum;
    private int[] colSum;

    public RowColSumHeuristicPolicyFunction(SwitchModel model,
                                            double alpha)
    {
        this.model = model;
        this.alpha = alpha;
        int switchSize = model.getSwitchSize();
        rowSum = new int [switchSize];
        colSum = new int [switchSize];
    }

    public double getValue(SwitchState state) {
        throw new UnsupportedOperationException();
    }

    public void addToMatrix(SwitchState state,
                            double weight,
                            MatchingMatrix matrix) {
        int switchSize = model.getSwitchSize();
        Arrays.fill(rowSum, 0);
        Arrays.fill(colSum, 0);
        for (int src = 0; src < switchSize; src++) {
            for (int dest = 0; dest < switchSize; dest++) {
                int q = state.getQueueLength(src, dest);
                rowSum[src] += q;
                colSum[dest] += q;
            }
        }
        for (int src = 0; src < switchSize; src++) {
            for (int dest = 0; dest < switchSize; dest++) {
                int q = state.getQueueLength(src, dest);
                if (q > 0) {
                    double w = 
                        1.0 + alpha * (Math.log(rowSum[src])
                                       + Math.log(colSum[dest]));
                    matrix.addWeight(src, dest, 
                                     -weight * w);
                }
            }
        }
    }
}

                                 
