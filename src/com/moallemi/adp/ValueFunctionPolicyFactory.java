package com.moallemi.adp;

public class ValueFunctionPolicyFactory {

    public Policy getPolicy(StateFunction valueFunction) {
        return new ValueFunctionPolicy(valueFunction);
    }
}