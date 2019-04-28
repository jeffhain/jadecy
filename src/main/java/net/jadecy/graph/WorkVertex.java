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

import java.util.TreeSet;

/**
 * Vertex that can be used to speed-up algorithms, by being used to create
 * a "work graph" copy of the initial graph:
 * - A work graph can be modified, i.e. cleaned-up.
 * - Each work vertex is identified by an int id, for fast comparison (but
 *   as a result, the ordering between work vertices can be different than
 *   between backing vertices).
 * - Predecessors are available from each vertex, making removal of a vertex
 *   easy and fast.
 * 
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
class WorkVertex implements InterfaceVertex {
    
    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------
    
    private final InterfaceVertex backingVertex;
    
    private final int id;
    
    private final TreeSet<WorkVertex> successors = new TreeSet<WorkVertex>();
    private final TreeSet<WorkVertex> predecessors = new TreeSet<WorkVertex>();
    
    /**
     * Data to hold state for the algorithm being run.
     * Avoids usage of a map(key=vertex,value=data).
     */
    private Object data;
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    /**
     * @param backingVertex Corresponding vertex of the initial graph.
     * @param id Id for ordering. Must be >= 0.
     */
    public WorkVertex(
            InterfaceVertex backingVertex,
            int id) {
        if (id < 0) {
            throw new IllegalArgumentException("" + id);
        }
        this.backingVertex = backingVertex;
        this.id = id;
    }
    
    @Override
    public String toString() {
        return "(id=" + this.id + ")" + this.backingVertex;
    }
    
    @Override
    public int compareTo(InterfaceVertex other) {
        final WorkVertex ozer = (WorkVertex) other;
        // Our ids are >= 0.
        return this.id - ozer.id;
    }

    /**
     * @return The corresponding vertex of the initial graph.
     */
    public InterfaceVertex backingVertex() {
        return this.backingVertex;
    }

    /**
     * @return The id for ordering of work vertices.
     */
    public int id() {
        return this.id;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public TreeSet<WorkVertex> successors() {
        return this.successors;
    }
    
    public TreeSet<WorkVertex> predecessors() {
        return this.predecessors;
    }
    
    /**
     * @param data Data to hold state for the algorithm being run.
     */
    public void setData(Object data) {
        this.data = data;
    }
    
    public Object getData() {
        return this.data;
    }
}
