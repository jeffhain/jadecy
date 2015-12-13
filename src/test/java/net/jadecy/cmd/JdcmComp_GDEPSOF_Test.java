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

public class JdcmComp_GDEPSOF_Test extends AbstractJdcmTezt {

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /*
     * Basic computations.
     */

    public void test_classes() {
        final String[] args = getArgs("-gdepsof " + C2N);

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "classes depended on and their successors:",
                ObjectN,
                C4N,
                "   -> " + ObjectN,
                "   -> " + C2N,
                "   -> " + C6N,
                C5N,
                "   -> " + ObjectN,
                "   -> " + C4N,
                "   -> " + C7N,
                C6N,
                "   -> " + ObjectN,
                "   -> " + C4N,
                "   -> " + C7N,
                C7N,
                "   -> " + ObjectN,
                "   -> " + C6N,
                "",
                "number of classes depended on: 5",
                "",
                "number of successors: 5",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_incl() {
        final String[] args = getArgs("-gdepsof " + C3N + " -incl");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "classes depended on and their successors:",
                ObjectN,
                C2N,
                "   -> " + ObjectN,
                "   -> " + C5N,
                C3N,
                "   -> " + ObjectN,
                "   -> " + C4N,
                C4N,
                "   -> " + ObjectN,
                "   -> " + C2N,
                "   -> " + C6N,
                C5N,
                "   -> " + ObjectN,
                "   -> " + C4N,
                "   -> " + C7N,
                C6N,
                "   -> " + ObjectN,
                "   -> " + C4N,
                "   -> " + C7N,
                C7N,
                "   -> " + ObjectN,
                "   -> " + C6N,
                "",
                "number of classes depended on: 7",
                "",
                "number of successors: 6",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_steps() {
        final String[] args = getArgs("-gdepsof " + C2N + " -steps");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "classes depended on and their successors:",
                "",
                "step 0:",
                "",
                "step 1:",
                ObjectN,
                C5N,
                "   -> " + ObjectN,
                "   -> " + C4N,
                "   -> " + C7N,
                "",
                "step 2:",
                C4N,
                "   -> " + ObjectN,
                "   -> " + C2N,
                "   -> " + C6N,
                C7N,
                "   -> " + ObjectN,
                "   -> " + C6N,
                "",
                "step 3:",
                C6N,
                "   -> " + ObjectN,
                "   -> " + C4N,
                "   -> " + C7N,
                "",
                "number of classes depended on: 5",
                "",
                "number of successors: 5",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_packages() {
        final String[] args = getArgs("-gdepsof " + P1N + " -packages");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "packages depended on and their successors with causes:",
                JLN,
                P2N,
                "      " + C5N,
                "      " + C6N,
                "      " + C7N,
                "   -> " + JLN,
                "      " + C5N,
                "      " + C6N,
                "   -> " + P1N,
                "",
                "number of packages depended on: 2",
                "",
                "number of successors: 2",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_packages_incl() {
        final String[] args = getArgs("-gdepsof " + P1N + " -packages" + " -incl");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "packages depended on and their successors with causes:",
                JLN,
                P1N,
                "      " + C1N,
                "      " + C2N,
                "      " + C3N,
                "      " + C4N,
                "   -> " + JLN,
                "      " + C2N,
                "      " + C4N,
                "   -> " + P2N,
                P2N,
                "      " + C5N,
                "      " + C6N,
                "      " + C7N,
                "   -> " + JLN,
                "      " + C5N,
                "      " + C6N,
                "   -> " + P1N,
                "",
                "number of packages depended on: 3",
                "",
                "number of successors: 3",
        };
        checkEqual(expectedLines, defaultStream);
    }

    /*
     * Advanced computations (only testing with classes).
     */
    
    public void test_classes_minusof() {
        final String[] args = getArgs("-gdepsof " + C1N + " -minusof " + C4N);

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "classes depended on and their successors:",
                C3N,
                "   -> " + ObjectN,
                "   -> " + C4N,
                "",
                "number of classes depended on: 1",
                "",
                "number of successors: 2",
        };
        checkEqual(expectedLines, defaultStream);
    }
    
    public void test_classes_incl_minusof() {
        final String[] args = getArgs("-gdepsof " + C1N + " -minusof " + C4N + " -incl");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "classes depended on and their successors:",
                C1N,
                "   -> " + ObjectN,
                "   -> " + C2N,
                "   -> " + C3N,
                C3N,
                "   -> " + ObjectN,
                "   -> " + C4N,
                "",
                "number of classes depended on: 2",
                "",
                "number of successors: 4",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_into() {
        final String[] args = getArgs("-gdepsof " + C4N + " -into " + P2N + ".*");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "classes depended on and their successors:",
                ObjectN,
                C2N,
                "   -> " + C5N,
                C5N,
                "   -> " + C7N,
                C6N,
                "   -> " + C7N,
                C7N,
                "   -> " + C6N,
                "",
                "number of classes depended on: 5",
                "",
                "number of successors: 3",
        };
        checkEqual(expectedLines, defaultStream);
    }
    
    public void test_classes_incl_into() {
        final String[] args = getArgs("-gdepsof " + C4N + " -incl"  + " -into " + P2N + ".*");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "classes depended on and their successors:",
                ObjectN,
                C2N,
                "   -> " + C5N,
                C4N,
                "   -> " + C6N,
                C5N,
                "   -> " + C7N,
                C6N,
                "   -> " + C7N,
                C7N,
                "   -> " + C6N,
                "",
                "number of classes depended on: 6",
                "",
                "number of successors: 3",
        };
        checkEqual(expectedLines, defaultStream);
    }
    
    public void test_classes_minusof_into() {
        final String[] args = getArgs("-gdepsof " + C1N  + " -minusof " + C4N + " -into " + P2N + ".*");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "classes depended on and their successors:",
                C3N,
                "",
                "number of classes depended on: 1",
                "",
                "number of successors: 0",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_incl_minusof_into() {
        final String[] args = getArgs("-gdepsof " + C1N + " -incl"  + " -minusof " + C4N + " -into " + P2N + ".*");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "classes depended on and their successors:",
                C1N,
                C3N,
                "",
                "number of classes depended on: 2",
                "",
                "number of successors: 0",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_maxsteps_noOrHugeLimit() {
        for (int maxSteps : new int[]{-1,Integer.MAX_VALUE}) {
            final String[] args = getArgs("-gdepsof " + C2N + " -maxsteps " + maxSteps);

            final MemPrintStream defaultStream = new MemPrintStream();
            runArgsWithVirtualDeps(args, defaultStream);

            final String[] expectedLines = new String[]{
                    "args: " + Arrays.toString(args),
                    "",
                    "classes depended on and their successors:",
                    ObjectN,
                    C4N,
                    "   -> " + ObjectN,
                    "   -> " + C2N,
                    "   -> " + C6N,
                    C5N,
                    "   -> " + ObjectN,
                    "   -> " + C4N,
                    "   -> " + C7N,
                    C6N,
                    "   -> " + ObjectN,
                    "   -> " + C4N,
                    "   -> " + C7N,
                    C7N,
                    "   -> " + ObjectN,
                    "   -> " + C6N,
                    "",
                    "number of classes depended on: 5",
                    "",
                    "number of successors: 5",
            };
            checkEqual(expectedLines, defaultStream);
        }
    }

    public void test_classes_maxsteps_0() {
        final String[] args = getArgs("-gdepsof " + C2N + " -maxsteps 0");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "classes depended on and their successors:",
                "",
                "number of classes depended on: 0",
                "",
                "number of successors: 0",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_incl_maxsteps_0() {
        final String[] args = getArgs("-gdepsof " + C2N + " -incl" + " -maxsteps 0");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "classes depended on and their successors:",
                C2N,
                "   -> " + ObjectN,
                "   -> " + C5N,
                "",
                "number of classes depended on: 1",
                "",
                "number of successors: 2",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_maxsteps_1() {
        final String[] args = getArgs("-gdepsof " + C2N + " -maxsteps 1");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "classes depended on and their successors:",
                ObjectN,
                C5N,
                "   -> " + ObjectN,
                "   -> " + C4N,
                "   -> " + C7N,
                "",
                "number of classes depended on: 2",
                "",
                "number of successors: 3",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_allCptOptions() {
        final String[] args = getArgs("-gdepsof " + C2N + " -incl" + " -into " + P1N + ".*" + " -minusof " + C7N + " -steps" + " -maxsteps -1");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "classes depended on and their successors:",
                "",
                "step 0:",
                C2N,
                "",
                "step 1:",
                "",
                "step 2:",
                "",
                "step 3:",
                "",
                "number of classes depended on: 1",
                "",
                "number of successors: 0",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_packages_allCptOptions() {
        final String[] args = getArgs("-gdepsof " + P1N + " -packages" + " -incl" + " -into " + P1N + ".*" + " -minusof " + C7N + " -steps" + " -maxsteps -1");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "packages depended on and their successors with causes:",
                "",
                "step 0:",
                P1N,
                "",
                "step 1:",
                JLN,
                P2N,
                "      " + C5N,
                "      " + C6N,
                "   -> " + P1N,
                "",
                "number of packages depended on: 3",
                "",
                "number of successors: 1",
        };
        checkEqual(expectedLines, defaultStream);
    }

    /*
     * Output options.
     */

    public void test_packages_nocauses() {
        final String[] args = getArgs("-gdepsof " + P1N + " -packages" + " -nocauses");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "packages depended on and their successors:",
                JLN,
                P2N,
                "   -> " + JLN,
                "   -> " + P1N,
                "",
                "number of packages depended on: 2",
                "",
                "number of successors: 2",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_packages_steps_nocauses() {
        final String[] args = getArgs("-gdepsof " + P1N + " -packages" + " -steps" + " -nocauses");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "packages depended on and their successors:",
                "",
                "step 0:",
                "",
                "step 1:",
                JLN,
                P2N,
                "   -> " + JLN,
                "   -> " + P1N,
                "",
                "number of packages depended on: 2",
                "",
                "number of successors: 2",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_nostats() {
        final String[] args = getArgs("-gdepsof " + C2N + " -nostats");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "classes depended on and their successors:",
                ObjectN,
                C4N,
                "   -> " + ObjectN,
                "   -> " + C2N,
                "   -> " + C6N,
                C5N,
                "   -> " + ObjectN,
                "   -> " + C4N,
                "   -> " + C7N,
                C6N,
                "   -> " + ObjectN,
                "   -> " + C4N,
                "   -> " + C7N,
                C7N,
                "   -> " + ObjectN,
                "   -> " + C6N,
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_packages_nostats() {
        final String[] args = getArgs("-gdepsof " + P1N + " -packages" + " -nostats");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "packages depended on and their successors with causes:",
                JLN,
                P2N,
                "      " + C5N,
                "      " + C6N,
                "      " + C7N,
                "   -> " + JLN,
                "      " + C5N,
                "      " + C6N,
                "   -> " + P1N,
        };
        checkEqual(expectedLines, defaultStream);
    }
    
    public void test_classes_onlystats() {
        for (boolean steps : new boolean[]{false,true}) {
            
            final String[] args;
            if (steps) {
                // -steps doesn't count here.
                args = getArgs("-gdepsof " + C4N + " -onlystats" + " -steps");
            } else {
                args = getArgs("-gdepsof " + C4N + " -onlystats");
            }
            
            final MemPrintStream defaultStream = new MemPrintStream();
            runArgsWithVirtualDeps(args, defaultStream);

            final String[] expectedLines = new String[]{
                    "args: " + Arrays.toString(args),
                    "",
                    "number of classes depended on: 5",
                    "",
                    "number of successors: 5",
            };
            checkEqual(expectedLines, defaultStream);
        }
    }

    public void test_packages_onlystats() {
        for (boolean steps : new boolean[]{false,true}) {
            
            final String[] args;
            if (steps) {
                // -steps doesn't count here.
                args = getArgs("-gdepsof " + P1N + " -packages" + " -onlystats" + " -steps");
            } else {
                args = getArgs("-gdepsof " + P1N + " -packages" + " -onlystats");
            }

            final MemPrintStream defaultStream = new MemPrintStream();
            runArgsWithVirtualDeps(args, defaultStream);

            final String[] expectedLines = new String[]{
                    "args: " + Arrays.toString(args),
                    "",
                    "number of packages depended on: 2",
                    "",
                    "number of successors: 2",
            };
            checkEqual(expectedLines, defaultStream);
        }
    }

    public void test_classes_dotformat() {
        final String[] args = getArgs("-gdepsof " + C2N + " -dotformat");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "// args: " + Arrays.toString(args),
                "digraph " + q("allsteps") + " {",
                "   " + q(ObjectN) + ";",
                "   " + q(C4N) + " -> " + q(ObjectN) + ";",
                "   " + q(C4N) + " -> " + q(C2N) + ";",
                "   " + q(C4N) + " -> " + q(C6N) + ";",
                "   " + q(C5N) + " -> " + q(ObjectN) + ";",
                "   " + q(C5N) + " -> " + q(C4N) + ";",
                "   " + q(C5N) + " -> " + q(C7N) + ";",
                "   " + q(C6N) + " -> " + q(ObjectN) + ";",
                "   " + q(C6N) + " -> " + q(C4N) + ";",
                "   " + q(C6N) + " -> " + q(C7N) + ";",
                "   " + q(C7N) + " -> " + q(ObjectN) + ";",
                "   " + q(C7N) + " -> " + q(C6N) + ";",
                "}",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_steps_dotformat() {
        final String[] args = getArgs("-gdepsof " + C2N + " -steps" + " -dotformat");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "// args: " + Arrays.toString(args),
                "digraph " + q("step_0") + " {",
                "}",
                "digraph " + q("step_1") + " {",
                "   " + q(ObjectN) + ";",
                "   " + q(C5N) + " -> " + q(ObjectN) + ";",
                "   " + q(C5N) + " -> " + q(C4N) + ";",
                "   " + q(C5N) + " -> " + q(C7N) + ";",
                "}",
                "digraph " + q("step_2") + " {",
                "   " + q(C4N) + " -> " + q(ObjectN) + ";",
                "   " + q(C4N) + " -> " + q(C2N) + ";",
                "   " + q(C4N) + " -> " + q(C6N) + ";",
                "   " + q(C7N) + " -> " + q(ObjectN) + ";",
                "   " + q(C7N) + " -> " + q(C6N) + ";",
                "}",
                "digraph " + q("step_3") + " {",
                "   " + q(C6N) + " -> " + q(ObjectN) + ";",
                "   " + q(C6N) + " -> " + q(C4N) + ";",
                "   " + q(C6N) + " -> " + q(C7N) + ";",
                "}",
        };
        checkEqual(expectedLines, defaultStream);
    }
    
    public void test_packages_dotformat() {
        final String[] args = getArgs("-gdepsof " + P1N + " -packages" + " -dotformat");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "// args: " + Arrays.toString(args),
                "digraph " + q("allsteps") + " {",
                "   " + q(JLN) + ";",
                "   " + q(P2N) + " -> " + q(JLN) + ";",
                "   " + q(P2N) + " -> " + q(P1N) + ";",
                "}",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_packages_steps_dotformat() {
        final String[] args = getArgs("-gdepsof " + P1N + " -packages" + " -steps" + " -dotformat");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "// args: " + Arrays.toString(args),
                "digraph " + q("step_0") + " {",
                "}",
                "digraph " + q("step_1") + " {",
                "   " + q(JLN) + ";",
                "   " + q(P2N) + " -> " + q(JLN) + ";",
                "   " + q(P2N) + " -> " + q(P1N) + ";",
                "}",
        };
        checkEqual(expectedLines, defaultStream);
    }
}
