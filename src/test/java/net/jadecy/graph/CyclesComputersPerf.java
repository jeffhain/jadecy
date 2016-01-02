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

import java.util.Collection;
import java.util.Random;

import net.jadecy.graph.CyclesComputer;
import net.jadecy.graph.InterfaceVertex;
import net.jadecy.graph.InterfaceVertexCollProcessor;
import net.jadecy.graph.SomeCyclesComputer;
import net.jadecy.graph.GraphTestsUtilz.BallGraphGenerator;
import net.jadecy.graph.GraphTestsUtilz.ChainGraphGenerator;
import net.jadecy.graph.GraphTestsUtilz.CycleGraphGenerator;
import net.jadecy.graph.GraphTestsUtilz.InterfaceGraphGenerator;
import net.jadecy.graph.GraphTestsUtilz.TreeGraphGenerator;

public class CyclesComputersPerf {

    /*
     * Not benching random graphs, because it causes too much variations.
     */
    
    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------
    
    private static final int NBR_OF_RUNS = 4;

    private static final int NBR_OF_CALLS = 10;

    private static final int CHAIN_GRAPHS_SIZE = 10*1000;
    private static final int TREE_GRAPHS_DEPTH = 12;
    private static final int CYCLE_GRAPHS_SIZE = 10*1000;
    private static final int BALL_GRAPHS_SIZE = 9;
    private static final int BIG_BALL_GRAPHS_SIZE = 100;
    
    private static final int BIG_BALL_GRAPHS_MAX_CYCLE_SIZE = 2;

    private static final long SEED = 123456789L;

    //--------------------------------------------------------------------------
    // PRIVATE CLASSES
    //--------------------------------------------------------------------------
    
    private static class MyCycleComputerVcp implements InterfaceVertexCollProcessor {
        int counter;
        //@Override
        public void processCollBegin() {
        }
        //@Override
        public void processCollVertex(InterfaceVertex vertex) {
        }
        //@Override
        public boolean processCollEnd() {
            this.counter++;
            return false;
        }
    }
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    public static void main(String[] args) {
        newRun(args);
    }

    public static void newRun(String[] args) {
        new CyclesComputersPerf().run(args);
    }
    
    public CyclesComputersPerf() {
    }
    
    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    private void run(String[] args) {
        System.out.println("--- " + CyclesComputersPerf.class.getSimpleName() + "... ---");
        System.out.println("number of calls = " + NBR_OF_CALLS);

        this.bench_computeSomeCycles_chain();
        this.bench_computeSomeCycles_tree();
        this.bench_computeSomeCycles_cycle();
        this.bench_computeSomeCycles_ball();
        this.bench_computeSomeCycles_bigBall();

        this.bench_computeShortestCycles_chain();
        this.bench_computeShortestCycles_tree();
        this.bench_computeShortestCycles_cycle();
        this.bench_computeShortestCycles_ball();
        this.bench_computeShortestCycles_bigBall();

        this.bench_computeCycles_chain();
        this.bench_computeCycles_tree();
        this.bench_computeCycles_cycle();
        this.bench_computeCycles_ball();
        this.bench_computeCycles_bigBall_smallMaxSize();

        System.out.println("--- ..." + CyclesComputersPerf.class.getSimpleName() + " ---");
    }
    
    /*
     * Chain.
     */
    
    private void bench_computeSomeCycles_chain() {
        final Random random = new Random(SEED);
        for (int size : new int[]{CHAIN_GRAPHS_SIZE}) {
            bench_computeSomeCycles(new ChainGraphGenerator(random.nextLong(), size));
        }
    }
    
    private void bench_computeShortestCycles_chain() {
        final Random random = new Random(SEED);
        for (int size : new int[]{CHAIN_GRAPHS_SIZE}) {
            bench_computeShortestCycles(new ChainGraphGenerator(random.nextLong(), size));
        }
    }
    
    private void bench_computeCycles_chain() {
        final Random random = new Random(SEED);
        for (int size : new int[]{CHAIN_GRAPHS_SIZE}) {
            bench_computeCycles(new ChainGraphGenerator(random.nextLong(), size));
        }
    }

    /*
     * Tree.
     */
    
    private void bench_computeSomeCycles_tree() {
        final Random random = new Random(SEED);
        for (int depth : new int[]{TREE_GRAPHS_DEPTH}) {
            bench_computeSomeCycles(new TreeGraphGenerator(random.nextLong(), depth));
        }
    }
    
    private void bench_computeShortestCycles_tree() {
        final Random random = new Random(SEED);
        for (int depth : new int[]{TREE_GRAPHS_DEPTH}) {
            bench_computeShortestCycles(new TreeGraphGenerator(random.nextLong(), depth));
        }
    }
    
    private void bench_computeCycles_tree() {
        final Random random = new Random(SEED);
        for (int depth : new int[]{TREE_GRAPHS_DEPTH}) {
            bench_computeCycles(new TreeGraphGenerator(random.nextLong(), depth));
        }
    }

    /*
     * Cycle.
     */
    
    private void bench_computeSomeCycles_cycle() {
        final Random random = new Random(SEED);
        for (int size : new int[]{CYCLE_GRAPHS_SIZE}) {
            bench_computeSomeCycles(new CycleGraphGenerator(random.nextLong(), size));
        }
    }
    
