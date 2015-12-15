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
package net.jadecy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import net.jadecy.code.AbstractCodeData;
import net.jadecy.code.CodeDataUtils;
import net.jadecy.code.DerivedTreeComputer;
import net.jadecy.code.InterfaceNameFilter;
import net.jadecy.code.NameFilters;
import net.jadecy.code.PackageData;
import net.jadecy.graph.CyclesComputer;
import net.jadecy.graph.InterfaceVertex;
import net.jadecy.graph.OneShortestPathComputer;
import net.jadecy.graph.PathsGraphComputer;
import net.jadecy.graph.ReachabilityComputer;
import net.jadecy.graph.SccsComputer;
import net.jadecy.graph.SomeCyclesComputer;
import net.jadecy.parsing.FsDepsParser;
import net.jadecy.parsing.InterfaceDepsParser;
import net.jadecy.utils.ArgsUtils;
import net.jadecy.utils.SortUtils;

/**
 * Treatments to compute dependencies, strongly connected components, and
 * cycles, in classes or packages dependencies graphs parsed from class files,
 * and possibly associated byte sizes.
 * 
 * Note that it is possible to have packages cycles, without classes cycles, and
 * vice versa.
 * 
 * As a general rule, for convenience, these treatments ensure determinism and
 * when possible some ordering in their result, and returned collections are
 * mutable.
 * 
 * Results contain the display name (i.e. the displayName()) of corresponding
 * classes or packages, i.e. their actual names like "java.lang" or
 * "java.lang.Math", except for default package for which
 * NameUtils.DEFAULT_PACKAGE_DISPLAY_NAME is used instead of an empty string.
 * 
 * For result types, using maps where we could otherwise use pairs, not to
 * create a specific pair class; for maps and sets, using sorted versions,
 * for convenience (ordering, firstXxx() method) and determinism.
 * 
 * To avoid spam due to dependencies within a same top level class and its
 * recursively nested classes, cycles and SCCs only involving such classes are
 * always ignored.
 * 
 * Configuration parameters:
 * Other than its parser's configuration, instances of this class have two
 * configuration parameters: whether computations must apply on inverse
 * dependencies graphs, and a filter defining retained classes among parsed
 * ones (plus their surrounding classes up to top level, plus their direct
 * and non-inverse dependencies, which presences are forced, as done on
 * parsing).
 * The inverse parameter allows to compute depending classes or packages
 * instead of classes or packages depended on.
 * The filter parameter allows to confine computations to sub sets
 * of parsed data, without having to clear and re-parse each time.
 * 
 * Caching:
 * No caching is done, because default package derivation is quite fast
 * (in the order of 50ms for a tree of thousands of packages and tens of
 * thousands of classes), it keeps code simple (even though it wouldn't be hard
 * to implement, using default package data mod count), and it avoids possible
 * noticeable memory leaks in case of multiple Jadecy instances creations.
 * 
 * Concurrency:
 * Jadecy computations can be executed concurrently with each other, but must
 * not be executed concurrently with parsing or programmatic modifications of
 * parser's default package data tree.
 */
public class Jadecy {
    
    /*
     * Note: dependencies causes array is required to be null if not applicable
     * (i.e. for classes dependencies) for cycle processor, for simplicity and
     * performance reasons since it's the lowest level API for cycles
     * processing, but here, for classes graphs results, we avoid eventual null
     * trouble by using an empty collection as causes.
     */
    
    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------
    
    private final InterfaceDepsParser parser;
    
    /**
     * If true using inverse dependencies. Useful to compute depending elements,
     * not elements depended on.
     */
    private final boolean mustUseInverseDeps;
    
