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

import net.jadecy.utils.ArgsUtils;
import net.jadecy.utils.UniqStack;

/**
 * Computes _some_ cycles of a graph, including cycles of size 1, and finds none
 * only if the graph has none.
 * 
 * Useful for quickly computing whether a graph has some cycles, or for quickly
 * getting some cycles in graphs for which an exhaustive computation would take
 * too long.
 * 
 * The point of this class over using CyclesComputer with an early-stopping
 * processor, is that it might find cycles in more areas of the graph, whereas
 * CyclesComputer could get stuck for a long time in a particular area and
 * not provide a lot of obvious cycles from other areas.
 */
public class SomeCyclesComputer {
    
    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------

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
    
    private static class MyContinuation {
        final InterfaceVertex v;
        /**
         * Lazily initialized, which allows to figure out whether it's first
         * run without storing a boolean.
         */
        Iterator<? extends InterfaceVertex> vSuccIt;
        public MyContinuation(InterfaceVertex v) {
            this.v = v;
        }
        /**
         * Must be called before vSuccIt().
         */
        public boolean firstRun() {
            return this.vSuccIt == null;
        }
        public Iterator<? extends InterfaceVertex> vSuccIt() {
            Iterator<? extends InterfaceVertex> vSuccIt = this.vSuccIt;
            if (vSuccIt == null) {
                vSuccIt = this.v.successors().iterator();
                this.vSuccIt = vSuccIt;
            }
            return vSuccIt;
        }
    }

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /**
     * Computes some cycles of the specified graph, including single vertex
     * cycles.
     * Computes none only if there is none, i.e. always finds at least one if
     * there are some.
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
     * @param graph Graph of which some cycles must be computed.
     *        Must be consistent, i.e. not contain duplicates, and contain all
     *        vertices reachable from contained ones.
     * @param maxSize Max size of processed cycles. If < 0, no limit.
     * @param processor Processor to process the cycles with.
     * @throws NullPointerException if graph or processor is null.
     */
    public static void computeSomeCycles(
            Collection<? extends InterfaceVertex> graph,
            int maxSize,
            InterfaceVertexCollProcessor processor) {
        
        ArgsUtils.requireNonNull(processor);
        
        // Implicit null check.
        if ((graph.size() == 0)
                || (maxSize == 0)) {
            return;
        }
        
        final HashSet<InterfaceVertex> encounteredVertices = new HashSet<InterfaceVertex>();
        final UniqStack<InterfaceVertex> stack = new UniqStack<InterfaceVertex>();
        
        final ArrayList<MyContinuation> continuations = (USE_CONTINUATION ? new ArrayList<MyContinuation>() : null);

        for (InterfaceVertex v : graph) {
            if (USE_CONTINUATION) {
                if (computeCyclesFrom_continuation(
                        v,
                        stack,
                        encounteredVertices,
                        maxSize,
                        processor,
                        //
                        continuations)) {
                    return;
                }
            } else {
                if (computeCyclesFrom_recursion(
                        v,
                        stack,
                        encounteredVertices,
                        maxSize,
                        processor)) {
                    return;
                }
            }
        }
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------

    private SomeCyclesComputer() {
    }

    /**
     * @param stack Always empty (on method call) in practice.
     * @return True if must stop computation, false otherwise.
     */
    private static boolean computeCyclesFrom_continuation(
            InterfaceVertex initialV,
            UniqStack<InterfaceVertex> stack,
            HashSet<InterfaceVertex> encounteredVertices,
            int maxSize,
            InterfaceVertexCollProcessor processor,
            //
            ArrayList<MyContinuation> continuations) {
        
        boolean mustStop = false;

        // This test could be done just once, before stack push
        // (with a "continue;" statement if true), but that would cause
        // useless continuations creations, so we do it before eventually
        // creating continuations.
        if (!encounteredVertices.add(initialV)) {
            return mustStop;
        }
        
        continuations.clear();
        continuations.add(new MyContinuation(initialV));

        LOOP_1 : while (continuations.size() != 0) {
            final MyContinuation continuation = continuations.remove(continuations.size()-1);
            
            if (continuation.firstRun()) {
                stack.push(continuation.v);
            }
            
            /*
             * Considering successors of vertex.
             */

            final Iterator<? extends InterfaceVertex> vSuccIt = continuation.vSuccIt();
            while (vSuccIt.hasNext()) {
                final InterfaceVertex w = vSuccIt.next();
                final int wIndexInStack = stack.indexOf(w);
                if (wIndexInStack >= 0) {
                    if (processCycleIfNotTooLarge(
                            stack,
                            wIndexInStack,
                            maxSize,
                            processor)) {
                        mustStop = true;
                        break LOOP_1;
                    }
                } else {
                    if (!encounteredVertices.add(w)) {
                        continue;
                    }
                    // Paused.
                    continuations.add(continuation);
                    // Started.
                    continuations.add(new MyContinuation(w));
                    continue LOOP_1;
                }
            }
            
            stack.pop(continuation.v);
        }
        
        return mustStop;
    }

    /**
     * This method is recursive.
     * 
     * @return True if must stop computation, false otherwise.
     */
    private static boolean computeCyclesFrom_recursion(
            InterfaceVertex v,
            UniqStack<InterfaceVertex> stack,
            HashSet<InterfaceVertex> encounteredVertices,
            int maxSize,
            InterfaceVertexCollProcessor processor) {

        boolean mustStop = false;

        if (!encounteredVertices.add(v)) {
            return mustStop;
        }
        
        stack.push(v);
        
        final InterfaceVertex lastOfStack = v;

        for (InterfaceVertex w : lastOfStack.successors()) {
            final int wIndexInStack = stack.indexOf(w);
            if (wIndexInStack >= 0) {
                mustStop = processCycleIfNotTooLarge(
                        stack,
                        wIndexInStack,
                        maxSize,
                        processor);
            } else {
                mustStop = computeCyclesFrom_recursion(
                        w,
                        stack,
                        encounteredVertices,
                        maxSize,
                        processor);
            }
            if (mustStop) {
                break;
            }
        }
        
        stack.pop(v);
        
        return mustStop;
    }
    
    /**
     * @return True if must stop computation, false otherwise.
     */
    private static boolean processCycleIfNotTooLarge(
            UniqStack<InterfaceVertex> stack,
            int firstIndex,
            int maxSize,
            InterfaceVertexCollProcessor processor) {
        final int cycleSize = stack.size() - firstIndex;
        if ((maxSize >= 0) && (cycleSize > maxSize)) {
            // Too large: ignoring it.
            return false;
        }
        
        processor.processCollBegin();
        for (int i = firstIndex; i < stack.size(); i++) {
            processor.processCollVertex(stack.get(i));
        }
        return processor.processCollEnd();
    }
}
