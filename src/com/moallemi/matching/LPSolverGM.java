package com.moallemi.matching;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloRange;
import ilog.cplex.IloCplex;

import java.util.*;

import com.moallemi.math.CplexFactory;
import com.moallemi.util.data.Pair;

public class LPSolverGM implements MatchingSolver {
  private ArrayList<Constraint> constSet;
  private ItemFunctionSet basisSetSupply, basisSetDemand;
  private GeneralMatchingModel model;
  private IoGMSet sampledInstances;
  private double eps;

  // cplex stuff
  IloCplex cplex;
  IloNumVar[] kappaS, kappaD, sVar;
  ArrayList<IloRange> lpConst;

  /**
   * Constructor.
   *
   * @param factory          stores the cplex preferences
   * @param sampledInstances set of matching instances sampled
   * @param basisSetSupply   basis set on the supply side
   * @param basisSetDemand   basis set of the demand side
   * @param model            the GeneralMatchingModel used
   * @param eps         0 if alp, 1 if salp
   */
  public LPSolverGM(CplexFactory factory,
                    IoGMSet sampledInstances,
                    ItemFunctionSet basisSetSupply,
                    ItemFunctionSet basisSetDemand,
                    GeneralMatchingModel model,
                    double eps)
      throws IloException {
    this.sampledInstances = sampledInstances;
    this.basisSetSupply = basisSetSupply;
    this.basisSetDemand = basisSetDemand;
    this.model = model;
    this.eps = eps;

    cplex = factory.getCplex();

    int basisCountSupply = basisSetSupply.size();
    int basisCountDemand = basisSetDemand.size();
    double[] lbS = new double[basisCountSupply];
    double[] ubS = new double[basisCountSupply];
    double[] lbD = new double[basisCountDemand];
    double[] ubD = new double[basisCountDemand];
    Arrays.fill(lbS, -Double.MAX_VALUE);
    Arrays.fill(ubS, Double.MAX_VALUE);
    Arrays.fill(lbD, -Double.MAX_VALUE);
    Arrays.fill(ubD, Double.MAX_VALUE);

    kappaS = cplex.numVarArray(basisCountSupply, lbS, ubS);
    kappaD = cplex.numVarArray(basisCountDemand, lbD, ubD);
    int sSize = 0;

    for (int s = 0; s < this.sampledInstances.size(); s++) {
      InstanceOfGeneralMatching instance = this.sampledInstances.get(s);
      sSize += (instance.getTimePeriods() + 1);
    }

    double[] lbSlack = new double[sSize];
    double[] ubSlack = new double[sSize];
    Arrays.fill(lbSlack, 0.0);
    if (this.eps > 1E8) {
      Arrays.fill(ubSlack, 0.0);
      System.out.println("Solving ALP");
    } else {
      Arrays.fill(ubSlack, Double.MAX_VALUE);
    }
    sVar = cplex.numVarArray(sSize, lbSlack, ubSlack);

    double[] objCoeffS = new double[basisCountSupply];
    double[] objCoeffD = new double[basisCountDemand];
    Arrays.fill(objCoeffS, 0.0);
    Arrays.fill(objCoeffD, 0.0);

    int sampleCount = this.sampledInstances.size();
    InstanceOfGeneralMatching instance;
    int tp;
    ArrayList<Item> state;
    ArrayList<Pair<Item, Item>> pairs;
    Constraint thisConst;
    constSet = new ArrayList<Constraint>();
    double rhs;
    double[] kappaSCoeff, kappaDCoeff;
    IloLinearNumExpr lhs, obj;
    obj = cplex.linearNumExpr();
    lpConst = new ArrayList<IloRange>();
    int acc = 0;
    for (int s = 0; s < sampleCount; s++) {
      instance = this.sampledInstances.get(s);
      tp = instance.getTimePeriods();
      for (int t = 0; t < tp; t++) {
        state = instance.getStates(t);
        pairs = instance.getMatchedPairs(t);
        thisConst = new Constraint(state, pairs, (t == tp));
        constSet.add(thisConst);
        rhs = thisConst.getRhs();
        kappaSCoeff = thisConst.getKappa1Coeff();
        kappaDCoeff = thisConst.getKappa2Coeff();
        lhs = cplex.scalProd(kappaSCoeff, kappaS);
        lhs.addTerms(kappaDCoeff, kappaD);
        lhs.addTerm(sVar[acc + t], 1.0);
        cplex.addGe(lhs, rhs);
        obj.add(lhs);
      }
      acc += (tp + 1);
    }
    double[] epsArray = new double[sSize];
    Arrays.fill(epsArray, this.eps);
    obj.add(cplex.scalProd(epsArray, sVar));
    cplex.addMinimize(obj);
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
    double[] kappaOne = cplex.getValues(kappaS);
    return basisSetSupply.getLinearCombination(kappaOne);
  }

