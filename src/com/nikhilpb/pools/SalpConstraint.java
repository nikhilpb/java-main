package com.nikhilpb.pools;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/10/13
 * Time: 9:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class SalpConstraint {
    private double[] coeff;

    public SalpConstraint (ArrayList<Node> nodes,
                       ArrayList<Node> matchedNodes,
                       boolean lastOne,
                       MatchingPoolsModel model,
                       NodeFunctionSet basisSet){

        coeff = new double[basisSet.size()];
        Arrays.fill(coeff, 0.0);
        double depRate;
        if (!lastOne){
            depRate = model.getDepartureRate();
        }
        else {
            depRate = 1;
        }

        for (int i = 0; i < nodes.size(); i++){
            Node node = nodes.get(i);
            double[] eval = basisSet.evaluate(node);
            for (int j = 0; j < basisSet.size(); j++){
                coeff[j] += eval[j]*depRate;
            }
        }
        for (int i = 0; i < matchedNodes.size(); i++){
            Node node = matchedNodes.get(i);
            double[] eval = basisSet.evaluate(node);
            for (int j = 0; j < basisSet.size(); j++){
                coeff[j] += eval[j]*(1-depRate);
            }
        }
    }

    public double[] getCoeff(){
        return coeff;
    }

    public boolean satisfied(double[] kappa, double rhs) {
        double value = 0.0;
        for (int i = 0; i < kappa.length; ++i) {
            value += kappa[i] * coeff[i];
        }
        return value >= rhs;
    }

    public String toString(){
        String ts = "";
        for (int i = 0; i < coeff.length; i++){
            if (coeff[i] > 0.0){
                ts += "coeff[" + i + "]: " + coeff[i] + ", ";
            }
        }
        ts += "\n";
        return ts;
    }
}
