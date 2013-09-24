package com.moallemi.math;

import ilog.concert.*;
import ilog.cplex.*;

import com.moallemi.util.PropertySet;

/**
* Class for initializing cplex objects.
*
* @author Ciamac Moallemi
* @version $Revision: 1.6 $, $Date: 2007-02-26 22:41:14 $
*/
public class CplexFactory {
    private PropertySet props;

    /**
     * Constructor.
     */
    public CplexFactory() { this(null); }

    /**
     * Constructor.
     *
     * @param props property set
     */
    public CplexFactory(PropertySet props) { this.props = props; }

    /**
     * Set a property.
     *
     * @param key the key
     * @param value the value
     */
    public void setProperty(String key, String value) {
        if (props == null)
            props = new PropertySet();
        props.setProperty(key, value);
    }

    /**
     * Get a cplex object.
     *
     * @return a fresh solver
     */
    public IloCplex getCplex() throws IloException {
        IloCplex cplex = new IloCplex();

        if (props != null) {
            if (props.containsKey("timelimit"))
                cplex.setParam(IloCplex.DoubleParam.TiLim, 
                               props.getDouble("timelimit"));

            if (props.containsKey("markowitz"))
                cplex.setParam(IloCplex.DoubleParam.EpMrk, 
                               props.getDouble("markowitz"));

            if (props.containsKey("barcolnonzeros")) 
                cplex.setParam(IloCplex.IntParam.BarColNz,
                               props.getInt("barcolnonzeros"));

            if (props.containsKey("lpmethod")) {
                String type = props.getString("lpmethod");
                if (type.equals("auto")) 
                    cplex.setParam(IloCplex.IntParam.RootAlg,
                                   IloCplex.Algorithm.Auto);
                else if (type.equals("barrier")) 
                    cplex.setParam(IloCplex.IntParam.RootAlg,
                                   IloCplex.Algorithm.Barrier);
                else if (type.equals("dual")) 
                    cplex.setParam(IloCplex.IntParam.RootAlg,
                                   IloCplex.Algorithm.Dual);
                else if (type.equals("network")) 
                    cplex.setParam(IloCplex.IntParam.RootAlg,
                                   IloCplex.Algorithm.Network);
                else if (type.equals("none")) 
                    cplex.setParam(IloCplex.IntParam.RootAlg,
                                   IloCplex.Algorithm.None);
                else if (type.equals("primal")) 
                    cplex.setParam(IloCplex.IntParam.RootAlg,
                                   IloCplex.Algorithm.Primal);
                else if (type.equals("sifting")) 
                    cplex.setParam(IloCplex.IntParam.RootAlg,
                                   IloCplex.Algorithm.Sifting);
                else
                    throw new IllegalArgumentException("unknown lpmethod "
                                                       + type);
            }

            if (props.containsKey("barcross")) {
                String type = props.getString("barcross");
                if (type.equals("auto")) 
                    cplex.setParam(IloCplex.IntParam.BarCrossAlg,
                                   IloCplex.Algorithm.Auto);
                else if (type.equals("dual")) 
                    cplex.setParam(IloCplex.IntParam.BarCrossAlg,
                                   IloCplex.Algorithm.Dual);
                else if (type.equals("none")) 
                    cplex.setParam(IloCplex.IntParam.BarCrossAlg,
                                   IloCplex.Algorithm.None);
                else if (type.equals("primal")) 
                    cplex.setParam(IloCplex.IntParam.BarCrossAlg,
                                   IloCplex.Algorithm.Primal);
                else
                    throw new IllegalArgumentException("unknown barcross "
                                                       + type);
            }                    

            if (props.containsKey("nooutput")) 
                cplex.setOut(null);

        }

        return cplex;
    }
}
