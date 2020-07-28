/*
 * Copyright 2015-2020 Jeff Hain
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.jadecy.code.ClassData;
import net.jadecy.code.PackageData;
import net.jadecy.names.InterfaceNameFilter;
import net.jadecy.names.NameUtils;
import net.jadecy.utils.ArgsUtils;

/**
 * Parses dependencies from class files in file system.
 * 
 * Jar files are not explored recursively, i.e. if a jar file contains another
 * jar file, the nested jar file won't be explored.
 * 
 * See ClassDepsParser for details about how dependencies are computed.
 */
public class FsDepsParser implements InterfaceDepsParser {

    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------

    /*
     * Merge and API booleans, as well as default package data,
     * stored as members, for less risk of them changing while accumulating
     * dependencies, which could easily mess things up.
     */
    
    private final boolean mustMergeNestedClasses;
    private final boolean apiOnly;
    
    private final PackageData defaultPackageData = new PackageData();
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /**
     * @param mustMergeNestedClasses True if must merge dependencies from and to
     *        nested classes into their top level classes, along with byte size,
     *        false otherwise.
     * @param apiOnly If true, only takes into account API dependencies
     *        (cf. ClassDepsParser for details).
     */
    public FsDepsParser(
            boolean mustMergeNestedClasses,
            boolean apiOnly) {
        this.mustMergeNestedClasses = mustMergeNestedClasses;
        this.apiOnly = apiOnly;
    }

    @Override
    public boolean getMustMergeNestedClasses() {
        return this.mustMergeNestedClasses;
    }

    @Override
    public boolean getApiOnly() {
        return this.apiOnly;
    }

    @Override
    public PackageData getDefaultPackageData() {
        return this.defaultPackageData;
    }

    @Override
    public boolean accumulateDependencies(
            File file,
            ParsingFilters filters) {
        
        ArgsUtils.requireNonNull(file);
        ArgsUtils.requireNonNull(filters);
        
        if (!file.exists()) {
            throw new IllegalArgumentException(
                    "file to parse not found: " + file.getAbsolutePath(),
                    new FileNotFoundException("" + file.getPath())); // Just path, as done by usual JDK code.
        }
        
        return accumulateDepData(
                file.getParentFile(),
                file,
                //
                this.mustMergeNestedClasses,
                this.apiOnly,
                filters,
                //
                defaultPackageData);
    }
    
    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------

    /**
     * This method is recursive.
     * 
     * @return True if did modify, false otherwise.
     */
    private static boolean accumulateDepData(
            File parent,
            File file,
            boolean mustMergeNestedClasses,
            boolean apiOnly,
            ParsingFilters filters,
            PackageData defaultPackageData) {
        
        boolean modified = false;
        
        final FilenameFilter dirFilenameFilter = filters.getDirFilenameFilter();
        final FilenameFilter jarFilenameFilter = filters.getJarFilenameFilter();
        final FilenameFilter jarEntryFilenameFilter = filters.getJarEntryFilenameFilter();
        final FilenameFilter classFilenameFilter = filters.getClassFilenameFilter();
        final InterfaceNameFilter classNameFilter = filters.getClassNameFilter();
        
        final String fileName = file.getName();
        if (file.isDirectory()) {
            if (dirFilenameFilter.accept(parent, fileName)) {
                final String[] children = file.list();
                if (children == null) {
                    return modified;
                }

                final String dirPath = file.getAbsolutePath();
                for (String childFileName : children) {
                    final String childPath = dirPath + "/" + childFileName;
                    final File child = new File(childPath);
                    modified |= accumulateDepData(
                            file,
                            child,
                            mustMergeNestedClasses,
                            apiOnly,
                            filters,
                            defaultPackageData);
                }
            }
        } else {
            // Not a directory: considering it must be a class file
            // or a jar file.
            if (classFilenameFilter.accept(parent, fileName)) {
                modified |= parseClassFile(
                        file,
                        classNameFilter,
                        mustMergeNestedClasses,
                        apiOnly,
                        defaultPackageData);
            } else if (jarFilenameFilter.accept(parent, fileName)) {
                modified |= parseJarFile(
                        file,
                        jarEntryFilenameFilter,
                        classNameFilter,
                        mustMergeNestedClasses,
                        apiOnly,
                        defaultPackageData);
            }
        }
        
        return modified;
    }
    
    /*
     * 
     */

