/**
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
package net.jadecy;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import net.jadecy.code.NameUtils;
import net.jadecy.utils.ArgsUtils;

/**
 * Utilities to deal with Jadecy class, in particular with computations results,
 * methods being provided to simplify them, to compute statistics out of them,
 * or to print them.
 * 
 * As a general rule, behavior of these methods is undefined whenever an
 * element within a collection is null.
 */
public class JadecyUtils {

    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------
    
    /**
     * 3 spaces as jdeps does.
     */
    static final String INCREMENT = "   ";

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /*
     * Bulk dependencies reduction.
     */
    
    /**
     * Merges the steps.
     * List<SortedMap<name,byteSize>>
     * becomes SortedMap<name,byteSize>.
     * 
     * @param byteSizeByDepList The list of maps to merge.
     * @return A new mutable map corresponding to the argument but in a single
     *         step.
     * @throws NullPointerException if byteSizeByDepList is null.
     * @throws IllegalArgumentException if equal keys are among the maps.
     */
    public static SortedMap<String,Long> computeDepsMergedFromDepsLm(
            List<SortedMap<String,Long>> byteSizeByDepList) {
        
        final SortedMap<String,Long> reduced =
                new TreeMap<String,Long>();
        
        // Implicit null check.
        for (SortedMap<String,Long> byteSizeByDep : byteSizeByDepList) {
            for (Map.Entry<String,Long> entry : byteSizeByDep.entrySet()) {
                final String name = entry.getKey();
                final Long byteSize = entry.getValue();
                
                // Merging.
                final Object forCheck = reduced.put(name, byteSize);
                if (forCheck != null) {
                    throw new IllegalArgumentException("duplicate mapping for " + name);
                }
            }
        }
        
        return reduced;
    }

    /*
     * Graphs reduction.
     */

    /**
     * Merges the steps.
     * List<SortedMap<name,SortedMap<dep,SortedSet<cause>>>>
     * becomes SortedMap<name,SortedMap<dep,SortedSet<cause>>>.
     * 
     * @param byteSizeByDepList The list of maps to merge.
     * @return A new mutable map corresponding to the argument but in a single
     *         step.
     * @throws NullPointerException if causesByDepByNameList is null.
     * @throws IllegalArgumentException if equal keys are among the primary maps.
     */
    public static SortedMap<String,SortedMap<String,SortedSet<String>>> computeGraphMergedFromGraphLmms(
            List<SortedMap<String,SortedMap<String,SortedSet<String>>>> causesByDepByNameList) {
        
        final SortedMap<String,SortedMap<String,SortedSet<String>>> reduced =
                new TreeMap<String,SortedMap<String,SortedSet<String>>>();
        
        for (SortedMap<String,SortedMap<String,SortedSet<String>>> causesByDepByName : causesByDepByNameList) {
            for (Map.Entry<String,SortedMap<String,SortedSet<String>>> entry : causesByDepByName.entrySet()) {
                final String name = entry.getKey();
                final SortedMap<String,SortedSet<String>> causesByDep = entry.getValue();
                
                // Merging.
                final Object forCheck = reduced.put(name, causesByDep);
                if (forCheck != null) {
                    throw new IllegalArgumentException("duplicate mapping for " + name);
                }
            }
        }
        return reduced;
    }
    
    /**
     * Merges the steps.
     * List<SortedMap<name,SortedSet<dep>>>
     * becomes SortedMap<name,SortedSet<dep>>.
     * 
     * @param depsByNameList The list of maps to merge.
     * @return A new mutable map corresponding to the argument but in a single
     *         step.
     * @throws NullPointerException if depsByNameList is null.
     * @throws IllegalArgumentException if equal keys are among the maps.
     */
    public static SortedMap<String,SortedSet<String>> computeGraphMergedFromGraphLms(
            List<SortedMap<String,SortedSet<String>>> depsByNameList) {
        
        final SortedMap<String,SortedSet<String>> reduced =
                new TreeMap<String,SortedSet<String>>();
        
        for (SortedMap<String,SortedSet<String>> depsByName : depsByNameList) {
            for (Map.Entry<String,SortedSet<String>> entry : depsByName.entrySet()) {
                final String name = entry.getKey();
                
                // Merging.
                final Object forCheck = reduced.put(name, entry.getValue());
                if (forCheck != null) {
                    throw new IllegalArgumentException("duplicate mapping for " + name);
                }
            }
        }
        return reduced;
    }

