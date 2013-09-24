package com.moallemi.queueing;

import com.moallemi.adp.*;

public class MaxWeightHeuristicPolicy extends MinValuePolicy {
    private OpenQueueingNetworkModel model;
    private double alpha;

    public MaxWeightHeuristicPolicy(OpenQueueingNetworkModel model,
                                    double alpha) 
    {
        this.model = model;
        this.alpha = alpha;
    }

    public double getActionValue(State state, StateInfo info, int a) {
	QueueState qState = (QueueState) state;
	ServerAction action = (ServerAction) info.getAction(a);
	double actionValue = 0.0;
	int serverCount = action.getServerCount();
	for (int j = 0; j < serverCount; j++) {
	    int q = action.getQueueWorkedOn(j);
	    if (q >= 0)
		actionValue += model.getCost(q) 
		    * Math.pow(qState.getQueueLength(q), alpha);
	}
	return actionValue;
    }
}
