package com.moallemi.matchingpools;

import com.moallemi.matching.Item;

public class Node {
	Item sItem, dItem;
	public Node(Item sItem, Item dItem){
		this.sItem = sItem;
		this.dItem = dItem;
		if ((sItem.isSod() != 1) 
		||  (dItem.isSod() != 0) 
		||  (sItem.getDimensions() != dItem.getDimensions())){
			System.out.printf("Improper single nodes");
		}
	}
	
	public int getDimension(){
		return sItem.getDimensions();
	}

	public Item getSItem(){
		return sItem;
	}
	
	public Item getDItem(){
		return dItem;
	}
	
	public String toString(){
		return sItem.toString() + "; " + dItem.toString();
	}
	
}
