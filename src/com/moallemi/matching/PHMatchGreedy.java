package com.moallemi.matching;

import java.util.*;

import com.moallemi.util.data.*;

public class PHMatchGreedy {
  private ArrayList<Item> supplySide, demandSide;
  private RewardFunction rewardFun;

  public PHMatchGreedy(ArrayList<Item> supplySide,
                       ArrayList<Item> demandSide,
                       RewardFunction rewardFun) {
    this.supplySide = supplySide;
    this.demandSide = demandSide;
    this.rewardFun = rewardFun;

  }

  public ArrayList<Pair<Item, Item>> getMatchedPairs() {
    ArrayList<Pair<Item, Item>> pairs = new ArrayList<Pair<Item, Item>>();
    boolean[] matched = new boolean[supplySide.size()];
    Arrays.fill(matched, false);
    double mx, matchValue;
    int mxInd;
    Item sItem, dItem;
    Pair<Item, Item> thisPair;
    for (int d = 0; d < demandSide.size(); d++) {
      mx = -Double.MAX_VALUE;
      mxInd = -1;
      dItem = demandSide.get(d);
      for (int s = 0; s < supplySide.size(); s++) {
        sItem = supplySide.get(s);
        matchValue = rewardFun.evaluate(sItem, dItem);
        if (!matched[s] && mx <= matchValue) {
          mx = matchValue;
          mxInd = s;
        }
      }
      sItem = supplySide.get(mxInd);
      thisPair = new Pair<Item, Item>(sItem, dItem);
      matched[mxInd] = true;
      pairs.add(thisPair);
    }
    return pairs;
  }


  public ArrayList<Pair<Item, Item>> getSalpPairs() {
    ArrayList<Pair<Item, Item>> pairs = new ArrayList<Pair<Item, Item>>();
    boolean[] matched = new boolean[supplySide.size()];
    Arrays.fill(matched, false);
    double mx, matchValue;
    int mxInd;
    Item sItem, dItem;
    Pair<Item, Item> thisPair;
    for (int d = 0; d < demandSide.size(); d++) {
      mx = -Double.MAX_VALUE;
      mxInd = -1;
      dItem = demandSide.get(d);
      for (int s = 0; s < supplySide.size(); s++) {
        sItem = supplySide.get(s);
        matchValue = rewardFun.evaluate(sItem, dItem);
        if (!matched[s] && mx <= matchValue) {
          mx = matchValue;
          mxInd = s;
        }
      }
      for (int s = 0; s < supplySide.size(); s++) {
        if (!matched[s]) {
          sItem = supplySide.get(s);
          thisPair = new Pair<Item, Item>(sItem, dItem);
          pairs.add(thisPair);
        }
      }
      matched[mxInd] = true;
    }
    return pairs;
  }

}
