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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;
import net.jadecy.graph.GraphTestsUtilz.ChainGraphGenerator;
import net.jadecy.graph.GraphTestsUtilz.CycleGraphGenerator;
import net.jadecy.graph.GraphTestsUtilz.DisconnectedGraphGenerator;
import net.jadecy.graph.GraphTestsUtilz.InterfaceGraphGenerator;
import net.jadecy.graph.GraphTestsUtilz.RakeCycleGraphGenerator;
import net.jadecy.graph.GraphTestsUtilz.RandomGraphGenerator;
import net.jadecy.graph.GraphTestsUtilz.Vertex;

public class ReachabilityComputerTest extends TestCase {
    
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

    private static class MyVertexCollProcessor implements InterfaceVertexCollProcessor {
        static final Integer BEGIN = 1;
        static final Integer PROCESS = 2;
        static final Integer END = 3;
        private static final int INVALID_MAX_COLL_COUNT = -1;
        private final int maxCollCount;
        int collCount = 0;
        final List<List<InterfaceVertex>> stepListList = new ArrayList<List<InterfaceVertex>>();
        final ArrayList<Integer> callList = new ArrayList<Integer>();
        /**
         * To check ordering and unicity.
         */
        InterfaceVertex collPrev = null;
        /**
         * To check unicity among steps.
         */
        final TreeSet<InterfaceVertex> totalSet = new TreeSet<InterfaceVertex>();
        /**
         * Never asks for stop.
         */
        public MyVertexCollProcessor() {
            this.maxCollCount = INVALID_MAX_COLL_COUNT;
        }
        /**
         * Asks for stop after maxCollCount processings of collections (must be > 0).
         */
        public MyVertexCollProcessor(int maxCollCount) {
            if (maxCollCount <= 0) {
                throw new IllegalArgumentException();
            }
            this.maxCollCount = maxCollCount;
        }
        @Override
        public String toString() {
            return "[step list list = " + this.stepListList + "]";
        }
        @Override
        public void processCollBegin() {
            assertTrue((this.callList.size() == 0) || (this.getLastCall() == END));
            this.callList.add(BEGIN);
            this.stepListList.add(new ArrayList<InterfaceVertex>());
            this.collPrev = null;
        }
        @Override
        public void processCollVertex(InterfaceVertex vertex) {
            assertTrue((this.getLastCall() == BEGIN) || (this.getLastCall() == PROCESS));
            this.callList.add(PROCESS);
            {
                final List<InterfaceVertex> lastStep = this.stepListList.get(this.stepListList.size()-1);
                lastStep.add(vertex);
            }
            {
                if (this.collPrev != null) {
                    if (this.collPrev.compareTo(vertex) >= 0) {
                        throw new AssertionError(this.collPrev + " >= " + vertex + " (bad ordering)");
                    }
                }
                this.collPrev = vertex;
            }
            {
                // Checking that vertex is always different from others (across all steps).
                final boolean didAdd = this.totalSet.add(vertex);
                assertTrue(didAdd);
            }
        }
        @Override
        public boolean processCollEnd() {
            assertTrue((this.getLastCall() == BEGIN) || (this.getLastCall() == PROCESS));
            this.callList.add(END);
            this.collCount++;
            if (this.maxCollCount != INVALID_MAX_COLL_COUNT) {
                return (this.collCount >= this.maxCollCount);
            } else {
                return false;
            }
        }
        Integer getFirstCall() {
            return this.callList.get(0);
        }
        Integer getLastCall() {
            return this.callList.get(this.callList.size()-1);
        }
    }

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /*
     * Special cases.
     */
    
