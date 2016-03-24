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
package net.jadecy.allx;

import java.io.File;

import junit.framework.TestCase;
import net.jadecy.DepUnit;
import net.jadecy.ElemType;
import net.jadecy.Jadecy;
import net.jadecy.code.InterfaceNameFilter;
import net.jadecy.code.NameFilters;
import net.jadecy.parsing.ParsingFilters;
import net.jadecy.parsing.test1.A;
import net.jadecy.parsing.test2.B;
import net.jadecy.parsing.test$.$;
import net.jadecy.parsing.test$.$X;
import net.jadecy.tests.JdcTestCompHelper;

/**
 * Tests dependencies of and within this library.
 * 
 * Must not be run concurrently.
 */
public class DepsTest extends TestCase {

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    public DepsTest() {
    }
    
    /*
     * Dependencies.
     */
    
    public void testPackagesDeps_main() {
        final DepUnit depUnit = newDepUnit(JdcTestCompHelper.MAIN_SRC_PATH);
        final ElemType elemType = ElemType.PACKAGE;

        depUnit.addAllowedDirectDeps(
                elemType,
                NameFilters.equalsName("net.jadecy"),
                new InterfaceNameFilter[]{
                    NameFilters.startsWithName("java.lang"),
                    NameFilters.startsWithName("java.util"),
                    NameFilters.startsWithName("java.io"),
                    NameFilters.startsWithName("net.jadecy.code"),
                    NameFilters.startsWithName("net.jadecy.graph"),
                    NameFilters.startsWithName("net.jadecy.parsing"),
                    NameFilters.startsWithName("net.jadecy.utils"),
                });

        depUnit.addAllowedDirectDeps(
                elemType,
                NameFilters.equalsName("net.jadecy.cmd"),
                new InterfaceNameFilter[]{
                    NameFilters.startsWithName("java.lang"),
                    NameFilters.startsWithName("java.util"),
                    NameFilters.startsWithName("java.io"),
                    NameFilters.equalsName("net.jadecy"),
                    NameFilters.startsWithName("net.jadecy.code"),
                    NameFilters.startsWithName("net.jadecy.parsing"),
                    NameFilters.startsWithName("net.jadecy.utils"),
                });

        depUnit.addAllowedDirectDeps(
                elemType,
                NameFilters.equalsName("net.jadecy.code"),
                new InterfaceNameFilter[]{
                    NameFilters.startsWithName("java.lang"),
                    NameFilters.startsWithName("java.util"),
                    NameFilters.startsWithName("java.io"),
                    NameFilters.startsWithName("net.jadecy.utils"),
                    NameFilters.startsWithName("net.jadecy.graph"),
                });

        depUnit.addAllowedDirectDeps(
                elemType,
                NameFilters.equalsName("net.jadecy.comp"),
                new InterfaceNameFilter[]{
                    NameFilters.startsWithName("java.lang"),
                    NameFilters.startsWithName("java.util"),
                    NameFilters.startsWithName("java.io"),
                    NameFilters.startsWithName("net.jadecy.utils"),
                });

        depUnit.addAllowedDirectDeps(
                elemType,
                NameFilters.equalsName("net.jadecy.graph"),
                new InterfaceNameFilter[]{
                    NameFilters.startsWithName("java.lang"),
                    NameFilters.startsWithName("java.util"),
                    NameFilters.startsWithName("net.jadecy.utils"),
                });

        depUnit.addAllowedDirectDeps(
                elemType,
                NameFilters.equalsName("net.jadecy.parsing"),
                new InterfaceNameFilter[]{
                    NameFilters.startsWithName("java.lang"),
                    NameFilters.startsWithName("java.util"),
                    NameFilters.startsWithName("java.io"),
                    NameFilters.startsWithName("net.jadecy.utils"),
                    NameFilters.startsWithName("net.jadecy.code"),
                });

        depUnit.addAllowedDirectDeps(
                elemType,
                NameFilters.equalsName("net.jadecy.utils"),
                new InterfaceNameFilter[]{
                    NameFilters.startsWithName("java.lang"),
                    NameFilters.startsWithName("java.util"),
                    NameFilters.startsWithName("java.io"),
                });

        depUnit.checkDeps(elemType);
    }
    
    /*
     * Cycles.
     */
    
    public void testClassesCycles_all() {
        final DepUnit depUnit = newDepUnit(
                JdcTestCompHelper.newAllSrcDirPathArr());
        final ElemType elemType = ElemType.CLASS;

        /*
         * 
         */
        
        depUnit.addAllowedCycle(
                elemType,
                new String[]{
                        A.class.getName(),
                        B.class.getName(),
                });

        depUnit.addAllowedCycle(
                elemType,
                new String[]{
                        $.class.getName(),
                        $.$$.class.getName(),
                });

        depUnit.addAllowedCycle(
                elemType,
                new String[]{
                        $X.class.getName(),
                        $X.Y.class.getName(),
                });

        /*
         * 
         */
        
        depUnit.checkShortestCycles(elemType);
    }
    
    public void testPackagesCycles_all() {
        final DepUnit depUnit = newDepUnit(
                JdcTestCompHelper.newAllSrcDirPathArr());
        final ElemType elemType = ElemType.PACKAGE;
        
        depUnit.addAllowedCycle(
                elemType,
                new String[]{
                        A.class.getPackage().getName(),
                        B.class.getPackage().getName(),
        });
        
        depUnit.checkShortestCycles(elemType);
    }
    
    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    /**
     * @return Jadecy with parsing done.
     */
    private static Jadecy newJadecy(String... srcDirPathArr) {
        final String compDirPath = JdcTestCompHelper.ensureCompiledAndGetOutputDirPath(srcDirPathArr);

        // We don't care about intra-class cycles.
        final boolean mustMergeNestedClasses = true;
        final boolean apiOnly = false;
        final Jadecy jadecy = new Jadecy(
                mustMergeNestedClasses,
                apiOnly);
        
        // We only care about this project, so we don't parse the JDK.
        jadecy.parser().accumulateDependencies(
                new File(compDirPath),
                ParsingFilters.defaultInstance());
        
        return jadecy;
    }
    
    private static DepUnit newDepUnit(String... srcDirPathArr) {
        return new DepUnit(newJadecy(srcDirPathArr));
    }
}
