package com.moallemi.univlearn;

import java.util.*;
import java.io.*;
import java.text.DecimalFormat;

public class LZAsyncStrategy implements Strategy {
    private MatrixGame game;
    private double discountFactor;
    private Random random;
    private double gamma;
    private double logOrder = 1.0;

    private static final double DIST_BIAS = 0.5;
    private static final double EPSILON = 1e-6;
    private static final double Q_TOLERANCE = 0.01;

    // temporary storage
    // not thread-safe!!!!
    private double[] tmpQValue;


    private class ContextNode {
        public float value;
        public int count;
        public ContextNode[][] nextNodes;

        public ContextNode() {
            value = 0.0f; 
            count = 0;
            int n = game.getNumPlays();
            nextNodes = new ContextNode [n][n];
            updateValues();
        }

        public void addVisit() { count++; }
        public int getVisitCount() { return count; }
        public int getTotalCount() {
            int totalCount = 1;
            int n = game.getNumPlays();
            for (int a = 0; a < n; a++) {
                for (int b = 0; b < n; b++) {
                    if (nextNodes[a][b] != null)
                        totalCount += nextNodes[a][b].count;
                }
            }
            return totalCount;
        }

        public double computeQValue(int action) {
            double expectedValue = 0.0;
            double sumWeight = 0.0;
            int n = game.getNumPlays();
            for (int otherAction = 0; otherAction < n; otherAction++)  {
                double cost 
                    = game.getCost(action, otherAction);
                ContextNode nextNode = getChild(action, otherAction);

                double nextValue, weight;
                if (nextNode == null) {
                    nextValue = root != null ? root.value : 0.0f;
                    weight = DIST_BIAS;
                }
                else {
                    nextValue = nextNode.value;
                    weight = nextNode.count + DIST_BIAS;
                }
                
                sumWeight += weight;
                
                if (discountFactor > 0.0) 
                    expectedValue += weight * 
                        (cost + discountFactor * nextValue);
                else 
                    expectedValue += weight * cost;
            }
            expectedValue /= sumWeight;

            return expectedValue;
        }            
        
        public void updateValues() {
            double minQValue = Double.MAX_VALUE;
            int n = game.getNumPlays(); 
            for (int action = 0; action < n; action++)  {
                double qValue = computeQValue(action);
                if (qValue < minQValue)
                    minQValue = qValue;
            }
            value = (float) minQValue;
        }


        public int getBestPlay() {
            int minActionCount = 0;
            int n = game.getNumPlays();
            double minQValue = Double.MAX_VALUE;
            for (int action = 0; action < n; action++)  {
                tmpQValue[action] = computeQValue(action);
                if (tmpQValue[action] < minQValue)
                    minQValue = tmpQValue[action];
            }

            for (int action = 0; action < n; action++)  {
                if (tmpQValue[action] - minQValue < Q_TOLERANCE) 
                    minActionCount++;
            }

            int selected = minActionCount > 1
                ? random.nextInt(minActionCount)
                : 0;
            for (int action = 0; action < n; action++)  {
                if (tmpQValue[action] - minQValue < Q_TOLERANCE) {
                    if (selected-- == 0)
                        return action;
                }
            }
            
            throw new IllegalStateException("inaccessible code");
        }


        public ContextNode visitChild(int ourPlay, int theirPlay) {
            ContextNode nextNode = getChild(ourPlay, theirPlay);
            if (nextNode == null)
                nextNode = newChild(ourPlay, theirPlay);
            return nextNode;
        }

        public ContextNode getChild(int action, int otherAction) {
            return nextNodes[action][otherAction];
        }

        public ContextNode newChild(int action, int otherAction) {
            if (nextNodes[action][otherAction] != null)
                throw new IllegalStateException("unreachable code");

            nextNodes[action][otherAction] = new ContextNode();
            return nextNodes[action][otherAction];
        }

        public int getChildCount() { 
            int n = game.getNumPlays();
            return n * n;
        }
        public ContextNode getChild(int i) { 
            int n = game.getNumPlays();
            return nextNodes[i / n][i % n];
        }
    }

    private static class PlayPair {
        public int ourPlay;
        public int otherPlay;
    }

    public static final int NO_EXPLORE = 0;
    public static final int LOG_EXPLORE = 1;
    public static final int LINEAR_EXPLORE = 2;


    private ContextNode root;
    private int explorePolicy;
    private Stack stack = new Stack();
    private ContextNode current;
    private int time;
    private int contextCount;
    private int maxContextDepth;
    private PlayPair lastPlay;

    public LZAsyncStrategy(MatrixGame game,
                           double discountFactor,
                           int explorePolicy,
                           double gamma)
    {
        this.game = game;
        this.tmpQValue = new double [game.getNumPlays()];
        this.explorePolicy = explorePolicy;
        this.discountFactor = discountFactor;
        this.gamma = gamma;
    }

    public void reset(Random random) {
        this.random = random;
        root = new ContextNode();
        current = root;
        root.addVisit();
        stack.clear();
        time = 1;
        contextCount = 1;
        maxContextDepth = 0;
        lastPlay = null;
    } 

