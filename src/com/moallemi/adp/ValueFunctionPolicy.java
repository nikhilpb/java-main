package com.moallemi.adp;

/**
 * A policy based on acting greedily with respect to a (expected)
 * value function.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.5 $, $Date: 2006-06-19 22:01:42 $
 */
public class ValueFunctionPolicy extends MinValuePolicy {
    // value function
    private StateFunction value;

    /**
     * Constructor.
     *
     * @param value the value function
     */
    public ValueFunctionPolicy(StateFunction value) { init(value); }

    protected ValueFunctionPolicy() {}

    protected void init(StateFunction value) { this.value = value; }

    // Policy interface
    
    public double getActionValue(State state, StateInfo info, int actionIndex)
    {
	return info.getDistribution(actionIndex).expectedValue(value, state);
    }
}
