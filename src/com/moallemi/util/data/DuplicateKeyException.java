package com.moallemi.util.data;

/**
 * An exception generated when trying load a key with a duplicated name.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.1 $, $Date: 2005-04-10 23:29:45 $
 */
public class DuplicateKeyException extends RuntimeException {
    public DuplicateKeyException() { super(); }
    public DuplicateKeyException(Throwable t) { super(t); }
    public DuplicateKeyException(String m, Throwable t) { super(m, t); }
    public DuplicateKeyException(String m) { super(m); }
}
