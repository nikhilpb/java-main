package com.nikhilpb.adp;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/17/13
 * Time: 10:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class BasisSet {
    ArrayList<StateFunction> basis;

    public BasisSet() {
        basis = new ArrayList<StateFunction>();
    }

    public BasisSet(ArrayList<StateFunction> basis) {
        this.basis = basis;
    }

    public void add(StateFunction stateFunction) {
        basis.add(stateFunction);
    }

    public int size() { return basis.size(); }

    public StateFunction get(int i) {
        return basis.get(i);
    }

    public double[] evaluate(State state) {
        double[] value = new double[basis.size()];
        for (int i = 0; i < basis.size(); ++i) {
            value[i] = basis.get(i).value(state);
        }
        return value;
    }

    @Override
    public String toString() {
        String string = "";
        for (int i = 0; i < basis.size(); ++i) {
            string += basis.get(i).toString() + "\n";
        }
        return string;
    }

    public StateFunction getLinComb(double[] coeffs) {
        return new LinCombStateFunction(coeffs, this);
    }

}
