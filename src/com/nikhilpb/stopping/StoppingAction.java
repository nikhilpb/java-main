package com.nikhilpb.stopping;


import com.nikhilpb.adp.Action;
import com.nikhilpb.adp.State;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/10/13
 * Time: 3:28 PM
 * To change this template use File | Settings | File Templates.
 */
public enum StoppingAction implements Action {
    STOP, CONTINUE;

    @Override
    public boolean isCompatible(State state) {
        try {
            StoppingState sState = (StoppingState)state;
            if (sState.getStateType() == StoppingState.StateType.NIL && this == CONTINUE) {
                return false;
            }
        } catch (ClassCastException e) {
            return false;
        }
        return true;
    }
}
