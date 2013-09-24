package com.moallemi.gaussianbp;

// parameterizes a Gaussian in the information form
// exp(-(1/2) K x^2 + h x)
public class Gaussian1D {
    public double K;
    public double h;

    public Gaussian1D() { reset(); }
    public Gaussian1D(double K, double h) {
        this.K = K;
        this.h = h;
    }

    // get the mean
    public double getMean() {
        return h / K;
    }

    // get the variance
    public double getVariance() {
        return 1.0 / K;
    }

    public void reset() {
        K = 0.0; h = 0.0;
    }
}