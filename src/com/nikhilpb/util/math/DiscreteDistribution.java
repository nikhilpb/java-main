package com.nikhilpb.util.math;

import java.util.Arrays;
import java.util.Random;

/**
 * Package for sampling from discrete distributions.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.3 $, $Date: 2005-01-28 02:42:51 $
 */
public class DiscreteDistribution {
  private double[] probability;
  private double[] cumulative;

  /**
   * Constructor.
   *
   * @param probability the probability vector
   * @throws IllegalArgumentException if it is not a probability vector
   */
  public DiscreteDistribution(double[] probability) {
    this.probability = probability;
    for (int i = 0; i < probability.length; i++) {
      if (probability[i] < 0.0 || probability[i] > 1.0)
        throw new IllegalArgumentException("not a probability "
                                                   + "distribution: "
                                                   + "probability out "
                                                   + "of range "
                                                   + probability[i]);
    }

    cumulative = new double[probability.length];
    cumulative[0] = probability[0];
    for (int i = 1; i < cumulative.length; i++)
      cumulative[i] = cumulative[i - 1] + probability[i];

    if (Math.abs(cumulative[cumulative.length - 1] - 1.0) > EPSILON)
      throw new IllegalArgumentException("not a probability "
                                                 + "distribution: "
                                                 + "sum is "
                                                 + cumulative[cumulative
                                                                      .length - 1]);

  }

  /**
   * Get the size of the distribution.
   *
   * @return the size
   */
  public int size() {
    return probability.length;
  }

  /**
   * Get the probability of an index.
   *
   * @param i the index
   * @return the probability
   */
  public double getProbability(int i) {
    return probability[i];
  }

  /**
   * Sample according to the distribution.
   *
   * @param random randomness source
   * @return the sampled state
   */
  public int nextSample(Random random) {
    double rv = random.nextDouble();
    int index = Arrays.binarySearch(cumulative, rv);
    int value;
    if (index >= 0) {
      value = index + 1;
      // deal with zero probability entries
      while (value > 0 && probability[value] == 0.0)
        value--;
    } else
      value = - (index + 1);
    if (value < 0 || value > probability.length)
      throw new IllegalStateException("bad sample index");

    return value;
  }

  private static final double EPSILON = 1.0e-10;

  /**
   * Does this array represent a distribution?
   *
   * @param x the array
   * @return <code>true</code> if all entries are positive and sum to 1,
   * <code>false</code> otherwise
   */
  public static boolean isDistribution(double[] x) {
    double sum = 0.0;
    for (int i = 0; i < x.length; i++) {
      if (x[i] < 0.0 || x[i] > 1.0)
        return false;
      sum += x[i];
    }
    return Math.abs(sum - 1.0) < EPSILON;
  }
}
