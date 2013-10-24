package com.nikhilpb.stopping;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/24/13
 * Time: 1:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class ColumnStoreArguments {
    public StoppingModel stoppingModel;
    public MeanGaussianKernel oneExp, twoExp;
    public GaussianStateKernel kernel;
    public ArrayList<ArrayList<StoppingState>> stateList;
}