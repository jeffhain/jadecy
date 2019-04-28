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
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import junit.framework.TestCase;
import net.jadecy.graph.GraphTestsUtilz.BallGraphGenerator;
import net.jadecy.graph.GraphTestsUtilz.ComparableVertexArrayList;
import net.jadecy.graph.GraphTestsUtilz.CycleGraphGenerator;
import net.jadecy.graph.GraphTestsUtilz.InterfaceGraphGenerator;
import net.jadecy.graph.GraphTestsUtilz.RandomGraphGenerator;

public class ShortestCyclesComputerTest extends TestCase {

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
    
    public void test_computeShortestCycles_exceptions() {
        final Collection<InterfaceVertex> graph = new ArrayList<InterfaceVertex>();
        final MyCycleComputerVcp processor = new MyCycleComputerVcp();
        
        try {
            ShortestCyclesComputer.computeShortestCycles(
                    null,
                    0,
                    processor);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        try {
            ShortestCyclesComputer.computeShortestCycles(
                    graph,
                    0,
                    null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
    }
    
    public void test_computeShortestCycles_emptyGraph() {
        final List<InterfaceVertex> graph = new ArrayList<InterfaceVertex>();
        
        final MyCycleComputerVcp processor = new MyCycleComputerVcp();
        ShortestCyclesComputer.computeShortestCycles(graph, -1, processor);

        assertEquals(0, processor.cycleList.size());
    }

    public void test_computeShortestCycles_oneVertex_noCycle() {
        final List<InterfaceVertex> graph = new ArrayList<InterfaceVertex>();
        GraphTestsUtilz.newInGraph(graph, 1);
        
        final MyCycleComputerVcp processor = new MyCycleComputerVcp();
        ShortestCyclesComputer.computeShortestCycles(graph, -1, processor);

        assertEquals(0, processor.cycleList.size());
    }

    public void test_computeShortestCycles_oneVertex_cycle() {
        final List<InterfaceVertex> graph = new ArrayList<InterfaceVertex>();
        
        GraphTestsUtilz.newInGraph(graph, 1);
        GraphTestsUtilz.ensurePathFromIndexes(graph, 0, 0);
        
        final MyCycleComputerVcp processor = new MyCycleComputerVcp();
        ShortestCyclesComputer.computeShortestCycles(graph, -1, processor);

        assertEquals(1, processor.cycleList.size());
    }
    
    public void test_computeShortestCycles_earlyStop() {
        for (boolean mustStopOnAOneVertexCycle : new boolean[]{false,true}) {
            final Random random = new Random(SEED);
            
            final BallGraphGenerator gg = new BallGraphGenerator(random.nextLong(), 4);
            
            final Collection<InterfaceVertex> graph = gg.newGraph();
            
            final MyCycleComputerVcp processor = new MyCycleComputerVcp();
            // One-vertex cycles are processed first.
            final int nbrUntilStop = (mustStopOnAOneVertexCycle ? 2 : 5);
            processor.nbrOfCyclesUntilStop = nbrUntilStop;
            ShortestCyclesComputer.computeShortestCycles(graph, -1, processor);
            
            assertEquals(nbrUntilStop, processor.cycleList.size());
        }
    }
    
    public void test_computeShortestCycles_maxSize_graphSize_0() {
        test_computeShortestCycles_maxSize(0);
    }
    
    public void test_computeShortestCycles_maxSize_graphSize_1() {
        test_computeShortestCycles_maxSize(1);
    }
    
    public void test_computeShortestCycles_maxSize_graphSize_2() {
        test_computeShortestCycles_maxSize(2);
    }
    
    public void test_computeShortestCycles_maxSize_graphSize_3() {
        test_computeShortestCycles_maxSize(3);
    }

    public void test_computeShortestCycles_maxSize(int graphSize) {
        final Random random = new Random(SEED);
        
        final CycleGraphGenerator gg = new CycleGraphGenerator(random.nextLong(), graphSize);
        final Collection<InterfaceVertex> graph = gg.newGraph();
        
        if (DEBUG) {
            GraphTestsUtilz.printGraph(graph);
        }
        
        for (int maxSize : new int[]{-1, 0, graphSize-1, graphSize, graphSize+1}) {

            if (DEBUG) {
                System.out.println("maxSize = " + maxSize);
            }

            final MyCycleComputerVcp processor = new MyCycleComputerVcp();
            CyclesComputer.computeCycles(graph, maxSize, processor);

            if ((graphSize != 0)
                    && ((maxSize < 0) || (maxSize >= graphSize))) {
                assertEquals(1, processor.cycleList.size());
                assertEquals(graphSize, processor.cycleList.get(0).size());
            } else {
                assertEquals(0, processor.cycleList.size());
            }
        }
    }

    /**
     * Tests than maxSize can cause smaller cycles to be processed,
     * that are not processed otherwise due to being screened out by
     * larger cycles that already cover their edges.
     * 
     * Using graph from test_computeShortestCycles_general_0_3_2_1_differentResult(),
     * for which [1,3] is not computed by our algorithm when maxSize < 0, but is
     * computed when maxSize = 2.
     */
    public void test_computeShortestCycles_maxSize_differentCycles() {

        /*
         * Graph (k = vertex of index and id k).
         * 
         *   3
         *   ^\
         *  /| v
         * 0 | 2
         * ^ |/
         *  \v
         *   1
         * 
         * All cycles:
         * [1,3]
         * [0,3,1]
         * [1,3,2]
         * [0,3,2,1]
         */
        
        final ArrayList<InterfaceVertex> vByIndex = new ArrayList<InterfaceVertex>();
        GraphTestsUtilz.newInGraph(vByIndex, 0);
        GraphTestsUtilz.newInGraph(vByIndex, 1);
        GraphTestsUtilz.newInGraph(vByIndex, 2);
        GraphTestsUtilz.newInGraph(vByIndex, 3);
        GraphTestsUtilz.ensurePathFromIndexes(vByIndex, 0, 3, 2, 1, 0);
        GraphTestsUtilz.ensurePathFromIndexes(vByIndex, 1, 3, 1);
        
        for (int maxSize : new int[]{-1, 2}) {
            
            if (DEBUG) {
                System.out.println("maxSize = " + maxSize);
            }
            
            @SuppressWarnings("unchecked")
            final List<InterfaceVertex> graph = (List<InterfaceVertex>) vByIndex.clone();

            final MyCycleComputerVcp processor = new MyCycleComputerVcp();
            ShortestCyclesComputer.computeShortestCycles(graph, maxSize, processor);

            final List<List<InterfaceVertex>> expectedCycleList = new ArrayList<List<InterfaceVertex>>();
            if (maxSize < 0) {
                expectedCycleList.add(GraphTestsUtilz.asListFromIndexes(vByIndex, 0, 3, 1));
                expectedCycleList.add(GraphTestsUtilz.asListFromIndexes(vByIndex, 1, 3, 2));
            } else {
                expectedCycleList.add(GraphTestsUtilz.asListFromIndexes(vByIndex, 1, 3));
            }
            
            checkEqualAfterNormalization(expectedCycleList, processor.cycleList);
        }
    }

    /*
     * General.
     * 
     * NB: Computed cycles doesn't depend on order in which vertices appear
     * in the specified graph, because our SCCs computation treatment always
     * gives vertices in their natural order.
     * 
     * Some of these tests are less here to check that the behavior is correct,
     * than to characterize it.
     */

    public void test_computeShortestCycles_general_0_1_2() {

        /*
         * Graph (k = vertex of index and id k).
         * 
         *   1
         *   ^
         *  /|
         * v |
         * 0 |
         * ^ |
         *  \|
         *   v
         *   2
         * 
         * All cycles:
         * [0,1]
         * [0,2]
         * [1,2]
         * [0,1,2]
         * [0,2,1]
         */
        
        final ArrayList<InterfaceVertex> vByIndex = new ArrayList<InterfaceVertex>();
        GraphTestsUtilz.newInGraph(vByIndex, 0);
        GraphTestsUtilz.newInGraph(vByIndex, 1);
        GraphTestsUtilz.newInGraph(vByIndex, 2);
        GraphTestsUtilz.ensurePathFromIndexes(vByIndex, 0, 1, 2, 0);
        GraphTestsUtilz.ensurePathFromIndexes(vByIndex, 0, 2, 1, 0);

        final Random random = new Random(SEED);

        for (int k = 0; k < 100; k++) {
            @SuppressWarnings("unchecked")
            final List<InterfaceVertex> graph = (List<InterfaceVertex>) vByIndex.clone();

            // Result won't depend on vertices order in input collection.
            shuffle(random, graph);

            if (DEBUG) {
                GraphTestsUtilz.printGraph(graph);
            }

            for (boolean mustUseRefAlgo : new boolean[]{false,true}) {
                
                if (DEBUG) {
                    System.out.println("mustUseRefAlgo = " + mustUseRefAlgo);
                }
                
                final MyCycleComputerVcp processor = new MyCycleComputerVcp();
                if (mustUseRefAlgo) {
                    RefShortestCyclesComputer.computeShortestCycles(graph, -1, processor);
                } else {
                    ShortestCyclesComputer.computeShortestCycles(graph, -1, processor);
                }
                
                final List<List<InterfaceVertex>> expectedCycleList = new ArrayList<List<InterfaceVertex>>();
                expectedCycleList.add(GraphTestsUtilz.asListFromIndexes(graph, 0, 1));
                expectedCycleList.add(GraphTestsUtilz.asListFromIndexes(graph, 0, 2));
                expectedCycleList.add(GraphTestsUtilz.asListFromIndexes(graph, 1, 2));
                
                checkEqualAfterNormalization(expectedCycleList, processor.cycleList);
            }
        }
    }

    /*
     * General: cases where type of computed cycles depend on order in which
     * vertices (possibly successors) are iterated on in the algorithm.
     * More precisely, for some iterations orders, optimized algorithm finds
     * less cycles (but which still cover all edges).
     */
    
    public void test_computeShortestCycles_general_0_1_2_3_sameResultUnlessSorting() {

        /*
         * Graph (k = vertex of index and id k).
         * 
         *   1
         *   ^\
         *  /| v
         * 0 | 2
         * ^ |/
         *  \v
         *   3
         * 
         * All cycles:
         * [1,3]
         * [0,1,3]
         * [1,2,3]
         * [0,1,2,3]
         */
        
        final ArrayList<InterfaceVertex> vByIndex = new ArrayList<InterfaceVertex>();
        GraphTestsUtilz.newInGraph(vByIndex, 0);
        GraphTestsUtilz.newInGraph(vByIndex, 1);
        GraphTestsUtilz.newInGraph(vByIndex, 2);
        GraphTestsUtilz.newInGraph(vByIndex, 3);
        GraphTestsUtilz.ensurePathFromIndexes(vByIndex, 0, 1, 2, 3, 0);
        GraphTestsUtilz.ensurePathFromIndexes(vByIndex, 1, 3, 1);

        final Random random = new Random(SEED);

        for (int k = 0; k < 100; k++) {
            @SuppressWarnings("unchecked")
            final List<InterfaceVertex> graph = (List<InterfaceVertex>) vByIndex.clone();

            // Result won't depend on vertices order in input collection.
            shuffle(random, graph);

            if (DEBUG) {
                GraphTestsUtilz.printGraph(graph);
            }

            for (boolean mustUseRefAlgo : new boolean[]{false,true}) {

                if (DEBUG) {
                    System.out.println("mustUseRefAlgo = " + mustUseRefAlgo);
                }

                final MyCycleComputerVcp processor = new MyCycleComputerVcp();
                if (mustUseRefAlgo) {
                    RefShortestCyclesComputer.computeShortestCycles(graph, -1, processor);
                } else {
                    ShortestCyclesComputer.computeShortestCycles(graph, -1, processor);
                }

                final List<List<InterfaceVertex>> expectedCycleList = new ArrayList<List<InterfaceVertex>>();
                if (mustUseRefAlgo
                        || (!ShortestCyclesComputer.MUST_SORT_VERTICES)) {
                    expectedCycleList.add(GraphTestsUtilz.asListFromIndexes(vByIndex, 1, 3));
                }
                expectedCycleList.add(GraphTestsUtilz.asListFromIndexes(vByIndex, 0, 1, 3));
                expectedCycleList.add(GraphTestsUtilz.asListFromIndexes(vByIndex, 1, 2, 3));

                checkEqualAfterNormalization(expectedCycleList, processor.cycleList);
            }
        }
    }

    public void test_computeShortestCycles_general_0_1_3_2_sameResultUnlessSorting() {

        /*
         * Graph (k = vertex of index and id k).
         * 
         *   1
         *   ^\
         *  /| v
         * 0 | 3
         * ^ |/
         *  \v
         *   2
         * 
         * All cycles:
         * [1,2]
         * [0,1,2]
         * [1,3,2]
         * [0,1,3,2]
         */
        
        final ArrayList<InterfaceVertex> vByIndex = new ArrayList<InterfaceVertex>();
        GraphTestsUtilz.newInGraph(vByIndex, 0);
        GraphTestsUtilz.newInGraph(vByIndex, 1);
        GraphTestsUtilz.newInGraph(vByIndex, 2);
        GraphTestsUtilz.newInGraph(vByIndex, 3);
        GraphTestsUtilz.ensurePathFromIndexes(vByIndex, 0, 1, 3, 2, 0);
        GraphTestsUtilz.ensurePathFromIndexes(vByIndex, 1, 2, 1);

        final Random random = new Random(SEED);

        for (int k = 0; k < 100; k++) {
            @SuppressWarnings("unchecked")
            final List<InterfaceVertex> graph = (List<InterfaceVertex>) vByIndex.clone();

            // Result won't depend on vertices order in input collection.
            shuffle(random, graph);

            if (DEBUG) {
                GraphTestsUtilz.printGraph(graph);
            }

            for (boolean mustUseRefAlgo : new boolean[]{false,true}) {

                if (DEBUG) {
                    System.out.println("mustUseRefAlgo = " + mustUseRefAlgo);
                }

                final MyCycleComputerVcp processor = new MyCycleComputerVcp();
                if (mustUseRefAlgo) {
                    RefShortestCyclesComputer.computeShortestCycles(graph, -1, processor);
                } else {
                    ShortestCyclesComputer.computeShortestCycles(graph, -1, processor);
                }

                final List<List<InterfaceVertex>> expectedCycleList = new ArrayList<List<InterfaceVertex>>();
                if (mustUseRefAlgo
                        || (!ShortestCyclesComputer.MUST_SORT_VERTICES)) {
                    expectedCycleList.add(GraphTestsUtilz.asListFromIndexes(vByIndex, 1, 2));
                }
                expectedCycleList.add(GraphTestsUtilz.asListFromIndexes(vByIndex, 0, 1, 2));
                expectedCycleList.add(GraphTestsUtilz.asListFromIndexes(vByIndex, 1, 3, 2));

                checkEqualAfterNormalization(expectedCycleList, processor.cycleList);
            }
        }
    }

    public void test_computeShortestCycles_general_0_3_2_1_differentResult() {

        /*
         * Graph (k = vertex of index and id k).
         * 
         *   3
         *   ^\
         *  /| v
         * 0 | 2
         * ^ |/
         *  \v
         *   1
         * 
         * All cycles:
         * [1,3]
         * [0,3,1]
         * [1,3,2]
         * [0,3,2,1]
         */
        
        final ArrayList<InterfaceVertex> vByIndex = new ArrayList<InterfaceVertex>();
        GraphTestsUtilz.newInGraph(vByIndex, 0);
        GraphTestsUtilz.newInGraph(vByIndex, 1);
        GraphTestsUtilz.newInGraph(vByIndex, 2);
        GraphTestsUtilz.newInGraph(vByIndex, 3);
        GraphTestsUtilz.ensurePathFromIndexes(vByIndex, 0, 3, 2, 1, 0);
        GraphTestsUtilz.ensurePathFromIndexes(vByIndex, 1, 3, 1);
        
        final Random random = new Random(SEED);
        
        for (int k = 0; k < 100; k++) {
            @SuppressWarnings("unchecked")
            final List<InterfaceVertex> graph = (List<InterfaceVertex>) vByIndex.clone();
            
            // Result won't depend on vertices order in input collection.
            shuffle(random, graph);

            if (DEBUG) {
                GraphTestsUtilz.printGraph(graph);
            }

            for (boolean mustUseRefAlgo : new boolean[]{false,true}) {
                
                if (DEBUG) {
                    System.out.println("mustUseRefAlgo = " + mustUseRefAlgo);
                }
                
                final MyCycleComputerVcp processor = new MyCycleComputerVcp();
                if (mustUseRefAlgo) {
                    RefShortestCyclesComputer.computeShortestCycles(graph, -1, processor);
                } else {
                    ShortestCyclesComputer.computeShortestCycles(graph, -1, processor);
                }
                
                final List<List<InterfaceVertex>> expectedCycleList = new ArrayList<List<InterfaceVertex>>();
                if (mustUseRefAlgo) {
                    expectedCycleList.add(GraphTestsUtilz.asListFromIndexes(vByIndex, 1, 3));
                }
                expectedCycleList.add(GraphTestsUtilz.asListFromIndexes(vByIndex, 0, 3, 1));
                expectedCycleList.add(GraphTestsUtilz.asListFromIndexes(vByIndex, 1, 3, 2));
                
                checkEqualAfterNormalization(expectedCycleList, processor.cycleList);
            }
        }
    }

    public void test_computeShortestCycles_general_0_3_1_2_differentResult() {

        /*
         * Graph (k = vertex of index and id k).
         * 
         *   3
         *   ^\
         *  /| v
         * 0 | 1
         * ^ |/
         *  \v
         *   2
         * 
         * All cycles:
         * [2,3]
         * [0,3,2]
         * [1,2,3]
         * [0,3,1,2]
         */
        
        final ArrayList<InterfaceVertex> vByIndex = new ArrayList<InterfaceVertex>();
        GraphTestsUtilz.newInGraph(vByIndex, 0);
        GraphTestsUtilz.newInGraph(vByIndex, 1);
        GraphTestsUtilz.newInGraph(vByIndex, 2);
        GraphTestsUtilz.newInGraph(vByIndex, 3);
        GraphTestsUtilz.ensurePathFromIndexes(vByIndex, 0, 3, 1, 2, 0);
        GraphTestsUtilz.ensurePathFromIndexes(vByIndex, 2, 3, 2);
        
        final Random random = new Random(SEED);
        
        for (int k = 0; k < 100; k++) {
            @SuppressWarnings("unchecked")
            final List<InterfaceVertex> graph = (List<InterfaceVertex>) vByIndex.clone();
            
            // Result won't depend on vertices order in input collection.
            shuffle(random, graph);

            if (DEBUG) {
                GraphTestsUtilz.printGraph(graph);
            }

            for (boolean mustUseRefAlgo : new boolean[]{false,true}) {
                
                if (DEBUG) {
                    System.out.println("mustUseRefAlgo = " + mustUseRefAlgo);
                }
                
                final MyCycleComputerVcp processor = new MyCycleComputerVcp();
                if (mustUseRefAlgo) {
                    RefShortestCyclesComputer.computeShortestCycles(graph, -1, processor);
                } else {
                    ShortestCyclesComputer.computeShortestCycles(graph, -1, processor);
                }
                
                final List<List<InterfaceVertex>> expectedCycleList = new ArrayList<List<InterfaceVertex>>();
                if (mustUseRefAlgo) {
                    expectedCycleList.add(GraphTestsUtilz.asListFromIndexes(vByIndex, 2, 3));
                }
                expectedCycleList.add(GraphTestsUtilz.asListFromIndexes(vByIndex, 0, 3, 2));
                expectedCycleList.add(GraphTestsUtilz.asListFromIndexes(vByIndex, 1, 2, 3));
                
                checkEqualAfterNormalization(expectedCycleList, processor.cycleList);
            }
        }
    }
    
    /*
     * General: cases where multiple shortest cycles of same size could be
     * computed, but not all of them are.
     */

    public void test_computeShortestCycles_general_sameSizeShortestCycles() {

        /*
         * Graph (k = vertex of index and id k).
         * 
         *    -> 1 <-
         *   /  / \  \
         *  /  v   v  \
         * 0   4   5   2
         *  ^   \ /   ^
         *   \   v   /
         *    -- 3 --
         * 
         * All cycles:
         * [0,1,4,3]
         * [0,1,5,3]
         * [1,4,3,2]
         * [1,5,3,2]
         */
        
        final ArrayList<InterfaceVertex> vByIndex = new ArrayList<InterfaceVertex>();
        GraphTestsUtilz.newInGraph(vByIndex, 0);
        GraphTestsUtilz.newInGraph(vByIndex, 1);
        GraphTestsUtilz.newInGraph(vByIndex, 2);
        GraphTestsUtilz.newInGraph(vByIndex, 3);
        GraphTestsUtilz.newInGraph(vByIndex, 4);
        GraphTestsUtilz.newInGraph(vByIndex, 5);
        GraphTestsUtilz.ensurePathFromIndexes(vByIndex, 0, 1, 4, 3, 0);
        GraphTestsUtilz.ensurePathFromIndexes(vByIndex, 2, 1, 5, 3, 2);

        final Random random = new Random(SEED);

        for (int k = 0; k < 100; k++) {
            @SuppressWarnings("unchecked")
            final List<InterfaceVertex> graph = (List<InterfaceVertex>) vByIndex.clone();

            // Result won't depend on vertices order in input collection.
            shuffle(random, graph);

            if (DEBUG) {
                GraphTestsUtilz.printGraph(graph);
            }

            for (boolean mustUseRefAlgo : new boolean[]{false,true}) {

                if (DEBUG) {
                    System.out.println("mustUseRefAlgo = " + mustUseRefAlgo);
                }

                final MyCycleComputerVcp processor = new MyCycleComputerVcp();
                if (mustUseRefAlgo) {
                    RefShortestCyclesComputer.computeShortestCycles(graph, -1, processor);
                } else {
                    ShortestCyclesComputer.computeShortestCycles(graph, -1, processor);
                }

                final List<List<InterfaceVertex>> expectedCycleList = new ArrayList<List<InterfaceVertex>>();
                expectedCycleList.add(GraphTestsUtilz.asListFromIndexes(vByIndex, 0, 1, 4, 3));
                expectedCycleList.add(GraphTestsUtilz.asListFromIndexes(vByIndex, 0, 1, 5, 3));
                expectedCycleList.add(GraphTestsUtilz.asListFromIndexes(vByIndex, 1, 4, 3, 2));
                if (false) {
                    /*
                     * This one is never computed by either algorithm.
                     * 
                     * It's normal even for paper algorithm, because it just
                     * look for shortest cycles using all predecessors, not
                     * using all possible paths to these predecessors.
                     */
                    expectedCycleList.add(GraphTestsUtilz.asListFromIndexes(vByIndex, 1, 5, 3, 2));
                }

                checkEqualAfterNormalization(expectedCycleList, processor.cycleList);
            }
        }
    }

    /*
     * General and sturdiness.
     */
    
    public void test_computeShortestCycles_general_sturdiness_againstNaive() {
        final Random random = new Random(SEED);
        for (int maxGraphSize = 2; maxGraphSize < 15; maxGraphSize++) {
            for (int i = 0; i < 10*1000; i++) {
                final int maxCycleSize = -1 + random.nextInt(10);
                this.test_computeShortestCycles_againstNaive(
                        new RandomGraphGenerator(
                                random.nextLong(),
                                maxGraphSize),
                                maxCycleSize);
            }
        }
    }
    
    public void test_computeShortestCycles_general_sturdiness_againstRef() {
        final Random random = new Random(SEED);
        for (int maxGraphSize = 2; maxGraphSize < 15; maxGraphSize++) {
            for (int i = 0; i < 10*1000; i++) {
                final int maxCycleSize = -1 + random.nextInt(10);
                this.test_computeShortestCycles_againstRef(
                        new RandomGraphGenerator(
                                random.nextLong(),
                                maxGraphSize),
                                maxCycleSize);
            }
        }
    }
    
    public void test_computeShortestCycles_noStackOverflowError() {
        final Random random = new Random(SEED);
        
        final CycleGraphGenerator gg = new CycleGraphGenerator(
                random.nextLong(),
                GraphTestsUtilz.LARGER_THAN_CALL_STACK);
        
        final Collection<InterfaceVertex> graph = gg.newGraph();
        
        final MyCycleComputerVcp processor = new MyCycleComputerVcp();
        if (false) {
            // No longer need that with optimized algorithm.
            
            // Giving up after first (and only) cycle, else takes huge time
            // due to algorithm complexity.
            processor.nbrOfCyclesUntilStop = 1;
        }
        ShortestCyclesComputer.computeShortestCycles(graph, -1, processor);
        
        assertEquals(1, processor.cycleList.size());
    }
    
    /*
     * Determinism.
     */
    
    /**
     * Checks that vertices hash codes have no effect on the result,
     * i.e. that ordered sets or maps are used where needed.
     */
    public void test_computeShortestCycles_determinism() {
        
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
                ShortestCyclesComputer.computeShortestCycles(graph, maxCycleSize, processor);
                
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
     * Tests that we only find cycles that exist, and that include all edges
     * of all cycles of same or lower size.
     */
    private void test_computeShortestCycles_againstNaive(
            InterfaceGraphGenerator gg,
            int maxSize) {

        if (DEBUG) {
            System.out.println("test_computeShortestCycles_againstNaive(" + gg + ",maxSize=" + maxSize + ")");
        }
        
        final Collection<InterfaceVertex> graph = gg.newGraph();
        
        if (DEBUG) {
            GraphTestsUtilz.printGraph(graph);
        }
        
        final TreeSet<ComparableVertexArrayList> allCycles;
        {
            final MyCycleComputerVcp processor = new MyCycleComputerVcp();
            NaiveCyclesComputer.computeCycles(graph, maxSize, processor);
            allCycles = GraphTestsUtilz.toNormalizedCyclesAsLists(processor.cycleList);
            
            // Only one call per cycle.
            assertEquals(allCycles.size(), processor.cycleList.size());

            if (DEBUG) {
                System.out.println();
                System.out.println("allCycles:");
                for (ComparableVertexArrayList cycle : allCycles) {
                    System.out.println(cycle);
                }
            }
        }
        
        final TreeSet<ComparableVertexArrayList> shortestCycles;
        {
            final MyCycleComputerVcp processor = new MyCycleComputerVcp();
            ShortestCyclesComputer.computeShortestCycles(graph, maxSize, processor);
            shortestCycles = GraphTestsUtilz.toNormalizedCyclesAsLists(processor.cycleList);
            
            // Only one call per cycle.
            assertEquals(shortestCycles.size(), processor.cycleList.size());

            if (DEBUG) {
                System.out.println();
                System.out.println("shortestCycles:");
                for (ComparableVertexArrayList cycle : shortestCycles) {
                    System.out.println(cycle);
                }
            }
        }

        checkContained(allCycles, shortestCycles);
        
        if (shortestCycles.size() != allCycles.size()) {
            checkShortestCyclesContainAllCyclesEdges(allCycles, shortestCycles);
        } else {
            // Same cycles, so nothing to check.
        }
    }
    
    /**
     * Tests that we don't compute cycles that reference algorithm doesn't compute,
     * i.e. that we do at least as good as it.
     */
    private void test_computeShortestCycles_againstRef(
            InterfaceGraphGenerator gg,
            int maxSize) {

        if (DEBUG) {
            System.out.println("test_computeShortestCycles_againstRef(" + gg + ",maxSize=" + maxSize + ")");
        }
        
        final Collection<InterfaceVertex> graph = gg.newGraph();
        
        if (DEBUG) {
            GraphTestsUtilz.printGraph(graph);
        }
        
        final TreeSet<ComparableVertexArrayList> refCycles;
        {
            final MyCycleComputerVcp processor = new MyCycleComputerVcp();
            RefShortestCyclesComputer.computeShortestCycles(graph, maxSize, processor);
            refCycles = GraphTestsUtilz.toNormalizedCyclesAsLists(processor.cycleList);
            
            // Only one call per cycle.
            assertEquals(refCycles.size(), processor.cycleList.size());

            if (DEBUG) {
                System.out.println();
                System.out.println("refCycles:");
                for (ComparableVertexArrayList cycle : refCycles) {
                    System.out.println(cycle);
                }
            }
        }
        
        final TreeSet<ComparableVertexArrayList> actualCycles;
        {
            final MyCycleComputerVcp processor = new MyCycleComputerVcp();
            ShortestCyclesComputer.computeShortestCycles(graph, maxSize, processor);
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

        if (false) {
            // Use that to find out cases where optimized algorithm
            // finds a different result.
            checkEqual(refCycles, actualCycles);
        }
        
        checkContained(refCycles, actualCycles);
    }
    
    /*
     * 
     */

    /**
     * Spec doesn't say that invariant by input shuffling,
     * but as long as it works like that we test it.
     */
    private static void shuffle(
            Random random,
            List<InterfaceVertex> graph) {
        final int size = graph.size();
        for (int i = 0; i < size; i++) {
            final int j = random.nextInt(size); // ok if size = 0, since won't pass here
            if (j != i) {
                final InterfaceVertex v = graph.get(i);
                graph.set(i, graph.get(j));
                graph.set(j, v);
            }
        }
    }

    /*
     * 
     */
    
    private static void checkEqualAfterNormalization(
            List<List<InterfaceVertex>> expectedCycles,
            List<List<InterfaceVertex>> actualCycles) {
        
        final TreeSet<ComparableVertexArrayList> expected =
                GraphTestsUtilz.toNormalizedCyclesAsLists(expectedCycles);
        final TreeSet<ComparableVertexArrayList> actual =
                GraphTestsUtilz.toNormalizedCyclesAsLists(actualCycles);
        
        checkEqual(expected, actual);
    }
    
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

    private static void checkContained(
            TreeSet<ComparableVertexArrayList> whole,
            TreeSet<ComparableVertexArrayList> part) {
        GraphTestsUtilz.checkConsistent(whole);
        GraphTestsUtilz.checkConsistent(part);

        if (DEBUG) {
            System.out.println();
            System.out.println("whole:");
            for (ComparableVertexArrayList cycle : whole) {
                System.out.println(cycle);
            }
        }
        
        if (DEBUG) {
            System.out.println();
            System.out.println("part:");
            for (ComparableVertexArrayList cycle : part) {
                System.out.println(cycle);
            }
        }
        
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
    
    private static void checkShortestCyclesContainAllCyclesEdges(
            TreeSet<ComparableVertexArrayList> allCycles,
            TreeSet<ComparableVertexArrayList> shortestCycles) {
        
        final TreeSet<String> expected = computeEdgeSet(allCycles);
        final TreeSet<String> actual = computeEdgeSet(shortestCycles);
        
        final boolean equal = expected.equals(actual);
        if (!equal) {
            {
                final TreeSet<String> missings = new TreeSet<String>(expected);
                missings.removeAll(actual);
                if (missings.size() != 0) {
                    for (String missing : missings) {
                        System.out.println("missing : " + missing);
                    }
                }
            }
            {
                final TreeSet<String> exceedings = new TreeSet<String>(actual);
                exceedings.removeAll(expected);
                if (exceedings.size() != 0) {
                    for (String exceeding : exceedings) {
                        System.out.println("exceeding : " + exceeding);
                    }
                }
            }
        }
        assertTrue(equal);
    }
    
    /**
     * @return Edges set, edges being "from->to" strings.
     */
    private static TreeSet<String> computeEdgeSet(TreeSet<ComparableVertexArrayList> cycleSet) {
        final TreeSet<String> edgeSet = new TreeSet<String>();
        for (ComparableVertexArrayList cycle : cycleSet) {
            final InterfaceVertex first = cycle.get(0);
            InterfaceVertex from = first;
            for (int i = 1; i < cycle.size(); i++) {
                final InterfaceVertex to = cycle.get(i);
                edgeSet.add(from + "->" + to);
                from = to;
            }
            edgeSet.add(from + "->" + first);
        }
        return edgeSet;
    }
}
