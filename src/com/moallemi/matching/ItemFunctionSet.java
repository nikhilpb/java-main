package com.moallemi.matching;

//import ilog.concert.*;
//import ilog.cplex.*;

import java.util.*;

public class ItemFunctionSet {
  private ArrayList<ItemFunction> functionList;

  public ItemFunctionSet() {
    functionList = new ArrayList<ItemFunction>();
  }

  public void add(ItemFunction tf) {
    functionList.add(tf);
  }

  public int size() {
    return functionList.size();
  }

  public ItemFunction getFunction(int i) {
    return functionList.get(i);
  }

  public String getFunctionName(int i) {
    if ((i >= this.size()) || (i < 0)) {
      return "this function set is not that big";
    } else {
      ItemFunction tf = functionList.get(i);
      return tf.getName();
    }

  }

  public double[] evaluate(Item type) {
    double[] out = new double[this.size()];
    for (int i = 0; i < this.size(); i++) {
      ItemFunction tf = functionList.get(i);
      out[i] = tf.evaluate(type);
    }
    return out;
  }

  //public void addConstraints(IloCplex cplex, IloNumVar[] rVar)
  //   throws IloException;
  public ItemFunction getLinearCombination(double[] r) {
    return new LinearCombinationItemFunction(functionList, r);
  }
}
