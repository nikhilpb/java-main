package com.moallemi.util.data;

/**
 * A non-mutable integer array. To be used as a key.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.3 $, $Date: 2005-04-14 21:32:23 $
 */
public class IntArray {
    private int[] data;
    private int hashCode;

    /**
     * Constructor. If the input array is later modified results are
     * unpredictable.
     *
     * @param data the data
     */
    public IntArray(int[] data) {
        this.data = data;

        // multiplicative hashing
        hashCode = 0;
        for (int i = 0; i < data.length; i++) {
            long y = data[i];
            y *= 2654435761L;
            y >>= 32;
            hashCode = ((hashCode << 1) | (hashCode >> 31)) ^ ((int) y);
        }
    }
    
    public int hashCode() {
        return hashCode;
    }

    public boolean equals(Object other) {
        if (other instanceof IntArray) {
            IntArray o = (IntArray) other;
            if (hashCode != o.hashCode)
                return false;
            if (data.length != o.data.length)
                return false;
            for (int i = 0; i < data.length; i++) {
                if (data[i] != o.data[i])
                    return false;
            }
            return true;
        }
        return false;
    }

    public int distanceLInf(IntArray other) {
        if (data.length != other.data.length) 
            throw new IllegalArgumentException();
        int dist = 0;
        for (int i = 0; i < data.length; i++) {
            int d = Math.abs(data[i] - other.data[i]);
            if (d > dist)
                dist = d;
        }
        return dist;
    }

    public int distanceL1(IntArray other) {
        if (data.length != other.data.length) 
            throw new IllegalArgumentException();
        int dist = 0;
        for (int i = 0; i < data.length; i++) {
            dist += Math.abs(data[i] - other.data[i]);
        }
        return dist;
    }

    public int distanceLInfModular(IntArray other, int modulus) {
        if (data.length != other.data.length) 
            throw new IllegalArgumentException();
        int dist = 0;
        for (int i = 0; i < data.length; i++) {
            int d1 = Math.abs(data[i] - other.data[i]) % modulus;
            int d = Math.min(d1, modulus - d1);
            if (d > dist)
                dist = d;
        }
        return dist;
    }

    public int distanceL1Modular(IntArray other, int modulus) {
        if (data.length != other.data.length) 
            throw new IllegalArgumentException();
        int dist = 0;
        for (int i = 0; i < data.length; i++) {
            int d1 = Math.abs(data[i] - other.data[i]) % modulus;
            dist += Math.min(d1, modulus - d1);
        }
        return dist;
    }
            
    public int size() { return data.length; }
    public int get(int i) { return data[i]; }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            if (i > 0)
                sb.append(",");
            sb.append(data[i]);
        }
        return sb.toString();
    }
    
            
}
               
