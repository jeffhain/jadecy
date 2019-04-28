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
package net.jadecy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import net.jadecy.names.NameUtils;
import net.jadecy.tests.PrintTestUtils;
import net.jadecy.utils.MemPrintStream;
import junit.framework.TestCase;

public class JadecyUtilsTest extends TestCase {

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    public void test_computeDepsMergedFromDepsLm_exceptions() {

        try {
            JadecyUtils.computeDepsMergedFromDepsLm(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        // Duplicate name.
        {
            final List<SortedMap<String,Long>> byteSizeByDepList =
                    new ArrayList<SortedMap<String,Long>>();
            byteSizeByDepList.add(
                    newM_S_L(
                            new Object[][]{
                                    {"elem1",17L}}));
            byteSizeByDepList.add(
                    newM_S_L(
                            new Object[][]{
                                    {"elem1",12L}}));

            final String inputStr = byteSizeByDepList.toString();

            try {
                JadecyUtils.computeDepsMergedFromDepsLm(byteSizeByDepList);
                assertTrue(false);
            } catch (IllegalArgumentException e) {
                // ok
            }

            // Not modified.
            assertEquals(inputStr, byteSizeByDepList.toString());
        }
    }

    public void test_computeDepsMergedFromDepsLm_normal() {

        final List<SortedMap<String,Long>> byteSizeByDepList =
                new ArrayList<SortedMap<String,Long>>();
        byteSizeByDepList.add(
                newM_S_L(
                        new Object[][]{
                                {"elem1",17L},
                                {"elem2",21L}}));
        byteSizeByDepList.add(
                newM_S_L(
                        new Object[][]{
                                {"elem3",12L},
                                {"elem4",71L}}));

        /*
         * 
         */

        final String inputStr = byteSizeByDepList.toString();

        final SortedMap<String,Long> res =
                JadecyUtils.computeDepsMergedFromDepsLm(byteSizeByDepList);

        // Not modified.
        assertEquals(inputStr, byteSizeByDepList.toString());

        assertEquals(4, res.size());
        assertEquals((Long) 17L, res.get("elem1"));
        assertEquals((Long) 21L, res.get("elem2"));
        assertEquals((Long) 12L, res.get("elem3"));
        assertEquals((Long) 71L, res.get("elem4"));
    }

    /*
     * Graphs reduction.
     */

    public void test_computeGraphMergedFromGraphLmms_exceptions() {

        try {
            JadecyUtils.computeGraphMergedFromGraphLmms(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        // Duplicate name.
        {
            final List<SortedMap<String,SortedMap<String,SortedSet<String>>>> causesByDepByNameList =
                    new ArrayList<SortedMap<String,SortedMap<String,SortedSet<String>>>>();
            {
                final SortedMap<String,SortedMap<String,SortedSet<String>>> causesByDepByName =
                        new TreeMap<String,SortedMap<String,SortedSet<String>>>();
                causesByDepByName.put("elem1", new TreeMap<String,SortedSet<String>>());
                causesByDepByNameList.add(causesByDepByName);
            }
            {
                final SortedMap<String,SortedMap<String,SortedSet<String>>> causesByDepByName =
                        new TreeMap<String,SortedMap<String,SortedSet<String>>>();
                causesByDepByName.put("elem1", new TreeMap<String,SortedSet<String>>());
                causesByDepByNameList.add(causesByDepByName);
            }

            final String inputStr = causesByDepByNameList.toString();

            try {
                JadecyUtils.computeGraphMergedFromGraphLmms(causesByDepByNameList);
                assertTrue(false);
            } catch (IllegalArgumentException e) {
                // ok
            }

            // Not modified.
            assertEquals(inputStr, causesByDepByNameList.toString());
        }
    }

    public void test_computeGraphMergedFromGraphLmms_normal() {

        final List<SortedMap<String,SortedMap<String,SortedSet<String>>>> causesByDepByNameList =
                new ArrayList<SortedMap<String,SortedMap<String,SortedSet<String>>>>();
        //
        final SortedMap<String,SortedMap<String,SortedSet<String>>> causesByDepByName1 =
                new TreeMap<String,SortedMap<String,SortedSet<String>>>();
        final SortedMap<String,SortedSet<String>> causesByDep1 =
                newM_S_SS(
                        new Object[][]{
                                {"dep1",newS("c1","c2")},
                                {"dep2",newS("c3","c4")}});
        causesByDepByName1.put("elem1", causesByDep1);
        final SortedMap<String,SortedSet<String>> causesByDep2 =
                newM_S_SS(
                        new Object[][]{
                                {"dep3",newS("c5","c6")},
                                {"dep4",newS("c7","c8")}});
        causesByDepByName1.put("elem2", causesByDep2);
        causesByDepByNameList.add(causesByDepByName1);
        //
        final SortedMap<String,SortedMap<String,SortedSet<String>>> causesByDepByName2 =
                new TreeMap<String,SortedMap<String,SortedSet<String>>>();
        final SortedMap<String,SortedSet<String>> causesByDep3 =
                newM_S_SS(
                        new Object[][]{
                                {"dep5",newS("c9","c10")},
                                {"dep6",newS("c11","c12")}});
        causesByDepByName2.put("elem3", causesByDep3);
        causesByDepByNameList.add(causesByDepByName2);

        /*
         * 
         */

        final String inputStr = causesByDepByNameList.toString();

        final SortedMap<String,SortedMap<String,SortedSet<String>>> res =
                JadecyUtils.computeGraphMergedFromGraphLmms(causesByDepByNameList);

        // Not modified.
        assertEquals(inputStr, causesByDepByNameList.toString());

        assertEquals(3, res.size());
        assertSame(causesByDep1, res.get("elem1"));
        assertSame(causesByDep2, res.get("elem2"));
        assertSame(causesByDep3, res.get("elem3"));
    }

    public void test_computeGraphMergedFromGraphLms_exceptions() {

        try {
            JadecyUtils.computeGraphMergedFromGraphLms(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        // Duplicate name.
        {
            final List<SortedMap<String,SortedSet<String>>> depsByNameList =
                    new ArrayList<SortedMap<String,SortedSet<String>>>();
            depsByNameList.add(
                    newM_S_SS(
                            new Object[][]{
                                    {"elem1",newS()}}));
            depsByNameList.add(
                    newM_S_SS(
                            new Object[][]{
                                    {"elem1",newS()}}));

            final String inputStr = depsByNameList.toString();

            try {
                JadecyUtils.computeGraphMergedFromGraphLms(depsByNameList);
                assertTrue(false);
            } catch (IllegalArgumentException e) {
                // ok
            }

            // Not modified.
            assertEquals(inputStr, depsByNameList.toString());
        }
    }

    public void test_computeGraphMergedFromGraphLms_normal() {

        final List<SortedMap<String,SortedSet<String>>> depsByNameList =
                new ArrayList<SortedMap<String,SortedSet<String>>>();
        //
        final SortedMap<String,SortedSet<String>> depsByName1 =
                new TreeMap<String,SortedSet<String>>();
        final SortedSet<String> deps1 = newS("dep1","dep2");
        depsByName1.put("elem1", deps1);
        final SortedSet<String> deps2 = newS("dep3","dep4");
        depsByName1.put("elem2", deps2);
        depsByNameList.add(depsByName1);
        //
        final SortedMap<String,SortedSet<String>> depsByName2 =
                new TreeMap<String,SortedSet<String>>();
        final SortedSet<String> deps3 = newS("dep5","dep6");
        depsByName1.put("elem3", deps3);
        depsByNameList.add(depsByName2);

        /*
         * 
         */

        final String inputStr = depsByNameList.toString();

        final SortedMap<String,SortedSet<String>> res =
                JadecyUtils.computeGraphMergedFromGraphLms(depsByNameList);

        // Not modified.
        assertEquals(inputStr, depsByNameList.toString());

        assertEquals(3, res.size());
        assertSame(deps1, res.get("elem1"));
        assertSame(deps2, res.get("elem2"));
        assertSame(deps3, res.get("elem3"));
    }

    public void test_computeGraphCauselessFromGraphLmms_exceptions() {

        try {
            JadecyUtils.computeGraphCauselessFromGraphLmms(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
    }

    public void test_computeGraphCauselessFromGraphLmms_normal() {

        /*
         * Doesn't check name unicity, since doesn't merge steps,
         * so doesn't throw, and we can use "elem1" in multiple steps.
         */

        final List<SortedMap<String,SortedMap<String,SortedSet<String>>>> causesByDepByNameList =
                new ArrayList<SortedMap<String,SortedMap<String,SortedSet<String>>>>();
        //
        final SortedMap<String,SortedMap<String,SortedSet<String>>> causesByDepByName1 =
                new TreeMap<String,SortedMap<String,SortedSet<String>>>();
        final SortedMap<String,SortedSet<String>> causesByDep1 =
                newM_S_SS(
                        new Object[][]{
                                {"dep1",newS("c1","c2")},
                                {"dep2",newS("c3","c4")}});
        causesByDepByName1.put("elem1", causesByDep1);
        final SortedMap<String,SortedSet<String>> causesByDep2 =
                newM_S_SS(
                        new Object[][]{
                                {"dep3",newS("c5","c6")},
                                {"dep4",newS("c7","c8")}});
        causesByDepByName1.put("elem2", causesByDep2);
        causesByDepByNameList.add(causesByDepByName1);
        //
        final SortedMap<String,SortedMap<String,SortedSet<String>>> causesByDepByName2 =
                new TreeMap<String,SortedMap<String,SortedSet<String>>>();
        final SortedMap<String,SortedSet<String>> causesByDep3 =
                newM_S_SS(
                        new Object[][]{
                                {"dep5",newS("c9","c10")},
                                {"dep6",newS("c11","c12")}});
        causesByDepByName2.put("elem1", causesByDep3);
        causesByDepByNameList.add(causesByDepByName2);

        /*
         * 
         */

        final String inputStr = causesByDepByNameList.toString();

        final List<SortedMap<String,SortedSet<String>>> res =
                JadecyUtils.computeGraphCauselessFromGraphLmms(causesByDepByNameList);

        // Not modified.
        assertEquals(inputStr, causesByDepByNameList.toString());

        assertEquals(2, res.size());
        {
            final SortedMap<String,SortedSet<String>> step = res.get(0);
            assertEquals(2, step.size());
            assertEquals("[dep1, dep2]", step.get("elem1").toString());
            assertEquals("[dep3, dep4]", step.get("elem2").toString());
        }
        {
            final SortedMap<String,SortedSet<String>> step = res.get(1);
            assertEquals(1, step.size());
            assertEquals("[dep5, dep6]", step.get("elem1").toString());
        }
    }

    public void test_computeGraphCauselessFromGraphMms_exceptions() {

        try {
            JadecyUtils.computeGraphCauselessFromGraphMms(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
    }

    public void test_computeGraphCauselessFromGraphMms_normal() {

        final SortedMap<String,SortedMap<String,SortedSet<String>>> causesByDepByName =
                new TreeMap<String,SortedMap<String,SortedSet<String>>>();
        causesByDepByName.put(
                "elem1",
                newM_S_SS(
                        new Object[][]{
                                {"dep1",newS("c1","c2")},
                                {"dep2",newS("c2","c3")}}));
        causesByDepByName.put(
                "elem2",
                newM_S_SS(
                        new Object[][]{
                                {"dep3",newS("c4","c5")}}));

        /*
         * 
         */

        final String inputStr = causesByDepByName.toString();

        final SortedMap<String,SortedSet<String>> res =
                JadecyUtils.computeGraphCauselessFromGraphMms(causesByDepByName);

        // Not modified.
        assertEquals(inputStr, causesByDepByName.toString());

        assertEquals(2, res.size());
        assertEquals("[dep1, dep2]", res.get("elem1").toString());
        assertEquals("[dep3]", res.get("elem2").toString());
    }

    public void test_computeGraphMergedAndCauselessFromGraphLmms_exceptions() {

        try {
            JadecyUtils.computeGraphMergedAndCauselessFromGraphLmms(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        // Duplicate name.
        {
            final List<SortedMap<String,SortedMap<String,SortedSet<String>>>> causesByDepByNameList =
                    new ArrayList<SortedMap<String,SortedMap<String,SortedSet<String>>>>();
            {
                final SortedMap<String,SortedMap<String,SortedSet<String>>> causesByDepByName =
                        new TreeMap<String,SortedMap<String,SortedSet<String>>>();
                causesByDepByName.put("elem1", new TreeMap<String,SortedSet<String>>());
                causesByDepByNameList.add(causesByDepByName);
            }
            {
                final SortedMap<String,SortedMap<String,SortedSet<String>>> causesByDepByName =
                        new TreeMap<String,SortedMap<String,SortedSet<String>>>();
                causesByDepByName.put("elem1", new TreeMap<String,SortedSet<String>>());
                causesByDepByNameList.add(causesByDepByName);
            }

            final String inputStr = causesByDepByNameList.toString();

            try {
                JadecyUtils.computeGraphMergedAndCauselessFromGraphLmms(causesByDepByNameList);
                assertTrue(false);
            } catch (IllegalArgumentException e) {
                // ok
            }

            // Not modified.
            assertEquals(inputStr, causesByDepByNameList.toString());
        }
    }

    public void test_computeGraphMergedAndCauselessFromGraphLmms_normal() {

        final List<SortedMap<String,SortedMap<String,SortedSet<String>>>> causesByDepByNameList =
                new ArrayList<SortedMap<String,SortedMap<String,SortedSet<String>>>>();
        //
        final SortedMap<String,SortedMap<String,SortedSet<String>>> causesByDepByName1 =
                new TreeMap<String,SortedMap<String,SortedSet<String>>>();
        final SortedMap<String,SortedSet<String>> causesByDep1 =
                newM_S_SS(
                        new Object[][]{
                                {"dep1",newS("c1","c2")},
                                {"dep2",newS("c3","c4")}});
        causesByDepByName1.put("elem1", causesByDep1);
        final SortedMap<String,SortedSet<String>> causesByDep2 =
                newM_S_SS(
                        new Object[][]{
                                {"dep3",newS("c5","c6")},
                                {"dep4",newS("c7","c8")}});
        causesByDepByName1.put("elem2", causesByDep2);
        causesByDepByNameList.add(causesByDepByName1);
        //
        final SortedMap<String,SortedMap<String,SortedSet<String>>> causesByDepByName2 =
                new TreeMap<String,SortedMap<String,SortedSet<String>>>();
        final SortedMap<String,SortedSet<String>> causesByDep3 =
                newM_S_SS(
                        new Object[][]{
                                {"dep5",newS("c9","c10")},
                                {"dep6",newS("c11","c12")}});
        causesByDepByName2.put("elem3", causesByDep3);
        causesByDepByNameList.add(causesByDepByName2);

        /*
         * 
         */

        final String inputStr = causesByDepByNameList.toString();

        final SortedMap<String,SortedSet<String>> res =
                JadecyUtils.computeGraphMergedAndCauselessFromGraphLmms(causesByDepByNameList);

        // Not modified.
        assertEquals(inputStr, causesByDepByNameList.toString());

        assertEquals(3, res.size());
        assertEquals("[dep1, dep2]", res.get("elem1").toString());
        assertEquals("[dep3, dep4]", res.get("elem2").toString());
        assertEquals("[dep5, dep6]", res.get("elem3").toString());
    }

    /*
     * Paths reduction.
     */

    public void test_computePathCauselessLms_exceptions() {

        try {
            JadecyUtils.computePathCauselessLms(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        // Map (used as pair) with size different than 1.
        for (boolean zeroElseTwo : new boolean[]{false,true}) {
            final List<SortedMap<String,SortedSet<String>>> depCausesByNameList =
                    new ArrayList<SortedMap<String,SortedSet<String>>>();
            if (zeroElseTwo) {
                depCausesByNameList.add(
                        newM_S_SS(
                                new Object[][]{}));
            } else {
                depCausesByNameList.add(
                        newM_S_SS(
                                new Object[][]{
                                        {"elem1",newS()},
                                        {"elem2",newS()}}));
            }

            final String inputStr = depCausesByNameList.toString();

            try {
                JadecyUtils.computePathCauselessLms(depCausesByNameList);
                assertTrue(false);
            } catch (IllegalArgumentException e) {
                // ok
            }

            // Not modified.
            assertEquals(inputStr, depCausesByNameList.toString());
        }

        // Last element must have no cause, since path ends here.
        {
            final List<SortedMap<String,SortedSet<String>>> depCausesByNameList =
                    new ArrayList<SortedMap<String,SortedSet<String>>>();
            depCausesByNameList.add(
                    newM_S_SS(
                            new Object[][]{
                                    {"elem1",newS("c1","c2")}}));
            depCausesByNameList.add(
                    newM_S_SS(
                            new Object[][]{
                                    {"elem2",newS("c3")}}));

            final String inputStr = depCausesByNameList.toString();

            try {
                JadecyUtils.computePathCauselessLms(depCausesByNameList);
                assertTrue(false);
            } catch (IllegalArgumentException e) {
                // ok
            }

            // Not modified.
            assertEquals(inputStr, depCausesByNameList.toString());
        }
    }

    public void test_computePathCauselessLms_normal() {

        // Path going through some vertices multiple times
        // (should not hurt).
        final List<SortedMap<String,SortedSet<String>>> depCausesByNameList =
                new ArrayList<SortedMap<String,SortedSet<String>>>();
        depCausesByNameList.add(
                newM_S_SS(
                        new Object[][]{
                                {"elem1",newS("c1","c2")}}));
        depCausesByNameList.add(
                newM_S_SS(
                        new Object[][]{
                                {"elem2",newS("c3","c4")}}));
        depCausesByNameList.add(
                newM_S_SS(
                        new Object[][]{
                                {"elem1",newS("c5","c6")}}));
        depCausesByNameList.add(
                newM_S_SS(
                        new Object[][]{
                                {"elem3",newS()}}));

        /*
         * 
         */

        final String inputStr = depCausesByNameList.toString();

        final List<String> res =
                JadecyUtils.computePathCauselessLms(depCausesByNameList);

        // Not modified.
        assertEquals(inputStr, depCausesByNameList.toString());

        assertEquals("[elem1, elem2, elem1, elem3]", res.toString());
    }

    /*
     * Paths to graphs.
     */

    public void test_computeGraphFromPathL_exceptions() {

        try {
            JadecyUtils.computeGraphFromPathL(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
    }

    public void test_computeGraphFromPathL_normal() {

        // elem3 depends on elem2 and then elem1,
        // to check that they are reordered in result.
        // elem3 depends twice on elem1,
        // to check that dependencies only appear once in result.
        final List<String> nameList =
                newL("elem1","elem3","elem2","elem3","elem1","elem3","elem1");

        /*
         * 
         */

        final String inputStr = nameList.toString();

        final SortedMap<String,SortedSet<String>> res =
                JadecyUtils.computeGraphFromPathL(nameList);

        // Not modified.
        assertEquals(inputStr, nameList.toString());

        assertEquals(3, res.size());
        assertEquals("[elem3]", res.get("elem1").toString());
        assertEquals("[elem3]", res.get("elem2").toString());
        assertEquals("[elem1, elem2]", res.get("elem3").toString());
    }

    /*
     * Cycles to graphs.
     */

    public void test_computeGraphFromCycle_2array_exceptions() {

        try {
            JadecyUtils.computeGraphFromCycle(
                    null,
                    new String[][]{});
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            JadecyUtils.computeGraphFromCycle(
                    new String[]{null},
                    new String[][]{});
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
    }

    public void test_computeGraphFromCycle_2array_normal() {
        for (boolean withCauses : new boolean[]{false,true}) {
            // elem3 depends on elem2 and then elem1,
            // to check that they are reordered in result.
            // elem3 depends twice on elem1,
            // to check that dependencies only appear once in result.
            final String[] cycle =
                    new String[]{"elem1","elem3","elem2","elem3","elem1","elem3","elem1"};
            final String[][] causesArr;
            if (withCauses) {
                causesArr =
                        new String[][]{
                        {"c1","c2"},
                        {"c3"},
                        {}, // Should not hurt.
                        {"c5"},
                        {"c6"},
                        {"c7"},
                        {"c8"},
                };
            } else {
                causesArr = null;
            }

            /*
             * 
             */

            final String inputStr = Arrays.toString(cycle);
            final String[] inputStr2;
            if (withCauses) {
                inputStr2 = new String[causesArr.length];
                for (int i = 0; i < causesArr.length; i++) {
                    inputStr2[i] = Arrays.toString(causesArr[i]);
                }
            } else {
                inputStr2 = null;
            }

            final SortedMap<String,SortedMap<String,SortedSet<String>>> res =
                    JadecyUtils.computeGraphFromCycle(
                            cycle,
                            causesArr);

            // Not modified.
            assertEquals(inputStr, Arrays.toString(cycle));
            if (withCauses) {
                for (int i = 0; i < causesArr.length; i++) {
                    assertEquals(inputStr2[i], Arrays.toString(causesArr[i]));
                }
            }

            assertEquals(3, res.size());
            {
                final SortedMap<String,SortedSet<String>> causesByDep = res.get("elem1");
                assertEquals(2, causesByDep.size());
                if (withCauses) {
                    assertEquals("[c8]", causesByDep.get("elem1").toString());
                    assertEquals("[c1, c2, c6]", causesByDep.get("elem3").toString());
                } else {
                    assertEquals("[]", causesByDep.get("elem1").toString());
                    assertEquals("[]", causesByDep.get("elem3").toString());
                }
            }
            {
                // No cause, but still have the dep.
                final SortedMap<String,SortedSet<String>> causesByDep = res.get("elem2");
                assertEquals(1, causesByDep.size());
                assertEquals("[]", causesByDep.get("elem3").toString());
            }
            {
                final SortedMap<String,SortedSet<String>> causesByDep = res.get("elem3");
                assertEquals(2, causesByDep.size());
                if (withCauses) {
                    assertEquals("[c5, c7]", causesByDep.get("elem1").toString());
                    assertEquals("[c3]", causesByDep.get("elem2").toString());
                } else {
                    assertEquals("[]", causesByDep.get("elem1").toString());
                    assertEquals("[]", causesByDep.get("elem2").toString());
                }
            }
        }
    }

    public void test_computeGraphFromCycle_array_exceptions() {

        try {
            JadecyUtils.computeGraphFromCycle(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            JadecyUtils.computeGraphFromCycle(new String[]{null});
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
    }

    public void test_computeGraphFromCycle_array_normal() {

        // elem3 depends on elem2 and then elem1,
        // to check that they are reordered in result.
        // elem3 depends twice on elem1,
        // to check that dependencies only appear once in result.
        final String[] cycle =
                new String[]{"elem1","elem3","elem2","elem3","elem1","elem3","elem1"};

        /*
         * 
         */

        final String inputStr = Arrays.toString(cycle);

        final SortedMap<String,SortedSet<String>> res =
                JadecyUtils.computeGraphFromCycle(cycle);

        // Not modified.
        assertEquals(inputStr, Arrays.toString(cycle));

        assertEquals(3, res.size());
        assertEquals("[elem1, elem3]", res.get("elem1").toString());
        assertEquals("[elem3]", res.get("elem2").toString());
        assertEquals("[elem1, elem2]", res.get("elem3").toString());
    }

    /*
     * Bulk dependencies stats.
     */

    public void test_computeByteSizeM_exceptions() {

        try {
            JadecyUtils.computeByteSizeM(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
    }

    public void test_computeByteSizeM_normal() {

        final SortedMap<String,Long> byteSizeByName =
                newM_S_L(
                        new Object[][]{
                                {"elem1",17L},
                                {"elem2",21L}});

        /*
         * 
         */

        final String inputStr = byteSizeByName.toString();

        final long res = JadecyUtils.computeByteSizeM(byteSizeByName);

        // Not modified.
        assertEquals(inputStr, byteSizeByName.toString());

        assertEquals(38L, res);
    }

    public void test_computeByteSizeLm_exceptions() {

        try {
            JadecyUtils.computeByteSizeLm(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        // Duplicate name.
        {
            final List<SortedMap<String,Long>> byteSizeByNameList =
                    new ArrayList<SortedMap<String,Long>>();
            byteSizeByNameList.add(
                    newM_S_L(
                            new Object[][]{
                                    {"elem1",17L},
                                    {"elem2",21L}}));
            byteSizeByNameList.add(
                    newM_S_L(
                            new Object[][]{
                                    {"elem3",12L},
                                    {"elem1",71L}}));

            try {
                JadecyUtils.computeByteSizeLm(byteSizeByNameList);
                assertTrue(false);
            } catch (IllegalArgumentException e) {
                // ok
            }
        }
    }

    public void test_computeByteSizeLm_normal() {

        final List<SortedMap<String,Long>> byteSizeByNameList =
                new ArrayList<SortedMap<String,Long>>();
        byteSizeByNameList.add(
                newM_S_L(
                        new Object[][]{
                                {"elem1",17L},
                                {"elem2",21L}}));
        byteSizeByNameList.add(
                newM_S_L(
                        new Object[][]{
                                {"elem3",12L},
                                {"elem4",71L}}));

        /*
         * 
         */

        final String inputStr = byteSizeByNameList.toString();

        final long res = JadecyUtils.computeByteSizeLm(byteSizeByNameList);

        // Not modified.
        assertEquals(inputStr, byteSizeByNameList.toString());

        // Palindromic sum!
        assertEquals(38L+83L, res);
    }

    public void test_computeCountBySizeLm_exceptions() {

        try {
            JadecyUtils.computeCountBySizeLm(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
    }

    public void test_computeCountBySizeLm_normal() {

        /*
         * Doesn't check name unicity, since doesn't merge SCCs,
         * so doesn't throw, and we can use "elem1" in multiple SCCs.
         */

        final List<SortedMap<String,Long>> byteSizeByNameList =
                new ArrayList<SortedMap<String,Long>>();
        byteSizeByNameList.add(
                newM_S_L(
                        new Object[][]{
                                {"elem1",17L},
                                {"elem2",21L}}));
        byteSizeByNameList.add(
                newM_S_L(
                        new Object[][]{
                                {"elem3",12L}}));
        byteSizeByNameList.add(
                newM_S_L(
                        new Object[][]{
                                {"elem1",71L}}));
        // Handles empty maps, even if it makes no sense.
        byteSizeByNameList.add(
                newM_S_L(
                        new Object[][]{}));

        /*
         * 
         */

        final String inputStr = byteSizeByNameList.toString();

        final SortedMap<Integer,Integer> res =
                JadecyUtils.computeCountBySizeLm(byteSizeByNameList);

        // Not modified.
        assertEquals(inputStr, byteSizeByNameList.toString());

        assertEquals(3, res.size());
        assertEquals((Integer) 1, res.get(0));
        assertEquals((Integer) 2, res.get(1));
        assertEquals((Integer) 1, res.get(2));
    }

    public void test_computeCountBySizeLs_exceptions() {

        try {
            JadecyUtils.computeCountBySizeLs(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
    }

    public void test_computeCountBySizeLs_normal() {

        /*
         * Doesn't check name unicity, since doesn't merge SCCs,
         * so doesn't throw, and we can use "elem1" in multiple SCCs.
         */

        final List<SortedSet<String>> nameSetList =
                new ArrayList<SortedSet<String>>();
        nameSetList.add(
                newS("elem1","elem2"));
        nameSetList.add(
                newS("elem3"));
        nameSetList.add(
                newS("elem1"));
        // Handles empty sets, even if it makes no sense.
        // Can't use newS() with 1.5 source compatibility.
        nameSetList.add(
                new TreeSet<String>());

        /*
         * 
         */

        final String inputStr = nameSetList.toString();

        final SortedMap<Integer,Integer> res =
                JadecyUtils.computeCountBySizeLs(nameSetList);

        // Not modified.
        assertEquals(inputStr, nameSetList.toString());

        assertEquals(3, res.size());
        assertEquals((Integer) 1, res.get(0));
        assertEquals((Integer) 2, res.get(1));
        assertEquals((Integer) 1, res.get(2));
    }

    /*
     * Names printing.
     */
    
    public void test_printNamesC_exceptions() {

        final Collection<String> nameColl = new ArrayList<String>();
        final MemPrintStream stream = new MemPrintStream();

        try {
            try {
                JadecyUtils.printNamesC(
                        null,
                        stream);
                assertTrue(false);
            } catch (NullPointerException e) {
                // ok
            }

            try {
                JadecyUtils.printNamesC(
                        nameColl,
                        null);
                assertTrue(false);
            } catch (NullPointerException e) {
                // ok
            }
        } finally {
            stream.close();
        }
    }

    public void test_printNamesC_normal() {

        final MemPrintStream stream = new MemPrintStream();
        try {
            final Collection<String> nameColl =
                    newL("elem1","elem2","elem1","elem3");

            /*
             * 
             */

            final String inputStr = nameColl.toString();

            stream.clear();
            JadecyUtils.printNamesC(
                    nameColl,
                    stream);

            // Not modified.
            assertEquals(inputStr, nameColl.toString());

            final String[] expected = new String[]{
                    "elem1",
                    "elem2",
                    "elem1",
                    "elem3",
            };
            checkEqual(expected, stream);
        } finally {
            stream.close();
        }
    }

    /*
     * Bulk dependencies printing.
     */

    public void test_printDepsLm_exceptions() {

        final List<SortedMap<String,Long>> byteSizeByNameList =
                new ArrayList<SortedMap<String,Long>>();
        final MemPrintStream stream = new MemPrintStream();

        try {
            try {
                JadecyUtils.printDepsLm(
                        null,
                        stream);
                assertTrue(false);
            } catch (NullPointerException e) {
                // ok
            }

            try {
                JadecyUtils.printDepsLm(
                        byteSizeByNameList,
                        null);
                assertTrue(false);
            } catch (NullPointerException e) {
                // ok
            }
        } finally {
            stream.close();
        }
    }

    public void test_printDepsLm_normal() {

        final MemPrintStream stream = new MemPrintStream();
        try {
            final List<SortedMap<String,Long>> byteSizeByNameList =
                    new ArrayList<SortedMap<String,Long>>();
            byteSizeByNameList.add(
                    newM_S_L(
                            new Object[][]{
                                    {"elem1",17L},
                                    {"elem2",21L}}));
            byteSizeByNameList.add(
                    newM_S_L(
                            new Object[][]{
                                    {"elem3",12L},
                                    {"elem1",71L}}));

            /*
             * 
             */

            final String inputStr = byteSizeByNameList.toString();

            JadecyUtils.printDepsLm(
                    byteSizeByNameList,
                    stream);

            // Not modified.
            assertEquals(inputStr, byteSizeByNameList.toString());

            final String[] expected = new String[]{
                    "step 0:",
                    "elem1: 17",
                    "elem2: 21",
                    "",
                    "step 1:",
                    "elem1: 71",
                    "elem3: 12",
            };
            checkEqual(expected, stream);
        } finally {
            stream.close();
        }
    }

    public void test_printDepsM_exceptions() {

        final SortedMap<String,Long> byteSizeByName =
                new TreeMap<String,Long>();
        final MemPrintStream stream = new MemPrintStream();

        try {
            try {
                JadecyUtils.printDepsM(
                        null,
                        stream);
                assertTrue(false);
            } catch (NullPointerException e) {
                // ok
            }

            try {
                JadecyUtils.printDepsM(
                        byteSizeByName,
                        null);
                assertTrue(false);
            } catch (NullPointerException e) {
                // ok
            }
        } finally {
            stream.close();
        }
    }

    public void test_printDepsM_normal() {

        final MemPrintStream stream = new MemPrintStream();
        try {
            final SortedMap<String,Long> byteSizeByName =
                    new TreeMap<String,Long>();
            byteSizeByName.put("elem1", 17L);
            byteSizeByName.put("elem2", 21L);

            /*
             * 
             */

            final String inputStr = byteSizeByName.toString();

            JadecyUtils.printDepsM(
                    byteSizeByName,
                    stream);

            // Not modified.
            assertEquals(inputStr, byteSizeByName.toString());

            final String[] expected = new String[]{
                    "elem1: 17",
                    "elem2: 21",
            };
            checkEqual(expected, stream);
        } finally {
            stream.close();
        }
    }

    /*
     * Graphs printing.
     */

    public void test_printGraphLmms_exceptions() {

        final List<SortedMap<String,SortedMap<String,SortedSet<String>>>> causesByDepByNameList =
                new ArrayList<SortedMap<String,SortedMap<String,SortedSet<String>>>>();
        final MemPrintStream stream = new MemPrintStream();

        try {
            try {
                JadecyUtils.printGraphLmms(
                        null,
                        false,
                        stream);
                assertTrue(false);
            } catch (NullPointerException e) {
                // ok
            }

            try {
                JadecyUtils.printGraphLmms(
                        causesByDepByNameList,
                        false,
                        null);
                assertTrue(false);
            } catch (NullPointerException e) {
                // ok
            }
        } finally {
            stream.close();
        }
    }

    public void test_printGraphLmms_normal() {

        final MemPrintStream stream = new MemPrintStream();
        try {
            for (boolean mustPrintCauses : new boolean[]{false,true}) {
                final List<SortedMap<String,SortedMap<String,SortedSet<String>>>> causesByDepByNameList =
                        new ArrayList<SortedMap<String,SortedMap<String,SortedSet<String>>>>();
                //
                final SortedMap<String,SortedMap<String,SortedSet<String>>> causesByDepByName1 =
                        new TreeMap<String,SortedMap<String,SortedSet<String>>>();
                final SortedMap<String,SortedSet<String>> causesByDep1 =
                        newM_S_SS(
                                new Object[][]{
                                        {"dep1",newS("c1","c2")},
                                        {"dep2",newS("c3")}});
                causesByDepByName1.put("elem1", causesByDep1);
                final SortedMap<String,SortedSet<String>> causesByDep2 =
                        newM_S_SS(
                                new Object[][]{
                                        {"dep3",newS("c4")},
                                        {"dep4",newS("c5")}});
                causesByDepByName1.put("elem2", causesByDep2);
                causesByDepByNameList.add(causesByDepByName1);
                //
                final SortedMap<String,SortedMap<String,SortedSet<String>>> causesByDepByName2 =
                        new TreeMap<String,SortedMap<String,SortedSet<String>>>();
                final SortedMap<String,SortedSet<String>> causesByDep3 =
                        newM_S_SS(
                                new Object[][]{
                                        {"dep5",newS("c6")},
                                        {"dep6",newS("c7")}});
                causesByDepByName2.put("elem3", causesByDep3);
                causesByDepByNameList.add(causesByDepByName2);

                /*
                 * 
                 */

                final String inputStr = causesByDepByNameList.toString();

                stream.clear();
                JadecyUtils.printGraphLmms(
                        causesByDepByNameList,
                        mustPrintCauses,
                        stream);

                // Not modified.
                assertEquals(inputStr, causesByDepByNameList.toString());

                final String[] expected;
                if (mustPrintCauses) {
                    expected = new String[]{
                            "step 0:",
                            "elem1",
                            "      c1",
                            "      c2",
                            "   -> dep1",
                            "      c3",
                            "   -> dep2",
                            "elem2",
                            "      c4",
                            "   -> dep3",
                            "      c5",
                            "   -> dep4",
                            "",
                            "step 1:",
                            "elem3",
                            "      c6",
                            "   -> dep5",
                            "      c7",
                            "   -> dep6",
                    };
                } else {
                    expected = new String[]{
                            "step 0:",
                            "elem1",
                            "   -> dep1",
                            "   -> dep2",
                            "elem2",
                            "   -> dep3",
                            "   -> dep4",
                            "",
                            "step 1:",
                            "elem3",
                            "   -> dep5",
                            "   -> dep6",
                    };
                }
                checkEqual(expected, stream);
            }
        } finally {
            stream.close();
        }
    }

    public void test_printGraphMms_exceptions() {

        final SortedMap<String,SortedMap<String,SortedSet<String>>> causesByDepByName =
                new TreeMap<String,SortedMap<String,SortedSet<String>>>();
        final MemPrintStream stream = new MemPrintStream();

        try {
            try {
                JadecyUtils.printGraphMms(
                        null,
                        false,
                        stream);
                assertTrue(false);
            } catch (NullPointerException e) {
                // ok
            }

            try {
                JadecyUtils.printGraphMms(
                        causesByDepByName,
                        false,
                        null);
                assertTrue(false);
            } catch (NullPointerException e) {
                // ok
            }
        } finally {
            stream.close();
        }
    }

    public void test_printGraphMms_normal() {

        final MemPrintStream stream = new MemPrintStream();
        try {
            for (boolean mustPrintCauses : new boolean[]{false,true}) {
                final SortedMap<String,SortedMap<String,SortedSet<String>>> causesByDepByName =
                        new TreeMap<String,SortedMap<String,SortedSet<String>>>();
                causesByDepByName.put(
                        "elem1",
                        newM_S_SS(
                                new Object[][]{
                                        {"dep1",newS("c1","c2")},
                                        {"dep2",newS("c3")}}));
                causesByDepByName.put(
                        "elem2",
                        newM_S_SS(
                                new Object[][]{
                                        {"dep3",newS("c4")}}));

                /*
                 * 
                 */

                final String inputStr = causesByDepByName.toString();

                stream.clear();
                JadecyUtils.printGraphMms(
                        causesByDepByName,
                        mustPrintCauses,
                        stream);

                // Not modified.
                assertEquals(inputStr, causesByDepByName.toString());

                final String[] expected;
                if (mustPrintCauses) {
                    expected = new String[]{
                            "elem1",
                            "      c1",
                            "      c2",
                            "   -> dep1",
                            "      c3",
                            "   -> dep2",
                            "elem2",
                            "      c4",
                            "   -> dep3",
                    };
                } else {
                    expected = new String[]{
                            "elem1",
                            "   -> dep1",
                            "   -> dep2",
                            "elem2",
                            "   -> dep3",
                    };
                }
                checkEqual(expected, stream);
            }
        } finally {
            stream.close();
        }
    }

    public void test_printGraphLms_exceptions() {

        final List<SortedMap<String,SortedSet<String>>> depsByNameList =
                new ArrayList<SortedMap<String,SortedSet<String>>>();
        final MemPrintStream stream = new MemPrintStream();

        try {
            try {
                JadecyUtils.printGraphLms(
                        null,
                        stream);
                assertTrue(false);
            } catch (NullPointerException e) {
                // ok
            }

            try {
                JadecyUtils.printGraphLms(
                        depsByNameList,
                        null);
                assertTrue(false);
            } catch (NullPointerException e) {
                // ok
            }
        } finally {
            stream.close();
        }
    }
    
    public void test_printGraphLms_normal() {

        final MemPrintStream stream = new MemPrintStream();
        try {
            final List<SortedMap<String,SortedSet<String>>> depsByNameList =
                    new ArrayList<SortedMap<String,SortedSet<String>>>();
            //
            final SortedMap<String,SortedSet<String>> depsByName1 =
                    new TreeMap<String,SortedSet<String>>();
            depsByName1.put(
                    "elem1",
                    newS("dep1","dep2"));
            depsByName1.put(
                    "elem2",
                    newS("dep3"));
            depsByNameList.add(depsByName1);
            //
            final SortedMap<String,SortedSet<String>> depsByName2 =
                    new TreeMap<String,SortedSet<String>>();
            depsByName2.put(
                    "elem3",
                    newS("dep4"));
            depsByNameList.add(depsByName2);

            /*
             * 
             */

            final String inputStr = depsByNameList.toString();

            stream.clear();
            JadecyUtils.printGraphLms(
                    depsByNameList,
                    stream);

            // Not modified.
            assertEquals(inputStr, depsByNameList.toString());

            final String[] expected = new String[]{
                    "step 0:",
                    "elem1",
                    "   -> dep1",
                    "   -> dep2",
                    "elem2",
                    "   -> dep3",
                    "",
                    "step 1:",
                    "elem3",
                    "   -> dep4",
            };
            checkEqual(expected, stream);
        } finally {
            stream.close();
        }
    }

    public void test_printGraphMs_exceptions() {

        final SortedMap<String,SortedSet<String>> depsByName =
                new TreeMap<String,SortedSet<String>>();
        final MemPrintStream stream = new MemPrintStream();

        try {
            try {
                JadecyUtils.printGraphMs(
                        null,
                        stream);
                assertTrue(false);
            } catch (NullPointerException e) {
                // ok
            }

            try {
                JadecyUtils.printGraphMs(
                        depsByName,
                        null);
                assertTrue(false);
            } catch (NullPointerException e) {
                // ok
            }
        } finally {
            stream.close();
        }
    }

    public void test_printGraphMs_normal() {

        final MemPrintStream stream = new MemPrintStream();
        try {
            final SortedMap<String,SortedSet<String>> depsByName =
                    new TreeMap<String,SortedSet<String>>();
            depsByName.put(
                    "elem1",
                    newS("dep1","dep2"));
            depsByName.put(
                    "elem2",
                    newS("dep3"));

            /*
             * 
             */

            final String inputStr = depsByName.toString();

            stream.clear();
            JadecyUtils.printGraphMs(
                    depsByName,
                    stream);

            // Not modified.
            assertEquals(inputStr, depsByName.toString());

            final String[] expected = new String[]{
                    "elem1",
                    "   -> dep1",
                    "   -> dep2",
                    "elem2",
                    "   -> dep3",
            };
            checkEqual(expected, stream);
        } finally {
            stream.close();
        }
    }

    public void test_printGraphInDOTFormatMs_exceptions() {

        final SortedMap<String,SortedSet<String>> depsByName =
                new TreeMap<String,SortedSet<String>>();
        final MemPrintStream stream = new MemPrintStream();

        try {
            try {
                JadecyUtils.printGraphInDOTFormatMs(
                        null,
                        "dummy",
                        stream);
                assertTrue(false);
            } catch (NullPointerException e) {
                // ok
            }

            try {
                JadecyUtils.printGraphInDOTFormatMs(
                        depsByName,
                        null,
                        stream);
                assertTrue(false);
            } catch (NullPointerException e) {
                // ok
            }

            try {
                JadecyUtils.printGraphInDOTFormatMs(
                        depsByName,
                        "dummy",
                        null);
                assertTrue(false);
            } catch (NullPointerException e) {
                // ok
            }
        } finally {
            stream.close();
        }
    }

    public void test_printGraphInDOTFormatMs_normal() {

        final MemPrintStream stream = new MemPrintStream();
        try {
            final SortedMap<String,SortedSet<String>> depsByName =
                    new TreeMap<String,SortedSet<String>>();
            depsByName.put(
                    "elem1",
                    newS("dep1","dep2"));
            depsByName.put(
                    "elem2",
                    newS("dep3"));

            final String graphName = "some name";

            /*
             * 
             */

            final String inputStr = depsByName.toString();

            stream.clear();
            JadecyUtils.printGraphInDOTFormatMs(
                    depsByName,
                    graphName,
                    stream);

            // Not modified.
            assertEquals(inputStr, depsByName.toString());

            final String[] expected = new String[]{
                    "digraph " + NameUtils.quoted(graphName) + " {",
                    "   " + NameUtils.quoted("elem1") + " -> " + NameUtils.quoted("dep1") + ";",
                    "   " + NameUtils.quoted("elem1") + " -> " + NameUtils.quoted("dep2") + ";",
                    "   " + NameUtils.quoted("elem2") + " -> " + NameUtils.quoted("dep3") + ";",
                    "}",
            };
            checkEqual(expected, stream);
        } finally {
            stream.close();
        }
    }

    /*
     * Paths printing.
     */

    public void test_printPathLms_exceptions() {

        final List<SortedMap<String,SortedSet<String>>> depsByNameList =
                new ArrayList<SortedMap<String,SortedSet<String>>>();
        final MemPrintStream stream = new MemPrintStream();

        try {
            try {
                JadecyUtils.printPathLms(
                        null,
                        false,
                        stream);
                assertTrue(false);
            } catch (NullPointerException e) {
                // ok
            }

            try {
                JadecyUtils.printPathLms(
                        depsByNameList,
                        false,
                        null);
                assertTrue(false);
            } catch (NullPointerException e) {
                // ok
            }
        } finally {
            stream.close();
        }
    }

    public void test_printPathLms_normal() {

        final MemPrintStream stream = new MemPrintStream();
        try {
            for (boolean mustPrintCauses : new boolean[]{false,true}) {
                // For print, we accept last element to have dependency causes,
                // to nothing since it's the last element.
                for (boolean lastWithCauses : new boolean[]{false,true}) {
                    // Path going through some vertices multiple times
                    // (should not hurt).
                    final List<SortedMap<String,SortedSet<String>>> depCausesByNameList =
                            new ArrayList<SortedMap<String,SortedSet<String>>>();
                    depCausesByNameList.add(
                            newM_S_SS(
                                    new Object[][]{
                                            {"elem1",newS("c1","c2")}}));
                    depCausesByNameList.add(
                            newM_S_SS(
                                    new Object[][]{
                                            {"elem2",newS("c3")}}));
                    depCausesByNameList.add(
                            newM_S_SS(
                                    new Object[][]{
                                            {"elem1",newS("c4")}}));
                    if (lastWithCauses) {
                        depCausesByNameList.add(
                                newM_S_SS(
                                        new Object[][]{
                                                {"elem3",newS("foolish")}})); // Not complaining.
                    } else {
                        depCausesByNameList.add(
                                newM_S_SS(
                                        new Object[][]{
                                                {"elem3",newS()}}));
                    }

                    /*
                     * 
                     */

                    final String inputStr = depCausesByNameList.toString();

                    stream.clear();
                    JadecyUtils.printPathLms(
                            depCausesByNameList,
                            mustPrintCauses,
                            stream);

                    // Not modified.
                    assertEquals(inputStr, depCausesByNameList.toString());

                    final String[] expected;
                    if (mustPrintCauses) {
                        if (lastWithCauses) {
                            expected = new String[]{
                                    "elem1",
                                    "   c1",
                                    "   c2",
                                    "elem2",
                                    "   c3",
                                    "elem1",
                                    "   c4",
                                    "elem3",
                                    "   foolish",
                            };
                        } else {
                            expected = new String[]{
                                    "elem1",
                                    "   c1",
                                    "   c2",
                                    "elem2",
                                    "   c3",
                                    "elem1",
                                    "   c4",
                                    "elem3",
                            };
                        }
                    } else {
                        expected = new String[]{
                                "elem1",
                                "elem2",
                                "elem1",
                                "elem3",
                        };
                    }
                    checkEqual(expected, stream);
                }
            }
        } finally {
            stream.close();
        }
    }

    /*
     * SCCs printing.
     */

    public void test_printSccS_exceptions() {

        final SortedMap<String,Long> byteSizeByName =
                new TreeMap<String,Long>();
        final MemPrintStream stream = new MemPrintStream();

        try {
            try {
                JadecyUtils.printSccS(
                        null,
                        stream);
                assertTrue(false);
            } catch (NullPointerException e) {
                // ok
            }

            try {
                JadecyUtils.printSccS(
                        byteSizeByName,
                        null);
                assertTrue(false);
            } catch (NullPointerException e) {
                // ok
            }
        } finally {
            stream.close();
        }
    }

    public void test_printSccS_normal() {

        final MemPrintStream stream = new MemPrintStream();
        try {
            // Path going through some vertices multiple times
            // (should not hurt).
            final SortedMap<String,Long> byteSizeByName =
                    new TreeMap<String,Long>();
            byteSizeByName.put("elem1",17L);
            byteSizeByName.put("elem2",21L);

            /*
             * 
             */

            final String inputStr = byteSizeByName.toString();

            stream.clear();
            JadecyUtils.printSccS(
                    byteSizeByName,
                    stream);

            // Not modified.
            assertEquals(inputStr, byteSizeByName.toString());

            final String[] expected = new String[]{
                    "elem1: 17",
                    "elem2: 21",
            };
            checkEqual(expected, stream);
        } finally {
            stream.close();
        }
    }

    public void test_printSccsCs_exceptions() {

        final Collection<SortedMap<String,Long>> byteSizeByNameColl =
                new ArrayList<SortedMap<String,Long>>();
        final MemPrintStream stream = new MemPrintStream();

        try {
            try {
                JadecyUtils.printSccsCs(
                        null,
                        stream);
                assertTrue(false);
            } catch (NullPointerException e) {
                // ok
            }

            try {
                JadecyUtils.printSccsCs(
                        byteSizeByNameColl,
                        null);
                assertTrue(false);
            } catch (NullPointerException e) {
                // ok
            }
        } finally {
            stream.close();
        }
    }

    public void test_printSccsCs_normal() {

        final MemPrintStream stream = new MemPrintStream();
        try {
            
            /*
             * Doesn't check name unicity, since doesn't merge SCCs,
             * so doesn't throw, and we can use "elem1" in multiple SCCs.
             */
            
            final Collection<SortedMap<String,Long>> byteSizeByNameColl =
                    new ArrayList<SortedMap<String,Long>>();
            byteSizeByNameColl.add(
                    newM_S_L(
                            new Object[][]{
                                    {"elem1",17L},
                                    {"elem2",12L}}));
            byteSizeByNameColl.add(
                    newM_S_L(
                            new Object[][]{
                                    {"elem3",21L},
                                    {"elem1",71L}}));

            /*
             * 
             */

            final String inputStr = byteSizeByNameColl.toString();

            stream.clear();
            JadecyUtils.printSccsCs(
                    byteSizeByNameColl,
                    stream);

            // Not modified.
            assertEquals(inputStr, byteSizeByNameColl.toString());

            // Palindromic sums too!
            final String[] expected = new String[]{
                    "SCC 1 (29 bytes):",
                    "elem1: 17",
                    "elem2: 12",
                    "",
                    "SCC 2 (92 bytes):",
                    "elem1: 71",
                    "elem3: 21",
            };
            checkEqual(expected, stream);
        } finally {
            stream.close();
        }
    }

    /*
     * Cycles printing.
     */

    public void test_printCycle_exceptions() {

        final String[] names = new String[]{};
        final String[][] causesArr = new String[][]{};
        final MemPrintStream stream = new MemPrintStream();

        try {
            try {
                JadecyUtils.printCycle(
                        null,
                        causesArr,
                        stream);
                assertTrue(false);
            } catch (NullPointerException e) {
                // ok
            }

            try {
                JadecyUtils.printCycle(
                        names,
                        causesArr,
                        null);
                assertTrue(false);
            } catch (NullPointerException e) {
                // ok
            }
        } finally {
            stream.close();
        }
    }

    public void test_printCycle_normal() {

        final MemPrintStream stream = new MemPrintStream();
        try {
            for (boolean withCauses : new boolean[]{false,true}) {
                final String[] names = new String[]{"elem1","elem3","elem2"};
                final String[][] causesArr;
                if (withCauses) {
                    causesArr = new String[][]{
                            {"c2","c1"}, // Not reordered.
                            {}, // Should not hurt if have no cause.
                            {"c4"},
                    };
                } else {
                    causesArr = null;
                }

                /*
                 * 
                 */

                final String inputStr = Arrays.toString(names);
                final String[] inputStr2;
                if (withCauses) {
                    inputStr2 = new String[causesArr.length];
                    for (int i = 0; i < causesArr.length; i++) {
                        inputStr2[i] = Arrays.toString(causesArr[i]);
                    }
                } else {
                    inputStr2 = null;
                }

                stream.clear();
                JadecyUtils.printCycle(
                        names,
                        causesArr,
                        stream);

                // Not modified.
                assertEquals(inputStr, Arrays.toString(names));
                if (withCauses) {
                    for (int i = 0; i < causesArr.length; i++) {
                        assertEquals(inputStr2[i], Arrays.toString(causesArr[i]));
                    }
                }

                final String[] expected;
                if (withCauses) {
                    expected = new String[]{
                            "elem1",
                            "   c2",
                            "   c1",
                            "elem3",
                            "elem2",
                            "   c4",
                            "elem1",
                    };
                } else {
                    expected = new String[]{
                            "elem1",
                            "elem3",
                            "elem2",
                            "elem1",
                    };
                }
                checkEqual(expected, stream);
            }
        } finally {
            stream.close();
        }
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------

    private static SortedMap<String,Long> newM_S_L(Object[][] entries) {
        final SortedMap<String,Long> map = new TreeMap<String,Long>();
        for (int i = 0; i < entries.length; i++) {
            map.put((String) entries[i][0], (Long) entries[i][1]);
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    private static <E> List<E> newL(E... elements) {
        final List<E> list = new ArrayList<E>();
        for (E element : elements) {
            list.add(element);
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private static <E> SortedSet<E> newS(E... elements) {
        final SortedSet<E> set = new TreeSet<E>();
        for (E element : elements) {
            final boolean didAdd = set.add(element);
            if (!didAdd) {
                throw new AssertionError("duplicate: " + element);
            }
        }
        return set;
    }

    private static SortedMap<String,SortedSet<String>> newM_S_SS(Object[][] entries) {
        final SortedMap<String,SortedSet<String>> map = new TreeMap<String,SortedSet<String>>();
        for (int i = 0; i < entries.length; i++) {
            @SuppressWarnings("unchecked")
            final SortedSet<String> set = (SortedSet<String>) entries[i][1];
            map.put((String) entries[i][0], set);
        }
        return map;
    }

    private static void checkEqual(String[] expected, MemPrintStream stream) {
        PrintTestUtils.checkEqual(expected, PrintTestUtils.toStringTab(stream.getLines()));
    }
}
