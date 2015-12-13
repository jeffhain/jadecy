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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Utility treatments for work graphs.
 */
class WorkGraphUtilz {

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /**
     * Work vertices are create while iterating over the specified graph,
     * first having id 1, second id 2, etc.
     * As a result, if the specified collection is sorted according to vertices
     * compareTo(...) method, the relative ordering of work vertices is
     * identical to the relative ordering of their backing vertices.
     * 
     * It is allowed for the specified collection not to contain some successors
     * or some of its vertices, in which case they just won't be included in
     * the created work graph.
     * 
     * @param graph Graph from which a work graph must be created.
     *        Must not contain duplicates (not checked).
     * @param mustIgnoreDeadEnds True if must ignore vertices that have no
     *        successor, recursively, false otherwise. Useful to build a reduced
     *        work graph for computing cycles or SCCs.
     * @return A work graph corresponding to the specified graph.
     */
    public static TreeSet<WorkVertex> newWorkGraph(
            Collection<? extends InterfaceVertex> graph,
            boolean mustIgnoreDeadEnds) {
        final TreeSet<WorkVertex> workGraph = new TreeSet<WorkVertex>();
        
        final HashSet<InterfaceVertex> unicitySet = new HashSet<InterfaceVertex>();
        
        int nextId = 1;
        for (InterfaceVertex v : graph) {
            if (!unicitySet.add(v)) {
                // Duplicate.
                continue;
            }
            if (mustIgnoreDeadEnds) {
                if (v.successors().size() == 0) {
                    // We do clean-up later anyway,
                    // but doesn't hurt to ignore it here.
                    continue;
                }
            }
            final WorkVertex wv = new WorkVertex(v, nextId++);
            workGraph.add(wv);
        }
        
        computeSuccessorsAndPredecessors(workGraph);
        
        if (mustIgnoreDeadEnds) {
            removeDeadEnds(workGraph);
        }
        
        return workGraph;
    }

    /**
     * Removes the specified vertex from the specified graph collection, and if
     * it succeeds to, also from its graph structure.
     * 
     * @return True if did remove the specified vertex from the specified graph,
     *         false otherwise.
     */
    public static boolean removeFromGraph(
            Set<WorkVertex> workGraph,
            WorkVertex workVertex) {
        final boolean didIt = workGraph.remove(workVertex);
        if (!didIt) {
            return false;
        }
        {
            for (WorkVertex succ : workVertex.successors()) {
                final boolean forCheck = succ.predecessors().remove(workVertex);
                if (!forCheck) {
                    throw new AssertionError();
                }
            }
            workVertex.successors().clear();
        }
        {
            for (WorkVertex pred : workVertex.predecessors()) {
                final boolean forCheck = pred.successors().remove(workVertex);
                if (!forCheck) {
                    throw new AssertionError();
                }
            }
            workVertex.predecessors().clear();
        }
        return true;
    }

    /**
     * Removes predecessors and successors of vertices of the specified graph
     * collection that are not in it.
     */
    public static void detachVerticesNotInGraph(Set<WorkVertex> workGraph) {
        for (WorkVertex v : workGraph) {
            removePredSuccNotInGraph(workGraph, v);
        }
    }

    /**
     * Removes predecessors and successors of the specified vertex if they are
     * not in the specified graph collection.
     */
    public static void removePredSuccNotInGraph(
            Set<WorkVertex> workGraph,
            WorkVertex workVertex) {
        {
            final Iterator<WorkVertex> it = workVertex.successors().iterator();
            while (it.hasNext()) {
                final WorkVertex succ = it.next();
                if (!workGraph.contains(succ)) {
                    it.remove();
                    final boolean forCheck = succ.predecessors().remove(workVertex);
                    if (!forCheck) {
                        throw new AssertionError();
                    }
                }
            }
        }
        {
            final Iterator<WorkVertex> it = workVertex.predecessors().iterator();
            while (it.hasNext()) {
                final WorkVertex pred = it.next();
                if (!workGraph.contains(pred)) {
                    it.remove();
                    final boolean forCheck = pred.successors().remove(workVertex);
                    if (!forCheck) {
                        throw new AssertionError();
                    }
                }
            }
        }
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------

    private WorkGraphUtilz() {
    }
    
    private static void computeSuccessorsAndPredecessors(TreeSet<WorkVertex> workGraph) {
        
        /*
         * Computing work vertex by backing vertex.
         */
        
        final HashMap<InterfaceVertex, WorkVertex> workVertexByBackingVertex =
            new HashMap<InterfaceVertex, WorkVertex>();
        for (WorkVertex wv : workGraph) {
            final InterfaceVertex bv = wv.backingVertex();
            if (bv == null) {
                throw new AssertionError();
            }
            workVertexByBackingVertex.put(bv, wv);
        }
        
        /*
         * Creating successors/predecessors links.
         */
        
        for (WorkVertex wv : workGraph) {
            final InterfaceVertex bv = wv.backingVertex();
            for (InterfaceVertex bvs : bv.successors()) {
                // Can be null in case of dead ends removal.
                final WorkVertex wvs = workVertexByBackingVertex.get(bvs);
                if (wvs == null) {
                    /*
                     * Allowing for not having a work vertex
                     * for actual successors, which happens
                     * if the successor was not in the specified
                     * list of vertices.
                     */
                    if (false) {
                        if (bvs.successors().size() != 0) {
                            throw new AssertionError();
                        }
                    }
                } else {
                    wv.successors().add(wvs);
                    wvs.predecessors().add(wv);
                }
            }
        }
    }

    /**
     * Removes vertices that have no successors, recursively (but is not
     * recursive).
     */
    private static void removeDeadEnds(TreeSet<WorkVertex> workGraph) {
        // To avoid recursion.
        final ArrayList<WorkVertex> leaves = new ArrayList<WorkVertex>();
        
        /*
         * Computing initial leaves.
         */
        
        for (WorkVertex wv : workGraph) {
            if (wv.successors().size() == 0) {
                leaves.add(wv);
            }
        }
        
        /*
         * Removing leaves and adding new ones, until none remains.
         */

        while (leaves.size() > 0) {
            final WorkVertex leaf = leaves.remove(leaves.size()-1);
            
            // Removing leaf from graph collection.
            {
                final boolean forCheck = workGraph.remove(leaf);
                if (!forCheck) {
                    throw new AssertionError();
                }
            }
            
            // Removing leaf from graph structure.
            for (WorkVertex p : leaf.predecessors()) {
                final boolean forCheck = p.successors().remove(leaf);
                if (!forCheck) {
                    throw new AssertionError();
                }
                if (p.successors().size() == 0) {
                    leaves.add(p);
                }
            }
            leaf.predecessors().clear(); // help GC
        }
    }
}