    /**
     * Removes the causes.
     * List<SortedMap<name,SortedMap<dep,SortedSet<cause>>>>
     * becomes List<SortedMap<name,SortedSet<dep>>>.
     * 
     * @param causesByDepByNameList The list of maps with causes.
     * @return A new mutable list corresponding to the argument but without causes.
     * @throws NullPointerException if causesByDepByNameList is null.
     */
    public static List<SortedMap<String,SortedSet<String>>> computeGraphCauselessFromGraphLmms(
            List<SortedMap<String,SortedMap<String,SortedSet<String>>>> causesByDepByNameList) {
        
        final List<SortedMap<String,SortedSet<String>>> reduced =
                new ArrayList<SortedMap<String,SortedSet<String>>>();
        
        // Implicit null check.
        for (SortedMap<String,SortedMap<String,SortedSet<String>>> causesByDepByName : causesByDepByNameList) {
            // Removing causes.
            final SortedMap<String,SortedSet<String>> withoutCausesMap = computeGraphCauselessFromGraphMms(causesByDepByName);
            reduced.add(withoutCausesMap);
        }
        
        return reduced;
    }

    /**
     * Removes the causes.
     * SortedMap<name,SortedMap<dep,SortedSet<cause>>>
     * becomes SortedMap<name,SortedSet<dep>>.
     * 
     * @param causesByDepByName The map with causes.
     * @return A new mutable map corresponding to the argument but without causes.
     * @throws NullPointerException if causesByDepByName is null.
     */
    public static SortedMap<String,SortedSet<String>> computeGraphCauselessFromGraphMms(
            SortedMap<String,SortedMap<String,SortedSet<String>>> causesByDepByName) {
        
        final SortedMap<String,SortedSet<String>> reduced =
                new TreeMap<String,SortedSet<String>>();
        
        // Implicit null check.
        for (Map.Entry<String,SortedMap<String,SortedSet<String>>> entry : causesByDepByName.entrySet()) {
            final String name = entry.getKey();
            final SortedMap<String,SortedSet<String>> causesByDep = entry.getValue();

            // Removing causes.
            final SortedSet<String> deps = new TreeSet<String>(causesByDep.keySet());

            final Object forCheck = reduced.put(name, deps);
            if (forCheck != null) {
                throw new AssertionError();
            }
        }
        
        return reduced;
    }
    
    /**
     * Merges the steps and removes the causes.
     * List<SortedMap<name,SortedMap<dep,SortedSet<cause>>>>
     * becomes SortedMap<name,SortedSet<dep>>.
     * 
     * @param causesByDepByNameList A list of maps with causes.
     * @return A new mutable map corresponding to the argument but in a single
     *         step and without causes.
     * @throws IllegalArgumentException if equal keys are among the primary maps.
     * @throws NullPointerException if causesByDepByNameList is null.
     */
    public static SortedMap<String,SortedSet<String>> computeGraphMergedAndCauselessFromGraphLmms(
            List<SortedMap<String,SortedMap<String,SortedSet<String>>>> causesByDepByNameList) {
        
        final SortedMap<String,SortedSet<String>> reduced =
                new TreeMap<String,SortedSet<String>>();
        
        // Implicit null check.
        for (SortedMap<String,SortedMap<String,SortedSet<String>>> causesByDepByName : causesByDepByNameList) {
            for (Map.Entry<String,SortedMap<String,SortedSet<String>>> entry : causesByDepByName.entrySet()) {
                final String name = entry.getKey();
                final SortedMap<String,SortedSet<String>> causesByDep = entry.getValue();

                // Removing causes.
                final SortedSet<String> deps = new TreeSet<String>(causesByDep.keySet());

                // Merging.
                final Object forCheck = reduced.put(name, deps);
                if (forCheck != null) {
                    throw new IllegalArgumentException("duplicate mapping for " + name);
                }
            }
        }
        
        return reduced;
    }
    
    /*
     * Paths reduction.
     */

