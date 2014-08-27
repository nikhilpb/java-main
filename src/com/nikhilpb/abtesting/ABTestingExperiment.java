package com.nikhilpb.abtesting;

import com.nikhilpb.util.Experiment;

import java.util.Properties;

/**
 * Created by nikhilpb on 8/27/14.
 */
public class ABTestingExperiment extends Experiment {
  private static Experiment instance = null;

  public static Experiment getInstance() {
    if (instance == null) {
      instance = new ABTestingExperiment();
    }
    return instance;
  }

  private ABTestingExperiment() {
    super();

    CommandProcessor modelProcessor = new CommandProcessor() {
      @Override
      public boolean processCommand(Properties props) throws Exception {
        return modelCommand(props);
      }
    };
    registerCommand("model", modelProcessor);
  }

  protected boolean modelCommand(Properties props) {
    final int dimension = Integer.parseInt(getPropertyOrDie(props, "dimension"));
    return true;
  }
}
