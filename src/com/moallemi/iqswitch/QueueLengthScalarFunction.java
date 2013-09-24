package com.moallemi.iqswitch;

public class QueueLengthScalarFunction 
    implements SeparableFunction 
{
    private SwitchModel model;
    private int src, dest;
    private ScalarFunction f;

    public QueueLengthScalarFunction(SwitchModel model,
                                     int src, 
                                     int dest, 
                                     ScalarFunction f) 
    {
        this.model = model;
        this.src = src;
        this.dest = dest;
        this.f = f;
    }

    public double getValue(SwitchState state) {
        return f.getValue(state.getQueueLength(src, dest));
    }

    public String toString() {
        return "len(" + f.toString() + ")[" + (src+1) + "," + (dest+1) + "]";
    }
        

    public void addToMatrix(SwitchState state, 
                            double weight,
                            MatchingMatrix matrix) {
        double lambda = model.getArrivalProbability(src, dest);
        int q = state.getQueueLength(src, dest);

        double hq0 = f.getValue(q);
        double hq1 = f.getValue(q + 1);

        matrix.addOffset(weight * (lambda * hq1 + (1.0 - lambda) * hq0));
        if (q > 0) {
            double w = (1.0 - lambda) * (f.getValue(q - 1) - hq0)
                + lambda * (hq0 - hq1);
            matrix.addWeight(src, dest, weight * w);
        }
    }
}