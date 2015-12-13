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
package net.jadecy.comp;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper to ensure compilation of Java sources into some directory.
 * 
 * Uses JavacHelper (has its side effects, like creating
 * JavacHelper.SRC_TO_COMP_FILE_NAME).
 * 
 * Useful to ensure compilation of some sources once among all unit tests.
 * 
 * If class files are automatically compiled with an IDE, can use the path of
 * the directory where it compiles class files instead, but then fresh
 * compilation is not ensured, and testing becomes IDE-dependent.
 */
public class CompHelper {

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
    
    private final Map<List<String>,MyCompData> compDataBySourcesDirs =
            new HashMap<List<String>,MyCompData>();

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /**
     * @param javacPath Path of javac executable.
     * @param javacOptions Options for javac (ex.: "-source 1.8 -target 1.8").
     * @param javacClasspath Classpath for javac (ex.: "lib/junit.jar").
     * @param stream Stream for javac output, and to output javac command line.
     * @param outputDirParentPath Path of directory where output directory
     *        must be created.
     */
    public CompHelper(
            String javacPath,
            String javacOptions,
            String javacClasspath,
            PrintStream stream,
            //
            String outputDirParentPath) {
        
        this.javacHelper = new JavacHelper(
                javacPath,
                javacOptions,
                javacClasspath,
                stream);

        this.outputDirParentPath = outputDirParentPath;
    }
    
    /**
     * Thread-safe, as long as the file system is not modified concurrently
     * by something else.
     * 
     * Ensures that specified source directories have been compiled at least once
     * since creation of this instance, in the specified order, in the output
     * directory which path is returned.
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
     * @param srcDirPathArr Paths of root directories which java classes must be compiled.
     * @return The path of the directory where compiled class files
     *         and related packages (as directories) can be found.
     */
    public String ensureCompiledAndGetOutputDirPath(String... srcDirPathArr) {
        final List<String> srcDirPathList = arrToList(srcDirPathArr);
        
        final MyCompData compData = this.getOrCreateCompData(srcDirPathList);
        
        compData.runOnceHelper.ensureRanOnceSync();
        
        return compData.outputDirPath;
    }

    /**
     * Thread-safe.
     */
    public String getOutputDirPath(String... srcDirPathArr) {
        final List<String> srcDirPathList = arrToList(srcDirPathArr);

        final MyCompData compData = this.getOrCreateCompData(srcDirPathList);
        
        return compData.outputDirPath;
    }
    
    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------

    private MyCompData getOrCreateCompData(List<String> srcDirPathList) {
        
        MyCompData compData;
        synchronized (this.compDataBySourcesDirs) {
            compData = this.compDataBySourcesDirs.get(srcDirPathList);
            if (compData == null) {
                final String[] srcDirPathArr = srcDirPathList.toArray(new String[srcDirPathList.size()]);
                final String outputDirPath = computeOutputDirPath(
                        this.outputDirParentPath,
                        srcDirPathArr);
                final Runnable runnable = new Runnable() {
                    public void run() {
                        FileSystemHelper.clearDir(outputDirPath);
                        javacHelper.compile(
                                outputDirPath,
                                srcDirPathArr);
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
    
    public static List<String> arrToList(String... arr) {
        final List<String> list = new ArrayList<String>();
        for (String element : arr) {
            list.add(element);
        }
        return list;
    }

    private static String computeOutputDirPath(
            String outputDirParentPath,
            String... srcDirPathArr) {
        final StringBuilder sb = new StringBuilder();
        sb.append(outputDirParentPath);
        sb.append("/");
        boolean first = true;
        for (String srcDirPath : srcDirPathArr) {
            if (srcDirPath.contains("_")) {
                throw new IllegalArgumentException("must not contain underscore : " + srcDirPath);
            }
            if (first) {
                first = false;
            } else {
                sb.append("_");
            }
            sb.append(flatPath(srcDirPath));
        }
        return sb.toString();
    }

    private static String flatPath(String path) {
        return path.replaceAll("/", "_");
    }
}
