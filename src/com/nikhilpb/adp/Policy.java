package com.nikhilpb.adp;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/13/13
 * Time: 7:40 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Policy {
  public Action getAction(State state);
}
