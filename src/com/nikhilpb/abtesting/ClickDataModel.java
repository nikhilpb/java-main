package com.nikhilpb.abtesting;

import com.nikhilpb.util.math.PSDMatrix;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

/**
 * Created by nikhilpb on 9/9/14.
 *
 * ClickDataModel reads Yahoo's click data obtained from Webscape and returns data points.
 */
public class ClickDataModel implements DataModel {

  private int dim;
  private ArrayList<DataPoint> dataPoints;
  private Random random;
  private PSDMatrix sigma;
  private static final double kTol = 1E-2;
  private HashSet<Integer> excludeSet;
  private HashMap<Integer, Integer> indexMap;
  private double[] mu;
  private static final int kMaxCount = 100000;

  /**
   *
   * @param trainFile Used to populate the covariance matrix
   * @param testFile Used for data points generation
   * @param userCount The number of users to sample from
   * @param dim number of covariates
   * @param seed for sampling with replacement
   */
  public ClickDataModel(final String trainFile,
                        final String testFile,
                        final int userCount,
                        final int dim,
                        final long seed) {
    random = new Random(seed);
    final int originalDim = dim;
    try {
      System.out.println("Finding covariates that are always zero");
      excludeSet = getExcludeSet(trainFile, originalDim);
      System.out.printf("%d covariates excluded\n", excludeSet.size());
      indexMap = populateIndexMap(excludeSet, originalDim);
      this.dim = indexMap.size() + 1;
      System.out.printf("The dimension of the covariates is %d\n", dim());
      computeStats(trainFile, dim);
      System.out.println("Stats computed");
      readDataPoints(testFile, userCount, originalDim);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Find a collection of indices that are 'nearly' always 0.
   * @param trainFile
   * @param dim
   * @return
   */
  private static HashSet<Integer> getExcludeSet(final String trainFile,
                                                final int dim) throws Exception {
    HashSet<Integer> set = new HashSet<Integer>();
    BufferedReader br = new BufferedReader(new FileReader(trainFile));
    double[] muArray = new double[dim];
    double[][] sigmaArray = new double[dim][dim];
    int count = 0;
    try {
      String line = br.readLine();
      while (line != null) {
        double[] dp = parseVector(line, dim);
        for (int i = 0; i < dp.length; ++i) {
          muArray[i] += dp[i];
          for (int j = 0; j < dp.length; ++j) {
            sigmaArray[i][j] += dp[i] * dp[j];
          }
        }
        count++;
        line = br.readLine();
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      br.close();
    }
    for (int i = 0; i < muArray.length; ++i) {
      muArray[i] = muArray[i] / count;
      if (muArray[i]  < kTol || muArray[i] > 1-kTol)  {
        set.add(i+1);
      }
    }

    for (int i = 0; i < dim; ++i) {
      for (int j = 0; j < dim; ++j) {
        sigmaArray[i][j] = (sigmaArray[i][j] / count) - muArray[i] * muArray[j];
      }
    }

    for (int i = 0; i < dim; ++i) {
      for (int j = 0; j < i; ++j) {
        if (Math.abs(sigmaArray[i][j] - sigmaArray[i][i]) + Math.abs(sigmaArray[i][j] - sigmaArray[j][j]) < kTol) {
          set.add(i);
        }
      }
    }

    return set;
  }

  /**
   * Parses the covariate vector of the from a line of a file containing Yahoo's user click data from Webscape.
   *
   * @param line
   * @param dim Maximum dimension to be considered
   * @return
   */
  private static double[] parseVector(String line, int dim) {
    String[] splits = line.split("\\|");
    if (splits.length < 2) return null;
    String tmpStr = splits[1];
    String[] numString = tmpStr.split(" ");
    double[] vector = new double[dim];
    for (int i = 1; i < numString.length; ++ i) {
      int ind = Integer.parseInt(numString[i]);
      if (dim >= ind) {
        vector[ind - 1] = 1.;
      }
    }
    return vector;
  }

  /**
   * Creates a map from indices in the data file to indices of the data points that will be returned.
   *
   * @param excludeSet These covariates are excluded.
   * @param dim Number of candidate covariates.
   * @return
   */
  private static HashMap<Integer, Integer> populateIndexMap(final HashSet<Integer> excludeSet,
                                                            final int dim) {
    HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
    int cur = 0;
    for (int i = 1; i < dim; ++i) {
      if (!excludeSet.contains(i)) {
        map.put(i, cur);
        cur++;
      }
    }
    return map;
  }

  /**
   * Compute the mean and covariance vector of the data.
   * @param trainFile
   * @param originalDim
   * @throws Exception
   */
  private void computeStats(final String trainFile, final int originalDim) throws Exception {
    BufferedReader br = new BufferedReader(new FileReader(trainFile));
    int count = 0;
    mu = new double[dim - 1];
    double[][] sigmaArray = new double[dim - 1][dim - 1];
    try {
      String line = br.readLine();
      while (line != null && count < kMaxCount) {
        double[] dp = parseVector(line, originalDim);
        for (int i = 0; i < dp.length; ++i) {
          if (indexMap.containsKey(i+1)) {
            mu[indexMap.get(i+1)] += dp[i];
          }
          for (int j = 0; j < dp.length; ++j) {
            if (indexMap.containsKey(i+1) && indexMap.containsKey(j+1)) {
              int mapI = indexMap.get(i+1), mapJ = indexMap.get(j+1);
              sigmaArray[mapI][mapJ] += dp[i] * dp[j];
            }
          }
        }
        count++;
        if (count % 10000 == 0) {
          System.out.printf("%d data points read\n", count);
        }
        line = br.readLine();
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      br.close();
    }
    for (int i = 0; i < dim - 1; ++i) {
      mu[i] = mu[i] / count;
      for (int j = 0; j < dim - 1; ++j) {
        sigmaArray[i][j] = sigmaArray[i][j] / count;
      }
    }

    for (int i = 0; i < dim - 1; ++i) {
      for (int j = 0; j < dim - 1; ++j) {
        sigmaArray[i][j] = sigmaArray[i][j] - mu[i] * mu[j];
      }
    }
    sigma = new PSDMatrix(sigmaArray);
  }

  /**
   * Read the specified number of test data points and store them in memory.
   * @param testFile
   * @param userCount
   * @param originalDim
   * @throws Exception
   */
  private void readDataPoints(final String testFile,
                              final int userCount,
                              final int originalDim) throws Exception {
    dataPoints = new ArrayList<DataPoint>();
    BufferedReader br = new BufferedReader(new FileReader(testFile));
    int count = 0;
    try {
      String line = br.readLine();
      while (line != null && count < userCount) {
        double[] dp = parseVector(line, originalDim);
        double[] thisDP = new double[dim-1];
        for (int i = 0; i < dp.length; ++i) {
          if (indexMap.containsKey(i+1)) {
            final int mapI = indexMap.get(i+1);
            thisDP[mapI] = dp[i] - mu[mapI];
          }
        }
        dataPoints.add(new DataPoint(thisDP));
        count++;
        line = br.readLine();
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      br.close();
    }
  }

  /**
   * Sample from the data points with replacement.
   * @return
   */
  @Override
  public DataPoint next() {
    return dataPoints.get(random.nextInt(dataPoints.size()));
  }

  @Override
  public PSDMatrix getSigma() {
    return sigma;
  }

  @Override
  public int dim() {
    return dim;
  }
}