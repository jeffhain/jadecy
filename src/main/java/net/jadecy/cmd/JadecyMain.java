/*
 * Copyright 2015-2016 Jeff Hain
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import net.jadecy.Jadecy;
import net.jadecy.code.InterfaceNameFilter;
import net.jadecy.code.NameFilters;
import net.jadecy.parsing.FsDepsParserFactory;
import net.jadecy.parsing.InterfaceDepsParser;
import net.jadecy.parsing.InterfaceDepsParserFactory;
import net.jadecy.parsing.ParsingFilters;
import net.jadecy.utils.ArgsUtils;

/**
 * Allows for a simple usage of Jadecy from command line, or programmatically.
 */
public class JadecyMain {

    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------

    private static final boolean DEBUG = false;
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /**
     * @param args Arguments, as specified by JdcmCommand.printUsage(...) code.
     */
    public static void main(String[] args) {
        runArgs(args, FsDepsParserFactory.DEFAULT_INSTANCE, System.out);
    }
    
    /**
     * For flexible programmatic usage.
     * 
     * @param args Arguments, as specified by JdcmCommand.printUsage(...) code.
     * @param parserFactory Factory for parser to use. Must not be null.
     * @param defaultStream Stream to use for output (unless -tofile option
     *        is used). Must not be null.
     * @throws NullPointerException if either argument is null.
     */
    public static void runArgs(
            String[] args,
            InterfaceDepsParserFactory parserFactory,
            PrintStream defaultStream) {
        
        ArgsUtils.requireNonNull2(args);
        ArgsUtils.requireNonNull(parserFactory);
        ArgsUtils.requireNonNull(defaultStream);
        
        if (DEBUG) {
            for (int i = 0; i < args.length; i++) {
                System.out.println("args[" + i + "] = " + args[i]);
            }
        }

        final JdcmCommand cmd = JdcmCommand.newValidCommandElseNull(
                args,
                defaultStream);
        if (cmd == null) {
            JdcmCommand.printUsage(
                    JadecyMain.class.getName(),
                    defaultStream);
            return;
        }
        
        final PrintStream resultStream;
        final FileOutputStream fos;
        if (cmd.toFilePath != null) {
            final File resultFile = JdcmUtils.newEmptyFile(
                    cmd.toFilePath,
                    defaultStream);
            if (resultFile == null) {
                // Error already printed.
                return;
            }
            
            try {
                final boolean append = false;
                fos = new FileOutputStream(
                        resultFile,
                        append);
            } catch (FileNotFoundException e) {
                defaultStream.println(
                        "ERROR: just created file not found: "
                                + resultFile.getAbsolutePath());
                return;
            }
            resultStream = new PrintStream(fos);
        } else {
            fos = null;
            resultStream = defaultStream;
        }
        try {
            runCommand(cmd, args, parserFactory, resultStream);
        } finally {
            if (fos != null) {
                // Closing out file-related streams.
                try {
                    resultStream.close();
                } finally {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        // quiet
                    }
                }
            }
        }
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------

    private JadecyMain() {
    }
    
    /**
     * If returns null, prints proper error message before.
     * 
     * @return A new Jadecy with parsing done, or null if a file to parse was
     *         not found.
     */
    private static Jadecy newReadyJadecy(
            JdcmCommand command,
            InterfaceDepsParserFactory parserFactory,
            PrintStream stream) {
        
        final boolean mustMergeNestedClasses = !command.noMerge;
        
        final InterfaceDepsParser parser = parserFactory.newInstance(
                mustMergeNestedClasses,
                command.apiOnly);

        final boolean mustUseInverseDeps = command.compType.usesInverseDeps();

        // Since we only do one computation per parsing,
        // we can rely on parsing regex to limit things,
        // even if it always allows whatever class as long as
        // it is depended on by a parsed class.
        final InterfaceNameFilter retainedClassNameFilter = NameFilters.any();

        final Jadecy jdc = new Jadecy(
                parser,
                mustUseInverseDeps,
                retainedClassNameFilter);

        ParsingFilters filters = ParsingFilters.defaultInstance();
        
        if (command.parseRegex != null) {
            filters = filters.withClassNameFilter(
                    NameFilters.matches(command.parseRegex));
        }

        for (String toParse : command.toParseList) {
            final File file = new File(toParse);
            try {
                parser.accumulateDependencies(file, filters);
            } catch (IllegalArgumentException e) {
                final Throwable cause = e.getCause();
                if (cause instanceof FileNotFoundException) {
                    stream.println(
                            "ERROR: file to parse not found: "
                                    + file.getAbsolutePath());
                    return null;
                } else {
                    throw e;
                }
            }
        }
        
        return jdc;
    }

    /**
     * @param cmd Must be valid (consistent).
     * @param args For printing purpose.
     */
    private static void runCommand(
            JdcmCommand cmd,
            String[] args,
            InterfaceDepsParserFactory parserFactory,
            PrintStream stream) {
        
        final Jadecy jdc = newReadyJadecy(cmd, parserFactory, stream);
        if (jdc == null) {
            // File to parse not found.
            return;
        }
        
        // Printing args: should not hurt, and serves as meta data
        // for the result (defines what it means).
        if (cmd.dotFormat) {
            // DOT format seem to accept C and C++ comments.
            stream.println("// args: " + Arrays.toString(args));
        } else {
            stream.println("args: " + Arrays.toString(args));
        }

        final JdcmCompType compType = cmd.compType;

        if ((compType == JdcmCompType.DEPSOF)
                || (compType == JdcmCompType.DEPSTO)) {

            JdcmComp_DEPSOF_DEPSTO.runCommand(jdc, cmd, stream);

        } else if ((compType == JdcmCompType.GDEPSOF)
                || (compType == JdcmCompType.GDEPSTO)) {

            JdcmComp_GDEPSOF_GDEPSTO.runCommand(jdc, cmd, stream);

        } else if (compType == JdcmCompType.SPATH) {

            JdcmComp_SPATH.runCommand(jdc, cmd, stream);

        } else if (compType == JdcmCompType.PATHSG) {

            JdcmComp_PATHSG.runCommand(jdc, cmd, stream);
            
        } else if (compType == JdcmCompType.SCCS) {

            JdcmComp_SCCS.runCommand(jdc, cmd, stream);

        } else if (compType == JdcmCompType.CYCLES) {

            JdcmComp_CYCLES.runCommand(jdc, cmd, stream);

        } else if (compType == JdcmCompType.SCYCLES) {

            JdcmComp_SCYCLES.runCommand(jdc, cmd, stream);

        } else if (compType == JdcmCompType.SOMECYCLES) {

            JdcmComp_SOMECYCLES.runCommand(jdc, cmd, stream);
        }
    }
}
