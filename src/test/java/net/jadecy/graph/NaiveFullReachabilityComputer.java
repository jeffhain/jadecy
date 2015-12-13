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
package net.jadecy.graph;

import java.util.Collection;
import java.util.HashSet;
import java.util.TreeSet;

/**
 * Computes full reachability (i.e. with unlimited number of steps),
 * simply but slowly.
 * Designed to serve as reference for tests.
 */
class NaiveFullReachabilityComputer {
    
    /*
     * We don't bother with continuations, since we only use it for tests.
     */

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    public static void computeFullReachability(
            Collection<? extends InterfaceVertex> beginVertexColl,
            boolean mustIncludeBeginVertices,
            boolean mustIncludeReachedBeginVertices,
            //
            InterfaceVertexCollProcessor processor) {
        
        final HashSet<InterfaceVertex> beginVertexSet = new HashSet<InterfaceVertex>(beginVertexColl);
        
        /*
         * Computing reached vertices.
         */
        
        final TreeSet<InterfaceVertex> reachedVertexSet = new TreeSet<InterfaceVertex>();
        for (InterfaceVertex beginVertex : beginVertexSet) {
            addReachableVerticesInto(
                    beginVertex,
                    reachedVertexSet);
        }
        
        /*
         * 
         */
        
        if (mustIncludeBeginVertices) {
            reachedVertexSet.addAll(beginVertexSet);
        } else {
            if (!mustIncludeReachedBeginVertices) {
                reachedVertexSet.removeAll(beginVertexSet);
            }
        }
        
        /*
         * 
         */
        
        if (reachedVertexSet.size() != 0) {
            processor.processCollBegin();
            for (InterfaceVertex reachedVertex : reachedVertexSet) {
                processor.processCollVertex(reachedVertex);
            }
            processor.processCollEnd();
        }
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    /**
     * This method is recursive.
     */
    private static void addReachableVerticesInto(InterfaceVertex vertex, TreeSet<InterfaceVertex> set) {
        for (InterfaceVertex successor : vertex.successors()) {
            if (set.add(successor)) {
                addReachableVerticesInto(successor, set);
            }
        }
    }
}
