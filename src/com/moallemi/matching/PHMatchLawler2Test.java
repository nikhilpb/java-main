package com.moallemi.matching;

import com.moallemi.util.data.Pair;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 9/19/13
 * Time: 11:25 AM
 * To change this template use File | Settings | File Templates.
 */
public class PHMatchLawler2Test extends TestCase {
  static final double kTol = 1E-4;

  @Test
  public void testMatch() {
    int[] type1 = {1, 0, 2}, type2 = {1, 1, 3}, type3 = {0, 2, 2};
    Item item1 = new Item(type1), item2 = new Item(type2), item3 = new Item(type3);
    ArrayList<Item> sSide = new ArrayList<Item>(), dSide = new ArrayList<Item>();
    sSide.add(item1); sSide.add(item2);
    dSide.add(item3);
    RewardFunction rf = new SeparableRewardFunction();
    PHMatchLawler2 matcher = new PHMatchLawler2(sSide, dSide, rf);
    double val = matcher.solve();
    double err = Math.abs(val - 1.0);
    assertTrue(err < kTol);
    ArrayList<Pair<Integer, Integer>> matches = matcher.getMatchedPairInds();
    assertEquals(matches.size(), 1);
    Pair<Integer, Integer> m = matches.get(0);
    assertEquals((int) m.getFirst(), 0);
    assertEquals((int) m.getSecond(), 0);
  }
}
