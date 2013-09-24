package com.moallemi.math.optim;

/**
 * A multivariable function.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.1 $, $Date: 2009-01-29 16:17:31 $
 */
public interface Function {
    /**
     * Get the dimension of the function.
     *
     * @return the dimension
     */
    public int getDimension();

    /**
     * Evalute the function at a point.
     *
     * @param x the point
     * @return value the value
     */
    public double evaluate(double[] x);

    /**
     * Evalute the function, the gradient, and the inverse Hessian at
     * a point.
     *
     * @param x the point
     * @param gradient a vector to hold the gradient
     * @param invHessian a matrix to hold the inverse Hessian
     * @return value the value
     */
    public double evaluate(double[] x, 
                           double[] gradient, 
                           double[][] invHessian);
}