    /**
     * Defines what classes are retained as if being parsed in the used classes
     * or packages graph, i.e. the classes they directly depend on are necessarily
     * included too, which includes surrounding and nested classes.
     * 
     * Allows to compute on various subsets without parsing from scratch
     * each time.
     * 
     * Never null.
     */
    private final InterfaceNameFilter retainedClassNameFilter;
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    /**
     * Creates an instance using a default parser,
     * computing non-inverse dependencies
     * and retaining all parser classes.
     * 
     * @param mustMergeNestedClasses True if must merge dependencies from and to
     *        nested classes into their top level classes, false otherwise.
     * @param apiOnly If true, only takes into account API dependencies
     *        (cf. ClassDepsParser for details).
     */
    public Jadecy(
            boolean mustMergeNestedClasses,
            boolean apiOnly) {
        this(
                new FsDepsParser(
                        mustMergeNestedClasses,
                        apiOnly),
                        //
                        false, // mustUseInverseDeps
                        NameFilters.any()); // retainedClassNameFilter
    }
    
    /**
     * @param parser The parser to use. Must not be null.
     * @param mustUseInverseDeps True if must use inverse dependencies graph,
     *        i.e. the graph to compute depending elements, not elements
     *        depended on.
     * @param retainedClassNameFilter Defines classes retained from the parsed
     *        classes, as if only them had been parsed, except that classes
     *        directly depended on will have a non-zero byte size if they
     *        have actually been parsed. Must not be null.
     *        If not wanting filtering, NameFilters.any() should be used
     *        as it can be recognized and related optimizations be done.
     * @throws NullPointerException if the specified parser or filter is null.
     */
    public Jadecy(
            InterfaceDepsParser parser,
            //
            boolean mustUseInverseDeps,
            InterfaceNameFilter retainedClassNameFilter) {
        this.parser = ArgsUtils.requireNonNull(parser);
        this.mustUseInverseDeps = mustUseInverseDeps;
        this.retainedClassNameFilter = ArgsUtils.requireNonNull(retainedClassNameFilter);
    }
    
    /*
     * 
     */
    
    /**
     * @param mustUseInverseDeps True if must use inverse dependencies graph,
     *        i.e. the graph to compute depending elements, not elements
     *        depended on.
     * @return A new Jadecy instance with the specified configuration,
     *         sharing the same parser than this one, or this instance
     *         if it has the requested configuration.
     */
    public Jadecy withMustUseInverseDeps(boolean mustUseInverseDeps) {
        if (mustUseInverseDeps == this.mustUseInverseDeps) {
            return this;
        }
        return new Jadecy(
                this.parser,
                mustUseInverseDeps,
                this.retainedClassNameFilter);
    }

    /**
     * @param retainedClassNameFilter Defines classes retained from the parsed
     *        classes, as if only them had been parsed, except that classes
     *        directly depended on will have a non-zero byte size if they
     *        have actually been parsed. Must not be null.
     *        If not wanting filtering, NameFilters.any() should be used
     *        as it can be recognized and related optimizations be done.
     * @return A new Jadecy instance with the specified configuration,
     *         sharing the same parser than this one, or this instance
     *         if it has the requested configuration.
     * @throws NullPointerException if the specified filter is null.
     */
    public Jadecy withRetainedClassNameFilter(InterfaceNameFilter retainedClassNameFilter) {
        if (retainedClassNameFilter == this.retainedClassNameFilter) {
            return this;
        }
        return new Jadecy(
                this.parser,
                this.mustUseInverseDeps,
                retainedClassNameFilter);
    }

    /*
     * 
     */
    
    /**
     * @return The parser used by this instance. Is shared among instances
     *         of Jadecy derived through withXXX methods.
     */
    public InterfaceDepsParser parser() {
        return this.parser;
    }
    
    /**
     * @return True if this instance uses inverse dependencies graph,
     *         false otherwise.
     */
    public boolean getMustUseInverseDeps() {
        return this.mustUseInverseDeps;
    }

    /**
     * @return The filter defining classes retained among parsed classes.
     *         Never null.
     */
    public InterfaceNameFilter getRetainedClassNameFilter() {
        return this.retainedClassNameFilter;
    }
    
    /*
     * Matches.
     */
    
