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
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;
import net.jadecy.graph.GraphTestsUtilz.CycleGraphGenerator;
import net.jadecy.graph.GraphTestsUtilz.DisconnectedGraphGenerator;
import net.jadecy.graph.GraphTestsUtilz.InterfaceGraphGenerator;
import net.jadecy.graph.GraphTestsUtilz.RakeCycleGraphGenerator;
import net.jadecy.graph.GraphTestsUtilz.RandomGraphGenerator;
import net.jadecy.graph.GraphTestsUtilz.Vertex;

public class OneShortestPathComputerTest extends TestCase {

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
        final ArrayList<InterfaceVertex> list = new ArrayList<InterfaceVertex>();
        final TreeSet<InterfaceVertex> set = new TreeSet<InterfaceVertex>();
        final ArrayList<Integer> callList = new ArrayList<Integer>();
        public MyVertexCollProcessor() {
        }
        @Override
        public String toString() {
            return "[\nlist = " + this.list + ",\nset = " + this.set + ",\ncallList = " + this.callList + "]";
        }
        //@Override
        public void processCollBegin() {
            assertTrue((this.callList.size() == 0) || (this.getLastCall() == END));
            this.callList.add(BEGIN);
        }
        //@Override
        public void processCollVertex(InterfaceVertex vertex) {
            assertTrue((this.getLastCall() == BEGIN) || (this.getLastCall() == PROCESS));
            this.callList.add(PROCESS);
            {
                this.list.add(vertex);
            }
            {
                // Checking that vertex is always different from others.
                final boolean didAdd = this.set.add(vertex);
                if (!didAdd) {
                    if (DEBUG) {
                        System.out.println("duplicate " + vertex + " in " + this.list);
                    }
                }
                assertTrue(didAdd);
            }
        }
        //@Override
        public boolean processCollEnd() {
            assertTrue(this.getLastCall() == PROCESS);
            this.callList.add(END);
            // Returned boolean must have no effect since there is
            // at most one call.
            return (this.list.size() & 1) != 0;
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
    
    public void test_computeOneShortestPath_exceptions() {

        final Set<InterfaceVertex> beginVertexSet = GraphTestsUtilz.asHashSet();
        final Set<InterfaceVertex> endVertexSet = GraphTestsUtilz.asHashSet();
        final MyVertexCollProcessor processor = new MyVertexCollProcessor();
        
        try {
            OneShortestPathComputer.computeOneShortestPath(
                    null,
                    endVertexSet,
                    processor);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            OneShortestPathComputer.computeOneShortestPath(
                    beginVertexSet,
                    null,
                    processor);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            OneShortestPathComputer.computeOneShortestPath(
                    beginVertexSet,
                    endVertexSet,
                    null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
    }
    
    public void test_computeOneShortestPath_noPath() {

        final Vertex v1 = new Vertex(1);
        final Vertex v2 = new Vertex(2);
        
        GraphTestsUtilz.ensurePath(v1, v2);
        
        for (boolean emptyBegin : new boolean[]{false,true}) {
            for (boolean emptyEnd : new boolean[]{false,true}) {
                final Set<InterfaceVertex> beginVertexSet = emptyBegin ? GraphTestsUtilz.asHashSet() : GraphTestsUtilz.asHashSet(v2);
                final Set<InterfaceVertex> endVertexSet = emptyEnd ? GraphTestsUtilz.asHashSet() : GraphTestsUtilz.asHashSet(v1);
                
                if (DEBUG) {
                    System.out.println("beginVertexSet = " + beginVertexSet);
                    System.out.println("endVertexSet = " + endVertexSet);
                }

                final MyVertexCollProcessor processor = new MyVertexCollProcessor();
                OneShortestPathComputer.computeOneShortestPath(
                        beginVertexSet,
                        endVertexSet,
                        processor);

                if (DEBUG) {
                    System.out.println("processor = " + processor);
                }

                assertEquals(0, processor.callList.size());
            }
        }
    }

    public void test_computeOneShortestPath_beginAndBeginOverlap() {
        
        final int size = 3;
        final DisconnectedGraphGenerator gg = new DisconnectedGraphGenerator(
                SEED,
                size);

        final List<InterfaceVertex> graph = gg.newGraph();

        GraphTestsUtilz.ensurePathFromIndexes(graph, 0, 1);
        GraphTestsUtilz.ensurePathFromIndexes(graph, 1, 0);
        GraphTestsUtilz.ensurePathFromIndexes(graph, 0, 2);
        GraphTestsUtilz.ensurePathFromIndexes(graph, 2, 0);

        final Set<InterfaceVertex> beginVertexSet = GraphTestsUtilz.asSetFromIndexes(graph, 0, 1);
        final Set<InterfaceVertex> endVertexSet = GraphTestsUtilz.asSetFromIndexes(graph, 0, 2);

        final MyVertexCollProcessor processor = new MyVertexCollProcessor();
        OneShortestPathComputer.computeOneShortestPath(
                beginVertexSet,
                endVertexSet,
                processor);
        
        if (DEBUG) {
            System.out.println("processor = " + processor);
        }

        final ArrayList<InterfaceVertex> actualList = processor.list;

        final List<InterfaceVertex> expectedList = GraphTestsUtilz.asListFromIndexes(graph, 0);

        assertEquals(expectedList.size() + 2, processor.callList.size());
        assertEquals(MyVertexCollProcessor.BEGIN, processor.getFirstCall());
        assertEquals(MyVertexCollProcessor.END, processor.getLastCall());

        assertEquals(expectedList, actualList);
    }
    
    /*
     * General.
     */

    public void test_computeOneShortestPath_general() {
        
        /*
         * Graph (k = vertex of index k, whatever its id).
         * 0 <-> 2 --> 4 --> 6
         *        ^         ^
         *         \       /
         *          \     /
         *           \   /
         *            \ v
         * 1 --> 3 --> 5 --> 7
         * 
         * Computing path from 3 to 6.
         */

        final DisconnectedGraphGenerator gg = new DisconnectedGraphGenerator(SEED, 8);
        final List<InterfaceVertex> graph = gg.newGraph();
        GraphTestsUtilz.ensurePathFromIndexes(graph, 0, 2, 4, 6);
        GraphTestsUtilz.ensurePathFromIndexes(graph, 1, 3, 5, 7);
        GraphTestsUtilz.ensurePathFromIndexes(graph, 2, 0);
        GraphTestsUtilz.ensurePathFromIndexes(graph, 5, 2);
        GraphTestsUtilz.ensurePathFromIndexes(graph, 5, 6);
        GraphTestsUtilz.ensurePathFromIndexes(graph, 6, 5);

        final Set<InterfaceVertex> beginVertexSet = GraphTestsUtilz.asHashSet(graph.get(3));
        final Set<InterfaceVertex> endVertexSet = GraphTestsUtilz.asHashSet(graph.get(6));

        final MyVertexCollProcessor processor = new MyVertexCollProcessor();
        OneShortestPathComputer.computeOneShortestPath(
                beginVertexSet,
                endVertexSet,
                processor);

        if (DEBUG) {
            System.out.println("processor = " + processor);
        }

        final ArrayList<InterfaceVertex> actualList = processor.list;

        final List<InterfaceVertex> expectedList = GraphTestsUtilz.asListFromIndexes(graph, 3, 5, 6);

        assertEquals(expectedList.size() + 2, processor.callList.size());
        assertEquals(MyVertexCollProcessor.BEGIN, processor.getFirstCall());
        assertEquals(MyVertexCollProcessor.END, processor.getLastCall());

        assertEquals(expectedList, actualList);
    }
    
    /*
     * Sturdiness.
     */

    public void test_computeOneShortestPath_sturdiness() {
        
        final Random random = new Random(SEED);
        
        for (int i = 0; i < 10*1000; i++) {
            final int maxSize = 9;
            final RandomGraphGenerator gg = new RandomGraphGenerator(
                    random.nextLong(),
                    maxSize);
            
            this.test_computeOneShortestPath_againstNaive(
                    random,
                    gg);
        }
    }
    
    public void test_computeOneShortestPath_noStackOverflowError() {

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
            final InterfaceVertex endVertex = graph.get(size-1);

            final Set<InterfaceVertex> beginVertexSet = GraphTestsUtilz.asHashSet(beginVertex);
            final Set<InterfaceVertex> endVertexSet = GraphTestsUtilz.asHashSet(endVertex);

            final MyVertexCollProcessor processor = new MyVertexCollProcessor();
            OneShortestPathComputer.computeOneShortestPath(
                    beginVertexSet,
                    endVertexSet,
                    processor);

            if (k == 0) {
                assertEquals(size + 2, processor.callList.size());
            } else {
                assertEquals(2 + 2, processor.callList.size());
            }
            assertEquals(MyVertexCollProcessor.BEGIN, processor.getFirstCall());
            assertEquals(MyVertexCollProcessor.END, processor.getLastCall());
        }
    }

    /*
     * Determinism.
     */
    
    /**
     * Checks that vertices hash codes have no effect on the result,
     * i.e. that ordered sets or maps are used where needed.
     */
    public void test_computeOneShortestPath_determinism() {
        
        final Random seedGenerator = new Random(SEED);
        
        for (int k = 0; k < 100; k++) {
            
            List<InterfaceVertex> firstResult = null;
            
            final long currentSeed = seedGenerator.nextLong();
            
            for (int i = 0; i < 10; i++) {
                // In this loop, always generating the same graph,
                // but with (typically) different hash codes for vertices.
                final Random random = new Random(currentSeed);
                final int maxSize = 1 + random.nextInt(5);
                final RandomGraphGenerator gg = new RandomGraphGenerator(
                        random.nextLong(),
                        maxSize);
                final List<InterfaceVertex> graph = gg.newGraph();
                
                final HashSet<InterfaceVertex> beginVertexSet = new HashSet<InterfaceVertex>();
                final HashSet<InterfaceVertex> endVertexSet = new HashSet<InterfaceVertex>();
                for (InterfaceVertex v : graph) {
                    if (random.nextDouble() < 0.1) {
                        beginVertexSet.add(v);
                        endVertexSet.add(v);
                    } else if (random.nextDouble() < 0.4) {
                        beginVertexSet.add(v);
                    } else if (random.nextDouble() < 0.6) {
                        endVertexSet.add(v);
                    }
                }
                
                final MyVertexCollProcessor processor = new MyVertexCollProcessor();
                OneShortestPathComputer.computeOneShortestPath(
                        beginVertexSet,
                        endVertexSet,
                        processor);
                
                final List<InterfaceVertex> result = processor.list;
                if (firstResult == null) {
                    firstResult = result;
                    if (DEBUG) {
                        System.out.println("firstResult = " + firstResult);
                    }
                } else {
                    // Can't compare instances because vertices are from different graphs.
                    assertEquals(firstResult.toString(), result.toString());
                }
            }
        }
    }
    
    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    private void test_computeOneShortestPath_againstNaive(
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
            final InterfaceVertex beginVertex2 = graph.get(graph.size()/8);
            beginVertexSet = GraphTestsUtilz.asHashSet(beginVertex1, beginVertex2);
        }

        final Set<InterfaceVertex> endVertexSet;
        if ((graph.size() == 0) || (random.nextDouble() < 0.1)) {
            endVertexSet = GraphTestsUtilz.asHashSet();
        } else {
            final InterfaceVertex beginVertex3 = graph.get(graph.size()/4);
            final InterfaceVertex beginVertex4 = graph.get(graph.size()/2);
            endVertexSet = GraphTestsUtilz.asHashSet(beginVertex3, beginVertex4);
        }

        final int beginVertexSetHC = beginVertexSet.hashCode();
        final int endVertexSetHC = endVertexSet.hashCode();

        if (DEBUG) {
            System.out.println("beginVertexSet = " + beginVertexSet);
            System.out.println("endVertexSet = " + endVertexSet);
        }

        if (DEBUG) {
            System.out.println("NaiveOneShortestPathComputer.computeOneShortestPath(...)...");
        }

        final int expectedShortestPathLength;
        {
            final MyVertexCollProcessor processor = new MyVertexCollProcessor();
            NaiveOneShortestPathComputer.computeOneShortestPath(
                    beginVertexSet,
                    endVertexSet,
                    processor);
            
            // Inputs not modified.
            assertEquals(beginVertexSetHC, beginVertexSet.hashCode());
            assertEquals(endVertexSetHC, endVertexSet.hashCode());

            if (processor.callList.size() != 0) {
                assertEquals(MyVertexCollProcessor.BEGIN, processor.getFirstCall());
                assertEquals(MyVertexCollProcessor.END, processor.getLastCall());
            }

            // Length = number of edges = number of vertices - 1.
            // If -1 means no shortest path.
            expectedShortestPathLength = processor.list.size() - 1;

            if (DEBUG) {
                System.out.println("possible shortest path = " + processor.list);
                System.out.println("expected shortest path length = " + expectedShortestPathLength);
            }
            
            GraphTestsUtilz.checkPathExists(processor.list);
        }

        if (DEBUG) {
            System.out.println("OneShortestPathComputer.computeOneShortestPath((...)...");
        }

        final int actualShortestPathLength;
        {
            final MyVertexCollProcessor processor = new MyVertexCollProcessor();
            OneShortestPathComputer.computeOneShortestPath(
                    beginVertexSet,
                    endVertexSet,
                    processor);

            // Inputs not modified.
            assertEquals(beginVertexSetHC, beginVertexSet.hashCode());
            assertEquals(endVertexSetHC, endVertexSet.hashCode());

            if (processor.callList.size() != 0) {
                assertEquals(MyVertexCollProcessor.BEGIN, processor.getFirstCall());
                assertEquals(MyVertexCollProcessor.END, processor.getLastCall());
            }

            actualShortestPathLength = processor.list.size() - 1;

            if (DEBUG) {
                System.out.println("actual shortest path = " + processor.list);
                System.out.println("actual shortest path length = " + actualShortestPathLength);
            }
            
            GraphTestsUtilz.checkPathExists(processor.list);
        }

        assertEquals(expectedShortestPathLength, actualShortestPathLength);
    }
}
