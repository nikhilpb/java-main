package com.nikhilpb.pools;
import com.moallemi.math.CplexFactory;

import ilog.concert.*;
import ilog.cplex.*;

import java.util.ArrayList;
import java.util.Arrays;

public class LpSolver {
	private ArrayList<SalpConstraint> constrSet;
	private MatchingPoolsModel model;
	private NodeFunctionSet basisSet;
	private InstanceSet instances;
	private double eps;
	
	IloCplex cplex;
	IloNumVar[] kappaVar, sVar;
	ArrayList<IloRange> lpConstr;
	
	public LpSolver(CplexFactory factory,
					InstanceSet instances,
					NodeFunctionSet basisSet,
					MatchingPoolsModel model,
					double eps)
	throws IloException{
		this.instances = instances;
		this.basisSet = basisSet;
		this.model = model;
		this.eps = eps;
		int basisCount = basisSet.size();
		
		// cplex setup
		cplex = factory.getCplex();
		
		// set up kappaVar
		double[] lb = new double[basisSet.size()];
		double[] ub = new double[basisSet.size()];
		Arrays.fill(lb, -Double.MAX_VALUE);
		Arrays.fill(ub,  Double.MAX_VALUE);
		kappaVar = cplex.numVarArray(basisCount, lb, ub);
		
		int sSize = 0;
		for (int s = 0; s < this.instances.size(); s++){
			sSize += (instances.get(s).getTimePeriods() + 1);
		}
		
		// set up sVar 
		// if epsilon > 1E8, these are uniformly set to zero 
		double[] lbSlack = new double[sSize];
		double[] ubSlack = new double[sSize];
		Arrays.fill(lbSlack, 0.0);
		if (this.eps > 1E8){
			Arrays.fill(ubSlack, 0.0);
			System.out.println("Solving ALP");
		}
		else {
			Arrays.fill(ubSlack, Double.MAX_VALUE);
		}
		sVar = cplex.numVarArray(sSize, lbSlack, ubSlack);
		
		int sampleCount = this.instances.size();
		SampleInstance thisInstance;
		int tp;
		ArrayList<Node> state, matchedNodes;
		SalpConstraint thisConstraint;
		constrSet = new ArrayList<SalpConstraint>();
		double rhs;
		IloLinearNumExpr lhs, obj;
		obj = cplex.linearNumExpr();
		lpConstr = new ArrayList<IloRange>();
		int acc = 0;
		for (int s = 0; s < sampleCount; s++){
			thisInstance = this.instances.get(s);
			tp = thisInstance.getTimePeriods();
			for (int t = 0; t <= tp; t++){
				state = thisInstance.getState(t);
				matchedNodes = thisInstance.getMatches(t);
				rhs = thisInstance.getReward(t);
				thisConstraint = new SalpConstraint(state, matchedNodes, (t==tp), model, basisSet);
				constrSet.add(thisConstraint);
				lhs = cplex.scalProd(thisConstraint.getCoeff(), kappaVar);
				lhs.addTerm(sVar[acc + t], 1.0);
				cplex.addGe(lhs, rhs);
				obj.add(lhs);
			}
			acc += (tp+1);
		}
		double[] epsArray = new double[sSize];
		Arrays.fill(epsArray, this.eps);
		obj.add(cplex.scalProd(epsArray, sVar));
		cplex.addMinimize(obj);
	}
	
	public boolean solve() throws IloException {
        boolean status = cplex.solve();
        System.out.println("objective = " + cplex.getObjValue());
        return status;
    }
	
	public NodeFunction getValue() throws IloException {
        double[] kappa = cplex.getValues(kappaVar);
        return basisSet.getLinearCombination(kappa);
    }
	

}
