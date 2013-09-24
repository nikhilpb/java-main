package com.moallemi.util;

/**
 * An iterator over tuples.
 * 
 * @author Ciamac Moallemi
 * @version $Revision: 1.2 $, $Date: 2006-06-16 20:02:38 $
 */
public class TupleIterator implements java.util.Iterator<int[]>
{
    private int dim;
    private int n;
    private int[] cur;
    private int[] out;
    private boolean hasMore = true;

    /**
     * Constructor. Enumerates all tuples of a given dimension where
     * each component takes a value in {0, ..., n - 1}.
     *
     * @param dim the dimension
     * @param n the number of values per dimension
     * @throws IllegalArgumentException if dim < 1 or n < 1
     */
    public TupleIterator(int dim, int n) 
    {
        this.dim = dim;
        this.n = n;

        if (dim < 1 || n < 1)
            throw new IllegalArgumentException();

        cur = new int [dim];
        out = new int [dim];
    }

    /**
     * Get the total number of tuples.
     *
     * @return the total number of tuples
     */
    public int getSize() {
        int size = 1;
        for (int i = 1; i <= dim; i++) 
            size *= n;
        return size;
    }
            

    public boolean hasNext() {
        return hasMore;
    }

    /**
     * Return the next tuple, as an <code>int[]</code>. Note
     * that the return value is always the same array and should not
     * be modified!
     *
     * @return the next tuple
     */
    public int[] next() {
        if (!hasMore) 
            return null;

        System.arraycopy(cur, 0, out, 0, dim);

        hasMore = false;
        for (int i = 0; i < dim && !hasMore; i++) {
            cur[i]++;
            if (cur[i] >= n) 
                cur[i] = 0;
            else
                hasMore = true;
        }

        return out;
    }

    public void remove() { throw new UnsupportedOperationException(); }
}
