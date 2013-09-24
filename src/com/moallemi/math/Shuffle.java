package com.moallemi.math;

import java.util.Random;

/**
 * A class for shuffling arrays.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.1 $, $Date: 2005-06-21 21:12:51 $
 */
public class Shuffle {

    /**
     * Shuffle an array in place.
     *
     * @param random a source of randomness
     * @param array the array
     */
    public static void shuffle(Random random, int[] array) {
        for (int i = 1; i < array.length; i++) {
            int position = random.nextInt(i+1);
            int tmp = array[position];
            array[position] = array[i];
            array[i] = tmp;
        }
    }

    /**
     * Shuffle an array in place.
     *
     * @param random a source of randomness
     * @param array the array
     */
    public static void shuffle(Random random, boolean[] array) {
        for (int i = 1; i < array.length; i++) {
            int position = random.nextInt(i+1);
            boolean tmp = array[position];
            array[position] = array[i];
            array[i] = tmp;
        }
    }

    /**
     * Shuffle an array in place.
     *
     * @param random a source of randomness
     * @param array the array
     */
    public static void shuffle(Random random, byte[] array) {
        for (int i = 1; i < array.length; i++) {
            int position = random.nextInt(i+1);
            byte tmp = array[position];
            array[position] = array[i];
            array[i] = tmp;
        }
    }

    /**
     * Shuffle an array in place.
     *
     * @param random a source of randomness
     * @param array the array
     */
    public static void shuffle(Random random, char[] array) {
        for (int i = 1; i < array.length; i++) {
            int position = random.nextInt(i+1);
            char tmp = array[position];
            array[position] = array[i];
            array[i] = tmp;
        }
    }

    /**
     * Shuffle an array in place.
     *
     * @param random a source of randomness
     * @param array the array
     */
    public static void shuffle(Random random, double[] array) {
        for (int i = 1; i < array.length; i++) {
            int position = random.nextInt(i+1);
            double tmp = array[position];
            array[position] = array[i];
            array[i] = tmp;
        }
    }

    /**
     * Shuffle an array in place.
     *
     * @param random a source of randomness
     * @param array the array
     */
    public static void shuffle(Random random, float[] array) {
        for (int i = 1; i < array.length; i++) {
            int position = random.nextInt(i+1);
            float tmp = array[position];
            array[position] = array[i];
            array[i] = tmp;
        }
    }

    /**
     * Shuffle an array in place.
     *
     * @param random a source of randomness
     * @param array the array
     */
    public static void shuffle(Random random, long[] array) {
        for (int i = 1; i < array.length; i++) {
            int position = random.nextInt(i+1);
            long tmp = array[position];
            array[position] = array[i];
            array[i] = tmp;
        }
    }

    /**
     * Shuffle an array in place.
     *
     * @param random a source of randomness
     * @param array the array
     */
    public static void shuffle(Random random, Object[] array) {
        for (int i = 1; i < array.length; i++) {
            int position = random.nextInt(i+1);
            Object tmp = array[position];
            array[position] = array[i];
            array[i] = tmp;
        }
    }
}