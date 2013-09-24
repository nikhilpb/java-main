package com.moallemi.matching;

import java.util.*;

import ilog.concert.*;
import ilog.cplex.*;

import com.moallemi.util.data.*;
import com.moallemi.math.*;

/**
 * Solves the ALP for the `online' version of the matching problem.
 *
 * @author Nikhil Bhat
 * @version $Revision: 0.1 $, $Date: 2012-09-20$
 */

public class AlpSolverOnline implements MatchingSolver {
  private ArrayList<Item> sampledSupplyTypes, sampledDemandTypes;
  private ArrayList<Pair<Item, Item>> sampledPairs;
  private ItemFunctionSet basisSetSupply, basisSetDemand;
  private RewardFunction rewardFun;

  // cplex stuff
  IloCplex cplex;
  IloNumVar[] kappaOneVar, kappaTwoVar;
  IloRange[] alpConst;

  /**
   * Constructor.
   *
   * @param factory              stores the cplex preferences
   * @param sampledSupplyTypesIn sampled supply side
   * @param sampledDemandTypesIn sampled demand side
   * @param sampledPairsIn       constraints sampled
   * @param basisSetSupply       basis set on the supply side
   * @param basisSetDemand       basis set of the demand side
   * @param rewardFunIn          reward function used
   */
  public AlpSolverOnline(CplexFactory factory,
                         ArrayList<Item> sampledSupplyTypesIn,
                         ArrayList<Item> sampledDemandTypesIn,
                         ArrayList<Pair<Item, Item>> sampledPairsIn,
                         ItemFunctionSet basisSetSupply,
                         ItemFunctionSet basisSetDemand,
                         RewardFunction rewardFunIn)
      throws IloException {
    //initilize
    this.sampledSupplyTypes = sampledSupplyTypesIn;
    this.sampledDemandTypes = sampledDemandTypesIn;
    this.sampledPairs = sampledPairsIn;
    this.basisSetSupply = basisSetSupply;
    this.basisSetDemand = basisSetDemand;
    this.rewardFun = rewardFunIn;
    cplex = factory.getCplex();
    cplex.setParam(IloCplex.IntParam.PreDual, 1);
    //set up cplex stuff
    int basisCountSupply = basisSetSupply.size();
    int basisCountDemand = basisSetDemand.size();
    double[] lbS = new double[basisCountSupply];
    double[] ubS = new double[basisCountSupply];
    double[] lbD = new double[basisCountDemand];
    double[] ubD = new double[basisCountDemand];
    double[] objCoeffTwo = new double[basisCountDemand];
    double[] objCoeffOne = new double[basisCountSupply];
    for (int i = 0; i < basisCountSupply; i++) {
      lbS[i] = -Double.MAX_VALUE;
      ubS[i] = Double.MAX_VALUE;
      objCoeffOne[i] = 0.0;
    }
    for (int i = 0; i < basisCountDemand; i++) {
      lbD[i] = -Double.MAX_VALUE;
      ubD[i] = Double.MAX_VALUE;
      objCoeffTwo[i] = 0.0;
    }
    kappaOneVar = cplex.numVarArray(basisCountSupply, lbS, ubS);
    kappaTwoVar = cplex.numVarArray(basisCountDemand, lbD, ubD);

    int nS = sampledSupplyTypes.size();
    int nD = sampledDemandTypes.size();
    for (int i = 0; i < nS; i++) {
      Item type = sampledSupplyTypes.get(i);
      double[] basisEval = basisSetSupply.evaluate(type);
      for (int j = 0; j < basisCountSupply; j++) {
        objCoeffOne[j] += (basisEval[j]) / nS;
      }
    }

    IloLinearNumExpr obj = cplex.scalProd(objCoeffOne, kappaOneVar);

    for (int i = 0; i < nD; i++) {
      Item type = sampledDemandTypes.get(i);
      double[] basisEval = basisSetDemand.evaluate(type);
      for (int j = 0; j < basisCountDemand; j++) {
        objCoeffTwo[j] += (basisEval[j]) / nD;
      }
    }

    obj.addTerms(objCoeffTwo, kappaTwoVar);
    cplex.addMinimize(obj);

    int nP = sampledPairs.size();
    alpConst = new IloRange[nP];
    IloLinearNumExpr tempExp;
    for (int p = 0; p < nP; p++) {
      Item typeS = sampledPairs.get(p).getFirst();
      Item typeD = sampledPairs.get(p).getSecond();
      tempExp = cplex.scalProd(basisSetSupply.evaluate(typeS), kappaOneVar);
      tempExp.addTerms(basisSetDemand.evaluate(typeD), kappaTwoVar);
      alpConst[p] = cplex.addGe(tempExp, rewardFun.evaluate(typeS, typeD));
    }
  }

  /**
   * Solves the current instance of the ALP.
   *
   * @return `true' if the solve was successful
   */
  public boolean solve() throws IloException {
    boolean status = cplex.solve();
    System.out.println("objective = " + cplex.getObjValue());
    return status;
  }

  /**
   * @return returns the supply value function in the form of $\ip{\kappa_1}{\alpha}$
   */
  public ItemFunction getSupplyValue() throws IloException {
    double[] kappaOne = cplex.getValues(kappaOneVar);
    return basisSetSupply.getLinearCombination(kappaOne);
  }

  /**
   * @return returns the demand value function in the form of $\ip{\kappa_2}{\beta}$
   */
  public ItemFunction getDemandValue() throws IloException {
    double[] kappaTwo = cplex.getValues(kappaTwoVar);
    return basisSetDemand.getLinearCombination(kappaTwo);
  }

}