    /**
     * @return True if did modify, false otherwise.
     * @throws IllegalArgumentException wrapping a FileNotFoundException if the
     *         specified file is not found.
     */
    private static boolean parseClassFile(
            File file,
            InterfaceNameFilter classNameFilter,
            boolean mustMergeNestedClasses,
            boolean apiOnly,
            PackageData defaultPackageData) {
        
        boolean modified = false;
        
        final long inputByteSize = file.length();
        final FileInputStream fis;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
        try {
            modified = parseClassFileStream(
                    inputByteSize,
                    fis,
                    classNameFilter,
                    mustMergeNestedClasses,
                    apiOnly,
                    defaultPackageData);
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        
        return modified;
    }
    
    /**
     * Computes classes dependencies with names not in internal form,
     * i.e. with dots instead of slashes.
     * 
     * @param depClassNameList (in,out)
     * @return Class name with dots, not slashed.
     */
    private static String computeClassDeps(
            InputStream inputStream,
            boolean apiOnly,
            List<String> depClassNameList) {
        
        final List<String> depInternalClassNameList = new ArrayList<String>();
        final String internalClassName = ClassDepsParser.computeDependencies(
                inputStream,
                apiOnly,
                depInternalClassNameList);
        
        for (String depInternalClassName : depInternalClassNameList) {
            final String depClassName = NameUtils.doted(depInternalClassName);
            depClassNameList.add(depClassName);
        }
        
        return NameUtils.doted(internalClassName);

    }
    
    /**
     * Not using InputStream.available() to compute input size,
     * because theoretically it might not always work.
     * 
     * @param inputByteSize If <= 0, does nothing.
     * @return True if did modify, false otherwise.
     */
    private static boolean parseClassFileStream(
            long inputByteSize,
            InputStream inputStream,
            InterfaceNameFilter classNameFilter,
            boolean mustMergeNestedClasses,
            boolean apiOnly,
            PackageData defaultPackageData) {
        
        boolean modified = false;
        
        // We need size to be > 0, else we could pretend we did
        // modify while we did not (if size is 0, and class data
        // had already been created but has no dependency).
        if (inputByteSize <= 0) {
            return modified;
        }
        
        /*
         * Parsing class file.
         */
        
        final List<String> depClassNameList = new ArrayList<String>();
        // Always retrieving class name from inside the class file,
        // not from its path, so that we can handle "badly located"
        // class files.
        final String classNameParsed = computeClassDeps(
                inputStream,
                apiOnly,
                depClassNameList);
        
        final boolean wasClassFile = (classNameParsed != null);
        if (wasClassFile
                && classNameFilter.accept(classNameParsed)) {

            /*
             * Eventually replacing nested classes with top level class.
             */
            
            final String classNameForData;
            final Collection<String> depClassNameCollForData;
            if (mustMergeNestedClasses) {
                classNameForData = NameUtils.getTopLevelClassName(classNameParsed);
                
                depClassNameCollForData = new HashSet<String>();
                for (String depClassName : depClassNameList) {
                    depClassNameCollForData.add(NameUtils.getTopLevelClassName(depClassName));
                }
                
                // Making sure we don't loop with ourselves.
                depClassNameCollForData.remove(classNameForData);
            } else {
                classNameForData = classNameParsed;
                depClassNameCollForData = depClassNameList;
            }
            
            /*
             * Updating structure.
             */
            
            // If just created, and then accepted (because not parsed yet),
            // modified will be set to true when we figure out it was not already
            // registered as already parsed (need not to set it to true here!).
            final ClassData classData = defaultPackageData.getOrCreateClassData(classNameForData);

            final String classFileNameNoExt = NameUtils.getFileNameNoExt(classNameParsed);
            final boolean classFileNameNoExtNotParsedYet = PackageData.setByteSizeForClassOrNested(
                    classData,
                    classFileNameNoExt,
                    inputByteSize);
            if (classFileNameNoExtNotParsedYet) {
                // Class data might have been created already
                // for some dependency, but has not yet been parsed
                // (else we would not pass here), so modification
                // will occur when we add its byte size to its package.
                modified = true;
                
                for (String depClassName : depClassNameCollForData) {
                    final ClassData depClassData = defaultPackageData.getOrCreateClassData(depClassName);

                    modified |= PackageData.ensureDependency(
                            classData,
                            depClassData);
                }
            } else {
                // Already parsed and taken care of: must ignore it.
                // (Or user nastily called setByteSizeForClass(...) already,
                // but that's his fault.)
            }
        }
        
        return modified;
    }

    /**
     * @return True if did modify, false otherwise.
     * @throws IllegalArgumentException wrapping a FileNotFoundException if the
     *         specified file is not found.
     */
    private static boolean parseJarFile(
            File jar,
            FilenameFilter jarEntryFilenameFilter,
            InterfaceNameFilter classNameFilter,
            boolean mustMergeNestedClasses,
            boolean apiOnly,
            PackageData defaultPackageData) {
        
        boolean modified = false;
        
        final ZipFile zipFile;
        try {
            zipFile = new ZipFile(jar);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        try {
            final Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                if (jarEntryFilenameFilter.accept(jar,entry.getName())) {
                    long inputByteSize = entry.getSize();
                    if (inputByteSize < 0) {
                        // Unknown.
                        inputByteSize = 0;
                    }
                    final InputStream is;
                    try {
                        is = zipFile.getInputStream(entry);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        modified |= parseClassFileStream(
                                inputByteSize,
                                is,
                                classNameFilter,
                                mustMergeNestedClasses,
                                apiOnly,
                                defaultPackageData);
                    } finally {
                        try {
                            is.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        } finally {
            try {
                zipFile.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        
        return modified;
    }
}
