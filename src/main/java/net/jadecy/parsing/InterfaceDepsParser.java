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
package net.jadecy.parsing;

import java.io.File;

import net.jadecy.code.PackageData;

/**
 * Interface for accumulating dependencies from different classes.
 * 
 * When parsing class files, ClassData for classes directly depended on
 * and surrounding classes up to top level classes must automatically be
 * created even if not parsed.
 * In particular, this allows to always have available the ClassData
 * of the corresponding top level class.
 */
public interface InterfaceDepsParser {
    
    /*
     * Could remove dependency to File, and be more general,
     * using a String URL, but parsing filters depend on File,
     * so it would make things inconsistent.
     */
    
    /**
     * @return True if this accumulator merges nested classes, false otherwise.
     */
    public boolean getMustMergeNestedClasses();

    /**
     * @return True if this accumulator only takes API dependencies into account,
     *         false otherwise.
     */
    public boolean getApiOnly();
    
    /**
     * Dependencies accumulated in the returned package data can be
     * programmatically modified.
     * 
     * @return The default package data where dependencies
     *         are accumulated.
     */
    public PackageData getDefaultPackageData();
    
    /**
     * Parses class files in directories or jar files, and accumulates
     * dependencies into the specified default package data.
     * 
     * @param file A directory, or a class file, or a jar file.
     * @param filters Filters to apply. Must not be null.
     * @return True if did modify, false otherwise.
     * @throws NullPointerException if either specified reference is null.
     * @throws IllegalArgumentException wrapping a FileNotFoundException if the
     *         specified file is not found, even if it doesn't match filters,
     *         and possibly if a file to parse has been concurrently deleted.
     */
    public boolean accumulateDependencies(
            File file,
            ParsingFilters filters);
}
