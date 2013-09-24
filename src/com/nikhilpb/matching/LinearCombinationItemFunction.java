package com.nikhilpb.matching;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 9/20/13
 * Time: 10:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class LinearCombinationItemFunction implements ItemFunction {
    private ArrayList<ItemFunction> functionList;
    private double[] r;

    public LinearCombinationItemFunction(ArrayList<ItemFunction> functionList, double[] r) {
        if (functionList.size() == r.length) {
            this.functionList = functionList;
            this.r = r;
        } else {
            System.err.println("Dimensions of function list and r don't match");
        }
    }

    public double evaluate(Item type) {
        double out = 0.0;
        for (int i = 0; i < r.length; i++) {
            out += r[i] * (functionList.get(i)).evaluate(type);
        }
        return out;
    }

    public String toString() {
        String name = "";
        for (int i = 0; i < r.length; i++) {
            name += (r[i] + "*f_" + i + " ");
            if (i < r.length - 1) {
                name += "+ ";
            }
        }
        return name;
    }
}
