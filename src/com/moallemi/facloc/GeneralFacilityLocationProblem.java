package com.moallemi.facloc;

import java.util.*;

import com.moallemi.util.data.Pair;
import com.moallemi.math.graph.*;
import com.moallemi.minsum.*;

public class GeneralFacilityLocationProblem 
    implements FacilityLocationProblem 
{
    private int cityCount, facilityCount;
    private double[][] distance;
    private double[] cost;

    private static final Comparator<Pair<Double,Integer>> comparator = 
        new Comparator<Pair<Double,Integer>>() {
        public int compare(Pair<Double,Integer> o1,
                           Pair<Double,Integer> o2) {
            return o1.getFirst().compareTo(o2.getFirst());
        }
    };

    public GeneralFacilityLocationProblem(int cityCount,
                                          int facilityCount,
                                          int closest,
                                          double[][] distance,
                                          double[] cost) {
        this.cityCount = cityCount;
        this.facilityCount = facilityCount;
        this.distance = distance;
        this.cost = cost;

        // keep only the closest neighbors
        if (closest > 0) {
            Pair<Double,Integer>[] tmp  
                = (Pair<Double,Integer>[]) (new Pair [facilityCount]);
            for (int i = 0; i < cityCount; i++) {
                for (int j = 0; j < facilityCount; j++) 
                    tmp[j] =
                        new Pair<Double,Integer> (new Double(distance[i][j]),
                                                  new Integer(j));
                Arrays.sort(tmp, comparator);
                for (int x = closest; x < facilityCount; x++) {
                    int j = tmp[x].getSecond().intValue();
                    distance[i][j] = Double.MAX_VALUE;
                }
            }
        }                
    }

    public int getCityCount() { return cityCount; }
    public int getFacilityCount() { return facilityCount; }
    public double getConstructionCost(int i) { return cost[i]; }
    public double getDistance(int i, int j) {
        return distance[i][j];
    }

    public double getAllFacilityCost() {
        double sum = 0.0;
        for (int i = 0; i < facilityCount; i++)
            sum += cost[i];
        for (int i = 0; i < cityCount; i++) {
            double min = Double.MAX_VALUE;
            for (int j = 0; j < facilityCount; j++) {
                if (distance[i][j] < min)
                    min = distance[i][j];
            }
            sum += min;
        }
        return sum;
    }

}
