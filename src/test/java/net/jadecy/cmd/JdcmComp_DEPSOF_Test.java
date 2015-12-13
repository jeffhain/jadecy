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

public class JdcmComp_DEPSOF_Test extends AbstractJdcmTezt {

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /*
     * Basic computations.
     */

    public void test_classes() {
        final String[] args = getArgs("-depsof " + C2N);

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "classes depended on and their byte size:",
                ObjectN + ": 0",
                C4N + ": " + C4BS,
                C5N + ": " + C5BS,
                C6N + ": " + C6BS,
                C7N + ": " + C7BS,
                "",
                "number of classes depended on: 5",
                "",
                "total byte size: " + (C4BS + C5BS + C6BS + C7BS),
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_incl() {
        final String[] args = getArgs("-depsof " + C3N + " -incl");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "classes depended on and their byte size:",
                ObjectN + ": 0",
                C2N + ": " + C2BS,
                C3N + ": " + C3BS,
                C4N + ": " + C4BS,
                C5N + ": " + C5BS,
                C6N + ": " + C6BS,
                C7N + ": " + C7BS,
                "",
                "number of classes depended on: 7",
                "",
                "total byte size: " + (C2BS + C3BS + C4BS + + C5BS + C6BS + C7BS),
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_steps() {
        final String[] args = getArgs("-depsof " + C2N + " -steps");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "classes depended on and their byte size:",
                "",
                "step 0:",
                "",
                "step 1:",
                ObjectN + ": 0",
                C5N + ": " + C5BS,
                "",
                "step 2:",
                C4N + ": " + C4BS,
                C7N + ": " + C7BS,
                "",
                "step 3:",
                C6N + ": " + C6BS,
                "",
                "number of classes depended on: 5",
                "",
                "total byte size: " + (C4BS + C5BS + C6BS + C7BS),
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_packages() {
        final String[] args = getArgs("-depsof " + P1N + " -packages");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "packages depended on and their byte size:",
                JLN + ": 0",
                P2N + ": " + P2BS,
                "",
                "number of packages depended on: 2",
                "",
                "total byte size: " + P2BS,
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_packages_incl() {
        final String[] args = getArgs("-depsof " + P1N + " -packages" + " -incl");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "packages depended on and their byte size:",
                JLN + ": 0",
                P1N + ": " + P1BS,
                P2N + ": " + P2BS,
                "",
                "number of packages depended on: 3",
                "",
                "total byte size: " + (P1BS + P2BS),
        };
        checkEqual(expectedLines, defaultStream);
    }

    /*
     * Advanced computations (only testing with classes).
     */

    public void test_classes_minusof() {
        final String[] args = getArgs("-depsof " + C1N + " -minusof " + C4N);

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "classes depended on and their byte size:",
                C3N + ": " + C3BS,
                "",
                "number of classes depended on: 1",
                "",
                "total byte size: " + C3BS,
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_incl_minusof() {
        final String[] args = getArgs("-depsof " + C1N + " -incl" + " -minusof " + C4N);

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "classes depended on and their byte size:",
                C1N + ": " + C1BS,
                C3N + ": " + C3BS,
                "",
                "number of classes depended on: 2",
                "",
                "total byte size: " + (C1BS + C3BS),
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_into() {
        final String[] args = getArgs("-depsof " + C4N + " -into " + P2N + ".*");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "classes depended on and their byte size:",
                C5N + ": " + C5BS,
                C6N + ": " + C6BS,
                C7N + ": " + C7BS,
                "",
                "number of classes depended on: 3",
                "",
                "total byte size: " + (C5BS + C6BS + C7BS),
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_incl_into() {
        final String[] args = getArgs("-depsof " + C4N + " -incl"  + " -into " + P2N + ".*");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "classes depended on and their byte size:",
                C4N + ": " + C4BS,
                C5N + ": " + C5BS,
                C6N + ": " + C6BS,
                C7N + ": " + C7BS,
                "",
                "number of classes depended on: 4",
                "",
                "total byte size: " + (C4BS + C5BS + C6BS + C7BS),
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_minusof_into() {
        final String[] args = getArgs("-depsof " + C1N  + " -minusof " + C4N + " -into " + P2N + ".*");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "classes depended on and their byte size:",
                "",
                "number of classes depended on: 0",
                "",
                "total byte size: 0",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_incl_minusof_into() {
        final String[] args = getArgs("-depsof " + C1N + " -incl"  + " -minusof " + C4N + " -into " + P2N + ".*");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "classes depended on and their byte size:",
                C1N + ": " + C1BS,
                "",
                "number of classes depended on: 1",
                "",
                "total byte size: " + C1BS,
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_maxsteps_noOrHugeLimit() {
        for (int maxSteps : new int[]{-1,Integer.MAX_VALUE}) {
            final String[] args = getArgs("-depsof " + C2N + " -maxsteps " + maxSteps);

            final MemPrintStream defaultStream = new MemPrintStream();
            runArgsWithVirtualDeps(args, defaultStream);

            final String[] expectedLines = new String[]{
                    "args: " + Arrays.toString(args),
                    "",
                    "classes depended on and their byte size:",
                    ObjectN + ": 0",
                    C4N + ": " + C4BS,
                    C5N + ": " + C5BS,
                    C6N + ": " + C6BS,
                    C7N + ": " + C7BS,
                    "",
                    "number of classes depended on: 5",
                    "",
                    "total byte size: " + (C4BS + C5BS + C6BS + C7BS),
            };
            checkEqual(expectedLines, defaultStream);
        }
    }

    public void test_classes_maxsteps_0() {
        final String[] args = getArgs("-depsof " + C2N + " -maxsteps 0");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "classes depended on and their byte size:",
                "",
                "number of classes depended on: 0",
                "",
                "total byte size: 0",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_incl_maxsteps_0() {
        final String[] args = getArgs("-depsof " + C2N + " -incl" + " -maxsteps 0");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "classes depended on and their byte size:",
                C2N + ": " + C2BS,
                "",
                "number of classes depended on: 1",
                "",
                "total byte size: " + C2BS,
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_maxsteps_1() {
        final String[] args = getArgs("-depsof " + C2N + " -maxsteps 1");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "classes depended on and their byte size:",
                ObjectN + ": 0",
                C5N + ": " + C5BS,
                "",
                "number of classes depended on: 2",
                "",
                "total byte size: " + C5BS,
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_allCptOptions() {
        final String[] args = getArgs("-depsof " + C2N + " -incl" + " -into " + P1N + ".*" + " -minusof " + C7N + " -steps" + " -maxsteps -1");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "classes depended on and their byte size:",
                "",
                "step 0:",
                C2N + ": " + C2BS,
                "",
                "step 1:",
                "",
                "step 2:",
                "",
                "step 3:",
                "",
                "number of classes depended on: 1",
                "",
                "total byte size: " + C2BS,
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_packages_allCptOptions() {
        final String[] args = getArgs("-depsof " + P1N + " -packages" + " -incl" + " -into " + P1N + ".*" + " -minusof " + C7N + " -steps" + " -maxsteps -1");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "packages depended on and their byte size:",
                "",
                "step 0:",
                P1N + ": " + P1BS,
                "",
                "step 1:",
                "",
                "number of packages depended on: 1",
                "",
                "total byte size: " + P1BS,
        };
        checkEqual(expectedLines, defaultStream);
    }

    /*
     * Output options.
     */

    public void test_classes_nostats() {
        final String[] args = getArgs("-depsof " + C2N + " -nostats");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "classes depended on and their byte size:",
                ObjectN + ": 0",
                C4N + ": " + C4BS,
                C5N + ": " + C5BS,
                C6N + ": " + C6BS,
                C7N + ": " + C7BS,
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_packages_nostats() {
        final String[] args = getArgs("-depsof " + P1N + " -packages" + " -nostats");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "packages depended on and their byte size:",
                JLN + ": 0",
                P2N + ": " + P2BS,
        };
        checkEqual(expectedLines, defaultStream);
    }
    
    public void test_classes_onlystats() {
        for (boolean steps : new boolean[]{false,true}) {
            
            final String[] args;
            if (steps) {
                // -steps doesn't count here.
                args = getArgs("-depsof " + C2N + " -onlystats" + " -steps");
            } else {
                args = getArgs("-depsof " + C2N + " -onlystats");
            }

            final MemPrintStream defaultStream = new MemPrintStream();
            runArgsWithVirtualDeps(args, defaultStream);

            final String[] expectedLines = new String[]{
                    "args: " + Arrays.toString(args),
                    "",
                    "number of classes depended on: 5",
                    "",
                    "total byte size: " + (C4BS + C5BS + C6BS + C7BS),
            };
            checkEqual(expectedLines, defaultStream);
        }
    }

    public void test_packages_onlystats() {
        for (boolean steps : new boolean[]{false,true}) {
            
            final String[] args;
            if (steps) {
                // -steps doesn't count here.
                args = getArgs("-depsof " + P1N + " -packages" + " -onlystats" + " -steps");
            } else {
                args = getArgs("-depsof " + P1N + " -packages" + " -onlystats");
            }

            final MemPrintStream defaultStream = new MemPrintStream();
            runArgsWithVirtualDeps(args, defaultStream);

            final String[] expectedLines = new String[]{
                    "args: " + Arrays.toString(args),
                    "",
                    "number of packages depended on: 2",
                    "",
                    "total byte size: " + P2BS,
            };
            checkEqual(expectedLines, defaultStream);
        }
    }
}
