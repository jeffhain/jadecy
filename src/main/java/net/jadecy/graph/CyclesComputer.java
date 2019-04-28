/*
 * Copyright 2015-2016 Jeff Hain
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
import java.util.Iterator;
import java.util.TreeSet;

import net.jadecy.utils.ArgsUtils;

/**
 * Computes cycles of a graph, including cycles of size 1.
 */
public class CyclesComputer {

    /*
     * Uses Donald B. Johnson's algorithm, with some pre-treatment to handle
     * cycles of size 1, and possibility to ignore cycles above a certain size,
     * which allows for faster execution.
     * 
     * n = vertices (nodes)
     * e = edges (arcs)
     * c = elementary circuits (cycles)
     * in time : O((n+e)*(c+1))
     * in space : O(n + e)
     */

    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------

    private static final boolean DEBUG = false;
    
    /**
     * To avoid StackOverflowError in case of long paths.
     * 
     * Keeping recursive code as dead code, for a cleaner formalization of the
     * algorithm.
     */
    private static final boolean USE_CONTINUATION = true;

    //--------------------------------------------------------------------------
    // PRIVATE CLASSES
    //--------------------------------------------------------------------------

    private static class MyVertexData {
        boolean blocked;
        final HashSet<WorkVertex> blockingSet = new HashSet<WorkVertex>();
        public MyVertexData() {
        }
        public void blockingClear() {
            // Could replace with a new set after clear, to make further clears
            // faster in case of huge set, since internal array can only grow,
            // but that slows down the common case where it's not too large so
            // we don't.
            this.blockingSet.clear();
        }
        public boolean blockingAdd(WorkVertex toAdd) {
            return this.blockingSet.add(toAdd);
        }
    }

    /**
     * Processor updating a sorted set of SCCs (ordered by comparing first vertex).
     */
    private static class MySccSortedSetUpdateProcessor implements InterfaceVertexCollProcessor {
        private final TreeSet<MyComparableScc> sccSortedSet;
        private final ArrayList<MyComparableScc> addedSccList = new ArrayList<MyComparableScc>();
        private MyComparableScc scc;
        public MySccSortedSetUpdateProcessor(TreeSet<MyComparableScc> sccSortedSet) {
            this.sccSortedSet = sccSortedSet;
        }
        public void clearAddedSccList() {
            this.addedSccList.clear();
        }
        @Override
        public void processCollBegin() {
            this.scc = new MyComparableScc();
        }
        @Override
        public void processCollVertex(InterfaceVertex vertex) {
            this.scc.add((WorkVertex) vertex);
        }
        @Override
        public boolean processCollEnd() {
            final MyComparableScc scc = this.scc;
            this.scc = null;
            
            final int size = scc.size();
            if (size > 1) {
                this.sccSortedSet.add(scc);
                this.addedSccList.add(scc);
            }
            return false;
        }
    }

    /**
     * Collection to hold a SCC, which is comparable to other such collections.
     * 
     * Since it extends TreeSet, and that SCCs don't overlap, an ordering can
     * be defined just based on the first vertex of each collection if both
     * are not empty, on their size otherwise.
     * 
     * SCCs are sorted by increasing first vertex order, which is required by
     * our algorithm.
     * 
     * Note: if vertices classes have a natural ordering that is inconsistent
     * with equals, this class also does.
     */
    private static class MyComparableScc extends TreeSet<WorkVertex> implements Comparable<MyComparableScc> {
        private static final long serialVersionUID = 1L;
        public MyComparableScc() {
        }
        @Override
        public int compareTo(MyComparableScc other) {
            final int size1 = this.size();
            final int size2 = other.size();
            // No overflow since both >= 0.
            final int cmp = size1 - size2;
            if (cmp != 0) {
                return cmp;
            }
            if (size1 == 0) {
                return 0;
            } else {
                final InterfaceVertex aFirst = this.first();
                final InterfaceVertex bFirst = other.first();
                return aFirst.compareTo(bFirst);
            }
        }
    }
    
    /*
     * 
     */

    /**
     * Continuation to avoid CIRCUIT recursion.
     */
    private static class MyCircuitContinuation {
        final WorkVertex v;
        Iterator<WorkVertex> vSuccIt;
        public MyCircuitContinuation(WorkVertex v) {
            this.v = v;
        }
        /**
         * Must be called before vSuccIt().
         */
        public boolean firstRun() {
            return this.vSuccIt == null;
        }
        public Iterator<WorkVertex> vSuccIt() {
            Iterator<WorkVertex> vSuccIt = this.vSuccIt;
            if (vSuccIt == null) {
                vSuccIt = v.successors().iterator();
                this.vSuccIt = vSuccIt;
            }
            return vSuccIt;
        }
    }
    
