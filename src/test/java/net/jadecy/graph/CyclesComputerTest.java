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
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeSet;

import junit.framework.TestCase;
import net.jadecy.graph.GraphTestsUtilz.BallGraphGenerator;
import net.jadecy.graph.GraphTestsUtilz.ComparableVertexArrayList;
import net.jadecy.graph.GraphTestsUtilz.CycleGraphGenerator;
import net.jadecy.graph.GraphTestsUtilz.InterfaceGraphGenerator;
import net.jadecy.graph.GraphTestsUtilz.RandomGraphGenerator;

public class CyclesComputerTest extends TestCase {

    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------

    private static final boolean DEBUG = false;
    
    private static final boolean USE_RANDOM_SEED = false;
    private static final long SEED = USE_RANDOM_SEED ? new Random().nextLong() : 123456789L;
    static {
        if (USE_RANDOM_SEED) {
            System.out.println("SEED = " + SEED);
        }
    }

    //--------------------------------------------------------------------------
    // PRIVATE CLASSES
    //--------------------------------------------------------------------------
    
    private static class MyCycleComputerVcp implements InterfaceVertexCollProcessor {
        final List<List<InterfaceVertex>> cycleList = new ArrayList<List<InterfaceVertex>>();
        int nbrOfCyclesUntilStop = -1;
        public MyCycleComputerVcp() {
        }
        @Override
        public void processCollBegin() {
            this.cycleList.add(new ArrayList<InterfaceVertex>());
        }
        @Override
        public void processCollVertex(InterfaceVertex vertex) {
            this.cycleList.get(this.cycleList.size()-1).add(vertex);
        }
        @Override
        public boolean processCollEnd() {
            if (this.nbrOfCyclesUntilStop < 0) {
                return false;
            } else {
                return (--this.nbrOfCyclesUntilStop <= 0);
            }
        }
    }
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /*
     * Special cases.
     */

