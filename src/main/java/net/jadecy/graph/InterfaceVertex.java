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

/**
 * Interface for vertices, a graph being just a collection of vertices
 * (and of their successors).
 * 
 * Vertices must be comparable to each other:
 * - An ordering of vertices is easy to ensure.
 * - It allows for deterministic behavior when using ordered collections
 *   (whether algorithms require their ordering or not).
 */
public interface InterfaceVertex extends Comparable<InterfaceVertex> {

    /*
     * Using
     * "public <V extends InterfaceVertex> Collection<V> successors();"
     * and not
     * "public Collection<? extends InterfaceVertex> successors();",
     * even though it causes more cast warnings, because it allows
     * to do things like:
     * 
     * public static <V extends InterfaceVertex> void ensurePath(V... path) {
     *   V prev = null;
     *   for (V v : path) {
     *       if ((prev != null) && (!prev.successors().contains(v))) {
     *           prev.successors().add(v); // Wildcard version causes trouble here.
     *       }
     *       prev = v;
     *   }
     * }
     * 
     * It might be more adequate for this class to be generic with vertex type
     * as parameterized type, but we don't want to clutter the code all over
     * with related and possibly unmanageable boilerplate.
     */
    
    /**
     * @return The collection (possibly mutable) of successors of this vertex.
     */
    public <V extends InterfaceVertex> Collection<V> successors();
}