    /**
     * Removes the causes.
     * List<SortedMap<name,SortedSet<depCause>>>>
     * becomes List<name>.
     * 
     * @param depCausesByNameList A list of maps with causes.
     * @return A new mutable map corresponding to the argument but without
     *         causes.
     * @throws IllegalArgumentException if equal keys are among the maps.
     * @throws NullPointerException if causesByNameList is null.
     */
    public static List<String> computePathCauselessLms(
            List<SortedMap<String,SortedSet<String>>> depCausesByNameList) {
        
        final List<String> nameList = new ArrayList<String>();
        
        // Implicit null check.
        final int size = depCausesByNameList.size();
        
        for (int i = 0; i < size; i++) {
            final SortedMap<String,SortedSet<String>> depCausesByName = depCausesByNameList.get(i);
            if (depCausesByName.size() != 1) {
                throw new IllegalArgumentException("map size must be 1, but map is " + depCausesByName);
            }
            final String name = depCausesByName.firstKey();
            if (i == size - 1) {
                final SortedSet<String> depCauses = depCausesByName.get(name);
                if (depCauses.size() != 0) {
                    throw new IllegalArgumentException("last causes set must be empty, but is " + depCauses);
                }
            }
            nameList.add(name);
        }
        
        return nameList;
    }

    /*
     * Paths to graphs.
     */
    
    /**
     * Computes the graph form of a path.
     * 
     * @param nameList A list of names, each element depending on the next one.
     * @return A new mutable map corresponding to the argument but as a graph.
     * @throws NullPointerException if nameList is null.
     */
    public static SortedMap<String,SortedSet<String>> computeGraphFromPathL(
            List<String> nameList) {
        
        final SortedMap<String,SortedSet<String>> depsByName =
                new TreeMap<String,SortedSet<String>>();
        
        // Implicit null check.
        final int size = nameList.size();
        
        for (int i = 0; i < size; i++) {
            final String name = nameList.get(i);
            
            SortedSet<String> deps = depsByName.get(name);
            if (deps == null) {
                deps = new TreeSet<String>();
                depsByName.put(name, deps);
            }
            
            if (i < size - 1) {
                final String dep = nameList.get(i+1);
                deps.add(dep);
            }
        }
        
        return depsByName;
    }

    /*
     * Cycles to graphs.
     */

    /**
     * Computes the graph form of a cycle with causes.
     * 
     * @param names An array of names, each element depending on the next one,
     *        and last one depending on first one.
     * @param causesArr The corresponding array of dependencies causes.
     *        Each array contains the causes of dependency from name of same
     *        index to the next one in cycle.
     *        Can be null, but if is not null, contained arrays must not be null
     *        nor contain nulls.
     * @return A new mutable map corresponding to the argument but as a graph.
     * @throws NullPointerException if names is null or contains nulls.
     */
    public static SortedMap<String,SortedMap<String,SortedSet<String>>> computeGraphFromCycle(
            String[] names,
            String[][] causesArr) {
        
        ArgsUtils.requireNonNull2(names);
        
        final SortedMap<String,SortedMap<String,SortedSet<String>>> causesByDepByName =
                new TreeMap<String,SortedMap<String,SortedSet<String>>>();
        
        for (int i = 0; i < names.length; i++) {
            final String name = names[i];

            SortedMap<String,SortedSet<String>> causesByDep = causesByDepByName.get(name);
            if (causesByDep == null) {
                causesByDep = new TreeMap<String,SortedSet<String>>();
                causesByDepByName.put(name, causesByDep);
            }
            
            final int depIndex;
            if (i < names.length - 1) {
                depIndex = i + 1;
            } else {
                depIndex = 0;
            }
            final String dep = names[depIndex];
            
            SortedSet<String> causes = causesByDep.get(dep);
            if (causes == null) {
                causes = new TreeSet<String>();
                causesByDep.put(dep, causes);
            }
            
            if (causesArr != null) {
                // Causes of dependency from name to dep.
                final String[] causeArr = causesArr[i];
                for (String cause : causeArr) {
                    causes.add(cause);
                }
            }
        }
        
        return causesByDepByName;
    }

    /**
     * Computes the graph form of a cycle.
     * 
     * @param names An array of names, each element depending on the next one,
     *        and last one depending on first one.
     * @return A new mutable map corresponding to the argument but as a graph.
     * @throws NullPointerException if names is null or contains nulls.
     */
    public static SortedMap<String,SortedSet<String>> computeGraphFromCycle(
            String[] names) {
        
        ArgsUtils.requireNonNull2(names);
        
        final SortedMap<String,SortedSet<String>> depsByName =
                new TreeMap<String,SortedSet<String>>();
        
        for (int i = 0; i < names.length; i++) {
            final String name = names[i];

            SortedSet<String> deps = depsByName.get(name);
            if (deps == null) {
                deps = new TreeSet<String>();
                depsByName.put(name, deps);
            }
            
            final int depIndex;
            if (i < names.length - 1) {
                depIndex = i + 1;
            } else {
                depIndex = 0;
            }
            final String dep = names[depIndex];
            
            deps.add(dep);
        }
        
        return depsByName;
    }
    
