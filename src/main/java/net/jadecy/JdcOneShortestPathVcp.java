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
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import net.jadecy.code.AbstractCodeData;
import net.jadecy.code.PackageData;
import net.jadecy.graph.InterfaceVertex;
import net.jadecy.graph.InterfaceVertexCollProcessor;

/**
 * For Jadecy to process one shortest path.
 */
class JdcOneShortestPathVcp implements InterfaceVertexCollProcessor {
    
    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------
    
    private final ElemType elemType;
    private final List<SortedMap<String,SortedSet<String>>> depCausesByNameList;
    
    /*
     * temps
     */
    
    private final List<InterfaceVertex> tmpVertexList = new ArrayList<InterfaceVertex>();
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    public JdcOneShortestPathVcp(
            ElemType elemType,
            List<SortedMap<String,SortedSet<String>>> depCausesByNameList) {
        this.elemType = elemType;
        this.depCausesByNameList = depCausesByNameList;
    }
    
    @Override
    public void processCollBegin() {
    }
    
    @Override
    public void processCollVertex(InterfaceVertex vertex) {
        this.tmpVertexList.add(vertex);
    }
    
    @Override
    public boolean processCollEnd() {
        
        final int dataCount = this.tmpVertexList.size();
        
        for (int i = 0; i < dataCount; i++) {
            final AbstractCodeData fromData = (AbstractCodeData) this.tmpVertexList.get(i);
            
            // Map as ersatz of a Pair.
            final SortedMap<String,SortedSet<String>> depCausesByName = new TreeMap<String,SortedSet<String>>();
            
            final SortedSet<String> depCauses = new TreeSet<String>();
            if (this.elemType == ElemType.PACKAGE) {
                final boolean isLast = (i == dataCount-1);
                if (!isLast) {
                    final PackageData fromPackageData = (PackageData) fromData;
                    final PackageData toPackageData = (PackageData) this.tmpVertexList.get(i+1);
                    JdcVcpUtils.addCausesInto(fromPackageData, toPackageData, depCauses);
                }
            }
            
            depCausesByName.put(fromData.displayName(), depCauses);
            this.depCausesByNameList.add(depCausesByName);
        }
        
        return false;
    }
}
