package com.moallemi.math.optim;

/**
 * A one-dimensional function.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.2 $, $Date: 2009-01-29 16:17:31 $
 */
public interface Function1D {
    /**
     * Evalute the function at a point.
     *
     * @param x the point
     * @return value the value
     */
    public double evaluate(double x);

    /**
     * Evalute the derivative of the function at a point.
     *
     * @param x the point
     * @return value the value
     */
    public double evaluateDerivative(double x);
}