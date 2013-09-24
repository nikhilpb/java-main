package com.nikhilpb.matching;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 9/20/13
 * Time: 1:08 PM
 * To change this template use File | Settings | File Templates.
 */


@RunWith(value = Suite.class)
@SuiteClasses(value = { MultiIndependentDistTest.class,
                        FirstOrderItemFunctionTest.class,
                        SimpleItemFunctionTest.class })
public class MatchingTestSuite { }
