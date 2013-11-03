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
    private final BasisSet basisSet;
    private final double[][] coeffs;
    private ArrayList<StateFunction> contValues;

    public BasisQFunction(BasisSet basisSet, double[][] coeffs) {
        this.basisSet = basisSet;
        this.coeffs = coeffs;
        contValues = new ArrayList<StateFunction>();
        for (int i = 0; i < coeffs.length; ++i) {
            contValues.add(new LinCombStateFunction(coeffs[i], basisSet));
        }
    }

    @Override
    public double value(State state, Action action) {
        StoppingAction stoppingAction = (StoppingAction)action;
        if (stoppingAction == StoppingAction.STOP) {
            return 0.;
        }
        StoppingState stoppingState = (StoppingState)state;
        if (stoppingState.time >= coeffs.length) {
            throw new RuntimeException("time " +
                                       stoppingState.time +
                                       " exceeds the supplied coeffients");
        }
        return contValues.get(stoppingState.time).value(state);
    }
}
