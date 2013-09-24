package com.moallemi.adp;

import java.util.*;

import org.apache.oro.util.*;

/**
 * Simulate a path of sample states.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.3 $, $Date: 2006-06-15 21:02:08 $
 */
public class PathSimulator implements Iterator<State> {
    private Random random;
    private State currentState;
    private Model model;
    private Policy policy;
    private StateSet set;
    private Cache cache;

    /**
     * Constructor.
     *
     * @param model the model
     * @param policy the policy to use
     * @param set a state set used for caching
     * @param random randomness source
     * @param size of policy action cache 
     */
    public PathSimulator(Model model,
                         Policy policy,
                         StateSet set,
                         Random random,
                         int cacheCapacity)
    {
        this.model = model;
        this.policy = policy;
        this.set = set;
        if (cacheCapacity > 0)
            cache = new CacheLRU(cacheCapacity);
        reset(random);
    }

    /**
     * Reset the sample path, and initialize with a new source of
     * randomness.
     *
     * param random randomness source
     */
    public void reset(Random random) {
        this.random = random;
        currentState = null;
    }

    // Iterator interface
    public boolean hasNext() { return true; }
    public void remove() { throw new UnsupportedOperationException(); }

    /**
     * Fetch the next state in the path.
     *
     * @return the next state
     */
    public State next() {
        // always start in the base state
        if (currentState == null) {
            currentState = model.getBaseState();
            return currentState;
        }

        StateInfo info = set.getStateInfo(currentState);

        // selected action
        int a;

        if (cache != null) {
            Integer actionInt = (Integer) cache.getElement(currentState);
            if (actionInt != null)
                a = actionInt.intValue();
            else {
                a = policy.getAction(currentState, info);
                cache.addElement(currentState, new Integer(a));
            }
        }
        else
            a = policy.getAction(currentState, info);
                
        StateDistribution dist = info.getDistribution(a);
        currentState = dist.nextSample(random);
        return currentState;
    }

}