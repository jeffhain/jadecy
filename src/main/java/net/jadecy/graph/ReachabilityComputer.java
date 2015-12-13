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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import net.jadecy.utils.ArgsUtils;
import net.jadecy.utils.SortUtils;

/**
 * Computes vertices reachable from a begin set of vertices, step by step,
 * i.e. computes each set that could be reached when moving from previously
 * reached vertices to all of their direct successors.
 */
public class ReachabilityComputer {

    /*
     * If not wanting step-by-step computation, reachability computation could be
     * made a bit faster by removing the contains(...) in the inner loop and dealing
     * with begin vertices inclusion after main loops, but that would only help in
     * case of very dense graph, and on the other hand step-by-step computation is
     * often faster due to having to order vertices only among each step.
     */
    
    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------
    
    private static final Collection<InterfaceVertex> EMPTY_COLL =
            new ArrayList<InterfaceVertex>(0);

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    /**
     * Computes vertices that can be reached from a specified begin set.
     * 
     * For a same input, and regardless of vertices hash codes,
     * this method always produces the same output.
     * 
     * Each steps contains vertices never present in any previous step
     * (else could run forever in case of cycles).
     * If no new vertex can be reached for a step, this step causes
     * no call to processor and the computation stops here.
     * Yet, if begin set is not empty and mustIncludeBeginVertices is false,
     * first step (i.e. step for zero edge crossing) is empty but is still
     * processed, eventually followed by more and non-empty steps.
     * 
     * For each step, calls to processor.processCollVertex(...) are ordered
     * according to vertices natural ordering.
     * 
     * @param graph A graph.
     * @param beginVertexFilter Filter to define begin vertices.
     * @param mustIncludeBeginVertices If true, begin vertices are added
     *        into the processed ones even if never reached.
     * @param mustIncludeReachedBeginVertices If true, reached begin vertices
     *        are not ignored. Only used if mustIncludeBeginVertices is false.
     * @param processor Processor to process the vertices that could be reached
     *        from begin vertices, step by step.
     * @throws NullPointerException if any argument is null.
     */
    public static void computeReachability(
            Collection<? extends InterfaceVertex> beginVertexColl,
            boolean mustIncludeBeginVertices,
            boolean mustIncludeReachedBeginVertices,
            //
            InterfaceVertexCollProcessor processor) {

        ArgsUtils.requireNonNull(beginVertexColl);
        ArgsUtils.requireNonNull(processor);
        
        if (beginVertexColl.size() == 0) {
            // Nothing new reached: no call to processor.
            return;
        }
        
        if (mustIncludeBeginVertices) {
            // To avoid useless tests in loops.
            mustIncludeReachedBeginVertices = true;
        }

        // Using a set to guard against duplications, and our own one even if input
        // is a set, not to risk messing up input, and to be sure of contains(...) performances.
        final Set<InterfaceVertex> beginVertexSet = new HashSet<InterfaceVertex>(beginVertexColl);

        {
            final Collection<InterfaceVertex> stepZeroColl;
            if (mustIncludeBeginVertices) {
                stepZeroColl = beginVertexSet;
            } else {
                stepZeroColl = EMPTY_COLL;
            }
            final boolean mustStop = callProcessor(
                    stepZeroColl,
                    processor);
            if (mustStop) {
                return;
            }
        }

        List<InterfaceVertex> previousStep = new ArrayList<InterfaceVertex>(beginVertexSet);
        List<InterfaceVertex> currentStep = new ArrayList<InterfaceVertex>();
        
        // Including reached begin vertices that we would not want to process
        // (only added in current step if want to process).
        final Set<InterfaceVertex> reachedVertexSet;
        if (mustIncludeBeginVertices) {
            reachedVertexSet = new HashSet<InterfaceVertex>(beginVertexSet);
        } else {
            reachedVertexSet = new HashSet<InterfaceVertex>();
        }
        
        while (true) {
            
            /*
             * Computing current step.
             */
            
            for (InterfaceVertex vertex : previousStep) {
                for (InterfaceVertex successor : vertex.successors()) {
                    if (reachedVertexSet.add(successor)) {
                        // Never reached this vertex before yet.
                        if (mustIncludeReachedBeginVertices
                                || (!beginVertexSet.contains(successor))) {
                            // Adding it into current step.
                            currentStep.add(successor);
                        }
                    }
                }
            }
            
            /*
             * 
             */
            
            if (currentStep.size() == 0) {
                // Nothing new reached: no call to processor.
                break;
            }
            
            /*
             * Processing current step.
             */
            
            final boolean mustStop = callProcessor(
                    currentStep,
                    processor);
            if (mustStop) {
                break;
            }
            
            /*
             * Swapping previous and current steps.
             */

            List<InterfaceVertex> tmp = previousStep;
            previousStep = currentStep;
            currentStep = tmp;

            // Clearing current step.
            currentStep.clear();
        }
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    private ReachabilityComputer() {
    }
    
    /**
     * @return True if must stop, false otherwise.
     */
    private static boolean callProcessor(
            Collection<? extends InterfaceVertex> coll,
            InterfaceVertexCollProcessor processor) {
        
        // Sorting for determinism, since we don't use a sorted set.
        if (coll instanceof SortedSet) {
            throw new AssertionError();
        }
        
        final Object[] arr = SortUtils.toSortedArr(coll);

        processor.processCollBegin();
        for (Object vertex : arr) {
            processor.processCollVertex((InterfaceVertex) vertex);
        }
        return processor.processCollEnd();
    }
}
