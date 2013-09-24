package com.moallemi.univlearn;

import java.io.*;
import java.util.*;

import com.moallemi.math.stats.MVSampleStatistics;
import com.moallemi.util.TicTocTimer;

public class UnivLearnMain {
    private String[] argv;
    private int index;
    private MatrixGame game;
    private Strategy playerA;
    private Strategy playerB;
    private long seed = -1L;

    public UnivLearnMain(String[] argv) {
        this.argv = argv;
        index = 0;
    }

    public void processNext() throws Exception {
        int lastIndex = index;
        if (argv[index].equals("game")) {
            String type = argv[++index];

            System.out.println("loading game type: " + type);

            if (type.equals("rps"))
                game = new RockPaperScissors();
            else if (type.equals("pd"))
                game = new PrisonersDilemma();
            else
                throw new IllegalArgumentException("unknown type: "
                                                   + type);
        }
        else if (argv[index].equals("player")) {
            String playerIndex = argv[++index];
            String type = argv[++index];

            System.out.println("loading player " + playerIndex 
                               + " type: " + type);
            
            Strategy strategy;
            if (type.equals("constant"))
                strategy = new ConstantStrategy(0);
            else if (type.equals("titfortat"))
                strategy = new TitForTatStrategy();
            else if (type.equals("tsachy"))
                strategy = new TsachyStrategy();
            else if (type.equals("tsachyopt"))
                strategy = new TsachyOptStrategy();
            else if (type.equals("pure")) {
                boolean useBoth = 
                    Boolean.valueOf(argv[++index]).booleanValue();
                int memory = Integer.parseInt(argv[++index]);
                Random random = new Random(seed);

                strategy = new DeterministicContextStrategy(game,
                                                            useBoth,
                                                            memory,
                                                            random);
            }
            else if (type.equals("mixed")) {
                boolean useBoth = 
                    Boolean.valueOf(argv[++index]).booleanValue();
                int memory = Integer.parseInt(argv[++index]);
                double fracRandom = Double.parseDouble(argv[++index]);
                Random random = new Random(seed);

                strategy = new RandomizedContextStrategy(game,
                                                         useBoth,
                                                         memory,
                                                         fracRandom,
                                                         random);
            }
            else if (type.equals("lz")) {
                boolean useBoth = 
                    Boolean.valueOf(argv[++index]).booleanValue();
                double discountFactor = Double.parseDouble(argv[++index]);
                String explorationStyle = argv[++index];
                int explorePolicy;
                if (explorationStyle.equals("none"))
                    explorePolicy = LZStrategy.NO_EXPLORE;
                else if (explorationStyle.equals("log"))
                    explorePolicy = LZStrategy.LOG_EXPLORE;
                else if (explorationStyle.equals("linear"))
                    explorePolicy = LZStrategy.LINEAR_EXPLORE;
                else
                    throw new IllegalArgumentException("bad exploration "
                                                       + "policy: "
                                                       + explorationStyle);

                double gamma = Double.parseDouble(argv[++index]);
                strategy = new LZStrategy(game,
                                          useBoth,
                                          discountFactor,
                                          explorePolicy,
                                          gamma);
            }
            else if (type.equals("lza")) {
                double discountFactor = Double.parseDouble(argv[++index]);
                String explorationStyle = argv[++index];
                int explorePolicy;
                if (explorationStyle.equals("none"))
                    explorePolicy = LZStrategy.NO_EXPLORE;
                else if (explorationStyle.equals("log"))
                    explorePolicy = LZStrategy.LOG_EXPLORE;
                else if (explorationStyle.equals("linear"))
                    explorePolicy = LZStrategy.LINEAR_EXPLORE;
                else
                    throw new IllegalArgumentException("bad exploration "
                                                       + "policy: "
                                                       + explorationStyle);

                double gamma = Double.parseDouble(argv[++index]);
                strategy = new LZAsyncStrategy(game,
                                               discountFactor,
                                               explorePolicy,
                                               gamma);
            }
            else
                throw new IllegalArgumentException("unknown type: "
                                                   + type);
            
            if (playerIndex.equals("a")) 
                playerA = strategy;
            else if (playerIndex.equals("b")) 
                playerB = strategy;
            else
                throw new IllegalArgumentException("unknown player: "
                                                   + playerIndex);
        }
        else if (argv[index].equals("simvalue")) {
            System.out.println("simulating average cost");

            int time = Integer.parseInt(argv[++index]);
            int paths = Integer.parseInt(argv[++index]);
            MVSampleStatistics statsA = new MVSampleStatistics();
            MVSampleStatistics statsB = new MVSampleStatistics();
            MVSampleStatistics pathStatsA = new MVSampleStatistics();
            MVSampleStatistics pathStatsB = new MVSampleStatistics();

            Random baseRandom = new Random(seed);

            for (int path = 0; path < paths; path++) {
                playerA.reset(new Random(baseRandom.nextLong()));
                playerB.reset(new Random(baseRandom.nextLong()));
                pathStatsA.clear();
                pathStatsB.clear();

                for (int t = 0; t < time; t++) {
                    int aPlay = playerA.nextPlay();
                    int bPlay = playerB.nextPlay();
                    double aCost = game.getCost(aPlay, bPlay);
                    double bCost = game.getCost(bPlay, aPlay);

                    pathStatsA.addSample(aCost);
                    pathStatsB.addSample(bCost);

                    playerA.setNextOpponentPlay(bPlay);
                    playerB.setNextOpponentPlay(aPlay);
                }
                double valueA = pathStatsA.getMean();
                double valueB = pathStatsB.getMean();

                statsA.addSample(valueA);
                statsB.addSample(valueB);

                System.out.println("PATH: " + path
                                   + " VALUE: " + valueA
                                   + " RUNNING AVG: " + statsA.getMean());
            }
            System.out.print("MEAN: " + statsA.getMean());
            if (statsA.getCount() > 1) {
                double stddev = statsA.getStandardDeviation();
                System.out.print(" STDDEV: " + stddev
                                 + " ERROR: " 
                                 + stddev/Math.sqrt(statsA.getCount()));
            }
            System.out.println();

        }
        else if (argv[index].equals("seed")) {
            seed = Long.parseLong(argv[++index]);
        }
        else if (argv[index].equals("dumplz")) {
            String playerIndex = argv[++index];
            int maxDepth = Integer.parseInt(argv[++index]);
            Strategy player;

            if (playerIndex.equals("a")) 
                player = playerA;
            else if (playerIndex.equals("b")) 
                player = playerB;
            else
                throw new IllegalArgumentException("unknown player: "
                                                   + playerIndex);

            if (player instanceof LZStrategy) {
                LZStrategy lzPlayer = (LZStrategy) player;
                lzPlayer.dumpInfo(System.out, maxDepth);
            }
            else if (player instanceof LZAsyncStrategy) {
                LZAsyncStrategy lzPlayer = (LZAsyncStrategy) player;
                lzPlayer.dumpInfo(System.out, maxDepth);
            }
            else
                throw new IllegalArgumentException("cannot dumplz on "
                                                   + "non-lz player");
        }
        else if (argv[index].equals("verifylz")) {
            String playerIndex = argv[++index];
            Strategy player;

            if (playerIndex.equals("a")) 
                player = playerA;
            else if (playerIndex.equals("b")) 
                player = playerB;
            else
                throw new IllegalArgumentException("unknown player: "
                                                   + playerIndex);

            if (player instanceof LZStrategy) {
                LZStrategy lzPlayer = (LZStrategy) player;
                lzPlayer.verify();
            }
            else
                throw new IllegalArgumentException("cannot verifylz on "
                                                   + "non-lz player");
        }
        else 
            throw new IllegalArgumentException("unknown command: " 
                                               + argv[index]);
       

        ++index;
        System.out.print(">>>");
        for (int i = lastIndex; i < index; i++)
            System.out.print(" " + argv[i]);
        System.out.println();
    }

    public boolean hasNext() {
        return index < argv.length;
    }

    public static void main(String[] argv) throws Exception {
        UnivLearnMain m = new UnivLearnMain(argv);

        TicTocTimer t = new TicTocTimer();
        while (m.hasNext()) {
            t.tic();
            m.processNext();
            t.toc();
            t.tocPrint();
        }
        
        System.exit(0);
    }
}