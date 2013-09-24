package com.moallemi.math.stats;

import java.util.*;
import java.io.*;
import java.text.*;

/**
 * For performing basic statistical operations on a file.
 *
 * @author Ciamac Moallemi
 * @version $Revision: 1.1 $, $Date: 2005-01-20 04:58:54 $
 */
public class FileStatistics {
    
    private static abstract class ColumnOp {
	private int column;
	public ColumnOp(int column) { this.column = column; }
	public int getColumn() { return column; }
	public abstract void addSample(double x);
	public abstract String printString();
	public abstract void reset();
    }

    private static class HistogramColumnOp extends ColumnOp {
	private Histogram histogram;
	public HistogramColumnOp(int column, Histogram histogram) {
	    super(column);
	    this.histogram = histogram;
	}
	public void addSample(double x) {
	    if (histogram.isInRange(x)) 
		histogram.add(x);
	}
	public String printString() {
	    StringBuffer sb = new StringBuffer();
	    double total = histogram.getTotalCount();
	    DecimalFormat percent_df = new DecimalFormat("0.00%");
	    sb.append(" |");
	    for (int i = 0; i < histogram.getNumBins(); i++)
		sb.append(",")
		    .append(percent_df.format(((double)
					       histogram.getBinCount(i)
					       / total)));
	    sb.append(",|");
	    return sb.toString();
	}
	public void reset() { histogram.clear(); }
    }

    private static abstract class SampleStatisticsColumnOp extends ColumnOp {
	protected SampleStatistics statistics;
	public SampleStatisticsColumnOp(int column) {
	    super(column);
	    statistics = new SampleStatistics();
	}
	public void addSample(double x) {
	    statistics.addSample(x);
	}
	public void reset() { statistics.clear(); }
    }

    private static class MinColumnOp extends SampleStatisticsColumnOp {
	public MinColumnOp(int column) { super(column); }
	public String printString() { 
	    return Double.toString(statistics.getMinimum());
	}
    }

    private static class MaxColumnOp extends SampleStatisticsColumnOp {
	public MaxColumnOp(int column) { super(column); }
	public String printString() { 
	    return Double.toString(statistics.getMaximum()); 
	}
    }

    private static class MeanColumnOp extends SampleStatisticsColumnOp {
	public MeanColumnOp(int column) { super(column); }
	public String printString() { 
	    return Double.toString(statistics.getMean()); 
	}
    }

    private static class MedianColumnOp extends SampleStatisticsColumnOp {
	public MedianColumnOp(int column) { super(column); }
	public String printString() { 
	    return Double.toString(statistics.getMedian()); 
	}
    }

    private static class StDevColumnOp extends SampleStatisticsColumnOp {
	public StDevColumnOp(int column) { super(column); }
	public String printString() { 
	    return Double.toString(statistics.getStandardDeviation());
	}
    }


    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(String[] argv) throws Exception {
	ArrayList opList = new ArrayList();
	ArrayList fileList = new ArrayList();
	boolean skipFirst = false;
	for (int i = 0; i < argv.length; i++) {
	    if (argv[i].equals("--histogram")) {
		int column = Integer.parseInt(argv[++i]);
		double min = Double.parseDouble(argv[++i]);
		double max = Double.parseDouble(argv[++i]);
		int binCount = Integer.parseInt(argv[++i]);
		boolean negInf = Boolean.valueOf(argv[++i]).booleanValue();
		boolean posInf = Boolean.valueOf(argv[++i]).booleanValue();
		Histogram histogram = new Histogram(min,
						    max,
						    binCount,
						    negInf,
						    posInf);
		opList.add(new HistogramColumnOp(column,
						 histogram));
	    }
	    else if (argv[i].equals("--min")) {
		int column = Integer.parseInt(argv[++i]);
		opList.add(new MinColumnOp(column));
	    }
	    else if (argv[i].equals("--max")) {
		int column = Integer.parseInt(argv[++i]);
		opList.add(new MaxColumnOp(column));
	    }
	    else if (argv[i].equals("--mean")) {
		int column = Integer.parseInt(argv[++i]);
		opList.add(new MeanColumnOp(column));
	    }
	    else if (argv[i].equals("--median")) {
		int column = Integer.parseInt(argv[++i]);
		opList.add(new MedianColumnOp(column));
	    }
	    else if (argv[i].equals("--stdev")) {
		int column = Integer.parseInt(argv[++i]);
		opList.add(new StDevColumnOp(column));
	    }
	    else if (argv[i].equals("--skipFirst")) {
		skipFirst = true;
	    }
	    else {
		fileList.add(argv[i]);
	    }
	}

	ColumnOp[] ops = (ColumnOp[])  opList.toArray(new ColumnOp [0]);
	for (Iterator i = fileList.iterator(); i.hasNext(); ) {
	    String fileName = (String) i.next();
	    BufferedReader in;
	    if (fileName.equals("-")) 
		in = new BufferedReader(new InputStreamReader(System.in));
	    else
		in = new BufferedReader(new FileReader(fileName));

	    for (int k = 0; k < ops.length; k++)
		ops[k].reset();

	    String line;
	    if (skipFirst) in.readLine();
	    while ((line = in.readLine()) != null) {
		String[] columns = line.split(",");
		for (int k = 0; k < ops.length; k++) {
		    String stringValue = columns[ops[k].getColumn()];
		    if (stringValue.equals("-") || stringValue.equals("")) 
			;
		    else {
			double value = 
			    Double.parseDouble(columns[ops[k].getColumn()]);
			ops[k].addSample(value);
		    }
		}
	    }
	    in.close();

	    System.out.print(fileName);
	    for (int k = 0; k < ops.length; k++)
		System.out.print(" " + ops[k].printString());
	    System.out.println();
	}
    }
}
