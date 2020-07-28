/*
 * Copyright 2016-2020 Jeff Hain
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.jadecy.utils.ArgsUtils;
import net.jadecy.utils.SortUtils;

/**
 * Implements the cycle detection algorithm (not the ranking part) described in the paper
 * "Efficient Retrieval and Ranking of Undesired Package Cycles in Large Software Systems",
 * by Jannik Laval, Jean-Remy Falleri, Philippe Vismara, and Stephane Ducasse,
 * published in "Journal of Object Technology":
 * http://www.jot.fm/issues/issue_2012_04/article4.pdf
 * modulo:
 * - optimizations that can greatly speed things up, for example in case of a
 *   single but long cycle, and usually reduce the amount of cycles found for
 *   covering SCCs edges.
 * - handling of cycles of size 1,
 * - possibility to ignore cycles above a certain size.
 */
public class ShortestCyclesComputer {
    
    /*
     * The basic idea of paper's algorithm: "the decomposition of a SCC in
     * multiple short cycles covering all dependencies of the SCC",
     * by iterating on vertices, and for each of them, pushing a
     * Breadth-First Search (BFS) until all predecessors have been reached,
     * and processing corresponding cycles if not processed already.
     * 
     * 
     * Main modifications/paper:
     * 
     * - Makes things a bit faster and simpler, by replacing one or two sets
     *   by a list, and collection belonging tests by a null check:
     *   Replaced testing whether the vertex ("y" in the paper) is in the queue
     *   or visited set, by testing whether its BFS predecessor has been set or
     *   whether it is "x" (using a special BFS predecessor for that), with
     *   cleanup of BFS predecessors at start of main loop.
     *   These tests are equivalent in practice (has been tested using both
     *   on random graphs).
     *   As a result, we don't need a visited set, nor an efficient
     *   queue.contains(...) method, and can just use a list for the queue.
     * 
     * - Can make things much faster (lower practical algorithmic complexity),
     *   for example in case of a long cycle, at the cost (positive or negative)
     *   of eventually causing some cycles to be screened out by multiple larger
     *   cycles already covering all their edges (depending on the order in
     *   which vertices are iterated on):
     *   For each vertex of main loop, not pushing the BFS until reaching all
     *   predecessors, but only until reaching predecessors corresponding to
     *   edges not already covered by processed cycles.
     *   As a result, we never terminate a (non-normalized) cycle by an already
     *   used edge (but can still include an already used edge in a cycle, as
     *   long as it's not the last), which means that we never compute a same
     *   cycle twice (else our terminal edge would be already used), and can get
     *   rid of the unicity check, and corresponding set and cycle
     *   normalization.
     * 
     * - The risk, introduced by the above optimization, of some cycles being
     *   screened out by larger ones should not hurt much in practice, largest
     *   "short" cycles being usually not that large or not in large numbers.
     *   To reduce that risk, for main loop we could iterate on vertices by
     *   decreasing numbers of predecessors and successors, so as to process
     *   edge-dense areas first.
     *   Though, we don't want to decide that it's more a risk than a feature,
     *   since it reduces the overall amount of cycles found, making it easier
     *   to deal with them as a whole.
     *   On the contrary, the original algorithm itself doesn't necessarily
     *   compute all shortest cycles, and we prefer to push further the
     *   reduction of the overall amount of cycles found by sorting vertices
     *   by increasing numbers of successors.
     *   Edge density is better measured if also taking into account the numbers
     *   of predecessors, but then it seems to cause more cycles to be computed
     *   in practice, maybe because elements with a lot of predecessors tend to
     *   be located in low level layers and, despite their numbers of incoming
     *   edges, are less cycle-prone than huge domain related tangled messes
     *   developers usually work in.
     * 
     * 
     * Further improvements ideas:
     * 
     * - It might be possible to reuse much of the BFS search done from a
     *   vertex, when processing one of its successors next, or some other
     *   close vertex.
     * 
     * - If the amount of computed cycles is never too large, nor takes too
     *   much time to compute, could compute them all at first, and then,
     *   before processing them, removing largest redundant ones.
     */
    
