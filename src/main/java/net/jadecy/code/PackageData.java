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
package net.jadecy.code;

import java.io.PrintStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import net.jadecy.utils.ArgsUtils;

/**
 * Data corresponding to a package.
 * 
 * The API of this class allows to create trees of PackageData and ClassData,
 * and dependencies between them, for use either by parsing treatments, by user
 * if wanting to create artificial data and dependencies, or in tests.
 * 
 * Classes or packages names can be invalid names, such as "0-virtual",
 * which allows for example to create virtual package dependencies
 * without risk of collisions with actual class names.
 * 
 * It is designed to be simple to use, and guard against misuses, which causes
 * a little overhead compared to what could be an optimized but complex and
 * error-prone API.
 * 
 * Returned collections instances might change on modification,
 * so you should not used cached references to them while modifying the tree.
 * 
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
public class PackageData extends AbstractCodeData {
    
    /*
     * Not overriding parent() and root() to use PackageData as return type,
     * because it wouldn't help that much and API would be less homogeneous
     * with ClassData.
     */
    
    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------
    
    private final SortedMap<String,ClassData> childClassDataByFileNameNoExt =
            new TreeMap<String,ClassData>();
    private final SortedMap<String,ClassData> childClassDataByFileNameNoExtUnmod =
            Collections.unmodifiableSortedMap(this.childClassDataByFileNameNoExt);
    
    private final SortedMap<String,PackageData> childPackageDataByDirName =
            new TreeMap<String,PackageData>();
    private final SortedMap<String,PackageData> childPackageDataByDirNameUnmod =
            Collections.unmodifiableSortedMap(this.childPackageDataByDirName);

    /**
     * key = package data for a package depended on, i.e. which some classes
     *       are depended on directly by some classes of this package.
     * values = class data for classes of this package which cause the
     *          dependency to the package corresponding to the key.
     */
    private final SortedMap<PackageData,SortedSet<ClassData>> causeSetBySuccessor =
            new TreeMap<PackageData,SortedSet<ClassData>>();
    private final SortedMap<PackageData,SortedSet<ClassData>> causeSetUnmodBySuccessor =
            new TreeMap<PackageData,SortedSet<ClassData>>();
    private final SortedMap<PackageData,SortedSet<ClassData>> causeSetUnmodBySuccessorUnmod =
            Collections.unmodifiableSortedMap(this.causeSetUnmodBySuccessor);
    
    private final Set<PackageData> successorSet = this.causeSetBySuccessor.keySet();
    private final Set<PackageData> successorSetUnmod =
            Collections.unmodifiableSet(this.successorSet);
    
    private final SortedMap<PackageData,SortedSet<ClassData>> causeSetByPredecessor =
            new TreeMap<PackageData,SortedSet<ClassData>>();
    private final SortedMap<PackageData,SortedSet<ClassData>> causeSetUnmodByPredecessor =
            new TreeMap<PackageData,SortedSet<ClassData>>();
    private final SortedMap<PackageData,SortedSet<ClassData>> causeSetUnmodByPredecessorUnmod =
            Collections.unmodifiableSortedMap(this.causeSetUnmodByPredecessor);
    
    private final Set<PackageData> predecessorSet = this.causeSetByPredecessor.keySet();
    private final Set<PackageData> predecessorSetUnmod =
            Collections.unmodifiableSet(this.predecessorSet);

    /*
     * Subtree stuffs.
     * Class count could be computed from other values, but having it here
     * allows to greatly speed some things up.
     */
    
    private long subtreeClassCount = 0;
    
    /**
     * Incremented one or multiple times each time one or multiple classes, or
     * successor or predecessor dependencies, are created or deleted somewhere
     * in the subtree this package is the root of.
     * 
     * When appropriate, boolean results are also computed (but not relying on
     * mod count, to keep things decoupled), so that user can figure out whether
     * modifications occurred without comparing old and new mod counts.
     */
    private long subtreeModCount = 0;

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    /**
     * Creates data for default package.
     */
    public PackageData() {
        super(null, null, null);
    }
    
    /*
     * 
     */
    
    /**
     * @return The number of classes in the subtree this package is the root of.
     */
    public long getSubtreeClassCount() {
        return this.subtreeClassCount;
    }

    /**
     * @return A value incremented one or multiple times each time one or
     *         multiple classes or packages, or classes dependencies (and
     *         incidentally packages dependencies), are created or deleted
     *         somewhere in the subtree this package is the root of.
     */
    public long getSubtreeModCount() {
        return this.subtreeModCount;
    }

    /*
     * 
     */
    
    /**
     * @return An unmodifiable view of the internal map, or an empty unmodifiable map.
     */
    public Map<String,ClassData> childClassDataByFileNameNoExt() {
        return this.childClassDataByFileNameNoExtUnmod;
    }
    
    /**
     * @return An unmodifiable view of the internal map, or an empty unmodifiable map.
     */
    public Map<String,PackageData> childPackageDataByDirName() {
        return this.childPackageDataByDirNameUnmod;
    }
    
    /**
     * The map contains, for each successor, the causes of the dependency from this package to the successor,
     * i.e. all classes of this package that have a non-inverse dependency to a class of the successor,
     * plus all classes of the successor that are inversely depended on by a class of this package.
     *         
     * @return An unmodifiable view of the internal map with unmodifiable views of its sets,
     *         or an empty unmodifiable map.
     */
    public Map<PackageData,SortedSet<ClassData>> causeSetBySuccessor() {
        return this.causeSetUnmodBySuccessorUnmod;
    }
    
    /**
     * @return An unmodifiable view of the internal set, or an empty unmodifiable set.
     */
    //Override
    public Set<PackageData> successors() {
        return this.successorSetUnmod;
    }
    
    /**
     * The map contains, for each predecessor, the causes of the dependency from the predecessor to this package,
     * i.e. all classes of the predecessor that have a non-inverse dependency to a class of this package,
     * plus all classes of this package that are inversely depended on by a class of the predecessor.
     * 
     * @return An unmodifiable view of the internal map with unmodifiable views of its sets,
     *         or an empty unmodifiable map.
     */
    public Map<PackageData,SortedSet<ClassData>> causeSetByPredecessor() {
        return this.causeSetUnmodByPredecessorUnmod;
    }
    
    /**
     * @return An unmodifiable view of the internal set, or an empty unmodifiable set.
     */
    //Override
    public Set<PackageData> predecessors() {
        return this.predecessorSetUnmod;
    }

    /*
     * 
     */

    /**
     * Clears this PackageData, i.e. deletes all of its child classes and packages,
     * but does not delete it.
     * 
     * Useful to clear all data by calling it on default package.
     * 
     * Does not just remove children from this PackageData, but deletes all of
     * them recursively, for less risk of user believing they are still
     * up-to-date if he mistakenly use them afterwards.
     * 
     * @throws IllegalStateException if this PackageData is deleted.
     */
    public void clear() {
        
        if (this.isDeleted()) {
            throw new IllegalStateException("deleted: " + this);
        }

        /*
         * If called on default package data, could be optimized by just recursively
         * clearing collections and zeroing byte sizes etc., but that would add
         * more code.
         */
        
        deleteChildrenIfAny(this);
    }

    /*
     * Class or package retrieval in subtree.
     */

    /**
     * @param relativeClassName Class name which data must be returned if found,
     *        starting after this package data's name.
     * @return Matching class data, or null if none, or if matching data
     *         was a package data.
     * @throws NullPointerException if the specified name is null.
     * @throws IllegalArgumentException if name has dots in wrong places or is empty.
     */
    public ClassData getClassData(String relativeClassName) {
        return (ClassData) getCodeData(relativeClassName, false);
    }
    
    /**
     * @param relativePackageName Package name which data must be returned if found,
     *        starting after this package data's name.
     *        Can be an empty string, in which case this package is returned.
     * @return Matching package data, or null if none, or if matching data
     *         was a class data.
     */
    public PackageData getPackageData(String relativePackageName) {
        // getCodeData(...) doesn't accept empty names,
        // so we need to special-case here.
        if (relativePackageName.length() == 0) {
            return this;
        }
        
        return (PackageData) getCodeData(relativePackageName, true);
    }
    
    /*
     * Class or package retrieval or creation in subtree.
     */

    public ClassData getOrCreateClassData(String relativeClassName) {
        final ClassData original = null;
        return this.getOrCreateClassData(relativeClassName, original);
    }
    
    public PackageData getOrCreatePackageData(String relativePackageName) {
        final PackageData original = null;
        return this.getOrCreatePackageData(relativePackageName, original);
    }
    
    /**
     * If returns true, also increments the byte size of the specified ClassData
     * and its parent PackageData with the specified byte size.
     * 
     * @param classData ClassData corresponding to fileNameNoExt, or one
     *        of its outer classes (typically the top level one).
     * @param classFileNameNoExt File name no ext corresponding to either the
     *        specified ClassData or one of its (recursively) nested classes.
     * @param byteSize Byte size to set for the specified class file name no ext.
     *        Must be > 0.
     * @return True if a byte size was not already set for the specified
     *         class file name no ext, false otherwise.
     * @throws NullPointerException if classData or classFileNameNoExt is null.
     * @throws IllegalArgumentException if classData is deleted, or if fileNameNoExt
     *         doesn't start with the name classData.fileNameNoExt(), or if the
     *         specified byte size is <= 0.
     */
    public static boolean setByteSizeForClassOrNested(
            ClassData classData,
            String classFileNameNoExt,
            long byteSize) {
        
        // Implicit null check.
        if (classData.isDeleted()) {
            throw new IllegalArgumentException("is deleted: " + classData);
        }
        
        // Implicit null check.
        final boolean isClassOrNested = NameUtils.startsWithName(classFileNameNoExt, classData.fileNameNoExt());
        if (!isClassOrNested) {
            throw new IllegalArgumentException(
                    classFileNameNoExt
                    + " name not included in "
                    + classData.fileNameNoExt());
        }
        
        if (byteSize <= 0) {
            throw new IllegalArgumentException(Long.toString(byteSize));
        }
        
        final boolean isByteSizeSet = classData.byteSizeByClassFileNameNoExt().containsKey(classFileNameNoExt);
        if (isByteSizeSet) {
            // Not complaining if the specified byte size is different than previous one.
            return false;
        } else {
            // Only need to check that here, since only non-null names make it into the map.
            ArgsUtils.requireNonNull(classFileNameNoExt);
            
            classData.setByteSizeForClassFileNameNoExt(classFileNameNoExt, byteSize);
            
            classData.addByteSize(byteSize);
            
            final PackageData packageData = (PackageData) classData.parent();
            packageData.addByteSize(byteSize);
            
            packageData.incrementModCounts();
            
            return true;
        }
    }

    /*
     * Class or package deletion.
     */
    
    /**
     * This method is recursive.
     * 
     * Removes the specified class, and all of its nested classes,
     * from its parent, and deletes all related dependencies, and
     * sets its byte size to 0.
     * 
     * After call, the specified instance must no longer be used.
     * 
     * @return True if was deleted by this call, false if was already deleted.
     * @throws NullPointerException if classData is null.
     */
    public static boolean deleteClassData(ClassData classData) {
        
        // Implicit null check.
        if (classData.isDeleted()) {
            return false;
        }
        
        final PackageData packageData = (PackageData) classData.parent();
        
        /*
         * Deleting nested classes, if any.
         */
        
        if (classData.nestedClassByFileNameNoExt().size() != 0) {
            final Object[] nestedClassDataArr = classData.nestedClassByFileNameNoExt().values().toArray();
            for (Object ncd : nestedClassDataArr) {
                final ClassData nestedClassData = (ClassData) ncd;
                // Recursion.
                deleteClassData(nestedClassData);
            }
        }
        
        /*
         * Deleting dependencies.
         */
        
        final Set<PackageData> modifiedPackageDataSet = new HashSet<PackageData>();

        // Deleting dependencies to successor classes, if any.
        if (classData.successors().size() != 0) {
            final Object[] succArr = classData.successors().toArray();
            for (Object succ : succArr) {
                final ClassData succClassData = (ClassData) succ;
                deleteExistingDependency(classData, succClassData, classData);

                modifiedPackageDataSet.add((PackageData) succClassData.parent());
            }
        }

        // Deleting dependencies from predecessor classes, if any.
        if (classData.predecessors().size() != 0) {
            final Object[] predArr = classData.predecessors().toArray();
            for (Object pred : predArr) {
                final ClassData predClassData = (ClassData) pred;
                deleteExistingDependency(predClassData, classData, classData);

                modifiedPackageDataSet.add((PackageData) predClassData.parent());
            }
        }
        
        /*
         * Removing from outer class.
         */

        if (classData.outerClassData() != null) {
            final SortedMap<String,ClassData> map = classData.outerClassData().nestedClassByFileNameNoExt_internal();
            final Object forCheck = map.remove(classData.fileNameNoExt());
            if (forCheck != classData) {
                throw new AssertionError();
            }
        }

        /*
         * Removing from parent.
         */

        final Object forCheck = packageData.childClassDataByFileNameNoExt.remove(classData.fileNameNoExt());
        if (forCheck != classData) {
            throw new AssertionError();
        }
        
        packageData.addByteSize(-classData.byteSize());
        
        packageData.addClassCount(-1);
        
        /*
         * Zeroing class byte size.
         */
        
        // Negating always works because byte size is always positive.
        classData.addByteSize(-classData.byteSize());
        
        classData.clearByteSizeByClassFileNameNoExt();
        
        /*
         * 
         */

        modifiedPackageDataSet.add(packageData);
        
        for (PackageData modifiedPackageData : modifiedPackageDataSet) {
            modifiedPackageData.incrementModCounts();
        }
        
        return true;
    }

    /**
     * This method is recursive.
     * 
     * Removes the specified PackageData, and all of its subtree,
     * and deletes all related dependencies.
     * 
     * After call, the specified instance, and all of its subtree (classes and packages),
     * must no longer be used.
     * 
     * @param packageData PackageData to delete.
     * @return True if was deleted by this call, false if was already deleted.
     * @throws IllegalArgumentException if packageData corresponds to
     *         default package.
     */
    public static boolean deletePackageData(PackageData packageData) {
        
        // Implicit null check.
        final PackageData parent = (PackageData) packageData.parent();
        if (parent == null) {
            throw new IllegalArgumentException("default package data");
        }
        
        if (packageData.isDeleted()) {
            return false;
        }

        deleteChildrenIfAny(packageData);
        
        /*
         * Removing from parent.
         */

        final PackageData forCheck = parent.childPackageDataByDirName.remove(packageData.fileNameNoExt());
        if (forCheck != packageData) {
            throw new AssertionError();
        }

        /*
         * 
         */
        
        parent.incrementModCounts();
        
        return true;
    }

    /*
     * Dependency ensuring.
     */
    
    /**
     * Ensures a non-inverse dependency from classA to classB.
     * 
     * Equivalent to ensureDependency(classA,classB,false).
     * 
     * @param classA A ClassData. Must not be null.
     * @param classB Another ClassData. Must not be null.
     * @return True if the non-inverse dependency to ensure did not exist already
     *         and was created, false if it did exist already, in which case
     *         nothing has been modified.
     * @throws NullPointerException if either classA or classB is null.
     * @throws IllegalArgumentException if classA or classB is deleted,
     *         or if they have different roots or are a same instance.
     */
    public static boolean ensureDependency(
            ClassData classA,
            ClassData classB) {
        boolean asInverseDep = false;
        return ensureDependency(
                classA,
                classB,
                asInverseDep);
    }
    
    /**
     * Ensures a non-inverse or inverse dependency from classA to classB.
     * 
     * There is no difference between non-inverse and inverse dependencies
     * within a same package.
     * If the dependency is not within a same package, then it has a cause,
     * which is the source class if it's non-inverse, and the destination
     * class if it's inverse.
     * 
     * A dependency from a class to another can be ensured as both non-inverse
     * and inverse, even though it doesn't make much sense:
     * one typically builds up a graph of only non-inverse or only inverse
     * dependencies, depending on what must be computed at a higher level
     * (like non-inverse dependencies to compute classes depended on,
     * and inverse dependencies to compute depending classes).
     * 
     * Note that a dependency across packages is considered as non-inverse
     * as long as the source class is a cause of it in one of the packages,
     * even if the cause presence is due to the source class non-inversely
     * depending on another class of destination package, and similarly
     * for inverse dependencies.
     * As a result, it's really unwise (doesn't correspond to anything real
     * or consistent) to mix up non-inverse and inverse dependencies within
     * a same tree.
     * 
     * @param classA A ClassData. Must not be null.
     * @param classB Another ClassData. Must not be null.
     * @param asInverseDep Whether the ensured dependency from classA to classB
     *        must be an inverse dependency (only has effect if their packages
     *        are different).
     * @return True if the dependency to ensure did not exist already, with the
     *         proper type (non-inverse or inverse), and was created, false if
     *         it did exist already, in which case nothing has been modified.
     * @throws NullPointerException if either classA or classB is null.
     * @throws IllegalArgumentException if classA or classB is deleted,
     *         or if they have different roots or are a same instance.
     */
    public static boolean ensureDependency(
            ClassData classA,
            ClassData classB,
            boolean asInverseDep) {
        
        // Implicit null checks.
        if (classA.isDeleted()) {
            throw new IllegalArgumentException("deleted: " + classA);
        }
        if (classB.isDeleted()) {
            throw new IllegalArgumentException("deleted: " + classB);
        }
        
        final PackageData packageA = (PackageData) classA.parent();
        final PackageData packageB = (PackageData) classB.parent();

        if (packageA.root() != packageB.root()) {
            throw new IllegalArgumentException("classes have different roots");
        }
        
        if (classA == classB) {
            throw new IllegalArgumentException("dependency to self");
        }
        
        boolean modified = false;
        
        {
            final boolean didAdd = classA.successors_internal().add(classB);
            if (didAdd) {
                modified = true;
                final boolean didAddInv = classB.predecessors_internal().add(classA);
                if (!didAddInv) {
                    throw new AssertionError();
                }
            }
        }
        
        if (packageA != packageB) {
            SortedSet<ClassData> causeSet = packageA.causeSetBySuccessor.get(packageB);
            SortedSet<ClassData> invCauseSet = packageB.causeSetByPredecessor.get(packageA);
            if (causeSet == null) {
                if (invCauseSet != null) {
                    throw new AssertionError();
                }
                causeSet = new TreeSet<ClassData>();
                packageA.causeSetBySuccessor.put(packageB, causeSet);
                packageA.causeSetUnmodBySuccessor.put(packageB, Collections.unmodifiableSortedSet(causeSet));
                invCauseSet = new TreeSet<ClassData>();
                packageB.causeSetByPredecessor.put(packageA, invCauseSet);
                packageB.causeSetUnmodByPredecessor.put(packageA, Collections.unmodifiableSortedSet(invCauseSet));
            } else {
                if (invCauseSet == null) {
                    throw new AssertionError();
                }
            }
            
            // If dependency is inverse, the cause is the destination,
            // not the source.
            // Note that the cause does not belong to the package of
            // one of the set it's added in.
            final ClassData cause;
            if (asInverseDep) {
                cause = classB;
            } else {
                cause = classA;
            }
            final boolean didAdd = causeSet.add(cause);
            if (didAdd) {
                modified = true;
                final boolean didAddInv = invCauseSet.add(cause);
                if (!didAddInv) {
                    throw new AssertionError();
                }
            }
        }

        if (modified) {
            packageA.incrementModCounts();
            if (packageB == packageA) {
                // Could, but no need to.
            } else {
                packageB.incrementModCounts();
            }
        }
        
        return modified;
    }

    /*
     * Dependency deletion.
     */
    
    /**
     * Deletes eventual dependency from classA to classB, whether it exists
     * as non-inverse dependency, inverse dependency, or both.
     * 
     * Unlike ensureDependency(...), this method tolerates both arguments
     * to be a same instance, in which case it necessarily returns false since
     * we can't have a dependency to self.
     * 
     * @param classA Must not be null.
     * @param classB Must not be null.
     * @return True if a dependency from classA to classB existed and was deleted,
     *         false otherwise.
     * @throws NullPointerException if either classA or classB is null.
     * @throws IllegalArgumentException if classA or classB is deleted,
     *         or if they have different roots.
     */
    public static boolean deleteDependency(
            ClassData classA,
            ClassData classB) {
        
        // Implicit null checks.
        if (classA.isDeleted()) {
            throw new IllegalArgumentException("deleted: " + classA);
        }
        if (classB.isDeleted()) {
            throw new IllegalArgumentException("deleted: " + classB);
        }
        
        final PackageData packageA = (PackageData) classA.parent();
        final PackageData packageB = (PackageData) classB.parent();
        
        if (packageA.root() != packageB.root()) {
            throw new IllegalArgumentException("classes have different roots");
        }
        
        boolean modified = false;
        
        // If classA is classB, is false since we can't have dependency to self.
        if (classA.successors().contains(classB)) {
            deleteExistingDependency(classA, classB, null);
            modified = true;
        }

        if (modified) {
            packageA.incrementModCounts();
            packageB.incrementModCounts();
        }
        
        return modified;
    }

    /*
     * Print.
     */

    /**
     * Prints into System.out, not verbose.
     */
    public void printSubtree() {
        this.printSubtree(
                System.out,
                false);
    }

    /**
     * This method is recursive.
     * 
     * Verbose doesn't show mod counts because it's just a technical indication.
     * 
     * @param stream Stream to print to.
     * @param verbose True if must give all details (except mod counts),
     *        false otherwise.
     * @throws NullPointerException if the specified stream is null.
     */
    public void printSubtree(
            PrintStream stream,
            boolean verbose) {
        
        // Implicit null check.
        if (verbose) {
            stream.println(this + " (" + this.byteSize() +  " bytes)");
            stream.println(" causeSetBySuccessor = " + this.causeSetBySuccessor);
            stream.println(" causeSetByPredecessor = " + this.causeSetByPredecessor);
        } else {
            stream.println(this);
        }

        for (ClassData childClassData : this.childClassDataByFileNameNoExt().values()) {
            if (verbose) {
                stream.println(
                        " "
                                + childClassData
                                + " ("
                                + childClassData.byteSize()
                                + " bytes: "
                                + childClassData.byteSizeByClassFileNameNoExt()
                                + ")");
            } else {
                stream.println(" " + childClassData);
            }
            for (ClassData succ : childClassData.successors()) {
                stream.println("   -> " + succ);
            }
        }
        
        for (PackageData childPackageData : this.childPackageDataByDirName().values()) {
            childPackageData.printSubtree(
                    stream,
                    verbose);
        }
    }

    //--------------------------------------------------------------------------
    // PACKAGE-PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    /**
     * To reuse string instances from original data, when deriving a default
     * package data.
     * No check is done on the specified ClassData, for performances, because
     * it is only non-null for calls by trusted code.
     * 
     * @param original For string reuse. Can be null.
     */
    ClassData getOrCreateClassData(
            String relativeClassName,
            ClassData original) {
        final String[] relativeNameParts = NameUtils.splitName(relativeClassName);
        return this.getOrCreateClassData(
                relativeNameParts,
                original);
    }
    
    PackageData getOrCreatePackageData(
            String relativePackageName,
            PackageData original) {
        // Implicit null check.
        if (relativePackageName.length() == 0) {
            return this;
        }
        
        final String[] dirNames = NameUtils.splitName(relativePackageName);
        return getOrCreatePackageData(dirNames, dirNames.length, original);
    }

    @Override
    boolean hasChild(AbstractCodeData data) {
        final String fileNameNoExt = data.fileNameNoExt();
        if (data instanceof ClassData) {
            return this.childClassDataByFileNameNoExt.get(fileNameNoExt) == data;
        } else {
            return this.childPackageDataByDirName.get(fileNameNoExt) == data;
        }
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    /**
     * Typically, this package must subsequently be added as child of its parent,
     * for it is not done by this constructor.
     * 
     * @param parent Must not be null.
     * @param dirName Must not be null.
     * @param original For string reuse. Can be null.
     */
    private PackageData(
            PackageData parent,
            String dirName,
            PackageData original) {
        super(parent, dirName, original);
        // Just need to test one, as super does test args consistency.
        ArgsUtils.requireNonNull(parent);
    }
    
    /*
     * 
     */

    private void addClassCount(int toAdd) {
        PackageData tmp = this;
        while (tmp != null) {
            tmp.subtreeClassCount += toAdd;
            tmp = (PackageData) tmp.parent();
        }
    }

    /**
     * Increment mod counts from this package up to default package.
     */
    private void incrementModCounts() {
        PackageData tmp = this;
        while (tmp != null) {
            tmp.subtreeModCount++;
            tmp = (PackageData) tmp.parent();
        }
    }

    /*
     * Class or package data retrieval.
     */
    
    /**
     * @param relativeName Package or class name which data must be returned if found,
     *        starting after this package data's name. Must not be empty.
     * @param wantPackage True if looking for a PackageData,
     *        false if looking for a ClassData.
     * @return The matching package or class data found in the sub tree
     *         this package data is the root of, or null if none.
     * @throws IllegalArgumentException if name has dots in wrong places or is empty.
     */
    private AbstractCodeData getCodeData(String relativeName, boolean wantPackage) {
        final String[] parts = NameUtils.splitName(relativeName);
        PackageData packageData = this;
        for (int i = 0; i < parts.length; i++) {
            final String part = parts[i];
            final boolean lastPart = (i == parts.length-1);
            if (lastPart) {
                if (wantPackage) {
                    return packageData.childPackageDataByDirName().get(part);
                } else {
                    return packageData.childClassDataByFileNameNoExt().get(part);
                }
            } else {
                final PackageData childPackageData = packageData.childPackageDataByDirName().get(part);
                if (childPackageData != null) {
                    // Found a package with that name,
                    // will try to go down further.
                    packageData = childPackageData;
                    continue;
                } else {
                    // Nothing matches.
                    break;
                }
            }
        }
        return null;
    }
    
    /*
     * Package data retrieval or creation.
     */
    
    /**
     * @param original For string reuse. Can be null.
     */
    private PackageData getOrCreatePackageData(
            String[] dirNames,
            int length,
            PackageData original) {
        if (length <= 0) {
            if (length < 0) {
                throw new IllegalArgumentException(Integer.toString(length));
            }
            return this;
        }
        
        PackageData firstPackageDataModified = null;
        
        PackageData tmpPackageData = this;
        for (int i = 0; i < length; i++) {
            final String dirName = dirNames[i];
            PackageData tmpChild = tmpPackageData.getPackageData(dirName);
            if (tmpChild == null) {
                
                final PackageData originalChild = getOriginalForChild(
                        tmpPackageData,
                        dirName,
                        original);
                
                tmpChild = tmpPackageData.createChildPackageData(
                        dirName,
                        originalChild);

                if (firstPackageDataModified == null) {
                    firstPackageDataModified = tmpPackageData;
                }
            }
            tmpPackageData = tmpChild;
        }
        
        if (firstPackageDataModified != null) {
            firstPackageDataModified.incrementModCounts();
        }
        
        return tmpPackageData;
    }

    /**
     * Does NOT increment mod count.
     * 
     * @param original For string reuse. Can be null.
     */
    private PackageData createChildPackageData(
            String dirName,
            PackageData original) {
        final PackageData result = new PackageData(this, dirName, original);
        // Using the String instance of the PackageData as key.
        final Object forCheck = this.childPackageDataByDirName.put(result.fileNameNoExt(), result);
        if (forCheck != null) {
            throw new AssertionError();
        }
        return result;
    }

    /*
     * Class data retrieval or creation.
     */
    
    /**
     * @param original For string reuse. Can be null.
     */
    private ClassData getOrCreateClassData(
            String[] relativeNameParts,
            ClassData original) {
        final PackageData packageData = this.getOrCreatePackageData(
                relativeNameParts,
                relativeNameParts.length-1,
                getOriginalParent(original));
        final String classFileNameNoExt = relativeNameParts[relativeNameParts.length-1];
        return packageData.getOrCreateChildClassData(
                classFileNameNoExt,
                original);
    }
    
    /**
     * getOrCreateChildClassData(...) and getOrCreateOuterClassDataElseNull(...)
     * are reentrant.
     * 
     * @param original For string reuse. Can be null.
     */
    private ClassData getOrCreateChildClassData(
            String classFileNameNoExt,
            ClassData original) {
        ClassData result = this.childClassDataByFileNameNoExt.get(classFileNameNoExt);
        if (result == null) {
            final PackageData packageData = this;
            final ClassData outerClassData = getOrCreateOuterClassDataElseNull(
                    packageData,
                    classFileNameNoExt,
                    getOriginalOuterClassData(original));
            result = new ClassData(
                    packageData,
                    classFileNameNoExt,
                    outerClassData,
                    original);
            
            // Using the String instance of the ClassData as key.
            this.childClassDataByFileNameNoExt.put(result.fileNameNoExt(), result);
            
            packageData.addClassCount(1);
            
            packageData.incrementModCounts();
        }
        return result;
    }

    /**
     * @param original For string reuse. Can be null.
     */
    private static ClassData getOrCreateOuterClassDataElseNull(
            final PackageData parent,
            String classFileNameNoExt,
            ClassData original) {
        final String outerClassFileNameNoExt =
                NameUtils.getOuterClassFileNameNoExt(classFileNameNoExt);
        if (outerClassFileNameNoExt != null) {
            return parent.getOrCreateChildClassData(
                    outerClassFileNameNoExt,
                    original);
        } else {
            return null;
        }
    }
    
    /*
     * Data deletion.
     */
    
    private static void deleteChildrenIfAny(PackageData packageData) {
        
        /*
         * Deleting child packages, if any.
         */

        if (packageData.childPackageDataByDirName().size() != 0) {
            final Object[] childPackageDataArr = packageData.childPackageDataByDirName.values().toArray();
            for (Object cpd : childPackageDataArr) {
                final PackageData childPackageData = (PackageData) cpd;
                deletePackageData(childPackageData);
            }
        }
        
        /*
         * Deleting child classes, if any.
         */

        if (packageData.childClassDataByFileNameNoExt().size() != 0) {
            final Object[] childClassDataArr = packageData.childClassDataByFileNameNoExt.values().toArray();
            for (Object ccd : childClassDataArr) {
                final ClassData childClassData = (ClassData) ccd;
                deleteClassData(childClassData);
            }
        }
    }
    
    /*
     * Dependency deletion.
     */
    
    /**
     * Deletes dependency from classA to classB,
     * whether it is inverse or not.
     * 
     * Does NOT increment mod count.
     * 
     * @param classBeingDeleted Can be null. Useful to avoid keeping causes because
     *        of a dependency from or to a class being deleted.
     */
    private static void deleteExistingDependency(
            ClassData classA,
            ClassData classB,
            ClassData classBeingDeleted) {
        
        /*
         * Deleting dependency in ClassData.
         */
        
        {
            final boolean forCheck = classA.successors_internal().remove(classB);
            if (!forCheck) {
                throw new AssertionError();
            }
        }
        {
            final boolean forCheck = classB.predecessors_internal().remove(classA);
            if (!forCheck) {
                throw new AssertionError();
            }
        }
        
        /*
         * Removing classA from cause sets if is no more a cause
         * of dependency from classA.parent() to classB.parent().
         */
        
        final PackageData packageA = (PackageData) classA.parent();
        final PackageData packageB = (PackageData) classB.parent();
        
        if (packageA == packageB) {
            // No dependency to self for packages (no for classes).
            return;
        }

        /*
         * Eventual cause cleanup for non-inverse dependencies.
         */
        
        {
            boolean foundDepFromClassAToPackageB = false;
            for (ClassData classASucc : classA.successors()) {
                if ((classASucc != classBeingDeleted)
                        && (classASucc.parent() == packageB)) {
                    foundDepFromClassAToPackageB = true;
                    break;
                }
            }
            
            final boolean canClassAStillBeACauseOfNonInverseDepFromPackageAToPackageB =
                    !foundDepFromClassAToPackageB;
            
            if (canClassAStillBeACauseOfNonInverseDepFromPackageAToPackageB) {
                removeFromCauseSetIfIn(packageA, packageB, classA);
            }
        }
        
        /*
         * Eventual cause cleanup for inverse dependencies.
         */
        
        {
            boolean foundDepFromPackageAToClassB = false;
            for (ClassData classBPred : classB.predecessors()) {
                if ((classBPred != classBeingDeleted)
                        && (classBPred.parent() == packageA)) {
                    foundDepFromPackageAToClassB = true;
                    break;
                }
            }
            
            final boolean canClassBStillBeACauseOfInverseDepFromPackageAToPackageB =
                    !foundDepFromPackageAToClassB;
            
            if (canClassBStillBeACauseOfInverseDepFromPackageAToPackageB) {
                removeFromCauseSetIfIn(packageA, packageB, classB);
            }
        }
    }
    
    private static void removeFromCauseSetIfIn(
            PackageData packageWhereCauseForSucc,
            PackageData packageWhereCauseForPred,
            ClassData cause) {
        {
            SortedSet<ClassData> causeSet = packageWhereCauseForSucc.causeSetBySuccessor.get(packageWhereCauseForPred);
            if (causeSet != null) {
                final boolean didRemove = causeSet.remove(cause);
                if (didRemove) {
                    if (causeSet.size() == 0) {
                        packageWhereCauseForSucc.causeSetBySuccessor.remove(packageWhereCauseForPred);
                        packageWhereCauseForSucc.causeSetUnmodBySuccessor.remove(packageWhereCauseForPred);
                    }
                }
            }
        }
        {
            SortedSet<ClassData> causeSet = packageWhereCauseForPred.causeSetByPredecessor.get(packageWhereCauseForSucc);
            if (causeSet != null) {
                final boolean didRemove = causeSet.remove(cause);
                if (didRemove) {
                    if (causeSet.size() == 0) {
                        packageWhereCauseForPred.causeSetByPredecessor.remove(packageWhereCauseForSucc);
                        packageWhereCauseForPred.causeSetUnmodByPredecessor.remove(packageWhereCauseForSucc);
                    }
                }
            }
        }
    }
    
    /*
     * Original stuffs.
     */
    
    /**
     * @param packageData Some PackageData.
     * @param childDirName Dir name of a child package of packageData.
     * @param terminalOriginal Original which name starts with packageData name
     *        followed (after a dot if not empty) by childDirName. Can be null.
     * @return The original corresponding to specified child,
     *         or null if terminalOriginal is null.
     */
    private static PackageData getOriginalForChild(
            PackageData packageData,
            String childDirName,
            PackageData terminalOriginal) {
        final PackageData originalChild;
        if (terminalOriginal == null) {
            originalChild = null;
        } else {
            final PackageData originalRoot = (PackageData) terminalOriginal.root();
            // Must not be null since terminalOriginal must start with
            // the name of the PackageData to return.
            if (packageData.parent() == null) {
                // Default package.
                originalChild = originalRoot.getPackageData(childDirName);
            } else {
                originalChild = originalRoot.getPackageData(packageData.name() + "." + childDirName);
            }
        }
        return originalChild;
    }
    
    /**
     * @param original Can be null.
     */
    private static PackageData getOriginalParent(AbstractCodeData original) {
        return (original != null) ? (PackageData) original.parent() : null;
    }
    
    /**
     * @param original Can be null.
     */
    private static ClassData getOriginalOuterClassData(ClassData original) {
        return (original != null) ? original.outerClassData() : null;
    }
}
