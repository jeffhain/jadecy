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

/**
 * Interface for processing cycles.
 */
public interface InterfaceCycleProcessor {

    /**
     * Specified arrays ownership is transfered to the implementation,
     * or to the GC if the implementation doesn't want them.
     * 
     * The processed cycles must be normalized, i.e. with lowest name in first
     * position in names array, and causes ordered in each causes array, both
     * orderings done according to String natural ordering.
     * 
     * @param names The array of elements names defining the cycle.
     * @param causesArr Array containing arrays of causes, each of which
     *        containing causes of dependencies from cycle element of same
     *        index to cycle element of next index (modulo cycle size).
     *        Must be null if causes are not applicable.
     * @return True if must stop computation, false otherwise.
     */
    public boolean processCycle(
            String[] names,
            String[][] causesArr);
}
