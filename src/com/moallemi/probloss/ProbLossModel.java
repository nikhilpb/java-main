package com.moallemi.probloss;

import java.util.Random;

import com.moallemi.math.NormalDistribution;

/**
 * Gaussian probabilistic model for probability loss estimation.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.1 $, $Date: 2009-02-13 04:55:46 $
 */
public class ProbLossModel {
    // parameters of the distribution we are estimating
    // stage1 sample is N(0,sigma1^2)
    private double sigma1; 
    // stage2 sample is stage1+N(0,sigma2^2)
    private double sigma2;

    // the threshold whose probability we are estimating, e.g.
    // P(Z <= threshold)
    private double threshold;

    public ProbLossModel(final double sigma1,
                         final double sigma2,
                         final double threshold) {
        this.sigma1 = sigma1;
        this.sigma2 = sigma2;
        this.threshold = threshold;
    }

    public double getSigma1() { return sigma1; }

    public double getSigma2() { return sigma2; }

    public double getThreshold() { return threshold; }

    public double getProbLossTrue() {
        return NormalDistribution.gsl_cdf_ugaussian_P(threshold / sigma1);
    }

    public double nextStage1Sample(Random random) {
        return random.nextGaussian()*sigma1;
    }

    public double nextStage2Sample(Random random, double Z) {
        return Z + random.nextGaussian()*sigma2;
    }

}
