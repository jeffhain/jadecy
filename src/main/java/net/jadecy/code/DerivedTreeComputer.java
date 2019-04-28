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

import java.util.Map;
import java.util.SortedSet;

import net.jadecy.names.InterfaceNameFilter;
import net.jadecy.names.NameFilters;
import net.jadecy.utils.ArgsUtils;

/**
 * Computes a derived default package data, more precisely a deep copy with
 * eventually inverse dependencies and filtered classes.
 */
public class DerivedTreeComputer {

    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------

    private static final boolean DEBUG = false;

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /**
     * @param defaultPackageData (in) Fully computed.
     * @param mustReverseDeps True if must replace non-inverse dependencies with
     *        reversed inverse dependencies, and inverse dependencies
     *        with reversed non-inverse dependencies, false otherwise.
     * @param retainedClassNameFilter Defines which classes must be present in
     *        the derived subtree, along with classes they directly depend on
     *        (after eventual dependencies reversal), and surrounding classes
     *        up to top level classes.
     * @return A derived deep copy of the specified package data.
     * @throws NullPointerException if the specified package data or filter is null.
     * @throws IllegalArgumentException if the specified PackageData does not correspond
     *         to default package, i.e. has a parent.
     */
    public static PackageData computeDerivedTree(
            PackageData defaultPackageData,
            boolean mustReverseDeps,
            InterfaceNameFilter retainedClassNameFilter) {

        ArgsUtils.requireNonNull(retainedClassNameFilter);

        // Implicit null check.
        if (defaultPackageData.parent() != null) {
            throw new IllegalArgumentException();
        }

        if (DEBUG) {
            System.out.println("mustReverseDeps = " + mustReverseDeps);
            System.out.println("retainedClassNameFilter = " + retainedClassNameFilter);
        }

        final PackageData derDefaultPackageData = new PackageData();

        computeDerivedSubtree(
                defaultPackageData,
                mustReverseDeps,
                retainedClassNameFilter,
                //
                derDefaultPackageData,
                derDefaultPackageData);

        return derDefaultPackageData;
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------

    private DerivedTreeComputer() {
    }

    /*
     * 
     */

    /**
     * This method is recursive.
     * 
     * @param packageData (in)
     * @param derDefaultPackageData (in,out)
     * @param derPackageData (out)
     */
    private static void computeDerivedSubtree(
            PackageData packageData,
            boolean mustReverseDeps,
            InterfaceNameFilter retainedClassNameFilter,
            //
            PackageData derDefaultPackageData,
            PackageData derPackageData) {

        if (DEBUG) {
            System.out.println("packageData = " + packageData);
        }

        for (ClassData childClassData : packageData.childClassDataByFileNameNoExt().values()) {

            if (DEBUG) {
                System.out.println("childClassData = " + childClassData);
            }

            final boolean mustRetainChildClass = retainedClassNameFilter.accept(childClassData.name());

            if (!mustRetainChildClass) {
                // The derived class might still be created as surrounding class,
                // or due to dependency.
                continue;
            }

            final ClassData derChildClassData = ensureDerivedClassData(
                    derPackageData,
                    childClassData.fileNameNoExt(),
                    childClassData);

            for (ClassData contClassData : (mustReverseDeps ? childClassData.predecessors() : childClassData.successors())) {

                if (DEBUG) {
                    System.out.println("contClassData = " + contClassData);
                }

                final ClassData derContClassData = ensureDerivedClassData(
                        derDefaultPackageData,
                        contClassData.name(),
                        contClassData);

                createDerivedDependency(
                        childClassData,
                        contClassData,
                        //
                        derChildClassData,
                        derContClassData,
                        //
                        mustReverseDeps);
            }
        }

        for (PackageData childPackageData : packageData.childPackageDataByDirName().values()) {

            final boolean mustGoDownInChildPackage = NameFilters.areCompatible(
                    retainedClassNameFilter.getPrefix(),
                    childPackageData.name());
            if (!mustGoDownInChildPackage) {
                // Wrong way.
                continue;
            }

            final PackageData derChildPackageData = derPackageData.getOrCreatePackageData(
                    childPackageData.fileNameNoExt(),
                    childPackageData);

            // Recursion.
            computeDerivedSubtree(
                    childPackageData,
                    mustReverseDeps,
                    retainedClassNameFilter,
                    //
                    derDefaultPackageData,
                    derChildPackageData);

            if (derChildPackageData.getSubtreeClassCount() == 0) {
                // Not keeping class-empty package trees.
                // Afterwards, package might be created again and kept,
                // due to containing a successor class or a retained class.
                PackageData.deletePackageData(derChildPackageData);
            }
        }
    }

    private static ClassData ensureDerivedClassData(
            PackageData derPackageData,
            String relativeClassName,
            ClassData original) {

        final ClassData derClassData = derPackageData.getOrCreateClassData(
                relativeClassName,
                original);

        for (Map.Entry<String,Long> entry : original.byteSizeByClassFileNameNoExt().entrySet()) {
            final String classFileNameNoExt = entry.getKey();
            final long byteSize = entry.getValue();
            // Doesn't hurt if already done.
            PackageData.setByteSizeForClassOrNested(
                    derClassData,
                    classFileNameNoExt,
                    byteSize);
        }

        return derClassData;
    }

    /**
     * @param retClass Retained class data.
     * @param contClass Contiguous (successor or predecessor) class data.
     * @param mustReverseDeps If true, then contiguous class
     *        is predecessor, else successor.
     */
    private static void createDerivedDependency(
            ClassData retClass,
            ClassData contClass,
            ClassData derRetClass,
            ClassData derContClass,
            boolean reverseDeps) {

        final PackageData retParent = (PackageData) retClass.parent();
        final PackageData contParent = (PackageData) contClass.parent();

        if (contParent == retParent) {
            final boolean forCheck = PackageData.ensureDependency(
                    derRetClass,
                    derContClass);
            if (!forCheck) {
                throw new AssertionError();
            }
        } else {
            final SortedSet<ClassData> causeSet;
            if (reverseDeps) {
                causeSet = retParent.causeSetByPredecessor().get(contParent);
            } else {
                causeSet = retParent.causeSetBySuccessor().get(contParent);
            }
            final boolean isNonInverseDep = causeSet.contains(retClass);
            final boolean isInverseDep = causeSet.contains(contClass);
            if (isNonInverseDep) {
                final boolean asInverseDep = false;
                final boolean forCheck = PackageData.ensureDependency(
                        derRetClass,
                        derContClass,
                        asInverseDep);
                if (!forCheck) {
                    throw new AssertionError();
                }
            }
            if (isInverseDep) {
                final boolean asInverseDep = true;
                // Might return false, if dependency already created just above,
                // and cause already there due to another already created dependency.
                PackageData.ensureDependency(
                        derRetClass,
                        derContClass,
                        asInverseDep);
            }
        }
    }
}
