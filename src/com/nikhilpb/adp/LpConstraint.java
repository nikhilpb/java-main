package com.nikhilpb.adp;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/17/13
 * Time: 10:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class LpConstraint {
    private double rhs;
    private double[] lhs;

    public LpConstraint(StateAction stateAction,
                        MarkovDecisionProcess mdp,
                        RewardFunction rewardFunction,
                        BasisSet basisSet,
                        double alpha) {
        State state = stateAction.getState();
        Action action = stateAction.getAction();
        StateDistribution distribution = mdp.getDistribution(state, action);
        rhs =rewardFunction.value(state, action);
        lhs = new double[basisSet.size()];
        for (int i = 0; i < basisSet.size(); ++i) {
            StateFunction basis = basisSet.get(i);
            if (distribution != null) {
                lhs[i] += (basis.value(state) - alpha * distribution.expectedValue(basis));
            } else {
                lhs[i] += basis.value(state);
            }
        }
    }

    public double getRhs() { return rhs; }

    public double[] getLhs() { return lhs; }
}
