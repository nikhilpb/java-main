package com.nikhilpb.matching;

import com.moallemi.math.CplexFactory;
import com.moallemi.math.Distributions;
import com.moallemi.math.LawlerBipartiteMatcher;
import com.moallemi.util.data.Pair;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 9/22/13
 * Time: 3:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class MatchingSamplePath {
    private ArrayList<Item> allExistingItems;
    private ArrayList<Integer> arrivalTimes, departureTimes, matchTimes;
    private ArrayList<ArrayList<Integer>> states;
    private ArrayList<ArrayList<Pair<Integer, Integer>>> matchedPairs;
    private int timePeriods, initialPopulationSize;
    private MatchingModel model;
    private boolean isSampled = false, isMatched = false;
    private LawlerBipartiteMatcher biparMatcher;

    ArrayList<Item> supplyItems, demandItems;
    ArrayList<Integer> supplyItemsI, demandItemsI;
    double[] supplyValues, demandValues;
    boolean foundValues = false;

    /**
     * Constructor. Initial population size sampled.
     *
     * @param model                matching model
     * @param seed                 seed for sampling'
     */
    public MatchingSamplePath(MatchingModel model,
                              final long seed) {
        this.model = model;
        this.timePeriods = model.getTimePeriods();
        model.initiateRandom(seed);
        this.initialPopulationSize = Distributions.nextGeometric(model.getRandom(), model.getInitPopParam());
    }

    public void sample() {
        allExistingItems = model.sampleArrivals(initialPopulationSize);
        arrivalTimes = new ArrayList<Integer>();
        departureTimes = new ArrayList<Integer>();
        ArrayList<Item> arrivals;
        ArrayList<Item> currentTypes = new ArrayList<Item>();
        ArrayList<Integer> currentTypesMap = new ArrayList<Integer>();
        for (int i = 0; i < initialPopulationSize; i++) {
            arrivalTimes.add(0);
            departureTimes.add(Integer.MAX_VALUE);
            currentTypes.add(allExistingItems.get(i));
            currentTypesMap.add(departureTimes.size() - 1);
        }
        ArrayList<Integer> departureInd;
        for (int t = 1; t < timePeriods + 1; t++) {
            // departures
            departureInd = model.sampleDepartures(currentTypes);
            for (int i = departureInd.size() - 1; i > -1; i--) {
                if (departureInd.get(i) == 0) {
                    departureTimes.set(currentTypesMap.get(i), t);
                    currentTypes.remove(i);
                    currentTypesMap.remove(i);
                }
            }
            // arrivals
            arrivals = model.sampleArrivals();
            allExistingItems.addAll(arrivals);
            currentTypes.addAll(arrivals);
            for (int i = 0; i < arrivals.size(); i++) {
                arrivalTimes.add(t);
                departureTimes.add(Integer.MAX_VALUE);
                currentTypesMap.add(departureTimes.size() - 1);
            }
        }
        isSampled = true;
    }

    public double offlineMatch() {
        if (!isSampled) {
            System.out.println("the instance is not yet sampled");
            return 0.0;
        }

        // stores the location of supply and demand items in an ArrayList
        ArrayList<Integer> supplyItemIndex = new ArrayList<Integer>();
        ArrayList<Integer> demandItemIndex = new ArrayList<Integer>();
        for (int i = 0; i < allExistingItems.size(); i++) {
            if (allExistingItems.get(i).isSod() == 0) {
                demandItemIndex.add(i);
            } else {
                supplyItemIndex.add(i);
            }
        }

        int n = Math.max(supplyItemIndex.size(), demandItemIndex.size());
        biparMatcher = new LawlerBipartiteMatcher(n);
        double[][] w = new double[n][];
        for (int i = 0; i < n; i++) {
            w[i] = new double[n];
            Arrays.fill(w[i], 0);
        }

        // two items that co-exist in the same period can be matched
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if ((i >= supplyItemIndex.size()) || (j >= demandItemIndex.size())) {
                    w[i][j] = 0;
                } else {
                    Item itemS = allExistingItems.get(supplyItemIndex.get(i));
                    Item itemD = allExistingItems.get(demandItemIndex.get(j));
                    int aS = arrivalTimes.get(supplyItemIndex.get(i));
                    int dS = departureTimes.get(supplyItemIndex.get(i));
                    int aD = arrivalTimes.get(demandItemIndex.get(j));
                    int dD = departureTimes.get(demandItemIndex.get(j));
                    if ((aS > dD) || (aD > dS)) {
                        w[i][j] = 0.0;
                    } else {
                        w[i][j] = model.getRewardFunction().evaluate(itemS, itemD);
                    }
                }
            }
        }
        biparMatcher.computeMax(w);

        matchTimes = new ArrayList<Integer>(allExistingItems.size());
        for (int i = 0; i < allExistingItems.size(); i++) {
            matchTimes.add(departureTimes.get(i));
        }

        matchedPairs = new ArrayList<ArrayList<Pair<Integer, Integer>>>(timePeriods + 1);
        for (int t = 0; t < timePeriods + 1; t++) {
            matchedPairs.add(new ArrayList<Pair<Integer, Integer>>());
        }
        Pair<Integer, Integer> pair;
        int[] mSource = biparMatcher.getMatchingSource();
        for (int i = 0; i < supplyItemIndex.size(); i++) {
            if (mSource[i] < demandItemIndex.size()) {
                int thisInd = supplyItemIndex.get(i);
                int thisMatchedInd = demandItemIndex.get(mSource[i]);
                if ((arrivalTimes.get(thisInd) <= departureTimes.get(thisMatchedInd)) &&
                        (arrivalTimes.get(thisMatchedInd) <= departureTimes.get(thisInd))) {
                    int thisMatchTime = Math.max(arrivalTimes.get(thisInd),
                            arrivalTimes.get(thisMatchedInd));
                    matchTimes.set(thisInd, thisMatchTime);
                    matchTimes.set(thisMatchedInd, thisMatchTime);
                    pair = new Pair<Integer, Integer>(thisInd, thisMatchedInd);
                    matchedPairs.get(thisMatchTime).add(pair);
                }
            }
        }

        states = new ArrayList<ArrayList<Integer>>(timePeriods + 1);
        for (int t = 0; t < timePeriods + 1; t++) {
            states.add(new ArrayList<Integer>());
        }
        for (int i = 0; i < allExistingItems.size(); i++) {
            for (int t = arrivalTimes.get(i); t <= Math.min(matchTimes.get(i), timePeriods); t++) {
                states.get(t).add(i);
            }
        }

        isMatched = true;
        return biparMatcher.getMatchingWeight();
    }

    /**
     * Print info about the instance showing each item.
     */
    public void printItemInfo() {
        if (!isSampled) {
            System.out.println("the instance is not sampled yet.");
        } else if (!isMatched) {
            for (int i = 0; i < allExistingItems.size(); i++) {
                System.out.println("item no: "
                        + i
                        + ", item type: "
                        + allExistingItems.get(i).toString()
                        + ", arrival time: "
                        + arrivalTimes.get(i)
                        + ", departure time: "
                        + departureTimes.get(i)
                );
            }
        } else {
            for (int i = 0; i < allExistingItems.size(); i++) {
                System.out.println("item no: "
                        + i
                        + ", item type: "
                        + allExistingItems.get(i).toString()
                        + ", arrival time: "
                        + arrivalTimes.get(i)
                        + ", departure time: "
                        + departureTimes.get(i)
                        + ", matching time: "
                        + matchTimes.get(i)
                );
            }
        }
    }

    /**
     * Print info about the instance showing each state.
     */
    public void printStatesInfo() {
        if (!isMatched) {
            System.out.println("this instance is not yet matched");
        } else {
            for (int t = 0; t < timePeriods + 1; t++) {
                System.out.println("Time period " + t + ":");
                System.out.print("existing states: ");
                for (int i = 0; i < states.get(t).size(); i++) {
                    System.out.print(states.get(t).get(i) + " ");
                }
                System.out.println();
                System.out.print("matched states: ");
                for (int i = 0; i < matchedPairs.get(t).size(); i++) {
                    System.out.print("("
                            + (matchedPairs.get(t).get(i)).getFirst()
                            + ","
                            + (matchedPairs.get(t).get(i)).getSecond()
                            + ")");
                }
                System.out.println();
            }
        }
    }

    /**
     * get states for a particular time period.
     *
     * @param t time
     */
    public ArrayList<Item> getStates(int t) {
        if (isMatched) {
            ArrayList<Integer> state = states.get(t);
            ArrayList<Item> stateItems = new ArrayList<Item>(state.size());
            for (int i = 0; i < state.size(); i++) {
                stateItems.add(allExistingItems.get(state.get(i)));
            }
            return stateItems;
        }
        System.err.println("the instance is not matched");
        return null;
    }


    public ArrayList<Pair<Item, Item>> getMatchedPairs(int t) {
        if (isMatched) {
            ArrayList<Pair<Integer, Integer>> matches = matchedPairs.get(t);
            ArrayList<Pair<Item, Item>> matchedsItemPairs =
                    new ArrayList<Pair<Item, Item>>(matches.size());
            for (int i = 0; i < matches.size(); i++) {
                Item first = allExistingItems.get(matches.get(i).getFirst());
                Item second = allExistingItems.get(matches.get(i).getSecond());
                matchedsItemPairs.add(new Pair<Item, Item>(first, second));
            }
            return matchedsItemPairs;
        }
        System.out.println("the instance is not matched");
        return null;
    }

    public int getTimePeriods() {
        return timePeriods;
    }

    public ArrayList<Item> getSupplyItems() {
        supplyItems = new ArrayList<Item>();
        supplyItemsI = new ArrayList<Integer>();
        Item item;
        for (int i = 0; i < allExistingItems.size(); i++) {
            item = allExistingItems.get(i);
            if (item.isSod() == 1) {
                supplyItems.add(item);
                supplyItemsI.add(i);
            }
        }
        return supplyItems;
    }

    public ArrayList<Item> getDemandItems() {
        demandItems = new ArrayList<Item>();
        demandItemsI = new ArrayList<Integer>();
        Item item;
        for (int i = 0; i < allExistingItems.size(); i++) {
            item = allExistingItems.get(i);
            if (item.isSod() == 0) {
                demandItems.add(item);
                demandItemsI.add(i);
            }
        }
        return demandItems;
    }

    public double dualPolicyEvaluate(ItemFunction sf,
                                     ItemFunction df,
                                     CplexFactory factory)
            throws Exception {
        if (!isSampled) {
            throw new RuntimeException("must be sampled");
        }
        System.out.println(sf.toString() + "\n" + df.toString());
        IloCplex cplex = factory.getCplex();
        cplex.setOut(null);
        IloNumVar[][] piVar;
        ArrayList<Item> supplyItems = new ArrayList<Item>();
        ArrayList<Item> demandItems = new ArrayList<Item>();
        ArrayList<Integer> supplyIMap = new ArrayList<Integer>();
        ArrayList<Integer> demandIMap = new ArrayList<Integer>();
        boolean[] matched = new boolean[allExistingItems.size()];
        Arrays.fill(matched, false);
        int supplySize, demandSize;
        double[] lb, ub, ones;
        double coeff;
        double[] pi;
        double tol = 1E-5, qs, qd;
        IloLinearNumExpr tempExp, obj;
        Item sItem, dItem;
        double totalReward = 0.0;
        for (int t = 0; t <= timePeriods; t++) {
            cplex.clearModel();
            supplyItems.clear();
            demandItems.clear();
            supplyIMap.clear();
            demandIMap.clear();
            for (int i = 0; i < allExistingItems.size(); i++) {
                if (!matched[i] && arrivalTimes.get(i) <= t && departureTimes.get(i) >= t) {
                    Item item = allExistingItems.get(i);
                    if (item.isSod() == 1) {
                        supplyItems.add(item);
                        supplyIMap.add(i);
                    } else {
                        demandItems.add(item);
                        demandIMap.add(i);
                    }
                }
            }
            supplySize = supplyItems.size();
            demandSize = demandItems.size();
            piVar = new IloNumVar[supplySize][];
            lb = new double[demandSize];
            ub = new double[demandSize];
            ones = new double[demandSize];
            Arrays.fill(lb, 0.0);
            Arrays.fill(ub, 1.0);
            Arrays.fill(ones, 1.0);
            for (int i = 0; i < supplySize; i++) {
                piVar[i] = cplex.numVarArray(demandSize, lb, ub);
                cplex.addLe(cplex.scalProd(ones, piVar[i]), 1.0);
            }
            for (int j = 0; j < demandSize; j++) {
                tempExp = cplex.linearNumExpr();
                for (int i = 0; i < supplySize; i++) {
                    tempExp.addTerm(1.0, piVar[i][j]);
                }
                cplex.addLe(tempExp, 1.0);
            }

            obj = cplex.linearNumExpr();
            for (int i = 0; i < supplySize; i++) {
                for (int j = 0; j < demandSize; j++) {
                    sItem = supplyItems.get(i);
                    dItem = demandItems.get(j);
                    qs = model.getSupplyDepartureRate();
                    qd = model.getDemandDepartureRate();
                    coeff = model.getRewardFunction().evaluate(sItem, dItem);
                    if (t != timePeriods) {
                        coeff -= (1 - qs) * sf.evaluate(sItem) + (1 - qd) * df.evaluate(dItem);
                    }
                    obj.addTerm(coeff, piVar[i][j]);
                }
            }
            cplex.addMaximize(obj);
            cplex.solve();

            for (int i = 0; i < supplySize; i++) {
                pi = cplex.getValues(piVar[i]);
                for (int j = 0; j < demandSize; j++) {
                    if (pi[j] > 1 - tol) {
                        matched[supplyIMap.get(i)] = true;
                        matched[demandIMap.get(j)] = true;
                        sItem = supplyItems.get(i);
                        dItem = demandItems.get(j);
                        totalReward += model.getRewardFunction().evaluate(sItem, dItem);
                    }
                }
            }
        }
        return totalReward;
    }

    public double[] getSupplyValues(CplexFactory factory)
            throws IloException {
        if (!foundValues) {
            computeValues(factory);
        }
        return supplyValues;
    }

    public double[] getDemandValues(CplexFactory factory)
            throws IloException {
        if (!foundValues) {
            computeValues(factory);
        }
        return demandValues;
    }

    private void computeValues(CplexFactory factory)
            throws IloException {
        foundValues = true;
        getSupplyItems();
        getDemandItems();
        int sSize = supplyItems.size();
        int dSize = demandItems.size();
        double[][] weights = new double[supplyItems.size()][demandItems.size()];
        for (int i = 0; i < supplyItems.size(); i++) {
            Arrays.fill(weights[i], 0.0);
        }

        for (int i = 0; i < sSize; i++) {
            for (int j = 0; j < dSize; j++) {
                Item itemS = supplyItems.get(i);
                Item itemD = demandItems.get(j);
                int sInd = supplyItemsI.get(i);
                int dInd = demandItemsI.get(j);
                int aS = arrivalTimes.get(sInd);
                int dS = departureTimes.get(sInd);
                int aD = arrivalTimes.get(dInd);
                int dD = departureTimes.get(dInd);
                if ((aS <= dD) && (aD <= dS)) {
                    weights[i][j] = model.getRewardFunction().evaluate(itemS, itemD);
                }
            }
        }

        IloCplex cplex = factory.getCplex();
        IloNumVar[] lambdaS, lambdaD;
        double[] lbS = new double[sSize];
        double[] ubS = new double[sSize];
        Arrays.fill(lbS, 0.0);
        Arrays.fill(ubS, Double.MAX_VALUE);
        double[] lbD = new double[dSize];
        double[] ubD = new double[dSize];
        Arrays.fill(lbD, 0.0);
        Arrays.fill(ubD, Double.MAX_VALUE);
        lambdaS = cplex.numVarArray(sSize, lbS, ubS);
        lambdaD = cplex.numVarArray(dSize, lbD, ubD);

        for (int i = 0; i < sSize; i++) {
            for (int j = 0; j < dSize; j++) {
                cplex.addGe(cplex.sum(
                        cplex.prod(1.0, lambdaS[i]),
                        cplex.prod(1.0, lambdaD[j]))
                        , weights[i][j]);
            }
        }

        double[] onesS = new double[sSize];
        Arrays.fill(onesS, 1.0);


        double[] onesD = new double[dSize];
        Arrays.fill(onesD, 1.0);

        IloNumExpr obj = cplex.sum(
                cplex.scalProd(lambdaS, onesS),
                cplex.scalProd(lambdaD, onesD)
        );
        cplex.addMinimize(obj);
        cplex.solve();
        supplyValues = cplex.getValues(lambdaS);
        demandValues = cplex.getValues(lambdaD);
    }
}
