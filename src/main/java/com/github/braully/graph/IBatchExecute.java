/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.braully.graph;

import com.github.braully.graph.operation.IGraphOperation;

/**
 *
 * @author Braully Rocha da Silva
 */
public interface IBatchExecute {

    public IGraphOperation[] getOperations();

    public String getDefaultInput();

}
