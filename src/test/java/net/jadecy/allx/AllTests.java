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
package net.jadecy.allx;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import net.jadecy.DepUnitTest;
import net.jadecy.JadecyTest;
import net.jadecy.JadecyUtilsTest;
import net.jadecy.cmd.JdcmComp_CYCLES_Test;
import net.jadecy.cmd.JdcmComp_DEPSOF_Test;
import net.jadecy.cmd.JdcmComp_GDEPSTO_Test;
import net.jadecy.cmd.JdcmComp_SCYCLES_Test;
import net.jadecy.cmd.JdcmGeneralTest;
import net.jadecy.cmd.JdcmComp_PATHSG_Test;
import net.jadecy.cmd.JdcmComp_SCCS_Test;
import net.jadecy.cmd.JdcmComp_SPATH_Test;
import net.jadecy.cmd.JdcmComp_SOME_CYCLES_Test;
import net.jadecy.cmd.JdcmComp_GDEPSOF_Test;
import net.jadecy.cmd.JdcmComp_DEPSTO_Test;
import net.jadecy.cmd.JdcmErrorsTest;
import net.jadecy.code.AbstractCodeDataTest;
import net.jadecy.code.ClassDataTest;
import net.jadecy.code.DerivedTreeComputerTest;
import net.jadecy.code.CodeDataUtilsTest;
import net.jadecy.code.NameFiltersTest;
import net.jadecy.code.NameUtilsTest;
import net.jadecy.code.CodeDataSturdinessTest;
import net.jadecy.code.PackageDataTest;
import net.jadecy.graph.CyclesComputerTest;
import net.jadecy.graph.CyclesUtilsTest;
import net.jadecy.graph.OneShortestPathComputerTest;
import net.jadecy.graph.PathsGraphComputerTest;
import net.jadecy.graph.ReachabilityComputerTest;
import net.jadecy.graph.SccsComputerTest;
import net.jadecy.graph.ShortestCyclesComputerTest;
import net.jadecy.graph.SomeCyclesComputerTest;
import net.jadecy.graph.WorkGraphUtilzTest;
import net.jadecy.parsing.ClassDepsParserTest;
import net.jadecy.parsing.FsDepsParserTest;
import net.jadecy.utils.ArgsUtilsTest;
import net.jadecy.utils.ComparableArrayListTest;
import net.jadecy.utils.ComparableTreeMapTest;
import net.jadecy.utils.ComparableTreeSetTest;
import net.jadecy.utils.MemPrintStreamTest;
import net.jadecy.utils.QuietSortTest;
import net.jadecy.utils.SortUtilsTest;
import net.jadecy.utils.UniqStackTest;

public class AllTests {

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    public static void main(String [] args) {
        TestRunner.main(new String[]{"-c",AllTests.class.getName()});
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite("Test suite for " + AllTests.class.getPackage());

        /*
         * net.jadecy
         */
        
        suite.addTestSuite(DepUnitTest.class);
        suite.addTestSuite(JadecyTest.class);
        suite.addTestSuite(JadecyUtilsTest.class);
        
        /*
         * net.jadecy.allx
         */
        
        suite.addTestSuite(DepsTest.class);

        /*
         * net.jadecy.cmd
         */
        
        suite.addTestSuite(JdcmComp_CYCLES_Test.class);
        suite.addTestSuite(JdcmComp_DEPSOF_Test.class);
        suite.addTestSuite(JdcmComp_DEPSTO_Test.class);
        suite.addTestSuite(JdcmComp_GDEPSOF_Test.class);
        suite.addTestSuite(JdcmComp_GDEPSTO_Test.class);
        suite.addTestSuite(JdcmComp_PATHSG_Test.class);
        suite.addTestSuite(JdcmComp_SCCS_Test.class);
        suite.addTestSuite(JdcmComp_SCYCLES_Test.class);
        suite.addTestSuite(JdcmComp_SOME_CYCLES_Test.class);
        suite.addTestSuite(JdcmComp_SPATH_Test.class);
        suite.addTestSuite(JdcmErrorsTest.class);
        suite.addTestSuite(JdcmGeneralTest.class);
        
        /*
         * net.jadecy.code
         */
        
        suite.addTestSuite(AbstractCodeDataTest.class);
        suite.addTestSuite(ClassDataTest.class);
        suite.addTestSuite(CodeDataSturdinessTest.class);
        suite.addTestSuite(CodeDataUtilsTest.class);
        suite.addTestSuite(DerivedTreeComputerTest.class);
        suite.addTestSuite(NameFiltersTest.class);
        suite.addTestSuite(NameUtilsTest.class);
        suite.addTestSuite(PackageDataTest.class);
        
        /*
         * net.jadecy.graph
         */
        
        suite.addTestSuite(CyclesComputerTest.class);
        suite.addTestSuite(CyclesUtilsTest.class);
        suite.addTestSuite(OneShortestPathComputerTest.class);
        suite.addTestSuite(PathsGraphComputerTest.class);
        suite.addTestSuite(ReachabilityComputerTest.class);
        suite.addTestSuite(SccsComputerTest.class);
        suite.addTestSuite(ShortestCyclesComputerTest.class);
        suite.addTestSuite(SomeCyclesComputerTest.class);
        suite.addTestSuite(WorkGraphUtilzTest.class);

        /*
         * net.jadecy.parsing
         */
        
        suite.addTestSuite(ClassDepsParserTest.class);
        suite.addTestSuite(FsDepsParserTest.class);
        
        /*
         * net.jadecy.utils
         */
        
        suite.addTestSuite(ArgsUtilsTest.class);
        suite.addTestSuite(ComparableArrayListTest.class);
        suite.addTestSuite(ComparableTreeMapTest.class);
        suite.addTestSuite(ComparableTreeSetTest.class);
        suite.addTestSuite(MemPrintStreamTest.class);
        suite.addTestSuite(QuietSortTest.class);
        suite.addTestSuite(SortUtilsTest.class);
        suite.addTestSuite(UniqStackTest.class);
        
        return suite;
    }
}
