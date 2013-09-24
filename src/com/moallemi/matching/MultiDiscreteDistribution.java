package com.moallemi.matching;

import java.util.*;

import com.moallemi.math.*;

public class MultiDiscreteDistribution {
  private int dimension;
  private int[] typeCountPerDimension;
  private double[][] probabilities;
  private DiscreteDistribution[] dists;
  private Random random;

  public MultiDiscreteDistribution(double[][] probs, Random rnd) {
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
      System.out.println("weight on dimension " + i + " is " + weight);
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
      System.out.println("this distribution does not have this dimension");
      return 0.0;
    } else {
      if (j >= typeCountPerDimension[i]) {
        System.out.println("this dimension in the distribution does not have this number of types");
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
