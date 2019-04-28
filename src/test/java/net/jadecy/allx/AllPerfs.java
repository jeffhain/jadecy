/*
 * Copyright 2015-2019 Jeff Hain
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

import net.jadecy.code.DerivedTreeComputerPerf;
import net.jadecy.code.CodeDataUtilsPerf;
import net.jadecy.graph.CyclesComputersPerf;
import net.jadecy.graph.OneShortestPathComputerPerf;
import net.jadecy.graph.ReachabilityComputerPerf;
import net.jadecy.graph.SccsComputerPerf;
import net.jadecy.names.NameFiltersPerf;
import net.jadecy.names.NameUtilsPerf;
import net.jadecy.parsing.FsDepsParserPerf;
import net.jadecy.utils.QuietSortPerf;

public class AllPerfs {

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    public static void main(String[] args) {
        
        /*
         * net.jadecy.code
         */
        
        CodeDataUtilsPerf.newRun(args);
        DerivedTreeComputerPerf.newRun(args);
        
        /*
         * net.jadecy.graph
         */
        
        CyclesComputersPerf.newRun(args);
        OneShortestPathComputerPerf.newRun(args);
        ReachabilityComputerPerf.newRun(args);
        SccsComputerPerf.newRun(args);
        
        /*
         * net.jadecy.names
         */
        
        NameFiltersPerf.newRun(args);
        NameUtilsPerf.newRun(args);
        
        /*
         * net.jadecy.parsing
         */
        
        FsDepsParserPerf.newRun(args);
        
        /*
         * net.jadecy.utils
         */
        
        QuietSortPerf.newRun(args);
    }
}
