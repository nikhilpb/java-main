package com.moallemi.adp;

import java.util.Arrays;
import java.util.Random;

import com.moallemi.math.DiscreteDistribution;

/**
 * Class representing distribution of next states.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.3 $, $Date: 2006-10-18 19:06:18 $
 */
public class StateDistribution {
    private DiscreteDistribution distribution;
    private State[] nextStates;

    /**
     * Constructor.
     *
     * @param distribution the distribution
     * @param nextStates the next states
     * @throws IllegalArgumentException if lengths don't match
     */
    public StateDistribution(DiscreteDistribution distribution,
                             State[] nextStates)
    {
        this.distribution = distribution;
	    this.nextStates = nextStates;
        if (distribution.size() != nextStates.length)
            throw new IllegalArgumentException("size mismatch");
    }

    /**
     * Return number of next states.
     *
     * @return the number of next states.
     */
    public int getNextStateCount() { return nextStates.length; }

    /**
     * Return a particular next state.
     *
     * @param i the index
     * @return the next state at that index
     */
    public State getNextState(int i) { return nextStates[i]; }

    /**
     * Return a particular next state.
     *
     * @param i the index
     * @return the next state at that index
     */
    public double getProbability(int i) { 
        return distribution.getProbability(i);
    }

    /**
     * Compute an expected value.
     *
     * @param function the function
     * @return the expected value
     */
    public double expectedValue(StateFunction function) {
        double sum = 0.0;
        for (int i = 0; i < nextStates.length; i++) {
            State nextState = nextStates[i];
            sum += function.getValue(nextState)
                * distribution.getProbability(i);
        }
        return sum;
    }

    /**
     * Compute an expected value.
     *
     * @param basis the set of functions
     * @param tmp temporary storage
     * @param out the expected value of functions in the basis
     */
    public void expectedBasisValue(BasisSet basis,
                                   double[] tmp,
                                   double[] out)
    {
        Arrays.fill(out, 0.0);
        int size = basis.size();
        for (int i = 0; i < nextStates.length; i++) {
            basis.evaluate(nextStates[i], tmp);
            double p = distribution.getProbability(i);
            for (int j = 0; j < size; j++)
                out[j] += p * tmp[j];
        }
    }

    /**
     * Compute an expected value.
     *
     * @param function the function
     * @param self the current state, used for caching (good for
     * distributions with a lot of self-transitions)
     * @return the expected value
     */
    public double expectedValue(StateFunction function, State self) {
        double sum = 0.0;
        double selfValue = function.getValue(self);
        for (int i = 0; i < nextStates.length; i++) {
            State nextState = nextStates[i];
            double value = nextState == self 
                ? selfValue 
                : function.getValue(nextState);
            sum += value * distribution.getProbability(i);
        }
        return sum;
    }

    /**
     * Sample according to the distribution.
     *
     * @param random randomness source
     * @return the sampled state
     */
    public State nextSample(Random random) {
        return nextStates[distribution.nextSample(random)];
    }


}
