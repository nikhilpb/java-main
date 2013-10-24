package com.nikhilpb.stopping;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/21/13
 * Time: 9:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class Lambda {
    private double[] vectorS, vectorC, grad;
    private ArrayList<StoppingState> states;

    public Lambda(ArrayList<StoppingState> states) {
        this.states = states;
        init();
    }

    private void init() {
        int len = states.size();
        vectorC = new double[len];
        vectorS = new double[len];
        Arrays.fill(vectorC, 1./len);
    }

    public double[] getVectorS() {
        return vectorS;
    }

    public double[] getVectorC() {
        return vectorC;
    }

    public double[] getGrad() {
        return grad;
    }

    public ArrayList<StoppingState> getStates() {
        return states;
    }

    public void setGrad(double[] grad) {
        this.grad = grad;
    }
}
