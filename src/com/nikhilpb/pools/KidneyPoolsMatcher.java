package com.nikhilpb.pools;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import com.nikhilpb.matching.RewardFunction;
import ilog.concert.*;
import ilog.cplex.*;

import com.moallemi.math.CplexFactory;
import com.moallemi.util.data.Pair;

public class KidneyPoolsMatcher {
	ArrayList<Node> nodes;
	NodeRewardFunction nrf;
	double[][] coeffs;
	int N;
	static final double TOL = 1-1E-5;
	
	IloCplex cplex;
	IloNumVar[][] piVar;
	
	public KidneyPoolsMatcher(CplexFactory factory,
							  NodeRewardFunction nrf)
	throws IloException{
		this.nrf = nrf;
		cplex = factory.getCplex();
	}
	
	public KidneyPoolsMatcher(CplexFactory factory,
								ArrayList<Node> nodes,
								NodeRewardFunction nrf)
	throws IloException{
		this.nodes = nodes;
		this.nrf = nrf;
		
		cplex = factory.getCplex();
		init();
	}
	
	public double solve() throws IloException {
		cplex.setOut(new NullOutputStream());
    	cplex.solve();
   		return cplex.getObjValue();
	}
	
	public Boolean[] findMatches()
	throws IloException{
		Boolean[] matches = new Boolean[N];
		Arrays.fill(matches, false);
		for (int i = 0; i < N; i++){
			double piSum = 0.0;
			for (int j = 0; j < N; j++){
				piSum += cplex.getValue(piVar[i][j]);
			}
			if (piSum > TOL){
				matches[i] = true;
			}
		}
		return matches;
	}
	
	public ArrayList<Pair<Integer,Integer>> findMatchedPairs()
	throws IloException{
		ArrayList<Pair<Integer, Integer>> matchPairs = new ArrayList<Pair<Integer, Integer>>();	
		for (int i = 0; i < N; i++){
			for (int j = 0; j < N; j++){
				if (cplex.getValue(piVar[i][j]) > TOL){
					matchPairs.add(new Pair<Integer, Integer>(i, j));
					break;
				}
			}
		}
		return matchPairs;
	}

    public ArrayList<Node> findMatchedNodes() throws Exception {
        ArrayList<Node> matchedNodes = new ArrayList<Node>();
        Boolean[] mat = findMatches();
        for (int i = 0; i < nodes.size(); ++i) {
            if (mat[i]) {
                matchedNodes.add(nodes.get(i));
            }
        }
        return matchedNodes;
    }
	
	public void printPi() 
	throws IloException{
		int N = nodes.size();
		for (int i = 0; i < N; i++){
			for (int j = 0; j < N; j++){
				double pi_ij = cplex.getValue(piVar[i][j]); 
				System.out.print(pi_ij + " ");
			}
			System.out.println();
		}
	}
	
	public void printCoeffs(){
		int N = nodes.size();
		for (int i = 0; i < N; i++){
			for (int j = 0; j < N; j++){
				System.out.print(coeffs[i][j] + " ");
			}
			System.out.println();
		} 
	}
	
	public void clear()
	throws IloException{
		cplex.clearModel();
		if (nodes != null){
			nodes.clear();
		}
	}
	
	public void newNodes(ArrayList<Node> newNodes)
	throws IloException{
		clear();
		nodes = new ArrayList<Node>();
		for (int i = 0; i < newNodes.size(); i++){
			nodes.add(newNodes.get(i));
		}
		init();
	}
	
	private void init()
	throws IloException{
		N = nodes.size();
		
		double[] lb = new double[N];
		double[] ub = new double[N];
		Arrays.fill(lb, 0.0);
		Arrays.fill(ub, 1.0);
		
		piVar = new IloNumVar[N][];
		coeffs = new double[N][N];
		for (int i = 0; i < N; i++){
			piVar[i] = cplex.numVarArray(N, lb, ub);
		}
		
		// objective
		IloLinearNumExpr obj = cplex.linearNumExpr();
		for (int i = 0; i < N; i++){
			Node node1 = nodes.get(i);
			for (int j = 0; j < N; j++){
				Node node2 = nodes.get(j);
				coeffs[i][j] = nrf.evaluate(node1, node2);
				obj.addTerm(piVar[i][j], coeffs[i][j]);
			}
		}
		cplex.addMaximize(obj);		

		// constraints
		for (int i = 0; i < N; i++){
			IloLinearNumExpr incoming = cplex.linearNumExpr();
			IloLinearNumExpr outgoing = cplex.linearNumExpr();
			for (int j = 0; j < N; j++){
				if (i != j){
					incoming.addTerm(piVar[j][i], 1.0);
					outgoing.addTerm(piVar[i][j], 1.0);
				}
			}
			// flow balance constraint
			cplex.addEq(incoming, outgoing);
			incoming.addTerm(piVar[i][i], 1.0);
			// unit flow constraint
			cplex.addLe(incoming, 1.0);
		}
	}

    public double value(NodeRewardFunction nrf)  throws Exception {
        double value = 0.;
        for (int i = 0; i < N; i++){
            for (int j = 0; j < N; j++){
                if (cplex.getValue(piVar[i][j]) > TOL){
                    value += nrf.evaluate(nodes.get(i), nodes.get(j));
                    break;
                }
            }
        }
        return value;
    }

	public class NullOutputStream extends OutputStream {
		@Override
		 public void write(int b) throws IOException {
		 }
	}
}
