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
import java.util.Map;

/**
 * Interface to define edge-weighted and vertex-weighted graphs
 * (not used for now, but defined here to make sure InterfaceVertex
 * allows for such an extension).
 * 
 * Weights are allowed to be null or NaN as a way to indicate that
 * they are not defined, but one could also choose to use +0.0 as
 * default value.
 * It's up to algorithms to mandate what they require about that.
 * 
 * NB: It's tempting to use "w" as name for instances of implementations
 * of this interface, but it's better to call them "v" instead and reserve
 * "w" for actual weights, not to let the pun leak all over the code where
 * it might not be recognized as such.
 */
public interface InterfaceWertex extends InterfaceVertex {
    
    /**
     * Modifications of weightBySuccessor() must be reported into successors().
     * For example, successors() could be a view on weightBySuccessor() key set
     * (in which case successors() might not be modifiable through its API).
     */
    @Override
    public <V extends InterfaceVertex> Collection<V> successors();
    
    /**
     * @return The weight of this vertex.
     */
    public Double weight();

    /**
     * Modifications of weightBySuccessor() must be reported into successors().
     * For example, successors() could be a view on weightBySuccessor() key set.
     * 
     * Successors are required to be instances of this class
     * (and not just non-weighted vertices), to keep things
     * homogeneous.
     * 
     * @return A map of weight by successor, corresponding to the weight
     *         of the edge to that successor.
     */
    public <V extends InterfaceWertex> Map<V,Double> weightBySuccessor();
}
