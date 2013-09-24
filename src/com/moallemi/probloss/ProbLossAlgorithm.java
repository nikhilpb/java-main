package com.moallemi.probloss;

/**
 * Interface for loss probability algorithms.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.1 $, $Date: 2009-02-13 04:55:46 $
 */
public interface ProbLossAlgorithm {
    public void initialize(ProbLoss probLoss);
    public void nextSample();
    public void reset();
}