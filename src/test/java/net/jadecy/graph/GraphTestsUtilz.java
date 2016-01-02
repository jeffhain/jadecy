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
package net.jadecy.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import net.jadecy.utils.ComparableArrayList;
import net.jadecy.utils.ComparableTreeSet;

/**
 * Utility treatments for our tests.
 */
class GraphTestsUtilz {

    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------

    public static final int LARGER_THAN_CALL_STACK = 20 * 1000;

    private static final boolean ONE_VERTEX_CYCLES_ALLOWED = true;

    //--------------------------------------------------------------------------
    // PUBLIC CLASSES
    //--------------------------------------------------------------------------

    /**
     * Generates random unique ids that slowly grow in magnitude.
     */
    public static class IdGenerator {
        private final Random random;
        private final HashSet<Integer> usedIds = new HashSet<Integer>();
        /**
         * Generates ids in [1,1+2*nbrOfGeneratesI].
         */
        public IdGenerator(Random random) {
            this.random = random;
        }
        /**
         * @return An id > 0.
         */
        public int nextId() {
            int result;
            do {
                final int pos = this.random.nextInt() & Integer.MAX_VALUE;
                // Modulo ensures that ids start small (more readable).
                result = 1 + pos % (1 + 2 * this.usedIds.size());
            } while (!this.usedIds.add(result));
            return result;
        }
    }

    /**
     * Note: this class has a natural ordering that is inconsistent with equals.
     */
    public static class Vertex implements InterfaceVertex {
        private final int id;
        /**
         * Object field so that it can be reused by extending classes
         * for other types of collections.
         */
        private Object successors;
        /**
         * @param id Id for ordering. Must be >= 0.
         */
        public Vertex(int id) {
            this((Void) null, id);
            this.setSuccessors(new TreeSet<Vertex>());
        }
        @Override
        public String toString() {
            /*
             * Not showing successors in vertex toString(),
             * to avoid spam if just wanting to show its id or such.
             * Use printGraph(...) to show successors.
             */
            return "[" + this.id + "]";
        }
        //@Override
        public int compareTo(InterfaceVertex other) {
            final Vertex ozer = (Vertex) other;
            // Our ids are >= 0.
            return this.id - ozer.id;
        }
        public int id() {
            return this.id;
        }
        //@Override
        @SuppressWarnings("unchecked")
        public Set<Vertex> successors() {
            return (Set<Vertex>) this.successors;
        }
        /**
         * setSuccessors(...) must be called after that
         * during construction.
         */
        protected Vertex(
                Void dummy,
                int id) {
            if (id < 0) {
                throw new IllegalArgumentException("" + id);
            }
            this.id = id;
        }
        /**
         * To be called during construction, and never after.
         */
        protected final void setSuccessors(Object successors) {
            this.successors = successors;
        }
    }

    public static class Wertex extends Vertex implements InterfaceWertex {
        /**
         * To avoid allocations for default weight.
         */
        private static final Double DEFAULT_WEIGHT = 0.0;
        private final TreeMap<Wertex, Double> weightBySuccessor = new TreeMap<Wertex, Double>();
        private Double weight;
        /**
         * Uses a default weight of +0.0.
         * @param id Id for ordering. Must be >= 0.
         */
        public Wertex(int id) {
            this(id, DEFAULT_WEIGHT);
        }
        /**
         * @param id Id for ordering. Must be >= 0.
         */
        public Wertex(int id, Double weight) {
            super((Void) null, id);
            this.setSuccessors(this.weightBySuccessor.keySet());
            this.weight = weight;
        }
        @Override
        public String toString() {
            return "[" + this.id() + "," + this.weight + "]";
        }
        // Overriding to suppress warning.
        @Override
        @SuppressWarnings("unchecked")
        public Set<Vertex> successors() {
            return (Set<Vertex>) super.successors();
        }
        //@Override
        public Double weight() {
            return this.weight;
        }
        //@Override
        @SuppressWarnings("unchecked")
        public Map<Wertex, Double> weightBySuccessor() {
            return this.weightBySuccessor;
        }
    }

