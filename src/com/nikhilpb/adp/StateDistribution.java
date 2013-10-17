package com.nikhilpb.adp;

import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/12/13
 * Time: 11:11 AM
 * To change this template use File | Settings | File Templates.
 */
public interface StateDistribution {
    public State nextSample();
    public double expectedValue(StateFunction sf);
}
