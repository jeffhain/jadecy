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
import java.util.TreeSet;

import net.jadecy.utils.ArgsUtils;
import net.jadecy.utils.SortUtils;

/**
 * Computes the vertices involved in paths from each vertex of a begin set
 * to any vertex of an end set, and only these vertices, but with all their
 * original successors (not to modify input).
 * The considered paths are possibly cyclic, going throw same vertices
 * multiple times.
 * 
 * This computation is much faster and much less spamming that computing
 * all possible paths from begin to end.
 */
public class PathsGraphComputer {
    
    /*
     * This computer was initially created to compute all possible paths,
     * but that was found to be too memory and CPU consuming, and to produce
     * a potentially huge and not easily exploitable output.
     * Just computing a subset of the paths covering all involved edges,
     * or just the number of paths going through each edge, is also much
     * expensive in worst cases.
     * As a result we just compute a graph containing the paths.
     */
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    /**
     * Computes the vertices being in the intersection of vertices reachable
     * from begin in up to the specified max number of steps, and vertices
     * from which end is reachable in up to the specified max number of steps
     * as well.
     * Also, when an end vertex is reached, even by a zero length path (i.e. if
     * it is also in begin collection), no further path is considered from that
     * vertex, such as if begin is included in end, only begin vertices will
     * be computed.
     * 
     * The computed set of vertices includes all the vertices involved in
     * (possibly cyclic) paths from begin to end in up to the specified
     * max number of steps, or max path length, plus eventually vertices
     * corresponding to about twice longer paths.
     * 
     * Note that, input not being modified, the processed vertices have all
     * their original successors, even if they are not part of the paths graph.
     * 
     * For a same input, and regardless of vertices hash codes,
     * this method always produces the same output.
     * 
     * Calls to processor.processCollVertex(...) are ordered according to vertices
     * natural ordering.
     * 
     * @param beginVertexColl Collection of begin vertices.
     * @param endVertexColl Collection of end vertices.
     * @param maxSteps Max number of times edges are crossed,
     *        i.e. max length of considered paths.
     *        If is 0, graphs corresponding to zero length paths
     *        can still be processed. If < 0, no limit.
     * @param processor Processor to process the vertices of the computed
     *        dependencies graph. Not called for empty collections.
     * @throws NullPointerException if any argument is null.
     */
    public static void computePathsGraph(
            Collection<? extends InterfaceVertex> beginVertexColl,
            Collection<? extends InterfaceVertex> endVertexColl,
            int maxSteps,
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
        
        /*
         * 
         */
        
        // Using sets to guard against duplications, and our own ones even if inputs
        // are sets, not to risk messing up inputs, and to be sure of contains(...) performances.
        Set<InterfaceVertex> beginVertexSet = new HashSet<InterfaceVertex>(beginVertexColl);
        Set<InterfaceVertex> endVertexSet = new HashSet<InterfaceVertex>(endVertexColl);
        
        // Computing which vertices we can reach from begin set included, going forward.
        Set<InterfaceVertex> beginSetReachability = new HashSet<InterfaceVertex>();
        final boolean didReachEnd = computeReachabilityFromBeginToEnd(
                beginVertexSet,
                endVertexSet,
                maxSteps,
                beginSetReachability);
        
        if (!didReachEnd) {
            // No need to go further.
            return;
        }
        
        // Computing a work graph, to have predecessors for going backward,
        // and to be able to remove edges (successors).
        // Using sorted collection for work graph for determinism
        // (not done before for contains(...) performances).
        final boolean mustIgnoreDeadEnds = false;
        final TreeSet<WorkVertex> workGraph = WorkGraphUtilz.newWorkGraph(
                SortUtils.toSortedList(beginSetReachability),
                mustIgnoreDeadEnds);
        
        // Help GC.
        beginSetReachability = null;
        
        // Computing end set (limited to what was reachable from begin)
        // as work vertices.
        Set<WorkVertex> endWorkSet = new HashSet<WorkVertex>();
        for (WorkVertex v : workGraph) {
            final InterfaceVertex bv = v.backingVertex();
            if (endVertexSet.contains(bv)) {
                endWorkSet.add(v);
            }
        }
        
        // Help GC.
        endVertexSet = null;
        
        // End set reachability, computed within begin set reachability
        // (which is initial work graph).
        Set<WorkVertex> endSetReachability = new HashSet<WorkVertex>();
        computeReachabilityFromEndToBegin(
                beginVertexSet,
                endWorkSet,
                maxSteps,
                endSetReachability);
        
        // Help GC.
        beginVertexSet = null;
        
        // Removing from work graph all vertices not in end set reachability.
        {
            final List<WorkVertex> toRemoveList = new ArrayList<WorkVertex>();
            for (WorkVertex v : workGraph) {
                if (!endSetReachability.contains(v)) {
                    toRemoveList.add(v);
                }
            }
            
            // Help GC.
            endSetReachability = null;
            
            for (WorkVertex v : toRemoveList) {
                WorkGraphUtilz.removeFromGraph(workGraph, v);
            }
        }
        
        /*
         * 
         */
        
        if (workGraph.size() != 0) {
            processor.processCollBegin();
            for (WorkVertex v : workGraph) {
                final InterfaceVertex bv = v.backingVertex();
                processor.processCollVertex(bv);
            }
            processor.processCollEnd();
        }
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    private PathsGraphComputer() {
    }

    /*
     * 
     */
    
    private static boolean doIntersect(
            Set<InterfaceVertex> set1,
            Set<InterfaceVertex> set2) {
        // Looping on smallest set.
        if (set1.size() < set2.size()) {
            for (InterfaceVertex v1 : set1) {
                if (set2.contains(v1)) {
                    return true;
                }
            }
        } else {
            for (InterfaceVertex v2 : set2) {
                if (set1.contains(v2)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * When an end vertex is reached (possibly due to begin and end
     * overlapping), does not look further past it.
     * 
     * @param beginVertexSet (in)
     * @param endVertexSet (in)
     * @param reachedVertexSet (out) Includes begin vertices (even if also in end vertex set),
     *        and all reached vertices up to any end vertex.
     * @return True is did reach end set, false otherwise.
     */
    private static boolean computeReachabilityFromBeginToEnd(
            Set<InterfaceVertex> beginVertexSet,
            Set<InterfaceVertex> endVertexSet,
            int maxSteps,
            Set<InterfaceVertex> reachedVertexSet) {

        reachedVertexSet.clear();
        reachedVertexSet.addAll(beginVertexSet);
        
        boolean didReachEnd = doIntersect(
                beginVertexSet,
                endVertexSet);
        
        List<InterfaceVertex> previousStep = new ArrayList<InterfaceVertex>(beginVertexSet);
        // Making sure we don't start exploration from begin vertices
        // that are also end vertices.
        previousStep.removeAll(endVertexSet);
        List<InterfaceVertex> currentStep = new ArrayList<InterfaceVertex>();

        // Step 0 was begin set.
        int nextStepId = 1;
        while ((maxSteps < 0) || ((nextStepId++) <= maxSteps)) {

            /*
             * Computing current step.
             */

             for (InterfaceVertex vertex : previousStep) {
                 for (InterfaceVertex succ : vertex.successors()) {
                     if (reachedVertexSet.add(succ)) {
                         // Never reached this vertex before yet.
                         if (endVertexSet.contains(succ)) {
                             didReachEnd = true;
                         } else {
                             /*
                              * Adding it into current step.
                              *
                              * If it's last step (with maxSteps >= 0),
                              * we are sure it's not part of a path from begin to end
                              * of length <= maxSteps, but we still add it, because
                              * this treatment is not supposed to return only vertices
                              * of such paths, and doing a special case here would
                              * complicate the spec.
                              */
                             currentStep.add(succ);
                         }
                     }
                 }
             }

             if (currentStep.size() == 0) {
                 // Could not reach anything new that was not in end set.
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
        
        return didReachEnd;
    }

    /**
     * @param endVertexSet (in)
     * @param reachedVertexSet (out) Includes end vertices.
     */
    private static void computeReachabilityFromEndToBegin(
            Set<InterfaceVertex> beginVertexSet,
            Set<WorkVertex> endWorkSet,
            int maxSteps,
            Set<WorkVertex> reachedVertexSet) {

        reachedVertexSet.clear();
        reachedVertexSet.addAll(endWorkSet);
        
        List<WorkVertex> previousStep = new ArrayList<WorkVertex>(endWorkSet);
        List<WorkVertex> currentStep = new ArrayList<WorkVertex>();

        // Step 0 was end set.
        int nextStepId = 1;
        while ((maxSteps < 0) || ((nextStepId++) <= maxSteps)) {

            /*
             * Computing current step.
             */

             for (WorkVertex vertex : previousStep) {
                 for (WorkVertex pred : vertex.predecessors()) {
                     if (reachedVertexSet.add(pred)) {
                         // Never reached this vertex before yet.
                         final InterfaceVertex backingPred = pred.backingVertex();
                         if (beginVertexSet.contains(backingPred)) {
                             // Ignoring.
                         } else {
                             // Adding it into current step.
                             currentStep.add(pred);
                         }
                     }
                 }
             }

             if (currentStep.size() == 0) {
                 // Could not reach anything new that was not in begin set.
                 break;
             }

             /*
              * Swapping previous and current steps.
              */

             List<WorkVertex> tmp = previousStep;
             previousStep = currentStep;
             currentStep = tmp;

             // Clearing current step.
             currentStep.clear();
        }
    }
}
