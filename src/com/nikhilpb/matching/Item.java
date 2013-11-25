package com.nikhilpb.matching;

/**
 * A class for the items to be matched.
 * <p/>
 * User: nikhilpb
 * Date: 9/20/13
 * Time: 9:46 AM
 */
public class Item {
    private int dimension;
    private Integer[] type;
    private boolean sodSpecified;
    private int sod;

    /**
     * Constructor.
     *
     * @param type integer array specifying the type
     */
    public Item(Integer[] type) {
        this.type = type;
        this.dimension = type.length;
        sodSpecified = false;
    }

    /**
     * Constructor.
     *
     * @param type integer array specifying the type
     */
    public Item(int[] type) {
        this.type = new Integer[type.length];
        for (int i = 0; i < type.length; i++) {
            this.type[i] = (Integer) type[i];
        }
        this.dimension = type.length;
        sodSpecified = false;
    }

    /**
     * Specifies if the item is on the supply or the demand side.
     *
     * @param sodIn should be 0 if it is on the demand side, else 1
     */
    public void specifySod(int sodIn) {
        if (sodIn > 1 || sodIn < 0) {
            System.err.println("Incorrect supply or demand specification.");
        } else {
            sodSpecified = true;
            this.sod = sodIn;
        }
    }

    /**
     * Returns the which side the item is on.
     *
     * @return is 0 if demand side, 1 if supply side, -1 if unspecified
     */
    public int isSod() {
        if (!sodSpecified) {
            return -1;
        } else {
            return sod;
        }
    }

    /**
     * @return the type at a particular getDimension
     */
    public int getTypeAtDimension(int i) {
        return type[i];
    }

    /**
     * @return the number of dimensions
     */
    public int getDimensions() {
        return dimension;
    }

    /**
     * @return string describing the item
     */
    public String toString() {
        String out = Integer.toString(type[0]);
        for (int i = 1; i < dimension; i++) {
            out = out + " " + Integer.toString(type[i]);
        }
        if (sodSpecified) {
            out += ", ";
            if (sod == 0) {
                out += "demand item";
            } else {
                out += "supply item";
            }
        }
        return out;
    }

    /**
     * @return true if both types are equal
     */
    public static boolean equals(Item type1, Item type2) {
        if (type1.getDimensions() != type2.getDimensions()) {
            return false;
        } else {
            int n = type1.getDimensions();
            for (int i = 0; i < n; i++) {
                if (type1.getTypeAtDimension(i) != type2.getTypeAtDimension(i)) {
                    return false;
                }
            }
        }
        return true;
    }
}
