/*
 * Copyright 2015-2019 Jeff Hain
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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import net.jadecy.graph.CyclesUtils;
import net.jadecy.names.InterfaceNameFilter;
import net.jadecy.names.NameFilters;
import net.jadecy.utils.ArgsUtils;
import net.jadecy.utils.ComparableArrayList;

/**
 * Utility for basic dependencies checks in unit tests.
 */
public class DepUnit {

    /*
     * Taking care of determinism.
     */

    //--------------------------------------------------------------------------
    // PRIVATE CLASSES
    //--------------------------------------------------------------------------

    private static class MyFromTo {
        final InterfaceNameFilter filterFrom;
        final InterfaceNameFilter[] filterToArr;
        /**
         * @throws NullPointerException if any argument or filter is null.
         */
        public MyFromTo(
                InterfaceNameFilter filterFrom,
                InterfaceNameFilter[] filterToArr) {
            ArgsUtils.requireNonNull(filterFrom);
            ArgsUtils.requireNonNull2(filterToArr);
            this.filterFrom = filterFrom;
            this.filterToArr = filterToArr;
        }
    }

    /**
     * Things allowed, either for classes or packages.
     */
    private static class MyElemTypeData {
        final ElemType elemType;
        final List<MyFromTo> allowedDirectDepsList = new ArrayList<MyFromTo>();
        /*
         * This set doesn't need to be sorted, as we don't iterate on it.
         */
        final Set<List<String>> allowedNormalizedCycleSet =
                new HashSet<List<String>>();
        public MyElemTypeData(ElemType elemType) {
            this.elemType = elemType;
        }
        public void clear() {
            this.allowedDirectDepsList.clear();
            this.allowedNormalizedCycleSet.clear();
        }
    }
    
    private class MyCycleProcessor implements InterfaceCycleProcessor {
        private final MyElemTypeData data;
        private long nbrOfErrorsReported = 0L;
        boolean foundError = false;
        public MyCycleProcessor(MyElemTypeData data) {
            this.data = data;
        }
        //@Override
        public boolean processCycle(String[] names, String[][] causesArr) {
            
            final ArrayList<String> normalizedCycleAsList = new ArrayList<String>(names.length);
            // Already normalized.
            for (String name : names) {
                normalizedCycleAsList.add(name);
            }
            
            final boolean isError = !data.allowedNormalizedCycleSet.contains(normalizedCycleAsList);
            
            this.foundError |= isError;
            
            if (isError) {
                if ((maxNbrOfErrorsReportedPerCheck >= 0)
                        && (this.nbrOfErrorsReported >= maxNbrOfErrorsReportedPerCheck)) {
                    return true;
                }

                if (this.nbrOfErrorsReported != 0) {
                    stream.println();
                }
                stream.println("ERROR: illegal " + data.elemType.toStringPluralLC() + " cycle:");
                JadecyUtils.printCycle(names, causesArr, stream);

                this.nbrOfErrorsReported++;
            }
            
            return false;
        }
    }

    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------
    
    private final Jadecy jadecy;
    
    /**
     * If < 0, no limit.
     */
    private final long maxNbrOfErrorsReportedPerCheck;

    private final PrintStream stream;

    private final MyElemTypeData[] dataByTypeOrdinal = new MyElemTypeData[ElemType.valuesList().size()];
    {
        for (ElemType elemType : ElemType.valuesList()) {
            dataByTypeOrdinal[elemType.ordinal()] = new MyElemTypeData(elemType);
        }
    }

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /**
     * Uses System.out as stream, with no limit on errors per check.
     * 
     * @param jadecy Jadecy instance to use for computations.
     * @throws NullPointerException if jadecy is null.
     */
    public DepUnit(
            Jadecy jadecy) {
        this(
                jadecy,
                -1,
                System.out);
    }
    
    /**
     * @param jadecy Jadecy instance to use for computations.
     * @param maxNbrOfErrorsReportedPerCheck Max number of errors reported per
     *        checkXxx method call, possibly 0. If < 0, no limit.
     * @param stream Stream where to output errors.
     * @throws NullPointerException if any argument is null.
     */
    public DepUnit(
            Jadecy jadecy,
            long maxNbrOfErrorsReportedPerCheck,
            PrintStream stream) {
        
        ArgsUtils.requireNonNull(jadecy);
        ArgsUtils.requireNonNull(stream);
        
        this.jadecy = jadecy;
        
        this.maxNbrOfErrorsReportedPerCheck = maxNbrOfErrorsReportedPerCheck;

        this.stream = stream;
    }
    
