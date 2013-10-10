package com.moallemi.matchingpools;

public class ConstantNodeFunction implements NodeFunction {

	private double value;
	
	public ConstantNodeFunction(double value){
		this.value = value;
	}
	
	public double evaluate(Node node) {
		return value;
	}
	
	public String toString(){
		return ("constant function that returns " + value);
	}

}
