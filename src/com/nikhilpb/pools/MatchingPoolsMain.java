package com.nikhilpb.pools;

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
		else {
			return false;
		}
		return true;
    }
	
	public static void main(String[] argv) throws Exception {
   		(new MatchingPoolsMain()).run(argv);
	}

}