    /*
     * Results comparison depending on whether using paper or optimized
     * algorithm, and for that one depending on whether and how vertices
     * are sorted for main loop.
     * 
     * Number of classes cycles found in rt.jar, for classes starting with "java.":
     * (using JadecyMain with args: [C:/Program Files (x86)/Java/jdk1.5.0_22/jre/lib/rt.jar, -regex, java\..*, -scycles, -onlystats])
     * +-------------+-------+-------------+------------+-------------+-------------+
     * |\ algorithm  | paper | pred + succ | no sorting | pred + succ | succ        |
     * | ----------- |       | decreasing  |            | increasing  | increasing  |
     * |cycle size  \|       |             |            |             |             |
     * +-------------+-------+-------------+------------+-------------+-------------+
     * |           2 |   417 |         407 |        382 |         329 |         324 |
     * |           3 |  1001 |         946 |        900 |         851 |         817 |
     * |           4 |  1632 |        1611 |       1602 |        1519 |        1518 |
     * |           5 |  1331 |        1328 |       1322 |        1305 |        1302 |
     * |           6 |   779 |         779 |        778 |         774 |         771 |
     * |           7 |   402 |         402 |        401 |  (yep!) 402 |         400 |
     * |           8 |   175 |         175 |        175 |         175 |         171 |
     * |           9 |    54 |          54 |         54 |          54 |          54 |
     * |          10 |    43 |          43 |         43 |          43 |          43 |
     * |          11 |    18 |          18 |         18 |          18 |          18 |
     * |          12 |     9 |           9 |          9 |           9 |           9 |
     * +-------------+-------+-------------+------------+-------------+-------------+
     * |         all |  5861 |        5772 |       5684 |        5479 |        5427 |
     * +-------------+-------+-------------+------------+-------------+-------------+
     * 
     * Computation time (3 GHz): about 0.3s for paper algorithm,
     * and 0.2s for optimized algorithms.
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
        @Override
        public void processCollBegin() {
            this.tmpScc.clear();
        }
        @Override
        public void processCollVertex(InterfaceVertex vertex) {
            this.tmpScc.add(vertex);
        }
        @Override
        public boolean processCollEnd() {
            return computeShortestCycles_onScc(
                    this.tmpScc,
                    this.maxSize,
                    this.processor);
        }
    }
    
    /**
     * To sort work vertices by increasing number of successors,
     * to help computing fewer cycles.
     */
    private static class MyWorkVertexComparator implements Comparator<WorkVertex> {
        @Override
        public int compare(WorkVertex v1, WorkVertex v2) {
            final int n1 = v1.successors().size();
            final int n2 = v2.successors().size();
            // No overflow since both >= 0.
            return n1 - n2;
        }
    }

    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------
    
    /**
     * Tombstone to identify the source vertex of the BFS, to make conditional
     * code simpler.
     */
    private static final WorkVertex TOMBSTONE = new WorkVertex(null, 0);
    
    /**
     * To help computing fewer cycles.
     */
    static final boolean MUST_SORT_VERTICES = true;
    
    private static final MyWorkVertexComparator WORK_VERTEX_COMPARATOR =
            new MyWorkVertexComparator();

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