    /*
     * Bulk dependencies stats.
     */

    /**
     * Computes total byte size.
     * 
     * @param byteSizeByName Map of byte size by name.
     * @return The total byte size.
     * @throws NullPointerException if byteSizeByName is null.
     */
    public static long computeByteSizeM(
            SortedMap<String,Long> byteSizeByName) {
        
        long totalByteSize = 0;
        // Implicit null check.
        for (Long byteSize : byteSizeByName.values()) {
            totalByteSize += byteSize.longValue();
        }
        
        return totalByteSize;
    }

    /**
     * Computes total byte size.
     * 
     * @param byteSizeByNameList A list of maps of byte size by name.
     * @return The total byte size.
     * @throws NullPointerException if byteSizeByNameList is null.
     * @throws IllegalArgumentException if equal keys are among the maps.
     */
    public static long computeByteSizeLm(
            List<SortedMap<String,Long>> byteSizeByNameList) {
        
        final Set<String> unicitySet = new HashSet<String>();
        
        long totalByteSize = 0;
        // Implicit null check.
        for (SortedMap<String,Long> byteSizeByName : byteSizeByNameList) {
            for (String name : byteSizeByName.keySet()) {
                if (!unicitySet.add(name)) {
                    throw new IllegalArgumentException("duplicate mapping for " + name);
                }
            }
            totalByteSize += computeByteSizeM(byteSizeByName);
        }
        
        return totalByteSize;
    }

    /**
     * Computes the number of maps by map size.
     * 
     * @param byteSizeByNameList A list of maps of byte size by name.
     * @return A new mutable map containing the number of maps by map size.
     * @throws NullPointerException if byteSizeByNameList is null.
     */
    public static SortedMap<Integer,Integer> computeCountBySizeLm(
            List<SortedMap<String,Long>> byteSizeByNameList) {
        
        final TreeMap<Integer, Integer> countBySize = new TreeMap<Integer, Integer>();
        
        // Implicit null check.
        for (SortedMap<String,Long> byteSizeByName : byteSizeByNameList) {
            increment(countBySize, byteSizeByName.size());
        }
        
        return countBySize;
    }

    /**
     * Computes the number of sets by set size.
     * 
     * @param nameSetList A list of sets of names.
     * @return A new mutable map containing the number of sets by set size.
     * @throws NullPointerException if nameSetList is null.
     */
    public static SortedMap<Integer,Integer> computeCountBySizeLs(
            List<SortedSet<String>> nameSetList) {
        
        final TreeMap<Integer, Integer> countBySize = new TreeMap<Integer, Integer>();
        
        // Implicit null check.
        for (SortedSet<String> nameSet : nameSetList) {
            increment(countBySize, nameSet.size());
        }
        
        return countBySize;
    }
    
    /*
     * Names printing.
     */

    /**
     * Prints a collection of names (like a path, or a SCC).
     * 
     * @param nameColl The collection to print.
     * @param stream Stream to print to.
     * @throws NullPointerException if any argument is null.
     */
    public static void printNamesC(
            Collection<String> nameColl,
            PrintStream stream) {
        
        ArgsUtils.requireNonNull(stream);
        
        // Implicit null check.
        for (String name : nameColl) {
            stream.println(name);
        }
    }

    /*
     * Bulk dependencies printing.
     */
    
    /**
     * Prints step-by-step bulk dependencies with byte sizes.
     * 
     * @param byteSizeByNameList The dependencies to print.
     * @param stream Stream to print to.
     * @throws NullPointerException if any argument is null.
     */
    public static void printDepsLm(
            List<SortedMap<String,Long>> byteSizeByNameList,
            PrintStream stream) {
        
        ArgsUtils.requireNonNull(stream);

        int nextStepId = 0;
        // Implicit null check.
        for (SortedMap<String,Long> step : byteSizeByNameList) {
            
            if (nextStepId != 0) {
                stream.println();
            }
            stream.println("step " + (nextStepId++) + ":");

            for (Map.Entry<String,Long> entry : step.entrySet()) {
                final String name = entry.getKey();
                final Long byteSize = entry.getValue();
                stream.println(name + ": " + byteSize);
            }
        }
    }

    /**
     * Prints bulk dependencies with byte sizes.
     * 
     * @param byteSizeByName The dependencies to print.
     * @param stream Stream to print to.
     * @throws NullPointerException if any argument is null.
     */
    public static void printDepsM(
            SortedMap<String,Long> byteSizeByName,
            PrintStream stream) {
        
        ArgsUtils.requireNonNull(stream);

        // Implicit null check.
        for (Map.Entry<String,Long> entry : byteSizeByName.entrySet()) {
            final Long byteSize = entry.getValue();
            final String name = entry.getKey();
            stream.println(name + ": " + byteSize);
        }
    }

