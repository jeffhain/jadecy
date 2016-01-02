/*
 * Copyright 2015-2016 Jeff Hain
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

public class JdcmComp_SCCS_Test extends AbstractJdcmTezt {

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    public JdcmComp_SCCS_Test() {
        // With p3, to have two SCCs.
        super(true);
    }
    
    /*
     * Basic computations.
     */

    public void test_classes() {
        final String[] args = getArgs("-sccs");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);
        
        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "SCC 1 (" + (C8BS + C9BS) + " bytes):",
                C8N + ": " + C8BS,
                C9N + ": " + C9BS,
                "",
                "SCC 2 (" + (C2BS + C4BS + C5BS + C6BS + C7BS) + " bytes):",
                C2N + ": " + C2BS,
                C4N + ": " + C4BS,
                C5N + ": " + C5BS,
                C6N + ": " + C6BS,
                C7N + ": " + C7BS,
                "",
                "number of SCCs by size (number of classes):",
                "2 : 1",
                "5 : 1",
                "",
                "number of SCCs found: 2",
                "",
                "total byte size: " + (C2BS + C4BS + C5BS + C6BS + C7BS + C8BS + C9BS),
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_packages() {
        final String[] args = getArgs("-sccs" + " -packages");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);
        
        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "SCC 1 (" + (P1BS + P2BS) + " bytes):",
                P1N + ": " + P1BS,
                P2N + ": " + P2BS,
                "",
                "number of SCCs by size (number of packages):",
                "2 : 1",
                "",
                "number of SCCs found: 1",
                "",
                "total byte size: " + (P1BS + P2BS),
        };
        checkEqual(expectedLines, defaultStream);
    }

    /*
     * Advanced computations (only testing with classes).
     */

    public void test_classes_minsize() {
        for (int minSize = -1; minSize <= 6; minSize++) {
            final String[] args = getArgs("-sccs" + " -minsize " + minSize);

            final MemPrintStream defaultStream = new MemPrintStream();
            runArgsWithVirtualDeps(args, defaultStream);

            final String[] expectedLines;
            if (minSize <= 2) {
                expectedLines = new String[]{
                        "args: " + Arrays.toString(args),
                        "",
                        "SCC 1 (" + (C8BS + C9BS) + " bytes):",
                        C8N + ": " + C8BS,
                        C9N + ": " + C9BS,
                        "",
                        "SCC 2 (" + (C2BS + C4BS + C5BS + C6BS + C7BS) + " bytes):",
                        C2N + ": " + C2BS,
                        C4N + ": " + C4BS,
                        C5N + ": " + C5BS,
                        C6N + ": " + C6BS,
                        C7N + ": " + C7BS,
                        "",
                        "number of SCCs by size (number of classes):",
                        "2 : 1",
                        "5 : 1",
                        "",
                        "number of SCCs found: 2",
                        "",
                        "total byte size: " + (C2BS + C4BS + C5BS + C6BS + C7BS + C8BS + C9BS),
                };
            } else if (minSize <= 5) {
                expectedLines = new String[]{
                        "args: " + Arrays.toString(args),
                        "",
                        "SCC 1 (" + (C2BS + C4BS + C5BS + C6BS + C7BS) + " bytes):",
                        C2N + ": " + C2BS,
                        C4N + ": " + C4BS,
                        C5N + ": " + C5BS,
                        C6N + ": " + C6BS,
                        C7N + ": " + C7BS,
                        "",
                        "number of SCCs by size (number of classes):",
                        "5 : 1",
                        "",
                        "number of SCCs found: 1",
                        "",
                        "total byte size: " + (C2BS + C4BS + C5BS + C6BS + C7BS),
                };
            } else {
                expectedLines = new String[]{
                        "args: " + Arrays.toString(args),
                        "",
                        "number of SCCs by size (number of classes):",
                        "",
                        "number of SCCs found: 0",
                        "",
                        "total byte size: 0",
                };
            }
            checkEqual(expectedLines, defaultStream);
        }
    }
    
    public void test_classes_minsize_maxsize() {
        for (int minSize = -1; minSize <= 6; minSize++) {
            for (int maxSize = -1; maxSize <= 6; maxSize++) {
                final String[] args = getArgs("-sccs" + " -minsize " + minSize + " -maxsize " + maxSize);

                final MemPrintStream defaultStream = new MemPrintStream();
                runArgsWithVirtualDeps(args, defaultStream);

                final String[] expectedLines;
                if ((minSize <= 2) && ((maxSize < 0) || (maxSize >= 5))) {
                    expectedLines = new String[]{
                            "args: " + Arrays.toString(args),
                            "",
                            "SCC 1 (" + (C8BS + C9BS) + " bytes):",
                            C8N + ": " + C8BS,
                            C9N + ": " + C9BS,
                            "",
                            "SCC 2 (" + (C2BS + C4BS + C5BS + C6BS + C7BS) + " bytes):",
                            C2N + ": " + C2BS,
                            C4N + ": " + C4BS,
                            C5N + ": " + C5BS,
                            C6N + ": " + C6BS,
                            C7N + ": " + C7BS,
                            "",
                            "number of SCCs by size (number of classes):",
                            "2 : 1",
                            "5 : 1",
                            "",
                            "number of SCCs found: 2",
                            "",
                            "total byte size: " + (C2BS + C4BS + C5BS + C6BS + C7BS + C8BS + C9BS),
                    };
                } else if ((minSize <= 2) && ((maxSize >= 2) && (maxSize < 5))) {
                    expectedLines = new String[]{
                            "args: " + Arrays.toString(args),
                            "",
                            "SCC 1 (" + (C8BS + C9BS) + " bytes):",
                            C8N + ": " + C8BS,
                            C9N + ": " + C9BS,
                            "",
                            "number of SCCs by size (number of classes):",
                            "2 : 1",
                            "",
                            "number of SCCs found: 1",
                            "",
                            "total byte size: " + (C8BS + C9BS),
                    };
                } else if ((minSize <= 5) && ((maxSize < 0) || (maxSize >= 5))) {
                    expectedLines = new String[]{
                            "args: " + Arrays.toString(args),
                            "",
                            "SCC 1 (" + (C2BS + C4BS + C5BS + C6BS + C7BS) + " bytes):",
                            C2N + ": " + C2BS,
                            C4N + ": " + C4BS,
                            C5N + ": " + C5BS,
                            C6N + ": " + C6BS,
                            C7N + ": " + C7BS,
                            "",
                            "number of SCCs by size (number of classes):",
                            "5 : 1",
                            "",
                            "number of SCCs found: 1",
                            "",
                            "total byte size: " + (C2BS + C4BS + C5BS + C6BS + C7BS),
                    };
                } else {
                    expectedLines = new String[]{
                            "args: " + Arrays.toString(args),
                            "",
                            "number of SCCs by size (number of classes):",
                            "",
                            "number of SCCs found: 0",
                            "",
                            "total byte size: 0",
                    };
                }
                checkEqual(expectedLines, defaultStream);
            }
        }
    }
    
    public void test_classes_maxsize_noOrHugeLimit() {
        for (int maxSize : new int[]{-1,Integer.MAX_VALUE}) {
            final String[] args = getArgs("-sccs" + " -maxsize " + maxSize);

            final MemPrintStream defaultStream = new MemPrintStream();
            runArgsWithVirtualDeps(args, defaultStream);

            final String[] expectedLines = new String[]{
                    "args: " + Arrays.toString(args),
                    "",
                    "SCC 1 (" + (C8BS + C9BS) + " bytes):",
                    C8N + ": " + C8BS,
                    C9N + ": " + C9BS,
                    "",
                    "SCC 2 (" + (C2BS + C4BS + C5BS + C6BS + C7BS) + " bytes):",
                    C2N + ": " + C2BS,
                    C4N + ": " + C4BS,
                    C5N + ": " + C5BS,
                    C6N + ": " + C6BS,
                    C7N + ": " + C7BS,
                    "",
                    "number of SCCs by size (number of classes):",
                    "2 : 1",
                    "5 : 1",
                    "",
                    "number of SCCs found: 2",
                    "",
                    "total byte size: " + (C2BS + C4BS + C5BS + C6BS + C7BS + C8BS + C9BS),
            };
            checkEqual(expectedLines, defaultStream);
        }
    }

    public void test_classes_maxsize_none() {
        for (int maxSize : new int[]{0,1}) {
            final String[] args = getArgs("-sccs" + " -maxsize " + maxSize);

            final MemPrintStream defaultStream = new MemPrintStream();
            runArgsWithVirtualDeps(args, defaultStream);

            final String[] expectedLines = new String[]{
                    "args: " + Arrays.toString(args),
                    "",
                    "number of SCCs by size (number of classes):",
                    "",
                    "number of SCCs found: 0",
                    "",
                    "total byte size: 0",
            };
            checkEqual(expectedLines, defaultStream);
        }
    }

    public void test_classes_maxsize_one() {
        for (int maxSize : new int[]{2,4}) {
            final String[] args = getArgs("-sccs" + " -maxsize " + maxSize);

            final MemPrintStream defaultStream = new MemPrintStream();
            runArgsWithVirtualDeps(args, defaultStream);

            final String[] expectedLines = new String[]{
                    "args: " + Arrays.toString(args),
                    "",
                    "SCC 1 (" + (C8BS + C9BS) + " bytes):",
                    C8N + ": " + C8BS,
                    C9N + ": " + C9BS,
                    "",
                    "number of SCCs by size (number of classes):",
                    "2 : 1",
                    "",
                    "number of SCCs found: 1",
                    "",
                    "total byte size: " + (C8BS + C9BS),
            };
            checkEqual(expectedLines, defaultStream);
        }
    }

    public void test_classes_maxcount_noOrHugeLimit() {
        for (long maxCount : new long[]{-1L,Long.MAX_VALUE}) {
            final String[] args = getArgs("-sccs" + " -maxcount " + maxCount);

            final MemPrintStream defaultStream = new MemPrintStream();
            runArgsWithVirtualDeps(args, defaultStream);

            final String[] expectedLines = new String[]{
                    "args: " + Arrays.toString(args),
                    "",
                    "SCC 1 (" + (C8BS + C9BS) + " bytes):",
                    C8N + ": " + C8BS,
                    C9N + ": " + C9BS,
                    "",
                    "SCC 2 (" + (C2BS + C4BS + C5BS + C6BS + C7BS) + " bytes):",
                    C2N + ": " + C2BS,
                    C4N + ": " + C4BS,
                    C5N + ": " + C5BS,
                    C6N + ": " + C6BS,
                    C7N + ": " + C7BS,
                    "",
                    "number of SCCs by size (number of classes):",
                    "2 : 1",
                    "5 : 1",
                    "",
                    "number of SCCs found: 2",
                    "",
                    "total byte size: " + (C2BS + C4BS + C5BS + C6BS + C7BS + C8BS + C9BS),
            };
            checkEqual(expectedLines, defaultStream);
        }
    }

    public void test_classes_maxcount_0() {
        final String[] args = getArgs("-sccs" + " -maxcount 0");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);
        
        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "number of SCCs by size (number of classes):",
                "",
                "number of SCCs found: 0",
                "",
                "total byte size: 0",
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_maxcount_1() {
        final String[] args = getArgs("-sccs" + " -maxcount 1");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);
        
        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "SCC 1 (" + (C8BS + C9BS) + " bytes):",
                C8N + ": " + C8BS,
                C9N + ": " + C9BS,
                "",
                "number of SCCs by size (number of classes):",
                "2 : 1",
                "",
                "number of SCCs found: 1",
                "",
                "total byte size: " + (C8BS + C9BS),
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_classes_maxsize_maxcount_noneOrOne() {
        for (int maxSize : new int[]{0,1,2}) {
            for (int maxCount : new int[]{0,1}) {
                final String[] args = getArgs("-sccs" + " -maxsize " + maxSize + " -maxcount " + maxCount);

                final MemPrintStream defaultStream = new MemPrintStream();
                runArgsWithVirtualDeps(args, defaultStream);
                
                final String[] expectedLines;
                if ((maxSize == 2) && (maxCount == 1)) {
                    expectedLines = new String[]{
                            "args: " + Arrays.toString(args),
                            "",
                            "SCC 1 (" + (C8BS + C9BS) + " bytes):",
                            C8N + ": " + C8BS,
                            C9N + ": " + C9BS,
                            "",
                            "number of SCCs by size (number of classes):",
                            "2 : 1",
                            "",
                            "number of SCCs found: 1",
                            "",
                            "total byte size: " + (C8BS + C9BS),
                    };
                } else {
                    expectedLines = new String[]{
                            "args: " + Arrays.toString(args),
                            "",
                            "number of SCCs by size (number of classes):",
                            "",
                            "number of SCCs found: 0",
                            "",
                            "total byte size: 0",
                    };
                }
                checkEqual(expectedLines, defaultStream);
            }
        }
    }

    /*
     * Output options.
     */

    public void test_classes_nostats() {
        final String[] args = getArgs("-sccs" + " -nostats");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);
        
        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "SCC 1 (" + (C8BS + C9BS) + " bytes):",
                C8N + ": " + C8BS,
                C9N + ": " + C9BS,
                "",
                "SCC 2 (" + (C2BS + C4BS + C5BS + C6BS + C7BS) + " bytes):",
                C2N + ": " + C2BS,
                C4N + ": " + C4BS,
                C5N + ": " + C5BS,
                C6N + ": " + C6BS,
                C7N + ": " + C7BS,
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_packages_nostats() {
        final String[] args = getArgs("-sccs" + " -packages" + " -nostats");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);
        
        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "SCC 1 (" + (P1BS + P2BS) + " bytes):",
                P1N + ": " + P1BS,
                P2N + ": " + P2BS,
        };
        checkEqual(expectedLines, defaultStream);
    }
    
    public void test_classes_onlystats() {
        final String[] args = getArgs("-sccs" + " -onlystats");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);
        
        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "number of SCCs by size (number of classes):",
                "2 : 1",
                "5 : 1",
                "",
                "number of SCCs found: 2",
                "",
                "total byte size: " + (C2BS + C4BS + C5BS + C6BS + C7BS + C8BS + C9BS),
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_packages_onlystats() {
        final String[] args = getArgs("-sccs" + " -packages" + " -onlystats");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);
        
        final String[] expectedLines = new String[]{
                "args: " + Arrays.toString(args),
                "",
                "number of SCCs by size (number of packages):",
                "2 : 1",
                "",
                "number of SCCs found: 1",
                "",
                "total byte size: " + (P1BS + P2BS),
        };
        checkEqual(expectedLines, defaultStream);
    }
}
