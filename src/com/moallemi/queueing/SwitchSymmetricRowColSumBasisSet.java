package com.moallemi.queueing;

import java.util.*;

import ilog.concert.*;
import ilog.cplex.*;

import com.moallemi.adp.*;

public class SwitchSymmetricRowColSumBasisSet implements BasisSet {
    private SwitchModel model;
    private StateFunction[] basis;
    private int[] rowSum;
    private int[] colSum;

    public SwitchSymmetricRowColSumBasisSet(SwitchModel model,
                                            boolean forceRowColSym,
                                            int cutoff, 
                                            int singleCutoff,
                                            double[] poly) 
    {
        this.model = model;
        ArrayList<StateFunction> list = new ArrayList<StateFunction>();
        list.add(new ConstantFunction());

        // is this correct?
        for (int p = 0; p < poly.length; p++) 
            list.add(new SymmetricQueueLengthFunction(poly[p]));
        for (int q = 0; q <= singleCutoff; q++) 
            list.add(new SymmetricSeparableQueueFunction(q));

        int switchSize = model.getSwitchSize();
        rowSum = new int [switchSize];
        colSum = new int [switchSize];
        for (int q = 0; q <= cutoff; q++) {
            final int length = q;
            
            if (forceRowColSym) {
                list.add(new StateFunction() {
                        public double getValue(State state) {
                            double sum = 0.0;
                            for (int i = 0; i < rowSum.length; i++) {
                                if (rowSum[i] == length) 
                                    sum++;
                                if (colSum[i] == length) 
                                    sum++;
                            }
                            return sum;
                        }
                        public String toString() {
                            return "swsymrowcolsum(" + length + ")";
                        }
                    });
            }
            else {
                list.add(new StateFunction() {
                        public double getValue(State state) {
                            double sum = 0.0;
                            for (int i = 0; i < rowSum.length; i++) {
                                if (rowSum[i] == length) 
                                    sum++;
                            }
                            return sum;
                        }
                        public String toString() {
                            return "swsymrowsum(" + length + ")";
                        }
                    });
                
                list.add(new StateFunction() {
                        public double getValue(State state) {
                            double sum = 0.0;
                            for (int i = 0; i < colSum.length; i++) {
                                if (colSum[i] == length) 
                                    sum++;
                            }
                            return sum;
                    }
                        public String toString() {
                            return "swsymcolsum(" + length + ")";
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