    /**
     * Note: if vertices classes have a natural ordering that is inconsistent
     * with equals, this class also does.
     */
    public static class ComparableVertexArrayList extends ComparableArrayList<InterfaceVertex> {
        private static final long serialVersionUID = 1L;
        public ComparableVertexArrayList() {
        }
        public ComparableVertexArrayList(Collection<? extends InterfaceVertex> c) {
            super(c);
        }
    }

    /**
     * Note: if vertices classes have a natural ordering that is inconsistent
     * with equals, this class also does.
     */
    public static class ComparableVertexTreeSet extends ComparableTreeSet<InterfaceVertex> {
        private static final long serialVersionUID = 1L;
        public ComparableVertexTreeSet() {
        }
        public ComparableVertexTreeSet(Collection<? extends InterfaceVertex> c) {
            super(c);
        }
    }

    /*
     * 
     */

    /**
     * Interface for generating graphs for tests or benches.
     */
    public interface InterfaceGraphGenerator {
        /**
         * @return A description of the type of graphs generated by this
         *         generator.
         */
        @Override
        public String toString();
        /**
         * @return A new graph. Each call can generate the same one.
         */
        public List<InterfaceVertex> newGraph();
        /**
         * Used to test SCCs computations (for cycles, we test against a naive
         * algorithm).
         * 
         * @return Set of expected SCCs, updated on each generation.
         */
        public TreeSet<ComparableVertexTreeSet> getExpectedSccs();
    }

    /**
     * Generates graphs of non-connected vertices.
     */
    public static class DisconnectedGraphGenerator implements InterfaceGraphGenerator {
        private final long seed;
        private final int size;
        private final TreeSet<ComparableVertexTreeSet> expectedSccs = new TreeSet<ComparableVertexTreeSet>();
        /**
         * @param size Must be >= 0.
         */
        public DisconnectedGraphGenerator(
                long seed,
                int size) {
            if (size < 0) {
                throw new IllegalArgumentException("" + size);
            }
            this.seed = seed;
            this.size = size;
        }
        @Override
        public String toString() {
            return "[disconnected graphs, seed = " + this.seed + ", size = " + this.size + "]";
        }
        //@Override
        public List<InterfaceVertex> newGraph() {
            final List<InterfaceVertex> graph = new ArrayList<InterfaceVertex>();

            final Random random = new Random(this.seed);
            final IdGenerator idGenerator = new IdGenerator(random);

            this.expectedSccs.clear();

            for (int i = 0; i < this.size; i++) {
                int id = idGenerator.nextId();
                Vertex v = newInGraph(graph, id);

                ComparableVertexTreeSet scc = new ComparableVertexTreeSet();
                scc.add(v);
                this.expectedSccs.add(scc);
            }

            return graph;
        }
        //@Override
        public TreeSet<ComparableVertexTreeSet> getExpectedSccs() {
            return this.expectedSccs;
        }
    }

