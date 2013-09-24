package com.moallemi.matching;

public class SeparableRewardFunction implements RewardFunction {

  public SeparableRewardFunction() {
  }

  public double evaluate(Item item1, Item item2) {

    if (item1.getDimensions() != item2.getDimensions()) {
      System.out.println("types don't match");
      return 0.0;
    }
    double out = 0.0;
    for (int i = 0; i < item1.getDimensions(); i++) {
      if (item1.getTypeAtDimension(i) == item2.getTypeAtDimension(i)) {
        out++;
      }
    }
    return out;
  }
}
