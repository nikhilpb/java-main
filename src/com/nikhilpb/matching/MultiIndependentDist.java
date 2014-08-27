package com.nikhilpb.matching;

import java.util.Random;

import com.nikhilpb.util.math.DiscreteDistribution;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 9/20/13
 * Time: 12:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class MultiIndependentDist {
  private int dimension;
  private int[] typeCountPerDimension;
  private double[][] probabilities;
  private DiscreteDistribution[] dists;
  private Random random;

  public MultiIndependentDist(double[][] probs, Random rnd) {
    dimension = probs.length;
    typeCountPerDimension = new int[dimension];
    dists = new DiscreteDistribution[dimension];
    probabilities = new double[dimension][];
    for (int i = 0; i < dimension; i++) {
      typeCountPerDimension[i] = probs[i].length;
      probabilities[i] = new double[typeCountPerDimension[i]];
      double weight = 0.0;
      for (int j = 0; j < typeCountPerDimension[i]; j++) {
        probabilities[i][j] = probs[i][j];
        weight += probs[i][j];
      }
      dists[i] = new DiscreteDistribution(probs[i]);
    }
    random = rnd;
  }

  public Integer[] nextSample() {
    Integer[] out = new Integer[dimension];
    for (int i = 0; i < dimension; i++) {
      out[i] = dists[i].nextSample(random);
    }
    return out;
  }

  public Integer[] nextSample(Random rnd) {
    Integer[] out = new Integer[dimension];
    for (int i = 0; i < dimension; i++) {
      out[i] = dists[i].nextSample(rnd);
    }
    return out;
  }

  public double getProb(int i, int j) {
    if (i >= dimension) {
      System.err.println("this distribution does not have this getDimension");
      return 0.0;
    } else {
      if (j >= typeCountPerDimension[i]) {
        System.err.println("this getDimension in the distribution does not have this number of types");
        return 0.0;
      } else {
        return dists[i].getProbability(j);
      }
    }
  }

  public void setRandom(Random rnd) {
    this.random = rnd;
  }
}