    /**
     * Generates graphs in which each vertex has only one successor and one
     * predecessor, except for one vertex that only has a successor, and one
     * that only has a predecessor.
     */
    public static class ChainGraphGenerator implements InterfaceGraphGenerator {
        private final long seed;
        private final int size;
        private final TreeSet<ComparableVertexTreeSet> expectedSccs = new TreeSet<ComparableVertexTreeSet>();
        /**
         * @param size Must be >= 0.
         */
        public ChainGraphGenerator(
                long seed,
                int size) {
            if (size < 0) {
                throw new IllegalArgumentException("" + size);
            }
            this.seed = seed;
            this.size = size;
        }
        @Override
        public String toString() {
            return "[chain graphs, seed = " + this.seed + ", size = " + this.size + "]";
        }
        //@Override
        public List<InterfaceVertex> newGraph() {
            final List<InterfaceVertex> graph = new ArrayList<InterfaceVertex>();

            final Random random = new Random(this.seed);
            final IdGenerator idGenerator = new IdGenerator(random);

            this.expectedSccs.clear();

            Vertex prevV = null;
            for (int i = 0; i < this.size; i++) {
                int id = idGenerator.nextId();
                Vertex v = newInGraph(graph, id);
                if (prevV != null) {
                    prevV.successors().add(v);
                }

                ComparableVertexTreeSet scc = new ComparableVertexTreeSet();
                scc.add(v);
                this.expectedSccs.add(scc);

                prevV = v;
            }

            return graph;
        }
        //@Override
        public TreeSet<ComparableVertexTreeSet> getExpectedSccs() {
            return this.expectedSccs;
        }
    }

    /**
     * Generates graphs corresponding to a binary tree, with first vertex being
     * the root, two next vertices being its successors, etc.
     * 
     * d = 0, size = 1
     * d = 1, size = 3
     * d = 2, size = 7
     * d = 3, size = 15
     * d = 4, size = 31
     * d = 5, size = 63
     * d = 6, size = 127
     * d = 7, size = 255
     * d = 8, size = 511
     * d = 9, size = 1023
     * d = 10, size = 2047
     * d = 11, size = 4095
     * d = 12, size = 8191
     * d = 13, size = 16383
     * d = 14, size = 32767
     * d = 15, size = 65535
     * d = 16, size = 131071
     * d = 17, size = 262143
     * d = 18, size = 524287
     * d = 19, size = 1048575
     * d = 20, size = 2097151
     * d = 21, size = 4194303
     * d = 22, size = 8388607
     * d = 23, size = 16777215
     * d = 24, size = 33554431
     * d = 25, size = 67108863
     * d = 26, size = 134217727
     * d = 27, size = 268435455
     * d = 28, size = 536870911
     * d = 29, size = 1073741823
     * d = 30, size = 2147483647
     */
    public static class TreeGraphGenerator implements InterfaceGraphGenerator {
        private final long seed;
        private final int depth;
        private final int size;
        private final TreeSet<ComparableVertexTreeSet> expectedSccs = new TreeSet<ComparableVertexTreeSet>();
        /**
         * @param depth 0 for only root vertex. Must be in [0,30].
         */
        public TreeGraphGenerator(
                long seed,
                int depth) {
            if ((depth < 0) || (depth > 30)) {
                throw new IllegalArgumentException("" + depth);
            }
            this.seed = seed;
            this.depth = depth;
            this.size = (1 << (depth+1)) - 1;
        }
        @Override
        public String toString() {
            return "[tree graph, seed = " + this.seed + ", depth = " + this.depth + ", size = " + this.size + "]";
        }
        //@Override
        public List<InterfaceVertex> newGraph() {
            final List<InterfaceVertex> graph = new ArrayList<InterfaceVertex>();

            final Random random = new Random(this.seed);
            final IdGenerator idGenerator = new IdGenerator(random);
            final ArrayList<Integer> idByIndex = new ArrayList<Integer>();
            for (int i = 0; i < this.size; i++) {
                idByIndex.add(idGenerator.nextId());
            }

            final Map<Integer,Vertex> vertexById = new HashMap<Integer,Vertex>();
            for (int i = 0; i < this.size; i++) {
                final int id = idByIndex.get(i);
                final Vertex v = GraphTestsUtilz.newInGraph(graph, id);
                vertexById.put(id,v);
                // One SCC per vertex.
                {
                    final ComparableVertexTreeSet scc = new ComparableVertexTreeSet();
                    scc.add(v);
                    this.expectedSccs.add(scc);
                }
            }
            for (int i = 1; i < this.size; i++) {
                final int idFrom = idByIndex.get((i-1)/2);
                final int idTo = idByIndex.get(i);
                GraphTestsUtilz.ensurePathFromIds(vertexById, idFrom, idTo);
            }

            return graph;
        }
        //@Override
        public TreeSet<ComparableVertexTreeSet> getExpectedSccs() {
            return this.expectedSccs;
        }
    }

