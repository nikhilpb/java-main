package com.moallemi.util.data;

/**
 * A pair. To be used for keys.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.2 $, $Date: 2006-06-15 21:01:42 $
 */
public class Pair<T1,T2> {
    private T1 first;
    private T2 second;

    /**
     * Constructor. 
     *
     * @param first first object
     * @param second second object
     */
    public Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    public int hashCode() {
        // multiplicative hashing
        int hashCode = 0;
        long y;

        y = first != null ? first.hashCode() : 0L;
        y *= 2654435761L;
        y >>= 32;
        hashCode = (int) y;
        y = second != null ? second.hashCode() : 0L;
        y *= 2654435761L;
        y >>= 32;
        hashCode = ((hashCode << 1) | (hashCode >> 31)) ^ ((int) y);
        return hashCode;
    }
    
    public boolean equals(Object other) {
        if (other instanceof Pair) {
            Pair o = (Pair) other;
            if (first == null || o.first == null)
                return first == o.first;
            if (!first.equals(o.first))
                return false;
            if (second == null || o.second == null)
                return second == o.second;
            if (!second.equals(o.second))
                return false;
            return true;
        }
        return false;
    }

  @Override
  public String toString() {
    return "< " + getFirst().toString() + ", " + getSecond().toString() + " >";
  }

  public T1 getFirst() { return first; }
    public T2 getSecond() { return second; }
}
               
