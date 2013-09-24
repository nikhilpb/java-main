package com.moallemi.matching;

import java.util.*;

import Jama.Matrix;

import com.moallemi.math.CplexFactory;

import ilog.concert.*;

public class Regression {
  IoGMSet instancesOfGM;
  ItemFunction supplyDualFunction, demandDualFunction;

  public Regression(IoGMSet instancesOfGM,
                    ItemFunctionSet basisSetSupply,
                    ItemFunctionSet basisSetDemand,
                    CplexFactory factory)
      throws IloException {
    this.instancesOfGM = instancesOfGM;
    InstanceOfGeneralMatching instance;
    int instanceCount = instancesOfGM.size();

    ArrayList<Item> supplyItems = new ArrayList<Item>();
    ArrayList<Item> demandItems = new ArrayList<Item>();
    ArrayList<Double> supplyValues = new ArrayList<Double>();
    ArrayList<Double> demandValues = new ArrayList<Double>();

    for (int i = 0; i < instanceCount; i++) {
      instance = instancesOfGM.get(i);
      supplyItems.addAll(instance.getSupplyItems());
      demandItems.addAll(instance.getDemandItems());
      double[] sValues = instance.getSupplyValues(factory);
      double[] dValues = instance.getDemandValues(factory);
      for (int s = 0; s < sValues.length; s++) {
        supplyValues.add(sValues[s]);
      }
      for (int d = 0; d < dValues.length; d++) {
        demandValues.add(dValues[d]);
      }
    }
    int supplyTypeCount = supplyValues.size();
    int demandTypeCount = demandValues.size();

    System.out.println("Total supply types = " + supplyTypeCount + " or " + supplyItems.size());
    System.out.println("Total demand types = " + demandTypeCount + " or " + demandItems.size());

    double[][] sXArray = new double[supplyTypeCount][];
    double[] sYArray = new double[supplyTypeCount];
    for (int i = 0; i < supplyTypeCount; i++) {
      sYArray[i] = supplyValues.get(i);
      sXArray[i] = basisSetSupply.evaluate(supplyItems.get(i));
    }
    Matrix sX = new Matrix(sXArray);
    Matrix sY = new Matrix(sYArray, supplyTypeCount);
    Matrix sW = sX.solve(sY);
    double[][] sWArrayTemp = sW.getArray();
    double[] sWArray = new double[basisSetSupply.size()];
    for (int i = 0; i < basisSetSupply.size(); i++) {
      sWArray[i] = sWArrayTemp[i][0];
    }
    supplyDualFunction = basisSetSupply.getLinearCombination(sWArray);

    double[][] dXArray = new double[demandTypeCount][];
    double[] dYArray = new double[demandTypeCount];
    for (int i = 0; i < demandTypeCount; i++) {
      dXArray[i] = basisSetDemand.evaluate(demandItems.get(i));
      dYArray[i] = demandValues.get(i);
    }
    Matrix dX = new Matrix(dXArray);
    Matrix dY = new Matrix(dYArray, demandTypeCount);
    Matrix dW = dX.solve(dY);
    double[][] dWArrayTemp = dW.getArray();
    double[] dWArray = new double[basisSetDemand.size()];
    for (int i = 0; i < basisSetDemand.size(); i++) {
      dWArray[i] = dWArrayTemp[i][0];
    }
    demandDualFunction = basisSetDemand.getLinearCombination(dWArray);
  }

  public ItemFunction getSupplyDualFunction() {
    return supplyDualFunction;
  }

  public ItemFunction getDemandDualFunction() {
    return demandDualFunction;
  }
}
