package com.nikhilpb.doe;

import com.sun.tools.javac.util.Pair;

import java.util.Random;

/**
 * Created by nikhilpb on 3/20/14.
 */
public class ABPairGenerator {
  private Random random;
  private int p;

  public ABPairGenerator(long seed, int p) {
    random = new Random(seed);
    this.p = p;
  }

  public ABPair next() {
    ABPair ab = new ABPair();
    ab.alpha = 0.;
    for (int i = 0; i < p; ++ i) {
      double g = random.nextGaussian();
      if (i == 0) {
        ab.beta = g;
      }
      ab.alpha += (g * g);
    }
    return ab;
  }

  public ABPair[] next(int count) {
    ABPair[] abArray = new ABPair[count];
    for (int i = 0; i < count; ++ i) {
      abArray[i] = next();
    }
    return abArray;
  }

  public static class ABPair {
    public double alpha, beta;
  }
}
