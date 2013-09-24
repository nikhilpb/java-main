package com.moallemi.queueing;

import java.util.*;

import ilog.concert.*;
import ilog.cplex.*;

import com.moallemi.adp.*;

public class SwitchSymmetricQueueStateBasisSet implements BasisSet {
    private StateFunction[] basis;
    private int queueCount;
    private int cutoff;

    public SwitchSymmetricQueueStateBasisSet(OpenQueueingNetworkModel model,
                                             int cutoff, 
                                             double[] poly) 
    {
        this.queueCount = model.getQueueCount();
        SwitchQueueStateSymmetry symmetry = 
            new SwitchQueueStateSymmetry(model);
        this.cutoff = cutoff;
        ArrayList<StateFunction> list = 
            new ArrayList<StateFunction>();
        list.add(new ConstantFunction());
        for (int p = 0; p < poly.length; p++)
            list.add(new SymmetricQueueLengthFunction(poly[p]));

        Map<QueueState,Set<QueueState>> stateMap
            = new HashMap<QueueState,Set<QueueState>> ();

        int[] queueLengths = new int [queueCount];
        Arrays.fill(queueLengths, 0);
        boolean found = true;
        while (found) {
            int[] tmp = new int [queueCount];
            System.arraycopy(queueLengths, 0, tmp, 0, queueCount);
            symmetry.canonicalForm(tmp);
            QueueState canonicalState = new QueueState(tmp);

            int[] copy = new int [queueCount];
            System.arraycopy(queueLengths, 0, copy, 0, queueCount);
            QueueState state = new QueueState(copy);

            Set<QueueState> set = stateMap.get(canonicalState);
            if (set == null) {
                set = new HashSet<QueueState> (11);
                stateMap.put(canonicalState, set);
            }
            set.add(state);

            found = false;
            for (int i = 0; i < queueCount && !found; i++) {
                queueLengths[i]++;
                if (queueLengths[i] > cutoff)
                    queueLengths[i] = 0;
                else
                    found = true;
            }
        }

        for (Iterator<Map.Entry<QueueState,Set<QueueState>>>
                 i = stateMap.entrySet().iterator();
             i.hasNext(); ) {
            Map.Entry<QueueState,Set<QueueState>> e = i.next();
            list.add(new QueueStateSetFunction(e.getValue(),
                                               e.getKey().toString()));
        }

        basis = list.toArray(new StateFunction [0]);
    }
        
    public int size() { return basis.length; }
//     public StateFunction getFunction(int i) { return basis[i]; }
    public String getFunctionName(int i) { return basis[i].toString(); }
    public double getMinValue(int i) { return -Double.MAX_VALUE; }
    public double getMaxValue(int i) { return Double.MAX_VALUE; }
    public void addConstraints(IloCplex cplex, IloNumVar[] rVar) 
        throws IloException 
    {}
    public StateFunction getLinearCombination(double[] r) {
        return new LinearCombinationFunction(r, this);
    }

    // not efficient, use hash table!
    public void evaluate(State state, double[] out) {
        for (int i = 0; i < basis.length; i++)
            out[i] = basis[i].getValue(state);
    }

}