    /*
     * Graphs printing.
     */

    /**
     * Prints a step-by-step dependencies graph with dependencies causes.
     * 
     * @param causesByDepByNameList The graph to print.
     * @param mustPrintCauses True if must print causes, false otherwise.
     * @param stream Stream to print to.
     * @throws NullPointerException if any argument is null.
     */
    public static void printGraphLmms(
            List<SortedMap<String,SortedMap<String,SortedSet<String>>>> causesByDepByNameList,
            boolean mustPrintCauses,
            PrintStream stream) {

        ArgsUtils.requireNonNull(stream);

        int nextStepId = 0;
        // Implicit null check.
        for (SortedMap<String,SortedMap<String,SortedSet<String>>> causesByDepByName : causesByDepByNameList) {
            
            if (nextStepId != 0) {
                stream.println();
            }
            stream.println("step " + (nextStepId++) + ":");

            printGraphMms(
                    causesByDepByName,
                    mustPrintCauses,
                    stream);
        }
    }

    /**
     * Prints a dependencies graph with dependencies causes.
     * 
     * @param causesByDepByName The graph to print.
     * @param mustPrintCauses True if must print causes, false otherwise.
     * @param stream Stream to print to.
     * @throws NullPointerException if any argument is null.
     */
    public static void printGraphMms(
            SortedMap<String,SortedMap<String,SortedSet<String>>> causesByDepByName,
            boolean mustPrintCauses,
            PrintStream stream) {
        
        ArgsUtils.requireNonNull(stream);
        
        // Implicit null check.
        for (Map.Entry<String,SortedMap<String,SortedSet<String>>> entry : causesByDepByName.entrySet()) {
            final String name = entry.getKey();
            final SortedMap<String,SortedSet<String>> causesByDep = entry.getValue();

            stream.println(name);

            for (Map.Entry<String,SortedSet<String>> depEntry : causesByDep.entrySet()) {
                final String dep = depEntry.getKey();
                final SortedSet<String> causes = depEntry.getValue();

                if (mustPrintCauses) {
                    for (String cause : causes) {
                        stream.println(INCREMENT + INCREMENT + cause);
                    }
                }
                stream.println(INCREMENT + "-> " + dep);
            }
        }
    }

    /**
     * Prints a step-by-step causeless dependencies graph.
     * 
     * @param depsByNameList The graph to print.
     * @param stream Stream to print to.
     * @throws NullPointerException if any argument is null.
     */
    public static void printGraphLms(
            List<SortedMap<String,SortedSet<String>>> depsByNameList,
            PrintStream stream) {

        ArgsUtils.requireNonNull(stream);

        int nextStepId = 0;
        // Implicit null check.
        for (SortedMap<String,SortedSet<String>> depsByName : depsByNameList) {
            
            if (nextStepId != 0) {
                stream.println();
            }
            stream.println("step " + (nextStepId++) + ":");

            printGraphMs(
                    depsByName,
                    stream);
        }
    }

    /**
     * Prints a causeless dependencies graph.
     * 
     * @param causesByDepByName The graph to print.
     * @param stream Stream to print to.
     * @throws NullPointerException if any argument is null.
     */
    public static void printGraphMs(
            SortedMap<String,SortedSet<String>> depsByName,
            PrintStream stream) {
        
        ArgsUtils.requireNonNull(stream);
        
        // Implicit null check.
        for (Map.Entry<String,SortedSet<String>> entry : depsByName.entrySet()) {
            final String name = entry.getKey();
            final SortedSet<String> deps = entry.getValue();

            stream.println(name);

            for (String dep : deps) {
                stream.println(INCREMENT + "-> " + dep);
            }
        }
    }

