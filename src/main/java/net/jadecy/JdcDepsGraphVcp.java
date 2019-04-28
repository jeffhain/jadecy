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
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import net.jadecy.graph.InterfaceVertex;
import net.jadecy.graph.InterfaceVertexCollProcessor;

/**
 * For Jadecy to process graph dependencies.
 */
class JdcDepsGraphVcp implements InterfaceVertexCollProcessor {
    
    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------
    
    private final ElemType elemType;
    private final List<SortedMap<String,SortedMap<String,SortedSet<String>>>> causesByDepByNameList;
    private final Set<InterfaceVertex> endVertexSet;
    private final int maxSteps;
    
    private SortedMap<String,SortedMap<String,SortedSet<String>>> currentCausesByDepByName;
    
    private int stepId = 0;
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    /**
     * @param maxSteps Max number of times edges are crossed, possibly 0.
     *        If < 0, no limit.
     */
    public JdcDepsGraphVcp(
            ElemType elemType,
            List<SortedMap<String,SortedMap<String,SortedSet<String>>>> causesByDepByNameList,
            Set<InterfaceVertex> endVertexSet,
            int maxSteps) {
        this.elemType = elemType;
        this.causesByDepByNameList = causesByDepByNameList;
        this.endVertexSet = endVertexSet;
        this.maxSteps = maxSteps;
    }
    
    @Override
    public void processCollBegin() {
        this.currentCausesByDepByName = new TreeMap<String,SortedMap<String,SortedSet<String>>>();
    }
    
    @Override
    public void processCollVertex(InterfaceVertex vertex) {
        JdcVcpUtils.addCausesByDepForVertex(
                this.elemType,
                this.endVertexSet,
                vertex,
                this.currentCausesByDepByName);
    }
    
    @Override
    public boolean processCollEnd() {
        this.causesByDepByNameList.add(this.currentCausesByDepByName);
        final int currentStepId = this.stepId++;
        return (this.maxSteps >= 0) && (currentStepId == this.maxSteps);
    }
}
