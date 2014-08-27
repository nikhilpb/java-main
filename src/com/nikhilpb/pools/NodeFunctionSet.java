package com.nikhilpb.pools;

import java.util.ArrayList;

public class NodeFunctionSet {
  private ArrayList<NodeFunction> functionList;

  public NodeFunctionSet() {
    functionList = new ArrayList<NodeFunction>();
  }

  public void add(NodeFunction nf) {
    functionList.add(nf);
  }

  public int size() {
    return functionList.size();
  }

  public NodeFunction getFunction(int i) {
    return functionList.get(i);
  }

  public String toString(int i) {
    if ((i >= this.size()) || (i < 0)) {
      return "index out of bound";
    } else {
      NodeFunction nf = functionList.get(i);
      return nf.toString();
    }
  }

  public double[] evaluate(Node node) {
    double[] out = new double[this.size()];
    for (int i = 0; i < this.size(); i++) {
      NodeFunction tf = functionList.get(i);
      out[i] = tf.evaluate(node);
    }
    return out;
  }

  public NodeFunction getLinearCombination(double[] r) {
    return new LinearCombinationNodeFunction(functionList, r);
  }
}
