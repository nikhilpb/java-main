package com.moallemi.matchingpools;

import com.moallemi.matching.RewardFunction;

public class NodeRewardFunction {
	private RewardFunction rf;
	private NodeFunction nf;
	private double multiplier;
	private boolean hasNF = false;
	
	public NodeRewardFunction(RewardFunction rf){
		this.rf = rf;
	}
	
	public NodeRewardFunction(RewardFunction rf,
								NodeFunction nf,
								double multiplier){
		this.rf = rf;
		this.nf = nf;
		this.multiplier = multiplier;
		this.hasNF = true;
	}
	
	public double evaluate(Node node1, Node node2){
		if (hasNF){
			return rf.evaluate(node1.getSItem(), node2.getDItem())
					- multiplier*nf.evaluate(node1);
		}
		else{
			return rf.evaluate(node1.getSItem(), node2.getDItem());
		}
	}
	
	public RewardFunction getRf(){
		return this.rf;
	}
}
