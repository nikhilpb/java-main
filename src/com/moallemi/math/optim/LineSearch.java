package com.moallemi.math.optim;

/**
 * Exactly minimize a function using a one-dimensional line
 * search. Uses Brent's method.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.2 $, $Date: 2009-01-29 16:17:31 $
 */
public class LineSearch {
    private double tolX;
    private int maxIter;
    private boolean debug = false;
    
    // machine precision
    private static final double EPSILON = 1e-15;
    private static final double SEPSILON = Math.sqrt(EPSILON);
    // golden ratio
    private static final double GOLDEN = 0.5 * (3.0 - Math.sqrt(5.0));

    /**
     * Constructor.
     */
    public LineSearch() {
        tolX = 1e-6;
        maxIter = 100;
    }

    
    /**
     * Constructor.
     *
     * @param tolX tolerance for optimal x value
     * @param maxIter maximum number of iterations
     */
    public LineSearch(double tolX, int maxIter) {
        this.tolX = tolX;
        this.maxIter = maxIter;
    }

    /**
     * Minimize a function f over the interval (ax,bx).
     *
     * @param f the function
     * @param ax left endpoint of the interval
     * @param bx right endpoint of the interval
     * @return the minimizing value of f
     * @throws IllegalArgumentException if ax > bx
     * @throws IllegalStateException if the algorithm does not converge
     */
    public double minimize(Function1D f, double ax, double bx) {
        if (ax > bx)
            throw new IllegalArgumentException("bounds are inconsisent");

        // start
        double a = ax;
        double b = bx;
        double x = a + GOLDEN * (b - a);
        double fx = f.evaluate(x);
        if (debug)
            System.out.println("x = " + x + " f(x) = " + fx);
        double xf = x;
        double v = x;
        double w = x;
        double fv = fx;
        double fw = fx;
        double d = 0.0;
        double e = 0.0;

        for (int iter = 0; iter < maxIter; iter++) {
            double xm = 0.5 * (a + b);
            double tol1 = SEPSILON * Math.abs(xf) + tolX / 3.0;
            double tol2 = 2.0 * tol1;

            if (Math.abs(xf - xm) <= tol2 - 0.5 * (b - a)) {
                // done!
                return x;
            }
            
            boolean useGoldenSearch = true;

            if (Math.abs(e) > tol1) {
                // parabolic fit
                useGoldenSearch = false;
                double r = (xf-w)*(fx-fv);
                double q = (xf-v)*(fx-fw);
                double p = (xf-v)*q-(xf-w)*r;
                q = 2.0*(q-r);
                if (q > 0.0) 
                    p = -p;
                else
                    q = -q;
                r = e;  
                e = d;

                // is the parabola acceptable
                if (Math.abs(p) < Math.abs(0.5*q*r)
                    && p > q*(a-xf) 
                    && p < q*(b-xf)) {
                    // yes, parabolic interpolation step
                    d = p / q;
                    x = xf + d;

                    // f must not be evaluated too close to ax or bx
                    if (x - a < tol2 || b-x < tol2) {
                        double si;
                        if (xm > xf)
                            si = 1.0;
                        else if (xm < xf)
                            si = -1.0;
                        else
                            si = 1.0;
                        d = tol1 * si;
                    }
                    if (debug) System.out.println("parabolic");
                }
                else {
                    // not acceptable, must do a golden section step
                    useGoldenSearch = true;
                }
            }

            if (useGoldenSearch) {
                // a golden-section step is required
                e = xf >= xm ? a - xf : b - xf;
                d = GOLDEN * e;
                if (debug) System.out.println("golden");
            }
            
            // don't evaluate too close to xf
            double si = d > 0.0 ? 1.0 : (d < 0.0 ? -1.0 : 1.0);
            x = xf + si * Math.max(Math.abs(d), tol1);
            double fu = f.evaluate(x);
            if (debug)
                System.out.println("x = " + x + " f(x) = " + fu);

            if (fu <= fx) {
                if (x >= xf)
                    a = xf;
                else
                    b = xf;
                v = w; fv = fw;
                w = xf; fw = fx;
                xf = x; fx = fu;
            }
            else { // fu > fx
                if (x < xf)
                    a = x; 
                else 
                    b = x;
                if (fu <= fw || w == xf) {
                    v = w; fv = fw;
                    w = x; fw = fu;
                }
                else if (fu <= fv || v == xf || v == w) {
                    v = x; fv = fu;
                }
            }

            xm = 0.5 * (a + b);
            tol1 = SEPSILON * Math.abs(xf) + tolX/3.0;
            tol2 = 2.0*tol1;
        }

        throw new IllegalStateException("line search "
                                        + "did not converge");
    }
        
    public static void main(String[] argv) throws Exception {
        Function1D testF = new Function1D() {
                public double evaluate(double x) {
                    return - Math.log(x) + x;
                }
                public double evaluateDerivative(double x) {
                    return - 1.0 / x + 1.0;
                }
            };
        LineSearch ls = new LineSearch();
        ls.debug = true;
        double x = ls.minimize(testF, 0.0, 10.0);
        System.out.println("min x = " + x);
    }


}