    /**
     * @return The used Jadecy instance. Never null.
     */
    public Jadecy jadecy() {
        return this.jadecy;
    }
    
    /**
     * @return The used max number of reported errors per checkXxx method call.
     *         If < 0, no limit.
     */
    public long getMaxNbrOfErrorsReportedPerCheck() {
        return this.maxNbrOfErrorsReportedPerCheck;
    }

    /**
     * @return The used stream for reporting errors.
     */
    public PrintStream getPrintStream() {
        return this.stream;
    }

    /**
     * Clears all registered allowed (or illegal) dependencies or cycles.
     */
    public void clear() {
        for (MyElemTypeData data : dataByTypeOrdinal) {
            data.clear();
        }
    }

    /*
     * Dependencies.
     */

    /**
     * Adds definition for some allowed direct dependencies, i.e. during check,
     * any dependency from an element matching filterFrom, to an element NOT
     * matching any of filterToArr filters, is considered to be an error, even
     * if it's allowed by subsequent calls to this method (i.e. calls to this
     * method are additive but not cumulative, which would be difficult to do
     * since we use filters and not just elements names).
     * 
     * Dependencies from elements not matching any filterFrom specified
     * to this method, are always considered allowed (else user would need
     * to specify all actual dependencies to avoid errors during check).
     * 
     * @param elemType Type of elements to work on.
     * @param filterFrom Filter constraining the set of elements which direct
     *        dependencies must be considered.
     * @param filterToArr Array of filters constraining the set of elements
     *        to which direct dependencies are allowed.
     * @throws NullPointerException if any argument or filter is null.
     */
    public void addAllowedDirectDeps(
            ElemType elemType,
            InterfaceNameFilter filterFrom,
            InterfaceNameFilter[] filterToArr) {
        
        final MyElemTypeData data = this.dataByTypeOrdinal[elemType.ordinal()];
        
        data.allowedDirectDepsList.add(
                new MyFromTo(
                        filterFrom,
                        filterToArr.clone()));
    }

    /**
     * Convenience method, delegating to addAllowedDirectDeps(...) with a single
     * filterTo being NameFilters.not(NameFilters.or(filterToArr)).
     * 
     * @param elemType Type of elements to work on.
     * @param filterFrom Filter constraining the set of elements which direct
     *        dependencies must be considered.
     * @param filterToArr Array of filters constraining the set of elements
     *        to which direct dependencies are illegal.
     * @throws NullPointerException if any argument or filter is null.
     */
    public void addIllegalDirectDeps(
            ElemType elemType,
            InterfaceNameFilter filterFrom,
            InterfaceNameFilter[] filterToArr) {
        this.addAllowedDirectDeps(
                elemType,
                filterFrom,
                new InterfaceNameFilter[]{
                        NameFilters.not(NameFilters.or(filterToArr))
                });
    }