    /**
     * Computes elements which names match the specified filter.
     * 
     * Equivalent to computeDeps(,,true,false,0), but conceptually simpler.
     * 
     * @param elemType Type of elements to work on.
     * @param nameFilter Filter for elements names to accept.
     * @return A map of byte size by element name, for elements which names
     *         match the specified filter.
     * @throws NullPointerException if any argument is null.
     */
    public SortedMap<String,Long> computeMatches(
            ElemType elemType,
            InterfaceNameFilter nameFilter) {
        
        ArgsUtils.requireNonNull(elemType);
        ArgsUtils.requireNonNull(nameFilter);
        
        final PackageData defaultPackageData = this.computeDefaultPackageDataToUse();
        
        final Collection<InterfaceVertex> vertexColl = computeVertexColl(
                defaultPackageData,
                elemType,
                nameFilter);
        
        final SortedMap<String,Long> byteSizeByName = new TreeMap<String,Long>();
        for (InterfaceVertex vertex : vertexColl) {
            final AbstractCodeData vertexD = (AbstractCodeData) vertex;
            byteSizeByName.put(vertexD.displayName(), vertexD.byteSize());
        }
        
        return byteSizeByName;
    }
    
    /*
     * Dependencies.
     */
    
    /**
     * Computes bulk dependencies, with byte sizes.
     * 
     * The returned list always contains at least one map (step 0 map)
     * (which is consistent with ReachabilityComputer behavior).
     * 
     * @param elemType Type of elements to work on.
     * @param beginNameFilter Filter for names of elements which dependencies
     *        must be computed.
     * @param mustIncludeBeginSet True if must include begin set into the
     *        result.
     * @param mustIncludeDepsToBeginSet True if must include dependencies to
     *        begin set into the result. Only used if mustIncludeBeginSet is
     *        false.
     * @param maxSteps Max number of times edges are crossed, possibly 0.
     *        If < 0, no limit.
     * @return A list, of maps, with a map for each step of edges traversal.
     *         Keys of these maps are names of dependencies for elements
     *         which names match beginNameFilter, and values are the byte
     *         sizes of these dependencies.
     * @throws NullPointerException if any argument is null.
     */
    public List<SortedMap<String,Long>> computeDeps(
            ElemType elemType,
            InterfaceNameFilter beginNameFilter,
            boolean mustIncludeBeginSet,
            boolean mustIncludeDepsToBeginSet,
            int maxSteps) {
        
        ArgsUtils.requireNonNull(elemType);
        ArgsUtils.requireNonNull(beginNameFilter);

        final PackageData defaultPackageData = this.computeDefaultPackageDataToUse();
        
        final Collection<InterfaceVertex> beginVertexColl = computeVertexColl(
                defaultPackageData,
                elemType,
                beginNameFilter);

        final List<SortedMap<String,Long>> byteSizeByDependencyList = new ArrayList<SortedMap<String,Long>>();
        final JdcDepsStepsVcp processor = new JdcDepsStepsVcp(
                byteSizeByDependencyList,
                maxSteps);
        ReachabilityComputer.computeReachability(
                beginVertexColl,
                mustIncludeBeginSet,
                mustIncludeDepsToBeginSet,
                processor);

        return byteSizeByDependencyList;
    }

