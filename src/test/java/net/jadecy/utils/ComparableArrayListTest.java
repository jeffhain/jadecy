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

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;

public class ComparableArrayListTest extends TestCase {

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    public void test_ComparableArrayList() {
        final ComparableArrayList<Integer> list1 = new ComparableArrayList<Integer>();
        list1.add(1);

        final ComparableArrayList<Integer> list2 = new ComparableArrayList<Integer>();
        list2.add(2);

        // Default comparator.
        assertEquals(-1, list1.compareTo(list2));
    }
    
    public void test_ComparableArrayList_Collection() {
        final Collection<Integer> coll = new ArrayList<Integer>();
        coll.add(1);
        coll.add(3);
        coll.add(2);
        
        final ComparableArrayList<Integer> list = new ComparableArrayList<Integer>(coll);
        assertEquals(3, list.size());
        assertEquals((Integer) 1, list.get(0));
        assertEquals((Integer) 3, list.get(1));
        assertEquals((Integer) 2, list.get(2));
    }

    public void test_compareTo_L1ShorterSoInferior() {
        final ComparableArrayList<Integer> list1 = new ComparableArrayList<Integer>();
        list1.add(17);

        final ComparableArrayList<Integer> list2 = new ComparableArrayList<Integer>();
        list2.add(1);
        list2.add(2);

        assertEquals(-1, list1.compareTo(list2));
    }
    
    public void test_compareTo_L1LongerSoSuperior() {
        final ComparableArrayList<Integer> list1 = new ComparableArrayList<Integer>();
        list1.add(1);
        list1.add(2);

        final ComparableArrayList<Integer> list2 = new ComparableArrayList<Integer>();
        list2.add(17);

        assertEquals(1, list1.compareTo(list2));
    }
    
    public void test_compareTo_L1Equal() {
        final ComparableArrayList<Integer> list1 = new ComparableArrayList<Integer>();
        list1.add(1);
        list1.add(2);

        final ComparableArrayList<Integer> list2 = new ComparableArrayList<Integer>();
        list2.add(1);
        list2.add(2);

        assertEquals(0, list1.compareTo(list2));
    }
    
    public void test_compareTo_L1Inferior() {
        final ComparableArrayList<Integer> list1 = new ComparableArrayList<Integer>();
        list1.add(1);
        list1.add(2);

        final ComparableArrayList<Integer> list2 = new ComparableArrayList<Integer>();
        list2.add(1);
        list2.add(3);

        assertEquals(-1, list1.compareTo(list2));
    }
    
    public void test_compareTo_L1Superior() {
        final ComparableArrayList<Integer> list1 = new ComparableArrayList<Integer>();
        list1.add(1);
        list1.add(3);

        final ComparableArrayList<Integer> list2 = new ComparableArrayList<Integer>();
        list2.add(1);
        list2.add(2);

        assertEquals(1, list1.compareTo(list2));
    }
}
