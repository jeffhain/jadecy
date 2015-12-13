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
import java.util.Set;
import java.util.SortedMap;

import net.jadecy.Jadecy;
import net.jadecy.JadecyUtils;
import net.jadecy.code.NameFilters;

class JdcmComp_DEPSOF_DEPSTO {

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    public static void runCommand(
            Jadecy jdc,
            JdcmCommand cmd,
            PrintStream stream) {

        final JdcmCompType computation = cmd.compType;

        final boolean mustUseInverseDeps = computation.usesInverseDeps();

        final String baseRegex;
        final String keepOnlyRegex;
        final String minusRegex;
        if (computation == JdcmCompType.DEPSOF) {
            baseRegex = cmd.ofRegex;
            keepOnlyRegex = cmd.intoRegex;
            minusRegex = cmd.minusOfRegex;
        } else {
            baseRegex = cmd.toRegex;
            keepOnlyRegex = cmd.fromRegex;
            minusRegex = cmd.minusToRegex;
        }
        final String exceptRegex = (cmd.incl ? baseRegex : null);

        /*
         * Computing.
         */

        final List<SortedMap<String,Long>> byteSizeByNameList;
        {
            final boolean mustIncludeBegin = cmd.incl;
            final boolean mustIncludeDepsToBegin = false;
            byteSizeByNameList = jdc.computeDeps(
                    cmd.elemType,
                    NameFilters.matches(baseRegex),
                    mustIncludeBegin,
                    mustIncludeDepsToBegin,
                    cmd.maxSteps);
        }
        JdcmUtils.keepOnlyMatchesInMapList(
                byteSizeByNameList,
                keepOnlyRegex,
                exceptRegex);
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
            JdcmUtils.removeSetFromBulkLm(
                    toRemove,
                    byteSizeByNameList,
                    exceptRegex);
        }

        /*
         * Printing result.
         */

        final SortedMap<String,Long> byteSizeByName = JadecyUtils.computeDepsMergedFromDepsLm(byteSizeByNameList);
        
        if (!cmd.onlyStats) {
            stream.println();
            if (mustUseInverseDeps) {
                stream.println("depending " + cmd.elemType.toStringPluralLC() + " and their byte size:");
            } else {
                stream.println(cmd.elemType.toStringPluralLC() + " depended on and their byte size:");
            }
            if (cmd.steps) {
                stream.println();
                JadecyUtils.printDepsLm(
                        byteSizeByNameList,
                        stream);
            } else {
                JadecyUtils.printDepsM(
                        byteSizeByName,
                        stream);
            }
        }
        
        /*
         * Printing stats.
         */

        if (!cmd.noStats) {
            stream.println();
            if (mustUseInverseDeps) {
                stream.println("number of depending " + cmd.elemType.toStringPluralLC() + ": " + byteSizeByName.size());
            } else {
                stream.println("number of " + cmd.elemType.toStringPluralLC() + " depended on: " + byteSizeByName.size());
            }
            
            final long totalByteSize = JadecyUtils.computeByteSizeM(byteSizeByName);
            
            stream.println();
            stream.println("total byte size: " + totalByteSize);
        }
    }
}
