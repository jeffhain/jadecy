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

import junit.framework.TestCase;

public class CodeTestUtils {

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /**
     * @return A new array of invalid class names, which are also invalid
     *         package names (except for empty name which is a valid package
     *         name).
     */
    public static String[] newBadNames() {
        return new String[]{
                "",
                ".",
                "..",
                ".a",
                "a.",
                ".a.",
                "..a",
                "a..",
                "a..a",
        };
    }
    
    /**
     * @return The new mod count.
     */
    public static long checkedModCount(
            boolean incrementExpected,
            long prevModCount,
            PackageData packageData) {
        final long modCount = packageData.getSubtreeModCount();
        if (incrementExpected) {
            TestCase.assertTrue(modCount > prevModCount);
        } else {
            TestCase.assertEquals(prevModCount, modCount);
        }
        return modCount;
    }
    
    /**
     * @param value An integer value > 0.
     * @return The integer part of the logarithm, in base 2, of the specified value,
     *         i.e. a result in [0,30]
     * @throws IllegalArgumentException if the specified value is <= 0.
     */
    public static int log2(int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("value [" + value + "] must be > 0");
        }
        return 31-Integer.numberOfLeadingZeros(value);
    }

    /**
     * Creates a tree for unit tests, with no dependency.
     * Contains up to two level of packages, and up to two level of nested classes,
     * each package containing two packages "p1" and "p2", and each package
     * the following classes: "a", "b", "b$c", "b$d" and "b$d$e".
     * 
     * @param mustUseSmallTree If true, creates two packages in default
     *        package, else also two packages in each of these packages.
     */
    public static PackageData newNoDepTestTree(boolean mustUseSmallTree) {

        final PackageData defaultP = new PackageData();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < (mustUseSmallTree ? 1 : 3); j++) {
                String name = "";
                if (i != 0) {
                    name += "p" + i;
                }
                if (j != 0) {
                    if (name.length() != 0) {
                        name += ".";
                    }
                    name += "p" + j;
                }
                if (name.length() != 0) {
                    name += ".";
                }
                defaultP.getOrCreateClassData(name + "a");
                defaultP.getOrCreateClassData(name + "b$c");
                defaultP.getOrCreateClassData(name + "b$d$e");
            }
        }
        
        return defaultP;
    }

    /**
     * When depth hits 0, ends up with ensuring a single
     * class, in default package.
     * 
     * In each package, creates two classes using "c" plus
     * current depth plus "_1" and "_2" as simple names.
     * 
     * Each class depends on the last created one, if any.
     * 
     * @param maxDepth Must be >= 0.
     * @return The last created class data.
     */
    public static ClassData createSubtree(
            PackageData parent,
            int maxDepth) {
        return createSubtree(
                parent,
                0,
                maxDepth,
                null);
    }
    
    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------

    private CodeTestUtils() {
    }
    
    /*
     * 
     */

    /**
     * This method is recursive.
     * 
     * When depth hits 0, ends up with ensuring a single
     * class, in default package.
     * 
     * In each package, creates two classes using "c" plus
     * current depth plus "_1" and "_2" as simple names.
     * 
     * Each class depends on the last created one, if any.
     * 
     * @param currentDepth Must be 0 initially.
     * @param maxDepth Must be >= 0.
     * @param lastC Must be null initially.
     * @return The last created class data.
     */
    private static ClassData createSubtree(
            PackageData parent,
            int currentDepth,
            int maxDepth,
            ClassData lastC) {

        final ClassData c1 = parent.getOrCreateClassData("c" + Integer.toString(currentDepth) + "_1");
        if (lastC != null) {
            PackageData.ensureDependency(c1, lastC);
        }
        lastC = c1;

        final ClassData c2 = parent.getOrCreateClassData("c" + Integer.toString(currentDepth) + "_2");
        PackageData.ensureDependency(c2, lastC);
        lastC = c2;

        if (currentDepth < maxDepth) {
            for (int i = 0; i < 2; i++) {
                final PackageData childP = parent.getOrCreatePackageData("p" + Integer.toString(i+1));
                lastC = createSubtree(childP, currentDepth+1, maxDepth, lastC);
            }
        }

        return lastC;
    }
}
