package com.nikhilpb.pools;

import java.util.*;

import com.moallemi.matching.Item;
import com.moallemi.matching.MultiDiscreteDistribution;
import com.moallemi.matching.RewardFunction;
import com.moallemi.matching.SeparableRewardFunction;
import com.moallemi.math.Distributions;
import com.moallemi.util.PropertySet;

public class MatchingPoolsModel {
	protected int dimension;
    protected int[] tissues;
    protected MultiDiscreteDistribution distribution;
	protected RewardFunction rewardFunction;
	protected NodeRewardFunction nrf;
	protected String modelType;
	protected Random random;
	protected double departureRate, meanArrivalCount, sodBias;
	protected double initialPopulationParameter;
	
	public void initiateRandom(long seed){
		random = new Random(seed);
	}
	
	public Random getRandom(){
		return random;
	}
	
	public RewardFunction getRewardFunction(){
		return rewardFunction;
	}
	
	public NodeRewardFunction getNodeRewardFunction(){
		return nrf;
	}
	
	public int getDimentsion(){
		return dimension;
	}
	
	public int[] getTissues(){
		return tissues;
	}
	
	public void setRandom(Random rand){
		distribution.setRandom(rand);
	}
	
	public void setRandomSeed(long seed){
		distribution.setRandom(new Random(seed));
	}
	
	public double getInitialPopulationParameter(){
		return initialPopulationParameter;
	}
	
	public double getDepartureRate(){
		return departureRate;
	}
	
	public MatchingPoolsModel(PropertySet props){
		double[][] probs;
		dimension = props.getInt("supply_type_dimentions");
		tissues = new int[dimension];
		probs = new double[dimension][];
		for (int i = 0; i < dimension; i++){
			tissues[i] = props.getIntDefault("st[" + (i+1) + "]",0);
			probs[i] = new double[tissues[i]];
			for (int j = 0; j < tissues[i]; j++){
				probs[i][j] = props.getDoubleDefault("sp[" + (i+1) + "][" + (j+1) + "]", 0.0);
			}
		}
		random = new Random(props.getLongDefault("supply_seed",123L));
		distribution = new MultiDiscreteDistribution(probs, random);	    
		if (props.getStringDefault("reward_function","separable").equals("separable")){
			rewardFunction = new SeparableRewardFunction();
			nrf = new NodeRewardFunction(rewardFunction);
		}
        departureRate = props.getDoubleDefault("supply_departure_rate", 0.1);
        meanArrivalCount = props.getDoubleDefault("mean_arrival_count", 5);
        sodBias = props.getDoubleDefault("sod_bias", 0.5);
        initialPopulationParameter = props.getDoubleDefault("initial_population_parameter", 0.2);
	}
	
	public ArrayList<Node> sampleNodes(){
		int arrivalCount = Distributions.nextGeometric(random, 1.0 - 1.0/meanArrivalCount);
		return this.sampleNodes(arrivalCount);
	}
	
	public ArrayList<Node> sampleNodes(int n){
		ArrayList<Node> nodesSampled = new ArrayList<Node>(n);
		for (int i = 0; i < n; i++){
			Item nodeS = new Item(distribution.nextSample());
			nodeS.specifySod(1);
			Item nodeD = new Item(distribution.nextSample());
			nodeD.specifySod(0);
			Node node = new Node(nodeS, nodeD);
			nodesSampled.add(node);
		}
		return nodesSampled;
	}
	
	public ArrayList<Integer> sampleDepartures(ArrayList<Node> currentList){
		ArrayList<Integer> remaining =  new ArrayList<Integer>(currentList.size());
		for (int i = 0; i < currentList.size(); i++){
			remaining.add(Distributions.nextBinomial(random, 1, 1-departureRate));
		}
		return remaining;
	}
	
	public void printInfo(){	 
		System.out.println("dimension is: " + dimension);
		System.out.print("tissue types: "); 
		for (int i = 0; i < dimension; i++){
			System.out.print(tissues[i] + " ");
		}
		System.out.println(" ");
		
		System.out.println("probabilities  -");
		for (int i = 0; i < dimension; i++){
			System.out.print("along dim " + (i+1) + ": ");
			for (int j = 0; j < tissues[i]; j++){
				System.out.print(distribution.getProb(i,j) + " ");
			}
			System.out.println(" ");
		}
	}
}