    /**
     * Prints a causeless dependencies graph, in DOT format.
     * 
     * Does not pad as jdeps does, for it takes more memory,
     * and is sometimes not enough, which leads to inconsistent layout.
     * 
     * @param depsByName The graph to print.
     * @param stream Stream to print to.
     * @throws NullPointerException if any argument is null.
     */
    public static void printGraphInDOTFormatMs(
            SortedMap<String,SortedSet<String>> depsByName,
            String graphName,
            PrintStream stream) {
        
        ArgsUtils.requireNonNull(depsByName);
        ArgsUtils.requireNonNull(graphName);
        
        // Implicit null check.
        stream.print("digraph ");
        stream.print(NameUtils.quoted(graphName));
        stream.println(" {");
        
        for (Map.Entry<String,SortedSet<String>> entry : depsByName.entrySet()) {
            final String name = entry.getKey();
            final SortedSet<String> deps = entry.getValue();
            
            if (deps.size() != 0) {
                for (String dep : deps) {
                    stream.print(INCREMENT);
                    stream.print(NameUtils.quoted(name));
                    stream.print(" -> ");
                    stream.print(NameUtils.quoted(dep));
                    stream.println(";");
                }
            } else {
                stream.print(INCREMENT);
                stream.print(NameUtils.quoted(name));
                stream.println(";");
            }
        }
        
        stream.println("}");
    }

    /*
     * Paths printing.
     */
    
    /**
     * Prints a path with dependencies causes.
     * 
     * @param depCausesByNameList The path to print.
     * @param mustPrintCauses True if must print causes, false otherwise.
     * @param stream Stream to print to.
     * @throws NullPointerException if any argument is null.
     */
    public static void printPathLms(
            List<SortedMap<String,SortedSet<String>>> depCausesByNameList,
            boolean mustPrintCauses,
            PrintStream stream) {
        
        ArgsUtils.requireNonNull(stream);
        
        // Implicit null check.
        for (SortedMap<String,SortedSet<String>> depCausesByName : depCausesByNameList) {
            final String name = depCausesByName.firstKey();
            
            stream.println(name);

            if (mustPrintCauses) {
                final SortedSet<String> depCauses = depCausesByName.get(name);
                for (String depCause : depCauses) {
                    stream.println(INCREMENT + depCause);
                }
            }
        }
    }

    /*
     * SCCs printing.
     */

    /**
     * Prints a strongly connected component with byte sizes.
     * 
     * @param byteSizeByName The SCC to print.
     * @param stream Stream to print to.
     * @throws NullPointerException if any argument is null.
     */
    public static void printSccS(
            SortedMap<String,Long> byteSizeByName,
            PrintStream stream) {
        
        ArgsUtils.requireNonNull(stream);
        
        // Implicit null check.
        for (Map.Entry<String,Long> entry : byteSizeByName.entrySet()) {
            final String name = entry.getKey();
            final long byteSize = entry.getValue();
            stream.println(name + ": " + byteSize);
        }
    }

    /**
     * Prints a collection of strongly connected components with byte sizes.
     * 
     * @param byteSizeByNameColl The collection of SCCs to print.
     * @param stream Stream to print to.
     * @throws NullPointerException if any argument is null.
     */
    public static void printSccsCs(
            Collection<SortedMap<String,Long>> byteSizeByNameColl,
            PrintStream stream) {
        
        ArgsUtils.requireNonNull(stream);
        
        int sccNum = 0;
        // Implicit null check.
        for (SortedMap<String,Long> byteSizeByName : byteSizeByNameColl) {
            sccNum++;
            
            final long sccByteSize = computeByteSizeM(byteSizeByName);

            if (sccNum != 1) {
                stream.println();
            }
            stream.println("SCC " + sccNum + " (" + sccByteSize + " bytes):");

            printSccS(byteSizeByName, stream);
        }
    }
    
    /*
     * Cycles printing.
     */
    
    /**
     * Prints a cycle, eventually with dependencies causes.
     * 
     * @param names The cycle to print.
     * @param causesArr The array of causes for dependencies from element
     *        of same index in names array. Can be null if no cause, but
     *        if it not null must not contain any null.
     * @param stream Stream to print to.
     * @throws NullPointerException if names or stream is null.
     */
    public static void printCycle(
            String[] names,
            String[][] causesArr,
            PrintStream stream) {
        
        ArgsUtils.requireNonNull(stream);
        
        // Implicit null check.
        for (int i = 0; i < names.length; i++) {
            stream.println(names[i]);
            if (causesArr != null) {
                final String[] causes = causesArr[i];
                for (String cause : causes) {
                    stream.println(INCREMENT + cause);
                }
            }
        }
        stream.println(names[0]);
    }
    
    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------

    private JadecyUtils() {
    }
    
    private static <K> void increment(
            Map<K, Integer> countByKey,
            K key) {
        Integer prev = countByKey.get(key);
        if (prev == null) {
            countByKey.put(key, 1);
        } else {
            countByKey.put(key, prev + 1);
        }
    }
}
