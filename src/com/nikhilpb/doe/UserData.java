package com.nikhilpb.doe;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Created by nikhilpb on 8/26/14.
 */
public class UserData {
  private ArrayList<DataPoint> dataPoints;

  public UserData(final String textFile, final int userCount, int dim) throws Exception {
    BufferedReader br = new BufferedReader(new FileReader(textFile));
    dataPoints = new ArrayList<DataPoint>();
    try {
      String line = br.readLine();
      int count = 0;
      while (line != null && count < userCount) {
        String[] splits = line.split("\\|");
        if (splits.length < 2) continue;
        String tmpStr = splits[1];
        String[] numString = tmpStr.split(" ");
        double[] vector = new double[dim];
        for (int i = 1; i < numString.length; ++ i) {
          int ind = Integer.parseInt(numString[i]);
          if (1 > ind || ind > dim) continue;
          vector[ind - 1] = 1.;
        }
        dataPoints.add(new DataPoint(vector));
        line = br.readLine();
        count++;
      }
      System.out.printf("%d users read from the file", count);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      br.close();
    }
  }
}
