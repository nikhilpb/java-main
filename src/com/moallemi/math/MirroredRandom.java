package com.moallemi.math;

import java.util.Random;

/**
 * Always return the negative of a given random number generator.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.2 $, $Date: 2005-01-28 02:16:16 $
 */
public class MirroredRandom extends Random {
    // underlying randomness
    private Random baseRandom;

    /**
     * Constructor.
     *
     * @param baseRandom the base random number generator to use
     */
    public MirroredRandom(Random baseRandom) { 
	this.baseRandom = baseRandom; 
    }


    protected int next(int bits) {
	throw new RuntimeException("not implemented");
    }

    public boolean nextBoolean() { return !baseRandom.nextBoolean(); }
    public void nextBytes(byte[] bytes) { 
	baseRandom.nextBytes(bytes);
	for (int i = 0; i < bytes.length; i++)
	    bytes[i] ^= 0xff;
    }
    public double nextDouble() { 
	double r = baseRandom.nextDouble();
	return r == 0.0 ? 0.0 : 1.0 - r;
    }
    public float nextFloat() {
	float r = baseRandom.nextFloat();
	return r == 0.0f ? 0.0f : 1.0f - r;
    }
    public double nextGaussian() { return -baseRandom.nextGaussian(); }
    public int nextInt() { return -baseRandom.nextInt(); }
    public int nextInt(int n) {
        return (-baseRandom.nextInt(n)) % n;
    }
    public long nextLong() { return -baseRandom.nextLong(); }
}
