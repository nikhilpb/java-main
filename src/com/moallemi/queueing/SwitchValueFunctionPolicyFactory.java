package com.moallemi.queueing;

import com.moallemi.adp.*;

public class SwitchValueFunctionPolicyFactory 
    extends ValueFunctionPolicyFactory {

    private SwitchModel model;

    public SwitchValueFunctionPolicyFactory(SwitchModel model) {
        this.model = model;
    }

    public Policy getPolicy(StateFunction valueFunction) {
        return new SwitchValueFunctionPolicy(model, valueFunction);
    }
}