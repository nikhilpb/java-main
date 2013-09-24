package com.moallemi.matching;

import java.util.ArrayList;

public class SGDSolverGM implements MatchingSolver {
  private ItemFunctionSet basisSetSupply, basisSetDemand;
  private GeneralMatchingModel model;
  private IoGMSet sampledInstances;
  private double eps, ssNumer, ssDenom;

  double[] kappaSupply, kappaDemand;

  /**
   * Constructor.
   *
   * @param sampledInstances set of matching instances sampled
   * @param basisSetSupply   basis set on the supply side
   * @param basisSetDemand   basis set of the demand side
   * @param model            the GeneralMatchingModel used
   * @param config           instance of class Config
   */
  public SGDSolverGM(IoGMSet sampledInstances,
                     ItemFunctionSet basisSetSupply, ItemFunctionSet basisSetDemand,
                     GeneralMatchingModel model, Config config) {
    this.sampledInstances = sampledInstances;
    this.basisSetSupply = basisSetSupply;
    this.basisSetDemand = basisSetDemand;
    this.model = model;
    this.eps = config.eps;
    this.ssNumer = config.ssNumer;
    this.ssDenom = config.ssDenom;
    int basisCountSupply = basisSetSupply.size();
    kappaSupply = new double[basisCountSupply];
    for (int i = 0; i < basisCountSupply; ++i) {
      kappaSupply[i] = 0.0;
    }
    int basisCountDemand = basisSetDemand.size();
    kappaDemand = new double[basisCountDemand];
    for (int i = 0; i < basisCountSupply; ++i) {
      kappaDemand[i] = 0.0;
    }
  }

  protected void findSubgrad(InstanceOfGeneralMatching instance,
                             double[] sgSupply,
                             double[] sgDemand) {
    for (int i = 0; i < sgSupply.length; ++i) {
      sgSupply[i] = 0.0;
    }
    for (int i = 0; i < sgDemand.length; ++i) {
      sgDemand[i] = 0.0;
    }
    int tp = instance.getTimePeriods();
    ArrayList<Item> states, supItems = new ArrayList<Item>(), demItems = new ArrayList<Item>();
    PHMatchLawler2 matcher;
    for (int t = 0; t < tp; ++t) {
      states = instance.getStates(t);
      supItems.clear(); demItems.clear();
      for (int i = 0; i < states.size(); ++i) {
        if (states.get(i).isSod() == 1) {
          supItems.add(states.get(i));
        } else {
          demItems.add(states.get(i));
        }
      }
      matcher = new PHMatchLawler2(supItems, demItems, model.getRewardFunction());
    }
  }


  /**
   * Solves the current instance of the ALP.
   *
   * @return `true' if the solve was successful
   */
  public boolean solve() {
    System.out.println("solving SALP with stochastic gradient descent");
    System.out.println("eps = " + eps);
    System.out.println("A = " + ssNumer);
    System.out.println("B = " + ssDenom);
    double stepSize;
    double[] sgSupply = new double[kappaSupply.length];
    double[] sgDemand = new double[kappaDemand.length];
    for (int i = 0; i < sampledInstances.size(); ++i) {
      stepSize = ssNumer / (ssDenom + (double) i);
      System.out.println("sampled instance: " + i
          + ", step size: " + stepSize);
      findSubgrad(sampledInstances.get(i), sgSupply, sgDemand);
      // Minimize objective, subtract sub-gradient
      for (int j = 0; j < kappaSupply.length; ++j) {
        kappaSupply[j] -= stepSize * sgSupply[j];
      }
      for (int j = 0; j < kappaDemand.length; ++j) {
        kappaDemand[j] -= stepSize * sgDemand[j];
      }
    }
    return true;
  }

  /**
   * @return returns the supply value function in the form of
   *         $\ip{\kappa_1}{\alpha}$
   */
  public ItemFunction getSupplyValue() {
    return basisSetSupply.getLinearCombination(kappaSupply);
  }

  /**
   * @return returns the demand value function in the form of
   *         $\ip{\kappa_2}{\beta}$
   */
  public ItemFunction getDemandValue() {
    return basisSetDemand.getLinearCombination(kappaDemand);
  }

  static class Config {
    public double eps, ssNumer, ssDenom;

    public Config(double eps, double ssNumer, double ssDenom) {
      this.eps = eps;
      this.ssNumer = ssNumer;
      this.ssDenom = ssDenom;
    }
  }
}