package com.moallemi.probloss;

import java.util.Comparator;

import org.apache.commons.collections.buffer.PriorityBuffer;

/**
 * Constant exponent loss probability algorithm.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.2 $, $Date: 2009-02-16 21:57:38 $
 */
public class ConstantExponentAlgorithm implements ProbLossAlgorithm {
    // the constant and exponent to use for stage 1 samples
    protected double constant;
    protected double exponent;

    private ProbLoss probLoss = null;
    private PriorityBuffer buffer;

    public ConstantExponentAlgorithm(final double constant, 
                                     final double exponent,
                                     final boolean stage2BiasPlacement)
    {
        if (constant <= 0.0) 
            throw new RuntimeException("constant must be positive");
        if (exponent <= 0.0 || exponent >= 1.0) 
            throw new RuntimeException("exponent must be between zero and one");
        this.constant = constant;
        this.exponent = exponent;

        Comparator<ProbLoss.Stage1Sample> c;
        if (stage2BiasPlacement) 
            c = ProbLoss.comparatorLocalBiasImprovement();
        else
            c = ProbLoss.comparatorStage2Count();
        buffer =  new PriorityBuffer(true, c);
    }

    public void initialize(final ProbLoss probLoss) {
        this.probLoss = probLoss;
        buffer.clear();
        int n = probLoss.getStage1Count();
        for (int i = 0; i < n; i++)
            buffer.add(probLoss.getStage1Sample(i));
    }

    public void reset() { 
        this.probLoss = null; 
        buffer.clear();
    }

    public void nextSample() {
        int n = probLoss.getStage1Count();
        if (n == 0) {
            ProbLoss.Stage1Sample stage1 = probLoss.addStage1Sample();
            buffer.add(stage1);
            return;
        }

        double mAverage = probLoss.getAverageStage2Count();

        double impliedWork1 = Math.pow(n / constant, 1.0/exponent);
        double impliedWork2 = Math.pow(mAverage * constant, 
                                       1.0/(1.0 - exponent));

        if (impliedWork1 < impliedWork2) {
            ProbLoss.Stage1Sample stage1 = probLoss.addStage1Sample();
            buffer.add(stage1);
        }
        else {
            ProbLoss.Stage1Sample minSample 
                = (ProbLoss.Stage1Sample) buffer.remove();
            probLoss.addStage2Sample(minSample.getIndex());
            buffer.add(minSample);
        }
    }


}