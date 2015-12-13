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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.jadecy.graph.InterfaceVertex;
import net.jadecy.utils.ArgsUtils;

/**
 * Utilities to deal with PackageData and ClassData.
 */
public class CodeDataUtils {

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /**
     * Classes are sorted first by package name,
     * and within each package, by class name,
     * according to String comparator.
     * 
     * @param packageData A package data. Must not be null.
     * @param classNameFilter A name filter. Must not be null.
     * @return A new list of ClassData found in the subtree of the
     *         specified PackageData and which names match the specified filter.
     * @throws NullPointerException if the specified package data or name filter
     *         is null.
     */
    public static List<InterfaceVertex> newClassDataList(
            PackageData packageData,
            InterfaceNameFilter classNameFilter) {
        
        ArgsUtils.requireNonNull(packageData);
        ArgsUtils.requireNonNull(classNameFilter);

        final List<InterfaceVertex> classDataColl = new ArrayList<InterfaceVertex>();

        addClassDataInto(
                packageData,
                classNameFilter,
                classDataColl);

        return classDataColl;
    }

    /**
     * Packages are sorted by name, according to String comparator.
     * 
     * @param packageData A package data. Must not be null.
     * @param packageNameFilter A name filter. Must not be null.
     * @return A new list of PackageData found in the subtree of the
     *         specified PackageData and which names match the specified filter.
     * @throws NullPointerException if the specified package data or name filter
     *         is null.
     */
    public static List<InterfaceVertex> newPackageDataList(
            PackageData packageData,
            InterfaceNameFilter packageNameFilter) {

        ArgsUtils.requireNonNull(packageData);
        ArgsUtils.requireNonNull(packageNameFilter);

        final List<InterfaceVertex> packageDataColl = new ArrayList<InterfaceVertex>();

        addPackageDataInto(
                packageData,
                packageNameFilter,
                packageDataColl);

        return packageDataColl;
    }
    
    /*
     * 
     */
    
    /**
     * @return True if the specified collection only contains classes
     *         being or contained within a same top level class, false
     *         otherwise.
     * @throws NullPointerException if the specified collection is null.
     */
    public static boolean haveSameTopLevelClass(Collection<? super ClassData> classDataColl) {
        ClassData refTopLevelCD = null;
        for (Object _classData : classDataColl) {
            final ClassData classData = (ClassData) _classData;
            final ClassData topLevelCD = classData.topLevelClassData();
            if (refTopLevelCD == null) {
                refTopLevelCD = topLevelCD;
            } else {
                if (topLevelCD != refTopLevelCD) {
                    return false;
                }
            }
        }
        return true;
    }
    
    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------

    private CodeDataUtils() {
    }
    
    /*
     * 
     */
    
    /**
     * This method is recursive.
     */
    private static void addClassDataInto(
            PackageData packageData,
            InterfaceNameFilter classNameFilter,
            Collection<? super ClassData> classDataColl) {

        if (!NameFilters.areCompatible(
                classNameFilter.getPrefix(),
                packageData.name())) {
            // Wrong way.
            return;
        }

        for (ClassData childData : packageData.childClassDataByFileNameNoExt().values()) {
            if (classNameFilter.accept(childData.name())) {
                classDataColl.add(childData);
            }
        }
        
        for (PackageData childData : packageData.childPackageDataByDirName().values()) {
            addClassDataInto(
                    childData,
                    classNameFilter,
                    classDataColl);
        }
    }

    /**
     * This method is recursive.
     */
    private static void addPackageDataInto(
            PackageData packageData,
            InterfaceNameFilter packageNameFilter,
            Collection<? super PackageData> packageDataColl) {

        if (!NameFilters.areCompatible(
                packageNameFilter.getPrefix(),
                packageData.name())) {
            // Wrong way.
            return;
        }

        if (packageNameFilter.accept(packageData.name())) {
            packageDataColl.add(packageData);
        }
        
        for (PackageData childData : packageData.childPackageDataByDirName().values()) {
            addPackageDataInto(
                    childData,
                    packageNameFilter,
                    packageDataColl);
        }
    }
}
