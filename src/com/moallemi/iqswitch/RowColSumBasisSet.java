package com.moallemi.iqswitch;

import java.util.*;

public class RowColSumBasisSet implements BasisSet {
    private SwitchModel model;
    private FunctionCollection[] collections;
    private SeparableFunction[] basis;

    public RowColSumBasisSet(SwitchModel model,
                             int cutoff,
                             int singleCutoff,
                             ScalarFunction[] queueFunction,
                             ScalarFunction[] rowColFunction) {
        this.model = model;
        int switchSize = model.getSwitchSize();

        ArrayList<SeparableFunction> list =
            new ArrayList<SeparableFunction>();
        ArrayList<FunctionCollection> collectionList = 
            new ArrayList<FunctionCollection>();

        list.add(new ConstantFunction());

        for (int src = 0; src < switchSize; src++) 
            for (int dest = 0; dest < switchSize; dest++) 
                for (int p = 0; p < queueFunction.length; p++)
                    list.add(new QueueLengthScalarFunction(model,
                                                           src,
                                                           dest,
                                                           queueFunction[p]));

        // add single queue indication functions
        for (int src = 0; src < switchSize; src++) 
            for (int dest = 0; dest < switchSize; dest++) 
                for (int length = 0; length <= singleCutoff; length++)
                    list.add(new QueueLengthFunction(model,
                                                     src,
                                                     dest,
                                                     length));
        
        // row sum functions
        if (cutoff >= 0 || rowColFunction.length > 0) {
            for (int src = 0; src < switchSize; src++) {
                FunctionCollection f = 
                    new RowSumFunctionCollection(model,
                                                 src,
                                                 cutoff,
                                                 rowColFunction);
                int size = f.size();
                for (int i = 0; i < size; i++)
                    list.add(f.getFunction(i));
                collectionList.add(f);
            }
        }

        // column sum functions
        if (cutoff >= 0 || rowColFunction.length > 0) {
            for (int dest = 0; dest < switchSize; dest++) {
                FunctionCollection f = 
                    new ColSumFunctionCollection(model,
                                                 dest,
                                                 cutoff,
                                                 rowColFunction);
                int size = f.size();
                for (int i = 0; i < size; i++)
                    list.add(f.getFunction(i));
                collectionList.add(f);
            }
        }                                              

        basis = list.toArray(new SeparableFunction [0]);
        collections = collectionList.toArray(new FunctionCollection [0]);
    } 

    public int size() { return basis.length; }
    public String getFunctionName(int i) { return basis[i].toString(); }
    
    public void evaluate(SwitchState state, double[] out) {
        for (int i = 0; i < collections.length; i++)
            collections[i].evaluateAt(state);
        for (int i = 0; i < basis.length; i++)
            out[i] = basis[i].getValue(state);
    }

    public void addToMatrix(SwitchState state,
                            MatchingMatrix[] matrix) {
        for (int i = 0; i < collections.length; i++)
            collections[i].evaluateAt(state);
        for (int i = 0; i < basis.length; i++)
            basis[i].addToMatrix(state, 1.0, matrix[i]);
    }

    public void addToMatrix(SwitchState state,
                            double[] weights,
                            MatchingMatrix matrix) {
        for (int i = 0; i < collections.length; i++)
            collections[i].evaluateAt(state);
        for (int i = 0; i < basis.length; i++)
            basis[i].addToMatrix(state, weights[i], matrix);
    }

}

