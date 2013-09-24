package com.moallemi.iqswitch;

public class QueueLengthFunction implements SeparableFunction 
{
    private SwitchModel model;
    private int src, dest, length;

    public QueueLengthFunction(SwitchModel model,
                               int src, 
                               int dest, 
                               int length)
    {
        this.model = model;
        this.src = src;
        this.dest = dest;
        this.length = length;
    }

    public double getValue(SwitchState state) {
        return state.getQueueLength(src, dest) == length
            ? 1.0
            : 0.0;
    }

    public String toString() {
        return "len[" + (src+1) + "," + (dest+1) + "][" + length + "]";
    }
        

    public void addToMatrix(SwitchState state, 
                            double weight,
                            MatchingMatrix matrix) {
        double lambda = model.getArrivalProbability(src, dest);
        int q = state.getQueueLength(src, dest);
        int diff = length - q;

        switch (diff) {
        case -1:
            if (q > 0)
                matrix.addWeight(src, dest, weight * (1.0 - lambda));
            break;
        case 0:
            if (q > 0)
                matrix.addWeight(src, dest, - weight * (1.0 - 2.0 * lambda));
            matrix.addOffset(weight * (1.0 - lambda));
            break;
        case 1:
            if (q > 0)
                matrix.addWeight(src, dest, - weight * lambda);
            matrix.addOffset(weight * lambda);
            break;
        }
    }
}