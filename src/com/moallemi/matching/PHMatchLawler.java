package com.moallemi.matching;

import java.util.*;

import com.moallemi.math.*;
import com.moallemi.util.data.*;

/**
 * A wrapper class to find the weighted bipartite matching using the LawlerBipartiteMatcher class.
 *
 * @author Nikhil Bhat
 * @version $Revision: 0.1 $, $Date: 2012-09-20 $
 */

public class PHMatchLawler {
  private ArrayList<Item> supplySide, demandSide;
  private RewardFunction rewardFun;

  double[][] w;
  LawlerBipartiteMatcher biparMatcher;

  /**
   * Constructor.
   *
   * @param supplySide items on the supply side
   * @param demandSide items on the demand side
   * @param rewardFun  to generate matrix w[][]
   */
  public PHMatchLawler(ArrayList<Item> supplySide,
                       ArrayList<Item> demandSide,
                       RewardFunction rewardFun) {
    if (supplySide.size() != demandSide.size()) {
      throw new RuntimeException("supply and demand sizes don't match");
    }
    //initialize
    this.supplySide = supplySide;
    this.demandSide = demandSide;
    this.rewardFun = rewardFun;

    int n = supplySide.size();
    biparMatcher = new LawlerBipartiteMatcher(n);
    w = new double[n][];
    for (int i = 0; i < n; i++) {
      w[i] = new double[n];
      for (int j = 0; j < n; j++) {
        w[i][j] = this.rewardFun.evaluate(supplySide.get(i), demandSide.get(j));
      }
    }
  }

  /**
   * Solves the bipartite matching problem.
   *
   * @return the value of the optimal matching
   */
  public double solve() {
    biparMatcher.computeMax(w);
    return biparMatcher.getMatchingWeight();
  }

  /**
   * @return ArrayList of Pair's of indices matched
   */
  public ArrayList<Pair<Integer, Integer>> getMatchedPairInds() {
    ArrayList<Pair<Integer, Integer>> out = new ArrayList<Pair<Integer, Integer>>();
    int[] sMatch = biparMatcher.getMatchingSource();
    for (int i = 0; i < supplySide.size(); i++) {
      if (sMatch[i] > -1) {
        Pair<Integer, Integer> pair = new Pair<Integer, Integer>(i, sMatch[i]);
        out.add(pair);
      }
    }
    return out;
  }

  /**
   * @return ArrayList of Pair's of items matched
   */
  public ArrayList<Pair<Item, Item>> getMatchedPairs() {
    ArrayList<Pair<Item, Item>> out = new ArrayList<Pair<Item, Item>>();
    int[] sMatch = biparMatcher.getMatchingSource();
    for (int i = 0; i < supplySide.size(); i++) {
      if (sMatch[i] > -1) {
        Pair<Item, Item> pair = new Pair<Item, Item>(supplySide.get(i), demandSide.get(sMatch[i]));
        out.add(pair);
      }
    }
    return out;
  }

  public ArrayList<Pair<Item, Item>> getSalpPairs() {
    ArrayList<Pair<Item, Item>> out = new ArrayList<Pair<Item, Item>>();
    int[] dMatch = biparMatcher.getMatchingDest();
    boolean[] matched = new boolean[supplySide.size()];
    Arrays.fill(matched, false);
    for (int i = 0; i < supplySide.size(); i++) {
      if (dMatch[i] > -1) {
        matched[i] = true;
      }
      for (int s = 0; s < supplySide.size(); s++) {
        if (!matched[s]) {
          Item sItem = supplySide.get(s);
          Item dItem = demandSide.get(i);
          out.add(new Pair<Item, Item>(sItem, dItem));
        }
      }
    }
    return out;
  }
}
