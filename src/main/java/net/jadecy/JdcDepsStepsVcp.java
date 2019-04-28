/*
 * Copyright 2015 Jeff Hain
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.jadecy;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import net.jadecy.code.AbstractCodeData;
import net.jadecy.graph.InterfaceVertex;
import net.jadecy.graph.InterfaceVertexCollProcessor;

/**
 * For Jadecy to process bulk dependencies.
 */
class JdcDepsStepsVcp implements  InterfaceVertexCollProcessor {
    
    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------
    
    private final List<SortedMap<String,Long>> byteSizeByDependencyList;
    private final int maxSteps;
    
    private int stepId = 0;
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    /**
     * @param maxSteps Max number of times edges are crossed, possibly 0.
     *        If < 0, no limit.
     */
    public JdcDepsStepsVcp(
            List<SortedMap<String,Long>> byteSizeByDependencyList,
            int maxSteps) {
        this.byteSizeByDependencyList = byteSizeByDependencyList;
        this.maxSteps = maxSteps;
    }
    
    @Override
    public void processCollBegin() {
        this.byteSizeByDependencyList.add(new TreeMap<String,Long>());
    }
    
    @Override
    public void processCollVertex(InterfaceVertex vertex) {
        final AbstractCodeData vertexD = (AbstractCodeData) vertex;
        
        final SortedMap<String,Long> byteSizeByDependency = this.byteSizeByDependencyList.get(this.byteSizeByDependencyList.size()-1);
        
        final Long previous = byteSizeByDependency.put(vertexD.displayName(), vertexD.byteSize());
        if (previous != null) {
            // Each dependency must only appears once in a step
            // (and among all steps as well, but we don't bother
            // to add a set to check it).
            throw new AssertionError();
        }
    }
    
    @Override
    public boolean processCollEnd() {
        final int currentStepId = this.stepId++;
        return (this.maxSteps >= 0) && (currentStepId == this.maxSteps);
    }
}
