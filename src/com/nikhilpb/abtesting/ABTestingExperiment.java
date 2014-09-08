package com.nikhilpb.abtesting;

import Jama.Matrix;
import com.nikhilpb.util.Experiment;
import com.nikhilpb.util.math.PSDMatrix;

import java.util.Properties;

/**
 * Created by nikhilpb on 8/27/14.
 *
 *
 */
public class ABTestingExperiment extends Experiment {
  private static Experiment instance = null;
  private static String name = "abtesting";

  private DataModel model;

  public static Experiment getInstance() {
    if (instance == null) {
      instance = new ABTestingExperiment();
    }
    return instance;
  }

  public static void register() {
    registerExperiment(name, getInstance());
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
    final String type = getPropertyOrDie(props, "type");
    if (type.equals("gaussian")) {
      model = getGaussianModel(props);
    } else if (type.equals("yahoo")) {
      model = null; //TODO(nikhilpb): finish this.
    } else {
      throw new RuntimeException("No model type: " + type + " found.");
    }
    return true;
  }

  /**
   * From the PropertySet get the relevant parameters and return a Gaussian model.
   * @param props
   * @return
   */
  private GaussianModel getGaussianModel(Properties props) {
    final int dim = Integer.parseInt(getPropertyOrDie(props, "dim"));
    final double sigma = Double.parseDouble(getPropertyOrDie(props, "sigma"));
    final double rho = Double.parseDouble(getPropertyOrDie(props, "rho"));
    final int timePeriods = Integer.parseInt(getPropertyOrDie(props, "time_periods"));
    final long seed = Long.parseLong(getPropertyOrDie(props, "seed"));
    double[][] muArray = new double[dim - 1][1];
    Matrix mu = new Matrix(muArray);
    double[][] sigmaArray = new double[dim - 1][dim - 1];
    // Construct the covariance matrix.
    for (int i = 0; i < dim - 1; ++ i) {
      for (int j = 0; j < dim - 1; ++ j) {
        if (i == j) {
          sigmaArray[i][j] = sigma * sigma;
        } else {
          sigmaArray[i][j] = sigma * sigma * rho;
        }
      }
    }
    PSDMatrix sigmaMatrix = new PSDMatrix(sigmaArray);
    return new GaussianModel(mu, sigmaMatrix, timePeriods, seed);
  }
}