    /**
     * Generates graphs in which each vertex has only one successor and one
     * predecessor, forming a single cycle (possibly of size 1).
     */
    public static class CycleGraphGenerator implements InterfaceGraphGenerator {
        private final long seed;
        private final int size;
        private final TreeSet<ComparableVertexTreeSet> expectedSccs = new TreeSet<ComparableVertexTreeSet>();
        /**
         * @param size Must be >= 0.
         */
        public CycleGraphGenerator(
                long seed,
                int size) {
            if (size < 0) {
                throw new IllegalArgumentException("" + size);
            }
            this.seed = seed;
            this.size = size;
        }
        @Override
        public String toString() {
            return "[cycle graphs, seed = " + this.seed + ", size = " + this.size + "]";
        }
        //@Override
        public List<InterfaceVertex> newGraph() {
            final List<InterfaceVertex> graph = new ArrayList<InterfaceVertex>();

            final Random random = new Random(this.seed);
            final IdGenerator idGenerator = new IdGenerator(random);

            this.expectedSccs.clear();

            if (this.size != 0) {
                Vertex initialV = null;
                Vertex prevV = null;
                for (int i = 0; i < this.size; i++) {
                    int id = idGenerator.nextId();
                    Vertex v = newInGraph(graph, id);
                    if (initialV == null) {
                        initialV = v;
                    }
                    if (prevV != null) {
                        prevV.successors().add(v);
                    }
                    prevV = v;
                }

                if (ONE_VERTEX_CYCLES_ALLOWED || (this.size > 1)) {
                    prevV.successors().add(initialV);
                }
            }

            this.expectedSccs.add(new ComparableVertexTreeSet(graph));

            return graph;
        }
        //@Override
        public TreeSet<ComparableVertexTreeSet> getExpectedSccs() {
            return this.expectedSccs;
        }
    }

    /**
     * Generates a cycle graph with first vertex also having all other vertices as successors.
     */
    public static class RakeCycleGraphGenerator implements InterfaceGraphGenerator {
        private final long seed;
        private final int size;
        private final TreeSet<ComparableVertexTreeSet> expectedSccs = new TreeSet<ComparableVertexTreeSet>();
        /**
         * @param size Must be >= 0.
         */
        public RakeCycleGraphGenerator(
                long seed,
                int size) {
            if (size < 0) {
                throw new IllegalArgumentException("" + size);
            }
            this.seed = seed;
            this.size = size;
        }
        @Override
        public String toString() {
            return "[rake cycle graphs, seed = " + this.seed + ", size = " + this.size + "]";
        }
        //@Override
        public List<InterfaceVertex> newGraph() {
            final List<InterfaceVertex> graph = new ArrayList<InterfaceVertex>();

            final Random random = new Random(this.seed);
            final IdGenerator idGenerator = new IdGenerator(random);

            this.expectedSccs.clear();

            if (this.size != 0) {
                Vertex initialV = null;
                Vertex prevV = null;
                for (int i = 0; i < this.size; i++) {
                    int id = idGenerator.nextId();
                    Vertex v = newInGraph(graph, id);
                    if (initialV == null) {
                        initialV = v;
                    } else {
                        initialV.successors().add(v);
                    }
                    if (prevV != null) {
                        prevV.successors().add(v);
                    }
                    prevV = v;
                }

                if (ONE_VERTEX_CYCLES_ALLOWED || (this.size > 1)) {
                    prevV.successors().add(initialV);
                }
            }

            this.expectedSccs.add(new ComparableVertexTreeSet(graph));

            return graph;
        }
        //@Override
        public TreeSet<ComparableVertexTreeSet> getExpectedSccs() {
            return this.expectedSccs;
        }
    }

