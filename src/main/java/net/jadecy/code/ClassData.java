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
package net.jadecy.code;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import net.jadecy.utils.ArgsUtils;

/**
 * Data corresponding to a class file.
 * 
 * Returned collections instances might change on modification,
 * so you should not used cached references to them while modifying the tree.
 * 
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
public class ClassData extends AbstractCodeData {
    
    /*
     * Collections returned in the API, such as successors(), are sorted
     * (for determinism and convenience), but not typed as such (as with
     * SortedSet), to avoid API heterogeneity in case some derived collection
     * implementations (such as a view) would not implement these sorted
     * interfaces.
     */
    
    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------
    
    private static final SortedSet<ClassData> EMPTY_SET_ClassData =
            Collections.unmodifiableSortedSet(new TreeSet<ClassData>());
    
    private static final SortedMap<String,ClassData> EMPTY_MAP_String_ClassData =
            Collections.unmodifiableSortedMap(new TreeMap<String,ClassData>());
    
    private static final SortedMap<String,Long> EMPTY_MAP_String_Long =
            Collections.unmodifiableSortedMap(new TreeMap<String,Long>());
    
    /*
     * 
     */
    
    private SortedSet<ClassData> successorSet;
    private SortedSet<ClassData> successorSetUnmod = EMPTY_SET_ClassData;

    private SortedSet<ClassData> predecessorSet;
    private SortedSet<ClassData> predecessorSetUnmod = EMPTY_SET_ClassData;
    
    /**
     * Null if this class is a top level class.
     */
    private final ClassData outerClassData;
    
    private SortedMap<String,ClassData> nestedClassDataByFileNameNoExt;
    private SortedMap<String,ClassData> nestedClassDataByFileNameNoExtUnmod = EMPTY_MAP_String_ClassData;
    
    /**
     * Not located in PackageData, not to have to clear it up
     * when deleting a ClassData.
     * 
     * Lazily initialized, for lower memory overhead in ClassData
     * instances for which it is not used.
     * 
     * Useful if merging nested classes, in which cases only one ClassData
     * is to be used, or to avoid parsing class files corresponding to a same
     * class multiple times.
     * 
     * key = fileNameNoExt of a top level class or nested class that has been
     * parsed and merged (dependencies and byte size) into this ClassData
     * (which is always top level class data in case of nested classes merging).
     * value = parsed byte size.
     */
    private SortedMap<String,Long> byteSizeByClassFileNameNoExtSet;
    private SortedMap<String,Long> byteSizeByClassFileNameNoExtSetUnmod = EMPTY_MAP_String_Long;
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /**
     * @return An unmodifiable view of the internal set, or an empty unmodifiable set.
     */
    @Override
    public Set<ClassData> successors() {
        return this.successorSetUnmod;
    }

    /**
     * @return An unmodifiable view of the internal set, or an empty unmodifiable set.
     */
    @Override
    public Set<ClassData> predecessors() {
        return this.predecessorSetUnmod;
    }
    
    /**
     * @return The ClassData of the top level class,
     *         possibly this class.
     */
    public ClassData topLevelClassData() {
        ClassData tmpCD = this;
        while (tmpCD.outerClassData != null) {
            tmpCD = tmpCD.outerClassData;
        }
        return tmpCD;
    }

    /**
     * @return The ClassData for surrounding class, or null if the class
     *         corresponding to this ClassData is a top level class.
     */
    public ClassData outerClassData() {
        return this.outerClassData;
    }
    
    /**
     * @return An unmodifiable view of the internal set, or an empty unmodifiable set.
     */
    public Map<String,ClassData> nestedClassByFileNameNoExt() {
        return this.nestedClassDataByFileNameNoExtUnmod;
    }

    /**
     * The returned map content corresponds to previous calls to
     * PackageData.setByteSizeForClassOrNested(this_class,,).
     * 
     * @return An unmodifiable view of the internal map, or an empty unmodifiable map.
     */
    public Map<String,Long> byteSizeByClassFileNameNoExt() {
        return this.byteSizeByClassFileNameNoExtSetUnmod;
    }

    //--------------------------------------------------------------------------
    // PACKAGE-PRIVATE METHODS
    //--------------------------------------------------------------------------

    /**
     * The created ClassData is added in this constructor as nested class into
     * the specified outer class if any.
     * 
     * @param parentPackage Must not be null.
     * @param classFileNameNoExt Must not be null.
     * @param outerClassData Null if not a nested class.
     * @param original For string reuse. Can be null.
     * @throws NullPointerException if parentPackage or classFileNameNoExt is null.
     * @throws IllegalArgumentException if parentPackage is a ClassData, or if
     *         classFileNameNoExt is empty or contains a dot, unless original
     *         is not null in which case no check is done.
     */
    ClassData(
            AbstractCodeData parentPackage,
            String classFileNameNoExt,
            ClassData outerClassData,
            ClassData original) {
        super(
                parentPackage,
                classFileNameNoExt,
                original);
        // Just need to test one, due to test in super constructor.
        // If throws, classFileNameNoExt is also null, else not.
        ArgsUtils.requireNonNull(parentPackage);
        if (parentPackage instanceof ClassData) {
            throw new IllegalArgumentException();
        }
        this.outerClassData = outerClassData;
        if (outerClassData != null) {
            final SortedMap<String,ClassData> map = outerClassData.nestedClassByFileNameNoExt_internal();
            final Object forCheck = map.put(classFileNameNoExt, this);
            if (forCheck != null) {
                // Map must be updated in case of class deletion,
                // so that must never happen.
                throw new AssertionError();
            }
        }
    }

    /**
     * Should only be used for modifications, to avoid useless lazy creation.
     */
    SortedSet<ClassData> successors_internal() {
        SortedSet<ClassData> coll = this.successorSet;
        if (coll == null) {
            coll = new TreeSet<ClassData>();
            this.successorSet = coll;
            this.successorSetUnmod = Collections.unmodifiableSortedSet(coll);
        }
        return coll;
    }

    /**
     * Should only be used for modifications, to avoid useless lazy creation.
     */
    SortedSet<ClassData> predecessors_internal() {
        SortedSet<ClassData> coll = this.predecessorSet;
        if (coll == null) {
            coll = new TreeSet<ClassData>();
            this.predecessorSet = coll;
            this.predecessorSetUnmod = Collections.unmodifiableSortedSet(coll);
        }
        return coll;
    }

    /**
     * Should only be used for modifications, to avoid useless lazy creation.
     */
    SortedMap<String,ClassData> nestedClassByFileNameNoExt_internal() {
        SortedMap<String,ClassData> coll = this.nestedClassDataByFileNameNoExt;
        if (coll == null) {
            coll = new TreeMap<String,ClassData>();
            this.nestedClassDataByFileNameNoExt = coll;
            this.nestedClassDataByFileNameNoExtUnmod = Collections.unmodifiableSortedMap(coll);
        }
        return coll;
    }
    
    /**
     * Does not update byte size: must be done aside.
     */
    void setByteSizeForClassFileNameNoExt(String classFileNameNoExt, long byteSize) {
        this.byteSizeByClassFileNameNoExt_internal().put(classFileNameNoExt, byteSize);
    }
    
    void clearByteSizeByClassFileNameNoExt() {
        final SortedMap<String,Long> coll = this.byteSizeByClassFileNameNoExtSet;
        if (coll != null) {
            coll.clear();
        }
    }

    @Override
    boolean hasChild(AbstractCodeData data) {
        return false;
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    private Map<String,Long> byteSizeByClassFileNameNoExt_internal() {
        SortedMap<String,Long> coll = this.byteSizeByClassFileNameNoExtSet;
        if (coll == null) {
            coll = new TreeMap<String,Long>();
            this.byteSizeByClassFileNameNoExtSet = coll;
            this.byteSizeByClassFileNameNoExtSetUnmod = Collections.unmodifiableSortedMap(coll);
        }
        return coll;
    }
}
