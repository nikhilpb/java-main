package com.nikhilpb.matching;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 9/20/13
 * Time: 10:10 AM
 * To change this template use File | Settings | File Templates.
 */

public class FirstOrderItemFunctionTest extends TestCase {
  @Test
  public void testEvaluate() throws Exception {
    ArrayList<Item> items = TestFactory.fakeItems();
    FirstOrderItemFunction foif = new FirstOrderItemFunction(0, TestFactory.kArr1[0]);
    assertEquals(foif.evaluate(items.get(0)), 1.0);
    foif = new FirstOrderItemFunction(0, TestFactory.kArr1[0] + 1);
    assertEquals(foif.evaluate(items.get(0)), 0.0);
  }
}
