package com.github.braully.graph.operation;

import com.github.braully.graph.UndirectedSparseGraphTO;
import java.util.Map;

/**
 * Graph operation.
 *
 * @author braully
 */
public interface IGraphOperation {

    public static final String DEFAULT_PARAM_NAME_RESULT = "result";
    public static final String DEFAULT_PARAM_NAME_SET = "set";

    public String getTypeProblem();

    public String getName();

    public Map<String, Object> doOperation(UndirectedSparseGraphTO<Integer, Integer> graph);
}
