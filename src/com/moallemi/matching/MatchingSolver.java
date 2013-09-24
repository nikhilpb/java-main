package com.moallemi.matching;


import ilog.concert.*;


public interface MatchingSolver {

  public boolean solve() throws IloException;

  public ItemFunction getSupplyValue() throws IloException;

  public ItemFunction getDemandValue() throws IloException;
}