    /**
     * Generates graphs in which each vertex has all vertices (including itself)
     * as successors (and therefore also as predecessors).
     */
    public static class BallGraphGenerator implements InterfaceGraphGenerator {
        private final long seed;
        private final int size;
        private final TreeSet<ComparableVertexTreeSet> expectedSccs = new TreeSet<ComparableVertexTreeSet>();
        /**
         * @param size Must be >= 0.
         */
        public BallGraphGenerator(
                long seed,
                int size) {
            if (size < 0) {
                throw new IllegalArgumentException("" + size);
            }
            this.seed = seed;
            this.size = size;
        }
        @Override
        public String toString() {
            return "[ball graphs, seed = " + this.seed + ", size = " + this.size + "]";
        }
        //@Override
        public List<InterfaceVertex> newGraph() {
            final List<InterfaceVertex> graph = new ArrayList<InterfaceVertex>();

            final Random random = new Random(this.seed);
            final IdGenerator idGenerator = new IdGenerator(random);

            this.expectedSccs.clear();

            for (int i = 0; i < this.size; i++) {
                int id = idGenerator.nextId();
                Vertex v = newInGraph(graph, id);
                for (InterfaceVertex _w : graph) {
                    final Vertex w = (Vertex) _w;
                    if (ONE_VERTEX_CYCLES_ALLOWED || (w != v)) {
                        v.successors().add(w);
                        w.successors().add(v);
                    }
                }
            }

            this.expectedSccs.add(new ComparableVertexTreeSet(graph));

            return graph;
        }
        //@Override
        public TreeSet<ComparableVertexTreeSet> getExpectedSccs() {
            return this.expectedSccs;
        }
    }

    /**
     * Generates graph with 0 or more vertices, and from 0 to 3 successors
     * for each vertex (3 should be enough to trigger all messy cases).
     */
    public static class RandomGraphGenerator implements InterfaceGraphGenerator {
        private final long seed;
        private final int maxSize;
        /**
         * @param maxSize Must be >= 0.
         */
        public RandomGraphGenerator(
                long seed,
                int maxSize) {
            if (maxSize < 0) {
                throw new IllegalArgumentException("" + maxSize);
            }
            this.seed = seed;
            this.maxSize = maxSize;
        }
        /**
         * @throws UnsupportedOperationException
         */
        //@Override
        public TreeSet<ComparableVertexTreeSet> getExpectedSccs() {
            throw new UnsupportedOperationException();
        }
        @Override
        public String toString() {
            return "[random graphs, seed = " + this.seed + ", maxSize = " + this.maxSize + "]";
        }
        //@Override
        public List<InterfaceVertex> newGraph() {
            final List<InterfaceVertex> graph = new ArrayList<InterfaceVertex>();

            final Random random = new Random(this.seed);
            final IdGenerator idGenerator = new IdGenerator(random);

            final int size = random.nextInt(this.maxSize + 1);
            
            /*
             * Creating vertices of random ids.
             */
            
            for (int i = 0; i < size; i++) {
                final int idi = idGenerator.nextId();
                newInGraph(graph, idi);
            }
            
            /*
             * Creating random edges, between different vertices.
             */
            
            for (int i = 0; i < size; i++) {
                final Vertex v = (Vertex) graph.get(i);
                
                // size - 1, because don't want itself as successor.
                final int maxNbrOfSucc = Math.min(3, size - 1);
                final int nbrOfSucc = random.nextInt(maxNbrOfSucc + 1);
                
                int succCount = 0;
                while (succCount < nbrOfSucc) {
                    final int succIndex = random.nextInt(size);
                    if (succIndex != i) {
                        final Vertex succ = (Vertex) graph.get(succIndex);
                        final boolean didAdd = v.successors().add(succ);
                        if (didAdd) {
                            succCount++;
                        }
                    }
                }
            }

            return graph;
        }
    }

