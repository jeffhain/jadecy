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

import net.jadecy.utils.ArgsUtils;
import net.jadecy.utils.SortUtils;

/**
 * Utility treatments to deal with cycles.
 */
public class CyclesUtils {

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /**
     * Ensures that the cycle starts with its lowest element.
     * 
     * Elements must be comparable with each other.
     * 
     * @param cycle Cycle to be normalized.
     * @throws NullPointerException if cycle or any of its elements is null.
     */
    public static <E extends Comparable<? super E>> void normalizeCycle(E[] cycle) {
        ArgsUtils.requireNonNull2(cycle);
        
        final int indexOfMin = computeIndexOfMin(cycle);
        if (indexOfMin <= 0) {
            return;
        } else {
            leftShift(cycle, indexOfMin);
        }
    }

    /**
     * Ensures that the cycle starts with its lowest element,
     * reordering arrays of causes accordingly but not sorting them.
     * Typically used for when causes are already sorted in their arrays.
     * 
     * Elements must be comparable with each other.
     * 
     * @param cycle Cycle to be normalized.
     * @param causesArr Array containing, for each element of the cycle, the
     *        causes of the link to the next element (or previous for inverse dependencies).
     * @throws NullPointerException if any array or array element is null.
     */
    public static <E extends Comparable<? super E>,C> void normalizeCycleWithCauses(
            E[] cycle,
            C[][] causesArr) {
        ArgsUtils.requireNonNull2(cycle);
        ArgsUtils.requireNonNull3(causesArr);
        
        final int indexOfMin = computeIndexOfMin(cycle);
        if (indexOfMin <= 0) {
            return;
        } else {
            leftShift(cycle, indexOfMin);
            leftShift(causesArr, indexOfMin);
        }
    }

    /**
     * Ensures that the cycle starts with its lowest element,
     * reordering arrays of causes accordingly and normalizing them by sorting.
     * 
     * Elements must be comparable with each other.
     * Causes must be comparable with each other.
     * 
     * @param cycle Cycle to be normalized.
     * @param causesArr Array containing, for each element of the cycle, the
     *        causes of the link to the next element (or previous for inverse dependencies).
     * @throws NullPointerException if any array or array element is null.
     */
    public static <E extends Comparable<? super E>,C> void normalizeCycleAndCauses(
            E[] cycle,
            C[][] causesArr) {
        normalizeCycleWithCauses(cycle, causesArr);
        for (C[] causes : causesArr) {
            SortUtils.sort(causes);
        }
    }
    
    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------

    private CyclesUtils() {
    }
    
    private static <E extends Comparable<? super E>> int computeIndexOfMin(E[] a) {
        E min = null;
        int minIndex = -1;
        for (int i = 0; i < a.length; i++) {
            if ((min == null) || (((Comparable<? super E>) a[i]).compareTo(min) < 0)) {
                min = a[i];
                minIndex = i;
            }
        }
        return minIndex;
    }
    
    private static <E> void leftShift(E[] a, int shift) {
        final Object[] tmp = new Object[a.length];
        System.arraycopy(a, 0, tmp, 0, a.length);
        System.arraycopy(tmp, shift, a, 0, a.length - shift);
        System.arraycopy(tmp, 0, a, a.length - shift, shift);
    }
}
