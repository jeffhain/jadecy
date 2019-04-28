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

import java.util.Collection;
import java.util.Random;

import net.jadecy.graph.InterfaceVertex;
import net.jadecy.graph.InterfaceVertexCollProcessor;
import net.jadecy.graph.SccsComputer;
import net.jadecy.graph.GraphTestsUtilz.BallGraphGenerator;
import net.jadecy.graph.GraphTestsUtilz.ChainGraphGenerator;
import net.jadecy.graph.GraphTestsUtilz.CycleGraphGenerator;
import net.jadecy.graph.GraphTestsUtilz.InterfaceGraphGenerator;
import net.jadecy.graph.GraphTestsUtilz.TreeGraphGenerator;

public class SccsComputerPerf {

    /*
     * Not benching random graphs, because there it causes too much variations.
     */
    
    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------
    
    private static final int NBR_OF_RUNS = 4;

    private static final int NBR_OF_CALLS = 10;

    private static final long SEED = 123456789L;
    
    //--------------------------------------------------------------------------
    // PRIVATE CLASSES
    //--------------------------------------------------------------------------
    
    private static class MySccVcp implements InterfaceVertexCollProcessor {
        int counter;
        @Override
        public void processCollBegin() {
        }
        @Override
        public void processCollVertex(InterfaceVertex vertex) {
        }
        @Override
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
        new SccsComputerPerf().run(args);
    }
    
    public SccsComputerPerf() {
    }
    
    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    private void run(String[] args) {
        System.out.println("--- " + SccsComputerPerf.class.getSimpleName() + "... ---");
        System.out.println("number of calls = " + NBR_OF_CALLS);
        
        this.bench_computeSccs_chain();
        
        this.bench_computeSccs_tree();
        
        this.bench_computeSccs_cycle();
        
        this.bench_computeSccs_ball();
        
        System.out.println("--- ..." + SccsComputerPerf.class.getSimpleName() + " ---");
    }
    
    /*
     * Chain.
     */
    
    private void bench_computeSccs_chain() {
        System.out.println();
        final Random random = new Random(SEED);
        for (int size : new int[]{1000, 10*1000}) {
            this.bench_computeSccs(new ChainGraphGenerator(random.nextLong(), size));
        }
    }
    
    /*
     * Tree.
     */
    
    private void bench_computeSccs_tree() {
        System.out.println();
        final Random random = new Random(SEED);
        for (int depth : new int[]{12}) {
            this.bench_computeSccs(new TreeGraphGenerator(random.nextLong(), depth));
        }
    }
    
    /*
     * Cycle.
     */
    
    private void bench_computeSccs_cycle() {
        System.out.println();
        final Random random = new Random(SEED);
        for (int size : new int[]{1000, 10*1000}) {
            this.bench_computeSccs(new CycleGraphGenerator(random.nextLong(), size));
        }
    }
    
    /*
     * Ball.
     */
    
    private void bench_computeSccs_ball() {
        System.out.println();
        final Random random = new Random(SEED);
        for (int size : new int[]{500, 1000}) {
            this.bench_computeSccs(new BallGraphGenerator(random.nextLong(), size));
        }
    }
    
    /*
     * 
     */
    
    private void bench_computeSccs(InterfaceGraphGenerator gg) {
        System.out.println();
        
        /*
         * 
         */
        
        final Collection<InterfaceVertex> graph = gg.newGraph();

        /*
         * 
         */

        for (int k = 0; k < NBR_OF_RUNS; k++) {
            final MySccVcp processor = new MySccVcp();
            long a = System.nanoTime();
            for (int i = 0; i < NBR_OF_CALLS; i++) {
                processor.counter = 0;
                SccsComputer.computeSccs(
                        graph,
                        processor);
            }
            long b = System.nanoTime();
            System.out.println("SccsComputer.computeSccs(...), (" + gg + ") (count=" + processor.counter + "), took " + ((b-a)/1e6/1000) + " s");
        }
    }
}
