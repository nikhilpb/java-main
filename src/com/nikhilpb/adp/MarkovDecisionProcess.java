package com.nikhilpb.adp;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/12/13
 * Time: 11:30 AM
 * To change this template use File | Settings | File Templates.
 */
public interface MarkovDecisionProcess {
    public StateDistribution getDistribution(State state, Action action);
}
