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
package net.jadecy.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * Utilities for sorting.
 * 
 * The sorting algorithm is not stable, but that has no down side if two
 * elements never compare to 0.
 */
public class SortUtils {

    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------

    private static final Object[] EMPTY_ARR = new Object[0];

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /**
     * Elements must be mutually comparable, which implies that they must not be
     * null.
     * 
     * @param arr The array to sort.
     * @throws NullPointerException if the specified array is null.
     */
    public static <T> void sort(T[] arr) {
        // Implicit null check.
        QuietSort.sort(arr, 0, arr.length);
    }

    /**
     * Elements must be mutually comparable, which implies that they must not be
     * null.
     * 
     * @param list The list to sort.
     * @throws NullPointerException if the specified list is null.
     */
    public static <E> void sort(List<E> list) {
        sort(list, null);
    }

    /**
     * If the specified comparator is null, elements must be mutually
     * comparable, which implies that they must not be null.
     * If the specified comparator is not null, elements must be comparable
     * with it.
     * 
     * @param list The list to sort.
     * @param c Comparator to use. Can be null, in which case elements are
     *        casted into Comparable for comparisons.
     * @throws NullPointerException if the specified list is null.
     */
    public static <E> void sort(List<E> list, Comparator<? super E> c) {
        
        // Implicit null check.
        final Object[] sortedArr = list.toArray();
        QuietSort.sort(sortedArr, 0, sortedArr.length, c);
        
        list.clear();
        for (Object obj : sortedArr) {
            @SuppressWarnings("unchecked")
            final E e = (E) obj;
            list.add(e);
        }
    }

    /**
     * Elements must be mutually comparable, which implies that they must not be
     * null.
     * 
     * @param coll The collection containing elements to sort. Not modified.
     * @return An array containing sorted elements.
     * @throws NullPointerException if the specified collection is null.
     */
    public static Object[] toSortedArr(Collection<?> coll) {
        
        // Implicit null check.
        final int size = coll.size();
        if (size == 0) {
            // Optimization, for call sites not to have to worry about it.
            return EMPTY_ARR;
        }
        
        final Object[] arr = coll.toArray(new Object[size]);
        QuietSort.sort(arr, 0, arr.length);
        
        return arr;
    }

    /**
     * Elements must be mutually comparable, which implies that they must not be
     * null.
     * 
     * @param coll The collection containing elements to sort. Not modified.
     * @return An new mutable list containing sorted elements.
     * @throws NullPointerException if the specified collection is null.
     */
    public static <V> List<V> toSortedList(Collection<V> coll) {
        
        // Implicit null check.
        final Object[] arr = toSortedArr(coll);
        
        final ArrayList<V> list = new ArrayList<V>(arr.length);
        for (Object obj : arr) {
            @SuppressWarnings("unchecked")
            final V v = (V) obj;
            list.add(v);
        }
        
        return list;
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------

    private SortUtils() {
    }
}
