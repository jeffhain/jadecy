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

public class JdcmComp_DEPSTO_Test extends AbstractJdcmTezt {

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /*
     * Basic computations.
     */

    public void test_classes() {
        final String[] args = getArgs("-depsto " + C4N);

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending classes and their byte size:",
                C1N + ": " + C1BS,
                C2N + ": " + C2BS,
                C3N + ": " + C3BS,
                C5N + ": " + C5BS,
                C6N + ": " + C6BS,
                C7N + ": " + C7BS,
                "",
                "number of depending classes: 6",
                "",
                "total byte size: " + (C1BS + C2BS + C3BS + C5BS + C6BS + C7BS),
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_incl() {
        final String[] args = getArgs("-depsto " + C4N + " -incl");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending classes and their byte size:",
                C1N + ": " + C1BS,
                C2N + ": " + C2BS,
                C3N + ": " + C3BS,
                C4N + ": " + C4BS,
                C5N + ": " + C5BS,
                C6N + ": " + C6BS,
                C7N + ": " + C7BS,
                "",
                "number of depending classes: 7",
                "",
                "total byte size: " + (C1BS + C2BS + C3BS + C4BS + C5BS + C6BS + C7BS),
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_steps() {
        final String[] args = getArgs("-depsto " + C4N + " -steps");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending classes and their byte size:",
                "",
                "step 0:",
                "",
                "step 1:",
                C3N + ": " + C3BS,
                C5N + ": " + C5BS,
                C6N + ": " + C6BS,
                "",
                "step 2:",
                C1N + ": " + C1BS,
                C2N + ": " + C2BS,
                C7N + ": " + C7BS,
                "",
                "number of depending classes: 6",
                "",
                "total byte size: " + (C1BS + C2BS + C3BS + C5BS + C6BS + C7BS),
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_packages() {
        final String[] args = getArgs("-depsto " + P2N + " -packages");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending packages and their byte size:",
                P1N + ": " + P1BS,
                "",
                "number of depending packages: 1",
                "",
                "total byte size: " + P1BS,
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_packages_incl() {
        final String[] args = getArgs("-depsto " + P2N + " -packages" + " -incl");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending packages and their byte size:",
                P1N + ": " + P1BS,
                P2N + ": " + P2BS,
                "",
                "number of depending packages: 2",
                "",
                "total byte size: " + (P1BS + P2BS),
        };
        checkEqual(expectedLines, defaultStream);
    }

    /*
     * Advanced computations (only testing with classes).
     */

    public void test_classes_minusto() {
        final String[] args = getArgs("-depsto " + C4N + " -minusto " + C3N);

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending classes and their byte size:",
                C2N + ": " + C2BS,
                C5N + ": " + C5BS,
                C6N + ": " + C6BS,
                C7N + ": " + C7BS,
                "",
                "number of depending classes: 4",
                "",
                "total byte size: " + (C2BS + C5BS + C6BS + C7BS),
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_incl_minusto() {
        final String[] args = getArgs("-depsto " + C4N + " -incl" + " -minusto " + C3N);

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending classes and their byte size:",
                C2N + ": " + C2BS,
                C4N + ": " + C4BS,
                C5N + ": " + C5BS,
                C6N + ": " + C6BS,
                C7N + ": " + C7BS,
                "",
                "number of depending classes: 5",
                "",
                "total byte size: " + (C2BS + C4BS + C5BS + C6BS + C7BS),
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_from() {
        final String[] args = getArgs("-depsto " + C4N + " -from " + P2N + ".*");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending classes and their byte size:",
                C5N + ": " + C5BS,
                C6N + ": " + C6BS,
                C7N + ": " + C7BS,
                "",
                "number of depending classes: 3",
                "",
                "total byte size: " + (C5BS + C6BS + C7BS),
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_incl_from() {
        final String[] args = getArgs("-depsto " + C4N + " -incl" + " -from " + P2N + ".*");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending classes and their byte size:",
                C4N + ": " + C4BS,
                C5N + ": " + C5BS,
                C6N + ": " + C6BS,
                C7N + ": " + C7BS,
                "",
                "number of depending classes: 4",
                "",
                "total byte size: " + (C4BS + C5BS + C6BS + C7BS),
        };
        checkEqual(expectedLines, defaultStream);
    }
    
    public void test_classes_minusto_from() {
        final String[] args = getArgs("-depsto " + C4N  + " -minusto " + C3N + " -from " + P2N + ".*");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending classes and their byte size:",
                C5N + ": " + C5BS,
                C6N + ": " + C6BS,
                C7N + ": " + C7BS,
                "",
                "number of depending classes: 3",
                "",
                "total byte size: " + (C5BS + C6BS + C7BS),
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_incl_minusto_from() {
        final String[] args = getArgs("-depsto " + C4N + " -incl"  + " -minusto " + C3N + " -from " + P2N + ".*");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending classes and their byte size:",
                C4N + ": " + C4BS,
                C5N + ": " + C5BS,
                C6N + ": " + C6BS,
                C7N + ": " + C7BS,
                "",
                "number of depending classes: 4",
                "",
                "total byte size: " + (C4BS + C5BS + C6BS + C7BS),
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_maxsteps_noOrHugeLimit() {
        for (int maxSteps : new int[]{-1,Integer.MAX_VALUE}) {
            final String[] args = getArgs("-depsto " + C4N + " -maxsteps " + maxSteps);

            final MemPrintStream defaultStream = new MemPrintStream();
            runArgsWithVirtualDeps(args, defaultStream);

            final String[] expectedLines = new String[]{
                    "args: " + Arrays.toString(args),
                    "",
                    "depending classes and their byte size:",
                    C1N + ": " + C1BS,
                    C2N + ": " + C2BS,
                    C3N + ": " + C3BS,
                    C5N + ": " + C5BS,
                    C6N + ": " + C6BS,
                    C7N + ": " + C7BS,
                    "",
                    "number of depending classes: 6",
                    "",
                    "total byte size: " + (C1BS + C2BS + C3BS + C5BS + C6BS + C7BS),
            };
            checkEqual(expectedLines, defaultStream);
        }
    }

    public void test_classes_maxsteps_0() {
        final String[] args = getArgs("-depsto " + C4N + " -maxsteps 0");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending classes and their byte size:",
                "",
                "number of depending classes: 0",
                "",
                "total byte size: 0",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_incl_maxsteps_0() {
        final String[] args = getArgs("-depsto " + C4N + " -incl" + " -maxsteps 0");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending classes and their byte size:",
                C4N + ": " + C4BS,
                "",
                "number of depending classes: 1",
                "",
                "total byte size: " + C4BS,
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_maxsteps_1() {
        final String[] args = getArgs("-depsto " + C4N + " -maxsteps 1");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending classes and their byte size:",
                C3N + ": " + C3BS,
                C5N + ": " + C5BS,
                C6N + ": " + C6BS,
                "",
                "number of depending classes: 3",
                "",
                "total byte size: " + (C3BS + C5BS + C6BS),
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_allCptOptions() {
        final String[] args = getArgs("-depsto " + C4N + " -incl" + " -from " + P1N + ".*" + " -minusto " + C1N + " -steps" + " -maxsteps -1");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending classes and their byte size:",
                "",
                "step 0:",
                C4N + ": " + C4BS,
                "",
                "step 1:",
                C3N + ": " + C3BS,
                "",
                "step 2:",
                C2N + ": " + C2BS,
                "",
                "number of depending classes: 3",
                "",
                "total byte size: " + (C4BS + C3BS + C2BS),
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_packages_allCptOptions() {
        final String[] args = getArgs("-depsto " + P2N + " -packages" + " -incl" + " -from " + P1N + ".*" + " -minusto " + C1N + " -steps" + " -maxsteps -1");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending packages and their byte size:",
                "",
                "step 0:",
                P2N + ": " + P2BS,
                "",
                "step 1:",
                P1N + ": " + P1BS,
                "",
                "number of depending packages: 2",
                "",
                "total byte size: " + (P2BS + P1BS),
        };
        checkEqual(expectedLines, defaultStream);
    }

    /*
     * Output options.
     */

    public void test_classes_nostats() {
        final String[] args = getArgs("-depsto " + C4N + " -nostats");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending classes and their byte size:",
                C1N + ": " + C1BS,
                C2N + ": " + C2BS,
                C3N + ": " + C3BS,
                C5N + ": " + C5BS,
                C6N + ": " + C6BS,
                C7N + ": " + C7BS,
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_packages_nostats() {
        final String[] args = getArgs("-depsto " + P2N + " -packages" + " -nostats");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "depending packages and their byte size:",
                P1N + ": " + P1BS,
        };
        checkEqual(expectedLines, defaultStream);
    }
    
    public void test_classes_onlystats() {
        for (boolean steps : new boolean[]{false,true}) {
            
            final String[] args;
            if (steps) {
                // -steps doesn't count here.
                args = getArgs("-depsto " + C4N + " -onlystats" + " -steps");
            } else {
                args = getArgs("-depsto " + C4N + " -onlystats");
            }

            final MemPrintStream defaultStream = new MemPrintStream();
            runArgsWithVirtualDeps(args, defaultStream);

            final String[] expectedLines = new String[]{
                    "args: " + Arrays.toString(args),
                    "",
                    "number of depending classes: 6",
                    "",
                    "total byte size: " + (C1BS + C2BS + C3BS + C5BS + C6BS + C7BS),
            };
            checkEqual(expectedLines, defaultStream);
        }
    }

    public void test_packages_onlystats() {
        for (boolean steps : new boolean[]{false,true}) {
            
            final String[] args;
            if (steps) {
                // -steps doesn't count here.
                args = getArgs("-depsto " + P2N + " -packages" + " -onlystats" + " -steps");
            } else {
                args = getArgs("-depsto " + P2N + " -packages" + " -onlystats");
            }

            final MemPrintStream defaultStream = new MemPrintStream();
            runArgsWithVirtualDeps(args, defaultStream);

            final String[] expectedLines = new String[]{
                    "args: " + Arrays.toString(args),
                    "",
                    "number of depending packages: 1",
                    "",
                    "total byte size: " + P1BS,
            };
            checkEqual(expectedLines, defaultStream);
        }
    }
}
