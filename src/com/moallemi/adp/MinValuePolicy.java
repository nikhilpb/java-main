package com.moallemi.adp;

import java.util.BitSet;

/**
 * A policy based on acting greedily with respect to a value function.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.2 $, $Date: 2006-10-17 16:35:16 $
 */
public abstract class MinValuePolicy implements Policy {
    // Policy interface
    
    public int getAction(State state, StateInfo info) {
        int actionCount = info.getActionCount();
        int minAction = -1;
        double minActionValue = Double.MAX_VALUE;
        for (int a = 0; a < actionCount; a++) {
            double actionValue = getActionValue(state, info, a);
            if (minAction < 0 || actionValue < minActionValue) {
                minAction = a;
                minActionValue = actionValue;
            }
        }
        return minAction;
    }

    public Action[] getOptimalActionList(State state, 
					 StateInfo info, 
					 double tolerance)
    {
        int actionCount = info.getActionCount();
	double[] actionValue = new double [actionCount];
        double minActionValue = Double.MAX_VALUE;
        for (int a = 0; a < actionCount; a++) {
	    actionValue[a] = getActionValue(state, info, a);
            if (actionValue[a] < minActionValue) {
                minActionValue = actionValue[a];
	    }
	}

	int minActionCount = 0;
        for (int a = 0; a < actionCount; a++) {
	    if (Math.abs(actionValue[a] - minActionValue) < tolerance) {
		minActionCount++;
	    }
        }

	Action[] opt = new Action [minActionCount];
	int cnt = 0;
        for (int a = 0; a < actionCount; a++) {
	    if (Math.abs(actionValue[a] - minActionValue) < tolerance) {
		opt[cnt++] = info.getAction(a);
	    }
	}

        return opt;
    }

    public BitSet getOptimalActionMask(State state, 
                                       StateInfo info, 
                                       double tolerance)
    {
        int actionCount = info.getActionCount();

	double[] actionValue = new double [actionCount];
        double minActionValue = Double.MAX_VALUE;
        for (int a = 0; a < actionCount; a++) {
	    actionValue[a] = getActionValue(state, info, a);
            if (actionValue[a] < minActionValue) 
                minActionValue = actionValue[a];
	}

        BitSet mask = new BitSet(actionCount);
        for (int a = 0; a < actionCount; a++) {
	    if (Math.abs(actionValue[a] - minActionValue) < tolerance) 
		mask.set(a);
        }

        return mask;
    }


    public abstract double getActionValue(State state, 
					  StateInfo info, 
					  int actionIndex);

}
