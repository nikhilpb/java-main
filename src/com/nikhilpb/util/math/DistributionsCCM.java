package com.nikhilpb.util.math;

import java.util.Random;

/**
 * Package for sampling from various random distributions.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.6 $, $Date: 2007-11-02 17:13:51 $
 */
public class DistributionsCCM {

  /**
   * Sample a geometric distribution on the non-negative integers.
   *
   * @param random source of randomness
   * @param p      distribution parameter, must be in (0,1)
   * @return sampled value
   */
  public static int nextGeometric(Random random, double p) {
    if (p <= 0.0 || p >= 1.0)
      throw new IllegalArgumentException();
    return
            (int) (Math.floor(Math.log(1.0 - random.nextDouble())
                                      / Math.log(p) - 1.0))
                    + 1;
  }

  /**
   * Sample a geometric distribution on the non-negative integers
   * less than K.
   *
   * @param random source of randomness
   * @param p      distribution parameter, must be in (0,1)
   * @param K      the maximum value (exclusive)
   * @return sampled value
   */
  public static int nextGeometric(Random random, double p, int K) {
    if (p <= 0.0 || p >= 1.0)
      throw new IllegalArgumentException();
    return
            (int) (Math
                           .floor(Math.log(1.0 -
                                                   random.nextDouble()
                                                           * (1.0 - Math.pow(p, K)))
                                          / Math.log(p) - 1.0))
                    + 1;
  }

  /**
   * Sample a geometric distribution on the non-negative integers
   * between Kmin and Kmax.
   *
   * @param random source of randomness
   * @param p      distribution parameter, must be in (0,1)
   * @param Kmin   the minimum value (exclusive)
   * @param Kmax   the maximum value (exclusive)
   * @return sampled value
   */
  public static int nextGeometric(Random random,
                                  double p,
                                  int Kmin,
                                  int Kmax) {
    return nextGeometric(random, p, Kmin - Kmax) + Kmin;
  }


  /**
   * Sample an exponential distribution.
   *
   * @param random source of randomness
   * @param lambda distribution parameter, must be positive
   * @return sampled value
   */
  public static double nextExponential(Random random, double lambda) {
    if (lambda <= 0.0)
      throw new IllegalArgumentException();
    return - Math.log(1.0 - random.nextDouble()) / lambda;
  }

  /**
   * Sample a uniform distribution on (0,1).
   *
   * @param random source of randomness
   * @return sampled value
   */
  public static double nextUniformPositive(Random random) {
    double x;
    do {
      x = random.nextDouble();
    } while (x <= 0.0);
    return x;
  }

  /**
   * Sample a beta(a,b) distribution.
   * <p/>
   * From GNU Scientific library:
   * <p/>
   * The beta distribution has the form
   * p(x) dx = (Gamma(a + b)/(Gamma(a) Gamma(b))) x^(a-1) (1-x)^(b-1) dx
   * The method used here is the one described in Knuth.
   *
   * @param random source of randomness
   * @param a      distribution parameter
   * @param b      distribution parameter
   * @return sampled value
   */
  public static double nextBeta(Random random, double a, double b) {
    double x1 = nextGamma(random, a, 1.0);
    double x2 = nextGamma(random, b, 1.0);
    return x1 / (x1 + x2);
  }


  /**
   * Sample a Gamma(a,b) distribution.
   * <p/>
   * From GNU Scientific library:
   * <p/>
   * The Gamma distribution of order a>0 is defined by:
   * p(x) dx = {1 / \Gamma(a) b^a } x^{a-1} e^{-x/b} dx
   * for x>0.  If X and Y are independent gamma-distributed random
   * variables of order a1 and a2 with the same scale parameter b, then
   * X+Y has gamma distribution of order a1+a2.
   * The algorithms below are from Knuth, vol 2, 2nd ed, p. 129.
   *
   * @param random source of randomness
   * @param a      distribution parameter
   * @param b      distribution parameter
   * @return sampled value
   */
  public static double nextGamma(Random random, double a, double b) {
    if (a <= 0.0 || b <= 0.0)
      throw new IllegalArgumentException("bad Gamma distribution "
                                                 + "parameters");

    int na = (int) Math.floor(a);
    if (a == na)
      return b * gsl_ran_gamma_int(random, na);
    if (na == 0)
      return b * gamma_frac(random, a);

    return b * (gsl_ran_gamma_int(random, na)
                        + gamma_frac(random, a - na));
  }

  // utility methods for Gamma sampling, from GSL

  private static double gsl_ran_gamma_int(Random r, int a) {
    if (a < 12) {
      double prod = 1.0;
      for (int i = 0; i < a; i++)
        prod *= nextUniformPositive(r);

            /* Note: for 12 iterations we are safe against underflow, since
               the smallest positive random number is O(2^-32). This means
               the smallest possible product is 2^(-12*32) = 10^-116 which
               is within the range of double precision. */

      return - Math.log(prod);
    }

    return gamma_large(r, (double) a);
  }


  private static double gamma_large(Random r, double a) {
        /* Works only if a > 1, and is most efficient if a is large
           
           This algorithm, reported in Knuth, is attributed to Ahrens.  A
           faster one, we are told, can be found in: J. H. Ahrens and
           U. Dieter, Computing 12 (1974) 223-246.  */

    double sqa, x, y, v;
    sqa = Math.sqrt(2.0 * a - 1.0);
    do {
      do {
        y = Math.tan(Math.PI * r.nextDouble());
        x = sqa * y + a - 1.0;
      } while (x <= 0.0);
      v = r.nextDouble();
    } while (v > (1.0 + y * y)
                         * Math.exp((a - 1.0) * Math.log(x / (a - 1.0)) - sqa * y));

    return x;
  }

  private static double gamma_frac(Random r, double a) {
        /* This is exercise 16 from Knuth; see page 135, and the solution is
           on page 551.  */

    double p, q, x, u, v;
    p = Math.E / (a + Math.E);
    do {
      u = r.nextDouble();
      v = nextUniformPositive(r);

      if (u < p) {
        x = Math.exp((1.0 / a) * Math.log(v));
        q = Math.exp(- x);
      } else {
        x = 1 - Math.log(v);
        q = Math.exp((a - 1.0) * Math.log(x));
      }
    } while (r.nextDouble() >= q);

    return x;
  }

  /**
   * Sample a binomial distribution.
   * <p/>
   * From GNU Scientific library:
   * <p/>
   * The binomial distribution has the form,
   * prob(k) =  n!/(k!(n-k)!) *  p^k (1-p)^(n-k) for k = 0, 1, ..., n
   * This is the algorithm from Knuth.
   *
   * @param random source of randomness
   * @param n      number of trials
   * @param p      probability of each trial
   * @return sampled value
   */
  public static int nextBinomial(Random random, int n, double p) {
    if (p < 0.0 || p > 1.0 || n < 0)
      throw new IllegalArgumentException("bad binomial parameters");

    int i, a, b, k = 0;

    while (n > 10) {        /* This parameter is tunable */
      double X;
      a = 1 + (n / 2);
      b = 1 + n - a;
      X = nextBeta(random, (double) a, (double) b);

      if (X >= p) {
        n = a - 1;
        p /= X;
      } else {
        k += a;
        n = b - 1;
        p = (p - X) / (1 - X);
      }
    }

    for (i = 0; i < n; i++) {
      if (random.nextDouble() < p)
        k++;
    }

    return k;
  }
}