    private void bench_computeShortestCycles_cycle() {
        final Random random = new Random(SEED);
        for (int size : new int[]{CYCLE_GRAPHS_SIZE}) {
            bench_computeShortestCycles(new CycleGraphGenerator(random.nextLong(), size));
        }
    }
    
    private void bench_computeCycles_cycle() {
        final Random random = new Random(SEED);
        for (int size : new int[]{CYCLE_GRAPHS_SIZE}) {
            bench_computeCycles(new CycleGraphGenerator(random.nextLong(), size));
        }
    }
    
    /*
     * Ball.
     */
    
    private void bench_computeSomeCycles_ball() {
        final Random random = new Random(SEED);
        for (int size : new int[]{BALL_GRAPHS_SIZE}) {
            bench_computeSomeCycles(new BallGraphGenerator(random.nextLong(), size));
        }
    }
    
    private void bench_computeShortestCycles_ball() {
        final Random random = new Random(SEED);
        for (int size : new int[]{BALL_GRAPHS_SIZE}) {
            bench_computeShortestCycles(new BallGraphGenerator(random.nextLong(), size));
        }
    }
    
    private void bench_computeCycles_ball() {
        final Random random = new Random(SEED);
        for (int size : new int[]{BALL_GRAPHS_SIZE}) {
            bench_computeCycles(new BallGraphGenerator(random.nextLong(), size));
        }
    }
    
    /*
     * To check that computation doesn't take ages even on a ball graphs with
     * a huge amount of cycles, whatever maxSize for computeSomeCycle and
     * computeShortestCycles, and for small values of maxSize for computeCycle.
     */
    
    private void bench_computeSomeCycles_bigBall() {
        final Random random = new Random(SEED);
        for (int size : new int[]{BIG_BALL_GRAPHS_SIZE}) {
            bench_computeSomeCycles(new BallGraphGenerator(random.nextLong(), size));
        }
    }
    
    private void bench_computeShortestCycles_bigBall() {
        final Random random = new Random(SEED);
        for (int size : new int[]{BIG_BALL_GRAPHS_SIZE}) {
            bench_computeShortestCycles(new BallGraphGenerator(random.nextLong(), size));
        }
    }
    
    private void bench_computeCycles_bigBall_smallMaxSize() {
        final Random random = new Random(SEED);
        for (int size : new int[]{BIG_BALL_GRAPHS_SIZE}) {
            bench_computeCycles(
                    new BallGraphGenerator(random.nextLong(), size),
                    BIG_BALL_GRAPHS_MAX_CYCLE_SIZE);
        }
    }
    
    /*
     * 
     */
    
    private void bench_computeSomeCycles(InterfaceGraphGenerator gg) {
        System.out.println();
        
        final Collection<InterfaceVertex> graph = gg.newGraph();

        for (int k = 0; k < NBR_OF_RUNS; k++) {
            final MyCycleComputerVcp processor = new MyCycleComputerVcp();
            long a = System.nanoTime();
            for (int i = 0; i < NBR_OF_CALLS; i++) {
                processor.counter = 0;
                SomeCyclesComputer.computeSomeCycles(graph, -1, processor);
            }
            long b = System.nanoTime();
            System.out.println(
                    "Loop on SomeCyclesComputer.computeSomeCycles(...), ("
                            + gg + ") (count=" + processor.counter + "), took "
                            + ((b-a)/1e6/1000) + " s");
        }
    }

    private void bench_computeShortestCycles(InterfaceGraphGenerator gg) {
        System.out.println();
        
        final Collection<InterfaceVertex> graph = gg.newGraph();

        for (int k = 0; k < NBR_OF_RUNS; k++) {
            final MyCycleComputerVcp processor = new MyCycleComputerVcp();
            long a = System.nanoTime();
            for (int i = 0; i < NBR_OF_CALLS; i++) {
                processor.counter = 0;
                ShortestCyclesComputer.computeShortestCycles(graph, -1, processor);
            }
            long b = System.nanoTime();
            System.out.println(
                    "Loop on ShortestCyclesComputer.computeShortestCycles(...), ("
                            + gg + ") (count=" + processor.counter + "), took "
                            + ((b-a)/1e6/1000) + " s");
        }
    }

    private void bench_computeCycles(InterfaceGraphGenerator gg) {
        final int maxSize = -1;
        bench_computeCycles(gg, maxSize);
    }

    private void bench_computeCycles(
            InterfaceGraphGenerator gg,
            int maxSize) {
        System.out.println();
        
        final Collection<InterfaceVertex> graph = gg.newGraph();

        for (int k = 0; k < NBR_OF_RUNS; k++) {
            final MyCycleComputerVcp processor = new MyCycleComputerVcp();
            long a = System.nanoTime();
            for (int i = 0; i < NBR_OF_CALLS; i++) {
                processor.counter = 0;
                CyclesComputer.computeCycles(graph, maxSize, processor);
            }
            long b = System.nanoTime();
            System.out.println(
                    "Loop on CyclesComputer.computeCycles(" + ((maxSize < 0) ? "..." : "," + maxSize + ",") + "), ("
                            + gg + ") (count=" + processor.counter + "), took "
                            + ((b-a)/1e6/1000) + " s");
        }
    }
}