    /**
     * Continuation to avoid UNBLOCK recursion.
     */
    private static class MyUnblockContinuation {
        final WorkVertex v;
        Iterator<WorkVertex> vBlockingSetIt;
        public MyUnblockContinuation(WorkVertex v) {
            this.v = v;
        }
        /**
         * Must be called before vBlockingSetIt().
         */
        public boolean firstRun() {
            return this.vBlockingSetIt == null;
        }
        public Iterator<WorkVertex> vBlockingSetIt() {
            Iterator<WorkVertex> vBlockingSetIt = this.vBlockingSetIt;
            if (vBlockingSetIt == null) {
                final MyVertexData vd = (MyVertexData) v.getData();
                vBlockingSetIt = vd.blockingSet.iterator();
                this.vBlockingSetIt = vBlockingSetIt;
            }
            return vBlockingSetIt;
        }
    }
    
    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------

    private static final int CIRCUIT_STOP = -1;
    private static final int CIRCUIT_NOT_FOUND = 0;
    private static final int CIRCUIT_FOUND = 1;

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /**
     * Computes cycles of the specified graph, including single vertex cycles.
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
     * @param graph Graph of which cycles must be computed.
     *        Must be consistent, i.e. not contain duplicates, and contain all
     *        vertices reachable from contained ones.
     * @param maxSize Max size of processed cycles. If < 0, no limit.
     * @param processor Processor to process the cycles with.
     * @throws NullPointerException if any argument is null.
     */
    public static void computeCycles(
            Collection<? extends InterfaceVertex> graph,
            int maxSize,
            InterfaceVertexCollProcessor processor) {

        ArgsUtils.requireNonNull(processor);
        
        // Implicit null check.
        if ((graph.size() == 0)
                || (maxSize == 0)) {
            return;
        }

        /*
         * We create a work copy of the input graph, so that we can modify that
         * copy to make it lighter as we go, especially splitting it into
         * multiple SCCs.
         * 
         * We keep track of SCCs in a sorted set, sorting them by comparing
         * their first vertex (as required by the algorithm).
         * 
         * When removing 's' from its SCC, we compute new SCCs only from the
         * resulting sub-graph.
         */

        final boolean mustIgnoreDeadEnds = true;
        final TreeSet<WorkVertex> workGraph = WorkGraphUtilz.newWorkGraphAsTreeSet(
                graph,
                mustIgnoreDeadEnds);
        if (workGraph.size() == 0) {
            // Can happen in case of dead ends removal.
            return;
        }
        
        // Taking care of (v,v) elementary circuits, which are not handled
        // by Johnson's algorithms, and removing them.
        for (WorkVertex v : workGraph) {
            if (WorkGraphUtilz.removeEdge(v, v)) {
                if (processOneVertexCycle(v, processor)) {
                    return;
                }
            }
        }
        
        if (maxSize == 1) {
            // No need to go further here.
            return;
        }

        for (WorkVertex v : workGraph) {
            v.setData(new MyVertexData());
        }

        final ArrayList<WorkVertex> stack = new ArrayList<WorkVertex>();
        WorkVertex s = workGraph.first();

        /*
         * Computing the initial sorted set of SCCs.
         */

        final TreeSet<MyComparableScc> sccSortedSet = new TreeSet<MyComparableScc>();
        final MySccSortedSetUpdateProcessor sccSortedSetUpdateProcessor = new MySccSortedSetUpdateProcessor(sccSortedSet);
        SccsComputer.computeSccs(
                workGraph,
                sccSortedSetUpdateProcessor);
        for (MyComparableScc scc : sccSortedSet) {
            // Isolating SCCs from each other.
            WorkGraphUtilz.detachVerticesNotInGraph(scc);
        }
        
        /*
         * 
         */
        
        final ArrayList<MyCircuitContinuation> circuitContinuations = (USE_CONTINUATION ? new ArrayList<MyCircuitContinuation>() : null);
        final ArrayList<MyUnblockContinuation> unblockContinuations = (USE_CONTINUATION ? new ArrayList<MyUnblockContinuation>() : null);

        while (sccSortedSet.size() != 0) {
            
            /*
             * Here, the paper says:
             * "Ak = adjacency structure of strong component K
             * with least vertex in subgraph of G induced by
             * {s,s+1,...,n}"
             * 
             * We understand this as:
             * "Ak is the subgraph corresponding to the strongly connected
             * component, if any, with the least vertex, among the strongly
             * connected components contained in the subgraph formed by
             * {s,s+1,...,n} vertices and the edges that link them."
             */

            final MyComparableScc scc = sccSortedSet.first();
            s = scc.first();
            for (WorkVertex w : scc) {
                final MyVertexData wd = (MyVertexData) w.getData();
                wd.blocked = false;
                wd.blockingClear();
            }

            final int ret;
            if (USE_CONTINUATION) {
                ret = CIRCUIT_continuation(
                        stack,
                        s,
                        s,
                        maxSize,
                        processor,
                        //
                        circuitContinuations,
                        unblockContinuations);
            } else {
                ret = CIRCUIT_recursive(
                        (DEBUG ? "" : null),
                        stack,
                        s,
                        s,
                        maxSize,
                        processor);
            }
            if (ret == CIRCUIT_STOP) {
                return;
            }

            /*
             * Removing the vertex from its SCC might break it apart,
             * so we need to recompute SCCs.
             */
            
            if (DEBUG) {
                System.out.println("removing " + s + " from " + scc);
                System.out.println("old sccSortedSet = " + sccSortedSet);
            }
            
            removeVertexFromSccAndUpdateSccSortedSet(
                    (MyComparableScc) scc,
                    s,
                    sccSortedSet,
                    //
                    sccSortedSetUpdateProcessor);
            
            if (DEBUG) {
                System.out.println("new sccSortedSet = " + sccSortedSet);
            }
        }
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------

    private CyclesComputer() {
    }

    /*
     * 
     */

    /**
     * @return CIRCUIT_STOP, CIRCUIT_NOT_FOUND or CIRCUIT_FOUND.
     */
    private static int CIRCUIT_continuation(
            ArrayList<WorkVertex> stack,
            final WorkVertex s,
            WorkVertex initialV,
            int maxSize,
            InterfaceVertexCollProcessor processor,
            //
            ArrayList<MyCircuitContinuation> circuitContinuations,
            ArrayList<MyUnblockContinuation> unblockContinuations) {
        
        // 'f' in paper (for "found"), but we use it for stop as well.
        int ret = CIRCUIT_NOT_FOUND;
        
        circuitContinuations.clear();
        circuitContinuations.add(new MyCircuitContinuation(initialV));
        
        LOOP_1 : while (circuitContinuations.size() != 0) {
            final MyCircuitContinuation continuation = circuitContinuations.remove(circuitContinuations.size()-1);
            final WorkVertex v = continuation.v;
            final MyVertexData vd = (MyVertexData) v.getData();
            
            if (continuation.firstRun()) {
                if ((maxSize >= 0) && (stack.size() == maxSize)) {
                    ret = CIRCUIT_FOUND;
                    continue;
                }
                stack.add(v);
                vd.blocked = true;
            }
            
            final Iterator<WorkVertex> vSuccIt = continuation.vSuccIt();
            while (vSuccIt.hasNext()) {
                final WorkVertex w = vSuccIt.next();
                if (w == s) {
                    if (processCycle(
                            stack,
                            processor)) {
                        return CIRCUIT_STOP;
                    }
                    ret = CIRCUIT_FOUND;
                } else {
                    final MyVertexData wd = (MyVertexData) w.getData();
                    if (!wd.blocked) {
                        // Paused.
                        circuitContinuations.add(continuation);
                        // Started.
                        circuitContinuations.add(new MyCircuitContinuation(w));
                        continue LOOP_1;
                    }
                }
            }
            if (ret == CIRCUIT_FOUND) {
                UNBLOCK_continuation(v, unblockContinuations);
            } else {
                for (WorkVertex w : v.successors()) {
                    final MyVertexData wd = (MyVertexData) w.getData();
                    wd.blockingAdd(v);
                }
            }
            // "unstack v".
            final WorkVertex forCheck = stack.remove(stack.size()-1);
            if (forCheck != v) {
                throw new AssertionError();
            }
        }
        
        return ret;
    }

    /**
     * This method is recursive.
     * 
     * @return CIRCUIT_STOP, CIRCUIT_NOT_FOUND or CIRCUIT_FOUND.
     */
    private static int CIRCUIT_recursive(
            String prefix,
            ArrayList<WorkVertex> stack,
            final WorkVertex s,
            final WorkVertex v,
            int maxSize,
            InterfaceVertexCollProcessor processor) {
        // 'f' in paper (for "found"), but we use it for stop as well.
        int ret = CIRCUIT_NOT_FOUND;
        if (DEBUG) {
            System.out.println();
            System.out.println(prefix + "CIRCUIT_recursive(,,s = " + s + ", v = " + v + ",,)");
        }
        
        // Modif/paper: max size handling.
        if ((maxSize >= 0) && (stack.size() == maxSize)) {
            // Further cycle would be too large.
            if (DEBUG) {
                System.out.println(prefix + "MAX SIZE already reached: not going further and pretending we found a circuit");
            }
            
            // Need to pretend that else callers won't unblock as needed.
            ret = CIRCUIT_FOUND;
            if (DEBUG) {
                System.out.println(prefix + "return CIRCUIT_recursive(...) (0) : " + ret);
            }
            return ret;
        }
        
        stack.add(v);
        if (DEBUG) {
            System.out.println(prefix + "push(" + v + ") : " + stack);
        }
        final MyVertexData vd = (MyVertexData) v.getData();
        if (DEBUG) {
            System.out.println(prefix + v + ".blocked : " + vd.blocked + " -> true");
        }
        vd.blocked = true;
        for (WorkVertex w : v.successors()) {
            if (DEBUG) {
                System.out.println();
                System.out.println(prefix + v + " succ = " + w);
            }
            if (w == s) {
                if (DEBUG) {
                    System.out.println(prefix + "=================> CYCLE: " + stack);
                }
                if (processCycle(
                        stack,
                        processor)) {
                    ret = CIRCUIT_STOP;
                    if (DEBUG) {
                        System.out.println(prefix + "return CIRCUIT_recursive(...) (1) : " + ret);
                    }
                    return ret;
                }
                ret = CIRCUIT_FOUND;
            } else {
                final MyVertexData wd = (MyVertexData) w.getData();
                if (DEBUG) {
                    System.out.println(prefix + w + ".blocked == " + wd.blocked);
                }
                if (!wd.blocked) {
                    final int tmpRet = CIRCUIT_recursive(
                            (DEBUG ? prefix + "  " : null),
                            stack,
                            s,
                            w,
                            maxSize,
                            processor);
                    if (tmpRet == CIRCUIT_FOUND) {
                        ret = CIRCUIT_FOUND;
                    } else if (tmpRet == CIRCUIT_STOP) {
                        ret = CIRCUIT_STOP;
                        if (DEBUG) {
                            System.out.println(prefix + "return CIRCUIT_recursive(...) (2) : " + ret);
                        }
                        return ret;
                    }
                }
            }
        }
        if (ret == CIRCUIT_FOUND) {
            /*
             * Unblocking v and calling UNBLOCK_recursive again
             * on vertices in its blocking set.
             */
            if (DEBUG) {
                System.out.println(prefix + "FOUND a circuit starting with " + stack);
            }
            UNBLOCK_recursive(
                    (DEBUG ? prefix : null),
                    v);
        } else {
            /*
             * Ensuring successors of v are in its blocking set
             * (so that when v is unblocked, they are too).
             */
            if (DEBUG) {
                System.out.println(prefix + "DID NOT find a circuit starting with " + stack);
            }
            for (WorkVertex w : v.successors()) {
                final MyVertexData wd = (MyVertexData) w.getData();
                final boolean didAdd = wd.blockingAdd(v);
                if (DEBUG) {
                    if (didAdd) {
                        System.out.println(prefix + "added " + v + " to blocking set (blocked = " + ((MyVertexData) v.getData()).blocked + ")");
                    }
                }
            }
        }
        // "unstack v".
        final WorkVertex forCheck = stack.remove(stack.size()-1);
        if (forCheck != v) {
            throw new AssertionError();
        }
        if (DEBUG) {
            System.out.println(prefix + "pop(" + v + ") : " + stack);
            System.out.println(prefix + "return CIRCUIT_recursive(...) (3) : " + ret);
        }
        return ret;
    }

    /*
     * 
     */
    
    private static void UNBLOCK_continuation(
            WorkVertex initialV,
            //
            ArrayList<MyUnblockContinuation> continuations) {
        
        continuations.clear();
        continuations.add(new MyUnblockContinuation(initialV));
        
        LOOP_1 : while (continuations.size() != 0) {
            final MyUnblockContinuation continuation = continuations.remove(continuations.size()-1);
            final WorkVertex v = continuation.v;
            final MyVertexData vd = (MyVertexData) v.getData();
            
            if (continuation.firstRun()) {
                vd.blocked = false;
            }
            
            final Iterator<WorkVertex> vBlockingSetIt = continuation.vBlockingSetIt();
            while (vBlockingSetIt.hasNext()) {
                final WorkVertex w = vBlockingSetIt.next();
                final MyVertexData wd = (MyVertexData) w.getData();
                if (wd.blocked) {
                    // Paused.
                    continuations.add(continuation);
                    // Started.
                    continuations.add(new MyUnblockContinuation(w));
                    continue LOOP_1;
                }
            }
            // Can clear after the loop, since the set is not used until then.
            vd.blockingClear();
        }
    }
    
    /**
     * This method is recursive.
     */
    private static void UNBLOCK_recursive(
            String prefix,
            WorkVertex v) {
        final MyVertexData vd = (MyVertexData) v.getData();
        if (DEBUG) {
            System.out.println(prefix + v + ".blocked : " + vd.blocked + " -> false");
        }
        vd.blocked = false;
        for (WorkVertex w : vd.blockingSet) {
            final MyVertexData wd = (MyVertexData) w.getData();
            if (wd.blocked) {
                UNBLOCK_recursive(prefix, w);
            }
        }
        if (DEBUG) {
            System.out.println(prefix + v + ".blockingClear()");
        }
        // Can clear after the loop, since the set is not used until then.
        vd.blockingClear();
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
            ArrayList<WorkVertex> stack,
            InterfaceVertexCollProcessor processor) {
        final int size = stack.size();
        processor.processCollBegin();
        for (int i = 0; i < size; i++) {
            processor.processCollVertex(stack.get(i).backingVertex());
        }
        return processor.processCollEnd();
    }
    
    /*
     * 
     */
    
    /**
     * The specified SCC is removed and never put back into the specified set
     * of SCC.
     * 
     * @param scc SCC to remove, and from which the specified vertex must be
     *        removed.
     *        Must be contained in the specified set of SCCs.
     *        Must contain at least two vertices.
     * @param toRemove Vertex to remove from its SCC.
     * @param sccSortedSet Set of SCCs to update.
     */
    private static void removeVertexFromSccAndUpdateSccSortedSet(
            MyComparableScc scc,
            WorkVertex toRemove,
            TreeSet<MyComparableScc> sccSortedSet,
            //
            MySccSortedSetUpdateProcessor sccOrderedSetUpdateProcessor) {
        if (scc.size() < 2) {
            throw new AssertionError();
        }

        /*
         * Removal of the SCC from SCCs set needs to be done before removal of
         * the vertex from its SCC, which could change the theoretical place of
         * the SCC in SCCs set.
         */

        {
            final boolean forCheck = sccSortedSet.remove(scc);
            if (!forCheck) {
                throw new AssertionError();
            }
        }

        /*
         * Removing the vertex from its SCC.
         */
        
        {
            final boolean forCheck = WorkGraphUtilz.removeFromGraph(
                    scc,
                    toRemove);
            if (!forCheck) {
                throw new AssertionError();
            }
        }
        if (scc.size() == 1) {
            /*
             * Only had two vertices in the SCC.
             * We don't care about singleton SCCs here, since Johson's algorithm
             * doesn't compute elementary circuits (which we do just before),
             * so we just return without adding it back into the SCCs set.
             */
            return;
        }
        
        /*
         * Computing SCCs from the graph resulting from removing the vertex from
         * its SCC.
         */

        sccOrderedSetUpdateProcessor.clearAddedSccList();
        SccsComputer.computeSccs(
                scc,
                sccOrderedSetUpdateProcessor);

        /*
         * Only need to detach (from each other) newly added SCCs.
         */
        
        for (MyComparableScc addedScc : sccOrderedSetUpdateProcessor.addedSccList) {
            // Isolating SCCs from each other.
            WorkGraphUtilz.detachVerticesNotInGraph(addedScc);
        }
    }
}
