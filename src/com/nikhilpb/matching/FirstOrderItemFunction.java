package com.nikhilpb.matching;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 9/20/13
 * Time: 10:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class FirstOrderItemFunction implements ItemFunction {
    private int dim;
    private int level;

    public FirstOrderItemFunction(int dim, int level) {
        this.dim = dim;
        this.level = level;
    }

    public double evaluate(Item type) {
        if (dim >= type.getDimensions()) {
            System.out.println("invalid function for this type");
            return -1.0;
        } else if (level == type.getTypeAtDimension(dim)) {
            return 1.0;
        }
        return 0.0;
    }

    public String toString() {
        return ("First order item function, dimensions: " + dim + ", Level:" + level);
    }
}