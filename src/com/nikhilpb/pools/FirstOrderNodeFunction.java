package com.moallemi.matchingpools;

public class FirstOrderNodeFunction implements NodeFunction {
	private int tissue;
	private int level;
	private int fos;
	
	public FirstOrderNodeFunction(int tissue, int level, int fos){
		this.tissue = tissue;
		this.level = level;
		if(fos < 0 || fos > 1){
			System.err.print("invalid specification of donor or recipient, setting to donor");
			this.fos = 0;
		}
		else{
			this.fos = fos;
		}
	}
	
	public double evaluate(Node node){
		if (tissue >= node.getDimension()){
			System.out.println("invalid function for this type");
			return -1.0;
		}
		else {
			if (fos == 0){
				if (level == node.getSItem().getTypeAtDimension(tissue)){
					return 1.0;
				}
			}
			else {
				if (level == node.getDItem().getTypeAtDimension(tissue)){
					return 1.0;
				}
			}
		}
		return 0.0;
	}
	
	public String toString(){
		if (fos == 0){
			return "indicator-tissue:"+tissue+"-level:"+level+"donor";
		}
		else{
			return "indicator-tissue:"+tissue+"-level:"+level+"recipient";
		}
	}
}
