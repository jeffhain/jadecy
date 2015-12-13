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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import net.jadecy.utils.ArgsUtils;

/**
 * Computes shortest paths, naively but slowly.
 * Designed to serve as reference for tests.
 * 
 * Maybe not so naive, but as least quite different than the main implementation.
 */
class NaiveOneShortestPathComputer {

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    public static void computeOneShortestPath(
            Collection<? extends InterfaceVertex> beginVertexColl,
            Collection<? extends InterfaceVertex> endVertexColl,
            //
            InterfaceVertexCollProcessor processor) {
        
        ArgsUtils.requireNonNull(processor);
        
        /*
         * 
         */
        
        final TreeSet<InterfaceVertex> beginVertexSet = new TreeSet<InterfaceVertex>(beginVertexColl);
        final TreeSet<InterfaceVertex> endVertexSet = new TreeSet<InterfaceVertex>(endVertexColl);
        
        /*
         * Map to avoid reusing a vertex for non-shorter paths
         * (allows to avoid cycle while not screening out shorter paths).
         */
        
        final TreeMap<InterfaceVertex,Integer> shortestLengthByReached = new TreeMap<InterfaceVertex,Integer>();
        
        /*
         * 
         */

        List<InterfaceVertex> currentShortestPathToEnd = null;

        for (InterfaceVertex beginVertex : beginVertexSet) {
            final List<InterfaceVertex> prevPath = new ArrayList<InterfaceVertex>();
            currentShortestPathToEnd = tryFindAShorterPathToEndFromIncluded(
                    prevPath,
                    beginVertex,
                    //
                    endVertexSet,
                    shortestLengthByReached,
                    //
                    currentShortestPathToEnd);
        }

        if (currentShortestPathToEnd != null) {
            processPath(currentShortestPathToEnd, processor);
        }
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    private static void processPath(
            List<InterfaceVertex> path,
            InterfaceVertexCollProcessor processor) {
        processor.processCollBegin();
        for (InterfaceVertex v : path) {
            processor.processCollVertex(v);
        }
        processor.processCollEnd();
    }
    
    /**
     * This method is recursive.
     * 
     * @param currentShortestPathToEnd Can be null.
     * @return A shortest path to end as a prolonging of rootPath,
     *         strictly shorter than the specified shortest path to end,
     *         else the specified shortest path to end, possibly null.
     */
    private static List<InterfaceVertex> tryFindAShorterPathToEndFromIncluded(
            List<InterfaceVertex> prevPath,
            InterfaceVertex currentVertex,
            //
            TreeSet<InterfaceVertex> endVertexSet, 
            TreeMap<InterfaceVertex,Integer> shortestLengthByReached,
            //
            List<InterfaceVertex> currentShortestPathToEnd) {
        
        // Creating the path by prolonging to current vertex.
        final ArrayList<InterfaceVertex> currentPath = new ArrayList<InterfaceVertex>(prevPath.size()+1);
        currentPath.addAll(prevPath);
        currentPath.add(currentVertex);

        // If we reached end set, that's the best we could do from current vertex.
        if (endVertexSet.contains(currentVertex)) {
            // Replacing only if better than current.
            if ((currentShortestPathToEnd == null)
                    || (currentPath.size() < currentShortestPathToEnd.size())) {
                currentShortestPathToEnd = currentPath;
            }
        } else {
            // Looking further.
            final int currentLengthToSucc = currentPath.size();
            for (InterfaceVertex successor : currentVertex.successors()) {

                // If already reached that vertex with a path of same length or shorter,
                // prolonging our path with it here is useless.
                final Integer length = shortestLengthByReached.get(successor);
                if ((length != null) && (length.intValue() <= currentLengthToSucc)) {
                    continue;
                }

                // Replaces null, or a longer length.
                shortestLengthByReached.put(successor, currentLengthToSucc);

                // Did not reach end set yet: looking further.
                currentShortestPathToEnd = tryFindAShorterPathToEndFromIncluded(
                        currentPath,
                        successor,
                        //
                        endVertexSet,
                        shortestLengthByReached,
                        //
                        currentShortestPathToEnd);
            }
        }

        return currentShortestPathToEnd;
    }
}
