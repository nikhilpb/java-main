package com.nikhilpb.stopping;

import com.nikhilpb.adp.State;
import com.nikhilpb.adp.StateDistribution;
import com.nikhilpb.adp.StateFunction;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/15/13
 * Time: 12:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class NilDistribution extends StateDistribution {
    private static NilDistribution NILDIST = new NilDistribution();
    private NilDistribution() { }
    public NilDistribution get() { return NILDIST; }

    public State nextSample() {
        return StoppingState.Nil.get();
    }

    public double expectedValue(StateFunction sf) {
        return 0.0;
    }
}
