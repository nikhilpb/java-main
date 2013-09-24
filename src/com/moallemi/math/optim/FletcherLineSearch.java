package com.moallemi.math.optim;

/**
 * Approximately minimize a function using a one-dimensional line
 * search. Uses Fletcher's method. This should only be used as a
 * subroutine in global optimization.
 *
 * References:
 * R. Fletcher, Practical Methods of Optimization, John Wiley & Sons, 1987,
 * second edition, section 2.6.
 *
 * M. Al-Baali and R. Fletcher, An Efficient Line Search for Nonlinear Least 
 * Squares, Journal of Optimization Theory and Applications, 1986, Volume 48, 
 * Number 3, pages 359-377.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.1 $, $Date: 2009-01-29 16:17:31 $
 */
public class FletcherLineSearch {

    // we are looking for alpha such that
    // f(alpha) <= f(0) + rho f'(0)
    // |f(alpha)| <= -sigma f'(0)
    private double rho = 0.01;
    private double sigma = 0.9;
    // factor to expand the current bracket
    private double tau1 = 9;
    private double tau2 = Math.min(0.1, sigma);
    private double tau3 = 0.5;

    // machine precision
    private static final double EPSILON = 1e-15;

    private static final int BRACKET_ITERS = 100;
    private static final int SECTION_ITERS = 100;

    /**
     * Approximately minimize a function f over the interval.
     *
     * @param f the function
     * @param f0 the value f(0)
     * @param fp0 the value f'(0)
     * @param alpha the initial step to consider
     * @return the minimizing value of the argument
     * @throws IllegalStateException if the algorithm does not converge
     */
    public double minimize(Function1D f, double f0, double fp0, double alpha) {
        int iter = 0;
        double alpha_prev = 0.0;
        double a = 0.0;
        double b = alpha;
        double fa = f0;
        double fb = 0.0;
        double fpa = fp0;
        double fpb = 0.0;
        double falpha_prev = f0;
        double fpalpha_prev = fp0;

        // bracketing phase
        while (iter++ < BRACKET_ITERS) {
            double falpha = f.evaluate(alpha);

            // Fletcher's rho test
            if (falpha > f0 + alpha * rho * fp0 || falpha >= falpha_prev) {
                a = alpha_prev; fa = falpha_prev; fpa = fpalpha_prev;
                b = alpha; fb = falpha; fpb = Double.NaN;
                break; // goto sectioning
            }

            double fpalpha = f.evaluateDerivative(alpha);

            // Fletcher's sigma test
            if (Math.abs(fpalpha) <= -sigma * fp0)
                return alpha; // success


            if (fpalpha >= 0.0) {
                a = alpha; fa = falpha; fpa = fpalpha;
                b = alpha_prev; fb = falpha_prev; fpb = fpalpha_prev;
                break; // goto sectioning 
            }

            double delta = alpha - alpha_prev;
            double lower = alpha + delta;
            double upper = alpha + tau1 * delta;

            double alpha_next = interpolate(alpha_prev, 
                                            falpha_prev, 
                                            fpalpha_prev,
                                            alpha, 
                                            falpha, 
                                            fpalpha, 
                                            lower, 
                                            upper);


            alpha_prev = alpha;
            falpha_prev = falpha;
            fpalpha_prev = fpalpha;
            alpha = alpha_next;
        }
         
        // sectioning phase
        while (iter++ < SECTION_ITERS) {
            double delta = b - a;

            double lower = a + tau2 * delta;
            double upper = b - tau3 * delta;
            alpha = interpolate (a, fa, fpa, b, fb, fpb, lower, upper);
            double falpha = f.evaluate(alpha);
      
            if ((a-alpha)*fpa <= EPSILON) 
                throw new IllegalStateException("roundoff error");

            if (falpha > f0 + rho * alpha * fp0 || falpha >= fa) {
                /*  a_next = a; */
                b = alpha; fb = falpha; fpb = Double.NaN;
            }
            else {
                double fpalpha = f.evaluateDerivative(alpha);
          
                if (Math.abs(fpalpha) <= -sigma * fp0)
                    return alpha;
          
                if ((b >= a && fpalpha >= 0.0) 
                    || (b <= a && fpalpha <= 0)) {
                    b = a; fb = fa; fpb = fpa;
                    a = alpha; fa = falpha; fpa = fpalpha;
                }
                else {
                    a = alpha; fa = falpha; fpa = fpalpha;
                }
            }
        }

        throw new IllegalStateException("line search did not converge");
    }

    // utility functions for interpolation

