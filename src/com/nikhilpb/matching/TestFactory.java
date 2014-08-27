package com.nikhilpb.matching;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 9/20/13
 * Time: 10:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class TestFactory {

  public static final int[] kArr1 = {1, 2}, kArr2 = {0, 1}, kArr3 = {3, 3};

  public static ArrayList<Item> fakeItems() {
    Item item1 = new Item(kArr1), item2 = new Item(kArr2), item3 = new Item(kArr3);
    ArrayList<Item> items = new ArrayList<Item>();
    items.add(item1);
    items.add(item2);
    items.add(item3);
    return items;
  }
}
