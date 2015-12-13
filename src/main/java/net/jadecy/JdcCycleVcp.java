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
import java.util.SortedSet;

import net.jadecy.code.AbstractCodeData;
import net.jadecy.code.ClassData;
import net.jadecy.code.CodeDataUtils;
import net.jadecy.code.PackageData;
import net.jadecy.graph.CyclesUtils;
import net.jadecy.graph.InterfaceVertex;
import net.jadecy.graph.InterfaceVertexCollProcessor;

/**
 * For Jadecy to process cycles.
 */
class JdcCycleVcp implements InterfaceVertexCollProcessor {
    
    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------
    
    private final ElemType elemType;
    private final InterfaceCycleProcessor processor;
    
    /*
     * temps
     */
    
    private final ArrayList<AbstractCodeData> tmpCycle = new ArrayList<AbstractCodeData>();
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    public JdcCycleVcp(
            ElemType elemType,
            InterfaceCycleProcessor processor) {
        this.elemType = elemType;
        this.processor = processor;
    }
    
    //@Override
    public void processCollBegin() {
        this.tmpCycle.clear();
    }
    
    //@Override
    public void processCollVertex(InterfaceVertex vertex) {
        this.tmpCycle.add((AbstractCodeData) vertex);
    }
    
    //@Override
    public boolean processCollEnd() {
        final int cycleSize = this.tmpCycle.size();
        
        if (this.elemType == ElemType.CLASS) {
            // Checking that even if merging nested classes,
            // in case used creates such dependencies programmatically
            // (as done in unit tests).
            if (CodeDataUtils.haveSameTopLevelClass(this.tmpCycle)) {
                // Ignoring cycle if only due to nested classes of a same
                // top level class.
                return false;
            }
            
            final String[] names = new String[cycleSize];
            for (int i = 0; i < names.length; i++) {
                names[i] = this.tmpCycle.get(i).displayName();
            }
            
            // Need to normalize since cycles computers don't.
            CyclesUtils.normalizeCycle(names);
            
            return this.processor.processCycle(names, null);
        } else {
            final String[] names = new String[cycleSize];
            final String[][] causesArr = new String[cycleSize][];
            for (int i = 0; i < cycleSize; i++) {
                final PackageData fromPackageData = (PackageData) this.tmpCycle.get(i);
                names[i] = fromPackageData.displayName();

                final PackageData toPackageData;
                if (i < cycleSize-1) {
                    toPackageData = (PackageData) this.tmpCycle.get(i+1);
                } else {
                    toPackageData = (PackageData) this.tmpCycle.get(0);
                }
                final SortedSet<ClassData> depCauses = fromPackageData.causeSetBySuccessor().get(toPackageData);

                causesArr[i] = JdcVcpUtils.causesToStringArr(depCauses);
            }

            // Causes arrays are already sorted, due to coming out of sorted
            // sets, so we just need to move these arrays around according
            // to names sorting.
            CyclesUtils.normalizeCycleWithCauses(names, causesArr);

            return this.processor.processCycle(names, causesArr);
        }
    }
}
