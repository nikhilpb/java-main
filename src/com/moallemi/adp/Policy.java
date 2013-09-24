package com.moallemi.adp;

import java.util.BitSet;

/**
 * A policy. No randomization allowed for now.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.3 $, $Date: 2006-10-17 16:35:16 $
 */
public interface Policy {
    public int getAction(State state, StateInfo info);

    public Action[] getOptimalActionList(State state, 
					 StateInfo info, 
					 double tolerance);

    public BitSet getOptimalActionMask(State state, 
                                       StateInfo info, 
                                       double tolerance);
}
