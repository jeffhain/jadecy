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
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import junit.framework.TestCase;

/**
 * Ordering tests are light, which is enough to check that we properly
 * use QuietSort.
 */
public class SortUtilsTest extends TestCase {
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    public void test_sort_array() {

        try {
            SortUtils.sort((Integer[]) null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        /*
         * 
         */
        
        SortUtils.sort(new Integer[]{});
        
        {
            final Integer[] arr = new Integer[]{2,1,4,3};
            SortUtils.sort(arr);
            assertEquals("[1, 2, 3, 4]", Arrays.toString(arr));
        }
    }

    public void test_sort_List() {

        try {
            SortUtils.sort((List<Integer>) null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        /*
         * 
         */
        
        {
            final ArrayList<Integer> list = new ArrayList<Integer>();
            SortUtils.sort(list);
            assertEquals(0, list.size());
        }
        
        {
            final ArrayList<Integer> list = new ArrayList<Integer>();
            list.add(2);
            list.add(1);
            list.add(4);
            list.add(3);
            SortUtils.sort(list);
            assertEquals("[1, 2, 3, 4]", list.toString());
        }
    }

    public void test_sort_List_Comparator() {
        
        /*
         * Quick test, since much code is shared.
         */

        // For decreasing ordering.
        final Comparator<Integer> comparator = new Comparator<Integer>() {
            //@Override
            public int compare(Integer i1, Integer i2) {
                final int v1 = i1.intValue();
                final int v2 = i2.intValue();
                if (v1 < v2) {
                    return 1;
                } else if (v1 > v2) {
                    return -1;
                } else {
                    return 0;
                }
            }
        };

        {
            final ArrayList<Integer> list = new ArrayList<Integer>();
            list.add(2);
            list.add(1);
            list.add(4);
            list.add(3);
            SortUtils.sort(list, comparator);
            assertEquals("[4, 3, 2, 1]", list.toString());
        }
    }

    public void test_toSortedArr_Collection() {

        try {
            SortUtils.toSortedArr(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        /*
         * 
         */
        
        {
            final Collection<Integer> coll = new ArrayList<Integer>();
            
            final String inputStr = coll.toString();
            
            final Object[] res = SortUtils.toSortedArr(coll);
            
            // Not modified.
            assertEquals(inputStr, coll.toString());

            assertEquals(0, res.length);
        }
        
        {
            final Collection<Integer> coll = new ArrayList<Integer>();
            coll.add(2);
            coll.add(1);
            coll.add(4);
            coll.add(3);
            
            final String inputStr = coll.toString();
            
            final Object[] res = SortUtils.toSortedArr(coll);
            
            // Not modified.
            assertEquals(inputStr, coll.toString());
            
            assertEquals("[1, 2, 3, 4]", Arrays.toString(res));
        }
    }

    public void test_toSortedList_Collection() {

        try {
            SortUtils.toSortedList(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        /*
         * 
         */
        
        {
            final Collection<Integer> coll = new ArrayList<Integer>();
            
            final String inputStr = coll.toString();
            
            final List<Integer> res = SortUtils.toSortedList(coll);
            
            // Not modified.
            assertEquals(inputStr, coll.toString());

            assertEquals(0, res.size());
            
            // Mutable.
            res.add(1);
            assertEquals(1, res.size());
        }
        
        {
            final Collection<Integer> coll = new ArrayList<Integer>();
            coll.add(2);
            coll.add(1);
            coll.add(4);
            coll.add(3);
            
            final String inputStr = coll.toString();
            
            final List<Integer> res = SortUtils.toSortedList(coll);
            
            // Not modified.
            assertEquals(inputStr, coll.toString());
            
            assertEquals("[1, 2, 3, 4]", res.toString());
            
            // Mutable.
            res.add(1);
            assertEquals(5, res.size());
        }
    }
}
