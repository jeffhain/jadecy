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
package net.jadecy.code;

import java.util.List;

import junit.framework.TestCase;
import net.jadecy.names.NameFilters;
import net.jadecy.tests.PrintTestUtils;
import net.jadecy.utils.MemPrintStream;

public class DerivedTreeComputerTest extends TestCase {

    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------

    private static final boolean DEBUG = false;

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    public void test_computeDerivedTree_exceptions() {

        final PackageData defaultP = new PackageData();

        try {
            final boolean mustReverseDeps = false;
            DerivedTreeComputer.computeDerivedTree(
                    defaultP,
                    mustReverseDeps,
                    null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            final boolean mustReverseDeps = false;
            DerivedTreeComputer.computeDerivedTree(
                    null,
                    mustReverseDeps,
                    NameFilters.any());
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        final PackageData p1 = defaultP.getOrCreatePackageData("p1");

        // Not default package.
        try {
            final boolean mustReverseDeps = false;
            DerivedTreeComputer.computeDerivedTree(
                    p1,
                    mustReverseDeps,
                    NameFilters.any());
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            // ok
        }
    }
    
    public void test_computeDerivedTree_stringReuse() {

        final PackageData defaultP = newInputTree();

        // Must reuse String instances even if reversing deps and doing filtering.
        final boolean mustReverseDeps = true;
        final PackageData derDefaultP = DerivedTreeComputer.computeDerivedTree(
                defaultP,
                mustReverseDeps,
                NameFilters.containsName("p1"));

        {
            final PackageData p1 = defaultP.getPackageData("p1");
            final PackageData der_p1 = derDefaultP.getPackageData("p1");
            assertSame(p1.fileNameNoExt(), der_p1.fileNameNoExt());
            assertSame(p1.name(), der_p1.name());
            assertSame(p1.displayName(), der_p1.displayName());
        }

        {
            final ClassData p1_bd = defaultP.getClassData("p1.b$d");
            final ClassData der_p1_bd = derDefaultP.getClassData("p1.b$d");
            assertSame(p1_bd.fileNameNoExt(), der_p1_bd.fileNameNoExt());
            assertSame(p1_bd.name(), der_p1_bd.name());
            assertSame(p1_bd.displayName(), der_p1_bd.displayName());
        }
    }

    public void test_computeDerivedTree_emptyPackage() {

        final PackageData defaultP = new PackageData();

        final PackageData emptyPackageData = defaultP.getOrCreatePackageData("boo");

        final boolean mustReverseDeps = false;
        final PackageData derDefaultP = DerivedTreeComputer.computeDerivedTree(
                defaultP,
                mustReverseDeps,
                NameFilters.any());

        // Not derived because is empty (and is not default package).
        assertEquals(null, derDefaultP.getPackageData(emptyPackageData.name()));

        final MemPrintStream stream = new MemPrintStream();

        derDefaultP.printSubtree(stream, true);

        final String[] expectedLines = new String[]{
                "[default package] (0 bytes)",
                " causeSetBySuccessor = {}",
                " causeSetByPredecessor = {}"
        };
        checkEqual(expectedLines, toStringTab(stream.getLines()));
    }

    public void test_computeDerivedTree_noneFilter() {

        final PackageData defaultP = new PackageData();

        defaultP.getOrCreatePackageData("c1");
        defaultP.getOrCreateClassData("c1");

        final boolean mustReverseDeps = false;
        final PackageData derDefaultP = DerivedTreeComputer.computeDerivedTree(
                defaultP,
                mustReverseDeps,
                NameFilters.none());

        final MemPrintStream stream = new MemPrintStream();

        derDefaultP.printSubtree(stream, true);

        final String[] expectedLines = new String[]{
                "[default package] (0 bytes)",
                " causeSetBySuccessor = {}",
                " causeSetByPredecessor = {}"
        };
        checkEqual(expectedLines, toStringTab(stream.getLines()));
    }

    public void test_computeDerivedTree_outerClass() {

        final PackageData defaultP = newInputTree();
        
        // Won't be derived, since "p1.b" is not retained.
        PackageData.ensureDependency(
                defaultP.getClassData("p1.b"),
                defaultP.getClassData("a"));
        
        final boolean mustReverseDeps = false;
        final PackageData derDefaultP = DerivedTreeComputer.computeDerivedTree(
                defaultP,
                mustReverseDeps,
                NameFilters.equalsName("p1.b$d"));
        
        final MemPrintStream stream = new MemPrintStream();

        derDefaultP.printSubtree(stream, true);

        // Outer class derived too, even if not retained by filter.
        final String[] expectedLines = new String[]{
                "[default package] (11 bytes)",
                " causeSetBySuccessor = {}",
                " causeSetByPredecessor = {p1=[a]}",
                " a (11 bytes: {a=11})",
                "p1 (30 bytes)",
                " causeSetBySuccessor = {[default package]=[a]}",
                " causeSetByPredecessor = {}",
                " p1.b (0 bytes: {})", // outer class of retained class
                " p1.b$d (30 bytes: {b$d=13, b$d$e=17})",
                "   -> a"
        };
        checkEqual(expectedLines, toStringTab(stream.getLines()));
    }

    public void test_computeDerivedTree_sameAsInput() {

        final PackageData defaultP = newInputTree();
        
        final boolean mustReverseDeps = false;
        final PackageData derDefaultP = DerivedTreeComputer.computeDerivedTree(
                defaultP,
                mustReverseDeps,
                NameFilters.any());
        
        @SuppressWarnings("unused")
        int debugCounter = 0;
        for (PackageData pd : new PackageData[]{defaultP,derDefaultP}) {

            debugCounter++;
            if (DEBUG) {
                System.out.println();
                System.out.println("debugCounter = " + debugCounter);
            }

            final MemPrintStream stream = new MemPrintStream();

            pd.printSubtree(stream, true);

            final String[] expectedLines = new String[]{
                    "[default package] (11 bytes)",
                    " causeSetBySuccessor = {p1=[a], p2=[b]}",
                    " causeSetByPredecessor = {p1=[a]}",
                    " a (11 bytes: {a=11})",
                    "   -> p1.b$d", // non-inverse (has opposite)
                    " b (0 bytes: {})",
                    "   -> p2.b", // non-inverse
                    " b$c (0 bytes: {})",
                    " b$d (0 bytes: {})",
                    " b$d$e (0 bytes: {})",
                    "p1 (30 bytes)",
                    " causeSetBySuccessor = {[default package]=[a], p2=[p1.a, p2.b]}",
                    " causeSetByPredecessor = {[default package]=[a]}",
                    " p1.a (0 bytes: {})",
                    "   -> p1.b", // non-inverse
                    "   -> p2.b", // inverse
                    " p1.b (0 bytes: {})",
                    " p1.b$c (0 bytes: {})",
                    " p1.b$d (30 bytes: {b$d=13, b$d$e=17})",
                    "   -> a", // inverse (has opposite)
                    " p1.b$d$e (0 bytes: {})",
                    "p2 (0 bytes)",
                    " causeSetBySuccessor = {}",
                    " causeSetByPredecessor = {[default package]=[b], p1=[p1.a, p2.b]}",
                    " p2.a (0 bytes: {})",
                    " p2.b (0 bytes: {})",
                    " p2.b$c (0 bytes: {})",
                    " p2.b$d (0 bytes: {})",
                    " p2.b$d$e (0 bytes: {})"
            };
            checkEqual(expectedLines, toStringTab(stream.getLines()));
        }
    }

    public void test_computeDerivedTree_reverseDeps() {

        final PackageData defaultP = newInputTree();

        final boolean mustReverseDeps = true;
        final PackageData derDefaultP = DerivedTreeComputer.computeDerivedTree(
                defaultP,
                mustReverseDeps,
                NameFilters.any());

        final MemPrintStream stream = new MemPrintStream();

        derDefaultP.printSubtree(stream, true);

        final String[] expectedLines = new String[]{
                "[default package] (11 bytes)",
                " causeSetBySuccessor = {p1=[a]}", // removed p2=[b]
                " causeSetByPredecessor = {p1=[a], p2=[b]}", // added p2=[b]
                " a (11 bytes: {a=11})",
                "   -> p1.b$d",
                " b (0 bytes: {})",
                //"   -> p2.b",
                " b$c (0 bytes: {})",
                " b$d (0 bytes: {})",
                " b$d$e (0 bytes: {})",
                "p1 (30 bytes)",
                " causeSetBySuccessor = {[default package]=[a]}", // removed p2=[p1.a, p2.b]
                " causeSetByPredecessor = {[default package]=[a], p2=[p1.a, p2.b]}", // added p2=[p1.a, p2.b]
                " p1.a (0 bytes: {})",
                //"   -> p1.b",
                //"   -> p2.b",
                " p1.b (0 bytes: {})",
                "   -> p1.a", // added
                " p1.b$c (0 bytes: {})",
                " p1.b$d (30 bytes: {b$d=13, b$d$e=17})",
                "   -> a",
                " p1.b$d$e (0 bytes: {})",
                "p2 (0 bytes)",
                " causeSetBySuccessor = {[default package]=[b], p1=[p1.a, p2.b]}", // added [default package]=[b], p1=[p1.a, p2.b]
                " causeSetByPredecessor = {}", // removed [default package]=[b], p1=[p1.a, p2.b]
                " p2.a (0 bytes: {})",
                " p2.b (0 bytes: {})",
                "   -> b", // added
                "   -> p1.a", // added
                " p2.b$c (0 bytes: {})",
                " p2.b$d (0 bytes: {})",
                " p2.b$d$e (0 bytes: {})"
        };
        checkEqual(expectedLines, toStringTab(stream.getLines()));
    }

    public void test_computeDerivedTree_filtering() {

        final PackageData defaultP = newInputTree();

        final boolean mustReverseDeps = false;
        final PackageData derDefaultP = DerivedTreeComputer.computeDerivedTree(
                defaultP,
                mustReverseDeps,
                NameFilters.containsName("p1"));

        final MemPrintStream stream = new MemPrintStream();

        derDefaultP.printSubtree(stream, true);

        final String[] expectedLines = new String[]{
                "[default package] (11 bytes)",
                " causeSetBySuccessor = {}", // removed p1=[a], p2=[b]
                " causeSetByPredecessor = {p1=[a]}",
                " a (11 bytes: {a=11})", // kept (but not deps from it) due to (inverse) dependency to it
                //"   -> p1.b$d",
                //" b (0 bytes: {})",
                //"   -> p2.b",
                //" b$c (0 bytes: {})",
                //" b$d (0 bytes: {})",
                //" b$d$e (0 bytes: {})",
                "p1 (30 bytes)",
                " causeSetBySuccessor = {[default package]=[a], p2=[p1.a, p2.b]}",
                " causeSetByPredecessor = {}", // removed [default package]=[a]
                " p1.a (0 bytes: {})",
                "   -> p1.b",
                "   -> p2.b",
                " p1.b (0 bytes: {})",
                " p1.b$c (0 bytes: {})",
                " p1.b$d (30 bytes: {b$d=13, b$d$e=17})",
                "   -> a",
                " p1.b$d$e (0 bytes: {})",
                "p2 (0 bytes)",
                " causeSetBySuccessor = {}",
                " causeSetByPredecessor = {p1=[p1.a, p2.b]}", // removed [default package]=[b]
                //" p2.a (0 bytes: {})",
                " p2.b (0 bytes: {})", // kept due to (inverse) dependency to it
                //" p2.b$c (0 bytes: {})",
                //" p2.b$d (0 bytes: {})",
                //" p2.b$d$e (0 bytes: {})"
        };
        checkEqual(expectedLines, toStringTab(stream.getLines()));
    }

    public void test_computeDerivedTree_reverseDeps_filtering() {

        final PackageData defaultP = newInputTree();

        final boolean mustReverseDeps = true;
        final PackageData derDefaultP = DerivedTreeComputer.computeDerivedTree(
                defaultP,
                mustReverseDeps,
                NameFilters.containsName("p1"));

        final MemPrintStream stream = new MemPrintStream();

        derDefaultP.printSubtree(stream, true);
        
        /*
         * Dependencies reversing is done first, and then filtering,
         * i.e. keeping retained classes and their outer classes
         * and successor classes.
         * Doing filtering first and then dependency inversion
         * would be less useful (for example when wanting to compute
         * what classes depend on a set of retained classes).
         */

        final String[] expectedLines = new String[]{
                "[default package] (11 bytes)",
                " causeSetBySuccessor = {}", // removed p1=[a], p2=[b]
                " causeSetByPredecessor = {p1=[a]}",
                " a (11 bytes: {a=11})",
                //"   -> p1.b$d",
                //" b (0 bytes: {})",
                //"   -> p2.b",
                //" b$c (0 bytes: {})",
                //" b$d (0 bytes: {})",
                //" b$d$e (0 bytes: {})",
                "p1 (30 bytes)",
                " causeSetBySuccessor = {[default package]=[a]}", // removed p2=[p1.a, p2.b]
                " causeSetByPredecessor = {}", // removed [default package]=[a]
                " p1.a (0 bytes: {})",
                //"   -> p1.b",
                //"   -> p2.b",
                " p1.b (0 bytes: {})",
                "   -> p1.a", // added
                " p1.b$c (0 bytes: {})",
                " p1.b$d (30 bytes: {b$d=13, b$d$e=17})",
                "   -> a",
                " p1.b$d$e (0 bytes: {})",
                //"p2 (0 bytes)",
                //" causeSetBySuccessor = {}",
                //" causeSetByPredecessor = {[default package]=[b], p1=[p1.a, p2.b]}",
                //" p2.a (0 bytes: {})",
                //" p2.b (0 bytes: {})",
                //" p2.b$c (0 bytes: {})",
                //" p2.b$d (0 bytes: {})",
                //" p2.b$d$e (0 bytes: {})"
        };
        checkEqual(expectedLines, toStringTab(stream.getLines()));
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------

    private static PackageData newInputTree() {

        final boolean mustUseSmallTree = true;
        final PackageData defaultP = CodeTestUtils.newNoDepTestTree(mustUseSmallTree);

        /*
         * (p1,p1) dependencies.
         */
        
        final ClassData p1_a = defaultP.getClassData("p1.a");
        final ClassData p1_b = defaultP.getClassData("p1.b");
        // Just a non-inverse dependency.
        PackageData.ensureDependency(p1_a, p1_b);
        
        /*
         * (default,p1) dependencies.
         */
        
        final ClassData a = defaultP.getClassData("a");
        final ClassData p1_bd = defaultP.getClassData("p1.b$d");
        // Mixing non-inverse and inverse dependencies,
        // even though it doesn't really make sense.
        // NB: Reversing that one doesn't change anything,
        // since both causes are "a".
        PackageData.ensureDependency(a, p1_bd);
        PackageData.ensureDependency(p1_bd, a, true);

        /*
         * (default,p2) dependencies.
         */
        
        final ClassData b = defaultP.getClassData("b");
        final ClassData p2_b = defaultP.getClassData("p2.b");
        // Just a non-inverse dependency.
        PackageData.ensureDependency(b, p2_b);
        
        /*
         * (p1,p2) dependencies.
         */

        // Non-inverse and inverse, but with different causes.
        PackageData.ensureDependency(p1_a, p2_b);
        PackageData.ensureDependency(p1_a, p2_b, true);
        
        /*
         * Some byte size.
         */

        PackageData.setByteSizeForClassOrNested(a, "a", 11);
        PackageData.setByteSizeForClassOrNested(p1_bd, "b$d", 13);
        PackageData.setByteSizeForClassOrNested(p1_bd, "b$d$e", 17);

        return defaultP;
    }
    
    /*
     * 
     */
    
    private static String[] toStringTab(List<String> lineList) {
        return PrintTestUtils.toStringTab(lineList);
    }
    
    private static void checkEqual(String[] expected, String[] actual) {
        PrintTestUtils.checkEqual(expected, actual);
    }
}