    /**
     * Computes dependencies as a graph.
     * 
     * @param elemType Type of elements to work on.
     * @param beginNameFilter Filter for names of elements which dependencies
     *        must be computed.
     * @param mustIncludeBeginSet True if must include begin set into the
     *        result.
     * @param mustIncludeDepsToBeginSet True if must include dependencies to
     *        begin set into the result. Only used if mustIncludeBeginSet is
     *        false.
     * @param endNameFilter Filter for names of successors (or predecessors in
     *        case of inverse dependencies) to retain in the result.
     * @param maxSteps Max number of times edges are crossed, possibly 0.
     *        If < 0, no limit.
     * @return A list, of maps, with a map for each step of edges traversal.
     *         Keys of these maps are names of dependencies for elements
     *         which names match beginNameFilter, and values are maps which keys
     *         are the successors (or predecessors in case of inverse
     *         dependencies) of these dependencies, and values the sets of the
     *         causes for these successors (or predecessors), i.e. empty sets
     *         if elements are classes, and sets of class names if elements are
     *         packages.
     * @throws NullPointerException if any argument is null.
     */
    public List<SortedMap<String,SortedMap<String,SortedSet<String>>>> computeDepsGraph(
            ElemType elemType,
            InterfaceNameFilter beginNameFilter,
            boolean mustIncludeBeginSet,
            boolean mustIncludeDepsToBeginSet,
            InterfaceNameFilter endNameFilter,
            int maxSteps) {
        
        ArgsUtils.requireNonNull(elemType);
        ArgsUtils.requireNonNull(beginNameFilter);
        ArgsUtils.requireNonNull(endNameFilter);

        final PackageData defaultPackageData = this.computeDefaultPackageDataToUse();

        final Collection<InterfaceVertex> beginVertexColl = computeVertexColl(
                defaultPackageData,
                elemType,
                beginNameFilter);
        final Collection<InterfaceVertex> endVertexColl = computeVertexColl(
                defaultPackageData,
                elemType,
                endNameFilter);
        
        final Set<InterfaceVertex> endVertexSet = new HashSet<InterfaceVertex>(endVertexColl);

        final List<SortedMap<String,SortedMap<String,SortedSet<String>>>> causesByDepByNameList =
                new ArrayList<SortedMap<String,SortedMap<String,SortedSet<String>>>>();

        final JdcDepsGraphVcp processor = new JdcDepsGraphVcp(
                elemType,
                causesByDepByNameList,
                endVertexSet,
                maxSteps);
        ReachabilityComputer.computeReachability(
                beginVertexColl,
                mustIncludeBeginSet,
                mustIncludeDepsToBeginSet,
                processor);

        return causesByDepByNameList;
    }
    
    /*
     * Paths.
     */

    /**
     * Computes one shortest path from a set of elements to another.
     * 
     * @param elemType Type of elements to work on.
     * @param beginNameFilter Filter for names of elements defining the set of
     *        elements to start from.
     * @param endNameFilter Filter for names of elements defining the set of
     *        elements to reach.
     * @return A shortest path from elements of begin set to elements of end
     *         set, as a list of maps, with a map for each step of edges
     *         traversal.
     *         These maps only contain one key, which is the element reached
     *         on the path for the step of the map, and the value is the set
     *         of causes of the dependency to the next element.
     *         The causes set in last map is empty, as there is no next element.
     * @throws NullPointerException if any argument is null.
     */
    public List<SortedMap<String,SortedSet<String>>> computeOneShortestPath(
            ElemType elemType,
            InterfaceNameFilter beginNameFilter,
            InterfaceNameFilter endNameFilter) {
        
        ArgsUtils.requireNonNull(elemType);
        ArgsUtils.requireNonNull(beginNameFilter);
        ArgsUtils.requireNonNull(endNameFilter);

        final PackageData defaultPackageData = this.computeDefaultPackageDataToUse();
        
        final Collection<InterfaceVertex> beginVertexColl = computeVertexColl(
                defaultPackageData,
                elemType,
                beginNameFilter);
        final Collection<InterfaceVertex> endVertexColl = computeVertexColl(
                defaultPackageData,
                elemType,
                endNameFilter);
        
        final List<SortedMap<String,SortedSet<String>>> depCausesByNameList =
                new ArrayList<SortedMap<String,SortedSet<String>>>();
        
        final JdcOneShortestPathVcp processor = new JdcOneShortestPathVcp(
                elemType,
                depCausesByNameList);
        OneShortestPathComputer.computeOneShortestPath(
                beginVertexColl,
                endVertexColl,
                processor);
        
        return depCausesByNameList;
    }

