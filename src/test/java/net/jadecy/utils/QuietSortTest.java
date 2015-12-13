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
import junit.framework.TestCase;

public class QuietSortTest extends TestCase {

    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------

    private static final boolean DEBUG = false;
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    public void test_sort_Object_2int_Comparator() {
        
        /*
         * NPE
         */
        
        try {
            QuietSort.sort(null, 0, 0);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        /*
         * from > to
         */
        
        try {
            QuietSort.sort(new Integer[10], 1, 0);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            // ok
        }
        try {
            QuietSort.sort(new Integer[10], Integer.MAX_VALUE, 0);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            // ok
        }
        try {
            QuietSort.sort(new Integer[10], Integer.MAX_VALUE, Integer.MIN_VALUE);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            // ok
        }
        try {
            QuietSort.sort(new Integer[10], 0, Integer.MIN_VALUE);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            // ok
        }
        
        /*
         * from < 0
         */
        
        try {
            QuietSort.sort(new Integer[10], -1, 0);
            assertTrue(false);
        } catch (ArrayIndexOutOfBoundsException e) {
            // ok
        }
        try {
            QuietSort.sort(new Integer[10], Integer.MIN_VALUE, 0);
            assertTrue(false);
        } catch (ArrayIndexOutOfBoundsException e) {
            // ok
        }
        
        /*
         * to > length
         */
        
        try {
            QuietSort.sort(new Integer[10], 0, 11);
            assertTrue(false);
        } catch (ArrayIndexOutOfBoundsException e) {
            // ok
        }
        
        /*
         * 
         */
        
        final Random random = new Random(123456789L);
        
        for (int i = 0; i < 1000; i++) {
            final int length = random.nextInt(2 * QuietSort.MAX_SIZE_FOR_INSERTION_SORT);
            
            final Integer[] a = new Integer[length];
            randomize(random, a);
            
            // Both in [0,length].
            final int fromIndex = random.nextInt(length + 1);
            final int toIndex = fromIndex + random.nextInt(length + 1 - fromIndex);
            
            final Integer[] expectedArr = a.clone();
            Arrays.sort(expectedArr, fromIndex, toIndex);
            
            final Integer[] actualArr = a.clone();
            QuietSort.sort(actualArr, fromIndex, toIndex);
            
            final String expectedStr = Arrays.toString(expectedArr);
            final String actualStr = Arrays.toString(actualArr);
            if (DEBUG) {
                System.out.println("fromIndex = " + fromIndex);
                System.out.println("toIndex =   " + toIndex);
                System.out.println("expectedStr = " + expectedStr);
                System.out.println("actualStr =   " + actualStr);
            }
            
            // Also checks values out of range etc.
            assertEquals(expectedStr, actualStr);
            
            // More check in case JDK gets funny.
            checkSorted(actualArr, fromIndex, toIndex);
        }
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    private static void randomize(
            Random random,
            Integer[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if (random.nextBoolean()) {
                arr[i] = random.nextInt();
            } else {
                // To have chances of identical elements.
                arr[i] = random.nextInt(10);
            }
        }
    }
    
    private static void checkSorted(Integer[] a, int fromIndex, int toIndex) {
        if (fromIndex == toIndex) {
            // Empty range.
            return;
        }
        int prev = a[fromIndex];
        for (int i = fromIndex + 1; i < toIndex; i++) {
            int ai = a[i];
            if (ai < prev) {
                throw new AssertionError(ai + " < " + prev);
            }
            prev = ai;
        }
    }
}
