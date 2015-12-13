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

public class SomeCyclesComputerTest extends TestCase {

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
        //@Override
        public void processCollBegin() {
            this.cycleList.add(new ArrayList<InterfaceVertex>());
        }
        //@Override
        public void processCollVertex(InterfaceVertex vertex) {
            this.cycleList.get(this.cycleList.size()-1).add(vertex);
        }
        //@Override
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
    
    public void test_computeSomeCycles_exceptions() {
        final Collection<InterfaceVertex> graph = new ArrayList<InterfaceVertex>();
        final MyCycleComputerVcp processor = new MyCycleComputerVcp();
        
        try {
            SomeCyclesComputer.computeSomeCycles(
                    null,
                    0,
                    processor);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        try {
            SomeCyclesComputer.computeSomeCycles(
                    graph,
                    0,
                    null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
    }
    
    public void test_computeSomeCycles_emptyGraph() {
        final List<InterfaceVertex> graph = new ArrayList<InterfaceVertex>();
        
        final MyCycleComputerVcp processor = new MyCycleComputerVcp();
        SomeCyclesComputer.computeSomeCycles(graph, -1, processor);

        assertEquals(0, processor.cycleList.size());
    }

    public void test_computeSomeCycles_oneVertex_noCycle() {
        final List<InterfaceVertex> graph = new ArrayList<InterfaceVertex>();
        GraphTestsUtilz.newInGraph(graph, 1);
        
        final MyCycleComputerVcp processor = new MyCycleComputerVcp();
        SomeCyclesComputer.computeSomeCycles(graph, -1, processor);

        assertEquals(0, processor.cycleList.size());
    }

    public void test_computeSomeCycles_oneVertex_cycle() {
        final List<InterfaceVertex> graph = new ArrayList<InterfaceVertex>();
        
        GraphTestsUtilz.newInGraph(graph, 1);
        GraphTestsUtilz.ensurePathFromIndexes(graph, 0, 0);
        
        final MyCycleComputerVcp processor = new MyCycleComputerVcp();
        SomeCyclesComputer.computeSomeCycles(graph, -1, processor);

        assertEquals(1, processor.cycleList.size());
    }
    
    public void test_computeSomeCycles_earlyStop() {
        final Random random = new Random(SEED);
        
        final BallGraphGenerator gg = new BallGraphGenerator(random.nextLong(), 4);
        final Collection<InterfaceVertex> graph = gg.newGraph();
        
        final MyCycleComputerVcp processor = new MyCycleComputerVcp();
        processor.nbrOfCyclesUntilStop = 3;
        SomeCyclesComputer.computeSomeCycles(graph, -1, processor);
        
        assertEquals(3, processor.cycleList.size());
    }
    
    public void test_computeSomeCycles_maxSize() {
        final int graphSize = 5;
        final BallGraphGenerator gg = new BallGraphGenerator(SEED, graphSize);
        
        // When max size growth, amount of cycles found for each size must not change,
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
            assertTrue(expectedMaxSize >= actualMaxSize);
            if (maxSize != 0) {
                // Checking we got some.
                assertTrue(actualMaxSize != 0);
            }

            if (maxSize < 0) {
                // First pass: computing the map.
                refCountBySize = GraphTestsUtilz.computeCountBySize(processor.cycleList);
            } else {
                // Other passes: checking count by size.
                final Map<Integer,Integer> countBySize = GraphTestsUtilz.computeCountBySize(processor.cycleList);
                for (Integer size : countBySize.keySet()) {
                    assertTrue(refCountBySize.get(size) >= countBySize.get(size));
                }
            }
        }
    }

    /*
     * General and sturdiness.
     */
    
    public void test_computeSomeCycles_general_sturdiness() {
        final Random random = new Random(SEED);
        for (int maxGraphSize = 2; maxGraphSize < 15; maxGraphSize++) {
            for (int i = 0; i < 10*1000; i++) {
                final int maxCycleSize = -1 + random.nextInt(10);
                this.test_computeSomeCycles_againstNaive(
                        new RandomGraphGenerator(
                                random.nextLong(),
                                maxGraphSize),
                                maxCycleSize);
            }
        }
    }
    
    public void test_computeSomeCycles_noStackOverflowError() {
        final Random random = new Random(SEED);
        
        final CycleGraphGenerator gg = new CycleGraphGenerator(
                random.nextLong(),
                GraphTestsUtilz.LARGER_THAN_CALL_STACK);
        
        final Collection<InterfaceVertex> graph = gg.newGraph();
        final MyCycleComputerVcp processor = new MyCycleComputerVcp();
        
        SomeCyclesComputer.computeSomeCycles(graph, -1, processor);
        
        assertEquals(1, processor.cycleList.size());
    }
    
    /*
     * Determinism.
     */
    
    /**
     * Checks that vertices hash codes have no effect on the result,
     * i.e. that ordered sets or maps are used where needed.
     */
    public void test_computeSomeCycles_determinism() {
        
        final Random seedGenerator = new Random(SEED);
        
        for (int k = 0; k < 100; k++) {
            
            List<List<InterfaceVertex>> firstResult = null;
            
            final long currentSeed = seedGenerator.nextLong();
            
            for (int i = 0; i < 10; i++) {
                // Always generating the same graph,
                // but with (typically) different hash codes for vertices.
                final Random random = new Random(currentSeed);
                final int maxGraphSize = 1 + random.nextInt(5);
                final RandomGraphGenerator gg = new RandomGraphGenerator(
                        random.nextLong(),
                        maxGraphSize);
                final List<InterfaceVertex> graph = gg.newGraph();
                
                final MyCycleComputerVcp processor = new MyCycleComputerVcp();
                final int maxCycleSize = (random.nextBoolean() ? -1 : graph.size()/2);
                SomeCyclesComputer.computeSomeCycles(graph, maxCycleSize, processor);
                
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

    /**
     * Tests that we only find cycles that exist.
     */
    private void test_computeSomeCycles_againstNaive(
            InterfaceGraphGenerator gg,
            int maxSize) {
        
        if (DEBUG) {
            System.out.println("test_computeSomeCycles_againstNaive(" + gg + "," + maxSize + ")");
        }
        
        final Collection<InterfaceVertex> graph = gg.newGraph();
        
        if (DEBUG) {
            GraphTestsUtilz.printGraph(graph);
        }
        
        final TreeSet<ComparableVertexArrayList> allNormalizedCycles;
        {
            final MyCycleComputerVcp processor = new MyCycleComputerVcp();
            NaiveCyclesComputer.computeCycles(graph, maxSize, processor);
            allNormalizedCycles = GraphTestsUtilz.toNormalizedCyclesAsLists(processor.cycleList);
            // Only one call per cycle.
            assertEquals(allNormalizedCycles.size(), processor.cycleList.size());

            if (DEBUG) {
                System.out.println();
                System.out.println("expectedCycles:");
                for (ComparableVertexArrayList cycle : allNormalizedCycles) {
                    System.out.println(cycle);
                }
            }
        }
        
        final TreeSet<ComparableVertexArrayList> someNormalizedCycles;
        {
            final MyCycleComputerVcp processor = new MyCycleComputerVcp();
            SomeCyclesComputer.computeSomeCycles(graph, maxSize, processor);
            someNormalizedCycles = GraphTestsUtilz.toNormalizedCyclesAsLists(processor.cycleList);
            // Only one call per cycle.
            assertEquals(someNormalizedCycles.size(), processor.cycleList.size());

            if (DEBUG) {
                System.out.println();
                System.out.println("actualCycles:");
                for (ComparableVertexArrayList cycle : someNormalizedCycles) {
                    System.out.println(cycle);
                }
            }
        }

        checkContained(allNormalizedCycles, someNormalizedCycles);
    }
    
    /*
     * 
     */
    
    private static void checkContained(
            TreeSet<ComparableVertexArrayList> whole,
            TreeSet<ComparableVertexArrayList> part) {
        GraphTestsUtilz.checkConsistent(whole);
        GraphTestsUtilz.checkConsistent(part);
        
        {
            final TreeSet<ComparableVertexArrayList> exceedings = new TreeSet<ComparableVertexArrayList>(part);
            exceedings.removeAll(whole);
            if (exceedings.size() != 0) {
                for (ComparableVertexArrayList exceeding : exceedings) {
                    System.out.println("exceeding : " + exceeding);
                }
                assertTrue(false);
            }
        }
    }
}
