package com.nikhilpb.doe;

import java.util.Random;

/**
 * Created by nikhilpb on 8/26/14.
 */
public class RandomPolicy implements Policy {
  private Random random;

  public RandomPolicy(long seed) {
    random = new Random(seed);
  }

  @Override
  public int getAction(double[] state, int diff, DataPoint nextPoint) {
    boolean rndBool = random.nextBoolean();
    if (rndBool) {
      return 1;
    } else {
      return - 1;
    }
  }
}