package com.nikhilpb.adp;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/12/13
 * Time: 11:08 AM
 * To change this template use File | Settings | File Templates.
 */
public interface State {
  public ArrayList<Action> getActions();
}
