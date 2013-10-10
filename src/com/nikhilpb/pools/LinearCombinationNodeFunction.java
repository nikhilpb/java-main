package com.moallemi.matchingpools;

import java.util.ArrayList;

public class LinearCombinationNodeFunction implements NodeFunction {
	private ArrayList<NodeFunction> functionList;
	private double[] r;
	
	public LinearCombinationNodeFunction(ArrayList<NodeFunction> functionList, double[] r){
		if (functionList.size() == r.length){	
			this.functionList = functionList;
			this.r = r;
		}
		else{
			System.out.println("dimentions of function list and r don't match");
		}
	}
	
	public double evaluate(Node node){
		double out = 0.0;
		for (int i = 0; i < r.length; i++){
			out += r[i]*(functionList.get(i)).evaluate(node);
		}
		return out;
	}
	
	public String toString(){
		String name = "";
		for (int i = 0; i < r.length; i++){
			name += (r[i] + "*"  + functionList.get(i).toString() + "\n");
			if (i < r.length - 1){
				name += " + ";
			}
		}
		return name;
	}
}
