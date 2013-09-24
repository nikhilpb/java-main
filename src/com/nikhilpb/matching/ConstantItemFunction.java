package com.nikhilpb.matching;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 9/22/13
 * Time: 12:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConstantItemFunction implements ItemFunction {

    private double value;

    public ConstantItemFunction(double value) {
        this.value = value;
    }

    public double evaluate(Item type) {
        return value;
    }

    public String toString() {
        return ("constant function that returns " + value);
    }
}