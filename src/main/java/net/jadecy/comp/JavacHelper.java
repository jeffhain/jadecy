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
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import net.jadecy.names.InterfaceNameFilter;
import net.jadecy.names.NameUtils;

/**
 * Helper to compile Java sources into some directory.
 */
public class JavacHelper {

    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------

    private static final boolean DEBUG = false;
    
    //--------------------------------------------------------------------------
    // FIELDS
    //--------------------------------------------------------------------------
    
    /**
     * We don't like system-dependent default values.
     */
    private static final Locale LOCALE = Locale.US;
    
    /**
     * We don't like system-dependent default values.
     */
    private static final String CHARSET_NAME = "UTF-8";
    private static final Charset CHARSET = Charset.forName(CHARSET_NAME);

    private final List<String> optionList;
    
    private final List<String> classpathElementList;
    
    private final PrintStream outStream;
    
    /**
     * "out" argument for JavaCompiler.getTask(...).
     */
    private final Writer out;

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /**
     * @param optionList Must not contain "-d" option, which is specified
     *        for each compilation, nor classpath options, which are specified
     *        aside. Can be empty.
     * @param classpathElementList Can be empty.
     * @param outStream Stream for logs (possibly from compiler, and possibly error logs).
     */
    public JavacHelper(
            List<String> optionList,
            List<String> classpathElementList,
            PrintStream outStream) {
        if (outStream == null) {
            throw new NullPointerException();
        }
        
        // Defensive copies.
        this.optionList = new ArrayList<String>(optionList);
        this.classpathElementList = new ArrayList<String>(classpathElementList);
        
        this.outStream = outStream;
        try {
            this.out = new OutputStreamWriter(outStream, CHARSET_NAME);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
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
     */
    public JavacHelper(
            String sourceVersion,
            String targetVersion,
            List<String> classpathElementList) {
        this(
                computeDefaultOptionList(sourceVersion, targetVersion),
                classpathElementList,
                System.err);
    }

    /*
     * 
     */

    /**
     * Only files ending with ".java" are provided to the specified filter.
     * 
     * @param compDirPath Path of the directory where class files and their
     *        packages must be generated.
     * @param relativeNameFilter Filter to accept source files, based on their
     *        relative path from the specified source directory in which
     *        they were found, with slashes replaced with dots and trailing ".java"
     *        removed (i.e. equal to classes names if packages start in
     *        specified source directories).
     * @param srcDirPathList Paths of source directories in which to
     *        recursively search for source files.
     */
    public void compile(
            String compDirPath,
            InterfaceNameFilter relativeNameFilter,
            List<String> srcDirPathList) {
        for (String srcDirPath : srcDirPathList) {
            this.compileInto(
                    compDirPath,
                    relativeNameFilter,
                    srcDirPath);
        }
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------

    private static List<String> computeDefaultOptionList(String sourceVersion, String targetVersion) {
        final ArrayList<String> optionList = new ArrayList<String>();

        // To have arguments names in class files, instead of arg0 etc.
        // NB: Doesn't seem to work for interfaces.
        optionList.add("-g:vars");

        // To avoid warnings with obsolete options.
        optionList.add("-Xlint:-options");
        optionList.add("-source");
        optionList.add(sourceVersion);
        optionList.add("-target");
        optionList.add(targetVersion);
        
        return optionList;
    }
    
    private void compileInto(
            String compDirPath,
            InterfaceNameFilter relativeNameFilter,
            String srcDirPath) {
        
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("no JavaCompiler provided for this platform");
        }

        final List<String> javaFilePathList = new ArrayList<String>();
        final File srcDir = new File(srcDirPath);
        addAllJavaFilesFromDirInto(
                "",
                srcDir,
                relativeNameFilter,
                javaFilePathList);

        {
            final File compDir = new File(compDirPath);
            JdcFsUtils.ensureDir(compDir);
        }

        final List<String> options = new ArrayList<String>(this.optionList);
        {
            options.add("-d");
            options.add(compDirPath);
            
            {
                options.add("-cp");
                
                final StringBuilder sb = new StringBuilder();
                
                // For visibility of compiled classes, when compiling
                // in multiple passes into a same directory.
                sb.append(compDirPath);
                
                for (String classpathElement : this.classpathElementList) {
                    sb.append(";");
                    sb.append(classpathElement);
                }
                
                options.add(sb.toString());
            }
        }

        final Writer out = this.out;
        // For annotation processing.
        // We don't delve into so language-specific things.
        final List<String> classes = null;
        final DiagnosticCollector<JavaFileObject> diagnosticListener =
                new DiagnosticCollector<JavaFileObject>();
        final StandardJavaFileManager fileManager = compiler.getStandardFileManager(
                diagnosticListener,
                LOCALE,
                CHARSET);
        final Iterable<? extends JavaFileObject> compilationUnits =
                fileManager.getJavaFileObjectsFromStrings(
                        javaFilePathList);
        
        if (DEBUG) {
            System.out.println("javaFilePathList = " + javaFilePathList);
            System.out.println("options = " + options);
        }
        
        final CompilationTask task = compiler.getTask(
                out,
                fileManager,
                diagnosticListener,
                options,
                classes,
                compilationUnits);

        final boolean success = task.call();
        if (!success) {
            onCompilationError(diagnosticListener);
        }

        try {
            fileManager.close();
        } catch (IOException ignore) {
        }
    }
    
    private void onCompilationError(DiagnosticCollector<JavaFileObject> diagnosticListener) {
        final PrintStream outStream = this.outStream;
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnosticListener.getDiagnostics()) {
            outStream.println();
            outStream.println("code = " + diagnostic.getCode());
            outStream.println("kind = " + diagnostic.getKind());
            outStream.println("position = " + diagnostic.getPosition());
            outStream.println("start position = " + diagnostic.getStartPosition());
            outStream.println("end position = " + diagnostic.getEndPosition());
            outStream.println("source = " + diagnostic.getSource());
            outStream.println("message = " + diagnostic.getMessage(LOCALE));
        }
        final RuntimeException exception = new RuntimeException("compilation failed");
        exception.printStackTrace(outStream);
        throw exception;
    }
    
    /*
     * 
     */
    
    private static String concatPathAndName(String path, String name) {
        if (path.isEmpty()) {
            return name;
        }
        return path + "/" + name;
    }
    
    /**
     * This method is recursive.
     * 
     * This method constructs files relative path from initial source directory,
     * rather than trying to recompute them from complete files paths,
     * which can be tricky depending on links or other horrors.
     * 
     * @param currentDirRelativePath Use empty string as initial value.
     */
    private static void addAllJavaFilesFromDirInto(
            String currentDirRelativePath,
            File currentDir,
            InterfaceNameFilter relativeNameFilter,
            List<String> javaFilePathList) {
        final File[] childArr = currentDir.listFiles();
        if (childArr == null) {
            return;
        }
        for (File child : childArr) {
            if (child.isDirectory()) {
                final String dirName = child.getName();
                final String newRelPath = concatPathAndName(currentDirRelativePath, dirName);
                addAllJavaFilesFromDirInto(
                        newRelPath,
                        child,
                        relativeNameFilter,
                        javaFilePathList);
            } else {
                final String fileName = child.getName();
                final String dotExt = ".java";
                final boolean isJavaFile = fileName.endsWith(dotExt);
                if (isJavaFile) {
                    final String fileRelPath = concatPathAndName(currentDirRelativePath, fileName);
                    final String fileRelPathNoExt = fileRelPath.substring(0, fileRelPath.length() - dotExt.length()); 
                    final String fileRelName = NameUtils.doted(fileRelPathNoExt);
                    if (relativeNameFilter.accept(fileRelName)) {
                        final String javaFilePath = child.getPath();
                        if (DEBUG) {
                            System.out.println("javaFilePath = " + javaFilePath);
                        }
                        javaFilePathList.add(javaFilePath);
                    }
                }
            }
        }
    }
}
