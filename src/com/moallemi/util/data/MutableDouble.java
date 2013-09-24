package com.moallemi.util.data;

/**
 *
 * A very simple class that wraps an double (like the Double class),
 * but retains the mutable aspect of the builtin int datatype.
 *
 * This is usefull for putting double values inside HashMap and
 * ArrayList, but yet allows you to change the value without the
 * overhead of creating and destroying objects.
 *
 * WARNING: This class does not implements all methods of the
 * java.lang.Double class
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.1 $, $Date: 2005-01-20 04:58:54 $
 */
public class MutableDouble extends Number implements Comparable {
    public double value;

    public static double MAX_VALUE = Double.MAX_VALUE;
    public static double MIN_VALUE = Double.MIN_VALUE;
    public static Class TYPE = Double.TYPE;

    public MutableDouble(double d) { value = d; }
    public MutableDouble(Double d) { value = d.intValue(); }
    public MutableDouble(String s) { 
	value = Double.parseDouble(s);
    }

    public int compareTo(MutableDouble d) {
	if (this.value < d.value) 
	    return -1;
	else if (this.value == d.value)
	    return 0;
	else if (this.value > d.value)
	    return 1;
	throw new IllegalStateException("unreachable code");
    }
    public int compareTo(Object o) {
	if (o instanceof MutableDouble)
	    return(this.compareTo((MutableDouble) o));
	else
	    throw new ClassCastException("not a MutableDouble");
    }
    public boolean equals(Object o) {
	if (o instanceof MutableDouble)
	    return this.value == ((MutableDouble) o).value;
	else
	    throw new ClassCastException("not a MutableDouble");
    }	    

    public double doubleValue() { return this.value; }

    // Simple casts
    public byte byteValue() { return (byte) this.value; }
    public int intValue() { return (int) this.value; }
    public float floatValue() { return (float) this.value; }
    public long longValue() { return (long) this.value; }
    public short shortValue() { return (short) this.value; }

    public String toString() { return Double.toString(this.value); }

    // WARNING:  Do we want MutableDouble to hash as its int value,
    //           or use the hashCode of the underlying Object?
//      public int hashCode() { return this.value; }

    // Static conversion methods.  Easy to implement by calling static Integer
    // methods, but DANGEROUS by adding another level of indirection and typo
//      public static MutableDouble decode(String nm) { return new MutableDouble(Integer.decode(nm)); }
//      public static String toString(int i) { return new String(i); }
//      public static int parseInt(String s) { return Integer.parseInt(s); }
//      public static int parseInt(String s, int radix) { return Integer.parseInt(s, radix); }
//      public static String toBinaryString(int i) { return Integer.toBinaryString(i); }
//      public static String toHexString(int i) { return Integer.toHexString(i); }
//      public static String toOctalString(int i) { return Integer.toOctalString(i); }


}