    private ShortestCyclesComputer() {
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
         * Dealing with SCCs of size 1 (and with cycles of size 1 along with that),
         * early, before creating a work graph, because it's the common case for
         * graphs with no or few cycles.
         */
        
        for (InterfaceVertex v : scc) {
            // Might have vertices outside the SCC as successors,
            // so need to use contains(...).
            if (v.successors().contains(v)) {
                // Has itself as successor.
                if (processOneBackingVertexCycle(v, processor)) {
                    return true;
                }
            }
        }
        
        if ((scc.size() == 1)
                || (maxSize == 1)) {
            // No need to go further here.
            return false;
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
        
        // Removing elementary circuits (already processed if any).
        for (WorkVertex v : workScc) {
            WorkGraphUtilz.removeEdge(v, v);
        }

        /*
         * 
         */
        
        final ArrayList<WorkVertex> bfsPredCleanupList = new ArrayList<WorkVertex>();
        
        // Sets initially never empty, since we are in an SCC of size > 1,
        // and mapping is removed when set gets empty, to help memory.
        final Map<WorkVertex,Set<WorkVertex>> predToVisitSetByVertex =
                new HashMap<WorkVertex,Set<WorkVertex>>();
        for (WorkVertex v : workScc) {
            predToVisitSetByVertex.put(v, new HashSet<WorkVertex>(v.predecessors()));
        }
        
        if (MUST_SORT_VERTICES) {
            SortUtils.sort(workScc, WORK_VERTEX_COMPARATOR);
        }

        /*
         * name_in_paper/name_in_code:
         * 
         * parent: predecessor (parent/child more suited to tree-like graphs)
         * x: x (vertex)
         * A: predToVisitSet (set of vertices)
         * Q: queue (FIFO of vertices)
         * p: v (vertex)
         * y: succ (Vertex)
         * c: cycle
         * B: v.successors() (set of vertices)
         */

        for (WorkVertex x : workScc) {
            
            final Set<WorkVertex> predToVisitSet = predToVisitSetByVertex.get(x);
            if (predToVisitSet == null) {
                // Corresponding cycles already taken care of.
                continue;
            }
            
            resetAndClear(bfsPredCleanupList);

            setBfsPredWithCleanup(x, TOMBSTONE, bfsPredCleanupList);

            final LinkedList<WorkVertex> queue = new LinkedList<WorkVertex>();
            
            push(queue, x);
            
            while (predToVisitSet.size() != 0) {
                // Queue must not be empty, as long as we work on a SCC.
                final WorkVertex v = pop(queue);
                
                for (WorkVertex succ : v.successors()) {
                    
                    /*
                     * Modif/paper: simpler conditional code.
                     */
                    
                    if (getBfsPred(succ) == null) {
                        // Not yet "visited" nor enqueued.
                        
                        setBfsPredWithCleanup(succ, v, bfsPredCleanupList);
                        
                        push(queue, succ);
                    }

                    if (removeAndCleanupIfEmpty(
                            predToVisitSet,
                            succ,
                            //
                            predToVisitSetByVertex,
                            x)) {
                        
                        /*
                         * Found a new cycle.
                         */
                        
                        final ArrayList<WorkVertex> cycle = newReversedCycle(
                                workScc.size(),
                                succ);
                        
                        final boolean smallEnoughForProcess =
                                (maxSize < 0)
                                || (cycle.size() <= maxSize);
                        if (smallEnoughForProcess) {
                            
                            dereverse(cycle);

                            if (processCycle(cycle, processor)) {
                                return true;
                            }

                            /*
                             * Modif/paper: removal of already covered edges.
                             */
                            
                            removeCycleEdges(predToVisitSetByVertex, cycle);
                        }
                    }
                }
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
            WorkVertex last) {
        final ArrayList<WorkVertex> reversedCycle = new ArrayList<WorkVertex>();
        {
            WorkVertex tmpV = last;
            // Building the cycle.
            while (tmpV != TOMBSTONE) {
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
    
    /*
     * 
     */
    
    /**
     * Useful not to forget to remove sets that get empty.
     * 
     * @param key Key for the specified set.
     * @return True if did remove, false otherwise.
     */
    private static boolean removeAndCleanupIfEmpty(
            Set<WorkVertex> predToVisitSet,
            WorkVertex pred,
            //
            Map<WorkVertex,Set<WorkVertex>> predToVisitSetByVertex,
            WorkVertex key) {
        final boolean didRemove = predToVisitSet.remove(pred);
        if (didRemove) {
            if (predToVisitSet.size() == 0) {
                final Object forCheck = predToVisitSetByVertex.remove(key);
                if (forCheck != predToVisitSet) {
                    // Bad key.
                    throw new AssertionError();
                }
            }
        }
        return didRemove;
    }

    /**
     * To make sure we won't try to compute cycles for edges already covered
     * by the specified cycle.
     * 
     * @param predToVisitSetByVertex (in,out)
     * @param cycle (in) Cycle.
     */
    private static void removeCycleEdges(
            Map<WorkVertex,Set<WorkVertex>> predToVisitSetByVertex,
            ArrayList<WorkVertex> cycle) {
        
        final int cycleSize = cycle.size();
        for (int i = 0; i < cycleSize; i++) {
            final WorkVertex v = cycle.get(i);
            final Set<WorkVertex> predToVisitSet = predToVisitSetByVertex.get(v);
            if (predToVisitSet != null) {
                final int predIndex = ((i == 0) ? cycleSize - 1 : i - 1);
                final WorkVertex pred = cycle.get(predIndex);
                removeAndCleanupIfEmpty(
                        predToVisitSet,
                        pred,
                        //
                        predToVisitSetByVertex,
                        v);
            }
        }
    }
    
    /*
     * 
     */
    
    private static boolean processOneBackingVertexCycle(
            InterfaceVertex vertex,
            InterfaceVertexCollProcessor processor) {
        processor.processCollBegin();
        processor.processCollVertex(vertex);
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
    
    private static void resetAndClear(List<WorkVertex> bfsPredCleanupList) {
        for (WorkVertex v : bfsPredCleanupList) {
            setBfsPred(v, null);
        }
        bfsPredCleanupList.clear();
    }
    
    private static void setBfsPredWithCleanup(
            WorkVertex v,
            WorkVertex bfsPred,
            List<WorkVertex> bfsPredCleanupList) {
        setBfsPred(v, bfsPred);
        bfsPredCleanupList.add(v);
    }
    
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
            LinkedList<WorkVertex> queue,
            WorkVertex vertex) {
        queue.addLast(vertex);
    }
    
    private static WorkVertex pop(LinkedList<WorkVertex> queue) {
        return queue.removeFirst();
    }
}
