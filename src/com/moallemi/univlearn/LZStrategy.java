package com.moallemi.univlearn;

import java.util.*;
import java.io.*;
import java.text.DecimalFormat;

public class LZStrategy implements Strategy {
    private MatrixGame game;
    private boolean useBoth;
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


    private abstract class ContextNode {
        public float value;
        public int count;

        public ContextNode() {
            value = 0.0f; 
            count = 0;
        }

        public abstract ContextNode getChild(int action, int otherAction);
        public abstract ContextNode newChild(int action, int otherAction);
        public abstract int getChildCount();
        public abstract ContextNode getChild(int index);

        public void addVisit() { count++; }
        public int getVisitCount() { return count; }

        public double computeQValue(double discountFactor, int action) {
            double expectedValue = 0.0;
            double sumWeight = 0.0;
            int n = game.getNumPlays();
            for (int otherAction = 0; otherAction < n; otherAction++)  {
                double cost 
                    = game.getCost(action, otherAction);
                ContextNode nextNode = getChild(action, otherAction);

                double nextValue, weight;
                if (nextNode == null) {
                    nextValue = 0.0;
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
        
        public void updateValues(double discountFactor) {
            double minQValue = Double.MAX_VALUE;
            int n = game.getNumPlays(); 
            for (int action = 0; action < n; action++)  {
                double qValue = computeQValue(discountFactor, action);
                if (qValue < minQValue)
                    minQValue = qValue;
            }
            value = (float) minQValue;
        }


        public int getBestPlay(double discountFactor, Random random) {
            int minActionCount = 0;
            int n = game.getNumPlays();
            for (int action = 0; action < n; action++)  {
                tmpQValue[action] = (float) computeQValue(discountFactor,
                                                          action);
                if (tmpQValue[action] - value < Q_TOLERANCE) 
                    minActionCount++;
            }

            int selected = minActionCount > 1
                ? random.nextInt(minActionCount)
                : 0;
            for (int action = 0; action < n; action++)  {
                if (tmpQValue[action] - value < Q_TOLERANCE) {
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

        public void verify(double discountFactor) {
            double minQValue = Double.MAX_VALUE;
            int n = game.getNumPlays();
            for (int action = 0; action < n; action++)  {
                double newQ = computeQValue(discountFactor, action);
                if (newQ < minQValue)
                    minQValue = newQ;
            }
            if (Math.abs(((float) minQValue) - value) > EPSILON)
                throw new IllegalStateException("bad value");
                
            int childCount = getChildCount();
            int totalCount = 0;
            for (int i = 0; i < childCount; i++) {
                ContextNode child = getChild(i);
                if (child != null) {
                    totalCount += child.count;
                    child.verify(discountFactor);
                }
            }

            if (count != totalCount + 1)
                throw new IllegalStateException("bad child count");
        }
            
    }

    private class OtherContextNode extends ContextNode {
        private ContextNode[] nextNodes = 
            new ContextNode [game.getNumPlays()];

        public ContextNode getChild(int action, int otherAction) {
            return nextNodes[otherAction];
        }

        public ContextNode newChild(int action, int otherAction) {
            if (nextNodes[otherAction] != null)
                throw new IllegalStateException("unreachable code");

            nextNodes[otherAction] = new OtherContextNode();
            return nextNodes[otherAction];
        }

        public int getChildCount() { return nextNodes.length; }
        public ContextNode getChild(int i) { return nextNodes[i]; }
    }

    private class BothContextNode extends ContextNode {
        private ContextNode[][] nextNodes = 
            new ContextNode [game.getNumPlays()][game.getNumPlays()];

        public ContextNode getChild(int action, int otherAction) {
            return nextNodes[action][otherAction];
        }

        public ContextNode newChild(int action, int otherAction) {
            if (nextNodes[action][otherAction] != null)
                throw new IllegalStateException("unreachable code");

            nextNodes[action][otherAction] = new BothContextNode();
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

    public static final int NO_EXPLORE = 0;
    public static final int LOG_EXPLORE = 1;
    public static final int LINEAR_EXPLORE = 2;


    private ContextNode root;
    private int explorePolicy;
    private int lastPlay;
    private Stack stack = new Stack();
    private int time;
    private int contextCount;
    private int maxContextDepth;

    public LZStrategy(MatrixGame game,
                      boolean useBoth,
                      double discountFactor,
                      int explorePolicy,
                      double gamma)
    {
        this.game = game;
        this.tmpQValue = new double [game.getNumPlays()];
        this.useBoth = useBoth;
        this.explorePolicy = explorePolicy;
        this.discountFactor = discountFactor;
        this.gamma = gamma;
    }

    public void reset(Random random) {
        this.random = random;
        root = useBoth 
            ? (ContextNode) new BothContextNode() 
            : (ContextNode) new OtherContextNode();
        root.addVisit();
        time = 1;
        contextCount = 1;
        maxContextDepth = 0;
        stack.clear();
        stack.push(root);
        lastPlay = -1;
    } 

    public int nextPlay() {
        ContextNode current = (ContextNode) stack.peek();
        double exploreProb;
        
        switch (explorePolicy) {
        case NO_EXPLORE:
            exploreProb = 0.0;
            break;
        case LOG_EXPLORE:
            exploreProb = gamma 
                * Math.pow(Math.log(2.0) / Math.log(1 + current.count),
                           1.0/logOrder);
            break;
        case LINEAR_EXPLORE:
            exploreProb = gamma / ((double) current.count);
            break;
        default:
            throw new RuntimeException("bad exploration policy");
        }

        if (random.nextDouble() < exploreProb) {
            // exploration
            lastPlay = (int) random.nextInt(game.getNumPlays());
        }
        else {
            // explotation
            lastPlay = current.getBestPlay(discountFactor, random);
        }

        return lastPlay;
    }

    public void setNextOpponentPlay(int play) {
        time++;

        ContextNode current = (ContextNode) stack.peek();
        current = current.visitChild(lastPlay, play);

        stack.push(current);
        if (current.getVisitCount() == 0) {
            contextCount++;
            maxContextDepth = Math.max(maxContextDepth, stack.size() - 1);

            // backtrack and update
            do {
                current = (ContextNode) stack.pop();
                current.addVisit();
                current.updateValues(discountFactor);
           } while (!stack.isEmpty());
            stack.push(root);
        }

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


    public void verify() {
        root.verify(discountFactor);
    }

    public void dumpInfo(PrintStream out, int maxDepth) {
        DecimalFormat df = new DecimalFormat("0.000");

        // general stats
        out.println("TIME: " + time
                    + " CONTEXTS: " + contextCount
                    + " AVG LENGTH: " + df.format(((double) time)
                                                  /((double) contextCount))
                    + " MAX LENGTH: " + maxContextDepth
                    );

        // most common path
        
        int n = game.getNumPlays();
        ContextWrapper current = new ContextWrapper(0, "", root);
        while (current != null) {
            out.print("MC STATE: " + current.symbol 
                      + " DEPTH: " + current.depth
                      + " VISITS: " + current.context.count 
                      + " VALUE: "   + df.format(current.context.value));
            out.print(" QV:");
            for (int a = 0; a < n; a++) 
                out.print(" " + game.getSymbol(a)
                          + " " 
                          + df.format(current
                                      .context.computeQValue(discountFactor,
                                                             a)));
            out.println();

            if (!useBoth) {
                OtherContextNode context = 
                    (OtherContextNode) current.context;
                ContextWrapper nextWrapper = null;
                int maxCount = -1;
                for (int a = 0; a < n; a++) {
                    OtherContextNode next = 
                        (OtherContextNode) context.nextNodes[a];
                    if (next == null)
                        continue;
                    if (next.count > maxCount) {
                        maxCount = next.count;
                        String nextSymbol = current.symbol 
                            + game.getSymbol(a);
                        nextWrapper = new ContextWrapper(current.depth + 1,
                                                         nextSymbol,
                                                         next);
                    }
                }
                current = nextWrapper;
            }
            else {
                BothContextNode context = 
                    (BothContextNode) current.context;
                ContextWrapper nextWrapper = null;
                int maxCount = -1;
                for (int a = 0; a < n; a++) {
                    for (int b = 0; b < n; b++) {
                        BothContextNode next = 
                            (BothContextNode) context.nextNodes[a][b];
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
        }

                
        // bread-first-search dump
        LinkedList stack = new LinkedList();
        stack.add(new ContextWrapper(0, "", root));
        while (!stack.isEmpty()) {
            current = (ContextWrapper) stack.removeFirst();
            out.print("STATE: " + current.symbol 
                        + " DEPTH: " + current.depth
                        + " VISITS: " + current.context.count 
                        + " VALUE: "   + df.format(current.context.value));
            out.print(" QV:");
            for (int a = 0; a < n; a++) 
                out.print(" " + game.getSymbol(a)
                          + " " 
                          + df.format(current
                                      .context.computeQValue(discountFactor,
                                                             a)));
            out.println();

            if (current.depth < maxDepth) {
                if (!useBoth) {
                    OtherContextNode context = 
                        (OtherContextNode) current.context;
                    for (int a = 0; a < n; a++) {
                        OtherContextNode next = 
                            (OtherContextNode) context.nextNodes[a];
                        if (next == null)
                            continue;
                        String nextSymbol = current.symbol 
                            + game.getSymbol(a);
                        stack.addLast(new ContextWrapper(current.depth + 1,
                                                         nextSymbol,
                                                         next));
                    }
                }
                else {
                    BothContextNode context = 
                        (BothContextNode) current.context;
                    for (int a = 0; a < n; a++) {
                        for (int b = 0; b < n; b++) {
                            BothContextNode next = 
                                (BothContextNode) context.nextNodes[a][b];
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
 
}