    public void test_computeReachability_exceptions() {

        final Set<InterfaceVertex> beginVertexSet = GraphTestsUtilz.asHashSet();
        final boolean mustIncludeBeginVertices = false;
        final boolean mustIncludeReachedBeginVertices = false;
        final MyVertexCollProcessor processor = new MyVertexCollProcessor();
        
        try {
            ReachabilityComputer.computeReachability(
                    null,
                    mustIncludeBeginVertices,
                    mustIncludeReachedBeginVertices,
                    processor);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            ReachabilityComputer.computeReachability(
                    beginVertexSet,
                    mustIncludeBeginVertices,
                    mustIncludeReachedBeginVertices,
                    null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
    }
    
    public void test_computeReachability_noOrZeroStepReach() {
        
        final Vertex v1 = new Vertex(1);
        final Vertex v2 = new Vertex(2);
        
        GraphTestsUtilz.ensurePath(v1, v2);
        
        for (boolean emptyBegin : new boolean[]{false,true}) {
            
            final Set<InterfaceVertex> beginVertexSet = emptyBegin ? GraphTestsUtilz.asHashSet() : GraphTestsUtilz.asHashSet(v2);

            if (DEBUG) {
                System.out.println("beginVertexSet = " + beginVertexSet);
            }

            for (boolean mustIncludeBeginVertices : new boolean[]{false,true}) {
                for (boolean mustIncludeReachedBeginVertices : new boolean[]{false,true}) {

                    if (DEBUG) {
                        System.out.println("emptyBegin = " + emptyBegin);
                        System.out.println("mustIncludeBeginVertices = " + mustIncludeBeginVertices);
                        System.out.println("mustIncludeReachedBeginVertices = " + mustIncludeReachedBeginVertices);
                    }

                    final MyVertexCollProcessor processor = new MyVertexCollProcessor();
                    ReachabilityComputer.computeReachability(
                            beginVertexSet,
                            mustIncludeBeginVertices,
                            mustIncludeReachedBeginVertices,
                            processor);

                    if (DEBUG) {
                        System.out.println("processor = " + processor);
                    }

                    if (emptyBegin) {
                        assertEquals(0, processor.callList.size());
                    } else {
                        assertEquals(MyVertexCollProcessor.BEGIN, processor.getFirstCall());
                        assertEquals(MyVertexCollProcessor.END, processor.getLastCall());
                        if (mustIncludeBeginVertices) {
                            // step 0: contains v2.
                            // Must have processed begin vertices (i.e. v2),
                            // but not the empty set of what could be reached
                            // from there.
                            assertEquals(3, processor.callList.size());
                            assertEquals(1, processor.stepListList.get(0).size());
                            assertEquals(1, processor.totalSet.size());
                            assertTrue(processor.totalSet.contains(v2));
                        } else {
                            // step 0: empty.
                            assertEquals(2, processor.callList.size());
                        }
                    }
                }
            }
        }
    }
    
    public void test_computeReachability_earlyStop() {
        
        final int size = 4;
        
        final ChainGraphGenerator gg = new ChainGraphGenerator(SEED, size);
        
        final List<InterfaceVertex> graph = gg.newGraph();
        
        if (DEBUG) {
            GraphTestsUtilz.printGraph(graph);
        }

        final InterfaceVertex firstVertex = graph.get(0);
        
        final Set<InterfaceVertex> beginVertexSet = GraphTestsUtilz.asHashSet(firstVertex);
        
        if (DEBUG) {
            System.out.println("beginVertexSet = " + beginVertexSet);
        }

        for (int maxCollCount = 1; maxCollCount < size; maxCollCount++) {
            for (boolean mustIncludeBeginVertices : new boolean[]{false,true}) {
                for (boolean mustIncludeReachedBeginVertices : new boolean[]{false,true}) {

                    if (DEBUG) {
                        System.out.println("maxCollCount = " + maxCollCount);
                        System.out.println("mustIncludeBeginVertices = " + mustIncludeBeginVertices);
                        System.out.println("mustIncludeReachedBeginVertices = " + mustIncludeReachedBeginVertices);
                    }
                    
                    final MyVertexCollProcessor processor = new MyVertexCollProcessor(maxCollCount);
                    ReachabilityComputer.computeReachability(
                            beginVertexSet,
                            mustIncludeBeginVertices,
                            mustIncludeReachedBeginVertices,
                            processor);
                    
                    assertEquals(maxCollCount, processor.collCount);
                }
            }
        }
    }

    /*
     * General.
     */

    public void test_computeReachability_general() {
        
        /*
         * Graph (k = vertex of index k, whatever its id).
         * 0 <-> 2 --> 4 --> 6 --> 8
         *        ^         ^
         *         \       /
         *          \     /
         *           \   /
         *            \ v
         * 1 --> 3 --> 5 --> 7 --> 9
         * 
         * begin = {3,4}
         */
        
        final DisconnectedGraphGenerator gg = new DisconnectedGraphGenerator(SEED, 10);
        final List<InterfaceVertex> graph = gg.newGraph();
        GraphTestsUtilz.ensurePathFromIndexes(graph, 0, 2, 4, 6, 8);
        GraphTestsUtilz.ensurePathFromIndexes(graph, 1, 3, 5, 7, 9);
        GraphTestsUtilz.ensurePathFromIndexes(graph, 2, 0);
        GraphTestsUtilz.ensurePathFromIndexes(graph, 5, 2);
        GraphTestsUtilz.ensurePathFromIndexes(graph, 5, 6);
        GraphTestsUtilz.ensurePathFromIndexes(graph, 6, 5);

        final Set<InterfaceVertex> beginVertexSet = GraphTestsUtilz.asSetFromIndexes(graph, 3, 4);

        if (DEBUG) {
            System.out.println("beginVertexSet = " + beginVertexSet);
        }

        for (boolean mustIncludeBeginVertices : new boolean[]{false,true}) {
            for (boolean mustIncludeReachedBeginVertices : new boolean[]{false,true}) {

                if (DEBUG) {
                    System.out.println("mustIncludeBeginVertices = " + mustIncludeBeginVertices);
                    System.out.println("mustIncludeReachedBeginVertices = " + mustIncludeReachedBeginVertices);
                }
                
                final MyVertexCollProcessor processor = new MyVertexCollProcessor();
                ReachabilityComputer.computeReachability(
                        beginVertexSet,
                        mustIncludeBeginVertices,
                        mustIncludeReachedBeginVertices,
                        processor);

                if (DEBUG) {
                    System.out.println("processor = " + processor);
                }

                assertEquals(MyVertexCollProcessor.BEGIN, processor.getFirstCall());
                assertEquals(MyVertexCollProcessor.END, processor.getLastCall());
                
                final List<List<InterfaceVertex>> expectedListList;

                if (mustIncludeBeginVertices) {
                    expectedListList = Arrays.asList(
                            GraphTestsUtilz.asListFromIndexes(graph, 3, 4), // step 0
                            GraphTestsUtilz.asListFromIndexes(graph, 5, 6), // step 1
                            GraphTestsUtilz.asListFromIndexes(graph, 2, 7, 8), // step 2
                            GraphTestsUtilz.asListFromIndexes(graph, 0, 9)); // step 3
                } else {
                    if (mustIncludeReachedBeginVertices) {
                        expectedListList = Arrays.asList(
                                GraphTestsUtilz.asListFromIndexes(graph), // step 0
                                GraphTestsUtilz.asListFromIndexes(graph, 5, 6), // step 1
                                GraphTestsUtilz.asListFromIndexes(graph, 2, 7, 8), // step 2
                                GraphTestsUtilz.asListFromIndexes(graph, 0, 4, 9)); // step 3
                    } else {
                        expectedListList = Arrays.asList(
                                GraphTestsUtilz.asListFromIndexes(graph), // step 0
                                GraphTestsUtilz.asListFromIndexes(graph, 5, 6), // step 1
                                GraphTestsUtilz.asListFromIndexes(graph, 2, 7, 8), // step 2
                                GraphTestsUtilz.asListFromIndexes(graph, 0, 9)); // step 3
                    }
                }
                
                final List<List<InterfaceVertex>> actualListList = processor.stepListList;

                assertEquals(expectedListList, actualListList);
            }
        }
    }
    
    /*
     * Sturdiness.
     */

    public void test_computeReachability_sturdiness() {
        
        final Random random = new Random(SEED);
        
        for (int i = 0; i < 10*1000; i++) {
            final int maxSize = 9;
            final RandomGraphGenerator gg = new RandomGraphGenerator(
                    random.nextLong(),
                    maxSize);
            
            this.test_computeFullReachability_againstNaive(
                    random,
                    gg);
        }
    }

    public void test_computeReachability_noStackOverflowError() {

        final Random random = new Random(SEED);
        
        final int size = GraphTestsUtilz.LARGER_THAN_CALL_STACK;
        
        for (int k = 0; k < 2; k++) {
            
            if (DEBUG) {
                System.out.println("k = " + k);
            }

            final InterfaceGraphGenerator gg;
            if (k == 0) {
                gg = new CycleGraphGenerator(
                        random.nextLong(),
                        size);
            } else {
                gg = new RakeCycleGraphGenerator(
                        random.nextLong(),
                        size);
            }

            final List<InterfaceVertex> graph = gg.newGraph();

            final InterfaceVertex beginVertex = graph.get(0);

            final Set<InterfaceVertex> beginVertexSet = GraphTestsUtilz.asHashSet(beginVertex);

            if (DEBUG) {
                System.out.println("beginVertexSet = " + beginVertexSet);
            }

            for (boolean mustIncludeBeginVertices : new boolean[]{false,true}) {
                for (boolean mustIncludeReachedBeginVertices : new boolean[]{false,true}) {

                    if (DEBUG) {
                        System.out.println("mustIncludeBeginVertices = " + mustIncludeBeginVertices);
                        System.out.println("mustIncludeReachedBeginVertices = " + mustIncludeReachedBeginVertices);
                    }

                    final MyVertexCollProcessor processor = new MyVertexCollProcessor();
                    ReachabilityComputer.computeReachability(
                            beginVertexSet,
                            mustIncludeBeginVertices,
                            mustIncludeReachedBeginVertices,
                            processor);

                    assertEquals(MyVertexCollProcessor.BEGIN, processor.getFirstCall());
                    assertEquals(MyVertexCollProcessor.END, processor.getLastCall());

                    final List<List<InterfaceVertex>> actualListList = processor.stepListList;

                    if (k == 0) {
                        if ((!mustIncludeBeginVertices) && mustIncludeReachedBeginVertices) {
                            // step 0 empty, and have one more step being reach of first vertex.
                            assertEquals(size + 1, actualListList.size());
                        } else {
                            assertEquals(size, actualListList.size());
                        }
                    } else {
                        if ((!mustIncludeBeginVertices) && mustIncludeReachedBeginVertices) {
                            assertEquals(3, actualListList.size());
                        } else {
                            assertEquals(2, actualListList.size());
                        }
                    }
                }
            }
        }
    }

    /*
     * Determinism.
     */
    
    /**
     * Checks that vertices hash codes have no effect on the result,
     * i.e. that ordered sets or maps are used where needed.
     */
    public void test_computeReachability_determinism() {
        
        for (boolean mustIncludeBeginVertices : new boolean[]{false,true}) {
            for (boolean mustIncludeReachedBeginVertices : new boolean[]{false,true}) {
                
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
                        
                        final HashSet<InterfaceVertex> beginVertexSet = new HashSet<InterfaceVertex>();
                        for (InterfaceVertex v : graph) {
                            if (random.nextDouble() < 0.2) {
                                beginVertexSet.add(v);
                            }
                        }
                        
                        final MyVertexCollProcessor processor = new MyVertexCollProcessor();
                        ReachabilityComputer.computeReachability(
                                beginVertexSet,
                                mustIncludeBeginVertices,
                                mustIncludeReachedBeginVertices,
                                processor);
                        
                        final List<List<InterfaceVertex>> result = processor.stepListList;
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
        }
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    private void test_computeFullReachability_againstNaive(
            Random random,
            InterfaceGraphGenerator gg) {
        
        final List<InterfaceVertex> graph = gg.newGraph();
        
        if (DEBUG) {
            GraphTestsUtilz.printGraph(graph);
        }
        
        final Set<InterfaceVertex> beginVertexSet;
        if ((graph.size() == 0) || (random.nextDouble() < 0.1)) {
            beginVertexSet = GraphTestsUtilz.asHashSet();
        } else {
            final InterfaceVertex beginVertex1 = graph.get(0);
            final InterfaceVertex beginVertex2 = graph.get(graph.size()/2);
            beginVertexSet = GraphTestsUtilz.asHashSet(beginVertex1, beginVertex2);
        }

        final int beginVertexSetHC = beginVertexSet.hashCode();
        
        if (DEBUG) {
            System.out.println("beginVertexSet = " + beginVertexSet);
        }

        for (boolean mustIncludeBeginVertices : new boolean[]{false,true}) {
            for (boolean mustIncludeReachedBeginVertices : new boolean[]{false,true}) {

                if (DEBUG) {
                    System.out.println("mustIncludeBeginVertices = " + mustIncludeBeginVertices);
                    System.out.println("mustIncludeReachedBeginVertices = " + mustIncludeReachedBeginVertices);
                }

                final TreeSet<InterfaceVertex> expectedSet;
                {
                    final MyVertexCollProcessor processor = new MyVertexCollProcessor();
                    NaiveFullReachabilityComputer.computeFullReachability(
                            beginVertexSet,
                            mustIncludeBeginVertices,
                            mustIncludeReachedBeginVertices,
                            processor);
                    
                    assertEquals(beginVertexSetHC, beginVertexSet.hashCode());
                    
                    if (processor.callList.size() != 0) {
                        assertEquals(MyVertexCollProcessor.BEGIN, processor.getFirstCall());
                        assertEquals(MyVertexCollProcessor.END, processor.getLastCall());
                    }

                    expectedSet = processor.totalSet;

                    if (DEBUG) {
                        System.out.println("expected reachability = " + expectedSet);
                    }
                }

                final TreeSet<InterfaceVertex> actualSet;
                {
                    final MyVertexCollProcessor processor = new MyVertexCollProcessor();
                    ReachabilityComputer.computeReachability(
                            beginVertexSet,
                            mustIncludeBeginVertices,
                            mustIncludeReachedBeginVertices,
                            processor);
                    
                    if (processor.callList.size() != 0) {
                        assertEquals(MyVertexCollProcessor.BEGIN, processor.getFirstCall());
                        assertEquals(MyVertexCollProcessor.END, processor.getLastCall());
                    }

                    actualSet = processor.totalSet;

                    if (DEBUG) {
                        System.out.println("actual reachability = " + actualSet);
                    }
                }
                
                assertEquals(expectedSet, actualSet);
            }
        }
    }
}
