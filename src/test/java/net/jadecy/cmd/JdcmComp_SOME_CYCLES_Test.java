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
package net.jadecy.cmd;

import java.util.Arrays;

import net.jadecy.utils.MemPrintStream;

public class JdcmComp_SOME_CYCLES_Test extends AbstractJdcmTezt {

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /*
     * Basic computations.
     */

    public void test_classes() {
        final String[] args = getArgs("-somecycles");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);
        
        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "CYCLE 1:",
                C2N, C5N, C4N,
                C2N,
                "",
                "CYCLE 2:",
                C4N, C6N,
                C4N,
                "",
                "CYCLE 3:",
                C6N, C7N,
                C6N,
                "",
                "number of cycles by class name:",
                C2N + ": 1",
                C5N + ": 1",
                C7N + ": 1",
                C4N + ": 2",
                C6N + ": 2",
                "",
                "number of cycles by size:",
                "2 : 2",
                "3 : 1",
                "",
                "number of cycles found: 3",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_packages() {
        final String[] args = getArgs("-somecycles" + " -packages");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);
        
        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "CYCLE 1:",
                P1N,
                "   " + C2N,
                "   " + C4N,
                P2N,
                "   " + C5N,
                "   " + C6N,
                P1N,
                "",
                "number of cycles by cause name:",
                C2N + ": 1",
                C4N + ": 1",
                C5N + ": 1",
                C6N + ": 1",
                "",
                "number of cycles by package name:",
                P1N + ": 1",
                P2N + ": 1",
                "",
                "number of cycles by size:",
                "2 : 1",
                "",
                "number of cycles found: 1",
        };
        checkEqual(expectedLines, defaultStream);
    }

    /*
     * Advanced computations (only testing with classes).
     */

    public void test_classes_maxsize_noOrHugeLimit() {
        for (int maxSize : new int[]{-1,Integer.MAX_VALUE}) {
            final String[] args = getArgs("-somecycles" + " -maxsize " + maxSize);

            final MemPrintStream defaultStream = new MemPrintStream();
            runArgsWithVirtualDeps(args, defaultStream);
            
            final String[] expectedLines = new String[]{
                    "args: " + Arrays.toString(args),
                    "",
                    "CYCLE 1:",
                    C2N, C5N, C4N,
                    C2N,
                    "",
                    "CYCLE 2:",
                    C4N, C6N,
                    C4N,
                    "",
                    "CYCLE 3:",
                    C6N, C7N,
                    C6N,
                    "",
                    "number of cycles by class name:",
                    C2N + ": 1",
                    C5N + ": 1",
                    C7N + ": 1",
                    C4N + ": 2",
                    C6N + ": 2",
                    "",
                    "number of cycles by size:",
                    "2 : 2",
                    "3 : 1",
                    "",
                    "number of cycles found: 3",
            };
            checkEqual(expectedLines, defaultStream);
        }
    }

    public void test_classes_maxsize_none() {
        for (int maxSize : new int[]{0,1}) {
            final String[] args = getArgs("-somecycles" + " -maxsize " + maxSize);

            final MemPrintStream defaultStream = new MemPrintStream();
            runArgsWithVirtualDeps(args, defaultStream);
            
            final String[] expectedLines = new String[]{
                    "args: " + Arrays.toString(args),
                    "",
                    "number of cycles by class name:",
                    "",
                    "number of cycles by size:",
                    "",
                    "number of cycles found: 0",
            };
            checkEqual(expectedLines, defaultStream);
        }
    }

    public void test_classes_maxsize_two() {
        for (int maxSize : new int[]{2}) {
            final String[] args = getArgs("-somecycles" + " -maxsize " + maxSize);

            final MemPrintStream defaultStream = new MemPrintStream();
            runArgsWithVirtualDeps(args, defaultStream);
            
            final String[] expectedLines = new String[]{
                    "args: " + Arrays.toString(args),
                    "",
                    "CYCLE 1:",
                    C4N, C6N,
                    C4N,
                    "",
                    "CYCLE 2:",
                    C6N, C7N,
                    C6N,
                    "",
                    "number of cycles by class name:",
                    C4N + ": 1",
                    C7N + ": 1",
                    C6N + ": 2",
                    "",
                    "number of cycles by size:",
                    "2 : 2",
                    "",
                    "number of cycles found: 2",
            };
            checkEqual(expectedLines, defaultStream);
        }
    }

    public void test_classes_maxcount_noOrHugeLimit() {
        for (long maxCount : new long[]{-1L,Long.MAX_VALUE}) {
            final String[] args = getArgs("-somecycles" + " -maxcount " + maxCount);

            final MemPrintStream defaultStream = new MemPrintStream();
            runArgsWithVirtualDeps(args, defaultStream);
            
            final String[] expectedLines = new String[]{
                    "args: " + Arrays.toString(args),
                    "",
                    "CYCLE 1:",
                    C2N, C5N, C4N,
                    C2N,
                    "",
                    "CYCLE 2:",
                    C4N, C6N,
                    C4N,
                    "",
                    "CYCLE 3:",
                    C6N, C7N,
                    C6N,
                    "",
                    "number of cycles by class name:",
                    C2N + ": 1",
                    C5N + ": 1",
                    C7N + ": 1",
                    C4N + ": 2",
                    C6N + ": 2",
                    "",
                    "number of cycles by size:",
                    "2 : 2",
                    "3 : 1",
                    "",
                    "number of cycles found: 3",
            };
            checkEqual(expectedLines, defaultStream);
        }
    }

    public void test_classes_maxcount_0() {
        final String[] args = getArgs("-somecycles" + " -maxcount 0");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);
        
        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "number of cycles by class name:",
                "",
                "number of cycles by size:",
                "",
                "number of cycles found: 0",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_maxcount_1() {
        final String[] args = getArgs("-somecycles" + " -maxcount 1");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);
        
        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "CYCLE 1:",
                C2N, C5N, C4N,
                C2N,
                "",
                "number of cycles by class name:",
                C2N + ": 1",
                C4N + ": 1",
                C5N + ": 1",
                "",
                "number of cycles by size:",
                "3 : 1",
                "",
                "number of cycles found: 1",
        };
        checkEqual(expectedLines, defaultStream);
    }
    
    public void test_classes_maxsize_2_maxcount_1() {
        final String[] args = getArgs("-somecycles" + " -maxsize 2" + " -maxcount 1");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);
        
        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "CYCLE 1:",
                C4N, C6N,
                C4N,
                "",
                "number of cycles by class name:",
                C4N + ": 1",
                C6N + ": 1",
                "",
                "number of cycles by size:",
                "2 : 1",
                "",
                "number of cycles found: 1",
        };
        checkEqual(expectedLines, defaultStream);
    }

    /*
     * Output options.
     */

    public void test_packages_nocauses() {
        final String[] args = getArgs("-somecycles" + " -packages" + " -nocauses");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);
        
        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "CYCLE 1:",
                P1N,
                P2N,
                P1N,
                "",
                "number of cycles by cause name:",
                C2N + ": 1",
                C4N + ": 1",
                C5N + ": 1",
                C6N + ": 1",
                "",
                "number of cycles by package name:",
                P1N + ": 1",
                P2N + ": 1",
                "",
                "number of cycles by size:",
                "2 : 1",
                "",
                "number of cycles found: 1",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_nostats() {
        final String[] args = getArgs("-somecycles" + " -nostats");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);
        
        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "CYCLE 1:",
                C2N, C5N, C4N,
                C2N,
                "",
                "CYCLE 2:",
                C4N, C6N,
                C4N,
                "",
                "CYCLE 3:",
                C6N, C7N,
                C6N,
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_packages_nostats() {
        final String[] args = getArgs("-somecycles" + " -packages" + " -nostats");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);
        
        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "CYCLE 1:",
                P1N,
                "   " + C2N,
                "   " + C4N,
                P2N,
                "   " + C5N,
                "   " + C6N,
                P1N,
        };
        checkEqual(expectedLines, defaultStream);
    }
    
    public void test_classes_onlystats() {
        final String[] args = getArgs("-somecycles" + " -onlystats");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);
        
        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "number of cycles by class name:",
                C2N + ": 1",
                C5N + ": 1",
                C7N + ": 1",
                C4N + ": 2",
                C6N + ": 2",
                "",
                "number of cycles by size:",
                "2 : 2",
                "3 : 1",
                "",
                "number of cycles found: 3",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_packages_onlystats() {
        final String[] args = getArgs("-somecycles" + " -packages" + " -onlystats");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);
        
        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "number of cycles by cause name:",
                C2N + ": 1",
                C4N + ": 1",
                C5N + ": 1",
                C6N + ": 1",
                "",
                "number of cycles by package name:",
                P1N + ": 1",
                P2N + ": 1",
                "",
                "number of cycles by size:",
                "2 : 1",
                "",
                "number of cycles found: 1",
        };
        checkEqual(expectedLines, defaultStream);
    }
    
    public void test_classes_dotformat() {
        final String[] args = getArgs("-somecycles" + " -dotformat");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);
        
        final String[] expectedLines = new String[]{
                "// args: " + Arrays.toString(args),
                "digraph " + q("cycle_1") + " {",
                "   " + q(C2N) + " -> " + q(C5N) + ";",
                "   " + q(C4N) + " -> " + q(C2N) + ";",
                "   " + q(C5N) + " -> " + q(C4N) + ";",
                "}",
                "digraph " + q("cycle_2") + " {",
                "   " + q(C4N) + " -> " + q(C6N) + ";",
                "   " + q(C6N) + " -> " + q(C4N) + ";",
                "}",
                "digraph " + q("cycle_3") + " {",
                "   " + q(C6N) + " -> " + q(C7N) + ";",
                "   " + q(C7N) + " -> " + q(C6N) + ";",
                "}",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_packages_dotformat() {
        final String[] args = getArgs("-somecycles" + " -packages" + " -dotformat");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);
        
        final String[] expectedLines = new String[]{
                "// args: " + Arrays.toString(args),
                "digraph " + q("cycle_1") + " {",
                "   " + q(P1N) + " -> " + q(P2N) + ";",
                "   " + q(P2N) + " -> " + q(P1N) + ";",
                "}",
        };
        checkEqual(expectedLines, defaultStream);
    }
}