    /**
     * Computes a graph that contains all paths up to a certain length from a
     * set of elements to another.
     * 
     * More precisely, computes the set of elements being in the intersection of
     * elements reachable from begin set in up to the specified max number of
     * steps, and elements from which end set is reachable in up to the
     * specified max number of steps as well.
     * Also, when an end element is reached, even by a zero length path (i.e. if
     * it is also in begin set), no further path is considered from that
     * element, such as if begin set is included in end set, only begin elements
     * will be computed.
     * 
     * The computed set of elements includes all the elements involved in
     * (possibly cyclic) paths from begin set to end set in up to the specified
     * max number of steps, or max path length, plus eventually elements
     * corresponding to about twice longer paths.
     * 
     * The returned graph only contains edges between its own elements,
     * i.e. there are no dangling edges.
     * 
     * @param elemType Type of elements to work on.
     * @param beginNameFilter Filter for names of elements defining the set of
     *        elements paths must start from.
     * @param endNameFilter Filter for names of elements defining the set of
     *        elements paths must reach.
     * @param maxSteps Max number of times edges are crossed, possibly 0.
     *        If < 0, no limit.
     * @return A graph containing all path from begin set to end set in up to
     *         the specified max number of steps, as a map.
     *         Keys of the map are elements of the graphs, and values are maps
     *         which keys are their successors (or predecessors in case of
     *         inverse dependencies), and values the causes set of these
     *         successors (or predecessors), i.e. empty sets if elements are
     *         classes, or sets of class names if elements are packages. 
     * @throws NullPointerException if any argument is null.
     */
    public SortedMap<String,SortedMap<String,SortedSet<String>>> computePathsGraph(
            ElemType elemType,
            InterfaceNameFilter beginNameFilter,
            InterfaceNameFilter endNameFilter,
            int maxSteps) {
        
        ArgsUtils.requireNonNull(elemType);
        ArgsUtils.requireNonNull(beginNameFilter);
        ArgsUtils.requireNonNull(endNameFilter);

        final PackageData defaultPackageData = this.computeDefaultPackageDataToUse();
        
        final Collection<InterfaceVertex> beginVertexColl = computeVertexColl(
                defaultPackageData,
                elemType,
                beginNameFilter);
        final Collection<InterfaceVertex> endVertexColl = computeVertexColl(
                defaultPackageData,
                elemType,
                endNameFilter);

        final SortedMap<String,SortedMap<String,SortedSet<String>>> causesByDepByName =
                new TreeMap<String,SortedMap<String,SortedSet<String>>>();
        final JdcPathsGraphVcp processor = new JdcPathsGraphVcp(
                elemType,
                causesByDepByName);
        PathsGraphComputer.computePathsGraph(
                beginVertexColl,
                endVertexColl,
                maxSteps,
                processor);
        
        return causesByDepByName;
    }
    
    /*
     * SCCs and cycles.
     */
    
    /**
     * Computes the strongly connected components (SCCs) in parsed elements
     * graph, i.e. each set of elements that all depend on each other
     * (possibly transitively), with byte sizes.
     * 
     * Never computes SCCs of size 1, to avoid good code causing spam.
     * 
     * @param elemType Type of elements to work on.
     * @return A list of maps, each map corresponding to a SCC.
     *         Keys of each map are names of an element in the SCC, and values
     *         are corresponding byte sizes.
     *         SCCs are sorted from smallest to largest, and by first key
     *         natural ordering for SCCs of same size.
     * @throws NullPointerException if elemType is null.
     */
    public List<SortedMap<String,Long>> computeSccs(ElemType elemType) {
        
        ArgsUtils.requireNonNull(elemType);

        final PackageData defaultPackageData = this.computeDefaultPackageDataToUse();
        
        // Need to never filter out vertices here, else graph would not be
        // consistent, i.e. would not contain vertices reachable from
        // contained ones.
        final Collection<InterfaceVertex> graph = computeVertexColl(
                defaultPackageData,
                elemType,
                NameFilters.any());
        
        final List<SortedMap<String,Long>> sccList = new ArrayList<SortedMap<String,Long>>();
        final JdcSccVcp processor = new JdcSccVcp(
                elemType,
                sccList);
        SccsComputer.computeSccs(
                graph,
                processor);
        
        SortUtils.sort(sccList);
        
        return sccList;
    }

