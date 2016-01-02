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

/**
 * Contains classes for graphs representations and computations.
 * 
 * Principal classes:
 * - InterfaceVertex: Interface for representing a vertex and its successors,
 *   a graph being a collection of vertices.
 * - ReachabilityComputer: Computes dependencies.
 * - OneShortestPathComputer: Computes one shortest path.
 * - PathsGraphComputer: Computes a graph containing all paths from a set of
 *   vertices to another.
 * - SccsComputer: Computes strongly connected components.
 * - CyclesComputer: Computes cycles.
 * - ShortestCyclesComputer: Computes shortest cycles covering all edges of each
 *   SCC.
 * - SomeCyclesComputer: Computes some cycles.
 */
package net.jadecy.graph;
