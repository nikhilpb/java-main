package com.nikhilpb.util.math;

import Jama.Matrix;
import org.junit.Test;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: nikhilpb
 * Date: 10/14/13
 * Time: 11:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class PSDMatrixTest {
    private static final double kTol = 1E-4;
    private static final String kFilename = "/tmp/psdtest-file";
    @Test
    public void testBase() throws Exception {
        double[][] psdMatArray = {{2.,0.},{0.,1.}};
        PSDMatrix psdMatrix = new PSDMatrix(new Matrix(psdMatArray));
        Matrix sqrt = psdMatrix.sqrt();
        Matrix sqrtt = sqrt.transpose();
        Matrix matrix = sqrt.times(sqrtt);
        assert Math.abs(matrix.minus(new Matrix(psdMatArray)).normF()) < kTol;
        psdMatrix.writeToFile(kFilename);

        PSDMatrix psdMatrix1 = new PSDMatrix(kFilename);

        assert psdMatrix.approxEqual(psdMatrix, kTol);
        assert psdMatrix.approxEqual(psdMatrix1, kTol);

        File file = new File(kFilename);
        assert file.delete();
    }
}
