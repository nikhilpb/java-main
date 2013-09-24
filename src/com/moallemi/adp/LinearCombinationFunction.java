package com.moallemi.adp;

public class LinearCombinationFunction implements StateFunction {
    private BasisSet basis;
    private double[] coeffs;
    private double[] tmp;

    public LinearCombinationFunction(double[] coeffs, 
                                     BasisSet basis) 
    {
        this.basis = basis;
        this.coeffs = coeffs;
        tmp = new double [coeffs.length];
    }

    // not thread safe
    public double getValue(State state) {
        basis.evaluate(state, tmp);
        double sum = 0.0;
        for (int i = 0; i < coeffs.length; i++)
            sum += coeffs[i] * tmp[i];
        return sum;
    }
}
