package com.nikhilpb.stopping;

import com.nikhilpb.adp.State;
import com.nikhilpb.adp.StateFunction;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/29/13
 * Time: 8:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConstantStateFunction implements StateFunction {
    private double val;

    public ConstantStateFunction(double val) { this.val = val; }

    @Override
    public double value(State state) {
        return val;
    }

    @Override
    public String toString() {
       return  "const_" + val;
    }
}
