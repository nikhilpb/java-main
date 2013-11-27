package com.moallemi.bloomhash;

import java.io.*;
import java.util.*;
import java.text.DecimalFormat;

import com.moallemi.math.stats.SampleStatistics;
import com.moallemi.util.TicTocTimer;

public class BloomMain {
    private String[] argv;
    private int index;

    private long seed = -1L;

    public BloomMain(String[] argv) {
        this.argv = argv;
        index = 0;
    }

    public boolean hasNext() {
        return index < argv.length;
    }

    public Random getRandom() {
	return new Random(seed >= 0L ? seed : System.currentTimeMillis());
    }

    public void processNext() throws Exception {
        int lastIndex = index;

        if (argv[index].equals("seed")) {
	    seed = Long.parseLong(argv[++index]);
        }
        else if (argv[index].equals("mcdups")) {
            String type = argv[++index];
            int n = Integer.parseInt(argv[++index]);
            int trials = Integer.parseInt(argv[++index]);

            BloomHash hash;
            if (type.equals("random")) 
                hash = new BloomHash(n);
            else if (type.equals("fixed")) 
                hash = new BloomHashFixedOrder(n);
            else
                throw new IllegalArgumentException("unknown type: " + type);

            Random random = getRandom();
            SampleStatistics maxSizeStats = new SampleStatistics();
            SampleStatistics totalDupStats = new SampleStatistics();
            for (int t = 0; t < trials; t++) {
                hash.build(new Random(random.nextLong()));
                
                // count duplicates
                int[] dupCount = new int [hash.getSize()];
                int maxSize = 0;
                for (int i = 0; i < dupCount.length; i++) {
                    int h = hash.getHashCode(i);                    
                    dupCount[h]++;
                    if (dupCount[h] > maxSize)
                        maxSize = dupCount[h];
                }
                int totalDups = 0;
                for (int i = 0; i < dupCount.length; i++) {
                    if (dupCount[i] > 1)
                        totalDups += dupCount[i];
                }
                maxSizeStats.addSample(maxSize);
                totalDupStats.addSample(totalDups);
            }
            
            DecimalFormat df = new DecimalFormat("0.00");
            System.out.println("max size = "
                               + df.format(maxSizeStats.getMean()) 
                               + " (" + df.format(maxSizeStats.getMinimum()) 
                               + ") (" 
                               + df.format(maxSizeStats.getMaximum())
                               + ")");
            System.out.println("total dups = " 
                               + df.format(totalDupStats.getMean()) 
                               + " (" + df.format(totalDupStats.getMinimum()) 
                               + ") (" 
                               + df.format(totalDupStats.getMaximum())
                               + ")");
            
        }
        else
            throw new IllegalArgumentException("unknown command: " 
                                               + argv[index]);

        index++;

        System.out.print(">>");
        for (int i = lastIndex; i < index; i++)
            System.out.print(" " + argv[i]);
        System.out.println();
    }

    public static void main(String[] argv) throws Exception {
        BloomMain m = new BloomMain(argv);
        System.out.print(">>>");
        for (int i = 0; i < argv.length; i++)
            System.out.print(" " + argv[i]);
        System.out.println();

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
    
