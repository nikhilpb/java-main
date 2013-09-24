package com.moallemi.adp;

import java.util.*;

public class StateList implements StateSet {
    // array of states
    private State[] states;
    // array of state info
    private StateInfo[] info;
    // the model
    private Model model;
    // state->array index map
    private Map<State,Integer> stateIndexMap;

    public StateList(Model model, State[] states) {
        this.states = states;
        this.model = model;
        info = new StateInfo [states.length];
        stateIndexMap = new HashMap<State,Integer> (2*states.length + 1);
        for (int s = 0; s < states.length; s++) 
            stateIndexMap.put(states[s], new Integer(s));
    }

    // StateSet interface
    public Model getModel() { return model; }

    public State getState(State state) { 
        int index = getStateIndex(state);
        return index >= 0 ?  states[index] : null;
    }

    public StateInfo getStateInfo(State state) {
        return getStateInfo(getStateIndex(state));
    }


    public int getStateCount() { return states.length; }
    public State getState(int s) { return states[s]; }

    public int getStateIndex(State state) {
        Integer index = stateIndexMap.get(state);
        return index == null ? -1 : index.intValue();
    }
    public StateInfo getStateInfo(int s) { 
        if (info[s] == null) 
            info[s] = model.getStateInfo(states[s], this);
        return info[s]; 
    }

    public Iterator<State> getSortedIterator(Comparator<State> c) {
	State[] sorted = new State [states.length];
	System.arraycopy(states, 0, sorted, 0, states.length);
	Arrays.sort(sorted, c);
	return Arrays.asList(sorted).iterator();
    }
}
