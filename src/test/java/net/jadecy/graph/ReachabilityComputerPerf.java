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

public class ReachabilityComputerPerf {

    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------
    
    private static final int NBR_OF_RUNS = 2;

    private static final int NBR_OF_CALLS = 10;

    private static final int CHAIN_GRAPHS_SIZE = 20*1000;
    private static final int TREE_GRAPHS_DEPTH = 13;
    private static final int CYCLE_GRAPHS_SIZE = 20*1000;
    private static final int BALL_GRAPHS_SIZE = 400;

    private static final int BEGIN_VERTEX_SET_MAX_SIZE = 10;

    private static final long SEED = 123456789L;
    
    //--------------------------------------------------------------------------
    // PRIVATE CLASSES
    //--------------------------------------------------------------------------
    
    private static class MyProcessor implements InterfaceVertexCollProcessor {
        long cCount = 0;
        long vCount = 0;
        public MyProcessor() {
        }
        //@Override
        public void processCollBegin() {
            this.cCount++;
        }
        //@Override
        public void processCollVertex(InterfaceVertex vertex) {
            this.vCount++;
        }
        //@Override
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
        new ReachabilityComputerPerf().run(args);
    }
    
    public ReachabilityComputerPerf() {
    }
    
    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    private void run(String[] args) {
        System.out.println("--- " + ReachabilityComputerPerf.class.getSimpleName() + "... ---");
        System.out.println("number of calls = " + NBR_OF_CALLS);
        
        this.bench_computeReachability_chain();
        
        this.bench_computeReachability_tree();
        
        this.bench_computeReachability_cycle();
        
        this.bench_computeReachability_ball();
        
        System.out.println("--- ..." + ReachabilityComputerPerf.class.getSimpleName() + " ---");
    }
    
    /*
     * Chain.
     */
    
    private void bench_computeReachability_chain() {
        final Random random = new Random(SEED);
        for (int size : new int[]{CHAIN_GRAPHS_SIZE}) {
            this.bench_computeReachability(
                    random,
                    new ChainGraphGenerator(random.nextLong(), size));
        }
    }
    
    /*
     * Tree.
     */
    
    private void bench_computeReachability_tree() {
        final Random random = new Random(SEED);
        for (int depth : new int[]{TREE_GRAPHS_DEPTH}) {
            this.bench_computeReachability(
                    random,
                    new TreeGraphGenerator(random.nextLong(), depth));
        }
    }

    /*
     * Cycle.
     */
    
    private void bench_computeReachability_cycle() {
        final Random random = new Random(SEED);
        for (int size : new int[]{CYCLE_GRAPHS_SIZE}) {
            this.bench_computeReachability(
                    random,
                    new CycleGraphGenerator(random.nextLong(), size));
        }
    }
    
    /*
     * Ball.
     */
    
    private void bench_computeReachability_ball() {
        final Random random = new Random(SEED);
        for (int size : new int[]{BALL_GRAPHS_SIZE}) {
            this.bench_computeReachability(
                    random,
                    new BallGraphGenerator(random.nextLong(), size));
        }
    }
    
    /*
     * 
     */
    
    private void bench_computeReachability(
            Random random,
            InterfaceGraphGenerator gg) {
        System.out.println();

        final List<InterfaceVertex> graph = gg.newGraph();
        
        // Some begin vertices, to check eventual related optimizations,
        // but not too many, else reachability might often be too easy to compute.
        final HashSet<InterfaceVertex> beginVertexSet = new HashSet<InterfaceVertex>();
        final int size = graph.size();
        for (int i = 0; i < Math.min(BEGIN_VERTEX_SET_MAX_SIZE, size/2); i++) {
            final int randomIndex = random.nextInt(size);
            beginVertexSet.add(graph.get(randomIndex));
        }
        
        for (boolean mustIncludeBeginVertices : new boolean[]{false,true}) {
            for (boolean mustIncludeReachedBeginVertices : new boolean[]{false,true}) {
                if (mustIncludeBeginVertices && mustIncludeReachedBeginVertices) {
                    // Second boolean not used.
                    continue;
                }
                
                final boolean b1 = mustIncludeBeginVertices;
                final boolean b2 = mustIncludeReachedBeginVertices;
                
                for (int k = 0; k < NBR_OF_RUNS; k++) {
                    final MyProcessor processor = new MyProcessor();
                    long a = System.nanoTime();
                    for (int i = 0; i < NBR_OF_CALLS; i++) {
                        processor.cCount = 0;
                        processor.vCount = 0;
                        ReachabilityComputer.computeReachability(
                                beginVertexSet,
                                mustIncludeBeginVertices,
                                mustIncludeReachedBeginVertices,
                                processor);
                    }
                    long b = System.nanoTime();
                    System.out.println(
                            ReachabilityComputer.class.getSimpleName()
                             + ".computeReachability(," + b1 + "," + b2 + ",,), (" + gg + ")"
                             + "(cCount=" + processor.cCount + ",vCount=" + processor.vCount + "), took " + ((b-a)/1e6/1000) + " s");
                }
            }
        }
    }
}
