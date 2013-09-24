package com.moallemi.matching;

import java.util.*;

public class DualFunctionPolicy implements OnlinePolicy {
  ItemFunction dualFunction;
  RewardFunction rewardFunction;

  public DualFunctionPolicy(ItemFunction dualFunction, RewardFunction rewardFunction) {
    this.dualFunction = dualFunction;
    this.rewardFunction = rewardFunction;
  }

  public int match(ArrayList<Item> remainingSupplyTypes, Item curDem) {
    double max = -Double.MAX_VALUE;
    int out = -1;
    for (int i = 0; i < remainingSupplyTypes.size(); i++) {
      double thisValue = rewardFunction.evaluate(remainingSupplyTypes.get(i), curDem)
          - dualFunction.evaluate(remainingSupplyTypes.get(i));
      if (thisValue > max) {
        max = thisValue;
        out = i;
      }
    }
    return out;
  }
}
