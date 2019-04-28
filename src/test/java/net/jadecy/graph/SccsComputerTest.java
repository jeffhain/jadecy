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
import java.util.Random;
import java.util.TreeSet;

import junit.framework.TestCase;
import net.jadecy.graph.GraphTestsUtilz.BallGraphGenerator;
import net.jadecy.graph.GraphTestsUtilz.ChainGraphGenerator;
import net.jadecy.graph.GraphTestsUtilz.ComparableVertexTreeSet;
import net.jadecy.graph.GraphTestsUtilz.CycleGraphGenerator;
import net.jadecy.graph.GraphTestsUtilz.InterfaceGraphGenerator;
import net.jadecy.graph.GraphTestsUtilz.RakeCycleGraphGenerator;
import net.jadecy.graph.GraphTestsUtilz.RandomGraphGenerator;
import net.jadecy.graph.GraphTestsUtilz.RandomGraphWithSccsGenerator;
import net.jadecy.graph.GraphTestsUtilz.TreeGraphGenerator;
import net.jadecy.graph.GraphTestsUtilz.Vertex;

public class SccsComputerTest extends TestCase {

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
    
    private static class MySccComputerVcp implements InterfaceVertexCollProcessor {
        final List<List<InterfaceVertex>> sccList = new ArrayList<List<InterfaceVertex>>();
        final TreeSet<ComparableVertexTreeSet> sccSet = new TreeSet<ComparableVertexTreeSet>();
        ComparableVertexTreeSet currentScc;
        /**
         * To check ordering and unicity.
         */
        InterfaceVertex collPrev = null;
        int nbrOfSccsUntilStop = -1;
        @Override
        public void processCollBegin() {
            this.sccList.add(new ArrayList<InterfaceVertex>());
            this.currentScc = new ComparableVertexTreeSet();
            this.collPrev = null;
        }
        @Override
        public void processCollVertex(InterfaceVertex vertex) {
            this.sccList.get(this.sccList.size()-1).add(vertex);
            this.currentScc.add(vertex);
            {
                if (this.collPrev != null) {
                    if (this.collPrev.compareTo(vertex) >= 0) {
                        throw new AssertionError(this.collPrev + " >= " + vertex + " (bad ordering)");
                    }
                }
                this.collPrev = vertex;
            }
        }
        @Override
        public boolean processCollEnd() {
            final boolean didAdd = this.sccSet.add(this.currentScc);
            if (!didAdd) {
                // Means duplicate.
                throw new AssertionError();
            }
            this.currentScc = null;
            if (this.nbrOfSccsUntilStop < 0) {
                return false;
            } else {
                return (--this.nbrOfSccsUntilStop <= 0);
            }
        }
    }
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    /*
     * Special cases.
     */
    
