package com.nikhilpb.doe;

/**
 * Created by nikhilpb on 3/20/14.
 */
public class QFunctionRecursion {

  public static DiscretizedFunction recurse(OneDFunction nextQ,
                                            int p,
                                            double upper,
                                            int pointsCount,
                                            long seed,
                                            int sampleCount,
                                            PolicyType type) {
    ABPairGenerator abPairGenerator = new ABPairGenerator(seed, p);
    ABPairGenerator.ABPair[] abPairs = abPairGenerator.next(sampleCount);

    DiscretizedFunction qFun = new DiscretizedFunction(upper, pointsCount);
    double[] values = new double[pointsCount];

    for (int c = 0; c < pointsCount; ++ c) {
      double lambda = qFun.getPoint(c);
      double pointEstimate = 0;
      for (int i = 0; i < abPairs.length; ++ i) {
        pointEstimate += qEstimate(nextQ, abPairs[i], lambda, type);
      }
      values[c] = pointEstimate / sampleCount;
    }

    qFun.setValues(values);

    return qFun;
  }

  public static double qEstimate(OneDFunction nextQ,
                                 ABPairGenerator.ABPair abPair,
                                 double lambda,
                                 PolicyType type) {
    double action;
    double beta = abPair.beta * Math.sqrt(lambda);
    double alpha = abPair.alpha;
    switch (type) {
      case OPTIMAL:
        if (nextQ.value(lambda + alpha - beta)
                    >= nextQ.value(lambda + alpha + beta)) {
          action = 1.;
        } else {
          action = - 1.;
        }
        break;
      case MYOPIC:
        if (beta < 0.) {
          action = 1.;
        } else {
          action = - 1.;
        }
        break;
      default:
        action = 1.;
        break;
    }

    return nextQ.value(lambda + alpha + 2. * action * beta);
  }

  public static enum PolicyType {
    OPTIMAL, MYOPIC;
  }
}
