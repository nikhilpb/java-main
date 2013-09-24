package com.moallemi.matching;

import java.util.*;

public class LinearCombinationItemFunction implements ItemFunction {
  private ArrayList<ItemFunction> functionList;
  private double[] r;

  public LinearCombinationItemFunction(ArrayList<ItemFunction> functionList, double[] r) {
    if (functionList.size() == r.length) {
      this.functionList = functionList;
      this.r = r;
    } else {
      System.out.println("dimentions of function list and r don't match");
    }
  }

  public double evaluate(Item type) {
    double out = 0.0;
    for (int i = 0; i < r.length; i++) {
      out += r[i] * (functionList.get(i)).evaluate(type);
    }
    return out;
  }

  public String getName() {
    String name = "";
    for (int i = 0; i < r.length; i++) {
      name += (r[i] + "*f_" + i + " ");
      if (i < r.length - 1) {
        name += "+ ";
      }
    }
    return name;
  }
}