    public int nextPlay() {
        double exploreProb;
        lastPlay = new PlayPair();

        int totalCount = current.getTotalCount();

        switch (explorePolicy) {
        case NO_EXPLORE:
            exploreProb = 0.0;
            break;
        case LOG_EXPLORE:
            exploreProb = gamma 
                * Math.pow(Math.log(2.0) / Math.log(1.0 + totalCount),
                           1.0/logOrder);
            break;
        case LINEAR_EXPLORE:
            exploreProb = gamma / ((double) totalCount);
            break;
        default:
            throw new RuntimeException("bad exploration policy");
        }

        if (random.nextDouble() < exploreProb) {
            // exploration
            lastPlay.ourPlay = (int) random.nextInt(game.getNumPlays());
        }
        else {
            // explotation
            
            // need to decide which context to use
            // so, generate a list of other contexts which match the
            // current history
            int depth = stack.size();
            double minValue = current.value;
            int minIndex = 0;
            ContextNode minNode = current;
            for (int i = 1; i < depth; i++) {
                ContextNode node = root;
                for (int j = i; j < depth; j++) {
                    PlayPair play = (PlayPair) stack.get(j);
                    node = node.getChild(play.ourPlay, play.otherPlay);
                    if (node == null)
                        break;
                }
                if (node != null) {
                    if (node.value < minValue) {
                        minValue = node.value;
                        minIndex = i;
                        minNode = node;
                    }
                }
            }
            if (minIndex > 0) {
                for (int i = minIndex; i < depth; i++)
                    stack.set(i - minIndex, stack.get(i));
                int newSize = depth - minIndex;
                while (stack.size() > newSize)
                    stack.pop();
                current = minNode;
            }

            lastPlay.ourPlay = current.getBestPlay();
        }

        return lastPlay.ourPlay;
    }

    public void setNextOpponentPlay(int play) {
        lastPlay.otherPlay = play;
        time++;
        ContextNode nextNode = current.visitChild(lastPlay.ourPlay, 
                                                  lastPlay.otherPlay);

        nextNode.addVisit();
        current.updateValues();

        if (nextNode.getVisitCount() == 1) {
            contextCount++;
            maxContextDepth = Math.max(maxContextDepth, stack.size());
            current = root;
            stack.clear();
        }
        else {
            stack.push(lastPlay);
            current = nextNode;
        }

        lastPlay = null;
    }
 
    private static class ContextWrapper {
        public int depth;
        public String symbol;
        public ContextNode context;

        public ContextWrapper(int depth, String symbol, ContextNode context) {
            this.depth = depth;
            this.symbol = symbol;
            this.context = context;
        }
    }


    public void dumpInfo(PrintStream out, int maxDepth) {
        DecimalFormat df = new DecimalFormat("0.000");

        // general stats
        out.println("TIME: " + time
                    + " CONTEXTS: " + contextCount
                    + " AVG LENGTH: " + df.format(((double) time)
                                                  /((double) contextCount))
                    + " MAX DEPTH: " + maxContextDepth
                    );

        // most common path
        
        int n = game.getNumPlays();
        ContextWrapper current = new ContextWrapper(0, "", root);
        while (current != null) {
            out.print("MC STATE: " + current.symbol 
                      + " DEPTH: " + current.depth
                      + " VISITS: " + current.context.count 
                      + " TV: " + current.context.getTotalCount()
                      + " VALUE: "   + df.format(current.context.value));
            out.print(" QV:");
            for (int a = 0; a < n; a++) 
                out.print(" " + game.getSymbol(a)
                          + " " 
                          + df.format(current.context.computeQValue(a)));
            out.println();

            ContextNode context = current.context;
            ContextWrapper nextWrapper = null;
            int maxCount = -1;
            for (int a = 0; a < n; a++) {
                for (int b = 0; b < n; b++) {
                    ContextNode next = context.nextNodes[a][b];
                    if (next == null)
                        continue;
                    if (next.count > maxCount) {
                        maxCount = next.count;
                            String nextSymbol = current.symbol 
                                + game.getSymbol(a)
                                + game.getSymbol(b);
                            nextWrapper = new ContextWrapper(current.depth + 1,
                                                             nextSymbol,
                                                             next);
                    }
                }
            }
            current = nextWrapper;
        }
                
        // bread-first-search dump
        LinkedList stack = new LinkedList();
        stack.add(new ContextWrapper(0, "", root));
        while (!stack.isEmpty()) {
            current = (ContextWrapper) stack.removeFirst();
            out.print("STATE: " + current.symbol 
                      + " DEPTH: " + current.depth
                      + " VISITS: " + current.context.count 
                      + " TV: " + current.context.getTotalCount()
                      + " VALUE: "   + df.format(current.context.value));
            out.print(" QV:");
            for (int a = 0; a < n; a++) 
                out.print(" " + game.getSymbol(a)
                          + " " 
                          + df.format(current.context.computeQValue(a)));
            out.println();

            if (current.depth < maxDepth) {
                ContextNode context = current.context;
                for (int a = 0; a < n; a++) {
                    for (int b = 0; b < n; b++) {
                        ContextNode next = context.nextNodes[a][b];
                        if (next == null)
                            continue;
                        String nextSymbol = current.symbol 
                            + game.getSymbol(a)
                            + game.getSymbol(b);
                        stack.addLast(new ContextWrapper(current.depth 
                                                         + 1,
                                                         nextSymbol,
                                                         next));
                    }
                }
            }
        }
    }
 
}