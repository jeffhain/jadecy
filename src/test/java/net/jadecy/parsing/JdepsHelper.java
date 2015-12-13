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
package net.jadecy.parsing;

import java.util.Collection;

import net.jadecy.comp.PathHelper;
import net.jadecy.comp.RuntimeExecHelper;
import net.jadecy.utils.MemPrintStream;

/**
 * Helper to use jdeps through a ClassDepsParser-like API.
 */
class JdepsHelper {
    
    /*
     * For equivalent configuration, we need:
     * - For jdeps, to use filter:none, not to get classes of same package filtered out.
     * - For Jadecy, to use mustMergeNestedClasses = false, because when not filtering
     *   out classes of same package, jdeps also doesn't filter out nested classes.
     */

    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------
    
    private static final boolean DEBUG = false;
    
    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------
    
    private final String jdepsPath;
    private final String classPath;
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    public JdepsHelper(
            String jdepsPath,
            String classPath) {
        this.jdepsPath = jdepsPath;
        this.classPath = classPath;
    }

    /**
     * @param jdepsDeps (in,out) Collection where to add dependencies computed
     *        by jdeps.
     * @return The specified class name if a dependency could be computed,
     *         null otherwise.
     */
    public String computeDependencies(
            String className,
            boolean apiOnly,
            Collection<String> jdepsDeps) {
        
        /*
         * Preparing jdeps command.
         */
        
        final StringBuilder sb = new StringBuilder();
        // Need quoted to avoid problems with spaces.
        sb.append(PathHelper.quoted(this.jdepsPath));
        sb.append(" -verbose:class");
        sb.append(" -filter:none");
        if (apiOnly) {
            sb.append(" -apionly");
        }
        sb.append(" -cp " + PathHelper.quoted(this.classPath));
        
        sb.append(" " + className);
        final String cmd = sb.toString();
        
        if (DEBUG) {
            System.out.println("cmd: " + cmd);
        }
        
        /*
         * Executing jdeps.
         */
        
        final MemPrintStream stream = new MemPrintStream();
        final int exitValue = RuntimeExecHelper.execSyncNoIE(cmd, stream);
        
        if (exitValue != 0) {
            // Had some trouble.
            return null;
        }
        
        /*
         * Converting jdeps output into output collection.
         */
        
        final String depPrefix = "-> ";
        
        boolean foundSomeDeps = false;
        
        for (String line : stream.getLines()) {
            
            if (DEBUG) {
                System.out.println(line);
            }
            
            final String trimmed = line.trim();
            if (trimmed.startsWith(depPrefix)) {
                foundSomeDeps = true;
                
                final String nameEtc = trimmed.substring(depPrefix.length());
                final int spaceIndex = nameEtc.indexOf(' ');
                final int lastExcl = ((spaceIndex < 0) ? nameEtc.length() : spaceIndex);
                final String depClassName = nameEtc.substring(0, lastExcl);
                jdepsDeps.add(slashed(depClassName));
            }
        }
        
        if (foundSomeDeps) {
            return slashed(className);
        } else {
            // Found no dependency, means couldn't deal with the class (should have
            // at least extended class, or String etc. for Object class).
            return null;
        }
    }
    
    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    private static String slashed(String className) {
        return className.replace('.','/');
    }
}