    public void test_computeSccs_exceptions() {

        final Collection<InterfaceVertex> graph = new ArrayList<InterfaceVertex>();
        final MySccComputerVcp processor = new MySccComputerVcp();
        
        try {
            SccsComputer.computeSccs(
                    null,
                    processor);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            SccsComputer.computeSccs(
                    graph,
                    null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
    }
    
    public void test_computeSccs_emptyGraph() {
        final Collection<InterfaceVertex> graph = new ArrayList<InterfaceVertex>();
        
        final MySccComputerVcp processor = new MySccComputerVcp();
        SccsComputer.computeSccs(
                graph,
                processor);
        
        assertEquals(0, processor.sccList.size());
    }

    public void test_computeSccs_v1_noCycle() {
        final List<InterfaceVertex> graph = new ArrayList<InterfaceVertex>();
        GraphTestsUtilz.newInGraph(graph, 1);
        
        final MySccComputerVcp processor = new MySccComputerVcp();
        SccsComputer.computeSccs(
                graph,
                processor);
        
        assertEquals(1, processor.sccList.size());
        assertTrue(processor.sccSet.contains(asCVTSet(graph.get(0))));
    }

    public void test_computeSccs_v1_cycle() {
        final List<Vertex> graph = new ArrayList<Vertex>();
        GraphTestsUtilz.newInGraph(graph, 1);
        GraphTestsUtilz.ensurePathFromIndexes(graph, 0, 0);
        
        final MySccComputerVcp processor = new MySccComputerVcp();
        SccsComputer.computeSccs(
                graph,
                processor);
        
        assertEquals(1, processor.sccList.size());
        assertTrue(processor.sccSet.contains(asCVTSet(graph.get(0))));
    }

    public void test_computeSccs_v1_v2() {
        final List<InterfaceVertex> graph = new ArrayList<InterfaceVertex>();
        GraphTestsUtilz.newInGraph(graph, 1);
        GraphTestsUtilz.newInGraph(graph, 2);
        
        final MySccComputerVcp processor = new MySccComputerVcp();
        SccsComputer.computeSccs(
                graph,
                processor);
        
        assertEquals(2, processor.sccList.size());
        assertTrue(processor.sccSet.contains(asCVTSet(graph.get(0))));
        assertTrue(processor.sccSet.contains(asCVTSet(graph.get(1))));
    }

    public void test_computeSccs_v1v2() {
        final List<InterfaceVertex> graph = new ArrayList<InterfaceVertex>();
        GraphTestsUtilz.newInGraph(graph, 1);
        GraphTestsUtilz.newInGraph(graph, 2);
        GraphTestsUtilz.ensurePathFromIndexes(graph, 0, 1);
        GraphTestsUtilz.ensurePathFromIndexes(graph, 1, 0);
        
        final MySccComputerVcp processor = new MySccComputerVcp();
        SccsComputer.computeSccs(
                graph,
                processor);
        
        assertEquals(1, processor.sccList.size());
        assertTrue(processor.sccSet.contains(asCVTSet(graph.get(0),graph.get(1))));
    }

    public void test_computeSccs_v1v1() {
        final List<InterfaceVertex> graph = new ArrayList<InterfaceVertex>();
        GraphTestsUtilz.newInGraph(graph, 1);
        GraphTestsUtilz.ensurePathFromIndexes(graph, 0, 0);
        
        final MySccComputerVcp processor = new MySccComputerVcp();
        SccsComputer.computeSccs(
                graph,
                processor);
        
        assertEquals(1, processor.sccList.size());
        assertTrue(processor.sccSet.contains(asCVTSet(graph.get(0))));
    }

    public void test_computeSccs_earlyStop() {
        final int n = 123;
        
        final List<InterfaceVertex> graph = new ArrayList<InterfaceVertex>();
        for (int i = 0; i < 2*n; i++) {
            GraphTestsUtilz.newInGraph(graph, (i+1));
        }
        
        final MySccComputerVcp processor = new MySccComputerVcp();
        processor.nbrOfSccsUntilStop = n;
        SccsComputer.computeSccs(
                graph,
                processor);
        
        assertEquals(n, processor.sccList.size());
        // Since the graph is a sorted collection, lowest vertices were
        // visited first.
        for (int i = 0; i < n; i++) {
            assertTrue(processor.sccSet.contains(asCVTSet(graph.get(i))));
        }
    }

    public void test_computeSccs_chain() {
        final Random random = new Random(SEED);
        for (int size = 1; size < 100; size++) {
            this.test_computeSccs_againstExpected(
                    new ChainGraphGenerator(
                            random.nextLong(),
                            size));
        }
    }
    
    public void test_computeSccs_tree() {
        final Random random = new Random(SEED);
        for (int depth = 0; depth < 10; depth++) {
            this.test_computeSccs_againstExpected(
                    new TreeGraphGenerator(
                            random.nextLong(),
                            depth));
        }
    }
    
    public void test_computeSccs_cycle() {
        final Random random = new Random(SEED);
        for (int size = 1; size < 100; size++) {
            this.test_computeSccs_againstExpected(
                    new CycleGraphGenerator(
                            random.nextLong(),
                            size));
        }
    }
    
    public void test_computeSccs_rakeCycle() {
        final Random random = new Random(SEED);
        for (int size = 1; size < 100; size++) {
            this.test_computeSccs_againstExpected(
                    new RakeCycleGraphGenerator(
                            random.nextLong(),
                            size));
        }
    }

    public void test_computeSccs_ball() {
        final Random random = new Random(SEED);
        for (int size = 1; size < 100; size++) {
            this.test_computeSccs_againstExpected(
                    new BallGraphGenerator(
                            random.nextLong(),
                            size));
        }
    }

    /*
     * General.
     */
    
    public void test_computeSccs_general() {
        final Random random = new Random(SEED);
        for (int nbrOfSccs = 1; nbrOfSccs < 15; nbrOfSccs++) {
            for (int maxSccSize = 1; maxSccSize < 15; maxSccSize++) {
                for (int i = 0; i < 100; i++) {
                    this.test_computeSccs_againstExpected(
                            new RandomGraphWithSccsGenerator(
                                    random.nextLong(),
                                    nbrOfSccs,
                                    maxSccSize));
                }
            }
        }
    }

    /*
     * Sturdiness.
     */

    public void test_computeSccs_sturdiness() {
        final Random random = new Random(SEED);
        
        for (int i = 0; i < 10*1000; i++) {
            final int maxSize = 9;
            final RandomGraphGenerator gg = new RandomGraphGenerator(
                    random.nextLong(),
                    maxSize);
            final List<InterfaceVertex> graph = gg.newGraph();
            
            final MySccComputerVcp processor = new MySccComputerVcp();
            // Just checking that doesn't throw
            // (no associated "expected SCCs" generated).
            SccsComputer.computeSccs(graph, processor);
        }
    }
    
    public void test_computeSccs_noStackOverflowError() {
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
            
            this.test_computeSccs_againstExpected(gg);
        }
    }

    /*
     * Determinism.
     */
    
    /**
     * Checks that vertices hash codes have no effect on the result,
     * i.e. that ordered sets or maps are used where needed.
     */
    public void test_computeSccs_determinism() {
        
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
                
                final MySccComputerVcp processor = new MySccComputerVcp();
                SccsComputer.computeSccs(graph, processor);
                
                final List<List<InterfaceVertex>> result = processor.sccList;
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
    
    private void test_computeSccs_againstExpected(InterfaceGraphGenerator gg) {
        if (DEBUG) {
            System.out.println("test_computeSccs(" + gg + ")");
        }
        
        final Collection<InterfaceVertex> graph = gg.newGraph();
        final TreeSet<ComparableVertexTreeSet> expectedSccs = gg.getExpectedSccs();
        
        final MySccComputerVcp processor = new MySccComputerVcp();
        
        /*
         * 
         */
        
        final boolean mustDebugAll = DEBUG && (graph.size() < GraphTestsUtilz.LARGER_THAN_CALL_STACK);
        
        if (mustDebugAll) {
            GraphTestsUtilz.printGraph(graph);
            System.out.println("expected SCCs = " + expectedSccs);
        }
        
        final int graphHC = graph.hashCode();
        
        SccsComputer.computeSccs(
                graph,
                processor);

        assertEquals(graphHC, graph.hashCode());
        
        if (mustDebugAll) {
            System.out.println("actual SCCs = " + processor.sccSet);
        }
        
        checkEqual(expectedSccs, processor.sccSet);
    }

    /*
     * 
     */
    
    private static ComparableVertexTreeSet asCVTSet(InterfaceVertex... vertices) {
        ComparableVertexTreeSet result = new ComparableVertexTreeSet();
        for (InterfaceVertex v : vertices) {
            result.add(v);
        }
        return result;
    }
    
    private static void checkEqual(
            TreeSet<ComparableVertexTreeSet> expected,
            TreeSet<ComparableVertexTreeSet> actual) {
        GraphTestsUtilz.checkConsistent(expected);
        GraphTestsUtilz.checkConsistent(actual);
        
        final boolean equal = expected.equals(actual);
        if (!equal) {
            {
                final TreeSet<ComparableVertexTreeSet> missing = new TreeSet<ComparableVertexTreeSet>(expected);
                missing.removeAll(actual);
                if (missing.size() != 0) {
                    System.out.println("missing : " + missing);
                }
            }
            {
                final TreeSet<ComparableVertexTreeSet> exceeding = new TreeSet<ComparableVertexTreeSet>(actual);
                exceeding.removeAll(expected);
                if (exceeding.size() != 0) {
                    System.out.println("exceeding : " + exceeding);
                }
            }
        }
        assertTrue(equal);
    }
    
}
