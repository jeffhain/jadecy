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
package net.jadecy.comp;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.jadecy.names.InterfaceNameFilter;
import net.jadecy.names.NameFilters;

/**
 * Helper to ensure compilation of Java sources into some directory.
 * 
 * Uses JavacHelper.
 * 
 * Useful to ensure compilation of some sources once among all unit tests.
 * 
 * If class files are automatically compiled with an IDE, can use the path of
 * the directory where it compiles class files instead, but then fresh
 * compilation is not ensured, and testing becomes IDE-dependent.
 */
public class CompHelper {

    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------
    
    private static final boolean DEBUG = false;
    
    //--------------------------------------------------------------------------
    // PRIVATE CLASSES
    //--------------------------------------------------------------------------

    private static class MyCompData {
        final String outputDirPath;
        final RunOnceHelper runOnceHelper;
        public MyCompData(
                String outputDirPath,
                RunOnceHelper runOnceHelper) {
            this.outputDirPath = outputDirPath;
            this.runOnceHelper = runOnceHelper;
        }
    }
    
    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------

    private final JavacHelper javacHelper;
    
    private final String outputDirParentPath;
    
    private final Object mutex = new Object();
    
    /**
     * Guarded by synchronization mutex.
     */
    private final Map<List<String>,MyCompData> compDataBySourcesDirs =
            new HashMap<List<String>,MyCompData>();

    /**
     * Guarded by synchronization mutex.
     * 
     * Used to compute unique yet short compilation directories corresponding
     * to a given list of directories to compile.
     */
    private final Map<String,Integer> srcDirIdBySrcDirPath = new HashMap<String,Integer>();
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /**
     * @param optionList Must not contain "-d" option, which is specified
     *        for each compilation, nor classpath options, which are specified
     *        aside. Can be empty.
     * @param classpathElementList Can be empty.
     * @param outStream Stream for logs (possibly from compiler, and possibly error logs).
     * @param outputDirParentPath Path of directory where output directory
     *        must be created.
     */
    public CompHelper(
            List<String> optionList,
            List<String> classpathElementList,
            PrintStream outStream,
            //
            String outputDirParentPath) {
        
        this.javacHelper = new JavacHelper(
                optionList,
                classpathElementList,
                outStream);

        this.outputDirParentPath = outputDirParentPath;
    }

    /**
     * Convenience constructor.
     * 
     * Uses "-g:vars -Xlint:-options -source sourceVersion -target targetVersion" option list,
     * System.err as out stream.
     * 
     * @param sourceVersion String for the -source option.
     * @param targetVersion String for the -target option.
     * @param classpathElementList Can be empty.
     * @param outputDirParentPath Path of directory where output directory
     *        must be created.
     */
    public CompHelper(
            String sourceVersion,
            String targetVersion,
            List<String> classpathElementList,
            //
            String outputDirParentPath) {
        
        this.javacHelper = new JavacHelper(
                sourceVersion,
                targetVersion,
                classpathElementList);

        this.outputDirParentPath = outputDirParentPath;
    }

    /*
     * 
     */
    
    /**
     * Thread-safe, as long as the file system is not modified concurrently
     * by something else.
     * 
     * Ensures that specified source directories have been compiled at least once
     * since creation of this instance, in the specified order, and searching
     * for source files recursively, in the output directory which path is returned.
     * 
     * The order of directories matters, i.e. different orders mean different
     * compilations and output directories.
     * 
     * Source directories paths must use '/' as separator.
     * They should not contain '_', for it is used to replace separators to
     * obtain a string unique for the specified list of directory paths, to avoid
     * interferences between different compilations.
     * 
     * Blocks until compilation has been ensured since this creation of this instance.
     * 
     * @param srcDirPathList Paths of root directories which java classes must be compiled.
     * @return The path of the directory where compiled class files
     *         and related packages (as directories) can be found.
     */
    public String ensureCompiledAndGetOutputDirPath(List<String> srcDirPathList) {
        
        final MyCompData compData = this.getOrCreateCompData(srcDirPathList);
        
        compData.runOnceHelper.ensureRanOnceSync();
        
        return compData.outputDirPath;
    }

    /**
     * Thread-safe.
     */
    public String getOutputDirPath(List<String> srcDirPathList) {

        final MyCompData compData = this.getOrCreateCompData(srcDirPathList);
        
        return compData.outputDirPath;
    }
    
    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------

    private MyCompData getOrCreateCompData(List<String> srcDirPathList) {
        
        if (DEBUG) {
            System.out.println("getOrCreateCompData(" + srcDirPathList + ")");
        }
        
        MyCompData compData;
        synchronized (this.mutex) {
            compData = this.compDataBySourcesDirs.get(srcDirPathList);
            if (compData == null) {
                final String outputDirPath = this.computeOutputDirPath(
                        this.outputDirParentPath,
                        srcDirPathList);
                final List<String> mySrcDirPathList = new ArrayList<String>(srcDirPathList);
                final Runnable runnable = new Runnable() {
                    public void run() {
                        JdcFsUtils.clearDir(new File(outputDirPath));
                        final InterfaceNameFilter relativeNameFilter = NameFilters.any();
                        javacHelper.compile(
                                outputDirPath,
                                relativeNameFilter,
                                mySrcDirPathList);
                    }
                };
                final RunOnceHelper runOnceHelper = new RunOnceHelper(runnable);
                
                compData = new MyCompData(
                        outputDirPath,
                        runOnceHelper);
                this.compDataBySourcesDirs.put(srcDirPathList, compData);
            }
        }
        
        return compData;
    }

    /*
     * 
     */

    private String computeOutputDirPath(
            String outputDirParentPath,
            List<String> srcDirPathList) {
        final StringBuilder sb = new StringBuilder();
        sb.append(outputDirParentPath);
        sb.append("/comp_");
        boolean first = true;
        for (String srcDirPath : srcDirPathList) {
            Integer srcDirId = this.srcDirIdBySrcDirPath.get(srcDirPath);
            if (srcDirId == null) {
                srcDirId = this.srcDirIdBySrcDirPath.size() + 1;
                this.srcDirIdBySrcDirPath.put(srcDirPath, srcDirId);
            }
            if (first) {
                first = false;
            } else {
                sb.append("_");
            }
            sb.append(srcDirId.intValue());
        }
        return sb.toString();
    }
}