    /**
     * Computes cycles in parsed elements graph.
     * 
     * Never computes cycles of size 1 (since dependencies to self cannot be
     * represented in the backing structures).
     * 
     * The maxSize argument is especially useful for tangled code, for which
     * all cycles computation could easily take ages due to their insane amount.
     * 
     * @param elemType Type of elements to work on.
     * @param maxSize Max size of cycles to compute, possibly 0.
     *        If < 0, no limit.
     * @param processor Processor to process the cycles with.
     * @throws NullPointerException if any argument is null.
     */
    public void computeCycles(
            ElemType elemType,
            int maxSize,
            InterfaceCycleProcessor processor) {
        
        ArgsUtils.requireNonNull(elemType);
        ArgsUtils.requireNonNull(processor);
        
        final PackageData defaultPackageData = this.computeDefaultPackageDataToUse();
        
        // Need to never filter out vertices here, else graph would not be
        // consistent, i.e. would not contain vertices reachable from
        // contained ones.
        final Collection<InterfaceVertex> graph = computeVertexColl(
                defaultPackageData,
                elemType,
                NameFilters.any());
        
        final JdcCycleVcp vcp = new JdcCycleVcp(
                elemType,
                processor);
        CyclesComputer.computeCycles(
                graph,
                maxSize,
                vcp);
    }
    
    /**
     * Computes some cycles in parsed elements graph.
     * Computes none only if there is none, i.e. always finds at least one if
     * there are some.
     * 
     * Never computes cycles of size 1 (since dependencies to self cannot be
     * represented in the backing structures).
     * 
     * @param elemType Type of elements to work on.
     * @param maxSize Max size of cycles to compute, possibly 0.
     *        If < 0, no limit.
     * @param processor Processor to process the cycles with.
     * @throws NullPointerException if any argument is null.
     */
    public void computeSomeCycles(
            ElemType elemType,
            int maxSize,
            InterfaceCycleProcessor processor) {
        
        ArgsUtils.requireNonNull(elemType);
        ArgsUtils.requireNonNull(processor);

        final PackageData defaultPackageData = this.computeDefaultPackageDataToUse();
        
        // Need to never filter out vertices here, else graph would not be
        // consistent, i.e. would not contain vertices reachable from
        // contained ones.
        final Collection<InterfaceVertex> graph = computeVertexColl(
                defaultPackageData,
                elemType,
                NameFilters.any());
        
        final JdcCycleVcp vcp = new JdcCycleVcp(
                elemType,
                processor);
        SomeCyclesComputer.computeSomeCycles(
                graph,
                maxSize,
                vcp);
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    /**
     * @return Default package data to use for computation, and for read only usage.
     */
    private PackageData computeDefaultPackageDataToUse() {
        
        final PackageData defaultPackageDataToUse;
        
        if ((!this.mustUseInverseDeps)
                && (this.retainedClassNameFilter == NameFilters.any())) {
            // No need to derive.
            defaultPackageDataToUse = this.parser.getDefaultPackageData();
        } else {
            defaultPackageDataToUse = DerivedTreeComputer.computeDerivedTree(
                    this.parser.getDefaultPackageData(),
                    this.mustUseInverseDeps,
                    this.retainedClassNameFilter);
        }
        
        return defaultPackageDataToUse;
    }
    
    private static Collection<InterfaceVertex> computeVertexColl(
            PackageData defaultPackageData,
            ElemType elemType,
            InterfaceNameFilter nameFilter) {
        final Collection<InterfaceVertex> vertexColl;
        if (elemType == ElemType.CLASS) {
            vertexColl = CodeDataUtils.newClassDataList(
                    defaultPackageData,
                    nameFilter);
        } else {
            vertexColl = CodeDataUtils.newPackageDataList(
                    defaultPackageData,
                    nameFilter);
        }
        return vertexColl;
    }
}
