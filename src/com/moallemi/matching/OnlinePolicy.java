package com.moallemi.matching;

import java.util.*;

public interface OnlinePolicy {
  public int match(ArrayList<Item> remainingSupplyTypes, Item curDem);
}
