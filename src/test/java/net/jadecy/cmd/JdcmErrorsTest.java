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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import net.jadecy.utils.MemPrintStream;
import net.jadecy.virtual.AbstractVirtualCodeGraphTezt;

public class JdcmErrorsTest extends AbstractJdcmTezt {
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    public void test_unrecognizedOption() {
        final String[] args = getArgs("-depsof a -foo");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = withPringUsageAdded(
                new String[]{
                        "ERROR: unrecognized option: -foo",
                });
        checkEqual(expectedLines, defaultStream);
    }

    public void test_fileToParseNotFound() {
        final String badFilePath = AbstractVirtualCodeGraphTezt.NON_EXISTING_FILE_PATH;
        
        final String[] args = getArgs(
                new String[]{
                        badFilePath
                },
                "-depsof a");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = new String[]{
                "ERROR: file to parse not found: " + (new File(badFilePath)).getAbsolutePath(),
        };
        checkEqual(expectedLines, defaultStream);
    }

    public void test_nothingToParse() {
        final String[] args = getArgs(new String[0], "-depsof a");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = withPringUsageAdded(
                new String[]{
                        "ERROR: nothing to parse",
                });
        checkEqual(expectedLines, defaultStream);
    }

    public void test_nothingToCompute() {
        final String[] args = getArgs("");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = withPringUsageAdded(
                new String[]{
                        "ERROR: nothing to compute",
                });
        checkEqual(expectedLines, defaultStream);
    }

    public void test_nostatsAndOnlystats() {
        final String[] args = getArgs("-depsof a -nostats -onlystats");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = withPringUsageAdded(
                new String[]{
                        "ERROR: -nostats and -onlystats are incompatible",
                });
        checkEqual(expectedLines, defaultStream);
    }

    public void test_onlystatsAndDotformat() {
        final String[] args = getArgs("-depsof a -onlystats -dotformat");

        final MemPrintStream defaultStream = new MemPrintStream();
        runArgsWithVirtualDeps(args, defaultStream);

        final String[] expectedLines = withPringUsageAdded(
                new String[]{
                        "ERROR: -onlystats and -dotformat are incompatible",
                });
        checkEqual(expectedLines, defaultStream);
    }
    
    /*
     * 
     */

    public void test_incompatibilitiesOfInto() {
        test_incompatibilitiesOf(
                "into",
                JdcmCompType.DEPSOF,
                JdcmCompType.GDEPSOF);
    }

    public void test_incompatibilitiesOfFrom() {
        test_incompatibilitiesOf(
                "from",
                JdcmCompType.DEPSTO,
                JdcmCompType.GDEPSTO);
    }

    public void test_incompatibilitiesOfMinusof() {
        test_incompatibilitiesOf(
                "minusof",
                JdcmCompType.DEPSOF,
                JdcmCompType.GDEPSOF);
    }

    public void test_incompatibilitiesOfMinusto() {
        test_incompatibilitiesOf(
                "minusto",
                JdcmCompType.DEPSTO,
                JdcmCompType.GDEPSTO);
    }

    public void test_incompatibilitiesOfIncl() {
        test_incompatibilitiesOf(
                "incl",
                JdcmCompType.DEPSOF,
                JdcmCompType.DEPSTO,
                JdcmCompType.GDEPSOF,
                JdcmCompType.GDEPSTO);
    }

    public void test_incompatibilitiesOfSteps() {
        test_incompatibilitiesOf(
                "steps",
                JdcmCompType.DEPSOF,
                JdcmCompType.DEPSTO,
                JdcmCompType.GDEPSOF,
                JdcmCompType.GDEPSTO,
                JdcmCompType.PATHSG);
    }

    public void test_incompatibilitiesOfMaxsteps() {
        test_incompatibilitiesOf(
                "maxsteps",
                JdcmCompType.DEPSOF,
                JdcmCompType.DEPSTO,
                JdcmCompType.GDEPSOF,
                JdcmCompType.GDEPSTO,
                JdcmCompType.PATHSG);
    }

    public void test_incompatibilitiesOfMaxsize() {
        test_incompatibilitiesOf(
                "maxsize",
                JdcmCompType.SCCS,
                JdcmCompType.CYCLES,
                JdcmCompType.SOMECYCLES);
    }

    public void test_incompatibilitiesOfMaxcount() {
        test_incompatibilitiesOf(
                "maxcount",
                JdcmCompType.SCCS,
                JdcmCompType.CYCLES,
                JdcmCompType.SOMECYCLES);
    }

    public void test_incompatibilitiesOfNocause() {
        test_incompatibilitiesOf(
                "nocauses",
                JdcmCompType.GDEPSOF,
                JdcmCompType.GDEPSTO,
                JdcmCompType.SPATH,
                JdcmCompType.PATHSG,
                JdcmCompType.CYCLES,
                JdcmCompType.SOMECYCLES);
    }

    public void test_incompatibilitiesOfDotformat() {
        test_incompatibilitiesOf(
                "dotformat",
                JdcmCompType.GDEPSOF,
                JdcmCompType.GDEPSTO,
                JdcmCompType.SPATH,
                JdcmCompType.PATHSG,
                JdcmCompType.CYCLES,
                JdcmCompType.SOMECYCLES);
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------

    private void test_incompatibilitiesOf(String option, JdcmCompType... compTypeArr) {
        for (JdcmCompType badType : newComplementary(compTypeArr)) {
            
            final String compTypeString = badType.toString().toLowerCase();
            
            // "1" so that works if expecting a number.
            final String[] args = getArgs("-" + option + " 1 -" + compTypeString + " a b");

            final MemPrintStream defaultStream = new MemPrintStream();
            runArgsWithVirtualDeps(args, defaultStream);

            final String[] expectedLines = withPringUsageAdded(
                    new String[]{
                            "ERROR: -" + option + " is incompatible with -" + compTypeString,
                    });
            checkEqual(expectedLines, defaultStream);
        }
    }
    
    /**
     * @return A new array containing all computation types different
     *         from the specified ones.
     */
    private static JdcmCompType[] newComplementary(JdcmCompType... compTypeArr) {
        final HashSet<JdcmCompType> set = new HashSet<JdcmCompType>();
        Collections.addAll(set, compTypeArr);
        
        final ArrayList<JdcmCompType> complementaryList = new ArrayList<JdcmCompType>();
        for (JdcmCompType value : JdcmCompType.valuesList()) {
            if (!set.contains(value)) {
                complementaryList.add(value);
            }
        }
        return complementaryList.toArray(new JdcmCompType[complementaryList.size()]);
    }
}
