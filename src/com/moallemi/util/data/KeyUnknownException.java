package com.moallemi.util.data;

/**
 * An exception generated when trying load a key with a duplicated name.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.1 $, $Date: 2005-04-10 23:29:45 $
 */
public class KeyUnknownException extends RuntimeException {
    public KeyUnknownException() { super(); }
    public KeyUnknownException(Throwable t) { super(t); }
    public KeyUnknownException(String m, Throwable t) { super(m, t); }
    public KeyUnknownException(String m) { super(m); }
}
