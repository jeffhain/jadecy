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
package net.jadecy.graph;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;
import net.jadecy.graph.GraphTestsUtilz.Vertex;

public class WorkGraphUtilzTest extends TestCase {

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    public void test_newWorkGraph_CollectionOfV_boolean_ordering() {
        for (boolean mustIgnoreDeadEnds : new boolean[]{false,true}) {
            final Collection<Vertex> graph = GraphTestsUtilz.newGraph(
                    new int[][][]{
                            {{3},{4}},
                            {{2},{1,3}},
                            {{4},{}},
                            {{1},{2,3}},
                    });

            final Set<WorkVertex> workGraph = WorkGraphUtilz.newWorkGraph(
                    graph,
                    mustIgnoreDeadEnds);

            final Vertex v1 = getVertexOfId(graph, 1);
            final Vertex v2 = getVertexOfId(graph, 2);
            final Vertex v3 = getVertexOfId(graph, 3);
            final Vertex v4 = getVertexOfId(graph, 4);

            final WorkVertex wv1 = getWorkVertexOfId(workGraph, 1);
            final WorkVertex wv2 = getWorkVertexOfId(workGraph, 2);
            final WorkVertex wv3 = getWorkVertexOfId(workGraph, 3);
            final WorkVertex wv4 = getWorkVertexOfId(workGraph, 4);

            if (mustIgnoreDeadEnds) {
                // Ignored vertices of ids 3 and 4.
                // Id 1 has been used for an internal work vertex for vertex of
                // id 3, before removal due to being a dead end, so resulting
                // work vertices ids start at 2.

                assertNull(wv1);
                assertNotNull(wv2);
                assertNotNull(wv3);
                assertNull(wv4);

                assertSame(v2, wv2.backingVertex());
                assertSame(v1, wv3.backingVertex());
            } else {
                assertNotNull(wv1);
                assertNotNull(wv2);
                assertNotNull(wv3);
                assertNotNull(wv4);

                assertSame(v3, wv1.backingVertex());
                assertSame(v2, wv2.backingVertex());
                assertSame(v4, wv3.backingVertex());
                assertSame(v1, wv4.backingVertex());
            }
        }
    }

    public void test_newWorkGraph_CollectionOfV_boolean_toIgnoreOrNoToIgnore() {
        for (boolean mustIgnoreDeadEnds : new boolean[]{false,true}) {
            final Collection<Vertex> graph = GraphTestsUtilz.newGraph(
                    new int[][][]{
                            {{1},{2,3}},
                            {{2},{1,3}},
                            {{3},{4}},
                            {{4},{}},
                    });

            final Set<WorkVertex> workGraph = WorkGraphUtilz.newWorkGraph(
                    graph,
                    mustIgnoreDeadEnds);

            final WorkVertex v1 = getWorkVertexOfId(workGraph, 1);
            final WorkVertex v2 = getWorkVertexOfId(workGraph, 2);
            final WorkVertex v3 = getWorkVertexOfId(workGraph, 3);
            final WorkVertex v4 = getWorkVertexOfId(workGraph, 4);

            if (mustIgnoreDeadEnds) {
                // Ignored vertices of ids 3 and 4.

                assertEquals(2, workGraph.size());
                assertNotNull(v1);
                assertNotNull(v2);
                assertNull(v3);
                assertNull(v4);

                assertEquals(1, v1.successors().size());
                assertTrue(v1.successors().contains(v2));
                assertEquals(1, v1.predecessors().size());
                assertTrue(v1.predecessors().contains(v2));

                assertEquals(1, v2.successors().size());
                assertTrue(v2.successors().contains(v1));
                assertEquals(1, v2.predecessors().size());
                assertTrue(v2.predecessors().contains(v1));
            } else {
                assertEquals(4, workGraph.size());
                assertNotNull(v1);
                assertNotNull(v2);
                assertNotNull(v3);
                assertNotNull(v4);

                assertEquals(2, v1.successors().size());
                assertTrue(v1.successors().contains(v2));
                assertTrue(v1.successors().contains(v3));
                assertEquals(1, v1.predecessors().size());
                assertTrue(v1.predecessors().contains(v2));

                assertEquals(2, v2.successors().size());
                assertTrue(v2.successors().contains(v1));
                assertTrue(v2.successors().contains(v3));
                assertEquals(1, v2.predecessors().size());
                assertTrue(v2.predecessors().contains(v1));

                assertEquals(1, v3.successors().size());
                assertTrue(v3.successors().contains(v4));
                assertEquals(2, v3.predecessors().size());
                assertTrue(v3.predecessors().contains(v1));
                assertTrue(v3.predecessors().contains(v2));

                assertEquals(0, v4.successors().size());
                assertEquals(1, v4.predecessors().size());
                assertTrue(v4.predecessors().contains(v3));
            }
        }
    }