  /**
   * @return returns the demand value function in the form of $\ip{\kappa_2}{\beta}$
   */
  public ItemFunction getDemandValue() throws IloException {
    double[] kappaTwo = cplex.getValues(kappaD);
    return basisSetDemand.getLinearCombination(kappaTwo);
  }

  /**
   * Stores the constraint in the form given in the paper.
   */
  protected class Constraint {
    private double[] coeffKappaS, coeffKappaD;
    private double rhs;

    /**
     * Constructor
     *
     * @param basisSetSupply set of basis functions on the supply side
     * @param basisSetDemand set of basis functions on the demand side
     * @param model          the model used
     * @param items          list of items in this state
     * @param matches        list of items matched
     */
    public Constraint(ArrayList<Item> items,
                      ArrayList<Pair<Item, Item>> matches,
                      boolean lastOne) {
      rhs = 0.0;
      for (int i = 0; i < matches.size(); i++) {
        rhs += model.getRewardFunction().evaluate(matches.get(i).getFirst(),
            matches.get(i).getSecond());
      }
      coeffKappaS = new double[basisSetSupply.size()];
      coeffKappaD = new double[basisSetDemand.size()];
      Arrays.fill(coeffKappaS, 0.0);
      Arrays.fill(coeffKappaD, 0.0);

      double supplyDepRate, demandDepRate;
      if (!lastOne) {
        supplyDepRate = model.getSupplyDepartureRate();
        demandDepRate = model.getDemandDepartureRate();
      } else {
        supplyDepRate = 1;
        demandDepRate = 1;
      }

      for (int i = 0; i < items.size(); i++) {
        Item item = items.get(i);
        if (item.isSod() == 1) {
          double[] eval = basisSetSupply.evaluate(item);
          for (int j = 0; j < basisSetSupply.size(); j++) {
            coeffKappaS[j] += eval[j] * supplyDepRate;
          }
        } else if (item.isSod() == 0) {
          double[] eval = basisSetDemand.evaluate(item);
          for (int j = 0; j < basisSetDemand.size(); j++) {
            coeffKappaD[j] += eval[j] * demandDepRate;
          }
        } else {
          throw new RuntimeException("the item sod type not specified");
        }
      }

      for (int i = 0; i < matches.size(); i++) {
        Item firstItem = matches.get(i).getFirst();
        Item secondItem = matches.get(i).getSecond();
        double[] eval = basisSetSupply.evaluate(firstItem);
        double[] eval2 = basisSetDemand.evaluate(secondItem);
        for (int j = 0; j < basisSetSupply.size(); j++) {
          coeffKappaS[j] += eval[j] * (1 - supplyDepRate);
        }
        for (int j = 0; j < basisSetDemand.size(); j++) {
          coeffKappaD[j] += eval2[j] * (1 - demandDepRate);
        }
      }
    }


    /**
     * @return constant of the constraint
     */
    public double getRhs() {
      return rhs;
    }

    /**
     * @return an array
     */
    public double[] getKappa1Coeff() {
      return coeffKappaS;
    }

    public double[] getKappa2Coeff() {
      return coeffKappaD;
    }

  }

}