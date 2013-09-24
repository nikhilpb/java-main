package com.moallemi.math.optim;


public class NewtonsMethod {

    /*
    public double minimize(Function f, 
                           double[] x0,
                           double[] lb,
                           double[] ub) 
    {
        int size = function.getDimension();
        if (x0.length != size)
            throw new IllegalArgumentException("initial point has "
                                               + "wrong dimension");

        double[] x = new double [size];
        System.arraycopy(x0, 0, xCurrent, 0, size);
        double[] dx = new double [size];
        double[] gradient = new double [size];
        double[][] invHessian = new double [size][size];
        FletcherLineSearch ls = new FletcherLineSearch();
        Function1D lsF = new Function1D() {
                private double[] x_t = new double [size];
                private double[] gradient_t = new double [size];
                public double evaluate(double t) {
                    for (int i = 0; i < size; i++)
                        xt[i] = x[i] + t * dx[i];
                    return f.evaluate(xt);
                }
                private double evaluateDerivative(double t) {
                    for (int i = 0; i < size; i++)
                        xt[i] = x[i] + t * dx[i];
                    f.evaluateDerivative(xt, gradient_t);
                    double dg = 0.0;
                    for (int i = 0; i < size; i++) 
                        dg += gradient_t[i] * dx[i];
                    return dg;
                }
            };

        for (int iter = 0; iter < maxIter; iter++) {
            double fx = f.evaluate(x);
            f.evaluateGradient(gradient);
            f.evaluateInverseHessian(invHessian);

            // compute the norm of the gradient
            double gradNorm = 0.0;
            for (int i = 0; i < size; i++) 
                gradNorm += gradient[i] * gradient[i];
            gradNorm = Math.sqrt(gradNorm);

            // check to see if we should terminate
            if (gradNorm <= gradTol) {
                // ...
            }

            // compute next direction
            // dx = -invHessian * gradient
            for (int i = 0; i < size; i++) {
                dx[i] = 0.0;
                for (int j = 0; j < size; j++) 
                    dx[i] -= invHessian[i][j] * gradient[i];
            }

            // line search to get the step size

            double alpha = ls.minimize(lsF,
                                       fx,
    */                                       
      
}                                 
           
