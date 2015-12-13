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

public class ComparableTreeSetTest extends TestCase {

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    public void test_ComparableTreeSet() {
        final ComparableTreeSet<Integer> set1 = new ComparableTreeSet<Integer>();
        set1.add(1);

        final ComparableTreeSet<Integer> set2 = new ComparableTreeSet<Integer>();
        set2.add(2);

        // Default comparator.
        assertEquals(-1, set1.compareTo(set2));
    }
    
    public void test_ComparableTreeSet_Collection() {
        final Collection<Integer> coll = new ArrayList<Integer>();
        coll.add(1);
        coll.add(3);
        coll.add(2);
        
        final ComparableTreeSet<Integer> set = new ComparableTreeSet<Integer>(coll);
        assertEquals(3, set.size());
        assertTrue(set.contains(1));
        assertTrue(set.contains(3));
        assertTrue(set.contains(2));
    }
    
    public void test_compareTo_S1ShorterSoInferior() {
        final ComparableTreeSet<Integer> set1 = new ComparableTreeSet<Integer>();
        set1.add(17);

        final ComparableTreeSet<Integer> set2 = new ComparableTreeSet<Integer>();
        set2.add(1);
        set2.add(2);

        assertEquals(-1, set1.compareTo(set2));
    }
    
    public void test_compareTo_S1LongerSoSuperior() {
        final ComparableTreeSet<Integer> set1 = new ComparableTreeSet<Integer>();
        set1.add(1);
        set1.add(2);

        final ComparableTreeSet<Integer> set2 = new ComparableTreeSet<Integer>();
        set2.add(17);

        assertEquals(1, set1.compareTo(set2));
    }
    
    public void test_compareTo_S1Equal() {
        final ComparableTreeSet<Integer> set1 = new ComparableTreeSet<Integer>();
        set1.add(1);
        set1.add(2);

        final ComparableTreeSet<Integer> set2 = new ComparableTreeSet<Integer>();
        set2.add(1);
        set2.add(2);

        assertEquals(0, set1.compareTo(set2));
    }
    
    public void test_compareTo_S1Inferior() {
        final ComparableTreeSet<Integer> set1 = new ComparableTreeSet<Integer>();
        set1.add(1);
        set1.add(2);

        final ComparableTreeSet<Integer> set2 = new ComparableTreeSet<Integer>();
        set2.add(1);
        set2.add(3);

        assertEquals(-1, set1.compareTo(set2));
    }
    
    public void test_compareTo_S1Superior() {
        final ComparableTreeSet<Integer> set1 = new ComparableTreeSet<Integer>();
        set1.add(1);
        set1.add(3);

        final ComparableTreeSet<Integer> set2 = new ComparableTreeSet<Integer>();
        set2.add(1);
        set2.add(2);

        assertEquals(1, set1.compareTo(set2));
    }
}