    /**
     * Generates graphs with SCCs containing eventually multiple cycles
     * (possibly of size 1), and linked to each other.
     */
    public static class RandomGraphWithSccsGenerator implements InterfaceGraphGenerator {
        private final long seed;
        private final int nbrOfSccs;
        private final int maxSccSize;
        private final TreeSet<ComparableVertexTreeSet> expectedSccs = new TreeSet<ComparableVertexTreeSet>();
        /**
         * @param nbrOfSccs Must be >= 0.
         * @param maxSccSize Must be > 0.
         */
        public RandomGraphWithSccsGenerator(
                long seed,
                int nbrOfSccs,
                int maxSccSize) {
            if (nbrOfSccs < 0) {
                throw new IllegalArgumentException("" + nbrOfSccs);
            }
            if (maxSccSize <= 0) {
                throw new IllegalArgumentException("" + maxSccSize);
            }
            this.seed = seed;
            this.nbrOfSccs = nbrOfSccs;
            this.maxSccSize = maxSccSize;
        }
        /**
         * Used to test SCCs computations (for cycles, we test against a naive
         * algorithm).
         * 
         * @return Set of expected SCCs, updated on each generation.
         */
        //@Override
        public TreeSet<ComparableVertexTreeSet> getExpectedSccs() {
            return this.expectedSccs;
        }
        @Override
        public String toString() {
            return "[random graphs, seed = " + this.seed + ", nbrOfSccs = " + this.nbrOfSccs + ", maxSccSize = " + this.maxSccSize + "]";
        }
        //@Override
        public List<InterfaceVertex> newGraph() {
            final List<InterfaceVertex> graph = new ArrayList<InterfaceVertex>();

            final Random random = new Random(this.seed);
            final IdGenerator idGenerator = new IdGenerator(random);

            this.expectedSccs.clear();

            Vertex vOfPrevScc = null;
            for (int k = 0; k < this.nbrOfSccs; k++) {
                final int sccSize = 1 + random.nextInt(this.maxSccSize);
                Vertex prevVOfScc = null;
                {
                    final ComparableVertexTreeSet scc = new ComparableVertexTreeSet();
                    Vertex firstVOfScc = null;
                    Vertex somePrevVOfScc = null;
                    for (int i = 0; i < sccSize; i++) {
                        int idi = idGenerator.nextId();
                        Vertex vi = newInGraph(graph, idi);
                        scc.add(vi);
                        if (firstVOfScc == null) {
                            firstVOfScc = vi;
                        }
                        if (prevVOfScc != null) {
                            // Connecting with the previous vertex of this SCC.
                            prevVOfScc.successors().add(vi);
                            if ((somePrevVOfScc != null) && random.nextBoolean()) {
                                // Sometimes, connecting with some previous vertex
                                // of this SCC, not to have SCCs always be just
                                // simple cycles.
                                connectEitherWay(random, somePrevVOfScc, vi);
                            }
                        } else if (vOfPrevScc != null) {
                            // Connecting with previous SCC, in either way
                            // (but not both, else SCCs would merge).
                            connectEitherWay(random, vOfPrevScc, vi);
                        }
                        prevVOfScc = vi;
                        if ((somePrevVOfScc == null) || random.nextBoolean()) {
                            somePrevVOfScc = vi;
                        }
                    }

                    if (prevVOfScc != firstVOfScc) {
                        // To have a cycle, to ensure strong connection.
                        prevVOfScc.successors().add(firstVOfScc);
                    } else if (ONE_VERTEX_CYCLES_ALLOWED && random.nextBoolean()) {
                        // To have some on-vertex cycles.
                        prevVOfScc.successors().add(firstVOfScc);
                    }

                    this.expectedSccs.add(scc);
                }
                vOfPrevScc = prevVOfScc;
            }

            return graph;
        }
    }

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /*
     * Graph construction.
     */

