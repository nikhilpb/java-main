package com.nikhilpb.util.math;

import java.util.ArrayList;

/**
 * Created by nikhilpb on 5/22/14.
 */
public class Series {
  ArrayList<Double> seq;

  public Series() {
    seq = new ArrayList<Double>();
  }

  public void add(double point) {
    seq.add(point);
  }

  public double getMean() {
    double sum = 0.;
    for (Double s : seq) {
      sum += s;
    }
    return sum / seq.size();
  }

  public double getStdDev() {
    double mean = getMean();
    double dev = 0.;
    for (Double s : seq) {
      dev += (s - mean) * (s - mean);
    }
    dev = dev / seq.size();
    return Math.sqrt(dev);
  }
}
