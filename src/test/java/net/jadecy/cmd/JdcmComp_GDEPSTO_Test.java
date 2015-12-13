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

public class JdcmComp_GDEPSTO_Test extends AbstractJdcmTezt {

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /*
     * Basic computations.
     */

    public void test_classes() {
        final String[] args = getArgs("-gdepsto " + C4N);

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending classes and their predecessors:",
                C1N,
                C2N,
                "   -> " + C1N,
                "   -> " + C4N,
                C3N,
                "   -> " + C1N,
                C5N,
                "   -> " + C2N,
                C6N,
                "   -> " + C4N,
                "   -> " + C7N,
                C7N,
                "   -> " + C5N,
                "   -> " + C6N,
                "",
                "number of depending classes: 6",
                "",
                "number of predecessors: 6",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_incl() {
        final String[] args = getArgs("-gdepsto " + C4N + " -incl");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending classes and their predecessors:",
                C1N,
                C2N,
                "   -> " + C1N,
                "   -> " + C4N,
                C3N,
                "   -> " + C1N,
                C4N,
                "   -> " + C3N,
                "   -> " + C5N,
                "   -> " + C6N,
                C5N,
                "   -> " + C2N,
                C6N,
                "   -> " + C4N,
                "   -> " + C7N,
                C7N,
                "   -> " + C5N,
                "   -> " + C6N,
                "",
                "number of depending classes: 7",
                "",
                "number of predecessors: 7",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_steps() {
        final String[] args = getArgs("-gdepsto " + C4N + " -steps");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending classes and their predecessors:",
                "",
                "step 0:",
                "",
                "step 1:",
                C3N,
                "   -> " + C1N,
                C5N,
                "   -> " + C2N,
                C6N,
                "   -> " + C4N,
                "   -> " + C7N,
                "",
                "step 2:",
                C1N,
                C2N,
                "   -> " + C1N,
                "   -> " + C4N,
                C7N,
                "   -> " + C5N,
                "   -> " + C6N,
                "",
                "number of depending classes: 6",
                "",
                "number of predecessors: 6",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_packages() {
        final String[] args = getArgs("-gdepsto " + P2N + " -packages");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending packages and their predecessors with causes:",
                P1N,
                "      " + C5N,
                "      " + C6N,
                "   -> " + P2N,
                "",
                "number of depending packages: 1",
                "",
                "number of predecessors: 1",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_packages_incl() {
        final String[] args = getArgs("-gdepsto " + P2N + " -packages" + " -incl");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending packages and their predecessors with causes:",
                P1N,
                "      " + C5N,
                "      " + C6N,
                "   -> " + P2N,
                P2N,
                "      " + C2N,
                "      " + C4N,
                "   -> " + P1N,
                "",
                "number of depending packages: 2",
                "",
                "number of predecessors: 2",
        };
        checkEqual(expectedLines, defaultStream);
    }

    /*
     * Advanced computations (only testing with classes).
     */

    public void test_classes_minusto() {
        final String[] args = getArgs("-gdepsto " + C4N + " -minusto " + C3N);

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending classes and their predecessors:",
                C2N,
                "   -> " + C1N,
                "   -> " + C4N,
                C5N,
                "   -> " + C2N,
                C6N,
                "   -> " + C4N,
                "   -> " + C7N,
                C7N,
                "   -> " + C5N,
                "   -> " + C6N,
                "",
                "number of depending classes: 4",
                "",
                "number of predecessors: 6",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_incl_minusto() {
        final String[] args = getArgs("-gdepsto " + C4N + " -incl" + " -minusto " + C3N);

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending classes and their predecessors:",
                C2N,
                "   -> " + C1N,
                "   -> " + C4N,
                C4N,
                "   -> " + C3N,
                "   -> " + C5N,
                "   -> " + C6N,
                C5N,
                "   -> " + C2N,
                C6N,
                "   -> " + C4N,
                "   -> " + C7N,
                C7N,
                "   -> " + C5N,
                "   -> " + C6N,
                "",
                "number of depending classes: 5",
                "",
                "number of predecessors: 7",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_from() {
        final String[] args = getArgs("-gdepsto " + C4N + " -from " + P2N + ".*");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending classes and their predecessors:",
                C1N,
                C2N,
                C3N,
                C5N,
                C6N,
                "   -> " + C7N,
                C7N,
                "   -> " + C5N,
                "   -> " + C6N,
                "",
                "number of depending classes: 6",
                "",
                "number of predecessors: 3",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_incl_from() {
        final String[] args = getArgs("-gdepsto " + C4N + " -incl" + " -from " + P2N + ".*");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending classes and their predecessors:",
                C1N,
                C2N,
                C3N,
                C4N,
                "   -> " + C5N,
                "   -> " + C6N,
                C5N,
                C6N,
                "   -> " + C7N,
                C7N,
                "   -> " + C5N,
                "   -> " + C6N,
                "",
                "number of depending classes: 7",
                "",
                "number of predecessors: 3",
        };
        checkEqual(expectedLines, defaultStream);
    }
    
    public void test_classes_minusto_from() {
        final String[] args = getArgs("-gdepsto " + C4N  + " -minusto " + C3N + " -from " + P2N + ".*");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending classes and their predecessors:",
                C2N,
                C5N,
                C6N,
                "   -> " + C7N,
                C7N,
                "   -> " + C5N,
                "   -> " + C6N,
                "",
                "number of depending classes: 4",
                "",
                "number of predecessors: 3",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_incl_minusto_from() {
        final String[] args = getArgs("-gdepsto " + C4N + " -incl"  + " -minusto " + C3N + " -from " + P2N + ".*");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending classes and their predecessors:",
                C2N,
                C4N,
                "   -> " + C5N,
                "   -> " + C6N,
                C5N,
                C6N,
                "   -> " + C7N,
                C7N,
                "   -> " + C5N,
                "   -> " + C6N,
                "",
                "number of depending classes: 5",
                "",
                "number of predecessors: 3",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_maxsteps_noOrHugeLimit() {
        for (int maxSteps : new int[]{-1,Integer.MAX_VALUE}) {
            final String[] args = getArgs("-gdepsto " + C4N + " -maxsteps " + maxSteps);

            final MemPrintStream defaultStream = new MemPrintStream();
            runArgsWithVirtualDeps(args, defaultStream);

            final String[] expectedLines = new String[]{
                    "args: " + Arrays.toString(args),
                    "",
                    "depending classes and their predecessors:",
                    C1N,
                    C2N,
                    "   -> " + C1N,
                    "   -> " + C4N,
                    C3N,
                    "   -> " + C1N,
                    C5N,
                    "   -> " + C2N,
                    C6N,
                    "   -> " + C4N,
                    "   -> " + C7N,
                    C7N,
                    "   -> " + C5N,
                    "   -> " + C6N,
                    "",
                    "number of depending classes: 6",
                    "",
                    "number of predecessors: 6",
            };
            checkEqual(expectedLines, defaultStream);
        }
    }

    public void test_classes_maxsteps_0() {
        final String[] args = getArgs("-gdepsto " + C4N + " -maxsteps 0");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending classes and their predecessors:",
                "",
                "number of depending classes: 0",
                "",
                "number of predecessors: 0",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_incl_maxsteps_0() {
        final String[] args = getArgs("-gdepsto " + C4N + " -incl" + " -maxsteps 0");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending classes and their predecessors:",
                C4N,
                "   -> " + C3N,
                "   -> " + C5N,
                "   -> " + C6N,
                "",
                "number of depending classes: 1",
                "",
                "number of predecessors: 3",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_maxsteps_1() {
        final String[] args = getArgs("-gdepsto " + C4N + " -maxsteps 1");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending classes and their predecessors:",
                C3N,
                "   -> " + C1N,
                C5N,
                "   -> " + C2N,
                C6N,
                "   -> " + C4N,
                "   -> " + C7N,
                "",
                "number of depending classes: 3",
                "",
                "number of predecessors: 4",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_allCptOptions() {
        final String[] args = getArgs("-gdepsto " + C4N + " -incl" + " -from " + P1N + ".*" + " -minusto " + C1N + " -steps" + " -maxsteps -1");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending classes and their predecessors:",
                "",
                "step 0:",
                C4N,
                "   -> " + C3N,
                "",
                "step 1:",
                C3N,
                "   -> " + C1N,
                C5N,
                "   -> " + C2N,
                C6N,
                "   -> " + C4N,
                "",
                "step 2:",
                C2N,
                "   -> " + C1N,
                "   -> " + C4N,
                C7N,
                "",
                "number of depending classes: 6",
                "",
                "number of predecessors: 4",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_packages_allCptOptions() {
        final String[] args = getArgs("-gdepsto " + P2N + " -packages" + " -incl" + " -from " + P1N + ".*" + " -minusto " + C1N + " -steps" + " -maxsteps -1");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending packages and their predecessors with causes:",
                "",
                "step 0:",
                P2N,
                "      " + C2N,
                "      " + C4N,
                "   -> " + P1N,
                "",
                "step 1:",
                P1N,
                "",
                "number of depending packages: 2",
                "",
                "number of predecessors: 1",
        };
        checkEqual(expectedLines, defaultStream);
    }

    /*
     * Output options.
     */

    public void test_packages_nocauses() {
        final String[] args = getArgs("-gdepsto " + P2N + " -packages" + " -nocauses");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending packages and their predecessors:",
                P1N,
                "   -> " + P2N,
                "",
                "number of depending packages: 1",
                "",
                "number of predecessors: 1",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_packages_steps_nocauses() {
        final String[] args = getArgs("-gdepsto " + P2N + " -packages" + " -steps" + " -nocauses");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending packages and their predecessors:",
                "",
                "step 0:",
                "",
                "step 1:",
                P1N,
                "   -> " + P2N,
                "",
                "number of depending packages: 1",
                "",
                "number of predecessors: 1",
        };
        checkEqual(expectedLines, defaultStream);
    }
    
    public void test_classes_nostats() {
        final String[] args = getArgs("-gdepsto " + C4N + " -nostats");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending classes and their predecessors:",
                C1N,
                C2N,
                "   -> " + C1N,
                "   -> " + C4N,
                C3N,
                "   -> " + C1N,
                C5N,
                "   -> " + C2N,
                C6N,
                "   -> " + C4N,
                "   -> " + C7N,
                C7N,
                "   -> " + C5N,
                "   -> " + C6N,
        };
        checkEqual(expectedLines, defaultStream);
    }
    
    public void test_packages_nostats() {
        final String[] args = getArgs("-gdepsto " + P2N + " -packages" + " -nostats");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending packages and their predecessors with causes:",
                P1N,
                "      " + C5N,
                "      " + C6N,
                "   -> " + P2N,
        };
        checkEqual(expectedLines, defaultStream);
    }
    
    public void test_classes_onlystats() {
        for (boolean steps : new boolean[]{false,true}) {
            
            final String[] args;
            if (steps) {
                // -steps doesn't count here.
                args = getArgs("-gdepsto " + C4N + " -onlystats" + " -steps");
            } else {
                args = getArgs("-gdepsto " + C4N + " -onlystats");
            }

            final MemPrintStream defaultStream = new MemPrintStream();
            runArgsWithVirtualDeps(args, defaultStream);

            final String[] expectedLines = new String[]{
                    "args: " + Arrays.toString(args),
                    "",
                    "number of depending classes: 6",
                    "",
                    "number of predecessors: 6",
            };
            checkEqual(expectedLines, defaultStream);
        }
    }

    public void test_packages_onlystats() {
        for (boolean steps : new boolean[]{false,true}) {
            
            final String[] args;
            if (steps) {
                // -steps doesn't count here.
                args = getArgs("-gdepsto " + P2N + " -packages" + " -onlystats" + " -steps");
            } else {
                args = getArgs("-gdepsto " + P2N + " -packages" + " -onlystats");
            }

            final MemPrintStream defaultStream = new MemPrintStream();
            runArgsWithVirtualDeps(args, defaultStream);

            final String[] expectedLines = new String[]{
                    "args: " + Arrays.toString(args),
                    "",
                    "number of depending packages: 1",
                    "",
                    "number of predecessors: 1",
            };
            checkEqual(expectedLines, defaultStream);
        }
    }

    public void test_classes_dotformat() {
        final String[] args = getArgs("-gdepsto " + C4N + " -dotformat");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "// args: " + Arrays.toString(args),
                "digraph " + q("allsteps") + " {",
                "   " + q(C1N) + ";",
                "   " + q(C2N) + " -> " + q(C1N) + ";",
                "   " + q(C2N) + " -> " + q(C4N) + ";",
                "   " + q(C3N) + " -> " + q(C1N) + ";",
                "   " + q(C5N) + " -> " + q(C2N) + ";",
                "   " + q(C6N) + " -> " + q(C4N) + ";",
                "   " + q(C6N) + " -> " + q(C7N) + ";",
                "   " + q(C7N) + " -> " + q(C5N) + ";",
                "   " + q(C7N) + " -> " + q(C6N) + ";",
                "}",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_steps_dotformat() {
        final String[] args = getArgs("-gdepsto " + C4N + " -steps" + " -dotformat");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "// args: " + Arrays.toString(args),
                "digraph " + q("step_0") + " {",
                "}",
                "digraph " + q("step_1") + " {",
                "   " + q(C3N) + " -> " + q(C1N) + ";",
                "   " + q(C5N) + " -> " + q(C2N) + ";",
                "   " + q(C6N) + " -> " + q(C4N) + ";",
                "   " + q(C6N) + " -> " + q(C7N) + ";",
                "}",
                "digraph " + q("step_2") + " {",
                "   " + q(C1N) + ";",
                "   " + q(C2N) + " -> " + q(C1N) + ";",
                "   " + q(C2N) + " -> " + q(C4N) + ";",
                "   " + q(C7N) + " -> " + q(C5N) + ";",
                "   " + q(C7N) + " -> " + q(C6N) + ";",
                "}",
        };
        checkEqual(expectedLines, defaultStream);
    }
    
    public void test_packages_dotformat() {
        final String[] args = getArgs("-gdepsto " + P2N + " -packages" + " -dotformat");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "// args: " + Arrays.toString(args),
                "digraph " + q("allsteps") + " {",
                "   " + q(P1N) + " -> " + q(P2N) + ";",
                "}",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_packages_steps_dotformat() {
        final String[] args = getArgs("-gdepsto " + P2N + " -packages" + " -steps" + " -dotformat");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "// args: " + Arrays.toString(args),
                "digraph " + q("step_0") + " {",
                "}",
                "digraph " + q("step_1") + " {",
                "   " + q(P1N) + " -> " + q(P2N) + ";",
                "}",
        };
        checkEqual(expectedLines, defaultStream);
    }
}