    /**
     * @param elemType Type of elements to work on.
     * @throws AssertionError if found non allowed dependencies, after printing
     *         them to the stream.
     * @throws NullPointerException if elemType is null.
     */
    public void checkDeps(ElemType elemType) {
        
        // Implicit null check.
        final MyElemTypeData data = this.dataByTypeOrdinal[elemType.ordinal()];
        
        boolean foundError = false;
        
        long nbrOfErrorsReported = 0;
        
        LOOP_1 : for (MyFromTo fromTo : data.allowedDirectDepsList) {

            final InterfaceNameFilter filterFrom = fromTo.filterFrom;
            
            /*
             * 
             */

            final boolean mustIncludeBeginSet = false;
            // True to check dependencies to elements in begin set.
            final boolean mustIncludeDepsToBeginSet = true;
            // Only checking direct dependencies.
            final int maxSteps = 1;
            final Set<String> depSet = JadecyUtils.computeDepsMergedFromDepsLm(
                    this.jadecy.computeDeps(
                            elemType,
                            filterFrom,
                            mustIncludeBeginSet,
                            mustIncludeDepsToBeginSet,
                            maxSteps)).keySet();

            /*
             * For each dependency, checking that it matches
             * at least one of the filters.
             */

            for (String dep : depSet) {

                boolean depFoundLegal = false;

                for (InterfaceNameFilter filterTo : fromTo.filterToArr) {
                    if (filterTo.accept(dep)) {
                        depFoundLegal = true;
                        break;
                    }
                }

                if (!depFoundLegal) {
                    foundError = true;
                    
                    if ((this.maxNbrOfErrorsReportedPerCheck >= 0)
                            && (nbrOfErrorsReported >= this.maxNbrOfErrorsReportedPerCheck)) {
                        break LOOP_1;
                    }
                    
                    if (nbrOfErrorsReported != 0) {
                        this.stream.println();
                    }
                    nbrOfErrorsReported++;
                    this.stream.println(
                            "ERROR: illegal "
                                    + elemType.toStringSingularLC()
                                    + " direct dependency from "
                                    + filterFrom
                                    + " to "
                                    + dep
                                    + ":");

                    final InterfaceNameFilter filterFromWithoutBadDep =
                            NameFilters.and(
                                    filterFrom,
                                    NameFilters.not(NameFilters.equalsName(dep)));
                    final List<SortedMap<String,SortedSet<String>>> depCausesByNameList =
                            this.jadecy.computeOneShortestPath(
                                    elemType,
                                    filterFromWithoutBadDep,
                                    NameFilters.equalsName(dep));
                    final boolean mustPrintCauses = true;
                    JadecyUtils.printPathLms(
                            depCausesByNameList,
                            mustPrintCauses,
                            this.stream);
                }
            }
        }
        
        if (foundError) {
            throw new AssertionError();
        }
    }

    /*
     * Cycles.
     */

    /**
     * Adds definition for an allowed cycle.
     * 
     * A name must not appear twice in a cycle, and the cycle can start at any
     * of the involved names.
     * 
     * @param elemType Type of elements to work on.
     * @param cycle Elements names defining an allowed cycle.
     *        Length must be >= 2.
     * @throws NullPointerException if any argument or cycle element name is
     *         null.
     * @throws IllegalArgumentException if the length of the specified cycle is
     *         strictly inferior to 2.
     */
    public void addAllowedCycle(
            ElemType elemType,
            String[] cycle) {
        
        ArgsUtils.requireNonNull2(cycle);
        
        if (cycle.length < 2) {
            throw new IllegalArgumentException("cycle length must be >= 2");
        }

        // Implicit null check.
        final MyElemTypeData data = this.dataByTypeOrdinal[elemType.ordinal()];
        
        final String[] myNormCycle = (String[]) cycle.clone();
        CyclesUtils.normalizeCycle(myNormCycle);

        final ComparableArrayList<String> myCycle = new ComparableArrayList<String>();
        for (String name : myNormCycle) {
            myCycle.add(name);
        }

        data.allowedNormalizedCycleSet.add(myCycle);
    }

    /**
     * Checks allowed cycles against those computed by Jadecy.computeCycles(...),
     * i.e. against all cycles: for highly tangled code, you might want to use
     * checkShortestCycles(...) instead, or to only take sub-parts of your
     * code base into account.
     * 
     * @param elemType Type of elements to work on.
     * @throws NullPointerException if elemType is null.
     * @throws AssertionError if found non allowed cycles, after printing them
     *         to the stream.
     */
    public void checkCycles(ElemType elemType) {
        
        // Implicit null check.
        final MyElemTypeData data = this.dataByTypeOrdinal[elemType.ordinal()];
        
        final int maxSize = -1;
        final MyCycleProcessor processor = new MyCycleProcessor(data);
        this.jadecy.computeCycles(elemType, maxSize, processor);
        
        if (processor.foundError) {
            throw new AssertionError();
        }
    }

    /**
     * Checks allowed cycles against those computed by Jadecy.computeShortestCycles(...),
     * i.e. against typically much less cycles than when using checkCycles(...).
     * 
     * @param elemType Type of elements to work on.
     * @throws NullPointerException if elemType is null.
     * @throws AssertionError if found non allowed shortest cycles, after
     *         printing them to the stream.
     */
    public void checkShortestCycles(ElemType elemType) {
        
        // Implicit null check.
        final MyElemTypeData data = this.dataByTypeOrdinal[elemType.ordinal()];
        
        final int maxSize = -1;
        final MyCycleProcessor processor = new MyCycleProcessor(data);
        this.jadecy.computeShortestCycles(elemType, maxSize, processor);
        
        if (processor.foundError) {
            throw new AssertionError();
        }
    }
}
