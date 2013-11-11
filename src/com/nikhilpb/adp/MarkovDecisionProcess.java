package com.nikhilpb.adp;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/12/13
 * Time: 11:30 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class MarkovDecisionProcess {
    protected RewardFunction rewardFunction;
    protected double alpha;
    protected boolean isInfHorizon;

    public abstract State getBaseState();
    public abstract StateDistribution getDistribution(State state, Action action);
    public abstract void reset(long seed);

    public RewardFunction getRewardFunction() {
        return rewardFunction;
    }

    public double getAlpha() {
        return alpha;
    }

    public boolean isInfHorizon() {
        return isInfHorizon;
    }
}