    public static Vertex newInGraph(Collection<? super Vertex> graph, int id) {
        final Vertex v = new Vertex(id);
        graph.add(v);
        return v;
    }

    /**
     * Ensures successors in the specified vertices so that the specified path
     * exists.
     */
    @SuppressWarnings("unchecked")
    public static <V extends InterfaceVertex> void ensurePath(V... path) {
        V prev = null;
        for (V v : path) {
            if ((prev != null) && (!prev.successors().contains(v))) {
                prev.successors().add(v);
            }
            prev = v;
        }
    }

    public static <V extends InterfaceVertex> List<V> asListFromIndexes(List<V> vertexByIndex, int... indexes) {
        final List<V> list = new ArrayList<V>();
        for (int index : indexes) {
            list.add(vertexByIndex.get(index));
        }
        return list;
    }

    public static <V extends InterfaceVertex> Set<V> asSetFromIndexes(List<V> vertexByIndex, int... indexes) {
        final Set<V> set = new HashSet<V>();
        for (int index : indexes) {
            set.add(vertexByIndex.get(index));
        }
        return set;
    }

    /**
     * Here "id" refers to whatever integer is used to identify vertices in the map.
     */
    public static <V extends InterfaceVertex> List<V> asListFromIds(Map<Integer,V> vertexById, int... ids) {
        final List<V> list = new ArrayList<V>();
        for (int id : ids) {
            list.add(vertexById.get(id));
        }
        return list;
    }

    public static <V extends InterfaceVertex> void ensurePathFromIndexes(List<V> vertexByIndex, int... indexes) {
        final List<V> list = asListFromIndexes(vertexByIndex, indexes);
        ensurePath(list.toArray(new InterfaceVertex[list.size()]));
    }

    /**
     * Here "id" refers to whatever integer is used to identify vertices in the map.
     */
    public static <V extends InterfaceVertex> void ensurePathFromIds(Map<Integer,V> vertexById, int... ids) {
        final List<V> list = asListFromIds(vertexById, ids);
        ensurePath(list.toArray(new InterfaceVertex[list.size()]));
    }

    /**
     * Using a HashSet allows for random iteration order,
     * which should not impact results (treatments must ensure
     * determinism by using vertices comparability).
     * 
     * @return A HashSet containing the specified vertices.
     */
    public static Set<InterfaceVertex> asHashSet(InterfaceVertex... vertices) {
        final HashSet<InterfaceVertex> set = new HashSet<InterfaceVertex>();
        for (InterfaceVertex v : vertices) {
            set.add(v);
        }
        return set;
    }

    /**
     * For work vertices to have same ids than their backing vertices (when
     * using WorkGraphUtilz.newWorkGraphXxx(...)), the definition of vertices ids
     * (but not of their successors) must start at 1, and increase by 1 for each
     * new vertex.
     * 
     * @param vertexIdAndSuccessorsIdsTab Ex.: {{{1},{2,3}},{{2},{3}},{{3},{}}}
     *        means 1 has 2 and 3 as successors, 2 has 3 as successor, and 3 has
     *        no successor.
     */
    public static List<Vertex> newGraph(
            int[][][] vertexIdAndSuccIdsTab) {
        /*
         * Creating vertices.
         */

        final HashMap<Integer, Vertex> vertexById = new HashMap<Integer, Vertex>();
        for (int[][] vertexIdAndSuccIds : vertexIdAndSuccIdsTab) {
            final int id = vertexIdAndSuccIds[0][0];
            final Vertex vertex = new Vertex(id);
            vertexById.put(id, vertex);
        }

        /*
         * Creating graph collection and structure.
         */

        final ArrayList<Vertex> graph = new ArrayList<Vertex>();
        for (int[][] vertexIdAndSuccIds : vertexIdAndSuccIdsTab) {
            final int id = vertexIdAndSuccIds[0][0];
            final Vertex vertex = vertexById.get(id);
            for (int succId : vertexIdAndSuccIds[1]) {
                final Vertex succ = vertexById.get(succId);
                vertex.successors().add(succ);
            }
            graph.add(vertex);
        }

        return graph;
    }

