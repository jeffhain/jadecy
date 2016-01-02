/*
 * Copyright 2016 Jeff Hain
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.jadecy.utils.ArgsUtils;

/**
 * Implements the cycle detection algorithm (not the ranking part) described in the paper
 * "Efficient Retrieval and Ranking of Undesired Package Cycles in Large Software Systems",
 * by Jannik Laval, Jean-Remy Falleri, Philippe Vismara, and Stephane Ducasse,
 * published in "Journal of Object Technology":
 * http://www.jot.fm/issues/issue_2012_04/article4.pdf
 * modulo:
 * - handling of cycles of size 1,
 * - possibility to ignore cycles above a certain size.
 * 
 * Serves as reference, to make sure our optimized algorithm is at least as good
 * in computing shortest cycles.
 */
class RefShortestCyclesComputer {
    
    /*
     * The basic idea of paper's algorithm: "the decomposition of a SCC in
     * multiple short cycles covering all dependencies of the SCC",
     * by iterating on vertices, and for each of them, pushing a
     * Breadth-First Search (BFS) until all predecessors have been reached,
     * and processing corresponding cycles if not processed already.
     */
    
    /*
     * This implementation provides cycles in their normalized form, but only
     * accidentally, due to normalizing work vertices cycles to make sure
     * a same cycle is only processed once, and SCCs computation treatment
     * providing vertices in their natural order, which is preserved when
     * creating work graph.
     * The spec indicates that cycles might not be in their normalized form,
     * to allow for eventual optimizations with that regard.
     */
    
    //--------------------------------------------------------------------------
    // PRIVATE CLASSES
    //--------------------------------------------------------------------------

