package com.nikhilpb.stopping;

import com.nikhilpb.adp.StateFunction;
import com.nikhilpb.adp.StateDistribution;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/13/13
 * Time: 8:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class GaussianDistribution extends StateDistribution {

    public double expectedValue(StateFunction s) {
        return 0.0;

    }

    public com.nikhilpb.adp.State nextSample() {
        double[] prices = {0., 0.};
        State pr = new State(prices);
        return pr;
    }

}
