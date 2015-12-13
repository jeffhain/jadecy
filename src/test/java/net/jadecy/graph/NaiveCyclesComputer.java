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

import net.jadecy.graph.InterfaceVertex;
import net.jadecy.graph.InterfaceVertexCollProcessor;
import net.jadecy.utils.UniqStack;

/**
 * Computes all cycles, simply but slowly.
 * Designed to serve as reference for tests.
 */
class NaiveCyclesComputer {

    /*
     * Uses the same basic algorithm than SomeCyclesComputer, but:
     * - We don't skip vertices that we already encountered.
     * - We need to take care not to cause multiple processing of a same cycle.
     * - We don't bother with continuations, since we only use it for tests.
     */
    
    //--------------------------------------------------------------------------
    // PRIVATE CLASSES
    //--------------------------------------------------------------------------

    /**
     * Abstract class for cycles based on the stack (for quick checks),
     * or on an effectively immutable array (when adding cycles in set of
     * processed cycles).
     * 
     * The equals(...) method computes equality modulo an eventual shift.
     */
    private static abstract class MyAbstractCycle {
        public abstract int size();
        public abstract InterfaceVertex get(int index);
        /**
         * Needs to be overridden.
         */
        @Override
        public int hashCode() {
            throw new UnsupportedOperationException();
        }
        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof MyAbstractCycle)) {
                return false;
            }
            final MyAbstractCycle ozer = (MyAbstractCycle) other;
            if (this.hashCode() != ozer.hashCode()) {
                return false;
            }

            final MyAbstractCycle a = this;
            final MyAbstractCycle b = ozer;
            final int aSize = a.size();
            final int bSize = b.size();
            if (aSize != bSize) {
                return false;
            }

            /*
             * shift = positive shift to obtain "b" from "a",
             * i.e. such as "a[i] = b[i+shift]".
             * Invalid if -1.
             * 
             * ex.:
             * a = {x,y,z,t}
             * b = {t,x,y,z}
             * shift = 1
             */

            int shift = -1;
            int i = 0;
            int j;
            {
                final InterfaceVertex aFirst = a.get(i);
                for (j=0;j<aSize;j++) {
                    final InterfaceVertex tmpBElement = b.get(j);
                    if (tmpBElement == aFirst) {
                        shift = j;
                        break;
                    }
                }
            }

            if (shift < 0) {
                // Could not find a's first in b.
                return false;
            }

            /*
             * Comparing:
             * a = {x,y,z,t}
             *      - - -
             * b = {t,x,y,z}
             *        - - -
             */

            while (i < aSize-shift) {
                if (b.get(j) != a.get(i)) {
                    return false;
                }
                i++;
                j++;
            }

            /*
             * Comparing:
             * a = {x,y,z,t}
             *            -
             * b = {t,x,y,z}
             *      -
             */

            j = 0;
            while (j < shift) {
                if (b.get(j) != a.get(i)) {
                    return false;
                }
                i++;
                j++;
            }

            return true;
        }
        static int computeHashCode(MyAbstractCycle cycle) {
            int h = 0;
            final int size = cycle.size();
            for (int i = 0; i < size; i++) {
                // Needs to be invariant modulo shift.
                h += cycle.get(i).hashCode();
            }
            return h;
        }
    }

    private static class MyCycleInSet extends MyAbstractCycle {
        private final InterfaceVertex[] cycleTab;
        private final int hc;
        public MyCycleInSet(
                InterfaceVertex[] cycleTab,
                int hc) {
            this.cycleTab = cycleTab;
            this.hc = hc;
        }
        @Override
        public int hashCode() {
            return this.hc;
        }
        @Override
        public int size() {
            return this.cycleTab.length;
        }
        @Override
        public InterfaceVertex get(int index) {
            return this.cycleTab[index];
        }
    }

    private static class MyCycleForQuickCheck extends MyAbstractCycle {
        private final UniqStack<InterfaceVertex> stack;
        private final int offset;
        private final int hc;
        public MyCycleForQuickCheck(
                UniqStack<InterfaceVertex> stack,
                int cycleSize) {
            this.stack = stack;
            this.offset = stack.size() - cycleSize;
            // Need to call it after other fields setting.
            this.hc = computeHashCode(this);
        }
        @Override
        public int hashCode() {
            return this.hc;
        }
        @Override
        public int size() {
            return this.stack.size() - this.offset;
        }
        @Override
        public InterfaceVertex get(int index) {
            return this.stack.get(this.offset + index);
        }
    }

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /**
     * Computes cycles of the specified graph.
     * 
     * Calls to processor.processCollVertex(...) are ordered according to the order
     * in which elements appear in the cycle.
     * The first call is not necessarily done on the lowest element, i.e. the
     * specified cycle is not necessarily in normalized form.
     * 
     * @param graph Graph of which cycles must be computed.
     * @param maxSize Max size of processed cycles. If < 0, no limit.
     * @param processor Processor to process the cycles with.
     */
    public static void computeCycles(
            Collection<? extends InterfaceVertex> graph,
            int maxSize,
            InterfaceVertexCollProcessor processor) {
        final HashSet<MyCycleInSet> processedCycles = new HashSet<MyCycleInSet>();
        final UniqStack<InterfaceVertex> stack = new UniqStack<InterfaceVertex>();
        for (InterfaceVertex v : graph) {
            if (computeCyclesFrom(
                    v,
                    stack,
                    processedCycles,
                    maxSize,
                    processor)) {
                return;
            }
        }
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------

    private NaiveCyclesComputer() {
    }
    
    /**
     * This method is recursive.
     * 
     * @return True if must stop computation, false otherwise.
     */
    private static boolean computeCyclesFrom(
            InterfaceVertex v,
            UniqStack<InterfaceVertex> stack,
            HashSet<MyCycleInSet> processedCycles,
            int maxSize,
            InterfaceVertexCollProcessor processor) {

        boolean mustStop = false;
        
        stack.push(v);
        
        for (InterfaceVertex w : v.successors()) {
            final int wIndexInStack = stack.indexOf(w);
            if (wIndexInStack >= 0) {
                if (onCycle(stack, wIndexInStack, processedCycles)) {
                    mustStop = processCycleIfNotTooLarge(
                            stack,
                            wIndexInStack,
                            maxSize,
                            processor);
                }
            } else {
                mustStop = computeCyclesFrom(
                        w,
                        stack,
                        processedCycles,
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
     * @return True if must process the cycle, false otherwise.
     */
    private static boolean onCycle(
            UniqStack<InterfaceVertex> stack,
            int wIndexInStack,
            HashSet<MyCycleInSet> processedCycles) {
        final int cycleSize = stack.size() - wIndexInStack;
        final MyCycleForQuickCheck cycleForCheck = new MyCycleForQuickCheck(
                stack,
                cycleSize);
        if (!processedCycles.contains(cycleForCheck)) {
            final InterfaceVertex[] cycleTab = new InterfaceVertex[cycleSize];
            for (int i = wIndexInStack; i < stack.size(); i++) {
                cycleTab[i-wIndexInStack] = stack.get(i);
            }
            final MyCycleInSet cycle = new MyCycleInSet(
                    cycleTab,
                    cycleForCheck.hashCode());
            final boolean forCheck = processedCycles.add(cycle);
            if (!forCheck) {
                throw new AssertionError();
            }
            return true;
        } else {
            return false;
        }
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
