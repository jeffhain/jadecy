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

import net.jadecy.utils.ArgsUtils;
import net.jadecy.utils.SortUtils;

/**
 * Computes the strongly connected components of a graph.
 */
public class SccsComputer {

    /*
     * Uses Robert Tarjan's algorithm (plus sorting).
     */

    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------

    /**
     * To avoid StackOverflowError in case of large SCCs.
     * 
     * Keeping recursive code as dead code, for a cleaner formalization of the
     * algorithm.
     */
    private static final boolean USE_CONTINUATION = true;

    /**
     * Useful in case of large SCCs.
     */
    private static final boolean USE_STACK_SET = true;

    //--------------------------------------------------------------------------
    // PRIVATE CLASSES
    //--------------------------------------------------------------------------

    private static class MyVertexData {
        final InterfaceVertex vertex;
        int index = -1;
        int lowlink = -1;
        public MyVertexData(InterfaceVertex vertex) {
            this.vertex = vertex;
        }
    }

    private static class MyData {
        final HashMap<InterfaceVertex, MyVertexData> dataByVertex = new HashMap<InterfaceVertex, MyVertexData>();
        int index = 0;
        final ArrayList<InterfaceVertex> stackList = new ArrayList<InterfaceVertex>();
        final HashSet<InterfaceVertex> stackSet = (USE_STACK_SET ? new HashSet<InterfaceVertex>() : null);
        public MyData() {
        }
        /**
         * @return The data corresponding to the specified vertex, creating it
         *         if it does not exist.
         */
        public MyVertexData get(InterfaceVertex vertex) {
            MyVertexData vd = this.dataByVertex.get(vertex);
            if (vd == null) {
                vd = new MyVertexData(vertex);
                this.dataByVertex.put(vertex, vd);
            }
            return vd;
        }
        public void stackPush(InterfaceVertex v) {
            this.stackList.add(v);
            if (USE_STACK_SET) {
                this.stackSet.add(v);
            }
        }
        public InterfaceVertex stackPop() {
            final InterfaceVertex v = this.stackList.remove(this.stackList.size()-1);
            if (USE_STACK_SET) {
                this.stackSet.remove(v);
            }
            return v;
        }
        public boolean stackContains(InterfaceVertex v) {
            if (USE_STACK_SET) {
                return this.stackSet.contains(v);
            } else {
                return this.stackList.contains(v);
            }
        }
    }

