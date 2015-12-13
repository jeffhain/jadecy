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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper to compile Java sources into some directory.
 */
public class JavacHelper {

    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------

    private static final boolean DEBUG = false;

    /**
     * javac way of compiling files in sub-directories, without specifying each
     * of them and ending up with command line length trouble, is to read
     * them from a text file, which is indicated by adding "@" before
     * (and touching) its name.
     * 
     * This is the name of the file created in output directory
     * to contain that list.
     */
    public static final String SRC_TO_COMP_FILE_NAME = "src_to_comp_list.txt";

    private final String javacPath;
    private final String javacOptions;
    private final String javacClasspath;
    
    private final PrintStream stream;
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /**
     * @param javacPath Path of javac executable.
     * @param javacOptions Options for javac (ex.: "-source 1.8 -target 1.8").
     * @param javacClasspath Classpath for javac (ex.: "lib/junit.jar").
     * @param stream Stream for javac output, and to output javac command line.
     */
    public JavacHelper(
            String javacPath,
            String javacOptions,
            String javacClasspath,
            PrintStream stream) {
        this.javacPath = javacPath;
        this.javacOptions = javacOptions;
        this.javacClasspath = javacClasspath;
        this.stream = stream;
    }

    /*
     * 
     */
    
    /**
     * @param outputDirPath Path of the directory where class files and their
     *        packages must be generated.
     * @param srcDirPathArr Paths of the root directories containing sources
     *        to compile.
     */
    public void compile(
            String outputDirPath,
            String... srcDirPathArr) {
        for (String srcDirPath : srcDirPathArr) {
            this.compileSingleDir(
                    srcDirPath,
                    outputDirPath);
        }
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    private void compileSingleDir(
            String srcDirPath,
            String outputDirPath) {
        
        if (DEBUG) {
            System.out.println("compile(" + srcDirPath + ")");
        }

        final List<String> srcFilePathList = new ArrayList<String>();

        addSrcFilesPathsInto(
                new File(srcDirPath),
                srcFilePathList);

        compile(
                srcFilePathList,
                outputDirPath,
                this.javacPath,
                this.getJavacArgs(outputDirPath),
                this.stream);
    }
    
    /*
     * 
     */

    private String getJavacArgs(String outputDirPath) {
        // Quoting paths in case user puts spaces in them.
        final String qOutputDirPath = PathHelper.quoted(outputDirPath);
        final String qSrcToOutputFilePath = PathHelper.quoted(outputDirPath + "/" + SRC_TO_COMP_FILE_NAME);
        return this.javacOptions + " -d " + qOutputDirPath
                + " @" + qSrcToOutputFilePath
                + " -cp " + qOutputDirPath + (this.javacClasspath.length() != 0 ? ";\"" + this.javacClasspath + "\"" : "");
    }

    /*
     * 
     */
    
    private static void compile(
            List<String> srcFilePathList,
            String outputDirPath,
            String javacPath,
            String javacArgs,
            PrintStream stream) {

        setSrcToCompFileContent(
                srcFilePathList,
                outputDirPath);

        final StringBuilder sb = new StringBuilder();
        sb.append(javacPath);
        sb.append(" ");
        sb.append(javacArgs);
        final String cmd = sb.toString();
        
        stream.println("cmd = " + cmd);
        if (DEBUG) {
            System.out.println("cmd = " + cmd);
        }

        RuntimeExecHelper.execSyncNoIE(cmd, stream);
    }

    private static void setSrcToCompFileContent(
            List<String> srcFilePathList,
            String outputDirPath) {
        final File file = new File(outputDirPath + "/" + SRC_TO_COMP_FILE_NAME);
        
        FileSystemHelper.ensureEmptyFile(file);

        try {
            final PrintWriter pw = new PrintWriter(file);
            try {
                for (String srcFilePath : srcFilePathList) {
                    pw.println(srcFilePath);
                }
            } finally {
                pw.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("file : " + file.getAbsolutePath(), e);
        }
    }

    /*
     * 
     */
    
    /**
     * This method is recursive.
     */
    private static void addSrcFilesPathsInto(
            File srcDir,
            List<String> srcFilePathList) {
        final String[] childList = srcDir.list();
        if (childList == null) {
            return;
        }
        for (String childName : childList) {
            final File child = new File(srcDir + "/" + childName);
            if (child.isDirectory()) {
                addSrcFilesPathsInto(
                        child,
                        srcFilePathList);
            } else {
                if (childName.endsWith(".java")) {
                    String filePath = child.getAbsolutePath();
                    filePath = PathHelper.quoted(filePath);
                    srcFilePathList.add(filePath);
                }
            }
        }
    }
}
