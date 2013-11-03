package com.nikhilpb.stopping;

import com.nikhilpb.adp.*;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 11/3/13
 * Time: 2:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class BasisQFunction implements QFunction {
    private ArrayList<StateFunction> contValues;

    public BasisQFunction(ArrayList<StateFunction> contValues) {
        this.contValues = contValues;
    }

    @Override
    public double value(State state, Action action) {
        StoppingAction stoppingAction = (StoppingAction)action;
        if (stoppingAction == StoppingAction.STOP) {
            return 0.;
        }
        StoppingState stoppingState = (StoppingState)state;
        return contValues.get(stoppingState.time).value(state);
    }
}
