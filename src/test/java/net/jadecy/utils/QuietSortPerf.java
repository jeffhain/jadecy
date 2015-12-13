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
package net.jadecy.utils;

import java.util.Arrays;
import java.util.Random;

import net.jadecy.utils.QuietSort;

public class QuietSortPerf {
    
    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------
    
    private static final int NBR_OF_RUNS = 4;

    private static final int NBR_OF_INPUTS_PER_RUN = 100;
    private static final int NBR_OF_ELEMENTS_PER_RUN = 10 * 1000 * 1000;

    private static final long SEED = 123456789L;

    //--------------------------------------------------------------------------
    // PRIVATE CLASSES
    //--------------------------------------------------------------------------
    
    private interface MySortAlgo {
        public void sort(Object[] a);
    }
    
    private static class MyJdkAlgo implements MySortAlgo {
        @Override
        public String toString() {
            return Arrays.class.getSimpleName();
        }
        //@Override
        public void sort(Object[] a) {
            Arrays.sort(a, 0, a.length);
        }
    }
    
    private static class MyQuietAlgo implements MySortAlgo {
        @Override
        public String toString() {
            return QuietSort.class.getSimpleName();
        }
        //@Override
        public void sort(Object[] a) {
            QuietSort.sort(a, 0, a.length);
        }
    }
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    public static void main(String[] args) {
        newRun(args);
    }

    public static void newRun(String[] args) {
        new QuietSortPerf().run(args);
    }
    
    public QuietSortPerf() {
    }
    
    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    private void run(String[] args) {
        System.out.println("--- " + QuietSortPerf.class.getSimpleName() + "... ---");

        this.bench_sort_Object_2int();

        System.out.println("--- ..." + QuietSortPerf.class.getSimpleName() + " ---");
    }
    
    /*
     * 
     */
    
    private void bench_sort_Object_2int() {
        for (int length : new int[]{
                10,
                QuietSort.MAX_SIZE_FOR_INSERTION_SORT - 1,
                QuietSort.MAX_SIZE_FOR_INSERTION_SORT,
                QuietSort.MAX_SIZE_FOR_INSERTION_SORT + 1,
                QuietSort.MAX_SIZE_FOR_INSERTION_SORT + 2,
                100,
                1000,
                10 * 1000,
                100 * 1000}) {
            
            System.out.println();
            
            bench_sort_Object_2int(
                    new MyJdkAlgo(),
                    length);
            bench_sort_Object_2int(
                    new MyQuietAlgo(),
                    length);
        }
    }
    
    private void bench_sort_Object_2int(
            MySortAlgo algo,
            int length) {
        
        final Random random = new Random(SEED);
        
        final Integer[] refArr = new Integer[length];
        final Integer[] arr = new Integer[length];

        // For class load.
        {
            randomize(random, refArr);
            algo.sort(refArr);
        }
        
        final int nbrOfCallsPerInput = (NBR_OF_ELEMENTS_PER_RUN / (NBR_OF_INPUTS_PER_RUN * length));
        final int nbrOfCalls = floorPowerOfTen(NBR_OF_INPUTS_PER_RUN * nbrOfCallsPerInput);
        if (nbrOfCalls == 0) {
            throw new AssertionError("bad scenario");
        }
        
        for (int k = 0; k < NBR_OF_RUNS; k++) {
            long sumNS = 0;
            for (int i = 0; i < NBR_OF_INPUTS_PER_RUN; i++) {
                randomize(random, refArr);
                for (int j = 0; j < nbrOfCallsPerInput; j++) {
                    copy(refArr, arr);
                    long a = System.nanoTime();
                    algo.sort(arr);
                    long b = System.nanoTime();
                    sumNS += (b-a);
                }
            }
            System.out.println(nbrOfCalls + " calls to " + algo + ".sort(...), length = " + length + ", took " + (sumNS/1e6/1000) + " s");
        }
    }
    
    private static void randomize(
            Random random,
            Integer[] arr) {
        for (int i = 0; i < arr.length; i++) {
            arr[i] = random.nextInt();
        }
    }
    
    private static void copy(
            Integer[] src,
            Integer[] dest) {
        System.arraycopy(src, 0, dest, 0, src.length);
    }
    
    private static int floorPowerOfTen(int a) {
        return (int) Math.exp(Math.log(10.0) * (int) Math.log10(a));
    }
}
