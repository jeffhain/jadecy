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
package net.jadecy.code;

public class NameFiltersPerf {

    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------

    private static final int NBR_OF_RUNS = 4;

    private static final int NBR_OF_CALLS = 10 * 1000 * 1000;

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    public static void main(String[] args) {
        newRun(args);
    }

    public static void newRun(String[] args) {
        new NameFiltersPerf().run(args);
    }
    
    public NameFiltersPerf() {
    }
    
    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    private void run(String[] args) {
        System.out.println("--- " + NameFiltersPerf.class.getSimpleName() + "... ---");
        System.out.println("number of calls = " + NBR_OF_CALLS);

        this.bench_startsWithName();

        this.bench_endsWithName();

        this.bench_containsName();

        System.out.println("--- ..." + NameFiltersPerf.class.getSimpleName() + " ---");
    }

    private void bench_startsWithName() {
        
        final String name = "a.bb.ccc.dddd.eeeee.ffffff.ggggggg.hhhhhhhh";
        final InterfaceNameFilter filter = NameFilters.startsWithName("not_in");
        
        for (int k = 0; k < NBR_OF_RUNS; k++) {
            long a = System.nanoTime();
            for (int i = 0; i < NBR_OF_CALLS; i++) {
                final boolean res = filter.accept(name);
                if (res) {
                    throw new AssertionError();
                }
            }
            long b = System.nanoTime();
            System.out.println("startsWithName(...) : accept(...) took " + ((b-a)/1000/1e6) + " s");
        }
    }

    private void bench_endsWithName() {
        
        final String name = "a.bb.ccc.dddd.eeeee.ffffff.ggggggg.hhhhhhhh";
        final InterfaceNameFilter filter = NameFilters.endsWithName("not_in");
        
        for (int k = 0; k < NBR_OF_RUNS; k++) {
            long a = System.nanoTime();
            for (int i = 0; i < NBR_OF_CALLS; i++) {
                final boolean res = filter.accept(name);
                if (res) {
                    throw new AssertionError();
                }
            }
            long b = System.nanoTime();
            System.out.println("endsWithName(...) : accept(...) took " + ((b-a)/1000/1e6) + " s");
        }
    }

    private void bench_containsName() {
        
        final String name = "a.bb.ccc.dddd.eeeee.ffffff.ggggggg.hhhhhhhh";
        final InterfaceNameFilter filter = NameFilters.containsName("not_in");
        
        for (int k = 0; k < NBR_OF_RUNS; k++) {
            long a = System.nanoTime();
            for (int i = 0; i < NBR_OF_CALLS; i++) {
                final boolean res = filter.accept(name);
                if (res) {
                    throw new AssertionError();
                }
            }
            long b = System.nanoTime();
            System.out.println("containsName(...) : accept(...) took " + ((b-a)/1000/1e6) + " s");
        }
    }
}
