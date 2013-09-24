package com.moallemi.queueing;

import java.util.BitSet;

import com.moallemi.adp.*;
import com.moallemi.math.LawlerBipartiteMatcher;

public class SwitchValueFunctionPolicy implements Policy {
    private SwitchModel model;
    private StateFunction valueFunction; 
    private LawlerBipartiteMatcher matcher;
    private int switchSize;
    private double[][] weights;
    private boolean[][] isEmpty;

    // not thread-safe!
    public SwitchValueFunctionPolicy(SwitchModel model,
                                     StateFunction valueFunction) {
        this.model = model;
        this.valueFunction = valueFunction;

        switchSize = model.getSwitchSize();
        matcher = new LawlerBipartiteMatcher(switchSize);
        weights = new double [switchSize][switchSize];
        isEmpty = new boolean [switchSize][switchSize];
    }

    private void initMatching(QueueState qState, QueueStateInfo qInfo) {
        double vCurrent = valueFunction.getValue(qState);
        for (int dest = 0; dest < switchSize; dest++) {
            for (int src = 0; src < switchSize; src++) {
                int q = model.getQueueIndex(src, dest);
                isEmpty[dest][src] = qState.getQueueLength(q) == 0;
                if (isEmpty[dest][src]) {
                    weights[dest][src] = 0.0;
                }
                else {
                    QueueState nextState = qInfo.getNextStateIfServiced(q);
                    weights[dest][src] = 
                        model.getServiceRate(q, dest) *
                        (vCurrent - valueFunction.getValue(nextState));
                }
                
            }
        }
        
        matcher.computeMax(weights);
    }

    public int getAction(State state, StateInfo info) {
        QueueState qState = (QueueState) state;
        QueueStateInfo qInfo = (QueueStateInfo) info;
        
        initMatching(qState, qInfo);
        int[] match = matcher.getMatchingSource();

        int actionCount = qInfo.getActionCount();
        int maxAction = -1;
        for (int a = 0; a < actionCount; a++) {
            ServerAction action = (ServerAction) qInfo.getAction(a);
            boolean found = true;
            for (int dest = 0; dest < switchSize; dest++) {
                if (!isEmpty[dest][match[dest]]) {
                    int qAction = action.getQueueWorkedOn(dest);
                    int qMatch = model.getQueueIndex(match[dest], dest);
                    if (qMatch != qAction) {
                        found = false;
                        break;
                    }
                }
            }
            if (found) {
                maxAction = a;
                break;
            }
        }

        if (maxAction < 0)
            throw new IllegalStateException("could not find action "
                                            + "corresponding "
                                            + "to matching");

        return maxAction;
    }

    public BitSet getOptimalActionMask(State state,
                                       StateInfo info,
                                       double tolerance) {
        QueueState qState = (QueueState) state;
        QueueStateInfo qInfo = (QueueStateInfo) info;
        
        initMatching(qState, qInfo);
        int[] match = matcher.getMatchingSource();

        int actionCount = qInfo.getActionCount();
        int maxAction = -1;
        for (int a = 0; a < actionCount; a++) {
            ServerAction action = (ServerAction) qInfo.getAction(a);
            boolean found = true;
            for (int dest = 0; dest < switchSize; dest++) {
                if (!isEmpty[dest][match[dest]]) {
                    int qAction = action.getQueueWorkedOn(dest);
                    int qMatch = model.getQueueIndex(match[dest], dest);
                    if (qMatch != qAction) {
                        found = false;
                        break;
                    }
                }
            }
            if (found) {
                maxAction = a;
                break;
            }
        }

        if (maxAction < 0)
            throw new IllegalStateException("could not find action "
                                            + "corresponding "
                                            + "to matching");

        double[] actionValue = new double [actionCount];
        double maxActionValue = Double.NEGATIVE_INFINITY;
        for (int a = 0; a < actionCount; a++) {
            ServerAction action = (ServerAction) qInfo.getAction(a);
            actionValue[a] = 0.0;
            for (int dest = 0; dest < switchSize; dest++) {
                int qAction = action.getQueueWorkedOn(dest);
                if (qAction >= 0) {
                    int src = model.getSourcePort(qAction);
                    actionValue[a] += weights[dest][src];
                }
            }
            if (actionValue[a] > maxActionValue)
                maxActionValue = actionValue[a];
        }

        BitSet mask = new BitSet(actionCount);
        for (int a = 0; a < actionCount; a++) {
            if (Math.abs(maxActionValue - actionValue[a]) < tolerance)
                mask.set(a);
        }

        if (!mask.get(maxAction))
            throw new IllegalStateException("matching failed");

        return mask;
    }
        
    public Action[] getOptimalActionList(State state, 
					 StateInfo info, 
					 double tolerance)
    {
        int actionCount = info.getActionCount();
        BitSet mask = getOptimalActionMask(state, info, tolerance);
	Action[] opt = new Action [mask.cardinality()];
	int cnt = 0;
        for (int a = 0; a < actionCount; a++) {
	    if (mask.get(a))
		opt[cnt++] = info.getAction(a);
	}
        return opt;
    }
}
