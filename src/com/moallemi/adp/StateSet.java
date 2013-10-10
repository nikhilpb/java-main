package com.moallemi.adp;

/**
 * Interface for a set of states and associated info.
 *
  *
 * @author Ciamac Moallemi
 * @version $Revision: 1.1 $, $Date: 2005-01-20 04:58:54 $
 */
public interface StateSet {
    
    /**
     * Get the underlying model.
     *
     * @return the model
     */
    public Model getModel();

    /**
     * Get a state.
     *
     * @param state
     * @return the copy of the state in this set, or <code>null</code>
     * if non exists.
     */
    public State getState(State state);

    /**
     * Get information on a particular state.
     *
     * @param state the state
     * @return the state information
     */
    public StateInfo getStateInfo(State state);

}