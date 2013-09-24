package com.moallemi.contresalloc;

public class PiecewiseConstantFunction {
    private double max;
    private double[] offset;
    // function takes value[i] if offset[i] <= C < offset[i+1], i < size
    // offset[0] = value[0] = 0 always
    // function value[i] and offset should be strictly increasing
    // values are assumed to be increasing
    // function is defined over 0 <= C <= max
    private double[] value;
    private int size;
    

    public PiecewiseConstantFunction(double max) {
        this.max = max;
        size = 0;
    }

    private void reset(int capacity) {
        if (value == null || value.length < capacity) {
            value = new double [capacity];
            offset = new double [capacity];
        }
        size = 0;
    }


    // check theat the function looks OK
    public void verify() {
        if (size == 0)
            return;

        if (offset[0] != 0.0 || value[0] != 0.0)
            throw new IllegalStateException("bad origin");

        for (int i = 1; i < size; i++) {
            if (offset[i-1] >= offset[i])
                throw new IllegalStateException("offsets not increasing");
            if (value[i-1] >= value[i])
                throw new IllegalStateException("values not increasing");
        }

        if (offset[size-1] > max)
            throw new IllegalStateException("offset exceeds max");
    }

    // set this function to the solution of the optimization problem
    // maximize v*x + f(C - w*x)
    // s.t. x \in {0,1}, w x <= C
    // (as a function of C)
    // assume that v >= 0, w > 0
    public void setSingleOpt(double v, 
                             double w, 
                             PiecewiseConstantFunction f) 
    {
        if (w <= 0.0)
            throw new IllegalArgumentException("negative parameters");
        if (f != null && f.max != max)
            throw new IllegalArgumentException("function domain insufficient");

        if (f == null) {
            reset(2);
            offset[size] = 0.0;
            value[size] = 0.0;
            size++;
            if (v > 0.0 && w <= max) {
                offset[size] = w;
                value[size] = v;
                size++;
            }
        }
        else if (v <= 0.0) {
            // just copy the other function
            reset(f.size);
            System.arraycopy(f.offset, 
                             0,
                             offset,
                             0,
                             f.size);
            System.arraycopy(f.value, 
                             0,
                             value,
                             0,
                             f.size);
            size = f.size;
        }
        else {
            reset(2*f.size);

            int i = 0;
            int i2 = 0;
            int j = 0;
            int j2 = 0;
            double lastValue = -1.0;
            while (j < f.size) {
                double C;
                if (i >= f.size) {
                    C = f.offset[j] + w;
                    j++;
                }
                else {
                    double Ci = f.offset[i];
                    double Cj = f.offset[j] + w;
                    if (Ci < Cj) { 
                        C = Ci;
                        i++;
                    }
                    else if (Cj < Ci) {
                        C = Cj;
                        j++;
                    }
                    else {
                        C = Ci;
                        i++;
                        j++;
                    }
                }

                if (C > max)
                    break;

                // lookup f evaluated at C
                while (i2 < f.size - 1 && f.offset[i2+1] <= C) 
                    i2++;
               
                // lookup f evaluated at C - w
                if (C >= w) {
                    double D = C - w;
                    while (j2 < f.size - 1 && f.offset[j2+1] <= D)
                        j2++;
                }
                
                double newValue = C < w 
                    ? f.value[i2]
                    : Math.max(f.value[i2], v + f.value[j2]);
                 
                if (newValue > lastValue) {
                    offset[size] = C;
                    value[size] = newValue;
                    lastValue = newValue;
                    size++;
                }
                else if (newValue < lastValue)
                    throw new IllegalStateException("function is "
                                                    + "not increasing");
            }
        }

        //verify();
    }

    // define
    // g(x) = maximize this(y) + f(x - y)
    //        subject to 0 <= y <= x
    // computes g(C-w) - g(C)
    double computePairOpt(double C, double w, PiecewiseConstantFunction f) {
        if (C > max || (f != null && C > f.max)) 
            throw new IllegalArgumentException("function domain insufficient");
        if (w <= 0.0 || C < w)
            throw new IllegalArgumentException("negative parameters");    

        double Cw = C - w;
        double opt1 = -1.0;
        double opt2 = -1.0;

        if (f == null) {
            for (int i = 0; i < size; i++) {
                double y = offset[i];
                if (y <= Cw) 
                    opt1 = opt2 = value[i];
                else if (y <= C)
                    opt1 = value[i];
                else
                    break;
            }
        }
        else {
            int j1 = f.size - 1;
            int j2 = f.size - 1;
            for (int i = 0; i < size; i++) {
                double y = offset[i];
                if (y > C)
                    break;
                while (j1 >= 0) {
                    if (y + f.offset[j1] <= C) {
                        double newValue = value[i] + f.value[j1];
                        if (newValue > opt1)
                            opt1 = newValue;
                        break;
                    }
                    j1--;
                }
                while (j2 >= 0) {
                    if (y + f.offset[j2] <= Cw) {
                        double newValue = value[i] + f.value[j2];
                        if (newValue > opt2)
                            opt2 = newValue;
                        break;
                    }
                    j2--;
                }
            }
        }

        if (opt1 < 0.0 || opt2 < 0.0)
            throw new IllegalStateException("failed to minimize");

        return opt2 - opt1;
    }
}
            
        