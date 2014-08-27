package com.nikhilpb.matching;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 9/20/13
 * Time: 9:53 AM
 * To change this template use File | Settings | File Templates.
 */
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

  public String toString() {
    String name = "";
    for (int i = 0; i < baseTypes.size(); i++) {
      name = name + " I_(" + baseTypes.get(i).toString() + ") * " + values[i];
      if (i < baseTypes.size() - 1) {
        name += " + ";
      }
    }
    return name;
  }
}
