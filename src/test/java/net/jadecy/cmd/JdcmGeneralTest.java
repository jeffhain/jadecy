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

import net.jadecy.code.NameUtils;
import net.jadecy.utils.MemPrintStream;

/**
 * Tests of non computation-specific behaviors.
 * -tofile is not tests, other than with sample script files.
 */
public class JdcmGeneralTest extends AbstractJdcmTezt {
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /*
     * Parse list.
     */
    
    public void test_parseList_p1() {

        final String[] parseList = new String[]{P1N};

        final String[] args = getArgs(parseList, "-depsof " + C1N);

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
                C5N + ": " + 0,
                C6N + ": " + 0,
                "",
                "number of classes depended on: 6",
                "",
                "total byte size: " + (C2BS + C3BS + C4BS + 0 + 0),
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_parseList_p1_p2() {

        final String[] parseList = new String[]{P1N, P2N};

        final String[] args = getArgs(parseList, "-depsof " + C1N);

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
                "total byte size: " + (C2BS + C3BS + C4BS + C5BS + C6BS + C7BS),
        };
        checkEqual(expectedLines, defaultStream);
    }
    
    /*
     * -regex.
     */

    public void test_regex() {
        final String[] args = getArgs("-regex " + NameUtils.toRegex(P1N) + ".* -depsof " + C1N);
        
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
                C5N + ": 0",
                C6N + ": 0",
                "",
                "number of classes depended on: 6",
                "",
                "total byte size: " + (C2BS + C3BS + C4BS + 0 + 0),
        };
        checkEqual(expectedLines, defaultStream);
    }
    
    /*
     * -nomerge and -apionly
     */
    
    public void test_merge_and_notApiOnly() {
        final String[] args = getArgs("-depsof " + C1N + " -maxsteps 2");
        
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
                "",
                "number of classes depended on: 5",
                "",
                "total byte size: " + (C2BS + C3BS + C4BS + C5BS),
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_merge_and_apiOnly() {
        final String[] args = getArgs("-depsof " + C1N + " -maxsteps 2" + " -apionly");
        
        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "classes depended on and their byte size:",
                ObjectN + ": 0",
                C3N + ": " + C3BS,
                C4N + ": " + C4BS,
                "",
                "number of classes depended on: 3",
                "",
                "total byte size: " + (C3BS + C4BS),
        };
        checkEqual(expectedLines, defaultStream);
    }
    
    public void test_noMerge_and_notApiOnly() {
        final String[] args = getArgs("-depsof " + C1N + " -maxsteps 2" + " -nomerge");
        
        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "classes depended on and their byte size:",
                ObjectN + ": 0",
                C1CXN + ": " + C1CXBS,
                C2N + ": " + C2BS,
                C3N + ": " + C3BS,
                C5N + ": " + C5BS,
                "",
                "number of classes depended on: 5",
                "",
                "total byte size: " + (C1CXBS + C2BS + C3BS + C5BS),
        };
        checkEqual(expectedLines, defaultStream);
    }
    
    public void test_noMerge_and_apiOnly() {
        final String[] args = getArgs("-depsof " + C1N + " -maxsteps 2" + " -nomerge" + " -apionly");
        
        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "classes depended on and their byte size:",
                ObjectN + ": 0",
                C1CXN + ": " + C1CXBS,
                C3N + ": " + C3BS,
                "",
                "number of classes depended on: 3",
                "",
                "total byte size: " + (C1CXBS + C3BS),
        };
        checkEqual(expectedLines, defaultStream);
    }
    
    /*
     * Compatibility checks.
     */
    
    public void test_noCause_and_classes() {
        final String[] args = getArgs("-spath " + C1N + " " + C2N + " -nocauses");
        
        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                C1N,
                C2N,
                "",
                "path length: 1",
        };
        checkEqual(expectedLines, defaultStream);
    }
    
    public void test_noCause_and_onlystats() {
        final String[] args = getArgs("-packages" + " -spath " + P1N + " " + P2N + " -nocauses" + " -onlystats");
        
        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "path length: 1",
        };
        checkEqual(expectedLines, defaultStream);
    }
    
    public void test_noCause_and_dotformat() {
        final String[] args = getArgs("-packages" + " -spath " + P1N + " " + P2N + " -nocauses" + " -dotformat");
        
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
    
    public void test_noStats_and_dotformat() {
        final String[] args = getArgs("-packages" + " -spath " + P1N + " " + P2N + " -nostats" + " -dotformat");
        
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
