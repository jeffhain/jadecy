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



/**
 * Sort for not using JDK sort, because it doesn't guard against quadratic worst
 * cases (and can create much garbage, which can hurt for large collections).
 * 
 * This sort uses heap sort and insertion sort, and is not stable.
 */
class QuietSort  {

    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------
    
    /**
     * Triggers the use of insertion sort, for sorting an inferior or equal
     * number of elements.
     * 
     * Value found experimentally to be not too far from the ideal if any.
     */
    static final int MAX_SIZE_FOR_INSERTION_SORT = 45;

    //--------------------------------------------------------------------------
    // PRIVATE CLASSES
    //--------------------------------------------------------------------------
    
    private static class InsertionSort {
        /**
         * @param minIndex Inclusive.
         * @param maxIndex Inclusive.
         */
        public static void sort(Object[] a, int minIndex, int maxIndex) {
            Object tmp;
            int j;
            for (int i = minIndex+1; i <= maxIndex; i++) {
                tmp = a[i];
                @SuppressWarnings("unchecked")
                final Comparable<Object> tmpC = (Comparable<Object>) tmp;
                j = i-1;
                while ((j >= minIndex) && (tmpC.compareTo(a[j]) < 0)) {
                    // j+1 evaluated before j--
                    a[j+1] = a[j--];
                }
                a[j+1] = tmp;
            }
        }
    }
    
    private static class HeapSort {
        /**
         * @param minIndex Inclusive.
         * @param maxIndex Inclusive.
         */
        public static void sort(Object[] a, int minIndex, int maxIndex) {
            int n = maxIndex - minIndex + 1;
            Object tmp;
            
            // Building the heap.
            for (int v=(n>>1); --v >= 0;) {
                downheap(a, n, v, minIndex);
            }
            
            while (n-- > 1) {
                tmp = a[minIndex];
                a[minIndex] = a[minIndex+n];
                a[minIndex+n] = tmp;
                downheap(a, n, 0, minIndex);
            } 
        }
        private static void downheap(Object[] a, int n, int v, int minIndex) {
            int w = (v<<1) + 1;
            int wm = w + minIndex;
            int vm = v + minIndex;
            int nm = n + minIndex;
            while (wm < nm) {
                if (wm+1 < nm) {
                    @SuppressWarnings("unchecked")
                    final Comparable<Object> awmC = (Comparable<Object>) a[wm];
                    if (awmC.compareTo(a[wm+1]) < 0) {
                        wm++;
                    }
                }
                @SuppressWarnings("unchecked")
                final Comparable<Object> awmC = (Comparable<Object>) a[wm];
                if (awmC.compareTo(a[vm]) <= 0) {
                    break;
                }
                Object tmp;
                tmp = a[vm];
                a[vm] = a[wm];
                a[wm] = tmp;
                v = wm - minIndex;
                w = (v+v) + 1;
                vm = v + minIndex;
                wm = w + minIndex;
            }
        }
    }
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    /**
     * Elements must be mutually comparable, which implies that they must not be
     * null.
     * 
     * Not stable, but has no down side if two elements never compare to 0.
     * 
     * @param a Must not be null.
     * @param fromIndex Inclusive.
     * @param toIndex Exclusive.
     * @throws NullPointerException if the specified array is null.
     * @throws IllegalArgumentException if fromIndex > toIndex.
     * @throws ArrayIndexOutOfBoundsException if fromIndex < 0 or
     *         toIndex > a.length.
     */
    public static void sort(Object[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        
        if (toIndex - fromIndex <= MAX_SIZE_FOR_INSERTION_SORT) {
            InsertionSort.sort(a, fromIndex, toIndex - 1);
        } else {
            HeapSort.sort(a, fromIndex, toIndex - 1);
        }
    }
    
    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    private QuietSort() {
    }

    private static void rangeCheck(int arrayLength, int fromIndex, int toIndex) {
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException(
                    "fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
        }
        if (fromIndex < 0) {
            throw new ArrayIndexOutOfBoundsException(fromIndex);
        }
        if (toIndex > arrayLength) {
            throw new ArrayIndexOutOfBoundsException(toIndex);
        }
    }
}
