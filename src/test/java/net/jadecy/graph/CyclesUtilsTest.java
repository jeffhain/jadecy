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
package net.jadecy.graph;

import net.jadecy.graph.CyclesUtils;
import junit.framework.TestCase;

public class CyclesUtilsTest extends TestCase {

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    public void test_normalizeCycle_arrayOfE() {
        
        try {
            CyclesUtils.normalizeCycle(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            CyclesUtils.normalizeCycle(
                    new Integer[]{
                            null
                    });
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            CyclesUtils.normalizeCycle(
                    new Integer[]{
                            1,
                            null
                    });
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        /*
         * 
         */
        
        {
            Integer[] names = new Integer[]{};
            CyclesUtils.normalizeCycle(names);
        }
        
        {
            String[] names = new String[]{"1"};
            CyclesUtils.normalizeCycle(names);
            assertEquals("1", names[0]);
        }
        
        /*
         * 
         */
        
        {
            Integer[] names = new Integer[]{1};
            CyclesUtils.normalizeCycle(names);
            assertEquals(new Integer(1), names[0]);
        }
        
        {
            Integer[] names = new Integer[]{1,3,2};
            CyclesUtils.normalizeCycle(names);
            assertEquals(new Integer(1), names[0]);
            assertEquals(new Integer(3), names[1]);
            assertEquals(new Integer(2), names[2]);
        }
        
        {
            Integer[] names = new Integer[]{3,2,1};
            CyclesUtils.normalizeCycle(names);
            assertEquals(new Integer(1), names[0]);
            assertEquals(new Integer(3), names[1]);
            assertEquals(new Integer(2), names[2]);
        }
    }

    public void test_normalizeCycleWithCauses_arrayOfE_arrayOfArrayOfC() {
        
        try {
            CyclesUtils.normalizeCycleWithCauses(
                    null,
                    new Integer[][]{});
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        try {
            CyclesUtils.normalizeCycleWithCauses(
                    new Integer[]{null},
                    new Integer[][]{});
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        try {
            CyclesUtils.normalizeCycleWithCauses(
                    new Integer[]{},
                    null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        try {
            CyclesUtils.normalizeCycleWithCauses(
                    new Integer[]{},
                    new Integer[][]{null});
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        /*
         * 
         */

        {
            Integer[] names = new Integer[]{};
            String[][] causesArr = new String[][]{};
            CyclesUtils.normalizeCycleWithCauses(names, causesArr);
        }

        // Name but no cause: allowed.
        {
            Integer[] names = new Integer[]{1};
            String[][] causesArr = new String[][]{};
            CyclesUtils.normalizeCycleWithCauses(names, causesArr);
        }

        // Causes but no name: allowed.
        {
            Integer[] names = new Integer[]{};
            String[][] causesArr = new String[][]{new String[]{"A"}};
            CyclesUtils.normalizeCycleWithCauses(names, causesArr);
        }

        /*
         * 
         */
        
        {
            Integer[] names = new Integer[]{1};
            String[][] causesArr = new String[][]{new String[]{"A"}};
            CyclesUtils.normalizeCycleWithCauses(names, causesArr);
            assertEquals(new Integer(1), names[0]);
            assertEquals("A", causesArr[0][0]);
        }

        {
            Integer[] names = new Integer[]{1,3,2};
            String[][] causesArr = new String[][]{
                    new String[]{"A"},
                    new String[]{"B","C"},
                    new String[]{"D","E","F","G"}
            };
            CyclesUtils.normalizeCycleWithCauses(names, causesArr);
            assertEquals(new Integer(1), names[0]);
            assertEquals(new Integer(3), names[1]);
            assertEquals(new Integer(2), names[2]);
            //
            assertEquals("A", causesArr[0][0]);
            //
            assertEquals("B", causesArr[1][0]);
            assertEquals("C", causesArr[1][1]);
            //
            assertEquals("D", causesArr[2][0]);
            assertEquals("E", causesArr[2][1]);
            assertEquals("F", causesArr[2][2]);
            assertEquals("G", causesArr[2][3]);
        }

        {
            Integer[] names = new Integer[]{3,2,1};
            String[][] causesArr = new String[][]{
                    new String[]{"C","B"},
                    new String[]{"G","E","F","D"},
                    new String[]{"A"}
            };
            CyclesUtils.normalizeCycleWithCauses(names, causesArr);
            assertEquals(new Integer(1), names[0]);
            assertEquals(new Integer(3), names[1]);
            assertEquals(new Integer(2), names[2]);
            // Causes arrays not sorted.
            assertEquals("A", causesArr[0][0]);
            //
            assertEquals("C", causesArr[1][0]);
            assertEquals("B", causesArr[1][1]);
            //
            assertEquals("G", causesArr[2][0]);
            assertEquals("E", causesArr[2][1]);
            assertEquals("F", causesArr[2][2]);
            assertEquals("D", causesArr[2][3]);
        }
    }

    public void test_normalizeCycleAndCauses_arrayOfE_arrayOfArrayOfC() {
        
        try {
            CyclesUtils.normalizeCycleAndCauses(
                    null,
                    new Integer[][]{});
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        try {
            CyclesUtils.normalizeCycleAndCauses(
                    new Integer[]{null},
                    new Integer[][]{});
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        try {
            CyclesUtils.normalizeCycleAndCauses(
                    new Integer[]{},
                    null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        try {
            CyclesUtils.normalizeCycleAndCauses(
                    new Integer[]{},
                    new Integer[][]{null});
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        /*
         * 
         */

        {
            Integer[] names = new Integer[]{};
            String[][] causesArr = new String[][]{};
            CyclesUtils.normalizeCycleAndCauses(names, causesArr);
        }

        // Name but no cause: allowed.
        {
            Integer[] names = new Integer[]{1};
            String[][] causesArr = new String[][]{};
            CyclesUtils.normalizeCycleAndCauses(names, causesArr);
        }

        // Causes but no name: allowed.
        {
            Integer[] names = new Integer[]{};
            String[][] causesArr = new String[][]{new String[]{"A"}};
            CyclesUtils.normalizeCycleAndCauses(names, causesArr);
        }

        /*
         * 
         */
        
        {
            Integer[] names = new Integer[]{1};
            String[][] causesArr = new String[][]{new String[]{"A"}};
            CyclesUtils.normalizeCycleAndCauses(names, causesArr);
            assertEquals(new Integer(1), names[0]);
            assertEquals("A", causesArr[0][0]);
        }

        {
            Integer[] names = new Integer[]{1,3,2};
            String[][] causesArr = new String[][]{
                    new String[]{"A"},
                    new String[]{"B","C"},
                    new String[]{"D","E","F","G"}
            };
            CyclesUtils.normalizeCycleAndCauses(names, causesArr);
            assertEquals(new Integer(1), names[0]);
            assertEquals(new Integer(3), names[1]);
            assertEquals(new Integer(2), names[2]);
            //
            assertEquals("A", causesArr[0][0]);
            //
            assertEquals("B", causesArr[1][0]);
            assertEquals("C", causesArr[1][1]);
            //
            assertEquals("D", causesArr[2][0]);
            assertEquals("E", causesArr[2][1]);
            assertEquals("F", causesArr[2][2]);
            assertEquals("G", causesArr[2][3]);
        }

        {
            Integer[] names = new Integer[]{3,2,1};
            String[][] causesArr = new String[][]{
                    new String[]{"C","B"},
                    new String[]{"G","E","F","D"},
                    new String[]{"A"}
            };
            CyclesUtils.normalizeCycleAndCauses(names, causesArr);
            assertEquals(new Integer(1), names[0]);
            assertEquals(new Integer(3), names[1]);
            assertEquals(new Integer(2), names[2]);
            // Causes arrays sorted.
            assertEquals("A", causesArr[0][0]);
            //
            assertEquals("B", causesArr[1][0]);
            assertEquals("C", causesArr[1][1]);
            //
            assertEquals("D", causesArr[2][0]);
            assertEquals("E", causesArr[2][1]);
            assertEquals("F", causesArr[2][2]);
            assertEquals("G", causesArr[2][3]);
        }
    }
}
