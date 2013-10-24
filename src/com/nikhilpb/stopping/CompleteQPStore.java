package com.nikhilpb.stopping;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/24/13
 * Time: 1:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class CompleteQPStore implements QPColumnStore {
    private QPColumn[][] columnsS, columnsC;

    @Override
    public QPColumn getColumn(int t, int stateInd, StoppingAction stoppingAction) {
        if (stoppingAction == StoppingAction.STOP) {
            return columnsS[t][stateInd];
        } else {
            return columnsC[t][stateInd];
        }
    }

    @Override
    public void initialize(ColumnStoreArguments args) {
        final int timePeriods = args.stateList.size();
        columnsC = new QPColumn[timePeriods][];
        columnsS = new QPColumn[timePeriods][];
        ArrayList<StoppingState> curStates, prevStates, nextStates;
        int curSize, prevSize, nextSize;
        for (int t = 0; t < timePeriods; ++t) {
            curStates = args.stateList.get(t);
            curSize = curStates.size();
            columnsC[t] = new QPColumn[curSize];
            columnsS[t] = new QPColumn[curSize];
            for (int i = 0; i < curSize; ++i) {
                columnsC[t][i] = new QPColumn();
                columnsS[t][i] = new QPColumn();
                StoppingState state = curStates.get(i);
                GaussianTransition gti = (GaussianTransition)
                        args.stoppingModel.getDistribution(state, StoppingAction.CONTINUE);

                double[] meani;
                if (t < timePeriods - 1) {
                    meani = gti.getMean();
                } else {
                    meani = new double[state.vector.length];
                }
                columnsC[t][i].curC = new double[curSize];
                columnsS[t][i].curC = new double[curSize];
                columnsC[t][i].curS = new double[curSize];
                columnsS[t][i].curS = new double[curSize];
                for (int j = 0; j < curSize; ++j) {
                    StoppingState otherState = curStates.get(j);
                    double valueS =  args.kernel.value(state, otherState);
                    columnsS[t][i].curS[j] = columnsS[t][i].curC[j] = columnsC[t][i].curS[j] = valueS;
                    double valueC = 0.;
                    if (t < timePeriods - 1) {
                        GaussianTransition gtj = (GaussianTransition)
                                args.stoppingModel.getDistribution(otherState, StoppingAction.CONTINUE);
                        double[] meanj = gtj.getMean();
                        for (int k = 0; k < meanj.length; ++k) {
                            meanj[k] -= meani[k];
                        }
                        valueC = valueS + args.twoExp.eval(meanj);
                    }
                    columnsC[t][i].curC[j] = valueC;
                }
                if (t > 0) {
                    prevStates = args.stateList.get(t-1);
                    prevSize = prevStates.size();
                    columnsC[t][i].prevC = new double[prevSize];
                    columnsS[t][i].prevC = new double[prevSize];
                    for (int j = 0; j < prevSize; ++j) {
                        StoppingState prevState = prevStates.get(j);
                        GaussianTransition gtj = (GaussianTransition)
                                args.stoppingModel.getDistribution(prevState, StoppingAction.CONTINUE);
                        double[] meanj = gtj.getMean();
                        for (int k = 0; k < meanj.length; ++k) {
                            meanj[k] -= state.vector[k];
                        }
                        double value = args.oneExp.eval(meanj);
                        columnsC[t][i].prevC[j] = value;
                        columnsS[t][i].prevC[j] = value;
                    }
                }
                if (t < timePeriods - 1) {
                    nextStates = args.stateList.get(t+1);
                    nextSize = nextStates.size();
                    columnsC[t][i].nextC = new double[nextSize];
                    columnsC[t][i].nextS = new double[nextSize];
                    for (int j = 0; j < nextSize; ++j) {
                        StoppingState nextState = nextStates.get(j);
                        double[] meanj = new double[nextState.vector.length];
                        for (int k = 0; k < meanj.length; ++k) {
                            meanj[k] = meani[k] - nextState.vector[k];
                        }
                        double value = args.oneExp.eval(meanj);
                        columnsC[t][i].nextC[j] = columnsC[t][i].nextS[j] = value;
                    }
                }
            }
        }
    }
}
