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

import java.io.File;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;

import net.jadecy.build.JadecyBuildConfig;
import net.jadecy.code.NameFilters;
import net.jadecy.parsing.ParsingFilters;

/**
 * Sample for just a few features.
 */
public class JadecySample {

    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------

    private static final String JAVA_HOME = JadecyBuildConfig.getJdkHome();
    
    private static final String RT_JAR_PATH = JAVA_HOME + "/jre/lib/rt.jar";

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    public static void main(String[] args) {
        
        /*
         * Creating Jadecy.
         */
        
        // True to merge nested classes with their top level class.
        boolean mustMergeNestedClasses = true;
        // False to get full dependencies.
        boolean apiOnly = false;
        
        final Jadecy jdc = new Jadecy(
                mustMergeNestedClasses,
                apiOnly);
        
        /*
         * Parsing class files.
         */
        
        jdc.parser().accumulateDependencies(new File(RT_JAR_PATH), ParsingFilters.defaultInstance());

        /*
         * 
         */
        
        {
            System.out.println("classes java.lang.Object directly depends on, in bulk with byte size:");
            
            final boolean mustIncludeBeginSet = true;
            final boolean mustIncludeDepsToBeginSet = false;
            final int maxSteps = 1;
            
            final List<SortedMap<String,Long>> depsSteps = jdc.computeDeps(
                    ElemType.CLASS,
                    NameFilters.equalsName("java.lang.Object"),
                    mustIncludeBeginSet,
                    mustIncludeDepsToBeginSet,
                    maxSteps);

            JadecyUtils.printDepsLm(
                    depsSteps,
                    System.out);
            
            final long totalByteSize = JadecyUtils.computeByteSizeLm(depsSteps);
            System.out.println();
            System.out.println("total byte size: " + totalByteSize);
        }

        System.out.println();
        System.out.println();
        System.out.println();
        
        {
            System.out.println("classes directly depending on java.util.concurrent.locks.Lock, with all their predecessors, as a graph:");
            
            final boolean mustIncludeBeginSet = true;
            final boolean mustIncludeDepsToBeginSet = false;
            final int maxSteps = 1;
            
            final Jadecy jdcInv = jdc.withMustUseInverseDeps(true);
            
            final List<SortedMap<String,SortedMap<String,SortedSet<String>>>> causesByDepByNameList =
                    jdcInv.computeDepsGraph(
                            ElemType.CLASS,
                            NameFilters.equalsName("java.util.concurrent.locks.Lock"),
                            mustIncludeBeginSet,
                            mustIncludeDepsToBeginSet,
                            NameFilters.any(), // Retaining all predecessors.
                            maxSteps);

            // Could also not merge, if want to see steps.
            final SortedMap<String,SortedMap<String,SortedSet<String>>> depsGraphMerged =
                    JadecyUtils.computeGraphMergedFromGraphLmms(causesByDepByNameList);
            
            JadecyUtils.printGraphMms(
                    depsGraphMerged,
                    true,
                    System.out);
        }

        System.out.println();
        System.out.println();
        System.out.println();
        
        {
            System.out.println("shortest path from java.lang.* to java.awt:");
            
            final List<SortedMap<String,SortedSet<String>>> depCausesByNameList =
                    jdc.computeOneShortestPath(
                            ElemType.PACKAGE,
                            NameFilters.startsWithName("java.lang"),
                            NameFilters.equalsName("java.awt"));

            JadecyUtils.printPathLms(
                    depCausesByNameList,
                    true,
                    System.out);
        }
    }
}
