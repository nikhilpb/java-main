package com.moallemi.iqswitch;

import java.util.*;

import com.moallemi.adp.State;

public class SwitchStateComparator implements Comparator<SwitchState> {
    private SwitchModel model;
    private SymmetricQueueLengthScalarFunction f;

    public SwitchStateComparator(SwitchModel model) {
        this.model = model;
        f = new SymmetricQueueLengthScalarFunction(model, 
                                                   new PolyScalarFunction(1.001));
    }

    public int compare(SwitchState a, SwitchState b) {
        double aF = f.getValue(a);
        double bF = f.getValue(b);
        if (aF < bF)
            return -1;
        if (aF > bF)
            return 1;
        int switchSize = model.getSwitchSize();
        for (int src = 0; src < switchSize; src++) {
            for (int dest = 0; dest < switchSize; dest++) {
                int aV = a.getQueueLength(src, dest);
                int bV = b.getQueueLength(src, dest);
                if (aV < bV)
                    return -1;
                if (aV > bV)
                    return 1;
            }
        }
        return 0;
    }
}