    /*
     * 
     */

    /**
     * @param treeGraph A balanced tree graph, such as generated by TreeGraphGenerator,
     *        with leaves as last elements.
     * @return A list of all leaves.
     */
    public static <V extends InterfaceVertex> List<V> newLeafList(List<V> treeGraph) {
        final int size = treeGraph.size();
        final int depth = 31 - Integer.numberOfLeadingZeros(size);
        final int sizeFromDepth = (1 << (depth+1)) - 1;
        if (sizeFromDepth != size) {
            throw new IllegalArgumentException("not the size of a (balanced) tree graph : " + size);
        }

        final List<V> leafList = new ArrayList<V>();
        final int nbrOfLeaves = (1<<depth);
        for (int i = 0; i < nbrOfLeaves; i++) {
            leafList.add(treeGraph.get(size-1 - i));
        }

        return leafList;
    }

    /*
     * 
     */

    public static void checkPathExists(List<? extends InterfaceVertex> path) {
        InterfaceVertex prev = null;
        for (InterfaceVertex v : path) {
            if (prev != null) {
                if (!prev.successors().contains(v)) {
                    throw new AssertionError("no edge from " + prev + " to " + v + ", path = " + path);
                }
            }
            prev = v;
        }
    }

    public static void printGraph(Collection<? extends InterfaceVertex> graph) {
        System.out.println("graph:");
        for (InterfaceVertex v : graph) {
            System.out.print("v = " + v + ", successors = {");
            final Collection<? extends InterfaceVertex> successors = v.successors();
            final int nbrOfSucc = successors.size();
            int i = 0;
            for (InterfaceVertex succ : v.successors()) {
                System.out.print(succ);
                if (i < nbrOfSucc-1) {
                    System.out.print(",");
                }
                i++;
            }
            System.out.println("}");
        }
    }

    /**
     * To guard against sets mess-up due to modification of elements while they
     * are in the specified set.
     */
    public static <T> void checkConsistent(Set<T> setOfStuffs) {
        final Object[] sets = setOfStuffs.toArray();
        for (Object stuff : sets) {
            if (!setOfStuffs.contains(stuff)) {
                throw new AssertionError("element not in its set : " + stuff);
            }
        }
    }

    public static TreeSet<ComparableVertexArrayList> toNormalizedCyclesAsLists(
            List<List<InterfaceVertex>> cycles) {
        final TreeSet<ComparableVertexArrayList> result = new TreeSet<ComparableVertexArrayList>();
        for (List<InterfaceVertex> cycle : cycles) {
            final InterfaceVertex[] cyclone = cycle.toArray(new InterfaceVertex[cycle.size()]);

            CyclesUtils.normalizeCycle(cyclone);
            final ComparableVertexArrayList cycleAL = new ComparableVertexArrayList();
            for (InterfaceVertex element : cyclone) {
                cycleAL.add(element);
            }
            result.add(cycleAL);
        }
        return result;
    }

    public static Map<Integer,Integer> computeCountBySize(List<List<InterfaceVertex>> cycleList) {
        final Map<Integer,Integer> countBySize = new HashMap<Integer,Integer>();
        for (List<InterfaceVertex> cycle : cycleList) {
            final int size = cycle.size();
            Integer count = countBySize.get(size);
            if (count == null) {
                count = 0;
            }
            countBySize.put(size, count + 1);
        }
        return countBySize;
    }
    
    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------

    private GraphTestsUtilz() {
    }

    private static void connectEitherWay(Random random, Vertex v1, Vertex v2) {
        if (random.nextBoolean()) {
            v1.successors().add(v2);
        } else {
            v2.successors().add(v1);
        }
    }
}
