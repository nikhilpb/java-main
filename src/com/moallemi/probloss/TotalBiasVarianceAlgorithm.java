package com.moallemi.probloss;

import java.util.Comparator;

import org.apache.commons.collections.buffer.PriorityBuffer;

/**
 * Loss probability algorithm comparing total bias and variance.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.2 $, $Date: 2009-02-16 21:57:38 $
 */
public class TotalBiasVarianceAlgorithm implements ProbLossAlgorithm {
    private ProbLoss probLoss = null;
    private PriorityBuffer buffer;

    public TotalBiasVarianceAlgorithm(final boolean stage2BiasPlacement) {
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

        double bias2 = probLoss.getBiasEstimate();
        bias2 *= bias2;
        double variance = probLoss.getVarianceEstimate();

        if (bias2 < variance) {
            ProbLoss.Stage1Sample stage1 = probLoss.addStage1Sample();
            buffer.add(stage1);
        }
        else {
            // figure out where the best place is to add the stage 2 sample
            ProbLoss.Stage1Sample minSample 
                = (ProbLoss.Stage1Sample) buffer.remove();
            probLoss.addStage2Sample(minSample.getIndex());
            buffer.add(minSample);
        }
    }


}