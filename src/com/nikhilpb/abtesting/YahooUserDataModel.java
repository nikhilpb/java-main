package com.nikhilpb.abtesting;

import com.nikhilpb.util.math.PSDMatrix;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by nikhilpb on 9/8/14.
 */
public class YahooUserDataModel implements DataModel {
  private int dim;
  private ArrayList<DataPoint> dataPoints;
  private Random random;
  private PSDMatrix sigma;

  public YahooUserDataModel(final String trainFile,
                            final String testFile,
                            final int userCount,
                            final int dim,
                            final long seed) throws Exception {
    this.random = new Random(seed);
    this.dim = dim;
    BufferedReader br = new BufferedReader(new FileReader(testFile));
    dataPoints = new ArrayList<DataPoint>();
    try {
      String line = br.readLine();
      int count = 0;
      while (line != null && count < userCount) {
        DataPoint dp = parseDataPoint(line);
        if (dp != null) {
          dataPoints.add(dp);
        }
        line = br.readLine();
        count++;
      }
      System.out.printf("%d users read from the file", count);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      br.close();
    }
    sigma = estimateSigma(trainFile);
  }

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

  private DataPoint parseDataPoint(String line) {
    String[] splits = line.split("\\|");
    if (splits.length < 2) return null;
    String tmpStr = splits[1];
    String[] numString = tmpStr.split(" ");
    double[] vector = new double[dim];
    for (int i = 1; i < numString.length; ++ i) {
      int ind = Integer.parseInt(numString[i]);
      if (1 > ind || ind > dim) continue;
      vector[ind - 1] = 1.;
    }
    return new DataPoint(vector);
  }

  private PSDMatrix estimateSigma(final String trainFile) throws Exception {
    BufferedReader br = new BufferedReader(new FileReader(trainFile));
    DataPointStats dataPointStats = new DataPointStats(dim);
    try {
      String line = br.readLine();
      while (line != null) {
        DataPoint dp = parseDataPoint(line);
        if (dp != null) {
          dataPointStats.add(dp);
        }
        line = br.readLine();
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      br.close();
    }
    return dataPointStats.getSigma();
  }
}
