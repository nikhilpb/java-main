package com.moallemi.gaussianbp;

// parameterizes a Gaussian in the information form
// exp(-(1/2) x^T K x + h^T x)
public class Gaussian2D {
    public double K00, K01, K11;
    public double h0, h1;

    public Gaussian2D() {}
    public Gaussian2D(double K00,
                      double K01,
                      double K11,
                      double h0,
                      double h1) 
    {
        this.K00 = K00;
        this.K01 = K01;
        this.K11 = K11;
        this.h0 = h0;
        this.h1 = h1;
    }
}