    /**
     * Testing that it's allowed to have some successors not par of
     * the specified collection.
     */
    public void test_newWorkGraph_CollectionOfV_boolean_partialGraph() {
        for (boolean mustIgnoreDeadEnds : new boolean[]{false,true}) {
            final List<Vertex> graph = GraphTestsUtilz.newGraph(
                    new int[][][]{
                            {{1},{2,3}},
                            {{2},{1,3}},
                            {{3},{4}},
                            {{4},{}},
                    });

            // Partial graph: removing vertex 3, at index 2.
            graph.remove(2);
            final Set<WorkVertex> workGraph = WorkGraphUtilz.newWorkGraph(
                    graph,
                    mustIgnoreDeadEnds);

            final WorkVertex v1 = getWorkVertexOfId(workGraph, 1);
            final WorkVertex v2 = getWorkVertexOfId(workGraph, 2);
            // Vertex 3 not in the specified collection, so id 3
            // was used for our vertex 4.
            final WorkVertex v4 = getWorkVertexOfId(workGraph, 3);

            if (mustIgnoreDeadEnds) {
                // Ignored vertex of id 4.

                assertEquals(2, workGraph.size());
                assertNotNull(v1);
                assertNotNull(v2);
                assertNull(v4);

                assertEquals(1, v1.successors().size());
                assertTrue(v1.successors().contains(v2));
                assertEquals(1, v1.predecessors().size());
                assertTrue(v1.predecessors().contains(v2));

                assertEquals(1, v2.successors().size());
                assertTrue(v2.successors().contains(v1));
                assertEquals(1, v2.predecessors().size());
                assertTrue(v2.predecessors().contains(v1));
            } else {
                assertEquals(3, workGraph.size());
                assertNotNull(v1);
                assertNotNull(v2);
                assertNotNull(v4);

                assertEquals(1, v1.successors().size());
                assertTrue(v1.successors().contains(v2));
                assertEquals(1, v1.predecessors().size());
                assertTrue(v1.predecessors().contains(v2));

                assertEquals(1, v2.successors().size());
                assertTrue(v2.successors().contains(v1));
                assertEquals(1, v2.predecessors().size());
                assertTrue(v2.predecessors().contains(v1));

                assertEquals(0, v4.successors().size());
                assertEquals(0, v4.predecessors().size());
            }
        }
    }

    public void test_removeFromGraph_SetOfWorkVertex_WorkVertex() {
        final Collection<Vertex> graph = GraphTestsUtilz.newGraph(
                new int[][][]{
                        {{1},{2,3}},
                        {{2},{3}},
                        {{3},{1}},
                });

        final boolean mustIgnoreDeadEnds = false;
        final Set<WorkVertex> workGraph = WorkGraphUtilz.newWorkGraph(
                graph,
                mustIgnoreDeadEnds);

        final WorkVertex v1 = getWorkVertexOfId(workGraph, 1);
        final WorkVertex v2 = getWorkVertexOfId(workGraph, 2);
        final WorkVertex v3 = getWorkVertexOfId(workGraph, 3);
        assertNotNull(v1);
        assertNotNull(v2);
        assertNotNull(v3);

        /*
         * 
         */

        WorkGraphUtilz.removeFromGraph(workGraph, v1);

        assertEquals(2, workGraph.size());
        assertFalse(workGraph.contains(v1));
        assertTrue(workGraph.contains(v2));
        assertTrue(workGraph.contains(v3));

        assertEquals(0, v1.successors().size());
        assertEquals(0, v1.predecessors().size());

        assertEquals(1, v2.successors().size());
        assertTrue(v2.successors().contains(v3));
        assertEquals(0, v2.predecessors().size());

        assertEquals(0, v3.successors().size());
        assertEquals(0, v2.predecessors().size());
        assertTrue(v3.predecessors().contains(v2));
    }

