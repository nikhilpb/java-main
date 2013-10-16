package com.nikhilpb.stopping;

import com.nikhilpb.adp.State;
import com.nikhilpb.adp.StateFunction;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/15/13
 * Time: 2:41 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class StoppingStateFunction implements StateFunction {
    public double value(State state) {
        StoppingState stopState = (StoppingState)state;
        if (stopState.getStateType() == StoppingState.StateType.NIL) {
            return 0.0;
        }
        return valueVector((StoppingState.Vector) stopState);
    }

    abstract double valueVector(StoppingState.Vector vec);
}