    private static class MySccProcessor implements InterfaceVertexCollProcessor {
        private final int maxSize;
        private final InterfaceVertexCollProcessor processor;
        private final ArrayList<InterfaceVertex> tmpScc = new ArrayList<InterfaceVertex>();
        public MySccProcessor(
                int maxSize,
                InterfaceVertexCollProcessor processor) {
            this.maxSize = maxSize;
            this.processor = processor;
        }
        //@Override
        public void processCollBegin() {
            this.tmpScc.clear();
        }
        //@Override
        public void processCollVertex(InterfaceVertex vertex) {
            this.tmpScc.add(vertex);
        }
        //@Override
        public boolean processCollEnd() {
            return computeShortestCycles_onScc(
                    this.tmpScc,
                    this.maxSize,
                    this.processor);
        }
    }
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /**
     * Computes a set of cycles that cover all dependencies of each SCC, doing
     * best effort in making this set and these cycles as small as possible,
     * i.e. some processed cycles might only cover dependencies already covered
     * by other processed cycles.
     * Cycles of size 1 are also processed.
     * 
     * For a same input, and regardless of vertices hash codes,
     * this method always produces the same output.
     * 
     * Calls to processor.processCollVertex(...) are ordered according to the order
     * in which elements appear in the cycle.
     * For performances purpose, the first call is not necessarily done on the
     * lowest element, i.e. the specified cycle is not necessarily in normalized
     * form.
     * 
     * @param graph Graph of which shortest cycles must be computed.
     *        Must be consistent, i.e. not contain duplicates, and contain all
     *        vertices reachable from contained ones.
     * @param maxSize Max size of processed cycles. If < 0, no limit.
     * @param processor Processor to process the cycles with.
     */
    public static void computeShortestCycles(
            Collection<? extends InterfaceVertex> graph,
            int maxSize,
            InterfaceVertexCollProcessor processor) {
        
        ArgsUtils.requireNonNull(processor);
        
        // Implicit null check.
        if ((graph.size() == 0)
                || (maxSize == 0)) {
            return;
        }

        final MySccProcessor sccProcessor = new MySccProcessor(
                maxSize,
                processor);
        SccsComputer.computeSccs(graph, sccProcessor);
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------

    private RefShortestCyclesComputer() {
    }
    
    /*
     * 
     */

    /**
     * @param maxSize Must be != 0.
     * @return True if must stop, false otherwise.
     */
    private static boolean computeShortestCycles_onScc(
            ArrayList<InterfaceVertex> scc,
            int maxSize,
            InterfaceVertexCollProcessor processor) {
        
        if (maxSize == 0) {
            throw new AssertionError();
        }
        
        /*
         * Using a work graph, not so much to have predecessors (which could
         * easily be computed by iterating on successors of each vertex), than
         * not to have edges dangling outside the SCC, and to be able to remove
         * cycles of size 1, to make things simpler.
         */

        final ArrayList<WorkVertex> workScc =
                WorkGraphUtilz.newWorkGraphAsArrayList(
                        scc);
        
        // Modif/paper: taking care of elementary circuits, and removing them.
        for (WorkVertex v : workScc) {
            if (WorkGraphUtilz.removeEdge(v, v)) {
                if (processOneVertexCycle(v, processor)) {
                    return true;
                }
            }
        }
        
        if (maxSize == 1) {
            // No need to go further here.
            return false;
        }

        /*
         * name_in_paper/name_in_code:
         * 
         * parent: predecessor (parent/child more suited to tree-like graphs)
         * C: normCycleSet (set of cycles)
         * x: x (vertex)
         * V: visitedSet (set of vertices)
         * A: predToVisitSet (set of vertices)
         * Q: qList (FIFO of vertices) + qSet (for contains(...))
         * p: v (vertex)
         * y: succ (Vertex)
         * c: cycle
         * B: v.successors() (set of vertices)
         */
        
        final Set<List<WorkVertex>> normCycleSet = new HashSet<List<WorkVertex>>();
        
        for (WorkVertex x : workScc) {
            
            final Set<WorkVertex> visitedSet =
                    new HashSet<WorkVertex>();
            
            final Set<WorkVertex> predToVisitSet =
                new HashSet<WorkVertex>(
                        x.predecessors());

            setBfsPred(x, null);

            final LinkedList<WorkVertex> qList = new LinkedList<WorkVertex>();
            // For fast contains(...).
            final HashSet<WorkVertex> qSet = new HashSet<WorkVertex>();
            
            push(qList, qSet, x);
            
            while (predToVisitSet.size() != 0) {
                // Queue must not be empty, as long as we work on a SCC.
                final WorkVertex v = pop(qList, qSet);

                for (WorkVertex succ : v.successors()) {
                    
                    if ((!visitedSet.contains(succ))
                            && (!qSet.contains(succ))) {
                        setBfsPred(succ, v);
                        push(qList, qSet, succ);
                    }

                    if (predToVisitSet.remove(succ)) {
                        
                        /*
                         * Found a cycle, new or not.
                         */
                        
                        final ArrayList<WorkVertex> cycle = newReversedCycle(
                                workScc.size(),
                                succ);
                        
                        final boolean smallEnoughForProcess =
                                (maxSize < 0)
                                || (cycle.size() <= maxSize);
                        if (smallEnoughForProcess) {
                            
                            dereverse(cycle);
                            
                            normalize(cycle);
                            
                            if (normCycleSet.add(cycle)) {
                                if (processCycle(cycle, processor)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
                
                visitedSet.add(v);
            }
        }
        
        return false;
    }

    /*
     * 
     */
    
    /**
     * @return The reversed cycle.
     */
    private static ArrayList<WorkVertex> newReversedCycle(
            int sccSize,
            WorkVertex succ) {
        final ArrayList<WorkVertex> reversedCycle = new ArrayList<WorkVertex>();
        {
            WorkVertex tmpV = succ;
            // Building the cycle.
            while (tmpV != null) {
                if (reversedCycle.size() == sccSize) {
                    // If happens, means bug somewhere.
                    throw new AssertionError("infinite loop");
                }
                reversedCycle.add(tmpV);
                tmpV = getBfsPred(tmpV);
            }
        }
        return reversedCycle;
    }

    /**
     * @param cycle (in,out) In: reverse cycle. Out: de-reversed cycle.
     */
    private static void dereverse(ArrayList<WorkVertex> cycle) {
        
        final int nMinus1 = cycle.size() - 1;
        
        // (size-1)/2
        final int maxI = (nMinus1 >> 1);
        
        for (int i = 0; i <= maxI; i++) {
            final int j = nMinus1 - i;
            final WorkVertex v = cycle.get(i);
            cycle.set(i, cycle.get(j));
            cycle.set(j, v);
        }
    }

    /**
     * Normalizes (according to work vertices ordering).
     * 
     * @param cycle (in,out) In: cycle. Out: normalized cycle.
     */
    private static void normalize(ArrayList<WorkVertex> cycle) {
        
        final WorkVertex[] cycleAsArr =
                cycle.toArray(
                    new WorkVertex[cycle.size()]);
        
        CyclesUtils.normalizeCycle(cycleAsArr);

        cycle.clear();
        for (WorkVertex v : cycleAsArr) {
            cycle.add(v);
        }
    }
    
    /*
     * 
     */
    
    private static boolean processOneVertexCycle(
            WorkVertex vertex,
            InterfaceVertexCollProcessor processor) {
        processor.processCollBegin();
        processor.processCollVertex(vertex.backingVertex());
        return processor.processCollEnd();
    }
    
    private static boolean processCycle(
            ArrayList<WorkVertex> cycle,
            InterfaceVertexCollProcessor processor) {
        final int size = cycle.size();
        processor.processCollBegin();
        for (int i = 0; i < size; i++) {
            processor.processCollVertex(cycle.get(i).backingVertex());
        }
        return processor.processCollEnd();
    }

    /*
     * 
     */
    
    private static void setBfsPred(WorkVertex v, WorkVertex bfsPred) {
        v.setData(bfsPred);
    }
    
    private static WorkVertex getBfsPred(WorkVertex v) {
        return (WorkVertex) v.getData();
    }
    
    /*
     * FIFO
     */
    
    private static void push(
            LinkedList<WorkVertex> qList,
            HashSet<WorkVertex> qSet,
            WorkVertex vertex) {
        
        if (!qSet.add(vertex)) {
            throw new AssertionError();
        }
        
        qList.addLast(vertex);
    }
    
    private static WorkVertex pop(
            LinkedList<WorkVertex> qList,
            HashSet<WorkVertex> qSet) {
        final WorkVertex vertex = qList.removeFirst();
        
        if (!qSet.remove(vertex)) {
            throw new AssertionError();
        }
        
        return vertex;
    }
}
