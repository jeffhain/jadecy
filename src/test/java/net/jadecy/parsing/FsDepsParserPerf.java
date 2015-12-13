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
package net.jadecy.parsing;

import java.io.File;

import net.jadecy.tests.JdcTestCompHelper;

public class FsDepsParserPerf {

    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------

    private static final int NBR_OF_RUNS = 4;

    private static final String RT_JAR_FILE_PATH = JdcTestCompHelper.JAVA_HOME + "/jre/lib/rt.jar";

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    public static void main(String[] args) {
        newRun(args);
    }

    public static void newRun(String[] args) {
        new FsDepsParserPerf().run(args);
    }
    
    public FsDepsParserPerf() {
    }
    
    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    private void run(String[] args) {
        System.out.println("--- " + FsDepsParserPerf.class.getSimpleName() + "... ---");

        this.bench_accumulateDependencies(RT_JAR_FILE_PATH);

        System.out.println("--- ..." + FsDepsParserPerf.class.getSimpleName() + " ---");
    }

    private void bench_accumulateDependencies(String filePath) {

        final File file = new File(filePath);
        
        for (boolean mustMergeNestedClasses : new boolean[]{false,true}) {
            for (boolean apiOnly : new boolean[]{false,true}) {
                bench_accumulateDependencies(
                        file,
                        mustMergeNestedClasses,
                        apiOnly);
            }
        }
    }

    private void bench_accumulateDependencies(
            File file,
            boolean mustMergeNestedClasses,
            boolean apiOnly) {

        System.out.println();

        for (int k = 0; k < NBR_OF_RUNS; k++) {
            long a = System.nanoTime();
            final long count;
            {
                final FsDepsParser parser = new FsDepsParser(
                        mustMergeNestedClasses,
                        apiOnly);
                parser.accumulateDependencies(
                        file,
                        ParsingFilters.defaultInstance());
                count = parser.getDefaultPackageData().getSubtreeClassCount();
            }
            long b = System.nanoTime();
            System.out.println(
                    "parsing "
                            + file.getAbsolutePath()
                            + " ("
                            + count
                            + " classes) (merge = "
                            + mustMergeNestedClasses
                            + ", apiOnly = "
                            + apiOnly
                            + ") took "
                            + ((b-a)/1000/1e6)
                            + " s");
        }
    }
}
