package com.moallemi.matching;

import com.moallemi.math.LawlerBipartiteMatcher;
import com.moallemi.util.data.Pair;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 9/19/13
 * Time: 11:17 AM
 * To change this template use File | Settings | File Templates.
 */
public class PHMatchLawler2 {
  private ArrayList<Item> supplySide, demandSide;
  private RewardFunction rewardFun;
  private double[][] w;
  private LawlerBipartiteMatcher biparMatcher;

  /**
   * Constructor.
   *
   * @param supplySide items on the supply side
   * @param demandSide items on the demand side
   * @param rewardFun  to generate matrix w[][]
   */
  public PHMatchLawler2(ArrayList<Item> supplySide,
                        ArrayList<Item> demandSide,
                        RewardFunction rewardFun) {
    //initialize
    this.supplySide = supplySide;
    this.demandSide = demandSide;
    this.rewardFun = rewardFun;

    int n;
    if (supplySide.size() > demandSide.size()) {
      n = supplySide.size();
    } else {
      n = demandSide.size();
    }
    biparMatcher = new LawlerBipartiteMatcher(n);
    w = new double[n][];
    for (int i = 0; i < n; i++) {
      w[i] = new double[n];
      for (int j = 0; j < n; j++) {
        if ((i >= supplySide.size()) || (j >= demandSide.size())) {
          w[i][j] = 0.0;
        } else {
          w[i][j] = this.rewardFun.evaluate(supplySide.get(i), demandSide.get(j));
        }
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

  public ArrayList<Pair<Integer, Integer>> getMatchedPairInds() {
    ArrayList<Pair<Integer, Integer>> out = new ArrayList<Pair<Integer, Integer>>();
    int[] sMatch = biparMatcher.getMatchingSource();
    for (int i = 0; i < supplySide.size(); i++) {
      if (sMatch[i] > -1 && sMatch[i] < demandSide.size()) {
        Pair<Integer, Integer> pair = new Pair<Integer, Integer>(i, sMatch[i]);
        out.add(pair);
      }
    }
    return out;
  }
}
