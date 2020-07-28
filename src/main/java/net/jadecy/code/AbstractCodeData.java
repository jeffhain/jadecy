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
package net.jadecy.code;

import java.util.Set;

import net.jadecy.graph.InterfaceVertex;
import net.jadecy.names.NameUtils;

/**
 * Abstract class for data related to a class file or a package.
 * 
 * Not calling that class "AbstractCodeVertex", because it's not only a
 * vertex in dependency graph, but also a node in packages and classes tree.
 * The use of the general term "data" makes this non-reduction explicit.
 * 
 * Note: this class has a natural ordering that is inconsistent with equals
 * (which doesn't hurt as long as we use a single instance per actual data).
 */
public abstract class AbstractCodeData implements InterfaceVertex {
    
    /*
     * No need to override hashCode and equals, since we don't use multiple
     * instances for a same class or package.
     * 
     * To save memory, it would be possible not to store the full name (like
     * java.lang for packages, or java.lang.Math for classes), but only the
     * part after the last dot, and:
     * - in toString() method, build the full String.
     * - in compareTo(...) method, recurse towards default package and then
     *   compare each couple of same-depth parts one by one.
     * Though, that would actually not save memory when data get processed,
     * since most likely full name Strings would be created and stored,
     * and anew for each processing.
     * As a result, we just store the full name here.
     */
    
    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------

    /**
     * If file: null if in default package (no package).
     * If package: null if root package.
     */
    private final AbstractCodeData parent;
    
    /**
     * Ex.:
     * - "Foo$1" for net.bar.Foo$1 class.
     * - "three" for one.two.three package.
     */
    private final String fileNameNoExt;
    
    /**
     * ex.: java.lang, java.lang.Math
     */
    private final String name;
    
    /**
     * For a class data, the byte size of the corresponding class file
     * (or merged class files), or 0 if has not been parsed yet.
     * For a package data, the sum of byte sizes of parsed classes
     * belonging to the corresponding package.
     */
    private long byteSize = 0;

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    /**
     * @return A String representation of this instance.
     */
    @Override
    public String toString() {
        /*
         * Not returning more than display name, for this method to be very
         * CPU and memory efficient.
         * Also allows for collections of these objects not to be too verbose
         * when using toString on them.
         */
        return this.displayName();
    }

    /**
     * For a same instance, the returned String instance is always the same:
     * it does not need to be cached aside to reduce memory footprint.
     * 
     * @return The class or package name, i.e. their name (like
     *         "java.lang" or "java.lang.Math", or an empty string for
     *         default package).
     */
    public String name() {
        return this.name;
    }

    /**
     * For a same instance, the returned String instance is always the same:
     * it does not need to be cached aside to reduce memory footprint.
     * 
     * @return The class or package display name, i.e. their name (like
     *         "java.lang" or "java.lang.Math"), except for default package
     *         for which NameUtils.DEFAULT_PACKAGE_DISPLAY_NAME is returned.
     */
    public String displayName() {
        return NameUtils.toDisplayName(this.name);
    }

