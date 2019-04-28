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

import java.io.PrintStream;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;

import net.jadecy.Jadecy;
import net.jadecy.JadecyUtils;
import net.jadecy.names.NameFilters;

class JdcmComp_SPATH {

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    public static void runCommand(
            Jadecy jdc,
            JdcmCommand cmd,
            PrintStream stream) {

        /*
         * Computing.
         */

        final List<SortedMap<String,SortedSet<String>>> depCausesByNameList =
                jdc.computeOneShortestPath(
                        cmd.elemType,
                        NameFilters.matches(cmd.beginRegex),
                        NameFilters.matches(cmd.endRegex));

        /*
         * Printing result.
         */
        
        if (cmd.dotFormat) {
            // Edges will be ordered according to start name String natural ordering,
            // i.e. typically not in occurrence order in the path,
            // but since there are no duplicates the path can be deduced from the graph.
            
            final List<String> nameList =
                    JadecyUtils.computePathCauselessLms(depCausesByNameList);
            
            final SortedMap<String,SortedSet<String>> depsByName =
                    JadecyUtils.computeGraphFromPathL(nameList);
            
            JadecyUtils.printGraphInDOTFormatMs(
                    depsByName,
                    "allsteps",
                    stream);
        } else if (!cmd.onlyStats) {
            if (depCausesByNameList.size() != 0) {
                
                final boolean mustPrintCauses = !cmd.noCauses;
                
                stream.println();
                JadecyUtils.printPathLms(
                        depCausesByNameList,
                        mustPrintCauses,
                        stream);
            }
        }
        
        /*
         * Printing stats.
         */
        
        if ((!cmd.dotFormat) && (!cmd.noStats)) {
            if (depCausesByNameList.size() != 0) {
                stream.println();
                stream.println("path length: " + (depCausesByNameList.size()-1));
            } else {
                stream.println();
                stream.println("no path");
            }
        }
    }
}
