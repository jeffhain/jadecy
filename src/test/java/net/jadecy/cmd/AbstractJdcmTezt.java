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

import net.jadecy.names.NameUtils;
import net.jadecy.utils.MemPrintStream;
import net.jadecy.virtual.AbstractVirtualCodeGraphTezt;

/**
 * Abstract class for JadecyMain tests.
 */
public abstract class AbstractJdcmTezt extends AbstractVirtualCodeGraphTezt {

    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------

    /**
     * Empty string means all classes are parsed (for our virtual graph).
     */
    private static final String DEFAULT_PARSE_PATH = "";
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /**
     * Creates without p3.
     */
    public AbstractJdcmTezt() {
        this(false);
    }

    /**
     * @param withP3 If true, adds a p3 package, containing an strongly
     *        connected component
     */
    public AbstractJdcmTezt(boolean withP3) {
        super(withP3);
    }

    //--------------------------------------------------------------------------
    // PACKAGE-PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    /**
     * @return Quoted string.
     */
    static String q(String string) {
        return NameUtils.quoted(string);
    }
    
    /**
     * @param prefixArgsArr Array of first arguments.
     * @param inlinedArgs Space separated arguments, except parsed files list
     *        which is automatically added.
     * @return Arguments for JadecyMain.
     */
    static String[] getArgs(String[] prefixArgsArr, String inlinedArgs) {
        final String[] splitArgs = inlinedArgs.split(" ");
        
        final String[] args = new String[prefixArgsArr.length + splitArgs.length];
        
        int i = copy(prefixArgsArr, args, 0);
        
        copy(splitArgs, args, i);
        
        return args;
    }

    /**
     * @param inlinedArgs Space separated arguments, except parsed files list
     *        which is automatically added.
     * @return Arguments for JadecyMain.
     */
    static String[] getArgs(String inlinedArgs) {
        return getArgs(new String[]{DEFAULT_PARSE_PATH}, inlinedArgs);
    }
    
    /*
     * 
     */
    
    /**
     * Uses virtual parser factory, which ensures usage of memory-defined test graph.
     */
    void runArgsWithVirtualDeps(String[] args, PrintStream defaultStream) {
        JadecyMain.runArgs(args, this.virtualDepsParserFactory, defaultStream);
    }
    
    /*
     * 
     */

    static String[] withPringUsageAdded(String[] prefixLines) {
        
        final MemPrintStream stream = new MemPrintStream();
        JdcmCommand.printUsage(
                JadecyMain.class.getName(),
                stream);
        final List<String> usageLines = stream.getLines();
        
        final String[] lines = new String[prefixLines.length + usageLines.size()];
        for (int i = 0; i < prefixLines.length; i++) {
            lines[i] = prefixLines[i];
        }
        for (int i = 0; i < usageLines.size(); i++) {
            lines[prefixLines.length + i] = usageLines.get(i);
        }
        return lines;
    }
    
    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    private static int copy(String[] from, String[] to, int toIndex) {
        for (int i = 0; i < from.length; i++) {
            to[toIndex++] = from[i];
        }
        return toIndex;
    }
}