    @Override
    public int compareTo(InterfaceVertex other) {
        return this.name.compareTo(((AbstractCodeData) other).name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public abstract Set<? extends AbstractCodeData> successors();

    public abstract Set<? extends AbstractCodeData> predecessors();

    /**
     * Default package data cannot be considered deleted,
     * since it cannot be deleted (it has nothing above to be deleted from).
     * 
     * @return True if this data has been deleted, i.e. is no longer attached
     *         to a tree, false otherwise.
     */
    public boolean isDeleted() {
        /*
         * Could just loop once since we always clear deleted data,
         * but going up to root for safety in case that changes
         * (should only hurt with pathological package trees).
         */
        AbstractCodeData tmpChild = this;
        AbstractCodeData tmpParent = tmpChild.parent;
        while (tmpParent != null) {
            if (!tmpParent.hasChild(tmpChild)) {
                return true;
            }
            tmpChild = tmpParent;
            tmpParent = tmpChild.parent;
        }
        // Went up to root.
        return false;
    }
    
    /**
     * @return The (package) data of the parent directory of this data,
     *         or null if called on default package data.
     */
    public AbstractCodeData parent() {
        return this.parent;
    }

    /**
     * @return The topmost data that is not null
     *         (in practice the default package data).
     */
    public AbstractCodeData root() {
        AbstractCodeData root = this;
        while (root.parent != null) {
            root = root.parent;
        }
        return root;
    }

    /**
     * Returns null for default package data.
     * 
     * @return The file name without extension, such as "Foo$1" for
     *         net.bar.Foo$1 class, or "three" for one.two.three package. 
     */
    public String fileNameNoExt() {
        return this.fileNameNoExt;
    }
    
    /**
     * @return For a class data, the byte size of the corresponding class file
     *         (or merged class files), or 0 if has not been parsed yet.
     *         For a package data, the sum of byte sizes of parsed classes
     *         belonging to the corresponding package.
     */
    public long byteSize() {
        return this.byteSize;
    }
    
    //--------------------------------------------------------------------------
    // PACKAGE-PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    /**
     * @param parent Null only for default package.
     * @param fileNameNoExt Null only for default package.
     * @param original For string reuse. Can be null.
     * @throws NullPointerException if either of parent and fileNameNoExt is null
     *         and the other is not.
     * @throws IllegalArgumentException if parent is not null and fileNameNoExt
     *         is empty or contains a dot, or if original is not null and
     *         does not pass some basic checks for consistency with specified
     *         parent and fileNameNoExt.
     */
    AbstractCodeData(
            AbstractCodeData parent,
            String fileNameNoExt,
            AbstractCodeData original) {
        // Must be both null or both non-null.
        if ((parent == null) ^ (fileNameNoExt == null)) {
            throw new NullPointerException(
                    "parent ["
                            + parent
                            + "] and fileNameNoExt ["
                            + fileNameNoExt
                            + "] must be both null or both non-null");
        }
        this.parent = parent;
        if (original != null) {
            // Quick checks (we like this path to be fast).
            if (parent == null) {
                // Default package.
                if (original.parent != null) {
                    throw new IllegalArgumentException();
                }
            } else {
                if ((original.parent == null)
                        || (original.parent.name.length() != parent.name.length())
                        || (original.fileNameNoExt.length() != fileNameNoExt.length())) {
                    throw new IllegalArgumentException();
                }
            }
            this.fileNameNoExt = original.fileNameNoExt;
            this.name = original.name;
        } else {
            if (parent == null) {
                // Default package: null.
                this.fileNameNoExt = null;
            } else {
                this.fileNameNoExt = checkedFileNameNoExt(fileNameNoExt);
            }
            this.name = computeName(parent, fileNameNoExt);
        }
    }
    
    /**
     * Adder, not setter, so that it also works when merging nested classes,
     * or for packages, or when deleting classes.
     * 
     * @param byteSize Can be negative, for example if deleting a class data.
     * @throws IllegalArgumentException if the add would cause byte size to be negative.
     */
    void addByteSize(long toAdd) {
        final long newByteSize = this.byteSize + toAdd;
        if (newByteSize < 0) {
            throw new IllegalArgumentException(Long.toString(toAdd));
        }
        this.byteSize = newByteSize;
    }

    /**
     * @return True if the specified data is child of this data,
     *         false otherwise.
     */
    abstract boolean hasChild(AbstractCodeData data);

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    /**
     * @param fileNameNoExt Must not contain a dot, nor be empty, nor be null.
     * @return The specified string, supposed to be a package or class name inter-dot part.
     * @throws NullPointerException if the specified string is null.
     * @throws IllegalArgumentException if the specified string is empty or contains a dot.
     */
    private static String checkedFileNameNoExt(String fileNameNoExt) {
        // Implicit null check.
        if (fileNameNoExt.length() == 0) {
            throw new IllegalArgumentException("empty file name no ext");
        }
        if (fileNameNoExt.indexOf('.') >= 0) {
            throw new IllegalArgumentException("there is a dot in file name no ext: " + fileNameNoExt);
        }
        return fileNameNoExt;
    }
    
    private static String computeName(
            AbstractCodeData parent,
            String fileNameNoExt) {
        if (parent == null) {
            // No parent: must be default package.
            return NameUtils.DEFAULT_PACKAGE_NAME;
        } else if (parent.fileNameNoExt == null) {
            // Parent is root.
            return fileNameNoExt;
        } else {
            return parent.name + "." + fileNameNoExt;
        }
    }
}
