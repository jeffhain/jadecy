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
package net.jadecy.code;

import java.util.List;

import net.jadecy.graph.InterfaceVertex;
import net.jadecy.names.AbstractNameFilter;
import net.jadecy.names.InterfaceNameFilter;
import net.jadecy.names.NameFilters;

public class CodeDataUtilsPerf {

    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------

    private static final int NBR_OF_RUNS = 4;

    private static final int NBR_OF_CALLS = 10;

    /**
     * 0 means just default package.
     */
    private static final int PACKAGE_TREE_DEPTH = CodeTestUtils.log2(100 * 1000);

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    public static void main(String[] args) {
        newRun(args);
    }

    public static void newRun(String[] args) {
        new CodeDataUtilsPerf().run(args);
    }
    
    public CodeDataUtilsPerf() {
    }
    
    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    private void run(String[] args) {
        System.out.println("--- " + CodeDataUtilsPerf.class.getSimpleName() + "... ---");
        System.out.println("number of calls = " + NBR_OF_CALLS);

        this.bench_newClassDataList();
        
        this.bench_newPackageDataList();

        System.out.println("--- ..." + CodeDataUtilsPerf.class.getSimpleName() + " ---");
    }

    /**
     * Benching single class retrieval, at last leaf of a binary package tree.
     */
    private void bench_newClassDataList() {
        final PackageData defaultP = new PackageData();
        
        final ClassData lastClassData = CodeTestUtils.createSubtree(defaultP, PACKAGE_TREE_DEPTH);
        final String lastClassName = lastClassData.name();
        
        for (boolean usePrefix : new boolean[]{false,true}) {
            
            final InterfaceNameFilter filter;
            if (usePrefix) {
                // Filter using prefix.
                filter = NameFilters.equalsName(lastClassName);
            } else {
                filter = new AbstractNameFilter() {
                    @Override
                    public boolean accept(String name) {
                        return name.equals(lastClassName);
                    }
                };
            }
            
            for (int k = 0; k < NBR_OF_RUNS; k++) {
                long a = System.nanoTime();
                for (int i = 0; i < NBR_OF_CALLS; i++) {
                    final List<InterfaceVertex> list = CodeDataUtils.newClassDataList(
                            defaultP,
                            filter);
                    // Checking that it computes the expected result
                    // (doesn't cost much time).
                    if ((list.size() != 1)
                            || (list.get(0) != lastClassData)) {
                        throw new AssertionError();
                    }
                }
                long b = System.nanoTime();
                System.out.println("class retrieval" + (usePrefix ? " (using prefix)" : " (brute force)") + " took " + ((b-a)/1000/1e6) + " s");
            }
        }
    }

    /**
     * Benching single package retrieval, of last leaf of a binary package tree.
     */
    private void bench_newPackageDataList() {
        final PackageData defaultP = new PackageData();
        
        final ClassData lastClassData = CodeTestUtils.createSubtree(defaultP, PACKAGE_TREE_DEPTH);
        final PackageData lastPackageData = (PackageData) lastClassData.parent();
        final String lastPackageName = lastPackageData.name();
        
        for (boolean usePrefix : new boolean[]{false,true}) {
            
            final InterfaceNameFilter filter;
            if (usePrefix) {
                // Filter using prefix.
                filter = NameFilters.equalsName(lastPackageName);
            } else {
                filter = new AbstractNameFilter() {
                    @Override
                    public boolean accept(String name) {
                        return name.equals(lastPackageName);
                    }
                };
            }
            
            for (int k = 0; k < NBR_OF_RUNS; k++) {
                long a = System.nanoTime();
                for (int i = 0; i < NBR_OF_CALLS; i++) {
                    final List<InterfaceVertex> list = CodeDataUtils.newPackageDataList(
                            defaultP,
                            filter);
                    // Checking that it computes the expected result
                    // (doesn't cost much time).
                    if ((list.size() != 1)
                            || (list.get(0) != lastPackageData)) {
                        throw new AssertionError();
                    }
                }
                long b = System.nanoTime();
                System.out.println("package retrieval" + (usePrefix ? " (using prefix)" : " (brute force)") + " took " + ((b-a)/1000/1e6) + " s");
            }
        }
    }
}
