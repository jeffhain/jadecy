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
import java.util.Collection;
import java.util.Collections;
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

public class PathsGraphComputerTest extends TestCase {

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
        final ArrayList<Integer> callList = new ArrayList<Integer>();
        /**
         * To check ordering and unicity.
         */
        InterfaceVertex collPrev = null;
        public MyVertexCollProcessor() {
        }
        @Override
        public String toString() {
            return "[\nlist = " + this.list + ",\ncallList = " + this.callList + "]";
        }
        //@Override
        public void processCollBegin() {
            assertTrue((this.callList.size() == 0) || (this.getLastCall() == END));
            this.callList.add(BEGIN);
            this.collPrev = null;
        }
        //@Override
        public void processCollVertex(InterfaceVertex vertex) {
            assertTrue((this.getLastCall() == BEGIN) || (this.getLastCall() == PROCESS));
            this.callList.add(PROCESS);
            {
                this.list.add(vertex);
            }
            {
                if (this.collPrev != null) {
                    if (this.collPrev.compareTo(vertex) >= 0) {
                        throw new AssertionError(this.collPrev + " >= " + vertex + " (bad ordering)");
                    }
                }
                this.collPrev = vertex;
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

    private static class MyPathProcessor implements InterfaceVertexCollProcessor {
        final ArrayList<InterfaceVertex> path = new ArrayList<InterfaceVertex>();
        @Override
        public String toString() {
            return "[\npath = " + this.path + "]";
        }
        //@Override
        public void processCollBegin() {
        }
        //@Override
        public void processCollVertex(InterfaceVertex vertex) {
            this.path.add(vertex);
        }
        //@Override
        public boolean processCollEnd() {
            return false;
        }
    }

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /*
     * Special cases.
     */

    public void test_computePathsGraph_exceptions() {

        final Set<InterfaceVertex> beginVertexSet = GraphTestsUtilz.asHashSet();
        final Set<InterfaceVertex> endVertexSet = GraphTestsUtilz.asHashSet();
        final MyVertexCollProcessor processor = new MyVertexCollProcessor();
        
        try {
            PathsGraphComputer.computePathsGraph(
                    null,
                    endVertexSet,
                    -1,
                    processor);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            PathsGraphComputer.computePathsGraph(
                    beginVertexSet,
                    null,
                    -1,
                    processor);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            PathsGraphComputer.computePathsGraph(
                    beginVertexSet,
                    endVertexSet,
                    -1,
                    null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
    }
    
    public void test_computePathsGraph_noPath() {
        
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
                PathsGraphComputer.computePathsGraph(
                        beginVertexSet,
                        endVertexSet,
                        -1,
                        processor);

                if (DEBUG) {
                    System.out.println("processor = " + processor);
                }

                assertEquals(0, processor.callList.size());
            }
        }
    }

    public void test_computePathsGraph_beginAndBeginOverlap() {
        
        final int size = 3;
        final DisconnectedGraphGenerator gg = new DisconnectedGraphGenerator(
                SEED,
                size);

        final List<InterfaceVertex> graph = gg.newGraph();

        GraphTestsUtilz.ensurePathFromIndexes(graph, 0, 1);
        GraphTestsUtilz.ensurePathFromIndexes(graph, 1, 0);
        GraphTestsUtilz.ensurePathFromIndexes(graph, 0, 2);
        GraphTestsUtilz.ensurePathFromIndexes(graph, 2, 0);

        final Set<InterfaceVertex> beginVertexSet = GraphTestsUtilz.asSetFromIndexes(graph, 0);
        final Set<InterfaceVertex> endVertexSet = GraphTestsUtilz.asSetFromIndexes(graph, 0, 2);

        for (int maxSteps : new int[]{-1,0,1}) {

            if (DEBUG) {
                System.out.println("maxSteps = " + maxSteps);
            }

            final MyVertexCollProcessor processor = new MyVertexCollProcessor();
            PathsGraphComputer.computePathsGraph(
                    beginVertexSet,
                    endVertexSet,
                    maxSteps,
                    processor);

            if (DEBUG) {
                System.out.println("processor = " + processor);
            }

            final ArrayList<InterfaceVertex> actualList = processor.list;

            // Exploration stops whenever reaching an end vertex,
            // even due to a zero-length path, so we never find more than
            // the origin vertex, even through vertices of indexes 1 and 2
            // are on paths from origin vertex to itself.
            final List<InterfaceVertex> expectedList = GraphTestsUtilz.asListFromIndexes(graph, 0);

            assertEquals(expectedList.size() + 2, processor.callList.size());
            assertEquals(MyVertexCollProcessor.BEGIN, processor.getFirstCall());
            assertEquals(MyVertexCollProcessor.END, processor.getLastCall());

            assertEquals(expectedList, actualList);
        }
    }

    /*
     * General.
     */
    
    public void test_computePathsGraph_general() {
        
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
         * end = {6,8,9}
         * Expected full paths graph:
         *             4 --> 6
         *                  ^
         *                 /
         *                /
         *               /
         *              v
         *       3 --> 5 --> 7 --> 9
         * 
         * 1 never makes it because can't be reached from begin (but can from end,
         * through reverse dependencies).
         * 2 never makes it because when going backward from 6 to it, we encounter 4
         * which is in begin set, so we stop.
         * 8 never makes it because can only b reached from 6 which is already in end.
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
        final Set<InterfaceVertex> endVertexSet = GraphTestsUtilz.asSetFromIndexes(graph, 6, 8, 9);

        for (int maxSteps : new int[]{-1, 0, 1, 2, 3}) {

            if (DEBUG) {
                System.out.println("beginVertexSet = " +beginVertexSet);
                System.out.println("endVertexSet = " + endVertexSet);
                System.out.println("maxSteps = " +maxSteps);
            }

            final MyVertexCollProcessor processor = new MyVertexCollProcessor();
            PathsGraphComputer.computePathsGraph(
                    beginVertexSet,
                    endVertexSet,
                    maxSteps,
                    processor);

            if (DEBUG) {
                System.out.println("processor = " + processor);
            }

            final List<InterfaceVertex> expectedList;
            if (maxSteps == 0) {
                expectedList = new ArrayList<InterfaceVertex>();
            } else if (maxSteps == 1) {
                // 5 is here because can be reached in one step from 3,
                // and from it 6 can be reached in one step too.
                expectedList = GraphTestsUtilz.asListFromIndexes(graph, 4, 5, 6);
            } else if (maxSteps == 2) {
                expectedList = GraphTestsUtilz.asListFromIndexes(graph, 3, 4, 5, 6);
            } else if ((maxSteps == 3) || (maxSteps < 0)) {
                // Enough steps to have all possible vertices.
                expectedList = GraphTestsUtilz.asListFromIndexes(graph, 3, 4, 5, 6, 7, 9);
            } else {
                throw new AssertionError("" + maxSteps);
            }
            // Paths graph vertices are sorted according to their natural ordering.
            Collections.sort(expectedList);
            
            if (expectedList.size() != 0) {
                assertEquals(expectedList.size() + 2, processor.callList.size());
                assertEquals(MyVertexCollProcessor.BEGIN, processor.getFirstCall());
                assertEquals(MyVertexCollProcessor.END, processor.getLastCall());
            } else {
                assertEquals(0, processor.callList.size());
            }
            
            final List<InterfaceVertex> actualList = processor.list;
            
            assertEquals(expectedList, actualList);
        }
    }
    
    /*
     * Sturdiness.
     */

    public void test_computePathsGraph_sturdiness() {
        
        final Random random = new Random(SEED);
        
        for (int i = 0; i < 1000; i++) {
            final int maxSize = 9;
            final RandomGraphGenerator gg = new RandomGraphGenerator(
                    random.nextLong(),
                    maxSize);
            
            test_computePathsGraph_againstNaiveOneShortestPath(
                    random,
                    gg);
        }
    }

    public void test_computePathsGraph_noStackOverflowError() {

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
            PathsGraphComputer.computePathsGraph(
                    beginVertexSet,
                    endVertexSet,
                    -1,
                    processor);

            assertEquals(size + 2, processor.callList.size());
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
    public void test_computePathsGraph_determinism() {

        final Random seedGenerator = new Random(SEED);

        for (int k = 0; k < 100; k++) {

            List<InterfaceVertex> firstResult = null;

            final long currentSeed = seedGenerator.nextLong();

            for (int i = 0; i < 10; i++) {
                // Always generating the same graph,
                // but with (typically) different hash codes for vertices.
                final Random random = new Random(currentSeed);
                final int maxSize = 1 + random.nextInt(9);
                final RandomGraphGenerator gg = new RandomGraphGenerator(
                        random.nextLong(),
                        maxSize);
                final List<InterfaceVertex> graph = gg.newGraph();

                final HashSet<InterfaceVertex> beginVertexSet = new HashSet<InterfaceVertex>();
                final HashSet<InterfaceVertex> endVertexSet = new HashSet<InterfaceVertex>();
                for (InterfaceVertex v : graph) {
                    if (random.nextDouble() < 0.5) {
                        beginVertexSet.add(v);
                    }
                    if (random.nextDouble() < 0.5) {
                        endVertexSet.add(v);
                    }
                }
                
                final int maxSteps = -1 + random.nextInt(maxSize);

                final MyVertexCollProcessor processor = new MyVertexCollProcessor();
                PathsGraphComputer.computePathsGraph(
                        beginVertexSet,
                        endVertexSet,
                        maxSteps,
                        processor);
                
                final List<InterfaceVertex> result = processor.list;
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
    
    private void test_computePathsGraph_againstNaiveOneShortestPath(
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

        // For size of 0, only one loop.
        for (int maxSteps = -1; maxSteps < graph.size(); maxSteps++) {

            if (DEBUG) {
                System.out.println("beginVertexSet = " +beginVertexSet);
                System.out.println("endVertexSet = " + endVertexSet);
                System.out.println("maxSteps = " +maxSteps);
            }

            final MyVertexCollProcessor processor = new MyVertexCollProcessor();
            PathsGraphComputer.computePathsGraph(
                    beginVertexSet,
                    endVertexSet,
                    maxSteps,
                    processor);

            if (DEBUG) {
                System.out.println("processor = " + processor);
            }

            // Inputs not modified.
            assertEquals(beginVertexSetHC, beginVertexSet.hashCode());
            assertEquals(endVertexSetHC, endVertexSet.hashCode());

            /*
             * For each vertex of paths graph, verifying that a path exists
             * within paths graph from begin to it, and from it to end,
             * and that the longest of these paths, which are computed as
             * shortest paths, is of length <= maxSteps (some can be longer
             * due to having edges within begin or within end).
             */
            
            final List<InterfaceVertex> pathsGraph = processor.list;
            for (InterfaceVertex v : pathsGraph) {
                checkShortestPaths(
                        beginVertexSet,
                        Arrays.asList(v),
                        graph,
                        maxSteps);
                checkShortestPaths(
                        Arrays.asList(v),
                        endVertexSet,
                        graph,
                        maxSteps);
            }
        }
    }

    /**
     * @throws AssertionError if found no path from fromColl to toColl,
     *         or if a shortest path was found which vertices don't all
     *         belong to the specified graph, or if maxSteps >= 0 and
     *         the longest shortest path length is superior to maxSteps.
     */
    private static void checkShortestPaths(
            Collection<InterfaceVertex> fromColl,
            Collection<InterfaceVertex> toColl,
            Collection<InterfaceVertex> graphColl,
            int maxSteps) {
        
        final TreeSet<InterfaceVertex> fromSet = new TreeSet<InterfaceVertex>(fromColl);
        final TreeSet<InterfaceVertex> toSet = new TreeSet<InterfaceVertex>(toColl);
        final TreeSet<InterfaceVertex> graphSet = new TreeSet<InterfaceVertex>(graphColl);
        
        if (!graphSet.containsAll(fromSet)) {
            throw new IllegalArgumentException();
        }
        if (!graphSet.containsAll(toSet)) {
            throw new IllegalArgumentException();
        }
        
        List<InterfaceVertex> shortestPath = null;
        
        for (InterfaceVertex fromV : fromSet) {
            for (InterfaceVertex toV : toSet) {
                final MyPathProcessor pathProcessor = new MyPathProcessor();
                NaiveOneShortestPathComputer.computeOneShortestPath(
                        GraphTestsUtilz.asHashSet(fromV),
                        GraphTestsUtilz.asHashSet(toV),
                        pathProcessor);
                final List<InterfaceVertex> path = pathProcessor.path;
                if (DEBUG) {
                    System.out.println("shortest path from " + fromV + " to " + toV + " : " + path);
                }
                final int shortestPathLength = path.size() - 1;
                if (shortestPathLength >= 0) {
                    for (InterfaceVertex v : pathProcessor.path) {
                        if (!graphSet.contains(v)) {
                            throw new AssertionError(v + " in path " + path + " but not in graph " + graphSet);
                        }
                    }
                    if ((shortestPath == null)
                            || (path.size() < shortestPath.size())) {
                        shortestPath = path;
                    }
                }
            }
        }
        
        if (shortestPath == null) {
            throw new AssertionError("found no path from " + fromSet + " to " + toSet);
        }
        
        final int shortestPathLength = shortestPath.size() - 1;
        if ((maxSteps >= 0) && (shortestPathLength > maxSteps)) {
            throw new AssertionError("longest shortest path from " + fromSet + " to " + toSet + " is too long : " + shortestPath);
        }
    }
}
