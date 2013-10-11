package com.nikhilpb.pools;

import ilog.concert.*;
import ilog.cplex.*;


import java.util.ArrayList;

import com.moallemi.math.CplexFactory;
import com.moallemi.math.Distributions;
import com.moallemi.util.data.Pair;

public class SampleInstance {
	private ArrayList<Node> allExistingNodes;
	private ArrayList<Integer> arrivalTimes, departureTimes, matchTimes;
	private ArrayList<ArrayList<Integer>> states, matchedStates, matchList;
	private ArrayList<Double> rewards;
	ArrayList<ArrayList<Pair<Integer, Integer>>> matchedPairs;
	private int timePeriods;
	private MatchingPoolsModel model;
	private boolean isSampled = false, isMatched = false;
	
	double[] nodeValues;
	boolean foundValues = false;
	
	public SampleInstance(MatchingPoolsModel model,
			long seed){
		this.model = model;
		this.timePeriods = model.getTimePeriods();
		model.initiateRandom(seed);
	}
	
	public void sample(){
		double initPopParameter = model.getInitialPopulationParameter();
		int initPop = Distributions.nextGeometric(model.getRandom(), 1.0-initPopParameter);
		allExistingNodes = model.sampleNodes(initPop);
		arrivalTimes = new ArrayList<Integer>();
		departureTimes = new ArrayList<Integer>();
		ArrayList<Node> arrivals;
		ArrayList<Node> currentNodes = new ArrayList<Node>();
		ArrayList<Integer> currentNodesMap = new ArrayList<Integer>();
		ArrayList<Integer> departureInd;
		for (int i = 0; i < initPop; i++){
			arrivalTimes.add(0);
			departureTimes.add(Integer.MAX_VALUE);
			currentNodes.add(allExistingNodes.get(i));
			currentNodesMap.add(departureTimes.size() - 1);
		}
		for (int t = 1; t < timePeriods; t++){
			// departures
			departureInd = model.sampleDepartures(currentNodes);
			for (int i = departureInd.size()-1; i > -1; i--){
				if (departureInd.get(i) == 0){
					departureTimes.set(currentNodesMap.get(i), t-1);
					currentNodes.remove(i);
					currentNodesMap.remove(i);
				}
			}
			
			//arrivals
			arrivals = model.sampleNodes();
			allExistingNodes.addAll(arrivals);
			currentNodes.addAll(arrivals);
			for (int i = 0; i < arrivals.size(); i++){
				arrivalTimes.add(t);
				departureTimes.add(Integer.MAX_VALUE);
				currentNodesMap.add(departureTimes.size() - 1);
			}
		}
		isSampled = true;
	}
	
	public void printStates(){
		if (!isSampled){
			System.out.println("instance not sampled yet");
			return;
		}
		findStates();
		for (int t = 0; t <= timePeriods; t++){
			System.out.print("time " + t + ": ");
			System.out.println(states.get(t).toString());
		}
	}
	
	private void findStates(){
		states = new ArrayList<ArrayList<Integer>>(timePeriods + 1);
		for (int t = 0; t <= timePeriods; t++){
			states.add(new ArrayList<Integer>());
		}
		int N = allExistingNodes.size(); 
		for (int i = 0; i < N; i++){
			int aTime = arrivalTimes.get(i);
			int dTime = Math.min(departureTimes.get(i),timePeriods);
			for (int t = aTime; t <= dTime; t++){
				states.get(t).add(i);
			}
		}
	}
	
	public void match(String matchType, CplexFactory factory)
	throws IloException{
		if (matchType.equals("greedy")){
			greedyMatch(factory, model.getNodeRewardFunction());
		}
		else {
			System.out.println("invalid match type");
			return;
		}
		isMatched = true;
		return;
	}
	