    public void test_computeCycles_exceptions() {
        final Collection<InterfaceVertex> graph = new ArrayList<InterfaceVertex>();
        final MyCycleComputerVcp processor = new MyCycleComputerVcp();
        
        try {
            CyclesComputer.computeCycles(
                    null,
                    0,
                    processor);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        try {
            CyclesComputer.computeCycles(
                    graph,
                    0,
                    null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
    }
    
    public void test_computeCycles_emptyGraph() {
        final List<InterfaceVertex> graph = new ArrayList<InterfaceVertex>();
        
        final MyCycleComputerVcp processor = new MyCycleComputerVcp();
        CyclesComputer.computeCycles(graph, -1, processor);

        assertEquals(0, processor.cycleList.size());
    }

    public void test_computeCycles_oneVertex_noCycle() {
        final List<InterfaceVertex> graph = new ArrayList<InterfaceVertex>();
        GraphTestsUtilz.newInGraph(graph, 1);
        
        final MyCycleComputerVcp processor = new MyCycleComputerVcp();
        CyclesComputer.computeCycles(graph, -1, processor);

        assertEquals(0, processor.cycleList.size());
    }

    public void test_computeCycles_oneVertex_cycle() {
        final List<InterfaceVertex> graph = new ArrayList<InterfaceVertex>();
        
        GraphTestsUtilz.newInGraph(graph, 1);
        GraphTestsUtilz.ensurePathFromIndexes(graph, 0, 0);
        
        final MyCycleComputerVcp processor = new MyCycleComputerVcp();
        CyclesComputer.computeCycles(graph, -1, processor);

        assertEquals(1, processor.cycleList.size());
    }

    public void test_computeCycles_earlyStop() {
        for (boolean mustStopOnAOneVertexCycle : new boolean[]{false,true}) {
            final Random random = new Random(SEED);
            
            final BallGraphGenerator gg = new BallGraphGenerator(random.nextLong(), 4);
            
            final Collection<InterfaceVertex> graph = gg.newGraph();
            
            final MyCycleComputerVcp processor = new MyCycleComputerVcp();
            // One-vertex cycles are processed first.
            final int nbrUntilStop = (mustStopOnAOneVertexCycle ? 2 : 5);
            processor.nbrOfCyclesUntilStop = nbrUntilStop;
            CyclesComputer.computeCycles(graph, -1, processor);
            
            assertEquals(nbrUntilStop, processor.cycleList.size());
        }
    }
    
    public void test_computeCycles_maxSize() {
        final int graphSize = 5;
        final BallGraphGenerator gg = new BallGraphGenerator(SEED, graphSize);
        
        // When max size grows, amount of cycles found for each size must not change,
        // else that would mean that some were screened out by some bug.
        Map<Integer,Integer> refCountBySize = null;
        
        for (int maxSize : new int[]{-1,0,1,2,3}) {
            final Collection<InterfaceVertex> graph = gg.newGraph();
            
            final MyCycleComputerVcp processor = new MyCycleComputerVcp();
            CyclesComputer.computeCycles(graph, maxSize, processor);

            final int expectedMaxSize = (maxSize < 0) ? graphSize : maxSize;
            int actualMaxSize = 0;
            for (List<InterfaceVertex> cycle : processor.cycleList) {
                actualMaxSize = Math.max(actualMaxSize, cycle.size());
            }
            assertEquals(expectedMaxSize, actualMaxSize);

            if (maxSize < 0) {
                // First pass: computing the map.
                refCountBySize = GraphTestsUtilz.computeCountBySize(processor.cycleList);
            } else {
                // Other passes: checking count by size.
                final Map<Integer,Integer> countBySize = GraphTestsUtilz.computeCountBySize(processor.cycleList);
                for (Integer size : countBySize.keySet()) {
                    assertEquals(refCountBySize.get(size), countBySize.get(size));
                }
            }
        }
    }
    
    /*
     * General and sturdiness.
     */
    
    /**
     * To make sure we don't miss any cycle, elementary or not.
     */
    public void test_computeCycles_ballGraph() {
        final Random random = new Random(SEED);
        for (int graphSize = 1; graphSize < 5; graphSize++) {
            final int maxCycleSize = -1;
            test_computeCycles_againstNaive(
                    new BallGraphGenerator(
                            random.nextLong(),
                            graphSize),
                            maxCycleSize);
        }
    }

    public void test_computeCycles_general_sturdiness() {
        final Random random = new Random(SEED);
        for (int i = 0; i < 100*1000; i++) {
            final int maxGraphSize = 1 + random.nextInt(10);
            final int maxCycleSize = -1 + random.nextInt(maxGraphSize + 2);
            test_computeCycles_againstNaive(
                    new RandomGraphGenerator(
                            random.nextLong(),
                            maxGraphSize),
                            maxCycleSize);
        }
    }

    public void test_computeCycles_noStackOverflowError() {
        final Random random = new Random(SEED);

        final CycleGraphGenerator gg = new CycleGraphGenerator(
                random.nextLong(),
                GraphTestsUtilz.LARGER_THAN_CALL_STACK);

        final Collection<InterfaceVertex> graph = gg.newGraph();
        
        final MyCycleComputerVcp processor = new MyCycleComputerVcp();
        CyclesComputer.computeCycles(graph, -1, processor);

        assertEquals(1, processor.cycleList.size());
    }

    /*
     * Determinism.
     */
    
    /**
     * Checks that vertices hash codes have no effect on the result,
     * i.e. that ordered sets or maps are used where needed.
     */
    public void test_computeCycles_determinism() {
        
        final Random seedGenerator = new Random(SEED);
        
        for (int k = 0; k < 100; k++) {
            
            List<List<InterfaceVertex>> firstResult = null;
            
            final long currentSeed = seedGenerator.nextLong();
            
            for (int i = 0; i < 10; i++) {
                // Always generating the same graph,
                // but with (typically) different hash codes for vertices.
                final Random random = new Random(currentSeed);
                final int maxSize = 1 + random.nextInt(5);
                final RandomGraphGenerator gg = new RandomGraphGenerator(
                        random.nextLong(),
                        maxSize);
                final List<InterfaceVertex> graph = gg.newGraph();
                
                final MyCycleComputerVcp processor = new MyCycleComputerVcp();
                final int maxCycleSize = (random.nextBoolean() ? -1 : graph.size()/2);
                CyclesComputer.computeCycles(graph, maxCycleSize, processor);
                
                final List<List<InterfaceVertex>> result = processor.cycleList;
                if (firstResult == null) {
                    firstResult = result;
                    if (DEBUG) {
                        System.out.println("firstResult = " + firstResult);
                    }
                } else {
                    // Need to compare strings because vertices are from different graphs.
                    assertEquals(firstResult.toString(), result.toString());
                }
            }
        }
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------

    private void test_computeCycles_againstNaive(
            InterfaceGraphGenerator gg,
            int maxSize) {
        if (DEBUG) {
            System.out.println("test_computeCycles_againstNaive(" + gg + ",maxSize=" + maxSize + ")");
        }
        
        final Collection<InterfaceVertex> graph = gg.newGraph();
        
        if (DEBUG) {
            GraphTestsUtilz.printGraph(graph);
        }
        
        final TreeSet<ComparableVertexArrayList> expectedCycles;
        {
            final MyCycleComputerVcp processor = new MyCycleComputerVcp();
            NaiveCyclesComputer.computeCycles(graph, maxSize, processor);
            expectedCycles = GraphTestsUtilz.toNormalizedCyclesAsLists(processor.cycleList);
            
            // Only one call per cycle.
            assertEquals(expectedCycles.size(), processor.cycleList.size());

            if (DEBUG) {
                System.out.println();
                System.out.println("expectedCycles:");
                for (ComparableVertexArrayList cycle : expectedCycles) {
                    System.out.println(cycle);
                }
            }
        }
        
        final TreeSet<ComparableVertexArrayList> actualCycles;
        {
            final MyCycleComputerVcp processor = new MyCycleComputerVcp();
            CyclesComputer.computeCycles(graph, maxSize, processor);
            actualCycles = GraphTestsUtilz.toNormalizedCyclesAsLists(processor.cycleList);
            
            // Only one call per cycle.
            assertEquals(actualCycles.size(), processor.cycleList.size());
            
            if (DEBUG) {
                System.out.println();
                System.out.println("actualCycles:");
                for (ComparableVertexArrayList cycle : actualCycles) {
                    System.out.println(cycle);
                }
            }
        }

        checkEqual(expectedCycles, actualCycles);
    }
    
    /*
     * 
     */
    
    private static void checkEqual(
            TreeSet<ComparableVertexArrayList> expected,
            TreeSet<ComparableVertexArrayList> actual) {
        GraphTestsUtilz.checkConsistent(expected);
        GraphTestsUtilz.checkConsistent(actual);
        
        final boolean equal = expected.equals(actual);
        if (!equal) {
            {
                final TreeSet<ComparableVertexArrayList> missings = new TreeSet<ComparableVertexArrayList>(expected);
                missings.removeAll(actual);
                if (missings.size() != 0) {
                    for (ComparableVertexArrayList missing : missings) {
                        System.out.println("missing : " + missing);
                    }
                }
            }
            {
                final TreeSet<ComparableVertexArrayList> exceedings = new TreeSet<ComparableVertexArrayList>(actual);
                exceedings.removeAll(expected);
                if (exceedings.size() != 0) {
                    for (ComparableVertexArrayList exceeding : exceedings) {
                        System.out.println("exceeding : " + exceeding);
                    }
                }
            }
        }
        assertTrue(equal);
    }
}
