package com.moallemi.probloss;

import java.util.Comparator;

import org.apache.commons.collections.buffer.PriorityBuffer;

/**
 * The optimal constant exponent loss probability algorithm.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.2 $, $Date: 2009-02-17 15:57:55 $
 */
public class OptimalConstantExponentAlgorithm 
    extends ConstantExponentAlgorithm 
{

    public OptimalConstantExponentAlgorithm(boolean stage2) {
        super(1.0, 2.0 / 3.0, stage2);
    }

    private static final double SQRT_2_PI = Math.sqrt(2.0 * Math.PI);

    public void initialize(final ProbLoss probLoss) {
        super.initialize(probLoss);

        // adjust the constant
        ProbLossModel model = probLoss.getModel();
        double alpha = model.getProbLossTrue();
        double sigma1 = model.getSigma1();
        double sigma1sq = sigma1*sigma1;
        double sigma2 = model.getSigma2();
        double u = model.getThreshold();
        double c = -0.5 * sigma2 * sigma2 
            * Math.exp(- u*u / (2.0 * sigma1sq))
            / (sigma1sq * sigma1 * SQRT_2_PI);
        constant = Math.pow(alpha * (1.0 - alpha) / (2.0 * c * c),
                            1.0 / 3.0);
    }

}