	public double greedyMatch(CplexFactory factory, NodeRewardFunction nrf)
	throws IloException{
		double value = 0.0;
		matchedStates = new ArrayList<ArrayList<Integer>>(timePeriods + 1);
		ArrayList<Integer> curState = new ArrayList<Integer>();
		ArrayList<Node> curNodes = new ArrayList<Node>();
		int N = allExistingNodes.size();
		matchTimes = new ArrayList<Integer>();
		for (int i = 0; i < N; i++){
			matchTimes.add(departureTimes.get(i));
		}
		Boolean[] curMatches;
		ArrayList<Pair<Integer, Integer>> curMatchedPairs;
		matchedPairs = new ArrayList<ArrayList<Pair<Integer, Integer>>>(timePeriods + 1);
		matchList = new ArrayList<ArrayList<Integer>>();
		rewards = new ArrayList<Double>(timePeriods + 1);
		KidneyPoolsMatcher matcher = new KidneyPoolsMatcher(factory, 
															nrf);
		
		for (int t = 0; t <= timePeriods; t++){
			
			// remove departed nodes
			for (int i = curState.size()-1; i > -1; i--){
				if (t > departureTimes.get(curState.get(i))){
					curState.remove(i);
				}
			}
			
			// add arrived nodes
			for (int i = 0; i < N; i++){
				if (arrivalTimes.get(i) == t){
					curState.add(i);
				}
			}
			
			// make the current list of nodes
			curNodes.clear();
			ArrayList<Integer> curStateCopy = new ArrayList<Integer>();
			for (int i = 0; i < curState.size(); i++){
				curNodes.add(allExistingNodes.get(curState.get(i)));
				curStateCopy.add(curState.get(i));
			}
			matchedStates.add(curStateCopy);
			
			// greedily match nodes
			matcher.clear();
			matcher.newNodes(curNodes);
			matcher.solve();
			curMatches = matcher.findMatches();
			
			// add the matched pairs
			curMatchedPairs = matcher.findMatchedPairs();
			ArrayList<Pair<Integer, Integer>> cmpCopy = new ArrayList<Pair<Integer, Integer>>();
			double curReward = 0.0;
			for (int i = 0; i < curMatchedPairs.size(); i++){
				Pair<Integer, Integer> pair, pairCopy;
				pair = curMatchedPairs.get(i);
				pairCopy = new Pair<Integer, Integer>(curState.get(pair.getFirst()),
														curState.get(pair.getSecond()));
				cmpCopy.add(pairCopy);
				Node node1 = allExistingNodes.get(curState.get(pair.getFirst()));
				Node node2 = allExistingNodes.get(curState.get(pair.getSecond()));
				curReward += model.getNodeRewardFunction().evaluate(node1, node2);
			}
			value += curReward;
			rewards.add(curReward);
			
			matchedPairs.add(cmpCopy);
			
			// remove matched nodes
			ArrayList<Integer> cmlCopy = new ArrayList<Integer>();
			for (int i = curState.size()-1; i > -1; i--){
				if (curMatches[i]){
					matchTimes.set(curState.get(i), t);
					cmlCopy.add(curState.get(i));
					curState.remove(i);
				}
			}
			matchList.add(cmlCopy);
		}
		return value;
	}
	
	// for debugging
	public void printMatchedStates(){
		if (!isMatched){
			System.err.println("instance not matched yet");
		}
		for (int t = 0; t <= timePeriods; t++){
			System.out.print("time " + t + ": ");
			System.out.println(matchedStates.get(t).toString());
			System.out.print("matched nodes at this time: ");
			for (int i = 0; i < allExistingNodes.size(); i++){
				if (matchTimes.get(i) == t){
					System.out.print(i + " ");
				}
			}
			System.out.println();
			System.out.print("matched pairs in this time period: ");
			ArrayList<Pair<Integer, Integer>> curMatchedPairs = matchedPairs.get(t);
			System.out.println("reward: " + rewards.get(t));
			for (int i = 0; i < curMatchedPairs.size(); i++){
				Pair<Integer, Integer> pair = curMatchedPairs.get(i);
				
				System.out.println("\t"
								+ allExistingNodes.get(pair.getFirst()).toString() 
								+ " -> " 
								+ allExistingNodes.get(pair.getSecond()).toString() 
								+ " : ");
			}
		}
	}
	
