package com.moallemi.util.data;

/**
 *
 * A very simple class that wraps an int (like the Integer class), but
 * retains the mutable aspect of the builtin int datatype.
 *
 * This is usefull for putting int values inside HashMap and
 * ArrayList, but yet allows you to change the value without the
 * overhead of creating and destroying objects.
 *
 * WARNING: This class does not implements all methods of the
 * java.lang.Integer class
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.1 $, $Date: 2005-01-20 04:58:54 $
 */
public class MutableInt extends Number implements Comparable {
    public int value;

    public static int MAX_VALUE = Integer.MAX_VALUE;
    public static int MIN_VALUE = Integer.MIN_VALUE;
    public static Class TYPE = Integer.TYPE;

    public MutableInt(int i) { value = i; }
    public MutableInt(Integer i) { value = i.intValue(); }
    public MutableInt(String s) { 
	value = Integer.parseInt(s);
    }

    public int compareTo(MutableInt i) {
	if (this.value < i.value) 
	    return -1;
	else if (this.value == i.value)
	    return 0;
	else if (this.value > i.value)
	    return 1;
	throw new IllegalStateException("unreachable code");
    }
    public int compareTo(Object o) {
	if (o instanceof MutableInt)
	    return(this.compareTo((MutableInt) o));
	else
	    throw new ClassCastException("not a MutableInt");
    }
    public boolean equals(Object o) {
	if (o instanceof MutableInt)
	    return this.value == ((MutableInt) o).value;
	else
	    throw new ClassCastException("not a MutableInt");
    }	    

    public int intValue() { return this.value; }

    // Simple casts
    public byte byteValue() { return (byte) this.value; }
    public double doubleValue() { return (double) this.value; }
    public float floatValue() { return (float) this.value; }
    public long longValue() { return (long) this.value; }
    public short shortValue() { return (short) this.value; }

    public String toString() { return Integer.toString(this.value); }

    // WARNING:  Do we want MutableInt to hash as its int value,
    //           or use the hashCode of the underlying Object?
//      public int hashCode() { return this.value; }

    // Static conversion methods.  Easy to implement by calling static Integer
    // methods, but DANGEROUS by adding another level of indirection and typo
//      public static MutableInt decode(String nm) { return new MutableInt(Integer.decode(nm)); }
//      public static String toString(int i) { return new String(i); }
//      public static int parseInt(String s) { return Integer.parseInt(s); }
//      public static int parseInt(String s, int radix) { return Integer.parseInt(s, radix); }
//      public static String toBinaryString(int i) { return Integer.toBinaryString(i); }
//      public static String toHexString(int i) { return Integer.toHexString(i); }
//      public static String toOctalString(int i) { return Integer.toOctalString(i); }


}

