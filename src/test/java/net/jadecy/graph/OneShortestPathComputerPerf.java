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

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import net.jadecy.graph.GraphTestsUtilz.BallGraphGenerator;
import net.jadecy.graph.GraphTestsUtilz.ChainGraphGenerator;
import net.jadecy.graph.GraphTestsUtilz.CycleGraphGenerator;
import net.jadecy.graph.GraphTestsUtilz.InterfaceGraphGenerator;
import net.jadecy.graph.GraphTestsUtilz.TreeGraphGenerator;

public class OneShortestPathComputerPerf {
    
    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------
    
    private static final int NBR_OF_RUNS = 4;

    private static final int NBR_OF_CALLS = 10;

    private static final int CHAIN_GRAPHS_SIZE = 20*1000;
    private static final int TREE_GRAPHS_DEPTH = 13;
    private static final int CYCLE_GRAPHS_SIZE = 20*1000;
    private static final int BALL_GRAPHS_SIZE = 400;

    private static final int BEGIN_END_VERTEX_SET_MAX_SIZE = 10;
    
    private static final long SEED = 123456789L;
    
    //--------------------------------------------------------------------------
    // PRIVATE CLASSES
    //--------------------------------------------------------------------------
    
    private static class MyProcessor implements InterfaceVertexCollProcessor {
        long vCount = 0;
        public MyProcessor() {
        }
        @Override
        public void processCollBegin() {
        }
        @Override
        public void processCollVertex(InterfaceVertex vertex) {
            this.vCount++;
        }
        @Override
        public boolean processCollEnd() {
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
        new OneShortestPathComputerPerf().run(args);
    }
    
    public OneShortestPathComputerPerf() {
    }
    
    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    private void run(String[] args) {
        System.out.println("--- " + OneShortestPathComputerPerf.class.getSimpleName() + "... ---");
        System.out.println("number of calls = " + NBR_OF_CALLS);
        
        this.bench_computeOneShortestPath_chain();
        
        this.bench_computeOneShortestPath_tree();
        
        this.bench_computeOneShortestPath_cycle();
        
        this.bench_computeOneShortestPath_ball();
        
        System.out.println("--- ..." + OneShortestPathComputerPerf.class.getSimpleName() + " ---");
    }
    
    /*
     * Chain.
     */
    
    private void bench_computeOneShortestPath_chain() {
        final Random random = new Random(SEED);
        for (int size : new int[]{CHAIN_GRAPHS_SIZE}) {
            this.bench_computeOneShortestPath(new ChainGraphGenerator(random.nextLong(), size));
        }
    }
    
    /*
     * Tree.
     */
    
    private void bench_computeOneShortestPath_tree() {
        
        final Random random = new Random(SEED);
        for (int size : new int[]{TREE_GRAPHS_DEPTH}) {
            this.bench_computeOneShortestPath(new TreeGraphGenerator(random.nextLong(), size));
        }
    }
    
    /*
     * Cycle.
     */
    
    private void bench_computeOneShortestPath_cycle() {
        final Random random = new Random(SEED);
        for (int size : new int[]{CYCLE_GRAPHS_SIZE}) {
            this.bench_computeOneShortestPath(new CycleGraphGenerator(random.nextLong(), size));
        }
    }
    
    /*
     * Ball.
     */
    
    private void bench_computeOneShortestPath_ball() {
        final Random random = new Random(SEED);
        for (int size : new int[]{BALL_GRAPHS_SIZE}) {
            this.bench_computeOneShortestPath(new BallGraphGenerator(random.nextLong(), size));
        }
    }

    /*
     * 
     */
    
    private void bench_computeOneShortestPath(InterfaceGraphGenerator gg) {
        System.out.println();

        final List<InterfaceVertex> graph = gg.newGraph();
        
        /*
         * Some begin and end vertices, to check eventual related optimizations,
         * but not too many, else shortest path might often be too easy to compute.
         * 
         * In particular, begin vertices near begin of graph, and end vertices near
         * end of it, for going from near root to near leaves for tree graph case,
         * or near chain begin towards chain end for chain graph, etc.
         */
        final HashSet<InterfaceVertex> beginVertexSet = new HashSet<InterfaceVertex>();
        final HashSet<InterfaceVertex> endVertexSet = new HashSet<InterfaceVertex>();
        final int size = graph.size();
        for (int i = 0; i < Math.min(BEGIN_END_VERTEX_SET_MAX_SIZE, size/4); i++) {
            beginVertexSet.add(graph.get(4*i));
            endVertexSet.add(graph.get(size-1 - 4*i));
        }
        // Making sure sets don't intersect.
        endVertexSet.removeAll(beginVertexSet);
        
        for (int k = 0; k < NBR_OF_RUNS; k++) {
            final MyProcessor processor = new MyProcessor();
            long a = System.nanoTime();
            for (int i = 0; i < NBR_OF_CALLS; i++) {
                processor.vCount = 0;
                OneShortestPathComputer.computeOneShortestPath(
                        beginVertexSet,
                        endVertexSet,
                        processor);
            }
            long b = System.nanoTime();
            System.out.println(
                    OneShortestPathComputer.class.getSimpleName()
                     + ".computeOneShortestPath(...), (" + gg + ")"
                     + "(vCount=" + processor.vCount + "), took " + ((b-a)/1e6/1000) + " s");
        }
    }
}
