package com.github.braully.graph.generator;

import edu.uci.ics.jung.graph.AbstractGraph;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Braully Rocha da Silva
 */
public interface IGraphGenerator {

    /**
     * Name unique (key) of graph generator.
     *
     * @return
     */
    public String[] getParameters();

    /**
     * Description in Visual representation of Generator.
     *
     * @return
     */
    public String getDescription();

    /**
     *
     *
     * @param parameters
     * @return
     */
    public AbstractGraph<Integer, Integer> generateGraph(Map parameters);
}
