package com.moallemi.matching;

import java.util.*;

public class SimpleItemFunction implements ItemFunction {
  private ArrayList<Item> baseTypes;
  private double[] values;

  public SimpleItemFunction(ArrayList<Item> baseTypes, double[] values) {
    if (baseTypes.size() != values.length) {
      throw new RuntimeException("base types and values not of the same length");
    }
    this.baseTypes = baseTypes;
    this.values = values;

  }

  public double evaluate(Item type) {
    for (int i = 0; i < baseTypes.size(); i++) {
      if (Item.equals(baseTypes.get(i), type)) {
        return values[i];
      }
    }
    return 0.0;
  }

  public String getName() {
    return ("no name");
  }
}
