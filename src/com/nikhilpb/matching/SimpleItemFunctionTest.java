package com.nikhilpb.matching;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 9/20/13
 * Time: 9:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleItemFunctionTest extends TestCase {
  @Test
  public void testEvaluate() throws Exception {
    double[] values = {1.0, 2.0};
    ArrayList<Item> items = TestFactory.fakeItems();
    Item item3 = items.remove(2);
    SimpleItemFunction sif = new SimpleItemFunction(items, values);
    assertEquals(sif.evaluate(items.get(0)), 1.0);
    assertEquals(sif.evaluate(items.get(1)), 2.0);
    assertEquals(sif.evaluate(item3), 0.0);
  }
}