	public int getTimePeriods(){
		return this.timePeriods;
	}
	
	public ArrayList<Node> getState(int t){
		ArrayList<Node> curState = new ArrayList<Node>();
		ArrayList<Integer> curStateInd = matchedStates.get(t);
		for (int i = 0; i < curStateInd.size(); i++){
			curState.add(allExistingNodes.get(curStateInd.get(i)));
		}
		return curState;
	}

	public ArrayList<Node> getMatches(int t){
		ArrayList<Node> curMatches = new ArrayList<Node>();
		ArrayList<Integer> curMatchInd = matchList.get(t);
		for (int i = 0; i < curMatchInd.size(); i++){
			curMatches.add(allExistingNodes.get(curMatchInd.get(i)));
		}
		return curMatches;
	}
	
	public double getReward(int t){
		return rewards.get(t);
	}
	
	public boolean existsAtTime(int i, int t){
		if (!isSampled){
			System.err.println("the instance isn't sampled yet");
			return false;
		}
		else {
			return (arrivalTimes.get(i) <= t) && (departureTimes.get(i) >= t);
		}
	}
	
	public double offlineMatch(IloCplex cplex)
	throws IloException{
		cplex.clearModel();
		if (!isSampled){
			System.err.print("the instance not sampled yet");
		}
		int N = allExistingNodes.size();
		boolean eI, eJ;
		
		// set up variables
		IloNumVar[][][] piVar = new IloNumVar[timePeriods+1][N][N];
		for (int t = 0; t <= timePeriods; t++){
			for (int i = 0; i < N; i++){
				eI = existsAtTime(i, t); 
				for (int j = 0; j < N; j++){
					eJ = existsAtTime(j, t);
					if (!eI || !eJ){
						piVar[t][i][j] = cplex.numVar(0.0, 0.0);
					}
					else{
						piVar[t][i][j] = cplex.numVar(0.0, 1.0);
					}
				}
			}
		}
		
		// add constraints
		IloLinearNumExpr[] incomingCum = new IloLinearNumExpr[N];
		for (int i = 0; i < N; i++){
			incomingCum[i] = cplex.linearNumExpr();
		}
		for (int t = 0; t <= timePeriods; t++){
			IloLinearNumExpr[] incoming = new IloLinearNumExpr[N];
			for (int i = 0; i < N; i++){	
				eI = existsAtTime(i, t);
				incoming[i] = cplex.linearNumExpr();
				if (eI){
					IloLinearNumExpr outgoing = cplex.linearNumExpr();
					for (int j = 0; j < N; j++){
						eJ = existsAtTime(j, t);
						if (eJ){
							incoming[i].addTerm(piVar[t][j][i], 1.0);
							outgoing.addTerm(piVar[t][i][j], 1.0);
						}
					}
					cplex.addEq(incoming[i], outgoing);
				}
				incomingCum[i].add(incoming[i]);
				cplex.addLe(incomingCum[i], 1.0);
			}
		}
		
		
		// add objective
		IloLinearNumExpr obj = cplex.linearNumExpr();
		NodeRewardFunction nrf = model.getNodeRewardFunction();
		for (int t = 0; t <= timePeriods; t++){		
			for (int i = 0; i < N; i++){
				Node node1 = allExistingNodes.get(i);
				for (int j = 0; j < N; j++){
					Node node2 = allExistingNodes.get(j);
					double weight = nrf.evaluate(node1, node2);
					obj.addTerm(piVar[t][i][j], weight);
				}
			}
		}
		cplex.addMaximize(obj);	
		cplex.solve();
		return cplex.getObjValue();
	}
	
	
}


