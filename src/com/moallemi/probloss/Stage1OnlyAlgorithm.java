package com.moallemi.probloss;

/**
 * Only add stage 1 samples.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.1 $, $Date: 2009-02-13 04:55:45 $
 */
public class Stage1OnlyAlgorithm implements ProbLossAlgorithm {
    private ProbLoss probLoss = null;

    public void initialize(final ProbLoss probLoss) {
        this.probLoss = probLoss;
    }

    public void reset() { probLoss = null; }

    public void nextSample() {
        probLoss.addStage1Sample();
    }

}