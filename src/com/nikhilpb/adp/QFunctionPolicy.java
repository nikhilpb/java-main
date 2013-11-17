package com.nikhilpb.adp;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 11/3/13
 * Time: 1:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class QFunctionPolicy implements Policy {
    private MarkovDecisionProcess model;
    private QFunction qFunction;
    private RewardFunction rewardFunction;
    private double alpha;

    public QFunctionPolicy(MarkovDecisionProcess model,
                           QFunction qFunction,
                           RewardFunction rewardFunction,
                           double alpha) {
        this.model = model;
        this.qFunction = qFunction;
        this.rewardFunction = rewardFunction;
        this.alpha = alpha;
    }

    @Override
    public Action getAction(State state) {
        ArrayList<Action> actions = state.getActions();
        System.out.println(state.toString() + " " + actions.toString());
        Action maxAction = null;
        double value, maxValue = Double.MIN_VALUE;
        for (Action a : actions) {
            value = rewardFunction.value(state, a) + alpha * qFunction.value(state, a);
            if (value > maxValue) {
                maxValue = value;
                maxAction = a;
            }
        }
        return maxAction;
    }
}