    // minimize f by interpolating a polynomial over [a,b]
    private static final double interpolate(double a, 
                                            double fa,
                                            double fpa,
                                            double b, 
                                            double fb,
                                            double fpb,
                                            double xmin,
                                            double xmax)
    {
        // Map [a,b] to [0,1] 
        double zmin = (xmin - a) / (b - a);
        double zmax = (xmax - a) / (b - a);

        if (zmin > zmax) {
            double tmp = zmin;
            zmin = zmax;
            zmax = tmp;
        }
  
        double z;

        if (!Double.isNaN(fpb)) 
            z = interp_cubic(fa, 
                             fpa * (b - a), 
                             fb, 
                             fpb * (b - a), 
                             zmin,
                             zmax);
        else
            z = interp_quad(fa, fpa * (b - a), fb, zmin, zmax);

        return a + z * (b - a);
    }

    // use cubic interpolation
    private static final double interp_cubic(double f0,
                                             double fp0,
                                             double f1,
                                             double fp1,
                                             double zl, 
                                             double zh)
    {
        double eta = 3.0 * (f1 - f0) - 2.0 * fp0 - fp1;
        double xi = fp0 + fp1 - 2 * (f1 - f0);
        double c0 = f0;
        double c1 = fp0;
        double c2 = eta;
        double c3 = xi;

        double z, f;

        // try the lower extremum
        double zmin = zl;
        double fmin = c0 + zl * (c1 + zl * (c2 + zl * c3));

        // and the higher extremum
        z = zh;
        f = c0 + z * (c1 + z * (c2 + z * c3));
        if (f < fmin) { zmin = z; fmin = f; }

        // now try to solve the quadratic first order condition
        double a = 3.0 * c3;
        double b = 2.0 * c2;
        double c = c1;

        // handle the linear case
        if (a == 0.0) {
            if (b == 0.0)
                return zmin; // no solutions
            else { 
                // one solution
                z = - c / b;

                if (z > zl && z < zh) {
                    f = c0 + z * (c1 + z * (c2 + z * c3));
                    if (f < fmin) { zmin = z; fmin = f; }
                }

                return zmin; 
            }
        }

        double disc = b*b - 4.0*a*c;
        
        if (disc > 0.0) {
            if (b == 0.0) {
                double r = Math.abs (0.5 * Math.sqrt(disc) / a);

                z = r;
                if (z > zl && z < zh) {
                    f = c0 + z * (c1 + z * (c2 + z * c3));
                    if (f < fmin) { zmin = z; fmin = f; }
                }

                z = -r;
                if (z > zl && z < zh) {
                    f = c0 + z * (c1 + z * (c2 + z * c3));
                    if (f < fmin) { zmin = z; fmin = f; }
                }
            }
            else {
                double sgnb = (b > 0.0 ? 1.0 : -1.0);
                double temp = -0.5 * (b + sgnb * Math.sqrt(disc));
                double r1 = temp / a ;
                double r2 = c / temp ;

                z = r1;
                if (z > zl && z < zh) {
                    f = c0 + z * (c1 + z * (c2 + z * c3));
                    if (f < fmin) { zmin = z; fmin = f; }
                }

                z = r2;
                if (z > zl && z < zh) {
                    f = c0 + z * (c1 + z * (c2 + z * c3));
                    if (f < fmin) { zmin = z; fmin = f; }
                }
            }
        }
        else if (disc == 0.0) {
            double r = -0.5 * b / a ;
            
            z = r;
            if (z > zl && z < zh) {
                f = c0 + z * (c1 + z * (c2 + z * c3));
                if (f < fmin) { zmin = z; fmin = f; }
            }
        }


        return zmin;
    }


    // use quadratic interpolation
    private static final double interp_quad(double f0,
                                            double fp0,
                                            double f1,
                                            double zl, 
                                            double zh)
    {
        double fl = f0 + zl*(fp0 + zl*(f1 - f0 -fp0));
        double fh = f0 + zh*(fp0 + zh*(f1 - f0 -fp0));
        double c = 2.0 * (f1 - f0 - fp0); // curvature
        
        double zmin = zl;
        double fmin = fl;

        if (fh < fmin) { zmin = zh; fmin = fh; } 

        if (c > 0.0)  { // positive curvature required for a minimum
            double z = -fp0 / c;      /* location of minimum */
            if (z > zl && z < zh) {
                double f = f0 + z*(fp0 + z*(f1 - f0 -fp0));
                if (f < fmin) { zmin = z; fmin = f; };
            }
        }

        return zmin;
    }


}