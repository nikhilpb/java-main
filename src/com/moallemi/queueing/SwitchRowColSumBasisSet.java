package com.moallemi.queueing;

import java.util.*;

import ilog.concert.*;
import ilog.cplex.*;

import com.moallemi.adp.*;

public class SwitchRowColSumBasisSet implements BasisSet {
    private SwitchModel model;
    private StateFunction[] basis;
    private int[] rowSum;
    private int[] colSum;

    public SwitchRowColSumBasisSet(SwitchModel model,
                                   int cutoff, 
                                   int singleCutoff,
                                   double[] poly) 
    {
        this.model = model;
        int queueCount = model.getQueueCount();
        int switchSize = model.getSwitchSize();

        ArrayList<StateFunction> list = new ArrayList<StateFunction>();
        list.add(new ConstantFunction());

        for (int i = 0; i < queueCount; i++)
            for (int p = 0; p < poly.length; p++) 
                list.add(new QueueLengthFunction(i, poly[p]));
        for (int i = 0; i < queueCount; i++) 
            for (int q = 0; q <= singleCutoff; q++) 
                list.add(new SeparableQueueFunction(i, q));
       
        rowSum = new int [switchSize];
        for (int r = 0; r < switchSize; r++) {
            for (int q = 0; q <= cutoff; q++) {
                final int row = r;
                final int length = q;
                list.add(new StateFunction() {
                        public double getValue(State state) {
                            return rowSum[row] == length
                                ? 1.0 : 0.0;
                        }
                        public String toString() {
                            return "rowsum(" + row + "," + length + ")";
                        }
                    });
            }
        }


        colSum = new int [switchSize];
        for (int c = 0; c < switchSize; c++) {
            for (int q = 0; q <= cutoff; q++) {
                final int column = c;
                final int length = q;
                list.add(new StateFunction() {
                        public double getValue(State state) {
                            return colSum[column] == length
                                ? 1.0 : 0.0;
                        }
                        public String toString() {
                            return "colsum(" + column + "," + length + ")";
                        }
                    });
            }
        }

        basis = list.toArray(new StateFunction [0]);
    }

    public int size() { return basis.length; }
    public String getFunctionName(int i) { return basis[i].toString(); }
    public double getMinValue(int i) { return -Double.MAX_VALUE; }
    public double getMaxValue(int i) { return Double.MAX_VALUE; }
    public void addConstraints(IloCplex cplex, IloNumVar[] rVar) 
        throws IloException {}

    public StateFunction getLinearCombination(double[] r) {
        return new LinearCombinationFunction(r, this);
    }

    public void evaluate(State state, double[] out) {
        QueueState qState = (QueueState) state;
        Arrays.fill(rowSum, 0);
        Arrays.fill(colSum, 0);
        for (int row = 0; row < rowSum.length; row++) {
            for (int col = 0; col < colSum.length; col++) {
                int l = qState.getQueueLength(model.getQueueIndex(row, col));
                rowSum[row] += l;
                colSum[col] += l;
            }
        }
        for (int i = 0; i < basis.length; i++)
            out[i] = basis[i].getValue(state);
    }

}
