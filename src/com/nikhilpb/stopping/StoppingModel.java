package com.nikhilpb.stopping;

import com.nikhilpb.adp.Action;
import com.nikhilpb.adp.MarkovDecisionProcess;
import com.nikhilpb.adp.State;
import com.nikhilpb.adp.StateDistribution;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/13/13
 * Time: 8:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class StoppingModel implements MarkovDecisionProcess {

    private GaussianDistribution gDist;

    public StoppingModel() {
        gDist = new GaussianDistribution();
    }

    public StateDistribution getDistribution(State state, Action action) {
        return gDist;
    }
}
