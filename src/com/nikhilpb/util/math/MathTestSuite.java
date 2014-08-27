package com.nikhilpb.util.math;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/15/13
 * Time: 12:46 AM
 * To change this template use File | Settings | File Templates.
 */
@RunWith(value = Suite.class)
@Suite.SuiteClasses(value = {DistributionsTest.class,
                                    PSDMatrixTest.class,
                                    RegressionTest.class})
public class MathTestSuite {
}
