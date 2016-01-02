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

/**
 * Interface to process collections of vertices without need of actually
 * creating such collections.
 * 
 * Whether empty collections are processed or not depends on usage.
 */
public interface InterfaceVertexCollProcessor {
    
    /*
     * This interface is especially designed for the case of computing cycles,
     * which can be many (easy to bench the gain with a "ball" graph (all
     * vertices successors of each other) of size 8 (-> 16064 cycles) or more).
     * 
     * For simplicity, we also use it for processing all vertices collections
     * computed by graph algorithms of this package.
     */
    
    /**
     * Called before calls to processCollVertex(...) for a same collection.
     * 
     * If this method throws, subsequent calls to processCollVertex(...) (if any)
     * and processCollEnd() are not done.
     */
    public void processCollBegin();
    
    /**
     * Not called if the collection to process is empty.
     * 
     * If this method throws, subsequent calls to it (if any) and
     * processCollEnd() are not done.
     * 
     * @param vertex A vertex of the currently processed collection.
     */
    public void processCollVertex(InterfaceVertex vertex);

    /**
     * Called after calls to processCollVertex(...) for a same collection,
     * or after processCollBegin() if the collection was empty.
     * 
     * @return True if must stop computation, false otherwise.
     */
    public boolean processCollEnd();
}
