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

public class JdcmComp_SPATH_Test extends AbstractJdcmTezt {
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /*
     * Basic computations.
     */

    public void test_classes() {
        // One shortest path from C2 to C4.
        final String[] args = getArgs("-spath " + C2N + " " + C4N);

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                C2N,
                C5N,
                C4N,
                "",
                "path length: 2",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_packages() {
        // One shortest path from p1 to p2.
        final String[] args = getArgs("-spath " + P1N + " " + P2N + " -packages");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                P1N,
                "   " + C2N,
                "   " + C4N,
                P2N,
                "",
                "path length: 1",
        };
        checkEqual(expectedLines, defaultStream);
    }

    /*
     * Advanced computations (only testing with classes).
     */

    public void test_classes_noPath() {
        // No path.
        final String[] args = getArgs("-spath " + C2N + " nomatch");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "no path",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_toSelf() {
        // One shortest path from C2 to C2.
        final String[] args = getArgs("-spath " + C2N + " " + C2N);

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                C2N,
                "",
                "path length: 0",
        };
        checkEqual(expectedLines, defaultStream);
    }

    /*
     * Output options.
     */

    public void test_packages_nocauses() {
        // One shortest path from p1 to p2, without causes.
        final String[] args = getArgs("-spath " + P1N + " " + P2N + " -packages" + " -nocauses");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                P1N,
                P2N,
                "",
                "path length: 1",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_nostats() {
        // One shortest path from C2 to C4.
        final String[] args = getArgs("-spath " + C2N + " " + C4N + " -nostats");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                C2N,
                C5N,
                C4N,
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_packages_nostats() {
        // One shortest path from p1 to p2.
        final String[] args = getArgs("-spath " + P1N + " " + P2N + " -packages" + " -nostats");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                P1N,
                "   " + C2N,
                "   " + C4N,
                P2N,
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_onlystats() {
        // One shortest path from C2 to C4, only stats.
        final String[] args = getArgs("-spath " + C2N + " " + C4N + " -onlystats");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "path length: 2",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_packages_onlystats() {
        // One shortest path from p1 to p2, only stats.
        final String[] args = getArgs("-spath " + P1N + " " + P2N + " -packages" + " -onlystats");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "path length: 1",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_dotformat() {
        // One shortest path from C2 to C4, in DOT format.
        final String[] args = getArgs("-spath " + C2N + " " + C4N + " -dotformat");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "// args: " + Arrays.toString(args),
                "digraph " + q("allsteps") + " {",
                "   " + q(C2N) + " -> " + q(C5N) + ";",
                "   " + q(C4N) + ";",
                "   " + q(C5N) + " -> " + q(C4N) + ";",
                "}",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_packages_dotformat() {
        // One shortest path from p1 to p2, in DOT format.
        final String[] args = getArgs("-spath " + P1N + " " + P2N + " -packages" + " -dotformat");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "// args: " + Arrays.toString(args),
                "digraph " + q("allsteps") + " {",
                "   " + q(P1N) + " -> " + q(P2N) + ";",
                "   " + q(P2N) + ";",
                "}",
        };
        checkEqual(expectedLines, defaultStream);
    }
}
