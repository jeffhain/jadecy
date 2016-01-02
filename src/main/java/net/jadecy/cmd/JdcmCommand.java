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

import java.io.PrintStream;
import java.util.ArrayList;

import net.jadecy.ElemType;

/**
 * Represents the command from command line.
 * Contains the validation logic.
 */
class JdcmCommand {
    
    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------

    private static final String JAR_FILE_NAME = "jadecy.jar";
    
    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------

    final ArrayList<String> toParseList = new ArrayList<String>();
    JdcmCompType compType = null;
    String parseRegex = null;
    String ofRegex = null;
    String toRegex = null;
    String intoRegex = null;
    String fromRegex = null;
    String minusOfRegex = null;
    String minusToRegex = null;
    String beginRegex = null;
    String endRegex = null;
    //
    boolean noMerge = false;
    //
    ElemType elemType = ElemType.CLASS;
    boolean apiOnly = false;
    //
    boolean steps = false;
    boolean incl = false;
    //
    /*
     * nulls by default to check for duplications.
     */
    Integer maxSteps = null;
    Integer minSize = null;
    Integer maxSize = null;
    Long maxCount = null;
    /**
     * For convenience, allowing usage with cases that make it useless or
     * implicitly true, such as not using -packages, or using -onlystats
     * or -dotformat.
     */
    boolean noCauses = false;
    boolean noStats = false;
    boolean onlyStats = false;
    //
    boolean dotFormat = false;
    String toFilePath = null;
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName()
                + "[toParseList = " + toParseList
                + ", computation = " + compType
                + ", parseRegex = " + parseRegex
                + ", ofRegex = " + ofRegex
                + ", toRegex = " + toRegex
                + ", intoRegex = " + intoRegex
                + ", fromRegex = " + fromRegex
                + ", minusOfRegex = " + minusOfRegex
                + ", minusToRegex = " + minusToRegex
                + ", beginRegex = " + beginRegex
                + ", endRegex = " + endRegex
                + ", noMerge = " + noMerge
                + ", elemType = " + elemType
                + ", apiOnly = " + apiOnly
                + ", steps = " + steps
                + ", incl = " + incl
                + ", maxSteps = " + maxSteps
                + ", minSize = " + minSize
                + ", maxSize = " + maxSize
                + ", maxCount = " + maxCount
                + ", noCauses = " + noCauses
                + ", noStats = " + noStats
                + ", onlyStats = " + onlyStats
                + ", dotFormat = " + dotFormat
                + ", toFilePath = " + toFilePath
                + "]";
    }
    
    /**
     * @param args Args from command line.
     * @param stream Stream to report errors or usage.
     * @return A valid command corresponding to the specified arguments,
     *         or null if the command was invalid.
     */
    public static JdcmCommand newValidCommandElseNull(
            String[] args,
            PrintStream stream) {

        JdcmCommand cmd = new JdcmCommand();

        int i = 0;
        while (i < args.length) {
            final String arg = args[i++];
            if (arg.startsWith("-")) {
                final String option = arg.substring(1);

                i = processOption(
                        args,
                        i,
                        cmd,
                        option,
                        stream);
                if (i < 0) {
                    return null;
                }
            } else {
                cmd.toParseList.add(arg);
            }
        }

        if (cmd.toParseList.size() == 0) {
            // Nothing to parse.
            stream.println("ERROR: nothing to parse");
            return null;
        }

        if (cmd.compType == null) {
            // Nothing to compute.
            stream.println("ERROR: nothing to compute");
            return null;
        }

        if (cmd.noStats && cmd.onlyStats) {
            stream.println("ERROR: -nostats and -onlystats are incompatible");
            return null;
        }

        if (cmd.onlyStats && cmd.dotFormat) {
            stream.println("ERROR: -onlystats and -dotformat are incompatible");
            return null;
        }

        /*
         * 
         */
        
        if ((cmd.intoRegex != null)
                && (cmd.compType != JdcmCompType.DEPSOF)
                && (cmd.compType != JdcmCompType.GDEPSOF)) {
            printErrorOptionIncompatibleWithComputation(cmd, "into", stream);
            return null;
        }

        if ((cmd.fromRegex != null)
                && (cmd.compType != JdcmCompType.DEPSTO)
                && (cmd.compType != JdcmCompType.GDEPSTO)) {
            printErrorOptionIncompatibleWithComputation(cmd, "from", stream);
            return null;
        }

        if ((cmd.minusOfRegex != null)
                && (cmd.compType != JdcmCompType.DEPSOF)
                && (cmd.compType != JdcmCompType.GDEPSOF)) {
            printErrorOptionIncompatibleWithComputation(cmd, "minusof", stream);
            return null;
        }

        if ((cmd.minusToRegex != null)
                && (cmd.compType != JdcmCompType.DEPSTO)
                && (cmd.compType != JdcmCompType.GDEPSTO)) {
            printErrorOptionIncompatibleWithComputation(cmd, "minusto", stream);
            return null;
        }

        if (cmd.incl
                && (cmd.compType != JdcmCompType.DEPSOF)
                && (cmd.compType != JdcmCompType.DEPSTO)
                && (cmd.compType != JdcmCompType.GDEPSOF)
                && (cmd.compType != JdcmCompType.GDEPSTO)) {
            printErrorOptionIncompatibleWithComputation(cmd, "incl", stream);
            return null;
        }

        if (cmd.steps
                && (cmd.compType != JdcmCompType.DEPSOF)
                && (cmd.compType != JdcmCompType.DEPSTO)
                && (cmd.compType != JdcmCompType.GDEPSOF)
                && (cmd.compType != JdcmCompType.GDEPSTO)
                && (cmd.compType != JdcmCompType.PATHSG)) {
            printErrorOptionIncompatibleWithComputation(cmd, "steps", stream);
            return null;
        }

        if ((cmd.maxSteps != null)
                && (cmd.compType != JdcmCompType.DEPSOF)
                && (cmd.compType != JdcmCompType.DEPSTO)
                && (cmd.compType != JdcmCompType.GDEPSOF)
                && (cmd.compType != JdcmCompType.GDEPSTO)
                && (cmd.compType != JdcmCompType.PATHSG)) {
            printErrorOptionIncompatibleWithComputation(cmd, "maxsteps", stream);
            return null;
        }

        if ((cmd.minSize != null)
                && (cmd.compType != JdcmCompType.SCCS)
                && (cmd.compType != JdcmCompType.CYCLES)
                && (cmd.compType != JdcmCompType.SCYCLES)
                && (cmd.compType != JdcmCompType.SOMECYCLES)) {
            printErrorOptionIncompatibleWithComputation(cmd, "minsize", stream);
            return null;
        }

        if ((cmd.maxSize != null)
                && (cmd.compType != JdcmCompType.SCCS)
                && (cmd.compType != JdcmCompType.CYCLES)
                && (cmd.compType != JdcmCompType.SCYCLES)
                && (cmd.compType != JdcmCompType.SOMECYCLES)) {
            printErrorOptionIncompatibleWithComputation(cmd, "maxsize", stream);
            return null;
        }

        if ((cmd.maxCount != null)
                && (cmd.compType != JdcmCompType.SCCS)
                && (cmd.compType != JdcmCompType.CYCLES)
                && (cmd.compType != JdcmCompType.SCYCLES)
                && (cmd.compType != JdcmCompType.SOMECYCLES)) {
            printErrorOptionIncompatibleWithComputation(cmd, "maxcount", stream);
            return null;
        }

        if (cmd.noCauses
                && (cmd.compType != JdcmCompType.GDEPSOF)
                && (cmd.compType != JdcmCompType.GDEPSTO)
                && (cmd.compType != JdcmCompType.SPATH)
                && (cmd.compType != JdcmCompType.PATHSG)
                && (cmd.compType != JdcmCompType.CYCLES)
                && (cmd.compType != JdcmCompType.SCYCLES)
                && (cmd.compType != JdcmCompType.SOMECYCLES)) {
            printErrorOptionIncompatibleWithComputation(cmd, "nocauses", stream);
            return null;
        }

        if (cmd.dotFormat
                && (cmd.compType != JdcmCompType.GDEPSOF)
                && (cmd.compType != JdcmCompType.GDEPSTO)
                && (cmd.compType != JdcmCompType.SPATH)
                && (cmd.compType != JdcmCompType.PATHSG)
                && (cmd.compType != JdcmCompType.CYCLES)
                && (cmd.compType != JdcmCompType.SCYCLES)
                && (cmd.compType != JdcmCompType.SOMECYCLES)) {
            printErrorOptionIncompatibleWithComputation(cmd, "dotformat", stream);
            return null;
        }
        
        /*
         * 
         */

        if (cmd.maxSteps == null) {
            // No limit.
            cmd.maxSteps = -1;
        }
        
        if (cmd.minSize == null) {
            // Getting rid of null.
            cmd.minSize = 0;
        }
        
        if (cmd.maxSize == null) {
            // No limit.
            cmd.maxSize = -1;
        }
        
        if (cmd.maxCount == null) {
            // No limit.
            cmd.maxCount = -1L;
        }
        
        return cmd;
    }

    /**
     * @param jadecyMainClassName Passed as argument to avoid cyclic dependency
     *        (and to avoid hard-coding it).
     */
    public static void printUsage(
            String jadecyMainClassName,
            PrintStream stream) {
        StringBuilder sb = new StringBuilder();
        appendLine(sb, "Usage: java -cp " + JAR_FILE_NAME + " " + jadecyMainClassName + " <to_parse>...");
        /*
         * Parsing options.
         */
        appendLine(sb, "       [-regex <parseregex>] [-nomerge]");
        appendLine(sb, "       [-packages] [-apionly]");
        /*
         * Computations and their options.
         */
        appendLine(sb, "       [-depsof <ofregex> [-into <intoregex>] [-minusof <minusofregex>] [-incl] [-steps] [-maxsteps <signed_int_32>]]");
        appendLine(sb, "       [-depsto <toregex> [-from <fromregex>] [-minusto <minustoregex>] [-incl] [-steps] [-maxsteps <signed_int_32>]]");
        //
        appendLine(sb, "       [-gdepsof <ofregex> [-into <intoregex>] [-minusof <minusofregex>] [-incl] [-steps] [-maxsteps <signed_int_32>]]");
        appendLine(sb, "       [-gdepsto <toregex> [-from <fromregex>] [-minusto <minustoregex>] [-incl] [-steps] [-maxsteps <signed_int_32>]]");
        //
        appendLine(sb, "       [-spath <beginregex> <endregex>]");
        appendLine(sb, "       [-pathsg <beginregex> <endregex> [-maxsteps <signed_int_32>]]");
        //
        appendLine(sb, "       [-sccs [-minsize <signed_int_32>] [-maxsize <signed_int_32>] [-maxcount <signed_int_64>]]");
        appendLine(sb, "       [-cycles [-minsize <signed_int_32>] [-maxsize <signed_int_32>] [-maxcount <signed_int_64>]]");
        appendLine(sb, "       [-scycles [-minsize <signed_int_32>] [-maxsize <signed_int_32>] [-maxcount <signed_int_64>]]");
        appendLine(sb, "       [-somecycles [-minsize <signed_int_32>] [-maxsize <signed_int_32>] [-maxcount <signed_int_64>]]");
        /*
         * Output options.
         */
        appendLine(sb, "       [-nocauses]");
        appendLine(sb, "       [-nostats]");
        appendLine(sb, "       [-onlystats]");
        appendLine(sb, "       [-dotformat]");
        appendLine(sb, "       [-tofile <file_path>]");

        appendLine(sb, "");

        appendLine(sb, "       Computations are exclusive, for example can't use -depsof");
        appendLine(sb, "       and -cycles together. Must have at least one computation.");

        appendLine(sb, "");

        appendLine(sb, "       Note that in regular expressions for classes or packages names,");
        appendLine(sb, "       dots should be escaped else they match any character (which might");
        appendLine(sb, "       often not hurt though).");

        appendLine(sb, "");

        appendLine(sb, "    <to_parse>...:");
        appendLine(sb, "        Paths of class files or jar files to parse, or of directories");
        appendLine(sb, "        containing them. Exploration of directories is recursive.");

        appendLine(sb, "");
        appendLine(sb, "  Parsing options:");
        appendLine(sb, "");

        appendLine(sb, "    -regex <parseregex>:");
        appendLine(sb, "        Classes which names don't match <parseregex> are not parsed");
        appendLine(sb, "        (but dependencies to them can still be considered).");
        appendLine(sb, "    -nomerge:");
        appendLine(sb, "        Does not identify nested classes with their top level class.");
        appendLine(sb, "        Allows to show more details, possibly at the cost of much noise.");
        appendLine(sb, "    -apionly:");
        appendLine(sb, "        Only considers API dependencies, i.e. public or protected");
        appendLine(sb, "        fields and methods.");
        
        appendLine(sb, "");
        appendLine(sb, "  Context options:");
        appendLine(sb, "");
        
        appendLine(sb, "    -packages:");
        appendLine(sb, "        Works on packages dependencies graph, instead of classes one.");

        appendLine(sb, "");
        appendLine(sb, "  Computations:");
        appendLine(sb, "");

        appendLine(sb, "    -depsof <ofregex>:");
        appendLine(sb, "        Computes elements depended on by <ofregex> matches, in bulk, with byte size.");
        appendLine(sb, "    -depsto <toregex>:");
        appendLine(sb, "        Computes elements depending on <toregex> matches, in bulk, with byte size.");

        appendLine(sb, "");

        appendLine(sb, "    -gdepsof <ofregex>:");
        appendLine(sb, "        Computes elements depended on by <ofregex> matches, and their successors,");
        appendLine(sb, "        as a graph, with classes causing the dependencies if -packages option");
        appendLine(sb, "        is used and -dotformat is not.");
        appendLine(sb, "    -gdepsto <toregex>:");
        appendLine(sb, "        Computes elements depending on <toregex> matches, and their predecessors,");
        appendLine(sb, "        as a graph, with classes causing the dependencies if -packages option");
        appendLine(sb, "        is used and -dotformat is not.");
        
        appendLine(sb, "");

        appendLine(sb, "    -spath <beginregex> <endregex>:");
        appendLine(sb, "        Computes one shortest path from <beginregex> matches to <endregex> matches,");
        appendLine(sb, "        as a graph, with classes causing dependencies if -packages option is used");
        appendLine(sb, "        and -dotformat is not.");
        appendLine(sb, "    -pathsg <beginregex> <endregex>:");
        appendLine(sb, "        Computes the intersection of vertices reachable from <beginregex> matches,");
        appendLine(sb, "        with vertices from which <endregex> matches are reachable, with edges between");
        appendLine(sb, "        them (no dangling edge), and classes causing the dependencies if -packages");
        appendLine(sb, "        option is used and -dotformat is not.");
        appendLine(sb, "        Each exploration path stops whenever reaching an <endregex> match.");
        appendLine(sb, "        Affected by -maxsteps option, which limits the number of exploratory steps.");

        appendLine(sb, "");
        
        appendLine(sb, "    -sccs:");
        appendLine(sb, "        Computes strongly connected components, in bulk, with byte size.");
        appendLine(sb, "    -cycles:");
        appendLine(sb, "        Computes all cycles, with classes causing dependencies if -packages option is used");
        appendLine(sb, "        and -dotformat is not.");
        appendLine(sb, "        For highly tangled code, can take ages and compute huges amounts of cycles,");
        appendLine(sb, "        such as you might want to use -scycles instead in practice.");
        appendLine(sb, "    -scycles:");
        appendLine(sb, "        Computes a set of cycles that cover all dependencies of each SCC, doing best");
        appendLine(sb, "        effort in making this set and these cycles as small as possible, with classes");
        appendLine(sb, "        causing dependencies if -packages option is used and -dotformat is not.");
        appendLine(sb, "    -somecycles:");
        appendLine(sb, "        Similar to -cycles, but typically only computes some cycles, quickly,");
        appendLine(sb, "        without spending ages in tangled code dependencies, and finds none only");
        appendLine(sb, "        if there is none.");
        appendLine(sb, "        Mostly pointless in practice since the introduction of -scycles.");
        
        appendLine(sb, "");
        appendLine(sb, "  Options for -depsof and -gdepsof computations only:");
        appendLine(sb, "");

        appendLine(sb, "    -into <intoregex>:");
        appendLine(sb, "        For -depsof, causes any element not matching <intoregex> to be finally removed,");
        appendLine(sb, "        except if it matches <ofregex> and -incl option is used.");
        appendLine(sb, "        For -gdepsof, causes any successor not matching <intoregex> to be finally removed,");
        appendLine(sb, "        even if it matches <ofregex> and -incl option is used.");
        appendLine(sb, "    -minusof <minusofregex>:");
        appendLine(sb, "        Removes <minusofregex> matches and elements they depend on from computed elements,");
        appendLine(sb, "        except those included due to -incl option.");
        appendLine(sb, "        For -gdepsof, successors are not affected, i.e. only matching source elements are");
        appendLine(sb, "        removed, so as to allow user to compute unstripped differential dependencies.");
        
        appendLine(sb, "");
        appendLine(sb, "  Options for -depsto and -gdepsto computations only:");
        appendLine(sb, "");
        
        appendLine(sb, "    -from <fromregex>:");
        appendLine(sb, "        For -depsto, causes any element not matching <fromregex> to be finally removed,");
        appendLine(sb, "        except if it matches <toregex> and -incl option is used.");
        appendLine(sb, "        For -gdepsto, causes any predecessor not matching <fromregex> to be finally removed,");
        appendLine(sb, "        even if it matches <toregex> and -incl option is used.");
        appendLine(sb, "    -minusto <minustoregex>:");
        appendLine(sb, "        Removes <minustoregex> matches and elements depending on them from computed elements,");
        appendLine(sb, "        except those included due to -incl option.");
        appendLine(sb, "        For -gdepsto, predecesors are not affected, i.e. only matching destination elements are");
        appendLine(sb, "        removed, so as to allow user to compute unstripped differential dependencies.");
        
        appendLine(sb, "");
        appendLine(sb, "  Options for -depsof, -gdepsof, -depsto and -gdepsto computations only:");
        appendLine(sb, "");
        
        appendLine(sb, "    -incl:");
        appendLine(sb, "        For -depsof and -gdepsof, causes any <ofregex> match to be included in the computed");
        appendLine(sb, "        elements, even if no other <ofregex> match depends on it.");
        appendLine(sb, "        For -depsto and -gdepsto, causes any <toregex> match to be included in the computed");
        appendLine(sb, "        elements, even if it does not depend on any other <toregex> match.");
        appendLine(sb, "        If -steps option is used as well, the set of elements matching <ofregex>");
        appendLine(sb, "        or <toregex> constitutes the first step.");
        appendLine(sb, "    -steps:");
        appendLine(sb, "        Causes output to be presented step-by-step, each step traversing all edges");
        appendLine(sb, "        from previous reach: step 0 before first edges traversal, then step 1, etc.");
        
        appendLine(sb, "");
        appendLine(sb, "  Options for -depsof, -gdepsof, -depsto, -gdepsto and -pathsg computations only:");
        appendLine(sb, "");
        
        appendLine(sb, "    -maxsteps <signed_int_32>:");
        appendLine(sb, "        A signed 32 bits integer in decimal, being max number of steps, i.e.");
        appendLine(sb, "        max number of edge traversal for each exploratory path.");
        appendLine(sb, "        A negative value corresponds to no limit.");
        
        appendLine(sb, "");
        appendLine(sb, "  Options for -sccs, -cycles, -scycles and -somecycles computations only:");
        appendLine(sb, "");
        
        appendLine(sb, "    -minsize <signed_int_32>:");
        appendLine(sb, "        A signed 32 bits integer in decimal, being min size of SCCs for -sccs");
        appendLine(sb, "        computation, and min size of cycles for -cycles, -scycles and -somecycles");
        appendLine(sb, "        computations.");

        appendLine(sb, "    -maxsize <signed_int_32>:");
        appendLine(sb, "        A signed 32 bits integer in decimal, being max size of SCCs for -sccs");
        appendLine(sb, "        computation, and max size of cycles for -cycles, -scycles and -somecycles");
        appendLine(sb, "        computations.");
        appendLine(sb, "        A negative value corresponds to no limit.");

        appendLine(sb, "    -maxcount <signed_int_64>:");
        appendLine(sb, "        A signed 64 bits integer in decimal, being max number of SCCs for -sccs");
        appendLine(sb, "        computation, and max number of cycles for -cycles, -scycles and -somecycles");
        appendLine(sb, "        computations.");
        appendLine(sb, "        A negative value corresponds to no limit.");

        appendLine(sb, "");
        appendLine(sb, "  Output options:");
        appendLine(sb, "");

        appendLine(sb, "    -nocauses:");
        appendLine(sb, "        Incompatible with -depsof, -depsto and -sccs computations,");
        appendLine(sb, "        and with -dotformat option.");
        appendLine(sb, "        Causes dependencies causes, i.e. classes causing dependencies between");
        appendLine(sb, "        packages when -packages option is used, not to be shown.");
        appendLine(sb, "        Has no effect but is still accepted when -packages option is not used.");
        appendLine(sb, "    -nostats");
        appendLine(sb, "        Does not output stats about the result, only the actual result.");
        appendLine(sb, "        Incompatible with -onlystats option.");
        appendLine(sb, "    -onlystats");
        appendLine(sb, "        Does not output the actual result, only stats about the result.");
        appendLine(sb, "        Incompatible with -nostats and -dotformat option.");
        appendLine(sb, "    -dotformat:");
        appendLine(sb, "        Incompatible with -depsof, -depsto and -sccs computations,");
        appendLine(sb, "        and with -onlystats option.");
        appendLine(sb, "        Causes the output to be in DOT format, and without stats.");
        appendLine(sb, "        For -gdepsof and -gdepsto computations with -steps option, there are as many");
        appendLine(sb, "        digraphs as steps, the name of each digraph being step_<stepId>, <stepId>");
        appendLine(sb, "        starting at 0 (for no edge traversal).");
        appendLine(sb, "        For -gdepsof and -gdepsto computations without -steps option, and for -spath");
        appendLine(sb, "        and -pathsg computations, there is only one digraph, named allsteps.");
        appendLine(sb, "        For -cycles, -scycles and -somecycles computations, there is one digraph per");
        appendLine(sb, "        cycle, named cycle_<cycleNum>, <cycleNum> starting at 1.");
        appendLine(sb, "    -tofile <file_path>:");
        appendLine(sb, "        Causes the output to be put into the specified file, which is deleted");
        appendLine(sb, "        if it already exists. If cannot delete or create the file, outputs some");
        appendLine(sb, "        error.");

        stream.println(sb.toString());
    }
    
    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    /**
     * @return New value for i, or -1 if detected a usage error.
     */
    private static int processOption(
            String[] args,
            int i,
            JdcmCommand command,
            String option,
            PrintStream stream) {

        final int bad = -1;

        if (option.equals("regex")) {
            if (command.parseRegex != null) {
                printErrorDuplication(option, stream);
                return bad;
            }
            if (i >= args.length) {
                printErrorNotEnoughArgs(option, 1, stream);
                return bad;
            }
            command.parseRegex = args[i++];
            
        } else if (option.equals("nomerge")) {
            if (command.noMerge) {
                printErrorDuplication(option, stream);
                return bad;
            }
            command.noMerge = true;
            
        } else if (option.equals("apionly")) {
            if (command.apiOnly) {
                printErrorDuplication(option, stream);
                return bad;
            }
            command.apiOnly = true;

        } else if (option.equals("packages")) {
            if (command.elemType == ElemType.PACKAGE) {
                printErrorDuplication(option, stream);
                return bad;
            }
            command.elemType = ElemType.PACKAGE;
            
            /*
             * 
             */

        } else if (option.equals("depsof")) {
            if (command.compType != null) {
                printErrorTwoComputations(command, option, stream);
                return bad;
            }
            if (i >= args.length) {
                printErrorNotEnoughArgs(option, 1, stream);
                return bad;
            }
            command.compType = JdcmCompType.DEPSOF;
            command.ofRegex = args[i++];

        } else if (option.equals("depsto")) {
            if (command.compType != null) {
                printErrorTwoComputations(command, option, stream);
                return bad;
            }
            if (i >= args.length) {
                printErrorNotEnoughArgs(option, 1, stream);
                return bad;
            }
            command.compType = JdcmCompType.DEPSTO;
            command.toRegex = args[i++];
            
        } else if (option.equals("gdepsof")) {
            if (command.compType != null) {
                printErrorTwoComputations(command, option, stream);
                return bad;
            }
            if (i >= args.length) {
                printErrorNotEnoughArgs(option, 1, stream);
                return bad;
            }
            command.compType = JdcmCompType.GDEPSOF;
            command.ofRegex = args[i++];
            
        } else if (option.equals("gdepsto")) {
            if (command.compType != null) {
                printErrorTwoComputations(command, option, stream);
                return bad;
            }
            if (i >= args.length) {
                printErrorNotEnoughArgs(option, 1, stream);
                return bad;
            }
            command.compType = JdcmCompType.GDEPSTO;
            command.toRegex = args[i++];

        } else if (option.equals("spath")) {
            if (command.compType != null) {
                printErrorTwoComputations(command, option, stream);
                return bad;
            }
            if (i-1 >= args.length) {
                printErrorNotEnoughArgs(option, 2, stream);
                return bad;
            }
            command.compType = JdcmCompType.SPATH;
            command.beginRegex = args[i++];
            command.endRegex = args[i++];

        } else if (option.equals("pathsg")) {
            if (command.compType != null) {
                printErrorTwoComputations(command, option, stream);
                return bad;
            }
            if (i-1 >= args.length) {
                printErrorNotEnoughArgs(option, 2, stream);
                return bad;
            }
            command.compType = JdcmCompType.PATHSG;
            command.beginRegex = args[i++];
            command.endRegex = args[i++];

        } else if (option.equals("sccs")) {
            if (command.compType != null) {
                printErrorTwoComputations(command, option, stream);
                return bad;
            }
            command.compType = JdcmCompType.SCCS;
            
        } else if (option.equals("cycles")) {
            if (command.compType != null) {
                printErrorTwoComputations(command, option, stream);
                return bad;
            }
            command.compType = JdcmCompType.CYCLES;
            
        } else if (option.equals("scycles")) {
            if (command.compType != null) {
                printErrorTwoComputations(command, option, stream);
                return bad;
            }
            command.compType = JdcmCompType.SCYCLES;
            
        } else if (option.equals("somecycles")) {
            if (command.compType != null) {
                printErrorTwoComputations(command, option, stream);
                return bad;
            }
            command.compType = JdcmCompType.SOMECYCLES;
            
            /*
             * 
             */
            
        } else if (option.equals("into")) {
            if (command.intoRegex != null) {
                printErrorDuplication(option, stream);
                return bad;
            }
            if (i >= args.length) {
                printErrorNotEnoughArgs(option, 1, stream);
                return bad;
            }
            command.intoRegex = args[i++];

        } else if (option.equals("minusof")) {
            if (command.minusOfRegex != null) {
                printErrorDuplication(option, stream);
                return bad;
            }
            if (i >= args.length) {
                printErrorNotEnoughArgs(option, 1, stream);
                return bad;
            }
            command.minusOfRegex = args[i++];
            
            /*
             * 
             */
            
        } else if (option.equals("from")) {
            if (command.fromRegex != null) {
                printErrorDuplication(option, stream);
                return bad;
            }
            if (i >= args.length) {
                printErrorNotEnoughArgs(option, 1, stream);
                return bad;
            }
            command.fromRegex = args[i++];

        } else if (option.equals("minusto")) {
            if (command.minusToRegex != null) {
                printErrorDuplication(option, stream);
                return bad;
            }
            if (i >= args.length) {
                printErrorNotEnoughArgs(option, 1, stream);
                return bad;
            }
            command.minusToRegex = args[i++];
            
            /*
             * 
             */
            
        } else if (option.equals("incl")) {
            if (command.incl) {
                printErrorDuplication(option, stream);
                return bad;
            }
            command.incl = true;

        } else if (option.equals("steps")) {
            if (command.steps) {
                printErrorDuplication(option, stream);
                return bad;
            }
            command.steps = true;
            
            /*
             * 
             */
            
        } else if (option.equals("maxsteps")) {
            if (command.maxSteps != null) {
                printErrorDuplication(option, stream);
                return bad;
            }
            if (i >= args.length) {
                printErrorNotEnoughArgs(option, 1, stream);
                return bad;
            }
            final String intString = args[i++];
            try {
                command.maxSteps = Integer.valueOf(intString);
            } catch (NumberFormatException e) {
                stream.println("ERROR: " + e.getClass().getSimpleName() + " for " + intString);
                return bad;
            }
            
            /*
             * 
             */

        } else if (option.equals("minsize")) {
            if (command.minSize != null) {
                printErrorDuplication(option, stream);
                return bad;
            }
            if (i >= args.length) {
                printErrorNotEnoughArgs(option, 1, stream);
                return bad;
            }
            final String intString = args[i++];
            try {
                command.minSize = Integer.valueOf(intString);
            } catch (NumberFormatException e) {
                stream.println("ERROR: " + e.getClass().getSimpleName() + " for " + intString);
                return bad;
            }
            
        } else if (option.equals("maxsize")) {
            if (command.maxSize != null) {
                printErrorDuplication(option, stream);
                return bad;
            }
            if (i >= args.length) {
                printErrorNotEnoughArgs(option, 1, stream);
                return bad;
            }
            final String intString = args[i++];
            try {
                command.maxSize = Integer.valueOf(intString);
            } catch (NumberFormatException e) {
                stream.println("ERROR: " + e.getClass().getSimpleName() + " for " + intString);
                return bad;
            }
            
        } else if (option.equals("maxcount")) {
            if (command.maxCount != null) {
                printErrorDuplication(option, stream);
                return bad;
            }
            if (i >= args.length) {
                printErrorNotEnoughArgs(option, 1, stream);
                return bad;
            }
            final String intString = args[i++];
            try {
                command.maxCount = Long.valueOf(intString);
            } catch (NumberFormatException e) {
                stream.println("ERROR: " + e.getClass().getSimpleName() + " for " + intString);
                return bad;
            }

            /*
             * 
             */
            
        } else if (option.equals("nocauses")) {
            if (command.noCauses) {
                printErrorDuplication(option, stream);
                return bad;
            }
            command.noCauses = true;

        } else if (option.equals("nostats")) {
            if (command.noStats) {
                printErrorDuplication(option, stream);
                return bad;
            }
            command.noStats = true;

        } else if (option.equals("onlystats")) {
            if (command.onlyStats) {
                printErrorDuplication(option, stream);
                return bad;
            }
            command.onlyStats = true;

        } else if (option.equals("dotformat")) {
            if (command.dotFormat) {
                printErrorDuplication(option, stream);
                return bad;
            }
            command.dotFormat = true;
            
        } else if (option.equals("tofile")) {
            if (command.toFilePath != null) {
                printErrorDuplication(option, stream);
                return bad;
            }
            if (i >= args.length) {
                printErrorNotEnoughArgs(option, 1, stream);
                return bad;
            }
            command.toFilePath = args[i++];

        } else {
            printErrorUnrecognizedOption(option, stream);
            return bad;
        }

        return i;
    }

    private static void printErrorUnrecognizedOption(
            String option,
            PrintStream stream) {
        stream.println("ERROR: unrecognized option: -" + option);
    }
    
    private static void printErrorTwoComputations(
            JdcmCommand command,
            String option,
            PrintStream stream) {
        stream.println("ERROR: two computations: -" + command.compType.name().toLowerCase() + " and -" + option);
    }
    
    private static void printErrorDuplication(
            String option,
            PrintStream stream) {
        stream.println("ERROR: -" + option + " duplication");
    }

    private static void printErrorNotEnoughArgs(
            String option,
            int nbrRequired,
            PrintStream stream) {
        stream.println("ERROR: need at least " + nbrRequired + " arg(s) after -" + option);
    }
    
    private static void printErrorOptionIncompatibleWithComputation(
            JdcmCommand command,
            String option,
            PrintStream stream) {
        stream.println("ERROR: -" + option + " is incompatible with -" + command.compType.name().toLowerCase());
    }
    
    /*
     * 
     */

    private static void appendLine(StringBuilder sb, String line) {
        sb.append(line);
        sb.append("\n");
    }
}
