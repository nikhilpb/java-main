package com.moallemi.iqswitch;

import java.util.*;

public class SymmetricRowColSumBasisSet implements BasisSet {
    private SwitchModel model;
    private FunctionCollection[] collections;
    private SeparableFunction[] basis;

    public SymmetricRowColSumBasisSet(SwitchModel model,
                                      int cutoff,
                                      int singleCutoff,
                                      boolean symRowCol,
                                      ScalarFunction[] queueFunction,
                                      ScalarFunction[] rowColFunction)
    {
        this.model = model;
        int switchSize = model.getSwitchSize();

        ArrayList<SeparableFunction> list =
            new ArrayList<SeparableFunction>();
        ArrayList<FunctionCollection> collectionList = 
            new ArrayList<FunctionCollection>();

        list.add(new ConstantFunction());

        for (int p = 0; p < queueFunction.length; p++) {
            SeparableFunction[] tmp = 
                new SeparableFunction [switchSize*switchSize];
            int cnt = 0;
            for (int src = 0; src < switchSize; src++) 
                for (int dest = 0; dest < switchSize; dest++) 
                    tmp[cnt++] 
                        = new QueueLengthScalarFunction(model,
                                                        src,
                                                        dest,
                                                        queueFunction[p]);
            list.add(new SumFunction(tmp,
                                     "symlen(" 
                                     + queueFunction[p].toString() + ")"));
        }

        // add single queue indication functions
        for (int length = 0; length <= singleCutoff; length++) {
            SeparableFunction[] tmp = 
                new SeparableFunction [switchSize*switchSize];
            int cnt = 0;
            for (int src = 0; src < switchSize; src++) 
                for (int dest = 0; dest < switchSize; dest++) 
                    tmp[cnt++] = new QueueLengthFunction(model,
                                                         src,
                                                         dest,
                                                         length);
            list.add(new SumFunction(tmp,
                                     "symlen[" + length + "]"));
        }

        // row and column sum functions
        if (cutoff >= 0 || rowColFunction.length > 0) {
            RowSumFunctionCollection[] rowF 
                = new RowSumFunctionCollection [switchSize];
            for (int src = 0; src < switchSize; src++) {
                rowF[src] = new RowSumFunctionCollection(model,
                                                         src,
                                                         cutoff,
                                                         rowColFunction);
                collectionList.add(rowF[src]);
            }
            ColSumFunctionCollection[] colF 
                = new ColSumFunctionCollection [switchSize];
            for (int dest = 0; dest < switchSize; dest++) {
                colF[dest] = new ColSumFunctionCollection(model,
                                                          dest,
                                                          cutoff,
                                                          rowColFunction);
                collectionList.add(colF[dest]);
            }

            if (symRowCol) {
                for (int p = 0; p < rowColFunction.length; p++) {
                    SeparableFunction[] tmp 
                        = new SeparableFunction [2*switchSize];
                    int cnt = 0;
                    for (int src = 0; src < switchSize; src++)
                        tmp[cnt++] = rowF[src].getScalarFunction(p);
                    for (int dest = 0; dest < switchSize; dest++)
                        tmp[cnt++] = colF[dest].getScalarFunction(p);
                    list.add(new SumFunction(tmp,
                                             "symrowcolsum(" 
                                             + rowColFunction[p].toString()
                                             + ")"));
                }
                for (int length = 0; length <= cutoff; length++) {
                    SeparableFunction[] tmp 
                        = new SeparableFunction [2*switchSize];
                    int cnt = 0;
                    for (int src = 0; src < switchSize; src++)
                        tmp[cnt++] = rowF[src].getIndicatorFunction(length);
                    for (int dest = 0; dest < switchSize; dest++)
                        tmp[cnt++] = colF[dest].getIndicatorFunction(length);
                    list.add(new SumFunction(tmp,
                                             "symrowcolsum[" + length + "]"));
                }
            }
            else {
                for (int p = 0; p < rowColFunction.length; p++) {
                    SeparableFunction[] tmp 
                        = new SeparableFunction [switchSize];
                    for (int src = 0; src < switchSize; src++)
                        tmp[src] = rowF[src].getScalarFunction(p);
                    list.add(new SumFunction(tmp,
                                             "symrowsum(" 
                                             + rowColFunction[p].toString()
                                             + ")"));
                }
                for (int p = 0; p < rowColFunction.length; p++) {
                    SeparableFunction[] tmp = 
                        new SeparableFunction [switchSize];
                    for (int dest = 0; dest < switchSize; dest++)
                        tmp[dest] = colF[dest].getScalarFunction(p);
                    list.add(new SumFunction(tmp,
                                             "symcolsum("
                                             + rowColFunction[p].toString()
                                             + ")"));
                }
                for (int length = 0; length <= cutoff; length++) {
                    SeparableFunction[] tmp 
                        = new SeparableFunction [switchSize];
                    for (int src = 0; src < switchSize; src++)
                        tmp[src] = rowF[src].getIndicatorFunction(length);
                    list.add(new SumFunction(tmp,
                                             "symrowsum[" + length + "]"));
                }
                for (int length = 0; length <= cutoff; length++) {
                    SeparableFunction[] tmp = 
                        new SeparableFunction [switchSize];
                    for (int dest = 0; dest < switchSize; dest++)
                        tmp[dest] = colF[dest].getIndicatorFunction(length);
                    list.add(new SumFunction(tmp,
                                             "symcolsum[" + length + "]"));
                }
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

