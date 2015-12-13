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
import java.util.SortedMap;
import java.util.SortedSet;

import net.jadecy.ElemType;
import net.jadecy.Jadecy;
import net.jadecy.JadecyUtils;
import net.jadecy.code.NameFilters;

class JdcmComp_PATHSG {

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    public static void runCommand(
            Jadecy jdc,
            JdcmCommand cmd,
            PrintStream stream) {

        final String elemBeginRegex = cmd.beginRegex;
        final String elemEndRegex = cmd.endRegex;

        /*
         * Computing.
         */
        
        final SortedMap<String,SortedMap<String,SortedSet<String>>> causesByDepByName =
                jdc.computePathsGraph(
                        cmd.elemType,
                        NameFilters.matches(elemBeginRegex),
                        NameFilters.matches(elemEndRegex),
                        cmd.maxSteps);

        /*
         * Printing result.
         */
        
        if (cmd.dotFormat) {
            final SortedMap<String,SortedSet<String>> depsByName =
                    JadecyUtils.computeGraphCauselessFromGraphMms(
                            causesByDepByName);
            JadecyUtils.printGraphInDOTFormatMs(
                    depsByName,
                    "allsteps",
                    stream);
        } else if (!cmd.onlyStats) {
            
            final boolean mustPrintCauses = !cmd.noCauses;
            
            final boolean haveCauses = (cmd.elemType == ElemType.PACKAGE) && mustPrintCauses;
            
            stream.println();
            stream.println("paths graph" + (haveCauses ? " with dependencies causes" : "") + ":");
            
            JadecyUtils.printGraphMms(
                    causesByDepByName,
                    mustPrintCauses,
                    stream);
        }
        
        /*
         * Printing stats.
         */
        
        if ((!cmd.dotFormat) && (!cmd.noStats)) {
            stream.println();
            stream.println("number of " + cmd.elemType.toStringPluralLC() + ": " + causesByDepByName.size());
        }
    }
}
