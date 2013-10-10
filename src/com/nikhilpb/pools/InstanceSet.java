package com.nikhilpb.pools;

import java.util.ArrayList;
import java.util.Random;

import com.moallemi.math.CplexFactory;

import ilog.concert.IloException;

public class InstanceSet {
	private ArrayList<SampleInstance> instances;
	private MatchingPoolsModel model;
	private Random random;
	
	public InstanceSet (MatchingPoolsModel model,
						long seed){
		this.model = model;
		random = new Random(seed);
	}
	
	public void sample (int sampleCount){
		instances = new ArrayList<SampleInstance>(sampleCount);
		SampleInstance thisInstance;
		for (int i = 0; i < sampleCount; i++){
			thisInstance = new SampleInstance(model,
											  random.nextLong());
			thisInstance.sample();
			instances.add(thisInstance);
		}
	}
	
	public void match(String matchType, CplexFactory factory)
	throws IloException{
		for (int i = 0; i < instances.size(); i++){
			instances.get(i).match(matchType, factory);
		}
	}
	
	public int size(){
		return instances.size();
	}
	
	public SampleInstance get(int i){
		if (i >= instances.size() || i < -1){
			System.out.println("incorrect index");
			return null;
		}
		else {
			return instances.get(i);
		}
	}
}