    private static class MyContinuation {
        final MyVertexData vd;
        final Iterator<? extends InterfaceVertex> vSuccIt;
        final MyVertexData wd;
        public MyContinuation(MyVertexData vd) {
            this.vd = vd;
            this.vSuccIt = vd.vertex.successors().iterator();
            this.wd = null;
        }
        public MyContinuation(
                MyVertexData vd,
                Iterator<? extends InterfaceVertex> vSuccIt,
                MyVertexData wd) {
            this.vd = vd;
            this.vSuccIt = vSuccIt;
            this.wd = wd;
        }
        public boolean firstRun() {
            return this.wd == null;
        }
    }

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /**
     * Computes strongly connected components of the specified graph.
     * Note that for this method, a SCC can contain a single vertex, even if
     * this vertex doesn't have itself as successor, for it's easier to ignore
     * these if useless than to compute them aside if useful.
     * 
     * For a same input, and regardless of vertices hash codes,
     * this method always produces the same output.
     * 
     * Calls to processor.processCollVertex(...) are ordered according to vertices
     * natural ordering, i.e. the specified SCC is in normalized form.
     * 
     * @param graph Graph of which SCCs must be computed.
     *        Must be consistent, i.e. not contain duplicates, and contain all
     *        vertices reachable from contained ones.
     * @param processor Processor to process the SCCs with.
     * @throws NullPointerException if graph or processor is null.
     */
    public static void computeSccs(
            Collection<? extends InterfaceVertex> graph,
            InterfaceVertexCollProcessor processor) {
        
        ArgsUtils.requireNonNull(processor);
        
        // Implicit null check.
        if (graph.size() == 0) {
            return;
        }

        final MyData data = new MyData();

        final ArrayList<MyContinuation> continuations = (USE_CONTINUATION ? new ArrayList<MyContinuation>() : null);

        for (InterfaceVertex v : graph) {
            final MyVertexData vd = data.get(v);
            if (vd.index < 0) {
                if (USE_CONTINUATION) {
                    if (strongConnect_continuation(
                            data,
                            vd,
                            processor,
                            //
                            continuations)) {
                        return;
                    }
                } else {
                    if (strongConnect_recursion(
                            data,
                            vd,
                            processor)) {
                        return;
                    }
                }
            }
        }
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------

    private SccsComputer() {
    }

    /**
     * @param continuations Instance to use for storing continuations.
     * @return True if must stop computation, false otherwise.
     */
    private static boolean strongConnect_continuation(
            MyData data,
            MyVertexData vd,
            InterfaceVertexCollProcessor processor,
            //
            ArrayList<MyContinuation> continuations) {
        continuations.clear();
        continuations.add(new MyContinuation(vd));

        LOOP_1 : while (continuations.size() != 0) {
            final MyContinuation continuation = continuations.remove(continuations.size()-1);
            vd = continuation.vd;

            if (continuation.firstRun()) {
                vd.index = data.index;
                vd.lowlink = data.index;
                data.index++;

                data.stackPush(vd.vertex);
            } else {
                vd.lowlink = Math.min(vd.lowlink, continuation.wd.lowlink);
            }

            /*
             * Considering successors of vertex.
             */

            final Iterator<? extends InterfaceVertex> vSuccIt = continuation.vSuccIt;
            while (vSuccIt.hasNext()) {
                final InterfaceVertex w = vSuccIt.next();
                final MyVertexData wd = data.get(w);
                if (wd.index < 0) {
                    // w has not yet been visited: recurse on it.
                    final MyContinuation paused = new MyContinuation(
                            vd,
                            vSuccIt,
                            wd);
                    continuations.add(paused);
                    final MyContinuation started = new MyContinuation(wd);
                    continuations.add(started);
                    continue LOOP_1;
                } else if (data.stackContains(w)) {
                    // w is in stack, hence in the current SCC.
                   vd.lowlink = Math.min(vd.lowlink, wd.index);
                }
            }

            /*
             * If v is a root node, pop the stack and generate an SCC.
             */

            if (vd.lowlink == vd.index) {
                if (callProcessor(
                        data,
                        vd,
                        processor)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * This method is closer to the usual description of the algorithms, but is
     * recursive, so causes StackOverflowError in case of large SCCs.
     * 
     * @return True if must stop computation, false otherwise.
     */
    private static boolean strongConnect_recursion(
            MyData data,
            MyVertexData vd,
            InterfaceVertexCollProcessor processor) {
        vd.index = data.index;
        vd.lowlink = data.index;
        data.index++;

        data.stackPush(vd.vertex);

        /*
         * Considering successors of vertex.
         */

        for (InterfaceVertex w : vd.vertex.successors()) {
            final MyVertexData wd = data.get(w);
            if (wd.index < 0) {
                // w has not yet been visited: recurse on it.
                if (strongConnect_recursion(
                        data,
                        wd,
                        processor)) {
                    return true;
                }
                vd.lowlink = Math.min(vd.lowlink, wd.lowlink);
            } else if (data.stackContains(w)) {
                // w is in stack, hence in the current SCC.
                vd.lowlink = Math.min(vd.lowlink, wd.index);
            }
        }

        /*
         * If v is a root node, pop the stack and generate an SCC.
         */

        if (vd.lowlink == vd.index) {
            if (callProcessor(
                    data,
                    vd,
                    processor)) {
                return true;
            }
        }

        return false;
    }
    
    /**
     * @return True if must stop, false otherwise.
     */
    private static boolean callProcessor(
            MyData data,
            MyVertexData vd,
            InterfaceVertexCollProcessor processor) {

        // Retrieving SCC vertices.
        final ArrayList<InterfaceVertex> vList = new ArrayList<InterfaceVertex>();
        {
            InterfaceVertex vertex;
            do {
                vertex = data.stackPop();
                vList.add(vertex);
            } while (vertex != vd.vertex);
        }

        final Object[] vArr = SortUtils.toSortedArr(vList);
        
        processor.processCollBegin();
        for (Object vertex : vArr) {
            processor.processCollVertex((InterfaceVertex) vertex);
        }
        return processor.processCollEnd();
    }
}
