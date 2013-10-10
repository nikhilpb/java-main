package com.moallemi.matchingpools;

import ilog.cplex.IloCplex;

import java.io.*;
import java.util.*;

import com.moallemi.matching.Item;
import com.moallemi.math.CplexFactory;
import com.moallemi.util.*;
import com.moallemi.util.data.*;

public class MatchingPoolsMain extends CommandLineMain {
	MatchingPoolsModel model;
	InstanceSet instances;
	NodeFunctionSet basisSet;
	LpSolver solver;
	NodeFunction valueFunction;
	
	protected boolean processCommand(final CommandLineIterator cmd) 
			throws Exception 
    	{
		String base = cmd.next();
		if (base.equals("model")){
			String fname = cmd.next();
			System.out.println("loading online matching model: " + fname);
			PropertySet props = new PropertySet(new File(fname));
			model = new MatchingPoolsModel(props);
		}
		else if (base.equals("sample")){
			System.out.println("sampling kidney pools");
			int sampleSize = cmd.nextInt();
			System.out.println("sample size: " + sampleSize);
			int problemSize = cmd.nextInt();
			System.out.println("problem size: " + problemSize);
			long sampleSeed = cmd.nextLong();
			System.out.println("sample seed is: " + sampleSeed);
			instances = new InstanceSet(model, problemSize, sampleSeed);
			instances.sample(sampleSize);
			instances.match("greedy", getCplexFactory());
		}
		else if (base.equals("basis")){
			String basisType = cmd.next();
			if (basisType.equals("separable")){
				int dim = model.getDimentsion();
				int[] typesPerDim = model.getTissues();
				basisSet = new NodeFunctionSet();
				for (int fos = 0; fos < 2; fos++){
					for (int i = 0; i < dim; i++){
						for (int j = 0; j < typesPerDim[i]; j++){
							FirstOrderNodeFunction nf = new FirstOrderNodeFunction(i,j,fos);
							basisSet.add(nf);
						}	
					}
				}
				basisSet.add(new ConstantNodeFunction(1.0));	
			}
		}
		else if (base.equals("solve")){
			String solverType = cmd.next();
			if (solverType.equals("salp")){
				System.out.println("solving salp");
				double eps = cmd.nextDouble();
				System.out.println("epsilon = " + eps);
				solver = new LpSolver(getCplexFactory(),
										instances,
										basisSet,
										model, 
										eps);
				boolean status = solver.solve();
				if (status){
					System.out.println("successfully solved model");
				}
				valueFunction = solver.getValue();
			}
			else {
				System.err.println("incorect solver type");
			}
		}
		else if (base.equals("value")){
			String policyType = cmd.next();
			NodeRewardFunction nrf = null;
			if (policyType.equals("greedy")){
				System.out.println("matching using greedy policy");
				nrf = model.getNodeRewardFunction();
			}
			else if(policyType.equals("vf")){
				System.out.println("matching using value function policy");
				nrf = new NodeRewardFunction(model.getNodeRewardFunction().getRf(),
											valueFunction,
											1-model.getDepartureRate());
			}
			else if(policyType.equals("offline")){
				System.out.println("matching using the offline policy");
			}
			int runCount = cmd.nextInt();
			System.out.println("number of sample runs is " + runCount);
			int timePeriods = cmd.nextInt();
			System.out.println("time periods is " + timePeriods);
			long simValueSeed = cmd.nextLong();
			System.out.println("using seed " + simValueSeed);
			Random svRandom = new Random(simValueSeed);
			double[] value = new double[runCount];
			SampleInstance inst;
			CplexFactory factory = getCplexFactory();
			IloCplex cplex = factory.getCplex();
			double mean = 0.0, std = 0.0, sterr = 0.0;
			for (int s=0; s < runCount; s++){
				inst = new SampleInstance(model, timePeriods, svRandom.nextLong());
				inst.sample();
				if (policyType.equals("offline")){
					value[s] = inst.offlineMatch(cplex);
				}
				else{
					value[s] = inst.greedyMatch(factory, nrf);
				}
				mean += value[s];
			}
			mean = mean/runCount;
			for (int s = 0; s < runCount; s++){
				std += Math.pow((value[s] - mean),2);
			}
			std = Math.pow(std/runCount, 0.5);
			sterr = std/ Math.pow(runCount, 0.5);
			System.out.println("mean reward: " + mean 
								+ ", standard deviation: " + std
								+ ", standard error: " + sterr);
		}
		// tests from here on
		else if (base.equals("test")){
			String testName = cmd.next();
			if (testName.equals("matcher-test")){
				System.out.println("starting matcher test");
				
				// made up example
				Item item1 = new Item(new Integer[]{4,0,2}, 1);
				Item item2 = new Item(new Integer[]{4,1,3}, 0);
				Item item3 = new Item(new Integer[]{0,1,0}, 1);
				Item item4 = new Item(new Integer[]{2,2,2}, 0);
				Item item5 = new Item(new Integer[]{1,2,3}, 1);
				Item item6 = new Item(new Integer[]{2,3,2}, 0);
				Item item7 = new Item(new Integer[]{1,2,3}, 1);
				Item item8 = new Item(new Integer[]{4,5,6}, 0);
				Node node1 = new Node(item1, item2);
				Node node2 = new Node(item3, item4);
				Node node3 = new Node(item1, item4);
				Node node4 = new Node(item5, item6);
				Node node5 = new Node(item5, item2);
				Node node6 = new Node(item7, item8);
				ArrayList<Node> nodeList = new ArrayList<Node>();
				nodeList.add(node1); nodeList.add(node2); nodeList.add(node3);
				nodeList.add(node4); nodeList.add(node5);
				nodeList.add(node6);
				
				System.out.println("nodes to be matched are: " + nodeList.toString());
				KidneyPoolsMatcher matcher = new KidneyPoolsMatcher(getCplexFactory(),
																	nodeList,
																	model.getNodeRewardFunction());
				
				System.out.println("printing objective coefficients");
				matcher.printCoeffs();
				matcher.solve();
				System.out.println("the optimal solution is:");
				matcher.printPi();
				
				System.out.println("printing matched node indices");
				Boolean[] matches = matcher.findMatches();
				for (int i = 0; i < nodeList.size(); i++){
					if (matches[i]){
						System.out.println("node " + i + " is matched");
					}
				}
				
				System.out.println("printing the pairs matched");
				ArrayList<Pair<Integer,Integer>> matchedPairs = matcher.findMatchedPairs();
				for (int i = 0; i < matchedPairs.size(); i++){
					System.out.println(matchedPairs.get(i).getFirst() + 
										" -> " + 
										matchedPairs.get(i).getSecond());
				}
			}
			else if (testName.equals("sample-test")){
				SampleInstance sInstance = instances.get(0);
				sInstance.printStates();
			}
			else if (testName.equals("greedy-match")){
				int problemSize = 10;
				long problemSeed = 101;
				System.out.println("performing greedy matching test of problem size: " 
									+ problemSize 
									+ ", with seed: "
									+ problemSeed);
				SampleInstance instance = new SampleInstance(model, 
															problemSize, 
															problemSeed);
				instance.sample();
				instance.printStates();
				instance.match("greedy", getCplexFactory());
				instance.printMatchedStates();
			}
			else if (testName.equals("basis")){
				Item item1 = new Item(new Integer[]{0,0,1,2,0,1});
				Item item2 = new Item(new Integer[]{1,1,0,2,1,1});
				Node node = new Node(item1, item2);
				System.out.println("evaluating node " + node.toString() + " on the basis set: ");
				for (int i = 0; i < basisSet.size(); i++){
					System.out.println("function, "
									+ basisSet.getFunction(i).toString()
									+ ", "
									+ basisSet.getFunction(i).evaluate(node));
				}
			}
		}
		else {
			return false;
		}
		return true;
    }
	
	public static void main(String[] argv) throws Exception {
   		(new MatchingPoolsMain()).run(argv);
	}

}
