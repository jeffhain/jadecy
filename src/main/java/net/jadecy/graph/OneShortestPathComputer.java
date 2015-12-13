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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.jadecy.utils.ArgsUtils;

/**
 * Computes one shortest path from a set of vertices to another.
 */
public class OneShortestPathComputer {
    
    /*
     * Since we only compute one shortest path, we don't need to keep track of
     * much things as the algorithm makes progress, and since we deal with
     * non-weighted graphs, we don't need to bother with heaps or such, so the
     * algorithm basically boils down to Dijkstra idea of growing from begin
     * (a set here, not a single vertex) until reaching end, and is quite fast.
     */
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    /**
     * Computes a shortest path from begin vertices to end vertices.
     * 
     * For a same input, and regardless of vertices hash codes,
     * this method always produces the same output.
     * 
     * @param beginVertexColl Collection of begin vertices.
     * @param endVertexColl Collection of end vertices.
     * @param processor Processor to process the vertices of the computed
     *        shortest path. Not called for empty collections.
     * @throws NullPointerException if any argument is null.
     */
    public static void computeOneShortestPath(
            Collection<? extends InterfaceVertex> beginVertexColl,
            Collection<? extends InterfaceVertex> endVertexColl,
            //
            InterfaceVertexCollProcessor processor) {
        
        ArgsUtils.requireNonNull(beginVertexColl);
        ArgsUtils.requireNonNull(endVertexColl);
        ArgsUtils.requireNonNull(processor);
        
        if ((beginVertexColl.size() == 0)
                || (endVertexColl.size() == 0)) {
            // Easy.
            return;
        }
        
        // Using sets to guard against duplications, and our own ones even if inputs
        // are sets, not to risk messing up inputs, and to be sure of contains(...) performances.
        Set<InterfaceVertex> beginVertexSet = new HashSet<InterfaceVertex>(beginVertexColl);
        Set<InterfaceVertex> endVertexSet = new HashSet<InterfaceVertex>(endVertexColl);

        /*
         * We need to rule out singleton path here because further
         * treatments ignore successors identical to predecessor,
         * which would screen out singleton paths if a candidate vertex
         * has itself as successor.
         * Looping on a sorted set, for determinism whatever hash codes.
         */
        
        InterfaceVertex singletonPath = null;
        {
            final SortedSet<InterfaceVertex> beginVertexSortedSet = new TreeSet<InterfaceVertex>(beginVertexSet);
            for (InterfaceVertex v : beginVertexSortedSet) {
                if (endVertexSet.contains(v)) {
                    singletonPath = v;
                    break;
                }
            }
        }
        
        if (singletonPath != null) {
            // Help GC.
            beginVertexSet = null;
            endVertexSet = null;

            processSingletonPath(singletonPath, processor);
            return;
        }
        
        /*
         * 
         */
        
        // Need sorted sets for determinism.
        SortedSet<InterfaceVertex> previousStep = new TreeSet<InterfaceVertex>(beginVertexSet);
        SortedSet<InterfaceVertex> currentStep = new TreeSet<InterfaceVertex>();
        
        // Help GC.
        beginVertexSet = null;

        /*
         * key = a reached vertex
         * value = a predecessor of the reached vertex corresponding
         *         to a shortest-known path from begin set to the reached vertex.
         */
        Map<InterfaceVertex, InterfaceVertex> prevByReached = new HashMap<InterfaceVertex, InterfaceVertex>();

        InterfaceVertex firstFoundShortestPathEndVertex = null;
        
        int currentEdgeCount = 0;
        
        LOOP_1 : while (previousStep.size() != 0) {
            
            currentEdgeCount++;
            
            if (currentEdgeCount < 0) {
                // These graphs are not supposed to be that big.
                throw new ArithmeticException("int overflow");
            }
            
            for (InterfaceVertex currentVertex : previousStep) {
                for (InterfaceVertex currentVertexSucc : currentVertex.successors()) {
                    if (currentVertexSucc == currentVertex) {
                        // OK to ignore because we already ruled out singleton paths,
                        // such as we know that previous step doesn't contain a vertex
                        // that is also in end set.
                        continue;
                    }
                    // NB: Could use putIfAbsent(...) for faster code, when available.
                    final InterfaceVertex oldPrev = prevByReached.get(currentVertexSucc);
                    if (oldPrev == null) {
                        // First time we reach this successor.
                        prevByReached.put(currentVertexSucc, currentVertex);
                        
                        // Building next step.
                        currentStep.add(currentVertexSucc);
                        
                        if (endVertexSet.contains(currentVertexSucc)) {
                            // Bingo.
                            firstFoundShortestPathEndVertex = currentVertexSucc;
                            break LOOP_1;
                        }
                    } else {
                        // Already reached the successor, during this step or a previous one,
                        // and if we are still there that means it wasn't in end set.
                    }
                }
            }
            
            /*
             * Swapping previous and current steps.
             */
            
            SortedSet<InterfaceVertex> tmp = previousStep;
            previousStep = currentStep;
            currentStep = tmp;
            
            // Clearing current step.
            currentStep.clear();
        }
        
        final InterfaceVertex[] path;
        if (firstFoundShortestPathEndVertex != null) {
            // Computing path.
            InterfaceVertex tmpV = firstFoundShortestPathEndVertex;
            path = new InterfaceVertex[currentEdgeCount+1];
            for (int i = currentEdgeCount + 1; --i >= 0;) {
                path[i] = tmpV;
                // NB: null for last.
                tmpV = prevByReached.get(tmpV);
            }
        } else {
            path = null;
        }
        
        if (path != null) {
            // Help GC.
            endVertexSet = null;
            previousStep = null;
            currentStep = null;
            prevByReached = null;
            
            processPath(path, processor);
        }
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    private OneShortestPathComputer() {
    }

    private static void processSingletonPath(
            InterfaceVertex vertex,
            InterfaceVertexCollProcessor processor) {
        processor.processCollBegin();
        processor.processCollVertex(vertex);
        processor.processCollEnd();
    }
    
    private static void processPath(
            InterfaceVertex[] path,
            InterfaceVertexCollProcessor processor) {
        processor.processCollBegin();
        for (InterfaceVertex vertex : path) {
            processor.processCollVertex(vertex);
        }
        processor.processCollEnd();
    }
}
