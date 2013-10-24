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
                StoppingState state = curStates.get(i);
                GaussianTransition gti = (GaussianTransition)
                        args.stoppingModel.getDistribution(state, StoppingAction.CONTINUE);
                double[] meani = gti.getMean();
                columnsC[t][i].curQC = new double[curSize];
                columnsS[t][i].curQC = new double[curSize];
                columnsC[t][i].curQS = new double[curSize];
                columnsS[t][i].curQS = new double[curSize];
                for (int j = 0; j < curSize; ++j) {
                    StoppingState otherState = curStates.get(j);
                    double valueS =  args.kernel.value(state, otherState);

                    GaussianTransition gtj = (GaussianTransition)
                            args.stoppingModel.getDistribution(otherState, StoppingAction.CONTINUE);
                    double[] meanj = gtj.getMean();
                    for (int k = 0; k < meanj.length; ++k) {
                        meanj[k] -= meani[k];
                    }
                    double valueC = valueS + args.twoExp.eval(meanj);
                    columnsC[t][i].curQS[j] = columnsS[t][i].curQC[j] = columnsS[t][i].curQS[j] = valueS;
                    if (t < timePeriods - 1) {
                        columnsS[t][i].curQS[j] = valueC;
                    } else {
                        columnsS[t][i].curQS[j] = valueS;
                    }
                }

            }
        }
    }
}
