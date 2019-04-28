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
import java.io.FilenameFilter;

import net.jadecy.names.InterfaceNameFilter;
import net.jadecy.names.NameFilters;
import net.jadecy.utils.ArgsUtils;

/**
 * Filters for parsing class files.
 * 
 * Effectively immutable.
 */
public final class ParsingFilters {
    
    //--------------------------------------------------------------------------
    // PRIVATE CLASSES
    //--------------------------------------------------------------------------
    
    private static class MyAnyDirFilenameFilter implements FilenameFilter {
        //@Override
        public boolean accept(File dir, String name) {
            return true;
        }
    }
    
    private static class MyAnyJarFilenameFilter implements FilenameFilter {
        //@Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".jar");
        }
    }

    private static class MyAnyClassFilenameFilter implements FilenameFilter {
        //@Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".class");
        }
    }

    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------
    
    private static final FilenameFilter DEFAULT_DIR_FILENAME_FILTER = new MyAnyDirFilenameFilter();
    private static final FilenameFilter DEFAULT_JAR_FILENAME_FILTER = new MyAnyJarFilenameFilter();
    private static final FilenameFilter DEFAULT_CLASS_FILENAME_FILTER = new MyAnyClassFilenameFilter();
    private static final InterfaceNameFilter DEFAULT_CLASS_NAME_FILTER = NameFilters.any();

    private static final ParsingFilters DEFAULT_INSTANCE = new ParsingFilters();

    private FilenameFilter dirFilenameFilter;
    private FilenameFilter jarFilenameFilter;
    private FilenameFilter jarEntryFilenameFilter;
    private FilenameFilter classFilenameFilter;
    private InterfaceNameFilter classNameFilter;
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /**
     * @return A default instance, using default filters described for each
     *         withXxx methods.
     */
    public static ParsingFilters defaultInstance() {
        return DEFAULT_INSTANCE;
    }

    /**
     * @param dirFilenameFilter The filter for accepting explored directories.
     *        Must not be null.
     * @param jarFilenameFilter The filter for accepting jar files names.
     *        Must not be null.
     * @param jarEntryFilenameFilter The filter for accepting jar entries files names.
     *        Must not be null.
     * @param classFilenameFilter The filter for accepting class files names.
     *        Must not be null.
     * @param classNameFilter The filter for accepting classes names.
     *        Must not be null.
     * @return A new instance, using the specified filters.
     * @throws NullPointerException if any of the filters is null.
     */
    public static ParsingFilters newInstance(
            FilenameFilter dirFilenameFilter,
            FilenameFilter jarFilenameFilter,
            FilenameFilter jarEntryFilenameFilter,
            FilenameFilter classFilenameFilter,
            InterfaceNameFilter classNameFilter) {
        return new ParsingFilters(
                dirFilenameFilter,
                jarFilenameFilter,
                jarEntryFilenameFilter,
                classFilenameFilter,
                classNameFilter);
    }

    /*
     * 
     */

    /**
     * Default accepts any directory.
     * 
     * @param dirFilenameFilter The filter for accepting explored directories.
     *        Must not be null.
     * @return A new instance, with same filters as this one except for the
     *         specified one.
     * @throws NullPointerException if the specified filter is null.
     */
    public ParsingFilters withDirFilenameFilter(FilenameFilter dirFilenameFilter) {
        ArgsUtils.requireNonNull(dirFilenameFilter);
        final ParsingFilters instance = new ParsingFilters(this);
        instance.dirFilenameFilter = dirFilenameFilter;
        return instance;
    }

    /**
     * Default accepts any file terminating with ".jar".
     * 
     * @param jarFilenameFilter The filter for accepting jar files names.
     *        Must not be null.
     * @return A new instance, with same filters as this one except for the
     *         specified one.
     * @throws NullPointerException if the specified filter is null.
     */
    public ParsingFilters withJarFilenameFilter(FilenameFilter jarFilenameFilter) {
        ArgsUtils.requireNonNull(jarFilenameFilter);
        final ParsingFilters instance = new ParsingFilters(this);
        instance.jarFilenameFilter = jarFilenameFilter;
        return instance;
    }

    /**
     * Default accepts any entry terminating with ".class".
     * 
     * @param jarEntryFilenameFilter The filter for accepting jar entries files names.
     *        Must not be null.
     * @return A new instance, with same filters as this one except for the
     *         specified one.
     * @throws NullPointerException if the specified filter is null.
     */
    public ParsingFilters withJarEntryFilenameFilter(FilenameFilter jarEntryFilenameFilter) {
        ArgsUtils.requireNonNull(jarEntryFilenameFilter);
        final ParsingFilters instance = new ParsingFilters(this);
        instance.jarEntryFilenameFilter = jarEntryFilenameFilter;
        return instance;
    }

    /**
     * Default accepts any file terminating with ".class".
     * 
     * @param classFilenameFilter The filter for accepting class files names.
     *        Must not be null.
     * @return A new instance, with same filters as this one except for the
     *         specified one.
     * @throws NullPointerException if the specified filter is null.
     */
    public ParsingFilters withClassFilenameFilter(FilenameFilter classFilenameFilter) {
        ArgsUtils.requireNonNull(classFilenameFilter);
        final ParsingFilters instance = new ParsingFilters(this);
        instance.classFilenameFilter = classFilenameFilter;
        return instance;
    }

    /**
     * Default accepts any class name.
     * 
     * @param classNameFilter The filter for accepting classes names.
     *        Must not be null.
     * @return A new instance, with same filters as this one except for the
     *         specified one.
     * @throws NullPointerException if the specified filter is null.
     */
    public ParsingFilters withClassNameFilter(InterfaceNameFilter classNameFilter) {
        ArgsUtils.requireNonNull(classNameFilter);
        final ParsingFilters instance = new ParsingFilters(this);
        instance.classNameFilter = classNameFilter;
        return instance;
    }

    /*
     * 
     */
    
    /**
     * @return The filter for accepting explored directories. Never null.
     */
    public FilenameFilter getDirFilenameFilter() {
        return this.dirFilenameFilter;
    }

    /**
     * @return The filter for accepting jar files names. Never null.
     */
    public FilenameFilter getJarFilenameFilter() {
        return this.jarFilenameFilter;
    }

    /**
     * @return The filter for accepting jar entries files names. Never null.
     */
    public FilenameFilter getJarEntryFilenameFilter() {
        return this.jarEntryFilenameFilter;
    }

    /**
     * @return The filter for accepting class files names. Never null.
     */
    public FilenameFilter getClassFilenameFilter() {
        return this.classFilenameFilter;
    }

    /**
     * @return The filter for accepting classes names. Never null.
     */
    public InterfaceNameFilter getClassNameFilter() {
        return this.classNameFilter;
    }
    
    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    private ParsingFilters() {
        this.dirFilenameFilter = DEFAULT_DIR_FILENAME_FILTER;
        this.jarFilenameFilter = DEFAULT_JAR_FILENAME_FILTER;
        this.jarEntryFilenameFilter = DEFAULT_CLASS_FILENAME_FILTER;
        this.classFilenameFilter = DEFAULT_CLASS_FILENAME_FILTER;
        this.classNameFilter = DEFAULT_CLASS_NAME_FILTER;
    }

    /**
     * @throws NullPointerException if any of the filters is null.
     */
    private ParsingFilters(
            FilenameFilter dirFilenameFilter,
            FilenameFilter jarFilenameFilter,
            FilenameFilter jarEntryFilenameFilter,
            FilenameFilter classFilenameFilter,
            InterfaceNameFilter classNameFilter) {
        this.dirFilenameFilter = ArgsUtils.requireNonNull(dirFilenameFilter);
        this.jarFilenameFilter = ArgsUtils.requireNonNull(jarFilenameFilter);
        this.jarEntryFilenameFilter = ArgsUtils.requireNonNull(jarEntryFilenameFilter);
        this.classFilenameFilter = ArgsUtils.requireNonNull(classFilenameFilter);
        this.classNameFilter = ArgsUtils.requireNonNull(classNameFilter);
    }

    /**
     * Copy constructor.
     */
    private ParsingFilters(ParsingFilters toCopy) {
        this.dirFilenameFilter = toCopy.dirFilenameFilter;
        this.jarFilenameFilter = toCopy.jarFilenameFilter;
        this.jarEntryFilenameFilter = toCopy.jarEntryFilenameFilter;
        this.classFilenameFilter = toCopy.classFilenameFilter;
        this.classNameFilter = toCopy.classNameFilter;
    }
}