    public void test_detachVerticesNotInGraph_SetOfWorkVertex() {
        final Collection<Vertex> graph = GraphTestsUtilz.newGraph(
                new int[][][]{
                        {{1},{2,3}},
                        {{2},{1,3,4}},
                        {{3},{1,2,4}},
                        {{4},{1}},
                });

        final boolean mustIgnoreDeadEnds = false;
        final Set<WorkVertex> workGraph = WorkGraphUtilz.newWorkGraph(
                graph,
                mustIgnoreDeadEnds);

        final WorkVertex v1 = getWorkVertexOfId(workGraph, 1);
        final WorkVertex v2 = getWorkVertexOfId(workGraph, 2);
        final WorkVertex v3 = getWorkVertexOfId(workGraph, 3);
        final WorkVertex v4 = getWorkVertexOfId(workGraph, 4);
        assertNotNull(v1);
        assertNotNull(v2);
        assertNotNull(v3);
        assertNotNull(v4);

        // Removing v3 and v4 from work graph collection, so that
        // they get detached.
        workGraph.remove(v3);
        workGraph.remove(v4);
        assertEquals(2, workGraph.size());
        
        WorkGraphUtilz.detachVerticesNotInGraph(workGraph);

        // Graph collection not damaged.
        assertEquals(2, workGraph.size());
        assertTrue(workGraph.contains(v1));
        assertTrue(workGraph.contains(v2));
        assertFalse(workGraph.contains(v3));
        assertFalse(workGraph.contains(v4));

        assertEquals(1, v1.successors().size());
        assertTrue(v1.successors().contains(v2));
        assertEquals(1, v1.predecessors().size());
        assertTrue(v1.predecessors().contains(v2));

        assertEquals(1, v2.successors().size());
        assertTrue(v2.successors().contains(v1));
        assertEquals(1, v2.predecessors().size());
        assertTrue(v2.predecessors().contains(v1));
        
        assertEquals(1, v3.successors().size());
        assertTrue(v3.successors().contains(v4));
        assertEquals(0, v3.predecessors().size());

        assertEquals(0, v4.successors().size());
        assertEquals(1, v4.predecessors().size());
        assertTrue(v4.predecessors().contains(v3));
    }

    public void test_removePredSuccNotInGraph_SetOfWorkVertex_WorkVertex() {
        final Collection<Vertex> graph = GraphTestsUtilz.newGraph(
                new int[][][]{
                        {{1},{2,3}},
                        {{2},{1,3,4}},
                        {{3},{1,2,4}},
                        {{4},{1}},
                });

        final boolean mustIgnoreDeadEnds = false;
        final Set<WorkVertex> workGraph = WorkGraphUtilz.newWorkGraph(
                graph,
                mustIgnoreDeadEnds);

        final WorkVertex v1 = getWorkVertexOfId(workGraph, 1);
        final WorkVertex v2 = getWorkVertexOfId(workGraph, 2);
        final WorkVertex v3 = getWorkVertexOfId(workGraph, 3);
        final WorkVertex v4 = getWorkVertexOfId(workGraph, 4);
        assertNotNull(v1);
        assertNotNull(v2);
        assertNotNull(v3);
        assertNotNull(v4);

        // Removing v3 and v4 from work graph collection, so that
        // they get detached from v2 (but not v1!).
        workGraph.remove(v3);
        workGraph.remove(v4);
        assertEquals(2, workGraph.size());
        
        WorkGraphUtilz.removePredSuccNotInGraph(workGraph, v2);

        // Graph collection not damaged.
        assertEquals(2, workGraph.size());
        assertTrue(workGraph.contains(v1));
        assertTrue(workGraph.contains(v2));
        assertFalse(workGraph.contains(v3));
        assertFalse(workGraph.contains(v4));

        assertEquals(2, v1.successors().size());
        assertTrue(v1.successors().contains(v2));
        assertTrue(v1.successors().contains(v3));
        assertEquals(3, v1.predecessors().size());
        assertTrue(v1.predecessors().contains(v2));
        assertTrue(v1.predecessors().contains(v3));
        assertTrue(v1.predecessors().contains(v4));

        assertEquals(1, v2.successors().size());
        assertTrue(v2.successors().contains(v1));
        assertEquals(1, v2.predecessors().size());
        assertTrue(v2.predecessors().contains(v1));
        
        assertEquals(2, v3.successors().size());
        assertTrue(v3.successors().contains(v1));
        assertTrue(v3.successors().contains(v4));
        assertEquals(1, v3.predecessors().size());
        assertTrue(v3.predecessors().contains(v1));

        assertEquals(1, v4.successors().size());
        assertTrue(v4.successors().contains(v1));
        assertEquals(1, v4.predecessors().size());
        assertTrue(v4.predecessors().contains(v3));
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------

    /**
     * Only looks in the collection, not in successors.
     * 
     * @return The (hopefully unique!) vertex of specified id, or null if could
     *         not find it.
     */
    private static WorkVertex getWorkVertexOfId(
            Collection<WorkVertex> workGraph,
            int id) {
        for (WorkVertex v : workGraph) {
            if (v.id() == id) {
                return v;
            }
        }
        return null;
    }

    /**
     * Only looks in the collection, not in successors.
     * 
     * @return The (hopefully unique!) vertex of specified id, or null if could
     *         not find it.
     */
    private static Vertex getVertexOfId(
            Collection<Vertex> graph,
            int id) {
        for (Vertex v : graph) {
            if (v.id() == id) {
                return v;
            }
        }
        return null;
    }
}
