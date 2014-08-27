package com.nikhilpb.stopping;

import com.nikhilpb.adp.*;
import com.nikhilpb.util.Experiment;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 4/23/14
 * Time: 11:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class StoppingExperiment extends Experiment {
  private StoppingModel model;
  private RewardFunction rewardFunction;
  private BasisSet basisSet;
  private ArrayList<SamplePath> samplePath;
  private Solver solver;
  private Policy policy;

  private static Experiment instance = null;

  public static Experiment getInstance() {
    if (instance == null) {
      instance = new StoppingExperiment();
    }
    return instance;
  }


}
