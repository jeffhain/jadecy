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
package net.jadecy;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import net.jadecy.code.ClassData;
import net.jadecy.code.PackageData;
import net.jadecy.names.InterfaceNameFilter;
import net.jadecy.names.NameFilters;
import net.jadecy.names.NameUtils;
import net.jadecy.parsing.FsDepsParser;
import net.jadecy.parsing.InterfaceDepsParser;
import net.jadecy.parsing.ParsingFilters;
import net.jadecy.virtual.AbstractVirtualCodeGraphTezt;

public class JadecyTest extends AbstractVirtualCodeGraphTezt {

    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------

    private static final boolean DEBUG = false;

    //--------------------------------------------------------------------------
    // PRIVATE CLASSES
    //--------------------------------------------------------------------------

    /**
     * For normalized cycles or normalized SCCs.
     */
    private static class MyProcessed {
        private final String[] names;
        private final String[][] causesArr;
        /**
         * For SCCs or classes cycles.
         */
        public MyProcessed(String[] names) {
            this(names, null);
        }
        /**
         * For packages cycles.
         */
        public MyProcessed(
                String[] names,
                String[][] causesArr) {
            this.names = names;
            this.causesArr = causesArr;
            if ((causesArr != null)
                    && (causesArr.length != names.length)) {
                throw new AssertionError();
            }
        }
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("\n");
            if (this.causesArr == null) {
                sb.append("[names=" + Arrays.toString(this.names) + "]");
            } else {
                sb.append("[");
                for (int i = 0; i < this.names.length; i++) {
                    sb.append("name=" + this.names[i]);
                    sb.append("\n");
                    sb.append("causes=" + Arrays.toString(this.causesArr[i]));
                    sb.append("\n");
                }
                sb.append("]");
            }
            return sb.toString();
        }
        @Override
        public int hashCode() {
            int h = 0;
            for (String name : this.names) {
                h += 31 * h + name.hashCode();
            }
            return h;
        }
        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof MyProcessed)) {
                return false;
            }
            final MyProcessed ozer = (MyProcessed) other;
            return equal(this.names, ozer.names)
                    && equal2(this.causesArr, ozer.causesArr);
        }
    }

    private static class MyCycleProcessor implements InterfaceCycleProcessor {
        int counter = 0;
        final List<MyProcessed> processedList = new ArrayList<JadecyTest.MyProcessed>();
        @Override
        public boolean processCycle(String[] names, String[][] causesArr) {
            if (DEBUG) {
                System.out.println();
                System.out.println("cycle " + (++this.counter) + ":");
                JadecyUtils.printCycle(
                        names,
                        causesArr,
                        System.out);
            }
            this.processedList.add(new MyProcessed(names, causesArr));
            return false;
        }
    }

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    public void test_Jadecy_2boolean() {
        for (boolean mustMergeNestedClasses : new boolean[]{false,true}) {
            for (boolean apiOnly : new boolean[]{false,true}) {
                final Jadecy jadecy = new Jadecy(
                        mustMergeNestedClasses,
                        apiOnly);
                
                assertEquals(mustMergeNestedClasses, jadecy.parser().getMustMergeNestedClasses());
                assertEquals(apiOnly, jadecy.parser().getApiOnly());
            }
        }
    }
    
    public void test_Jadecy_InterfaceDepsParser_boolean_InterfaceNameFilter() {
        
        final FsDepsParser parser = new FsDepsParser(false, false);
        final InterfaceNameFilter filter = NameFilters.equalsName("foo");
        
        try {
            new Jadecy(
                    null,
                    false,
                    filter);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        try {
            new Jadecy(
                    parser,
                    false,
                    null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        for (boolean mustUseInverseDeps : new boolean[]{false,true}) {
            final Jadecy jadecy = new Jadecy(
                    parser,
                    mustUseInverseDeps,
                    filter);

            assertSame(parser, jadecy.parser());
            assertEquals(mustUseInverseDeps, jadecy.getMustUseInverseDeps());
            assertSame(filter, jadecy.getRetainedClassNameFilter());
        }
    }
    
    public void test_withMustUseInverseDeps_and_getMustUseInverseDeps() {
        final Jadecy refJdc = newJadecy();

        for (boolean mustUseInverseDeps : new boolean[]{false,true}) {

            if (DEBUG) {
                System.out.println();
                System.out.println("mustUseInverseDeps = " + mustUseInverseDeps);
            }

            final Jadecy jdc = refJdc.withMustUseInverseDeps(mustUseInverseDeps);

            assertEquals(mustUseInverseDeps, jdc.getMustUseInverseDeps());

            final List<SortedMap<String,Long>> res =
                    jdc.computeDeps(
                            ElemType.CLASS,
                            NameFilters.equalsName(C3N),
                            false,
                            false,
                            -1);

            if (DEBUG) {
                System.out.println("res = " + res);
            }

            if (mustUseInverseDeps) {
                assertTrue(res.get(1).containsKey(C1N));
            } else {
                assertTrue(res.get(1).containsKey(C4N));
            }
        }
    }

    public void test_withRetainedClassNameFilter_and_getRetainedClassNameFilter() {
        final Jadecy refJdc = newJadecy();

        try {
            refJdc.withRetainedClassNameFilter(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        for (InterfaceNameFilter retainedClassNameFilter : new InterfaceNameFilter[]{
                NameFilters.any(),
                NameFilters.startsWithName(P1N)}) {

            if (DEBUG) {
                System.out.println();
                System.out.println("retainedClassNameFilter = " + retainedClassNameFilter);
            }

            final Jadecy jdc = refJdc.withRetainedClassNameFilter(retainedClassNameFilter);

            assertSame(retainedClassNameFilter, jdc.getRetainedClassNameFilter());

            final List<SortedMap<String,Long>> res =
                    jdc.computeDeps(
                            ElemType.CLASS,
                            NameFilters.equalsName(C1N),
                            false,
                            false,
                            -1);

            if (DEBUG) {
                System.out.println("res = " + res);
            }

            if (retainedClassNameFilter == NameFilters.any()) {
                assertTrue(res.get(3).containsKey(C7N));
            } else {
                assertFalse(res.get(3).containsKey(C7N));
            }
        }
    }

    public void test_parser() {
        // Already covered while creating Jadecy.
    }

    /*
     * 
     */

    public void test_computeMatches_exceptions() {
        final Jadecy jdc = newJadecy();

        try {
            jdc.computeMatches(null, NameFilters.any());
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            jdc.computeMatches(ElemType.CLASS, null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
    }

    public void test_computeMatches_displayNameUsage() {
        final Jadecy jdc = newJadecy();

        final SortedMap<String,Long> res = jdc.computeMatches(
                ElemType.PACKAGE,
                NameFilters.equalsName(NameUtils.DEFAULT_PACKAGE_NAME));

        assertEquals(NameUtils.DEFAULT_PACKAGE_DISPLAY_NAME, res.firstKey());
    }

    public void test_computeMatches_normal() {
        final Jadecy jdc = newJadecy();

        for (ElemType elemType : ElemType.valuesList()) {
            for (InterfaceNameFilter nameFilter : new InterfaceNameFilter[]{
                    NameFilters.any(),
                    NameFilters.startsWithName(P1N)}) {

                if (DEBUG) {
                    System.out.println();
                    System.out.println("elemType = " + elemType);
                    System.out.println("nameFilter = " + nameFilter);
                }


                final SortedMap<String,Long> res = jdc.computeMatches(elemType, nameFilter);

                if (DEBUG) {
                    System.out.println("res = " + res);
                }

                if (elemType == ElemType.CLASS) {
                    if (nameFilter == NameFilters.any()) {
                        assertEquals(8, res.size());
                        assertEquals((Long)0L, res.get(ObjectN));
                        assertEquals((Long)C1BS, res.get(C1N));
                        assertEquals((Long)C2BS, res.get(C2N));
                        assertEquals((Long)C3BS, res.get(C3N));
                        assertEquals((Long)C4BS, res.get(C4N));
                        assertEquals((Long)C5BS, res.get(C5N));
                        assertEquals((Long)C6BS, res.get(C6N));
                        assertEquals((Long)C7BS, res.get(C7N));
                    } else {
                        assertEquals(4, res.size());
                        assertEquals((Long)C1BS, res.get(C1N));
                        assertEquals((Long)C2BS, res.get(C2N));
                        assertEquals((Long)C3BS, res.get(C3N));
                        assertEquals((Long)C4BS, res.get(C4N));
                    }
                } else {
                    if (nameFilter == NameFilters.any()) {
                        int count = 0;
                        //
                        for (String name : getParentPackagesDisplayNames(JLN)) {
                            assertEquals((Long)0L, res.get(name));
                            count++;
                        }
                        assertEquals((Long)0L, res.get(JLN));
                        count++;
                        //
                        for (String name : getParentPackagesDisplayNames(P1N)) {
                            assertEquals((Long)0L, res.get(name));
                            // Not counting default package twice.
                            if (!name.equals(NameUtils.DEFAULT_PACKAGE_DISPLAY_NAME)) {
                                count++;
                            }
                        }
                        assertEquals((Long)P1BS, res.get(P1N));
                        count++;
                        //
                        assertEquals((Long)P2BS, res.get(P2N));
                        count++;
                        //
                        assertEquals(count, res.size());
                    } else {
                        assertEquals(1, res.size());
                        assertEquals((Long)P1BS, res.get(P1N));
                    }
                }
            }
        }
    }

    /*
     * 
     */

    public void test_computeDeps_exceptions() {
        final Jadecy jdc = newJadecy();

        final boolean mustIncludeBeginSet = false;
        final boolean mustIncludeDepsToBeginSet = false;
        final int maxSteps = -1;

        try {
            jdc.computeDeps(
                    null,
                    NameFilters.any(),
                    mustIncludeBeginSet,
                    mustIncludeDepsToBeginSet,
                    maxSteps);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            jdc.computeDeps(
                    ElemType.CLASS,
                    null,
                    mustIncludeBeginSet,
                    mustIncludeDepsToBeginSet,
                    maxSteps);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
    }

    public void test_computeDeps_displayNameUsage() {
        final Jadecy jdc = newJadecy();

        final boolean mustIncludeBeginSet = true;
        final boolean mustIncludeDepsToBeginSet = false;
        final int maxSteps = -1;

        final List<SortedMap<String,Long>> res = jdc.computeDeps(
                ElemType.PACKAGE,
                NameFilters.equalsName(NameUtils.DEFAULT_PACKAGE_NAME),
                mustIncludeBeginSet,
                mustIncludeDepsToBeginSet,
                maxSteps);

        final SortedMap<String,Long> step = res.get(0);
        assertEquals(NameUtils.DEFAULT_PACKAGE_DISPLAY_NAME, step.firstKey());
    }

    public void test_computeDeps_elemTypeAndFilter() {
        final Jadecy jdc = newJadecy();

        final boolean mustIncludeBeginSet = false;
        final boolean mustIncludeDepsToBeginSet = false;
        final int maxSteps = -1;

        for (ElemType elemType : ElemType.valuesList()) {
            for (InterfaceNameFilter beginNameFilter : new InterfaceNameFilter[]{
                    NameFilters.any(),
                    NameFilters.startsWithName(P1N)}) {

                if (DEBUG) {
                    System.out.println();
                    System.out.println("elemType = " + elemType);
                    System.out.println("beginNameFilter = " + beginNameFilter);
                }

                final List<SortedMap<String,Long>> res = jdc.computeDeps(
                        elemType,
                        beginNameFilter,
                        mustIncludeBeginSet,
                        mustIncludeDepsToBeginSet,
                        maxSteps);

                if (DEBUG) {
                    System.out.println("res = " + res);
                }

                if (elemType == ElemType.CLASS) {
                    if (beginNameFilter == NameFilters.any()) {
                        assertEquals(1, res.size());
                        {
                            final SortedMap<String,Long> step = res.get(0);
                            assertEquals(0, step.size());
                        }
                    } else {
                        assertEquals(3, res.size());
                        {
                            final SortedMap<String,Long> step = res.get(0);
                            assertEquals(0, step.size());
                        }
                        {
                            final SortedMap<String,Long> step = res.get(1);
                            assertEquals(3, step.size());
                            assertEquals((Long)0L, step.get(ObjectN));
                            assertEquals((Long)C5BS, step.get(C5N));
                            assertEquals((Long)C6BS, step.get(C6N));
                        }
                        {
                            final SortedMap<String,Long> step = res.get(2);
                            assertEquals(1, step.size());
                            assertEquals((Long)C7BS, step.get(C7N));
                        }
                    }
                } else {
                    if (beginNameFilter == NameFilters.any()) {
                        assertEquals(1, res.size());
                        {
                            final SortedMap<String,Long> step = res.get(0);
                            assertEquals(0, step.size());
                        }
                    } else {
                        assertEquals(2, res.size());
                        {
                            final SortedMap<String,Long> step = res.get(0);
                            assertEquals(0, step.size());
                        }
                        {
                            final SortedMap<String,Long> step = res.get(1);
                            assertEquals(2, step.size());
                            assertEquals((Long)0L, step.get(JLN));
                            assertEquals((Long)P2BS, step.get(P2N));
                        }
                    }
                }
            }
        }
    }

    public void test_computeDeps_flagsAndMaxSteps() {
        final Jadecy jdc = newJadecy();

        final ElemType elemType = ElemType.CLASS;
        final InterfaceNameFilter beginNameFilter = NameFilters.startsWithName(P1N);

        for (boolean mustIncludeBeginSet : new boolean[]{false,true}) {
            for (boolean mustIncludeDepsToBeginSet : new boolean[]{false,true}) {
                for (int maxSteps : new int[]{-1,0,1}) {

                    if (DEBUG) {
                        System.out.println();
                        System.out.println("mustIncludeBeginSet = " + mustIncludeBeginSet);
                        System.out.println("mustIncludeDepsToBeginSet = " + mustIncludeDepsToBeginSet);
                        System.out.println("maxSteps = " + maxSteps);
                    }

                    final List<SortedMap<String,Long>> res = jdc.computeDeps(
                            elemType,
                            beginNameFilter,
                            mustIncludeBeginSet,
                            mustIncludeDepsToBeginSet,
                            maxSteps);

                    final int maxStepsPossible = 2;

                    final int expectedStepCount = ((maxSteps < 0) ? maxStepsPossible : Math.min(maxStepsPossible, maxSteps)) + 1;
                    assertEquals(expectedStepCount, res.size());

                    {
                        final SortedMap<String,Long> step = res.get(0);
                        if (mustIncludeBeginSet) {
                            assertEquals(4, step.size());
                            assertEquals((Long)C1BS, step.get(C1N));
                            assertEquals((Long)C2BS, step.get(C2N));
                            assertEquals((Long)C3BS, step.get(C3N));
                            assertEquals((Long)C4BS, step.get(C4N));
                        } else {
                            assertEquals(0, step.size());
                        }
                    }
                    if (expectedStepCount > 1) {
                        final SortedMap<String,Long> step = res.get(1);
                        if ((!mustIncludeBeginSet) && mustIncludeDepsToBeginSet) {
                            assertEquals(6, step.size());
                            assertEquals((Long)0L, step.get(ObjectN));
                            assertEquals((Long)C2BS, step.get(C2N));
                            assertEquals((Long)C3BS, step.get(C3N));
                            assertEquals((Long)C4BS, step.get(C4N));
                            assertEquals((Long)C5BS, step.get(C5N));
                            assertEquals((Long)C6BS, step.get(C6N));
                        } else {
                            assertEquals(3, step.size());
                            assertEquals((Long)0L, step.get(ObjectN));
                            assertEquals((Long)C5BS, step.get(C5N));
                            assertEquals((Long)C6BS, step.get(C6N));
                        }
                    }
                    if (expectedStepCount > 2) {
                        final SortedMap<String,Long> step = res.get(2);
                        assertEquals(1, step.size());
                        assertEquals((Long)C7BS, step.get(C7N));
                    }
                }
            }
        }
    }

    /*
     * 
     */

    public void test_computeDepsGraph_exceptions() {
        final Jadecy jdc = newJadecy();

        final boolean mustIncludeBeginSet = false;
        final boolean mustIncludeDepsToBeginSet = false;
        final int maxSteps = -1;

        try {
            jdc.computeDepsGraph(
                    null,
                    NameFilters.any(),
                    mustIncludeBeginSet,
                    mustIncludeDepsToBeginSet,
                    NameFilters.any(),
                    maxSteps);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            jdc.computeDepsGraph(
                    ElemType.CLASS,
                    null,
                    mustIncludeBeginSet,
                    mustIncludeDepsToBeginSet,
                    NameFilters.any(),
                    maxSteps);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            jdc.computeDepsGraph(
                    ElemType.CLASS,
                    NameFilters.any(),
                    mustIncludeBeginSet,
                    mustIncludeDepsToBeginSet,
                    null,
                    maxSteps);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
    }

    public void test_computeDepsGraph_displayNameUsage() {
        final Jadecy jdc = newJadecy();

        final boolean mustIncludeBeginSet = true;
        final boolean mustIncludeDepsToBeginSet = false;
        final int maxSteps = -1;

        final List<SortedMap<String,SortedMap<String,SortedSet<String>>>> res =
                jdc.computeDepsGraph(
                        ElemType.PACKAGE,
                        NameFilters.equalsName(NameUtils.DEFAULT_PACKAGE_NAME),
                        mustIncludeBeginSet,
                        mustIncludeDepsToBeginSet,
                        NameFilters.equalsName(NameUtils.DEFAULT_PACKAGE_NAME),
                        maxSteps);

        final SortedMap<String,SortedMap<String,SortedSet<String>>> step = res.get(0);
        assertEquals(NameUtils.DEFAULT_PACKAGE_DISPLAY_NAME, step.firstKey());
    }

    public void test_computeDepsGraph_elemTypeAndFilters() {
        final Jadecy jdc = newJadecy();

        final boolean mustIncludeBeginSet = false;
        final boolean mustIncludeDepsToBeginSet = false;
        final InterfaceNameFilter endNameFilter = NameFilters.startsWithName(P2N);
        final int maxSteps = -1;

        for (ElemType elemType : ElemType.valuesList()) {
            for (InterfaceNameFilter beginNameFilter : new InterfaceNameFilter[]{
                    NameFilters.any(),
                    NameFilters.startsWithName(P1N)}) {

                if (DEBUG) {
                    System.out.println();
                    System.out.println("elemType = " + elemType);
                    System.out.println("beginNameFilter = " + beginNameFilter);
                }

                final List<SortedMap<String,SortedMap<String,SortedSet<String>>>> res =
                        jdc.computeDepsGraph(
                                elemType,
                                beginNameFilter,
                                mustIncludeBeginSet,
                                mustIncludeDepsToBeginSet,
                                endNameFilter,
                                maxSteps);

                if (DEBUG) {
                    System.out.println("res = " + res);
                }

                if (elemType == ElemType.CLASS) {
                    if (beginNameFilter == NameFilters.any()) {
                        assertEquals(1, res.size());
                        {
                            final SortedMap<String,SortedMap<String,SortedSet<String>>> step = res.get(0);
                            assertEquals(0, step.size());
                        }
                    } else {
                        assertEquals(3, res.size());
                        {
                            final SortedMap<String,SortedMap<String,SortedSet<String>>> step = res.get(0);
                            assertEquals(0, step.size());
                        }
                        {
                            final SortedMap<String,SortedMap<String,SortedSet<String>>> step = res.get(1);
                            assertEquals(3, step.size());
                            {
                                final SortedMap<String,SortedSet<String>> causesByDep = step.get(ObjectN);
                                assertEquals(0, causesByDep.size());
                            }
                            {
                                final SortedMap<String,SortedSet<String>> causesByDep = step.get(C5N);
                                assertEquals(1, causesByDep.size());
                                {
                                    final SortedSet<String> causes = causesByDep.get(C7N);
                                    assertEquals(0, causes.size());
                                }
                            }
                            {
                                final SortedMap<String,SortedSet<String>> causesByDep = step.get(C6N);
                                assertEquals(1, causesByDep.size());
                                {
                                    final SortedSet<String> causes = causesByDep.get(C7N);
                                    assertEquals(0, causes.size());
                                }
                            }
                        }
                        {
                            final SortedMap<String,SortedMap<String,SortedSet<String>>> step = res.get(2);
                            assertEquals(1, step.size());
                            {
                                final SortedMap<String,SortedSet<String>> dep = step.get(C7N);
                                assertEquals(1, dep.size());
                                {
                                    final SortedSet<String> causes = dep.get(C6N);
                                    assertEquals(0, causes.size());
                                }
                            }
                        }
                    }
                } else {
                    if (beginNameFilter == NameFilters.any()) {
                        assertEquals(1, res.size());
                        {
                            final SortedMap<String,SortedMap<String,SortedSet<String>>> step = res.get(0);
                            assertEquals(0, step.size());
                        }
                    } else {
                        assertEquals(2, res.size());
                        {
                            final SortedMap<String,SortedMap<String,SortedSet<String>>> step = res.get(0);
                            assertEquals(0, step.size());
                        }
                        {
                            final SortedMap<String,SortedMap<String,SortedSet<String>>> step = res.get(1);
                            assertEquals(2, step.size());
                            {
                                final SortedMap<String,SortedSet<String>> causesByDep = step.get(JLN);
                                assertEquals(0, causesByDep.size());
                            }
                            {
                                final SortedMap<String,SortedSet<String>> causesByDep = step.get(P2N);
                                assertEquals(0, causesByDep.size());
                            }
                        }
                    }
                }
            }
        }
    }

    public void test_computeDepsGraph_flagsAndMaxSteps() {
        final Jadecy jdc = newJadecy();

        // Testing on packages, to have some causes.
        final ElemType elemType = ElemType.PACKAGE;
        // Dependencies from p1.
        final InterfaceNameFilter beginNameFilter = NameFilters.startsWithName(P1N);
        // For each dependency, only having successors that are in p2.
        final InterfaceNameFilter endNameFilter = NameFilters.startsWithName(P2N);

        for (boolean mustIncludeBeginSet : new boolean[]{false,true}) {
            for (boolean mustIncludeDepsToBeginSet : new boolean[]{false,true}) {
                for (int maxSteps : new int[]{-1,0,1}) {

                    if (DEBUG) {
                        System.out.println();
                        System.out.println("mustIncludeBeginSet = " + mustIncludeBeginSet);
                        System.out.println("mustIncludeDepsToBeginSet = " + mustIncludeDepsToBeginSet);
                        System.out.println("maxSteps = " + maxSteps);
                    }

                    final List<SortedMap<String,SortedMap<String,SortedSet<String>>>> res =
                            jdc.computeDepsGraph(
                                    elemType,
                                    beginNameFilter,
                                    mustIncludeBeginSet,
                                    mustIncludeDepsToBeginSet,
                                    endNameFilter,
                                    maxSteps);

                    if (DEBUG) {
                        System.out.println("res = " + res);
                    }

                    final boolean haveLateDepsToBegin = (!mustIncludeBeginSet) && mustIncludeDepsToBeginSet;

                    final int maxStepsPossible;
                    if (haveLateDepsToBegin) {
                        // Dependency to begin step occurs alone in last step
                        // (step 1: 2 -> 5 and 4 -> 6, step 2: 5 and 6 -> 4).
                        maxStepsPossible = 2;
                    } else {
                        maxStepsPossible = 1;
                    }

                    final int expectedStepCount = ((maxSteps < 0) ? maxStepsPossible : Math.min(maxStepsPossible, maxSteps)) + 1;
                    assertEquals(expectedStepCount, res.size());

                    {
                        final SortedMap<String,SortedMap<String,SortedSet<String>>> step = res.get(0);
                        if (mustIncludeBeginSet) {
                            assertEquals(1, step.size());
                            {
                                final SortedMap<String,SortedSet<String>> causesByDep = step.get(P1N);
                                // Successors are java.lang and p2, but we only have p2
                                // due to end filter.
                                assertEquals(1, causesByDep.size());
                                {
                                    final SortedSet<String> causes = causesByDep.get(P2N);
                                    assertEquals(2, causes.size());
                                    assertTrue(causes.contains(C2N));
                                    assertTrue(causes.contains(C4N));
                                }
                            }
                        } else {
                            assertEquals(0, step.size());
                        }
                    }
                    if (expectedStepCount > 1) {
                        final SortedMap<String,SortedMap<String,SortedSet<String>>> step = res.get(1);
                        assertEquals(2, step.size());
                        {
                            final SortedMap<String,SortedSet<String>> causesByDep = step.get(JLN);
                            assertEquals(0, causesByDep.size());
                        }
                        {
                            final SortedMap<String,SortedSet<String>> causesByDep = step.get(P2N);
                            // Successor is p1, but we don't have it here due to end filter.
                            assertEquals(0, causesByDep.size());
                        }
                    }
                    if (expectedStepCount > 2) {
                        final SortedMap<String,SortedMap<String,SortedSet<String>>> step = res.get(2);
                        assertEquals(1, step.size());
                        {
                            final SortedMap<String,SortedSet<String>> causesByDep = step.get(P1N);
                            // Successors are java.lang and p2, but we only have p2
                            // due to end filter.
                            assertEquals(1, causesByDep.size());
                            {
                                final SortedSet<String> causes = causesByDep.get(P2N);
                                assertEquals(2, causes.size());
                                assertTrue(causes.contains(C2N));
                                assertTrue(causes.contains(C4N));
                            }
                        }
                    }
                }
            }
        }
    }

    /*
     * 
     */

    public void test_computeOneShortestPath_exceptions() {
        final Jadecy jdc = newJadecy();

        try {
            jdc.computeOneShortestPath(
                    null,
                    NameFilters.any(),
                    NameFilters.any());
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            jdc.computeOneShortestPath(
                    ElemType.CLASS,
                    null,
                    NameFilters.any());
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            jdc.computeOneShortestPath(
                    ElemType.CLASS,
                    NameFilters.any(),
                    null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
    }

    public void test_computeOneShortestPath_displayNameUsage() {
        final Jadecy jdc = newJadecy();

        final List<SortedMap<String,SortedSet<String>>> res =
                jdc.computeOneShortestPath(
                        ElemType.PACKAGE,
                        NameFilters.equalsName(NameUtils.DEFAULT_PACKAGE_NAME),
                        NameFilters.equalsName(NameUtils.DEFAULT_PACKAGE_NAME));

        final SortedMap<String,SortedSet<String>> causesByDep = res.get(0);
        final String dep = causesByDep.firstKey();
        assertEquals(NameUtils.DEFAULT_PACKAGE_DISPLAY_NAME, dep);
    }

    public void test_computeOneShortestPath_normal_CLASS() {
        final Jadecy jdc = newJadecy();

        for (InterfaceNameFilter beginNameFilter : new InterfaceNameFilter[]{
                NameFilters.any(),
                NameFilters.or(NameFilters.equalsName(C2N),NameFilters.equalsName(C3N))}) {
            for (InterfaceNameFilter endNameFilter : new InterfaceNameFilter[]{
                    NameFilters.any(),
                    NameFilters.or(NameFilters.equalsName(C5N),NameFilters.equalsName(C7N))}) {

                if (DEBUG) {
                    System.out.println();
                    System.out.println("beginNameFilter = " + beginNameFilter);
                    System.out.println("endNameFilter = " + endNameFilter);
                }

                final List<SortedMap<String,SortedSet<String>>> res =
                        jdc.computeOneShortestPath(
                                ElemType.CLASS,
                                beginNameFilter,
                                endNameFilter);

                if (DEBUG) {
                    System.out.println("res = " + res);
                }

                if (beginNameFilter == NameFilters.any()) {
                    if (endNameFilter == NameFilters.any()) {
                        assertEquals(1, res.size());
                        {
                            final SortedMap<String,SortedSet<String>> causesByDep = res.get(0);
                            assertEquals(1, causesByDep.size());
                            final String dep = causesByDep.firstKey();
                            /*
                             * Object is in the graph (just as dependency, since not parsed).
                             * Since our algorithms use sorted collections (for both having
                             * more or less sorted results, and for determinism), and since
                             * java.lang.Object it is alphabetically before our classes, it
                             * pops out as the returned singleton path.
                             */
                            assertEquals(ObjectN, dep);
                            final SortedSet<String> causes = causesByDep.get(dep);
                            assertEquals(0, causes.size());
                        }
                    } else {
                        assertEquals(1, res.size());
                        {
                            final SortedMap<String,SortedSet<String>> causesByDep = res.get(0);
                            assertEquals(1, causesByDep.size());
                            final String dep = causesByDep.firstKey();
                            assertEquals(C5N, dep);
                            final SortedSet<String> causes = causesByDep.get(dep);
                            assertEquals(0, causes.size());
                        }
                    }
                } else {
                    if (endNameFilter == NameFilters.any()) {
                        assertEquals(1, res.size());
                        {
                            final SortedMap<String,SortedSet<String>> causesByDep = res.get(0);
                            assertEquals(1, causesByDep.size());
                            final String dep = causesByDep.firstKey();
                            assertEquals(C2N, dep);
                            final SortedSet<String> causes = causesByDep.get(dep);
                            assertEquals(0, causes.size());
                        }
                    } else {
                        assertEquals(2, res.size());
                        {
                            final SortedMap<String,SortedSet<String>> causesByDep = res.get(0);
                            assertEquals(1, causesByDep.size());
                            final String dep = causesByDep.firstKey();
                            assertEquals(C2N, dep);
                            final SortedSet<String> causes = causesByDep.get(dep);
                            assertEquals(0, causes.size());
                        }
                        {
                            final SortedMap<String,SortedSet<String>> causesByDep = res.get(1);
                            assertEquals(1, causesByDep.size());
                            final String dep = causesByDep.firstKey();
                            assertEquals(C5N, dep);
                            final SortedSet<String> causes = causesByDep.get(dep);
                            assertEquals(0, causes.size());
                        }
                    }
                }
            }
        }
    }

    public void test_computeOneShortestPath_normal_PACKAGE() {
        final Jadecy jdc = newJadecy();

        for (InterfaceNameFilter beginNameFilter : new InterfaceNameFilter[]{
                NameFilters.any(),
                NameFilters.equalsName(P1N)}) {
            for (InterfaceNameFilter endNameFilter : new InterfaceNameFilter[]{
                    NameFilters.any(),
                    NameFilters.equalsName(P2N)}) {

                if (DEBUG) {
                    System.out.println();
                    System.out.println("beginNameFilter = " + beginNameFilter);
                    System.out.println("endNameFilter = " + endNameFilter);
                }

                final List<SortedMap<String,SortedSet<String>>> res =
                        jdc.computeOneShortestPath(
                                ElemType.PACKAGE,
                                beginNameFilter,
                                endNameFilter);

                if (DEBUG) {
                    System.out.println("res = " + res);
                }

                if (beginNameFilter == NameFilters.any()) {
                    if (endNameFilter == NameFilters.any()) {
                        assertEquals(1, res.size());
                        {
                            final SortedMap<String,SortedSet<String>> causesByDep = res.get(0);
                            assertEquals(1, causesByDep.size());
                            final String dep = causesByDep.firstKey();
                            /*
                             * Cf. comment about Object in test for CLASS:
                             * default package is always in non-empty packages
                             * dependencies graphs, and its name (empty string)
                             * is before any other package name.
                             */
                            assertEquals(NameUtils.DEFAULT_PACKAGE_DISPLAY_NAME, dep);
                            final SortedSet<String> causes = causesByDep.get(dep);
                            assertEquals(0, causes.size());
                        }
                    } else {
                        assertEquals(1, res.size());
                        {
                            final SortedMap<String,SortedSet<String>> causesByDep = res.get(0);
                            assertEquals(1, causesByDep.size());
                            final String dep = causesByDep.firstKey();
                            assertEquals(P2N, dep);
                            final SortedSet<String> causes = causesByDep.get(dep);
                            assertEquals(0, causes.size());
                        }
                    }
                } else {
                    if (endNameFilter == NameFilters.any()) {
                        assertEquals(1, res.size());
                        {
                            final SortedMap<String,SortedSet<String>> causesByDep = res.get(0);
                            assertEquals(1, causesByDep.size());
                            final String dep = causesByDep.firstKey();
                            assertEquals(P1N, dep);
                            final SortedSet<String> causes = causesByDep.get(dep);
                            assertEquals(0, causes.size());
                        }
                    } else {
                        assertEquals(2, res.size());
                        {
                            final SortedMap<String,SortedSet<String>> causesByDep = res.get(0);
                            assertEquals(1, causesByDep.size());
                            final String dep = causesByDep.firstKey();
                            assertEquals(P1N, dep);
                            final SortedSet<String> causes = causesByDep.get(dep);
                            assertEquals(2, causes.size());
                            {
                                assertTrue(causes.contains(C2N));
                                assertTrue(causes.contains(C4N));
                            }
                        }
                        {
                            final SortedMap<String,SortedSet<String>> causesByDep = res.get(1);
                            assertEquals(1, causesByDep.size());
                            final String dep = causesByDep.firstKey();
                            assertEquals(P2N, dep);
                            final SortedSet<String> causes = causesByDep.get(dep);
                            assertEquals(0, causes.size());
                        }
                    }
                }
            }
        }
    }

    /*
     * 
     */

    public void test_computePathsGraph_exceptions() {
        final Jadecy jdc = newJadecy();

        final int maxSteps = -1;

        try {
            jdc.computePathsGraph(
                    null,
                    NameFilters.any(),
                    NameFilters.any(),
                    maxSteps);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            jdc.computePathsGraph(
                    ElemType.CLASS,
                    null,
                    NameFilters.any(),
                    maxSteps);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            jdc.computePathsGraph(
                    ElemType.CLASS,
                    NameFilters.any(),
                    null,
                    maxSteps);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
    }

    public void test_computePathsGraph_displayNameUsage() {
        final Jadecy jdc = newJadecy();

        final int maxSteps = -1;

        final SortedMap<String,SortedMap<String,SortedSet<String>>> res =
                jdc.computePathsGraph(
                        ElemType.PACKAGE,
                        NameFilters.equalsName(NameUtils.DEFAULT_PACKAGE_NAME),
                        NameFilters.equalsName(NameUtils.DEFAULT_PACKAGE_NAME),
                        maxSteps);

        assertEquals(NameUtils.DEFAULT_PACKAGE_DISPLAY_NAME, res.firstKey());
    }

    public void test_computePathsGraph_normal_CLASSAndMaxSteps() {
        final Jadecy jdc = newJadecy();

        for (int maxSteps : new int[]{-1,0,1,2}) {

            if (DEBUG) {
                System.out.println();
                System.out.println("maxSteps = " + maxSteps);
            }

            final SortedMap<String,SortedMap<String,SortedSet<String>>> res =
                    jdc.computePathsGraph(
                            ElemType.CLASS,
                            NameFilters.or(NameFilters.equalsName(C2N),NameFilters.equalsName(C3N)),
                            NameFilters.or(NameFilters.equalsName(C5N),NameFilters.equalsName(C6N)),
                            maxSteps);

            if (DEBUG) {
                System.out.println("res = " + res);
            }

            if ((maxSteps < 0) || (maxSteps == 2)) {
                // C1 not reachable from {C2,C3}, and exploration
                // from {C2,C3} stops when reaching end set so C7
                // doesn't make it.
                assertEquals(5, res.size());
                {
                    final SortedMap<String,SortedSet<String>> causesByDep = res.get(C2N);
                    assertEquals(1, causesByDep.size());
                    assertEquals(0, causesByDep.get(C5N).size());
                }
                {
                    final SortedMap<String,SortedSet<String>> causesByDep = res.get(C3N);
                    assertEquals(1, causesByDep.size());
                    assertEquals(0, causesByDep.get(C4N).size());
                }
                {
                    final SortedMap<String,SortedSet<String>> causesByDep = res.get(C4N);
                    assertEquals(2, causesByDep.size());
                    assertEquals(0, causesByDep.get(C2N).size());
                    assertEquals(0, causesByDep.get(C6N).size());
                }
                {
                    final SortedMap<String,SortedSet<String>> causesByDep = res.get(C5N);
                    assertEquals(1, causesByDep.size());
                    assertEquals(0, causesByDep.get(C4N).size());
                }
                {
                    final SortedMap<String,SortedSet<String>> causesByDep = res.get(C6N);
                    assertEquals(1, causesByDep.size());
                    assertEquals(0, causesByDep.get(C4N).size());
                }
            } else if (maxSteps == 0) {
                assertEquals(0, res.size());
            } else if (maxSteps == 1) {
                assertEquals(2, res.size());
                {
                    final SortedMap<String,SortedSet<String>> causesByDep = res.get(C2N);
                    assertEquals(1, causesByDep.size());
                    assertEquals(0, causesByDep.get(C5N).size());
                }
                {
                    final SortedMap<String,SortedSet<String>> causesByDep = res.get(C5N);
                    assertEquals(0, causesByDep.size());
                }
            } else {
                throw new AssertionError();
            }
        }
    }

    public void test_computePathsGraph_normal_PACKAGEAndFilters() {
        final Jadecy jdc = newJadecy();

        final int maxSteps = -1;

        for (InterfaceNameFilter beginNameFilter : new InterfaceNameFilter[]{
                NameFilters.any(),
                NameFilters.equalsName(P1N)}) {
            for (InterfaceNameFilter endNameFilter : new InterfaceNameFilter[]{
                    NameFilters.any(),
                    NameFilters.equalsName(P2N)}) {

                if (DEBUG) {
                    System.out.println();
                    System.out.println("beginNameFilter = " + beginNameFilter);
                    System.out.println("endNameFilter = " + endNameFilter);
                }

                final SortedMap<String,SortedMap<String,SortedSet<String>>> res =
                        jdc.computePathsGraph(
                                ElemType.PACKAGE,
                                beginNameFilter,
                                endNameFilter,
                                maxSteps);

                if (DEBUG) {
                    System.out.println("res = " + res);
                }

                if (beginNameFilter == NameFilters.any()) {
                    if (endNameFilter == NameFilters.any()) {
                        /*
                         * any -> any
                         */
                        int count = 0;
                        //
                        for (String name : getParentPackagesDisplayNames(JLN)) {
                            final SortedMap<String,SortedSet<String>> causesByDep = res.get(name);
                            assertEquals(0, causesByDep.size());
                            count++;
                        }
                        {
                            final SortedMap<String,SortedSet<String>> causesByDep = res.get(JLN);
                            assertEquals(0, causesByDep.size());
                            count++;
                        }
                        for (String name : getParentPackagesDisplayNames(P1N)) {
                            final SortedMap<String,SortedSet<String>> causesByDep = res.get(name);
                            assertEquals(0, causesByDep.size());
                            // Not counting default package twice.
                            if (!name.equals(NameUtils.DEFAULT_PACKAGE_DISPLAY_NAME)) {
                                count++;
                            }
                        }
                        {
                            final SortedMap<String,SortedSet<String>> causesByDep = res.get(P1N);
                            assertEquals(2, causesByDep.size());
                            {
                                final SortedSet<String> causes = causesByDep.get(JLN);
                                assertEquals(4, causes.size());
                                assertTrue(causes.contains(C1N));
                                assertTrue(causes.contains(C2N));
                                assertTrue(causes.contains(C3N));
                                assertTrue(causes.contains(C4N));
                            }
                            {
                                final SortedSet<String> causes = causesByDep.get(P2N);
                                assertEquals(2, causes.size());
                                assertTrue(causes.contains(C2N));
                                assertTrue(causes.contains(C4N));
                            }
                            count++;
                        }
                        {
                            final SortedMap<String,SortedSet<String>> causesByDep = res.get(P2N);
                            assertEquals(2, causesByDep.size());
                            {
                                final SortedSet<String> causes = causesByDep.get(JLN);
                                assertEquals(3, causes.size());
                                assertTrue(causes.contains(C5N));
                                assertTrue(causes.contains(C6N));
                                assertTrue(causes.contains(C7N));
                            }
                            {
                                final SortedSet<String> causes = causesByDep.get(P1N);
                                assertEquals(2, causes.size());
                                assertTrue(causes.contains(C5N));
                                assertTrue(causes.contains(C6N));
                            }
                            count++;
                        }
                        //
                        assertEquals(count, res.size());
                    } else {
                        /*
                         * any -> p2
                         */
                        int count = 0;
                        //
                        {
                            final SortedMap<String,SortedSet<String>> causesByDep = res.get(P1N);
                            assertEquals(1, causesByDep.size());
                            {
                                final SortedSet<String> causes = causesByDep.get(P2N);
                                assertEquals(2, causes.size());
                                assertTrue(causes.contains(C2N));
                                assertTrue(causes.contains(C4N));
                            }
                            count++;
                        }
                        {
                            final SortedMap<String,SortedSet<String>> causesByDep = res.get(P2N);
                            assertEquals(1, causesByDep.size());
                            {
                                final SortedSet<String> causes = causesByDep.get(P1N);
                                assertEquals(2, causes.size());
                                assertTrue(causes.contains(C5N));
                                assertTrue(causes.contains(C6N));
                            }
                            count++;
                        }
                        //
                        assertEquals(count, res.size());
                    }
                } else {
                    if (endNameFilter == NameFilters.any()) {
                        /*
                         * p1 -> any
                         * Stops right away since p1 is in end set.
                         */
                        int count = 0;
                        //
                        {
                            final SortedMap<String,SortedSet<String>> causesByDep = res.get(P1N);
                            assertEquals(0, causesByDep.size());
                            count++;
                        }
                        //
                        assertEquals(count, res.size());
                    } else {
                        /*
                         * p1 -> p2
                         * Same as any -> p2, since p2 can only be reached from p1.
                         */
                        int count = 0;
                        //
                        {
                            final SortedMap<String,SortedSet<String>> causesByDep = res.get(P1N);
                            assertEquals(1, causesByDep.size());
                            {
                                final SortedSet<String> causes = causesByDep.get(P2N);
                                assertEquals(2, causes.size());
                                assertTrue(causes.contains(C2N));
                                assertTrue(causes.contains(C4N));
                            }
                            count++;
                        }
                        {
                            final SortedMap<String,SortedSet<String>> causesByDep = res.get(P2N);
                            assertEquals(1, causesByDep.size());
                            {
                                final SortedSet<String> causes = causesByDep.get(P1N);
                                assertEquals(2, causes.size());
                                assertTrue(causes.contains(C5N));
                                assertTrue(causes.contains(C6N));
                            }
                            count++;
                        }
                        //
                        assertEquals(count, res.size());
                    }
                }
            }
        }
    }

    /*
     * 
     */

    public void test_computeSccs_exceptions() {
        final Jadecy jdc = newJadecy();

        try {
            jdc.computeSccs(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
    }

    public void test_computeSccs_displayNameUsage() {
        final Jadecy jdc = newJadecy();

        // Ensuring default package is in a SCC.
        final PackageData defaultP = jdc.parser().getDefaultPackageData();
        final ClassData c0 = defaultP.getOrCreateClassData("ClassInDefaultP");
        final ClassData c1 = defaultP.getClassData(C1N);
        PackageData.ensureDependency(c0, c1);
        PackageData.ensureDependency(c1, c0);

        final List<SortedMap<String,Long>> res = jdc.computeSccs(ElemType.PACKAGE);

        assertEquals(NameUtils.DEFAULT_PACKAGE_DISPLAY_NAME, res.get(0).firstKey());
    }

    public void test_computeSccs_ignoredIfConfinedInATopLevelClass() {
        final Jadecy jdc = newJadecy();

        for (boolean withTopLevel : new boolean[]{false,true}) {
            
            if (DEBUG) {
                System.out.println();
                System.out.println("withTopLevel = " + withTopLevel);
            }

            final PackageData defaultP = jdc.parser().getDefaultPackageData();
            defaultP.clear();

            final ClassData c1 = defaultP.getOrCreateClassData("a.b.A$B");
            final ClassData c2;
            if (withTopLevel) {
                c2 = defaultP.getOrCreateClassData("a.b.A");
            } else {
                c2 = defaultP.getOrCreateClassData("a.b.A$C");
            }
            {
                PackageData.ensureDependency(c1, c2);
                PackageData.ensureDependency(c2, c1);
            }

            /*
             * 
             */

            final List<SortedMap<String,Long>> res = jdc.computeSccs(ElemType.CLASS);

            if (DEBUG) {
                System.out.println("res = " + res);
            }

            assertEquals(0, res.size());
        }
    }
    
    public void test_computeSccs_normal_CLASS() {
        final Jadecy jdc = newJadecy();

        /*
         * Adding others SCCs, to check ordering between SCCs.
         */

        final PackageData defaultP = jdc.parser().getDefaultPackageData();
        final ClassData aaaC1 = defaultP.getOrCreateClassData("aaa.C1");
        final ClassData aaaC2 = defaultP.getOrCreateClassData("aaa.C2");
        final ClassData aaaC3 = defaultP.getOrCreateClassData("aaa.C3");
        final ClassData aaaC4 = defaultP.getOrCreateClassData("aaa.C4");
        final ClassData aaaC5 = defaultP.getOrCreateClassData("aaa.C5");
        {
            PackageData.ensureDependency(aaaC1, aaaC2);
            PackageData.ensureDependency(aaaC2, aaaC3);
            PackageData.ensureDependency(aaaC3, aaaC4);
            PackageData.ensureDependency(aaaC4, aaaC5);
            PackageData.ensureDependency(aaaC5, aaaC1);
        }

        final ClassData yyyC1 = defaultP.getOrCreateClassData("yyy.C1");
        final ClassData yyyC2 = defaultP.getOrCreateClassData("yyy.C2");
        final ClassData yyyC3 = defaultP.getOrCreateClassData("yyy.C3");
        final ClassData yyyC4 = defaultP.getOrCreateClassData("yyy.C4");
        final ClassData yyyC5 = defaultP.getOrCreateClassData("yyy.C5");
        {
            PackageData.ensureDependency(yyyC1, yyyC2);
            PackageData.ensureDependency(yyyC2, yyyC3);
            PackageData.ensureDependency(yyyC3, yyyC4);
            PackageData.ensureDependency(yyyC4, yyyC5);
            PackageData.ensureDependency(yyyC5, yyyC1);
        }

        final ClassData zzzC1 = defaultP.getOrCreateClassData("zzz.C1");
        final ClassData zzzC2 = defaultP.getOrCreateClassData("zzz.C2");
        {
            PackageData.ensureDependency(zzzC1, zzzC2);
            PackageData.ensureDependency(zzzC2, zzzC1);
        }

        /*
         * 
         */

        final List<SortedMap<String,Long>> res = jdc.computeSccs(ElemType.CLASS);

        if (DEBUG) {
            System.out.println("res = " + res);
        }

        assertEquals(4, res.size());
        {
            final SortedMap<String,Long> scc = res.get(0);
            assertEquals(2, scc.size());
            assertEquals((Long)0L, scc.get(zzzC1.displayName()));
            assertEquals((Long)0L, scc.get(zzzC2.displayName()));
        }
        {
            final SortedMap<String,Long> scc = res.get(1);
            assertEquals(5, scc.size());
            assertEquals((Long)0L, scc.get(aaaC1.displayName()));
            assertEquals((Long)0L, scc.get(aaaC2.displayName()));
            assertEquals((Long)0L, scc.get(aaaC3.displayName()));
            assertEquals((Long)0L, scc.get(aaaC4.displayName()));
            assertEquals((Long)0L, scc.get(aaaC5.displayName()));
        }
        {
            final SortedMap<String,Long> scc = res.get(2);
            assertEquals(5, scc.size());
            assertEquals((Long)C2BS, scc.get(C2N));
            assertEquals((Long)C4BS, scc.get(C4N));
            assertEquals((Long)C5BS, scc.get(C5N));
            assertEquals((Long)C6BS, scc.get(C6N));
            assertEquals((Long)C7BS, scc.get(C7N));
        }
        {
            final SortedMap<String,Long> scc = res.get(3);
            assertEquals(5, scc.size());
            assertEquals((Long)0L, scc.get(yyyC1.displayName()));
            assertEquals((Long)0L, scc.get(yyyC2.displayName()));
            assertEquals((Long)0L, scc.get(yyyC3.displayName()));
            assertEquals((Long)0L, scc.get(yyyC4.displayName()));
            assertEquals((Long)0L, scc.get(yyyC5.displayName()));
        }
    }

    public void test_computeSccs_normal_PACKAGE() {
        final Jadecy jdc = newJadecy();

        final List<SortedMap<String,Long>> res = jdc.computeSccs(ElemType.PACKAGE);

        if (DEBUG) {
            System.out.println("res = " + res);
        }

        // No single element SCC for p3.
        assertEquals(1, res.size());
        {
            final SortedMap<String,Long> scc = res.get(0);
            assertEquals(2, scc.size());
            assertEquals((Long)P1BS, scc.get(P1N));
            assertEquals((Long)P2BS, scc.get(P2N));
        }
    }
    
    /*
     * 
     */

    public void test_computeCycles_exceptions() {
        final Jadecy jdc = newJadecy();

        try {
            jdc.computeCycles(
                    null,
                    0,
                    new MyCycleProcessor());
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            jdc.computeCycles(
                    ElemType.CLASS,
                    0,
                    null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
    }

    public void test_computeCycles_displayNameUsage() {
        final Jadecy jdc = newJadecy();

        // Ensuring default package is in a cycle.
        final PackageData defaultP = jdc.parser().getDefaultPackageData();
        final ClassData c0 = defaultP.getOrCreateClassData("ClassInDefaultP");
        final ClassData c1 = defaultP.getClassData(C1N);
        PackageData.ensureDependency(c0, c1);
        PackageData.ensureDependency(c1, c0);

        final MyCycleProcessor processor = new MyCycleProcessor();
        jdc.computeCycles(ElemType.PACKAGE, -1, processor);
        final List<MyProcessed> res = processor.processedList;

        if (DEBUG) {
            System.out.println("res = " + res);
        }

        assertEquals(NameUtils.DEFAULT_PACKAGE_DISPLAY_NAME, res.get(0).names[0]);
    }

    public void test_computeCycles_ignoredIfConfinedInATopLevelClass() {
        final Jadecy jdc = newJadecy();

        for (boolean withTopLevel : new boolean[]{false,true}) {
            
            if (DEBUG) {
                System.out.println();
                System.out.println("withTopLevel = " + withTopLevel);
            }

            final PackageData defaultP = jdc.parser().getDefaultPackageData();
            defaultP.clear();

            final ClassData c1 = defaultP.getOrCreateClassData("a.b.A$B");
            final ClassData c2;
            if (withTopLevel) {
                c2 = defaultP.getOrCreateClassData("a.b.A");
            } else {
                c2 = defaultP.getOrCreateClassData("a.b.A$C");
            }
            {
                PackageData.ensureDependency(c1, c2);
                PackageData.ensureDependency(c2, c1);
            }

            /*
             * 
             */

            final MyCycleProcessor processor = new MyCycleProcessor();
            jdc.computeCycles(ElemType.CLASS, -1, processor);
            final List<MyProcessed> res = processor.processedList;

            if (DEBUG) {
                System.out.println("res = " + res);
            }

            assertEquals(0, res.size());
        }
    }
    
    public void test_computeCycles_normal_CLASS() {
        final Jadecy jdc = newJadecy();

        final MyCycleProcessor processor = new MyCycleProcessor();
        jdc.computeCycles(ElemType.CLASS, -1, processor);
        final Set<MyProcessed> res = new HashSet<MyProcessed>(processor.processedList);

        if (DEBUG) {
            System.out.println("res = " + res);
        }

        final Set<MyProcessed> expected = new HashSet<JadecyTest.MyProcessed>();
        expected.add(
                new MyProcessed(
                        new String[]{C2N, C5N, C4N}));
        expected.add(
                new MyProcessed(
                        new String[]{C2N, C5N, C7N, C6N, C4N}));
        expected.add(
                new MyProcessed(
                        new String[]{C4N, C6N}));
        expected.add(
                new MyProcessed(
                        new String[]{C6N, C7N}));

        checkEqual(expected, res);
    }

    public void test_computeCycles_normal_PACKAGE() {
        final Jadecy jdc = newJadecy();

        final MyCycleProcessor processor = new MyCycleProcessor();
        jdc.computeCycles(ElemType.PACKAGE, -1, processor);

        final Set<MyProcessed> res = new HashSet<MyProcessed>(processor.processedList);

        if (DEBUG) {
            System.out.println("res = " + res);
        }

        final Set<MyProcessed> expected = new HashSet<JadecyTest.MyProcessed>();
        expected.add(
                new MyProcessed(
                        new String[]{P1N, P2N},
                        new String[][]{
                                new String[]{C2N, C4N},
                                new String[]{C5N, C6N},
                        }));

        checkEqual(expected, res);
    }

    public void test_computeCycles_normal_CLASS_maxSize() {
        for (int maxSize : new int[]{-1,0,1,2,3,4,5}) {
            
            if (DEBUG) {
                System.out.println();
                System.out.println("maxSize = " + maxSize);
            }
            
            final Jadecy jdc = newJadecy();

            final MyCycleProcessor processor = new MyCycleProcessor();
            jdc.computeCycles(
                    ElemType.CLASS,
                    maxSize,
                    processor);
            final Set<MyProcessed> res = new HashSet<MyProcessed>(processor.processedList);

            if (DEBUG) {
                System.out.println("res = " + res);
            }

            final Set<MyProcessed> expected = new HashSet<JadecyTest.MyProcessed>();
            if ((maxSize < 0) || (maxSize >= 3)) {
                expected.add(
                        new MyProcessed(
                                new String[]{C2N, C5N, C4N}));
            }
            if ((maxSize < 0) || (maxSize >= 5)) {
                expected.add(
                        new MyProcessed(
                                new String[]{C2N, C5N, C7N, C6N, C4N}));
            }
            if ((maxSize < 0) || (maxSize >= 2)) {
                expected.add(
                        new MyProcessed(
                                new String[]{C4N, C6N}));
                expected.add(
                        new MyProcessed(
                                new String[]{C6N, C7N}));
            }
            checkEqual(expected, res);
        }
    }

    /*
     * 
     */

    public void test_computeShortestCycles_exceptions() {
        final Jadecy jdc = newJadecy();

        try {
            jdc.computeShortestCycles(
                    null,
                    0,
                    new MyCycleProcessor());
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            jdc.computeShortestCycles(
                    ElemType.CLASS,
                    0,
                    null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
    }

    public void test_computeShortestCycles_displayNameUsage() {
        final Jadecy jdc = newJadecy();

        // Ensuring default package is in a cycle.
        final PackageData defaultP = jdc.parser().getDefaultPackageData();
        final ClassData c0 = defaultP.getOrCreateClassData("ClassInDefaultP");
        final ClassData c1 = defaultP.getClassData(C1N);
        PackageData.ensureDependency(c0, c1);
        PackageData.ensureDependency(c1, c0);

        final MyCycleProcessor processor = new MyCycleProcessor();
        jdc.computeShortestCycles(ElemType.PACKAGE, -1, processor);
        final List<MyProcessed> res = processor.processedList;

        if (DEBUG) {
            System.out.println("res = " + res);
        }

        assertEquals(NameUtils.DEFAULT_PACKAGE_DISPLAY_NAME, res.get(0).names[0]);
    }

    public void test_computeShortestCycles_ignoredIfConfinedInATopLevelClass() {
        final Jadecy jdc = newJadecy();

        for (boolean withTopLevel : new boolean[]{false,true}) {
            
            if (DEBUG) {
                System.out.println();
                System.out.println("withTopLevel = " + withTopLevel);
            }

            final PackageData defaultP = jdc.parser().getDefaultPackageData();
            defaultP.clear();

            final ClassData c1 = defaultP.getOrCreateClassData("a.b.A$B");
            final ClassData c2;
            if (withTopLevel) {
                c2 = defaultP.getOrCreateClassData("a.b.A");
            } else {
                c2 = defaultP.getOrCreateClassData("a.b.A$C");
            }
            {
                PackageData.ensureDependency(c1, c2);
                PackageData.ensureDependency(c2, c1);
            }

            /*
             * 
             */

            final MyCycleProcessor processor = new MyCycleProcessor();
            jdc.computeShortestCycles(ElemType.CLASS, -1, processor);
            final List<MyProcessed> res = processor.processedList;

            if (DEBUG) {
                System.out.println("res = " + res);
            }

            assertEquals(0, res.size());
        }
    }
    
    public void test_computeShortestCycles_normal_CLASS() {
        final Jadecy jdc = newJadecy();

        final MyCycleProcessor processor = new MyCycleProcessor();
        jdc.computeShortestCycles(ElemType.CLASS, -1, processor);
        final Set<MyProcessed> res = new HashSet<MyProcessed>(processor.processedList);

        if (DEBUG) {
            System.out.println("res = " + res);
        }

        final Set<MyProcessed> expected = new HashSet<JadecyTest.MyProcessed>();
        expected.add(
                new MyProcessed(
                        new String[]{C2N, C5N, C4N}));
        expected.add(
                new MyProcessed(
                        new String[]{C2N, C5N, C7N, C6N, C4N}));
        expected.add(
                new MyProcessed(
                        new String[]{C4N, C6N}));
        expected.add(
                new MyProcessed(
                        new String[]{C6N, C7N}));

        checkEqual(expected, res);
    }

    public void test_computeShortestCycles_normal_PACKAGE() {
        final Jadecy jdc = newJadecy();

        final MyCycleProcessor processor = new MyCycleProcessor();
        jdc.computeShortestCycles(ElemType.PACKAGE, -1, processor);

        final Set<MyProcessed> res = new HashSet<MyProcessed>(processor.processedList);

        if (DEBUG) {
            System.out.println("res = " + res);
        }

        final Set<MyProcessed> expected = new HashSet<JadecyTest.MyProcessed>();
        expected.add(
                new MyProcessed(
                        new String[]{P1N, P2N},
                        new String[][]{
                                new String[]{C2N, C4N},
                                new String[]{C5N, C6N},
                        }));

        checkEqual(expected, res);
    }

    public void test_computeShortestCycles_normal_CLASS_maxSize() {
        for (int maxSize : new int[]{-1,0,1,2,3,4,5}) {
            
            if (DEBUG) {
                System.out.println();
                System.out.println("maxSize = " + maxSize);
            }

            final Jadecy jdc = newJadecy();

            final MyCycleProcessor processor = new MyCycleProcessor();
            jdc.computeShortestCycles(
                    ElemType.CLASS,
                    maxSize,
                    processor);
            final Set<MyProcessed> res = new HashSet<MyProcessed>(processor.processedList);

            if (DEBUG) {
                System.out.println("res = " + res);
            }

            final Set<MyProcessed> expected = new HashSet<JadecyTest.MyProcessed>();
            if ((maxSize < 0) || (maxSize >= 3)) {
                expected.add(
                        new MyProcessed(
                                new String[]{C2N, C5N, C4N}));
            }
            if ((maxSize < 0) || (maxSize >= 5)) {
                expected.add(
                        new MyProcessed(
                                new String[]{C2N, C5N, C7N, C6N, C4N}));
            }
            if ((maxSize < 0) || (maxSize >= 2)) {
                expected.add(
                        new MyProcessed(
                                new String[]{C4N, C6N}));
                expected.add(
                        new MyProcessed(
                                new String[]{C6N, C7N}));
            }
            checkEqual(expected, res);
        }
    }
    
    /*
     * 
     */
    
    /**
     * Checks that computeCycles(...) and computeShortestCycles(...)
     * use proper algorithms (they behave identically for other tests).
     */
    public void test_computeAllOrShortestCycles_computedCycles() {
        final Jadecy jdc = newJadecy();
        
        /*
         * Replacing all dependencies with a small "ball graph".
         */
        
        {
            final PackageData defaultP =
                    jdc.parser().getDefaultPackageData();
            
            defaultP.clear();
            
            final int n = 3;
            final ArrayList<ClassData> cList = new ArrayList<ClassData>();
            for (int i = 0; i < n; i++) {
                final ClassData cN = defaultP.getOrCreateClassData("c" + i);
                cList.add(cN);
            }
            for (int i = 0; i < n; i++) {
                final ClassData ci = cList.get(i);
                for (int j = 0; j < n; j++) {
                    if (j == i) {
                        // Can't have dependency to self.
                        continue;
                    }
                    final ClassData cj = cList.get(j);
                    PackageData.ensureDependency(ci, cj);
                }
            }
        }
        
        /*
         * 
         */
        
        final int maxSize = -1;
        
        for (boolean mustUseShortestCycles : new boolean[]{false,true}) {
            
            if (DEBUG) {
                System.out.println();
                System.out.println("mustUseShortestCycles = " + mustUseShortestCycles);
            }

            final MyCycleProcessor processor = new MyCycleProcessor();
            if (mustUseShortestCycles) {
                jdc.computeShortestCycles(
                        ElemType.CLASS,
                        maxSize,
                        processor);
            } else {
                jdc.computeCycles(
                        ElemType.CLASS,
                        maxSize,
                        processor);
            }
            final Set<MyProcessed> res = new HashSet<MyProcessed>(processor.processedList);

            if (DEBUG) {
                System.out.println("res = " + res);
            }
            
            if (mustUseShortestCycles) {
                assertEquals(3, res.size());
            } else {
                assertEquals(5, res.size());
            }
        }
    }

    /*
     * 
     */

    public void test_computeSomeCycles_exceptions() {
        final Jadecy jdc = newJadecy();

        try {
            jdc.computeSomeCycles(
                    null,
                    0,
                    new MyCycleProcessor());
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            jdc.computeSomeCycles(
                    ElemType.CLASS,
                    0,
                    null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
    }

    public void test_computeSomeCycles_displayNameUsage() {
        final Jadecy jdc = newJadecy();

        // Ensuring default package is in a cycle.
        final PackageData defaultP = jdc.parser().getDefaultPackageData();
        final ClassData c0 = defaultP.getOrCreateClassData("ClassInDefaultP");
        final ClassData c1 = defaultP.getClassData(C1N);
        PackageData.ensureDependency(c0, c1);
        PackageData.ensureDependency(c1, c0);

        final MyCycleProcessor processor = new MyCycleProcessor();
        jdc.computeSomeCycles(ElemType.PACKAGE, -1, processor);
        final List<MyProcessed> res = processor.processedList;

        if (DEBUG) {
            System.out.println("res = " + res);
        }

        assertEquals(NameUtils.DEFAULT_PACKAGE_DISPLAY_NAME, res.get(0).names[0]);
    }

    public void test_computeSomeCycles_normal_CLASS() {
        final Jadecy jdc = newJadecy();

        final MyCycleProcessor processor = new MyCycleProcessor();
        jdc.computeSomeCycles(ElemType.CLASS, -1, processor);
        final Set<MyProcessed> res = new HashSet<MyProcessed>(processor.processedList);

        if (DEBUG) {
            System.out.println("res = " + res);
        }

        final Set<MyProcessed> expected = new HashSet<JadecyTest.MyProcessed>();
        expected.add(
                new MyProcessed(
                        new String[]{C2N, C5N, C4N}));
        expected.add(
                new MyProcessed(
                        new String[]{C2N, C5N, C7N, C6N, C4N}));
        expected.add(
                new MyProcessed(
                        new String[]{C4N, C6N}));
        expected.add(
                new MyProcessed(
                        new String[]{C6N, C7N}));

        checkContainedEtc(expected, res);
    }

    public void test_computeSomeCycles_normal_PACKAGE() {
        final Jadecy jdc = newJadecy();

        final MyCycleProcessor processor = new MyCycleProcessor();
        jdc.computeSomeCycles(ElemType.PACKAGE, -1, processor);
        final Set<MyProcessed> res = new HashSet<MyProcessed>(processor.processedList);

        if (DEBUG) {
            System.out.println("res = " + res);
        }

        final Set<MyProcessed> expected = new HashSet<JadecyTest.MyProcessed>();
        expected.add(
                new MyProcessed(
                        new String[]{P1N, P2N},
                        new String[][]{
                                new String[]{C2N, C4N},
                                new String[]{C5N, C6N},
                        }));

        checkContainedEtc(expected, res);
    }

    public void test_computeSomeCycles_normal_CLASS_maxSize() {
        for (int maxSize : new int[]{-1,0,1,2,3}) {
            
            if (DEBUG) {
                System.out.println();
                System.out.println("maxSize = " + maxSize);
            }
            
            final Jadecy jdc = newJadecy();

            final MyCycleProcessor processor = new MyCycleProcessor();
            jdc.computeSomeCycles(
                    ElemType.CLASS,
                    maxSize,
                    processor);
            final Set<MyProcessed> res = new HashSet<MyProcessed>(processor.processedList);

            if (DEBUG) {
                System.out.println("res = " + res);
            }

            final Set<MyProcessed> expected = new HashSet<JadecyTest.MyProcessed>();
            if ((maxSize < 0) || (maxSize >= 3)) {
                expected.add(
                        new MyProcessed(
                                new String[]{C2N, C5N, C4N}));
            }
            if ((maxSize < 0) || (maxSize >= 2)) {
                expected.add(
                        new MyProcessed(
                                new String[]{C4N, C6N}));
                expected.add(
                        new MyProcessed(
                                new String[]{C6N, C7N}));
            }
            checkEqual(expected, res);
        }
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------

    private Jadecy newJadecy() {
        
        // Same as Jadecy defaults.
        // Testing with different values done when testing constructor,
        // making sure that parser is properly configured.
        final boolean mustMergeNestedClasses = true;
        final boolean apiOnly = false;
        
        // Same as Jadecy defaults.
        // Testing with different values done when testing constructor
        // and withXxx methods.
        final boolean mustUseInverseDeps = false;
        final InterfaceNameFilter retainedClassNameFilter = NameFilters.any();
        
        final InterfaceDepsParser parser =
                this.virtualDepsParserFactory.newInstance(
                        mustMergeNestedClasses,
                        apiOnly);

        parser.accumulateDependencies(new File(""), ParsingFilters.defaultInstance());

        return new Jadecy(
                parser,
                mustUseInverseDeps,
                retainedClassNameFilter);
    }

    /*
     * 
     */

    /**
     * @return A new array containing packages display names
     *         from default package down to the parent package.
     * @throws IllegalArgumentException if the specified name is empty.
     */
    private static String[] getParentPackagesDisplayNames(String name) {
        if (name.length() == 0) {
            throw new IllegalArgumentException();
        }
        final String[] parts = name.split("\\.");
        final String[] result = new String[parts.length];
        String tmp = "";
        for (int i = 0; i < result.length; i++) {
            result[i] = NameUtils.toDisplayName(tmp);
            if (i == 0) {
                tmp = parts[0];
            } else {
                tmp += "." + parts[i];
            }
        }
        return result;
    }

    /*
     * 
     */

    private static void checkEqual(
            Set<MyProcessed> expected,
            Set<MyProcessed> actual) {
        if (!actual.equals(expected)) {
            {
                System.out.println();
                System.out.println("expected = " + expected);
                System.out.println("actual = " + actual);
                {
                    final HashSet<MyProcessed> exceeding = new HashSet<MyProcessed>(actual);
                    exceeding.removeAll(expected);
                    System.out.println("exceeding = " + exceeding);
                }
                {
                    final HashSet<MyProcessed> missing = new HashSet<MyProcessed>(expected);
                    missing.removeAll(actual);
                    System.out.println("missing = " + missing);
                }
            }
            assertTrue(false);
        }
    }

    private static void checkContainedEtc(
            Set<MyProcessed> allCycles,
            Set<MyProcessed> someCycles) {
        if ((allCycles.size() != 0) && (someCycles.size() == 0)) {
            // If all not empty, some must not be empty.
            {
                System.out.println();
                System.out.println("allCycles = " + allCycles);
                System.out.println("someCycles = " + someCycles);
            }
            assertTrue(false);
        }

        if (!allCycles.containsAll(someCycles)) {
            {
                System.out.println();
                System.out.println("allCycles = " + allCycles);
                System.out.println("someCycles = " + someCycles);
                final HashSet<MyProcessed> exceedingCycles = new HashSet<MyProcessed>(someCycles);
                exceedingCycles.removeAll(allCycles);
                System.out.println("exceedingCycles = " + exceedingCycles);
            }
            assertTrue(false);
        }
    }

    /*
     * 
     */

    private static boolean equal(String[] a, String[] b) {
        if ((a != null) ^ (b != null)) {
            return false;
        }
        if (a == null) {
            return true;
        }
        if (a.length != b.length) {
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (!a[i].equals(b[i])) {
                return false;
            }
        }
        return true;
    }

    private static boolean equal2(String[][] a, String[][] b) {
        if ((a != null) ^ (b != null)) {
            return false;
        }
        if (a == null) {
            return true;
        }
        if (a.length != b.length) {
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (!equal(a[i], b[i])) {
                return false;
            }
        }
        return true;
    }
}
