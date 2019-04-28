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

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import net.jadecy.code.AbstractCodeData;
import net.jadecy.code.CodeDataUtils;
import net.jadecy.graph.InterfaceVertex;
import net.jadecy.graph.InterfaceVertexCollProcessor;
import net.jadecy.utils.ComparableTreeMap;

/**
 * For Jadecy to process strongly connected components.
 */
class JdcSccVcp implements InterfaceVertexCollProcessor {
    
    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------
    
    private final ElemType elemType;
    private final List<SortedMap<String,Long>> sccList;
    
    /*
     * temps
     */
    
    private final ArrayList<InterfaceVertex> tmpSccDataList = new ArrayList<InterfaceVertex>();
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    public JdcSccVcp(
            ElemType elemType,
            List<SortedMap<String,Long>> sccList) {
        this.elemType = elemType;
        this.sccList = sccList;
    }
    
    @Override
    public void processCollBegin() {
        this.tmpSccDataList.clear();
    }
    
    @Override
    public void processCollVertex(InterfaceVertex vertex) {
        this.tmpSccDataList.add(vertex);
    }
    
    @Override
    public boolean processCollEnd() {
        final ArrayList<InterfaceVertex> sccDataList = this.tmpSccDataList;
        if (sccDataList.size() == 1) {
            // We ignore these.
            return false;
        }
        if (this.elemType == ElemType.CLASS) {
            // Checking that even if merging nested classes,
            // in case used creates such dependencies programmatically
            // (as done in unit tests).
            if (CodeDataUtils.haveSameTopLevelClass(sccDataList)) {
                // Ignoring SCCs if only due to nested classes of a same
                // top level class.
                return false;
            }
        }
        final ComparableTreeMap<String,Long> scc = new ComparableTreeMap<String,Long>();
        for (InterfaceVertex vertex : sccDataList) {
            final AbstractCodeData vertexD = (AbstractCodeData) vertex;
            final Object forCheck = scc.put(vertexD.displayName(), vertexD.byteSize());
            if (forCheck != null) {
                throw new AssertionError();
            }
        }
        this.sccList.add(scc);
        return false;
    }
}
