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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import net.jadecy.ElemType;
import net.jadecy.Jadecy;
import net.jadecy.JadecyUtils;
import net.jadecy.code.NameFilters;

class JdcmComp_GDEPSOF_GDEPSTO {

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    public static void runCommand(
            Jadecy jdc,
            JdcmCommand cmd,
            PrintStream stream) {

        final JdcmCompType computation = cmd.compType;

        final boolean mustUseInverseDeps = computation.usesInverseDeps();

        final String elemBeginRegex;
        final String elemEndRegex;
        final String minusRegex;
        if (computation == JdcmCompType.GDEPSOF) {
            elemBeginRegex = cmd.ofRegex;
            elemEndRegex = cmd.intoRegex;
            minusRegex = cmd.minusOfRegex;
        } else {
            elemBeginRegex = cmd.toRegex;
            elemEndRegex = cmd.fromRegex;
            minusRegex = cmd.minusToRegex;
        }
        final String exceptRegex = (cmd.incl ? elemBeginRegex : null);

        /*
         * Computing.
         */

        final List<SortedMap<String,SortedMap<String,SortedSet<String>>>> causesByDepByNameList;
        {
            final boolean mustIncludeBegin = cmd.incl;
            final boolean mustIncludeDepsToBegin = false;
            causesByDepByNameList = jdc.computeDepsGraph(
                    cmd.elemType,
                    NameFilters.matches(elemBeginRegex),
                    mustIncludeBegin,
                    mustIncludeDepsToBegin,
                    ((elemEndRegex != null) ? NameFilters.matches(elemEndRegex) : NameFilters.any()),
                    cmd.maxSteps);
        }
        if (minusRegex != null) {
            final boolean mustIncludeBegin = true;
            final boolean mustIncludeDepsToBegin = false;
            final int maxSteps = -1;
            final Set<String> toRemove = JadecyUtils.computeDepsMergedFromDepsLm(
                    jdc.computeDeps(
                            cmd.elemType,
                            NameFilters.matches(minusRegex),
                            mustIncludeBegin,
                            mustIncludeDepsToBegin,
                            maxSteps)).keySet();
            JdcmUtils.removeSetFromGraphLmml(
                    toRemove,
                    causesByDepByNameList,
                    exceptRegex);
        }

        /*
         * Printing result.
         */
        
        if (cmd.dotFormat) {
            if (cmd.steps) {
                final List<SortedMap<String,SortedSet<String>>> depsByNameList =
                        JadecyUtils.computeGraphCauselessFromGraphLmms(
                                causesByDepByNameList);
                int nextStepId = 0;
                for (SortedMap<String,SortedSet<String>> depsByName : depsByNameList) {
                    JadecyUtils.printGraphInDOTFormatMs(
                            depsByName,
                            "step_" + (nextStepId++),
                            stream);
                }
            } else {
                final SortedMap<String,SortedSet<String>> depsByName =
                        JadecyUtils.computeGraphMergedAndCauselessFromGraphLmms(
                                causesByDepByNameList);
                JadecyUtils.printGraphInDOTFormatMs(
                        depsByName,
                        "allsteps",
                        stream);
            }
        } else if (!cmd.onlyStats) {
            final boolean mustPrintCauses = !cmd.noCauses;
            
            final boolean haveCauses = (cmd.elemType == ElemType.PACKAGE) && mustPrintCauses;
            
            stream.println();
            if (mustUseInverseDeps) {
                stream.println("depending " + cmd.elemType.toStringPluralLC() + " and their predecessors" + (haveCauses ? " with causes" : "") + ":");
            } else {
                stream.println(cmd.elemType.toStringPluralLC() + " depended on and their successors" + (haveCauses ? " with causes" : "") + ":");
            }
            
            if (cmd.steps) {
                stream.println();
                JadecyUtils.printGraphLmms(
                        causesByDepByNameList,
                        mustPrintCauses,
                        stream);
            } else {
                final SortedMap<String,SortedMap<String,SortedSet<String>>> causesByDepByName =
                        JadecyUtils.computeGraphMergedFromGraphLmms(
                                causesByDepByNameList);
                JadecyUtils.printGraphMms(
                        causesByDepByName,
                        mustPrintCauses,
                        stream);
            }
        }
        
        /*
         * Printing stats.
         */
        
        if ((!cmd.dotFormat) && (!cmd.noStats)) {
            final SortedMap<String,SortedSet<String>> depsByName =
                    JadecyUtils.computeGraphMergedAndCauselessFromGraphLmms(
                            causesByDepByNameList);
            final Set<String> totalDepSet = new HashSet<String>();
            for (SortedSet<String> deps : depsByName.values()) {
                totalDepSet.addAll(deps);
            }
            stream.println();
            if (mustUseInverseDeps) {
                stream.println("number of depending " + cmd.elemType.toStringPluralLC() + ": " + depsByName.size());
                stream.println();
                stream.println("number of predecessors: " + totalDepSet.size());
            } else {
                stream.println("number of " + cmd.elemType.toStringPluralLC() + " depended on: " + depsByName.size());
                stream.println();
                stream.println("number of successors: " + totalDepSet.size());
            }
        }
    }
}
