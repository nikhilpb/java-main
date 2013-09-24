package com.moallemi.adp;

import java.util.*;
import org.apache.oro.util.*;

/**
 * Used for caching states and associated information. Uses LRU algorithm.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.1 $, $Date: 2005-01-20 04:58:54 $
 */
public class StateCache implements StateSet {
    // underlying model
    private Model model;
    // cache capacity
    private int capacity;
    // the underlying cache
    private Cache cache;

    /**
     * Constructor.
     *
     * @param capacity the cache capacity
     */
    public StateCache(Model model, int capacity) {
        this.model = model;
        this.capacity = capacity;
        clear();
    }

    /**
     * Empty the cache.
     */
    public void clear() {
        cache = new CacheLRU(capacity);
    }


    // cache entries
    private static class Entry {
        public State state;
        public StateInfo info;

        public Entry(State state, StateInfo info) {
            this.state = state;
            this.info = info;
        }
    }

    // StateSet interface
    public Model getModel() { return model; }

    public State getState(State state) {
        Entry entry = (Entry) cache.getElement(state);
        return entry != null ? entry.state : null;
    }

    public StateInfo getStateInfo(State state) {
        Entry entry = (Entry) cache.getElement(state);
        if (entry == null) {
            entry = new Entry(state, model.getStateInfo(state, this));
            cache.addElement(state, entry);
        }
        return entry.info;
    }

}