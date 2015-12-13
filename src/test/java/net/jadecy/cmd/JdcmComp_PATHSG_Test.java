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

public class JdcmComp_PATHSG_Test extends AbstractJdcmTezt {
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /*
     * Basic computations.
     */

    public void test_classes() {
        // Paths graph from C1 to C2 (the whole graph except C5,
        // which can only be reached from C2 which is the end set).
        final String[] args = getArgs("-pathsg " + C1N + " " + C2N);

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "paths graph:",
                C1N,
                "   -> " + C2N,
                "   -> " + C3N,
                C2N,
                C3N,
                "   -> " + C4N,
                C4N,
                "   -> " + C2N,
                "   -> " + C6N,
                C6N,
                "   -> " + C4N,
                "   -> " + C7N,
                C7N,
                "   -> " + C6N,
                "",
                "number of classes: 6",
        };
        checkEqual(expectedLines, defaultStream);
    }
    
    public void test_packages() {
        // Paths graph from p1 to p2.
        final String[] args = getArgs("-pathsg " + P1N + " " + P2N + " -packages");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "paths graph with dependencies causes:",
                P1N,
                "      " + C2N,
                "      " + C4N,
                "   -> " + P2N,
                P2N,
                "      " + C5N,
                "      " + C6N,
                "   -> " + P1N,
                "",
                "number of packages: 2",
        };
        checkEqual(expectedLines, defaultStream);
    }
    
    /*
     * Advanced computations (only testing with classes).
     */

    public void test_classes_noPath() {
        // No path.
        final String[] args = getArgs("-pathsg " + C1N + " nomatch");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "paths graph:",
                "",
                "number of classes: 0",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_toSelf() {
        // Paths graph from C1 to C1
        // (exploration stops each time end is reached,
        // so here we stop right away).
        final String[] args = getArgs("-pathsg " + C1N + " " + C1N);

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "paths graph:",
                C1N,
                "",
                "number of classes: 1",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_maxsteps_noOrHugeLimit() {
        for (int maxSteps : new int[]{-1,Integer.MAX_VALUE}) {
            // Paths graph from C1 to C2, no limit on number of steps.
            final String[] args = getArgs("-pathsg " + C1N + " " + C2N + " -maxsteps " + maxSteps);

            final MemPrintStream defaultStream = new MemPrintStream();
            runArgsWithVirtualDeps(args, defaultStream);

            final String[] expectedLines = new String[]{
                    "args: " + Arrays.toString(args),
                    "",
                    "paths graph:",
                    C1N,
                    "   -> " + C2N,
                    "   -> " + C3N,
                    C2N,
                    C3N,
                    "   -> " + C4N,
                    C4N,
                    "   -> " + C2N,
                    "   -> " + C6N,
                    C6N,
                    "   -> " + C4N,
                    "   -> " + C7N,
                    C7N,
                    "   -> " + C6N,
                    "",
                    "number of classes: 6",
            };
            checkEqual(expectedLines, defaultStream);
        }
    }
    
    public void test_classes_maxsteps_3() {
        // Paths graph from C1 to C2, with up to 3 step from C1 or to C2.
        final String[] args = getArgs("-pathsg " + C1N + " " + C2N + " -maxsteps 3");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "paths graph:",
                C1N,
                "   -> " + C2N,
                "   -> " + C3N,
                C2N,
                C3N,
                "   -> " + C4N,
                C4N,
                "   -> " + C2N,
                "   -> " + C6N,
                C6N,
                "   -> " + C4N,
                "",
                "number of classes: 5",
        };
        checkEqual(expectedLines, defaultStream);
    }
    
    public void test_classes_maxsteps_0() {
        // Paths graph from C1 to C2, no step.
        final String[] args = getArgs("-pathsg " + C1N + " " + C2N + " -maxsteps 0");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "paths graph:",
                "",
                "number of classes: 0",
        };
        checkEqual(expectedLines, defaultStream);
    }
    
    public void test_classes_maxsteps_1() {
        // Paths graph from C1 to C2, with up to 1 step from C1 or to C2.
        final String[] args = getArgs("-pathsg " + C1N + " " + C2N + " -maxsteps 1");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "paths graph:",
                C1N,
                "   -> " + C2N,
                C2N,
                "",
                "number of classes: 2",
        };
        checkEqual(expectedLines, defaultStream);
    }

    /*
     * Output options.
     */
    
    public void test_packages_nocauses() {
        // Paths graph from p1 to p2, without causes.
        final String[] args = getArgs("-pathsg " + P1N + " " + P2N + " -packages" + " -nocauses");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "paths graph:",
                P1N,
                "   -> " + P2N,
                P2N,
                "   -> " + P1N,
                "",
                "number of packages: 2",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_nostats() {
        // Paths graph from C1 to C2 (the whole graph except C5,
        // which can only be reached from C2 which is the end set).
        final String[] args = getArgs("-pathsg " + C1N + " " + C2N + " -nostats");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "paths graph:",
                C1N,
                "   -> " + C2N,
                "   -> " + C3N,
                C2N,
                C3N,
                "   -> " + C4N,
                C4N,
                "   -> " + C2N,
                "   -> " + C6N,
                C6N,
                "   -> " + C4N,
                "   -> " + C7N,
                C7N,
                "   -> " + C6N,
        };
        checkEqual(expectedLines, defaultStream);
    }
    
    public void test_packages_nostats() {
        // Paths graph from p1 to p2.
        final String[] args = getArgs("-pathsg " + P1N + " " + P2N + " -packages" + " -nostats");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "paths graph with dependencies causes:",
                P1N,
                "      " + C2N,
                "      " + C4N,
                "   -> " + P2N,
                P2N,
                "      " + C5N,
                "      " + C6N,
                "   -> " + P1N,
        };
        checkEqual(expectedLines, defaultStream);
    }
    
    public void test_classes_onlystats() {
        // Paths graph from C1 to C2, only stats.
        final String[] args = getArgs("-pathsg " + C1N + " " + C2N + " -onlystats");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "number of classes: 6",
        };
        checkEqual(expectedLines, defaultStream);
    }
    
    public void test_packages_onlystats() {
        // Paths graph from p1 to p2, only stats.
        final String[] args = getArgs("-pathsg " + P1N + " " + P2N + " -packages" + " -onlystats");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "number of packages: 2",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_dotformat() {
        final String[] args = getArgs("-pathsg " + C1N + " " + C2N + " -dotformat");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "// args: " + Arrays.toString(args),
                "digraph " + q("allsteps") + " {",
                "   " + q(C1N) + " -> " + q(C2N) + ";",
                "   " + q(C1N) + " -> " + q(C3N) + ";",
                "   " + q(C2N) + ";",
                "   " + q(C3N) + " -> " + q(C4N) + ";",
                "   " + q(C4N) + " -> " + q(C2N) + ";",
                "   " + q(C4N) + " -> " + q(C6N) + ";",
                "   " + q(C6N) + " -> " + q(C4N) + ";",
                "   " + q(C6N) + " -> " + q(C7N) + ";",
                "   " + q(C7N) + " -> " + q(C6N) + ";",
                "}",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_packages_dotformat() {
        final String[] args = getArgs("-pathsg " + P1N + " " + P2N + " -packages" + " -dotformat");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "// args: " + Arrays.toString(args),
                "digraph " + q("allsteps") + " {",
                "   " + q(P1N) + " -> " + q(P2N) + ";",
                "   " + q(P2N) + " -> " + q(P1N) + ";",
                "}",
        };
        checkEqual(expectedLines, defaultStream);
    }
}
