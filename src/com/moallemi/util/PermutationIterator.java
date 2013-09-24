package com.moallemi.util;

/**
 * An iterator over permutations.
 * 
 * @author Ciamac Moallemi
 * @version $Revision: 1.2 $, $Date: 2006-06-14 21:39:00 $
 */
public class PermutationIterator implements java.util.Iterator<int[]>
{
    private int n;
    private int m;
    private int[] index;
    private int[] out;
    private boolean hasMore = true;

    /**
     * Create a Permutation to enumerate through all possible lineups
     * permutations of m things out of a collection of n things.
     *
     * @param n the number of objects total
     * @param m the number of objects in the permutation
     * @throws IllegalArgumentException if m is bigger than n
     */
    public PermutationIterator(int n, int m) throws IllegalArgumentException
    {
        this.n = n;
        this.m = m;

        if (m > n)
            throw new IllegalArgumentException();

        /*
         * index is an array of ints that keep track of the next 
         * permutation to return. For example, an index on a permutation 
         * of 3 things might contain {1 2 0}. This index will be followed 
         * by {2 0 1} and {2 1 0}.
         * Initially, the index is {0 ... n - 1}.
         */
        index = new int [n];
        out = new int [m];
        for (int i = 0; i < n; i++) {
            index[i] = i;
        }

        /*
         * The elements from m to n are always kept ascending right
         * to left. This keeps the dip in the interesting region.     
         */    
        reverseAfter(m - 1);
    }

    /**
     * Get the total number of permutations.
     *
     * @return the total number of permuations
     */
    public int getSize() {
        int size = 1;
        int low = n - m + 1;
        for (int i = n; i >= low; i--) 
            size *= i;
        return size;
    }
            

    public boolean hasNext() {
        return hasMore;
    }


    /*
     * Move the index forward a notch. The algorithm first finds the 
     * rightmost index that is less than its neighbor to the right. This 
     * is the dip point. The algorithm next finds the least element to
     * the right of the dip that is greater than the dip. That element is
     * switched with the dip. Finally, the list of elements to the right 
     * of the dip is reversed.
     * <p>
     * For example, in a permutation of 5 items, the index may be 
     * {1, 2, 4, 3, 0}. The dip is 2  the rightmost element less 
     * than its neighbor on its right. The least element to the right of 
     * 2 that is greater than 2 is 3. These elements are swapped, 
     * yielding {1, 3, 4, 2, 0}, and the list right of the dip point is 
     * reversed, yielding {1, 3, 0, 2, 4}.
     * <p>
     * The algorithm is from Applied Combinatorics, by Alan Tucker.
     *
     */
    private void moveIndex() {
        // find the index of the first element that dips
        int i = rightmostDip();
        if (i < 0) {
            hasMore = false;
            return;
        }

        // find the least greater element to the right of the dip
        int leastToRightIndex = i + 1;
        for (int j = i + 2; j < n; j++) {
            if (index[j] < index[leastToRightIndex] &&  index[j] > index[i])
                leastToRightIndex = j;
        }
        
        // switch dip element with least greater element to its right
        int t = index[i];
        index[i] = index[leastToRightIndex];
        index[leastToRightIndex] = t;

        if (m - 1 > i) {
            // reverse the elements to the right of the dip
            reverseAfter(i);    
            // reverse the elements to the right of m - 1
            reverseAfter(m - 1);
        }
    }

    /**
     * Return the next permutation, as an <code>int[]</code>. Note
     * that the return value is always the same array and should not
     * be modified!
     *
     * @return the next permutation
     */
    public int[] next() {
        if (!hasMore) 
            return null;

        for (int i = 0; i < m; i++) 
            out[i] = index[i];

        moveIndex();
        return out;
    }

    /*
     * Reverse the index elements to the right of the specified index.
     */
    private void reverseAfter(int i)
    {
        int start = i + 1;
        int end = n - 1;
        while (start < end)
        {
            int t = index[start];
            index[start] = index[end];
            index[end] = t;
            start++;
            end--;
        }

    }

    /*
     * @return int the index of the first element from the right
     * that is less than its neighbor on the right.
     */
    private int rightmostDip()
    {
        for (int i = n - 2; i >= 0; i--)
        {
            if (index[i] < index[i+1])
                return i;
        }
        return -1;
    }

    public void remove() { throw new UnsupportedOperationException(); }
}
