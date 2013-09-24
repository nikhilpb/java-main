package com.moallemi.iqswitch;

public class LinearCombinationFunction implements SeparableFunction 
{
    private BasisSet basis;
    private double[] coeffs;
    private double[] tmp;

    public LinearCombinationFunction(double[] coeffs,
                                     BasisSet basis) {
        this.basis = basis;
        this.coeffs = coeffs;
        if (basis.size() != coeffs.length) 
            throw new IllegalArgumentException("coeff size much match basis");
        tmp = new double [coeffs.length];
    }

    public double getValue(SwitchState state) {
        basis.evaluate(state, tmp);
        double sum = 0.0;
        for (int i = 0; i < coeffs.length; i++)
            sum += coeffs[i] * tmp[i];
        return sum;
    }

    // not thread safe
    public void addToMatrix(SwitchState state,
                            double weight,
                            MatchingMatrix matrix) {
        double[] r;
        if (weight == 1.0)
            r = coeffs;
        else {
            for (int i = 0; i < coeffs.length; i++)
                tmp[i] = weight * coeffs[i];
            r = tmp;
        }

        basis.addToMatrix(state,
                          r,
                          matrix);
    }
}
            
    