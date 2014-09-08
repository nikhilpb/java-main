package com.nikhilpb.abtesting;

import java.util.Random;

/**
 * Created by nikhilpb on 9/8/14.
 */
public class RandomPolicy implements ABPolicy {
  private Random random;

  public RandomPolicy(long seed) {
    random = new Random(seed);
  }

  /**
   * The action returned is PLUS or MINUS with equal probability.
   * @param state Ignored
   * @param nextPoint Ignored
   * @return
   */
  @Override
  public ABAction getAction(ABState state, DataPoint nextPoint) {
    if (random.nextBoolean()) {
      return ABAction.PLUS;
    } else {
      return ABAction.MINUS;
    }
  }
}
