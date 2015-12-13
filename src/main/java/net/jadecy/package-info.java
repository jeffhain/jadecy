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

/**
 * Contains treatments to compute dependencies, strongly connected components,
 * and cycles, in classes or packages dependencies graphs parsed from class
 * files.
 *
 * Principal classes:
 * - Jadecy: The entry point.
 * - JadecyUtils: Utilities for dealing with Jadecy, in particular its results.
 * - DepUnit: To check dependencies and cycles in unit tests.
 */
package net.jadecy;
