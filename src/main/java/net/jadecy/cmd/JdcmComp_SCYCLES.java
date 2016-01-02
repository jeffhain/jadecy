/*
 * Copyright 2016 Jeff Hain
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

import net.jadecy.Jadecy;

class JdcmComp_SCYCLES {

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    public static void runCommand(
            Jadecy jdc,
            JdcmCommand cmd,
            PrintStream stream) {

        /*
         * Computing and printing result.
         */
        
        final boolean mustPrintOnProcess = !cmd.onlyStats;
        final boolean mustPrintCauses = !cmd.noCauses;
        
        final JdcmCycleProcessor processor = new JdcmCycleProcessor(
                cmd.elemType,
                mustPrintOnProcess,
                mustPrintCauses,
                cmd.dotFormat,
                stream,
                cmd.minSize,
                cmd.maxCount);
        if (JdcmUtils.notEmpty(cmd.minSize, cmd.maxSize)
                && (cmd.maxCount != 0)) {
            jdc.computeShortestCycles(
                    cmd.elemType,
                    cmd.maxSize,
                    processor);
        }
        
        /*
         * Printing stats.
         */
        
        if ((!cmd.dotFormat) && (!cmd.noStats)) {
            processor.printStats();
        }
    }
}
