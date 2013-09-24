package com.moallemi.adp;

/**
 * The probabilistic model for a Markov Decision Process.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.1 $, $Date: 2005-01-20 04:58:54 $
 */
public interface Model {


    /**
     * Get the "base" state for this model.
     *
     * @return the base state
     */
    public State getBaseState();

    /**
     * Get information (e.g. actions, distributions) on a particular state.
     *
     * @param state the state
     * @param set used for caching new states than may need to be created,
     * can be <code>null</code>
     * @return the base state
     */
    public StateInfo getStateInfo(State state, StateSet set);

    /**
     * Enumerate a list of all states. May not be supported under 
     * all models (e.g. countable state spaces).
     *
     * @return a list of all states
     */
    public StateList enumerateStates();
    
}