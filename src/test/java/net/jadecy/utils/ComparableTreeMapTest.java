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

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import junit.framework.TestCase;

public class ComparableTreeMapTest extends TestCase {

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    public void test_ComparableTreeMap() {
        final ComparableTreeMap<Integer,String> map1 = new ComparableTreeMap<Integer,String>();
        map1.put(1, "_1");

        final ComparableTreeMap<Integer,String> map2 = new ComparableTreeMap<Integer,String>();
        map2.put(2, "_2");

        // Default comparator.
        assertEquals(-1, map1.compareTo(map2));
    }
    
    public void test_ComparableTreeMap_Map() {
        final Map<Integer,String> map = new HashMap<Integer,String>();
        map.put(1, "_1");
        map.put(3, "_3");
        map.put(2, "_2");
        
        final ComparableTreeMap<Integer,String> map1 = new ComparableTreeMap<Integer,String>(map);
        assertEquals(3, map1.size());
        assertEquals("_1", map1.get(1));
        assertEquals("_3", map1.get(3));
        assertEquals("_2", map1.get(2));
    }

    public void test_ComparableTreeMap_SortedMap() {
        final SortedMap<Integer,String> sortedMap = new TreeMap<Integer,String>();
        sortedMap.put(1, "_1");
        sortedMap.put(3, "_3");
        sortedMap.put(2, "_2");
        
        final ComparableTreeMap<Integer,String> map1 = new ComparableTreeMap<Integer,String>(sortedMap);
        assertEquals(3, map1.size());
        assertEquals("_1", map1.get(1));
        assertEquals("_3", map1.get(3));
        assertEquals("_2", map1.get(2));
    }
    
    public void test_compareTo_M1ShorterSoInferior() {
        final ComparableTreeMap<Integer,String> map1 = new ComparableTreeMap<Integer,String>();
        map1.put(17, "");

        final ComparableTreeMap<Integer,String> map2 = new ComparableTreeMap<Integer,String>();
        map2.put(1, "");
        map2.put(2, "");

        assertEquals(-1, map1.compareTo(map2));
    }
    
    public void test_compareTo_M1LongerSoSuperior() {
        final ComparableTreeMap<Integer,String> map1 = new ComparableTreeMap<Integer,String>();
        map1.put(1, "");
        map1.put(2, "");

        final ComparableTreeMap<Integer,String> map2 = new ComparableTreeMap<Integer,String>();
        map2.put(17, "");

        assertEquals(1, map1.compareTo(map2));
    }
    
    public void test_compareTo_M1Equal() {
        final ComparableTreeMap<Integer,String> map1 = new ComparableTreeMap<Integer,String>();
        map1.put(1, "");
        map1.put(2, "");

        final ComparableTreeMap<Integer,String> map2 = new ComparableTreeMap<Integer,String>();
        map2.put(1, "");
        map2.put(2, "");

        assertEquals(0, map1.compareTo(map2));
    }
    
    public void test_compareTo_M1Inferior() {
        final ComparableTreeMap<Integer,String> map1 = new ComparableTreeMap<Integer,String>();
        map1.put(1, "");
        map1.put(2, "");

        final ComparableTreeMap<Integer,String> map2 = new ComparableTreeMap<Integer,String>();
        map2.put(1, "");
        map2.put(3, "");

        assertEquals(-1, map1.compareTo(map2));
    }
    
    public void test_compareTo_M1Superior() {
        final ComparableTreeMap<Integer,String> map1 = new ComparableTreeMap<Integer,String>();
        map1.put(1, "");
        map1.put(3, "");

        final ComparableTreeMap<Integer,String> map2 = new ComparableTreeMap<Integer,String>();
        map2.put(1, "");
        map2.put(2, "");

        assertEquals(1, map1.compareTo(map2));
    }
}
