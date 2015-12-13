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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import net.jadecy.Jadecy;
import net.jadecy.JadecyUtils;

class JdcmComp_SCCS {

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
        
        final List<SortedMap<String,Long>> byteSizeByNameList;
        if ((cmd.maxSize != 0) && (cmd.maxCount != 0)) {
            final List<SortedMap<String,Long>> res = jdc.computeSccs(cmd.elemType);
            /*
             * Only keeping small enough SCCs.
             */
            if (cmd.maxSize >= 0) {
                // Only keeping small enough ones.
                byteSizeByNameList = new ArrayList<SortedMap<String,Long>>();
                for (SortedMap<String,Long> scc : res) {
                    if (scc.size() <= cmd.maxSize) {
                        byteSizeByNameList.add(scc);
                    }
                }
            } else {
                byteSizeByNameList = res;
            }
            /*
             * Removing supernumerary SCCs.
             */
            if (cmd.maxCount >= 0) {
                while (byteSizeByNameList.size() > cmd.maxCount) {
                    // Removing last ones first, i.e. biggest ones first,
                    // as if they had been discovered in the order they
                    // are in the list.
                    byteSizeByNameList.remove(byteSizeByNameList.size() - 1);
                }
            }
        } else {
            byteSizeByNameList = new ArrayList<SortedMap<String,Long>>();
        }
        
        /*
         * Printing result.
         */
        
        if (!cmd.onlyStats) {
            if (byteSizeByNameList.size() != 0) {
                stream.println();
                JadecyUtils.printSccsCs(
                        byteSizeByNameList,
                        stream);
            }
        }
        
        /*
         * Printing stats.
         */
        
        if (!cmd.noStats) {
            final SortedMap<Integer,Integer> sccCountBySize =
                    JadecyUtils.computeCountBySizeLm(byteSizeByNameList);
            
            stream.println();
            stream.println("number of SCCs by size (number of " + cmd.elemType.toStringPluralLC() + "):");
            for (Map.Entry<Integer, Integer> entry : sccCountBySize.entrySet()) {
                stream.println(entry.getKey() + " : " + entry.getValue());
            }

            stream.println();
            stream.println("number of SCCs found: " + byteSizeByNameList.size());

            stream.println();
            stream.println("total byte size: " + JadecyUtils.computeByteSizeLm(byteSizeByNameList));
        }
    }
}
