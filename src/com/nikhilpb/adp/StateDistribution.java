package com.nikhilpb.adp;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/12/13
 * Time: 11:11 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class StateDistribution {
    protected Util.DistType distType;
    protected Random random;
    abstract public State nextSample();
    abstract public double expectedValue(StateFunction sf);
}
