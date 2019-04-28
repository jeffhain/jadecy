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

import net.jadecy.code.ClassData;
import net.jadecy.code.PackageData;
import net.jadecy.names.InterfaceNameFilter;
import net.jadecy.names.NameFilters;
import net.jadecy.parsing.InterfaceDepsParser;
import net.jadecy.parsing.ParsingFilters;
import net.jadecy.utils.MemPrintStream;
import net.jadecy.virtual.AbstractVirtualCodeGraphTezt;

public class DepUnitTest extends AbstractVirtualCodeGraphTezt {

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    public void test_DepUnit_Jadecy() {
        try {
            new DepUnit(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        final Jadecy jadecy = new Jadecy(false, false);
        final DepUnit depUnit = new DepUnit(jadecy);
        assertSame(jadecy, depUnit.jadecy());
    }

    public void test_DepUnit_Jadecy_long_PrintStream() {
        final Jadecy jadecy = new Jadecy(false, false);
        final MemPrintStream stream = new MemPrintStream();

        try {
            new DepUnit(
                    null,
                    -1,
                    stream);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            new DepUnit(
                    jadecy,
                    -1,
                    null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        for (long maxNbrOfErrorsReportedPerCheck : new long[]{Long.MIN_VALUE, -1L, 0L, 1L, Long.MAX_VALUE}) {
            final DepUnit depUnit = new DepUnit(
                    jadecy,
                    maxNbrOfErrorsReportedPerCheck,
                    stream);

            assertSame(jadecy, depUnit.jadecy());
            assertEquals(maxNbrOfErrorsReportedPerCheck, depUnit.getMaxNbrOfErrorsReportedPerCheck());
            assertSame(stream, depUnit.getPrintStream());
        }
    }

    public void test_jadecy() {
        // Already covered by constructors tests.
    }

    public void test_getMaxNbrOfErrorsPerCheck() {
        // Already covered by constructors tests.
    }

    public void test_getPrintStream() {
        // Already covered by constructors tests.
    }

    public void test_clear() {
        Jadecy jadecy = newJadecy();
        // As if only C4 and C6 were parsed,
        // to only have C4->C6->C4 classes cycle.
        jadecy = jadecy.withRetainedClassNameFilter(
                NameFilters.or(
                        NameFilters.equalsName(C4N),
                        NameFilters.equalsName(C6N)));

        final DepUnit depUnit = newDepUnit(jadecy, 0L);

        /*
         * 
         */

        // Causes error.
        depUnit.addAllowedDirectDeps(
                ElemType.CLASS,
                NameFilters.equalsName(C4N),
                new InterfaceNameFilter[]{
                    NameFilters.none(),
                });

        // Causes error.
        depUnit.addAllowedDirectDeps(
                ElemType.PACKAGE,
                NameFilters.equalsName(P1N),
                new InterfaceNameFilter[]{
                    NameFilters.none(),
                });

        // Causes no error.
        depUnit.addAllowedCycle(
                ElemType.CLASS,
                new String[] {
                        C4N,
                        C6N
                });

        // Causes no error.
        depUnit.addAllowedCycle(
                ElemType.PACKAGE,
                new String[] {
                        P1N,
                        P2N
                });

        /*
         * 
         */

        try {
            depUnit.checkDeps(ElemType.CLASS);
            assertTrue(false);
        } catch (AssertionError e) {
            // ok
        }

        try {
            depUnit.checkDeps(ElemType.PACKAGE);
            assertTrue(false);
        } catch (AssertionError e) {
            // ok
        }

        depUnit.checkCycles(ElemType.CLASS);

        depUnit.checkCycles(ElemType.PACKAGE);

        /*
         * Clearing.
         */

        depUnit.clear();

        /*
         * 
         */

        depUnit.checkDeps(ElemType.CLASS);

        depUnit.checkDeps(ElemType.PACKAGE);

        try {
            depUnit.checkCycles(ElemType.CLASS);
            assertTrue(false);
        } catch (AssertionError e) {
            // ok
        }

        try {
            depUnit.checkCycles(ElemType.PACKAGE);
            assertTrue(false);
        } catch (AssertionError e) {
            // ok
        }
    }

    /*
     * Dependencies.
     */

    public void test_addAllowedDirectDeps_exceptions() {
        final DepUnit depUnit = newDepUnit();

        try {
            depUnit.addAllowedDirectDeps(
                    null,
                    NameFilters.any(),
                    new InterfaceNameFilter[]{
                        NameFilters.any(),
                    });
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            depUnit.addAllowedDirectDeps(
                    ElemType.CLASS,
                    null,
                    new InterfaceNameFilter[]{
                            NameFilters.any(),
                    });
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            depUnit.addAllowedDirectDeps(
                    ElemType.CLASS,
                    NameFilters.any(),
                    null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            depUnit.addAllowedDirectDeps(
                    ElemType.CLASS,
                    NameFilters.any(),
                    new InterfaceNameFilter[]{
                        null,
                    });
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
    }

    public void test_addIllegalDirectDeps_exceptions() {
        final DepUnit depUnit = newDepUnit();

        try {
            depUnit.addIllegalDirectDeps(
                    null,
                    NameFilters.any(),
                    new InterfaceNameFilter[]{
                        NameFilters.any(),
                    });
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            depUnit.addIllegalDirectDeps(
                    ElemType.CLASS,
                    null,
                    new InterfaceNameFilter[]{
                            NameFilters.any(),
                    });
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            depUnit.addIllegalDirectDeps(
                    ElemType.CLASS,
                    NameFilters.any(),
                    null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            depUnit.addIllegalDirectDeps(
                    ElemType.CLASS,
                    NameFilters.any(),
                    new InterfaceNameFilter[]{
                        null,
                    });
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
    }

    public void test_checkDeps_exceptions() {
        final DepUnit depUnit = newDepUnit();

        try {
            depUnit.checkDeps(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
    }

    public void test_addAllowedDirectDeps_and_checkDeps_noFilterTo() {
        final ElemType elemType = ElemType.CLASS;
        
        final DepUnit depUnit = newDepUnit();
        final MemPrintStream stream = (MemPrintStream) depUnit.getPrintStream();
        
        final InterfaceNameFilter filterFrom = NameFilters.equalsName(C1N);

        // Having no filter is same as having a filter rejecting all names.
        depUnit.addAllowedDirectDeps(
                elemType,
                filterFrom,
                new InterfaceNameFilter[]{
                });

        // Allowed dependency to nothing, so nothing is legal.
        try {
            depUnit.checkDeps(elemType);
            assertTrue(false);
        } catch (AssertionError e) {
            // ok
        }

        final String[] expectedLines = new String[]{
                "ERROR: illegal class direct dependency from " + filterFrom + " to " + ObjectN + ":",
                C1N,
                ObjectN,
                "",
                "ERROR: illegal class direct dependency from " + filterFrom + " to " + C2N + ":",
                C1N,
                C2N,
                "",
                "ERROR: illegal class direct dependency from " + filterFrom + " to " + C3N + ":",
                C1N,
                C3N,
        };
        checkEqual(expectedLines, stream);
    }

    public void test_addIllegalDirectDeps_and_checkDeps_noFilterTo() {
        final ElemType elemType = ElemType.CLASS;
        
        final DepUnit depUnit = newDepUnit();
        
        final InterfaceNameFilter filterFrom = NameFilters.equalsName(C1N);

        // Having no filter is same as having a filter rejecting all names.
        depUnit.addIllegalDirectDeps(
                elemType,
                filterFrom,
                new InterfaceNameFilter[]{
                });

        // Illegal dependency to nothing, so everything is legal.
        depUnit.checkDeps(elemType);
    }

    public void test_addAllowedDirectDeps_and_checkDeps_normal_CLASS() {
        final ElemType elemType = ElemType.CLASS;

        for (long maxNbrOfErrorsReportedPerCheck : new long[]{-1L, 0L, 1L, 2L}) {
            final DepUnit depUnit = newDepUnit(maxNbrOfErrorsReportedPerCheck);
            final MemPrintStream stream = (MemPrintStream) depUnit.getPrintStream();

            /*
             * OK: Nothing to check.
             */

            depUnit.checkDeps(elemType);
            assertEquals(0, stream.getLines().size());

            /*
             * Now indicating that C1 can directly depend only on C2 and C7,
             * while it actually depends on Object and C3.
             */

            final InterfaceNameFilter filterFrom = NameFilters.equalsName(C1N);

            depUnit.addAllowedDirectDeps(
                    elemType,
                    filterFrom,
                    new InterfaceNameFilter[]{
                            NameFilters.equalsName(C2N),
                            NameFilters.equalsName(C7N),
                    });

            try {
                depUnit.checkDeps(elemType);
                assertTrue(false);
            } catch (AssertionError e) {
                // ok
            }

            {
                final String[] expectedLines;
                if ((maxNbrOfErrorsReportedPerCheck < 0) || (maxNbrOfErrorsReportedPerCheck >= 2)) {
                    expectedLines = new String[]{
                            "ERROR: illegal class direct dependency from " + filterFrom + " to " + ObjectN + ":",
                            C1N,
                            ObjectN,
                            "",
                            "ERROR: illegal class direct dependency from " + filterFrom + " to " + C3N + ":",
                            C1N,
                            C3N,
                    };
                } else if (maxNbrOfErrorsReportedPerCheck == 1) {
                    expectedLines = new String[]{
                            "ERROR: illegal class direct dependency from " + filterFrom + " to " + ObjectN + ":",
                            C1N,
                            ObjectN,
                    };
                } else {
                    expectedLines = new String[]{};
                }
                checkEqual(expectedLines, stream);
            }
        }
    }

    public void test_addIllegalDirectDeps_and_checkDeps_normal_CLASS() {
        final ElemType elemType = ElemType.CLASS;

        for (long maxNbrOfErrorsReportedPerCheck : new long[]{-1L, 0L, 1L, 2L}) {
            final DepUnit depUnit = newDepUnit(maxNbrOfErrorsReportedPerCheck);
            final MemPrintStream stream = (MemPrintStream) depUnit.getPrintStream();

            /*
             * OK: Nothing to check.
             */

            depUnit.checkDeps(elemType);
            assertEquals(0, stream.getLines().size());

            /*
             * Now indicating that C1 can NOT directly depend on Object and C2
             * and C7, while it actually directly depends Object and C2.
             */

            final InterfaceNameFilter filterFrom = NameFilters.equalsName(C1N);

            depUnit.addIllegalDirectDeps(
                    elemType,
                    filterFrom,
                    new InterfaceNameFilter[]{
                            NameFilters.equalsName(ObjectN),
                            NameFilters.equalsName(C2N),
                            NameFilters.equalsName(C7N),
                    });

            try {
                depUnit.checkDeps(elemType);
                assertTrue(false);
            } catch (AssertionError e) {
                // ok
            }

            final String[] expectedLines;
            if ((maxNbrOfErrorsReportedPerCheck < 0) || (maxNbrOfErrorsReportedPerCheck >= 2)) {
                expectedLines = new String[]{
                        "ERROR: illegal class direct dependency from " + filterFrom + " to " + ObjectN + ":",
                        C1N,
                        ObjectN,
                        "",
                        "ERROR: illegal class direct dependency from " + filterFrom + " to " + C2N + ":",
                        C1N,
                        C2N,
                };
            } else if (maxNbrOfErrorsReportedPerCheck == 1) {
                expectedLines = new String[]{
                        "ERROR: illegal class direct dependency from " + filterFrom + " to " + ObjectN + ":",
                        C1N,
                        ObjectN,
                };
            } else {
                expectedLines = new String[]{};
            }
            checkEqual(expectedLines, stream);
        }
    }

    public void test_addAllowedDirectDeps_and_checkDeps_normal_PACKAGE() {
        final ElemType elemType = ElemType.PACKAGE;

        for (long maxNbrOfErrorsReportedPerCheck : new long[]{-1L, 0L, 1L, 2L}) {
            final DepUnit depUnit = newDepUnit(maxNbrOfErrorsReportedPerCheck);
            final MemPrintStream stream = (MemPrintStream) depUnit.getPrintStream();

            /*
             * OK: Nothing to check.
             */

            depUnit.checkDeps(elemType);
            assertEquals(0, stream.getLines().size());

            /*
             * Now indicating that p1 can directly depend only on p2 and p3,
             * while it actually directly depends on p2 and java.lang.
             */

            final InterfaceNameFilter filterFrom = NameFilters.equalsName(P1N);

            depUnit.addAllowedDirectDeps(
                    elemType,
                    filterFrom,
                    new InterfaceNameFilter[]{
                            NameFilters.equalsName(P2N),
                            NameFilters.equalsName(P3N),
                    });

            try {
                depUnit.checkDeps(elemType);
                assertTrue(false);
            } catch (AssertionError e) {
                // ok
            }

            {
                final String[] expectedLines;
                if ((maxNbrOfErrorsReportedPerCheck < 0) || (maxNbrOfErrorsReportedPerCheck >= 1)) {
                    expectedLines = new String[]{
                            "ERROR: illegal package direct dependency from " + filterFrom + " to " + JLN + ":",
                            P1N,
                            "   " + C1N,
                            "   " + C2N,
                            "   " + C3N,
                            "   " + C4N,
                            JLN,
                    };
                } else {
                    expectedLines = new String[]{};
                }
                checkEqual(expectedLines, stream);
            }
        }
    }

    /*
     * Cycles.
     */

    public void test_addAllowedCycle_exceptions() {
        final DepUnit depUnit = newDepUnit();

        try {
            depUnit.addAllowedCycle(
                    null,
                    new String[]{"a","b"});
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            depUnit.addAllowedCycle(
                    ElemType.CLASS,
                    new String[]{"a",null});
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            depUnit.addAllowedCycle(
                    ElemType.CLASS,
                    new String[]{});
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            // ok
        }

        try {
            depUnit.addAllowedCycle(
                    ElemType.CLASS,
                    new String[]{"a"});
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            // ok
        }
    }
    
    public void test_checkCycles_exceptions() {
        final DepUnit depUnit = newDepUnit();

        try {
            depUnit.checkCycles(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
    }

    public void test_addAllowedCycle_normal_CLASS() {
        Jadecy jadecy = newJadecy();
        // As if only C4 and C6 were parsed,
        // to only have C4->C6->C4 classes cycle.
        jadecy = jadecy.withRetainedClassNameFilter(
                NameFilters.or(
                        NameFilters.equalsName(C4N),
                        NameFilters.equalsName(C6N)));

        final ElemType elemType = ElemType.CLASS;

        for (long maxNbrOfErrorsReportedPerCheck : new long[]{-1L, 0L, 1L}) {
            final DepUnit depUnit = newDepUnit(
                    jadecy,
                    maxNbrOfErrorsReportedPerCheck);
            final MemPrintStream stream = (MemPrintStream) depUnit.getPrintStream();
            
            /*
             * 
             */
            
            try {
                depUnit.checkCycles(elemType);
                assertTrue(false);
            } catch (AssertionError e) {
                // ok
            }
            
            {
                final String[] expectedLines;
                if ((maxNbrOfErrorsReportedPerCheck < 0) || (maxNbrOfErrorsReportedPerCheck >= 1)) {
                    expectedLines = new String[]{
                            "ERROR: illegal classes cycle:",
                            C4N,
                            C6N,
                            C4N,
                    };
                } else {
                    expectedLines = new String[]{};
                }
                checkEqual(expectedLines, stream);
            }
            
            /*
             * 
             */
            
            depUnit.addAllowedCycle(
                    elemType,
                    new String[] {
                            C4N,
                            C6N
                    });
            
            depUnit.checkCycles(elemType);
        }
    }

    public void test_addAllowedCycle_normal_PACKAGE() {
        final ElemType elemType = ElemType.PACKAGE;

        for (long maxNbrOfErrorsReportedPerCheck : new long[]{-1L, 0L, 1L}) {
            final DepUnit depUnit = newDepUnit(maxNbrOfErrorsReportedPerCheck);
            final MemPrintStream stream = (MemPrintStream) depUnit.getPrintStream();
            
            /*
             * 
             */
            
            try {
                depUnit.checkCycles(elemType);
                assertTrue(false);
            } catch (AssertionError e) {
                // ok
            }
            
            {
                final String[] expectedLines;
                if ((maxNbrOfErrorsReportedPerCheck < 0) || (maxNbrOfErrorsReportedPerCheck >= 1)) {
                    expectedLines = new String[]{
                            "ERROR: illegal packages cycle:",
                            P1N,
                            "   " + C2N,
                            "   " + C4N,
                            P2N,
                            "   " + C5N,
                            "   " + C6N,
                            P1N,
                    };
                } else {
                    expectedLines = new String[]{};
                }
                checkEqual(expectedLines, stream);
            }
            
            /*
             * 
             */
            
            depUnit.addAllowedCycle(
                    elemType,
                    new String[] {
                            P1N,
                            P2N,
                    });
            
            depUnit.checkCycles(elemType);
        }
    }

    /*
     * Shortest cycles.
     * 
     * Quick tests, since cycles allowance logic is already tested along with
     * checkCycles(...).
     */
    
    public void test_checkShortestCycles_exceptions() {
        final DepUnit depUnit = newDepUnit();

        try {
            depUnit.checkShortestCycles(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
    }
    
    /*
     * checkXxxCycles(...).
     */

    /**
     * Checks that check methods use the proper cycles algorithm.
     */
    public void test_checkAllOrShortestCycles_computedCycles() {
        final DepUnit depUnit = newDepUnit();
        
        /*
         * Replacing all dependencies with a small "ball graph".
         */
        
        {
            final PackageData defaultP =
                    depUnit.jadecy().parser().getDefaultPackageData();
            
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
        
        final MemPrintStream stream = (MemPrintStream) depUnit.getPrintStream();
        
        for (boolean mustUseShortestCycles : new boolean[]{false,true}) {
            
            stream.clear();
            
            /*
             * Running the check.
             */
            
            try {
                if (mustUseShortestCycles) {
                    depUnit.checkShortestCycles(ElemType.CLASS);
                } else {
                    depUnit.checkCycles(ElemType.CLASS);
                }
                // Check must throw, since we didn't allow anything.
                assertTrue(false);
            } catch (AssertionError e) {
                // ok
            }
            
            /*
             * Computing the number of (illegal) cycles reported.
             */
            
            int nbrOfCycles = 0;
            for (String line : stream.getLines()) {
                if (line.startsWith("ERROR:")) {
                    nbrOfCycles++;
                }
            }

            if (mustUseShortestCycles) {
                assertEquals(3, nbrOfCycles);
            } else {
                assertEquals(5, nbrOfCycles);
            }
        }
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------

    private Jadecy newJadecy() {

        // Same as Jadecy defaults.
        final boolean mustMergeNestedClasses = true;
        final boolean apiOnly = false;
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

    private DepUnit newDepUnit() {
        final long maxNbrOfErrorsReportedPerCheck = -1;
        return newDepUnit(maxNbrOfErrorsReportedPerCheck);
    }

    private DepUnit newDepUnit(long maxNbrOfErrorsReportedPerCheck) {
        return newDepUnit(
                newJadecy(),
                maxNbrOfErrorsReportedPerCheck);
    }

    private DepUnit newDepUnit(
            Jadecy jadecy,
            long maxNbrOfErrorsReportedPerCheck) {
        return new DepUnit(
                jadecy,
                maxNbrOfErrorsReportedPerCheck,
                new MemPrintStream());
    }
}
