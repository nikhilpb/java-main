package com.nikhilpb.matching;

import com.moallemi.util.data.Pair;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 9/30/13
 * Time: 5:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class AsymmetricMatcher {
    private double[][] weights;
    private int supplySize, demandSize;
    private IloCplex cplex;
    private IloNumVar[][] piVar;
    private static final double kTol = 1E-5;

    public AsymmetricMatcher(double[][] weights, IloCplex cplex) {
        this.weights = weights;
        this.cplex = cplex;
        supplySize = weights.length;
        System.out.println(supplySize);
        demandSize = weights[0].length;
        for (int i = 0; i < weights.length; ++i) {
            if (weights[i].length != demandSize) {
                throw new RuntimeException("inappropriate wrights array");
            }
        }
    }

    boolean solve() throws IloException {
        cplex.clearModel();
        piVar = new IloNumVar[supplySize][demandSize];
        double[] lb = new double[demandSize], ub = new double[demandSize],ones = new double[demandSize];
        Arrays.fill(lb, 0.0); Arrays.fill(ub, 1.0); Arrays.fill(ones, 1.0);
        for (int i = 0; i < supplySize; i++) {
            piVar[i] = cplex.numVarArray(demandSize, lb, ub);
            cplex.addLe(cplex.scalProd(ones, piVar[i]), 1.0);
        }
        IloLinearNumExpr tempExp, obj;
        for (int j = 0; j < demandSize; j++) {
            tempExp = cplex.linearNumExpr();
            for (int i = 0; i < supplySize; i++) {
                tempExp.addTerm(1.0, piVar[i][j]);
            }
            cplex.addLe(tempExp, 1.0);
        }
        obj = cplex.linearNumExpr();
        for (int i = 0; i < supplySize; ++i) {
            for (int j = 0; j < demandSize; ++j) {
                obj.addTerm(weights[i][j], piVar[i][j]);
            }
        }
        cplex.addMaximize(obj);
        return cplex.solve();
    }

    public ArrayList<Pair<Integer, Integer>> getMatchedPairs() throws IloException {
        ArrayList<Pair<Integer, Integer>> pairs = new ArrayList<Pair<Integer, Integer>>();
        for (int i = 0; i < supplySize; ++i) {
            for (int j = 0; j < demandSize; ++j) {
                double pi = cplex.getValue(piVar[i][j]);
                if (pi > kTol) {
                    pairs.add(new Pair<Integer, Integer>(i, j));
                }
            }
        }
        return pairs;
    }
}
