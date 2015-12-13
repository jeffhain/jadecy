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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import junit.framework.TestCase;

/**
 * Tests that things stay consistent while calling random methods
 * working on a tree of code data with random arguments.
 */
public class CodeDataSturdinessTest extends TestCase {

    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------

    private static final boolean DEBUG = false;

    private static final int NBR_OF_PACKAGE_DATA_OPS = 100 * 1000;
    
    /**
     * Not too many because heavy, and don't need too many
     * to cover all kinds of cases.
     */
    private static final int NBR_OF_TREE_DERIVATION_SPREES = 1000;

    //--------------------------------------------------------------------------
    // PRIVATE CLASSES
    //--------------------------------------------------------------------------

    private static class MyTestData {

        final PackageData defaultP;

        final List<ClassData> currentClassDataList = new ArrayList<ClassData>();
        final List<PackageData> currentPackageDataList = new ArrayList<PackageData>();

        final List<String> possibleClassNameList;
        final List<String> possiblePackageNameList;

        /**
         * @param defaultP Initial tree.
         */
        public MyTestData(PackageData defaultP) {
            this.defaultP = defaultP;

            // Need initial data to compute possible names.
            this.updateTreeData();

            final List<String> possibleClassNameList = new ArrayList<String>();
            final List<String> possiblePackageNameList = new ArrayList<String>();
            for (ClassData classData : this.currentClassDataList) {
                possibleClassNameList.add(classData.name());
            }
            for (PackageData packageData : this.currentPackageDataList) {
                possiblePackageNameList.add(packageData.name());
            }

            this.possibleClassNameList = Collections.unmodifiableList(possibleClassNameList); 
            this.possiblePackageNameList = Collections.unmodifiableList(possiblePackageNameList); 
        }

        public final void updateTreeData() {
            computeSubtreeData(
                    this.defaultP,
                    this.currentClassDataList,
                    this.currentPackageDataList);
        }
    }

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    public void test_sturdiness_smallTree() {
        final boolean mustUseSmallTree = true;
        this.test_sturdiness(mustUseSmallTree);
    }

    public void test_sturdiness_normalTree() {
        final boolean mustUseSmallTree = false;
        this.test_sturdiness(mustUseSmallTree);
    }

    /*
     * 
     */

    /**
     * @param mustUseSmallTree If true, more chance of "functional collisions"
     *        (doing the inverse of what was just done, etc.).
     */
    public void test_sturdiness(boolean mustUseSmallTree) {

        final Random random = new Random(123456789L);

        final PackageData defaultP = CodeTestUtils.newNoDepTestTree(mustUseSmallTree);

        /*
         * Classes and packages randomly created/deleted
         * are those of initial graph.
         */

        final MyTestData testData = new MyTestData(defaultP);

        /*
         * 
         */

        int opCount = 0;
        while (opCount < NBR_OF_PACKAGE_DATA_OPS) {

            final double u01 = random.nextDouble();

            if (DEBUG) {
                System.out.println();
            }

            testData.updateTreeData();

            /*
             * PackageData API usage.
             */
            
            boolean didCall = false;
            
            if (u01 < 0.3) { // p = 0.3
                didCall = callAndCheck_getOrCreateClassData_String(random, testData);

            } else if (u01 < 0.35) { // p = 0.05
                didCall = callAndCheck_getOrCreatePackageData_String(random, testData);

            } else if (u01 < 0.45) { // p = 0.1
                didCall = callAndCheck_deleteClassData_ClassData(random, testData);

            } else if (u01 < 0.5) { // p = 0.05
                didCall = callAndCheck_deletePackageData_PackageData(random, testData);

            } else if (u01 < 0.7) { // p = 0.2
                didCall = callAndCheck_ensureDependency_2ClassData_boolean(random, testData);

            } else if (u01 < 0.9) { // p = 0.2
                didCall = callAndCheck_deleteDependency_2ClassData(random, testData);

            } else if (u01 < 0.99) { // p = 0.09
                didCall = callAndCheck_setByteSizeForClassOrNested_ClassData_String_long(random, testData);

            } else { // p = 0.01
                didCall = callAndCheck_clear(random, testData);
            }
            
            if (didCall) {
                opCount++;
            }
            
            /*
             * DerivedTreeComputer API usage.
             */
            
            final double spreeProba = (NBR_OF_TREE_DERIVATION_SPREES / (double) NBR_OF_PACKAGE_DATA_OPS);
            if (random.nextDouble() < spreeProba) {
                for (boolean mustReverseDeps : new boolean[]{false,true}) {
                    for (InterfaceNameFilter retainedClassNameFilter : new InterfaceNameFilter[]{
                            NameFilters.any(),
                            NameFilters.containsName("p1"),
                            NameFilters.containsName("b$d")
                    }) {
                        if (DEBUG) {
                            System.out.println("mustReverseDeps = " + mustReverseDeps);
                            System.out.println("retainedClassNameFilter = " + retainedClassNameFilter);
                        }
                        
                        final PackageData derDefaultP = DerivedTreeComputer.computeDerivedTree(
                                defaultP,
                                mustReverseDeps,
                                retainedClassNameFilter);
                        
                        checkConsistent_all(derDefaultP);
                    }
                }
            }
        }
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------

    /*
     * Call-and-check methods.
     * 
     * Only check things not already checked in tree consistency check,
     * such as subtree class counts.
     * 
     * Nasty bad arguments (deleted data, etc.) are tested in other unit tests,
     * not to make this code even more messy, and is safe since related checks
     * are done at the beginning of treatments, before eventual mutations.
     */
    
    private static boolean callAndCheck_getOrCreateClassData_String(
            Random random,
            MyTestData testData) {
        
        final PackageData defaultP = testData.defaultP;
        final long oldNbrOfClasses = defaultP.getSubtreeClassCount();
        final Map<String,Long> oldSubtreeModCountByPackageName = computeSubtreeModCountByPackageName(testData);
        
        final PackageData refP = randomPackageData(
                random,
                testData.currentPackageDataList);
        final String relativeClassName = randomRelativeName(
                random,
                refP.name(),
                testData.possibleClassNameList);
        
        final ClassData oldInstance = refP.getClassData(relativeClassName);
        final boolean didExist = (oldInstance != null);
        final int nbrOfOuterClassesToCreate = computeNbrOfNonExistingOuterClasses(refP, relativeClassName);
        
        /*
         * Call.
         */
        
        beforeCall(
                defaultP,
                refP + ".getOrCreateClassData(" + relativeClassName + ")");

        final ClassData ensured = refP.getOrCreateClassData(relativeClassName);
        
        afterCall(defaultP);
        
        /*
         * Checks.
         */
        
        // Ensured.
        assertNotNull(ensured);
        
        // Name.
        if (refP == defaultP) {
            assertEquals(relativeClassName, ensured.name());
        } else {
            assertEquals(refP.name() + "." + relativeClassName, ensured.name());
        }
        
        final PackageData parent = (PackageData) ensured.parent();
        
        if (didExist) {
            assertSame(oldInstance, ensured);
            
            assertEquals(oldNbrOfClasses, defaultP.getSubtreeClassCount());
        } else {
            assertEquals(0L, ensured.byteSize());
            assertEquals(0, ensured.successors().size());
            assertEquals(0, ensured.predecessors().size());
            
            assertEquals(oldNbrOfClasses + nbrOfOuterClassesToCreate + 1, defaultP.getSubtreeClassCount());
        }

        checkSubtreeModCounts(
                testData,
                oldSubtreeModCountByPackageName,
                new AbstractNameFilter() {
                    //@Override
                    public boolean accept(String name) {
                        return (!didExist) && NameUtils.startsWithName(parent.name(), name);
                    }
                });
        
        return true;
    }

    private static boolean callAndCheck_getOrCreatePackageData_String(
            Random random,
            MyTestData testData) {
        
        final PackageData defaultP = testData.defaultP;
        final long oldNbrOfClasses = defaultP.getSubtreeClassCount();
        final Map<String,Long> oldSubtreeModCountByPackageName = computeSubtreeModCountByPackageName(testData);
        
        final PackageData refP = randomPackageData(
                random,
                testData.currentPackageDataList);
        final String relativePackageName = randomRelativeName(
                random,
                refP.name(),
                testData.possiblePackageNameList);
        
        // If creates, creates an empty package.
        final PackageData oldInstance = refP.getPackageData(relativePackageName);
        final boolean didExist = (oldInstance != null);
        
        /*
         * Call.
         */
        
        beforeCall(
                defaultP,
                refP
                + ".getOrCreatePackageData("
                + NameUtils.toDisplayName(relativePackageName)
                + ")");

        final PackageData ensured = refP.getOrCreatePackageData(relativePackageName);
        
        afterCall(defaultP);

        /*
         * Checks.
         */
        
        // Ensured.
        assertNotNull(ensured);
        
        // Name.
        if (refP == defaultP) {
            assertEquals(relativePackageName, ensured.name());
        } else if (relativePackageName.length() == 0) {
            assertEquals(refP.name(), ensured.name());
        } else {
            assertEquals(refP.name() + "." + relativePackageName, ensured.name());
        }

        final PackageData parent = (PackageData) ensured.parent();
        
        if (didExist) {
            assertSame(oldInstance, ensured);
        } else {
            assertEquals(0L, ensured.byteSize());
            assertEquals(0, ensured.successors().size());
            assertEquals(0, ensured.predecessors().size());
        }

        assertEquals(oldNbrOfClasses, defaultP.getSubtreeClassCount());

        checkSubtreeModCounts(
                testData,
                oldSubtreeModCountByPackageName,
                new AbstractNameFilter() {
                    //@Override
                    public boolean accept(String name) {
                        return (!didExist) && NameUtils.startsWithName(parent.name(), name);
                    }
                });
        
        return true;
    }

    private static boolean callAndCheck_deleteClassData_ClassData(
            Random random,
            MyTestData testData) {
        
        final PackageData defaultP = testData.defaultP;
        final long oldNbrOfClasses = defaultP.getSubtreeClassCount();
        final Map<String,Long> oldSubtreeModCountByPackageName = computeSubtreeModCountByPackageName(testData);
        
        final ClassData classData = randomClassData(
                random,
                testData.currentClassDataList);
        if (classData == null) {
            // Empty.
            return false;
        }
        
        final Set<ClassData> classesToDelete = new TreeSet<ClassData>();
        classesToDelete.add(classData);
        addNestedClasses(classData, classesToDelete);
        
        final Set<PackageData> packagesToModify = new TreeSet<PackageData>();
        packagesToModify.add((PackageData) classData.parent());
        addSuccPredClassesParents(
                classesToDelete,
                packagesToModify);
        
        /*
         * Call.
         */
        
        beforeCall(
                defaultP,
                "deleteClassData(" + classData + ")");

        final boolean didMod = PackageData.deleteClassData(classData);
        
        afterCall(defaultP);
        
        /*
         * Checks.
         */
        
        assertTrue(didMod);
        assertTrue(classData.isDeleted());
        
        assertEquals(0, classData.successors().size());
        assertEquals(0, classData.predecessors().size());
        assertEquals(0L, classData.byteSize());
        assertEquals(0, classData.byteSizeByClassFileNameNoExt().size());
        
        assertEquals(oldNbrOfClasses - classesToDelete.size(), defaultP.getSubtreeClassCount());
        
        checkSubtreeModCounts(
                testData,
                oldSubtreeModCountByPackageName,
                new AbstractNameFilter() {
                    //@Override
                    public boolean accept(String name) {
                        for (PackageData packageData : packagesToModify) {
                            if (NameUtils.startsWithName(packageData.name(), name)) {
                                return true;
                            }
                        }
                        return false;
                    }
                });

        return true;
    }

    private static boolean callAndCheck_deletePackageData_PackageData(
            Random random,
            MyTestData testData) {
        
        final PackageData defaultP = testData.defaultP;
        final long oldNbrOfClasses = defaultP.getSubtreeClassCount();
        final Map<String,Long> oldSubtreeModCountByPackageName = computeSubtreeModCountByPackageName(testData);
        
        final PackageData packageData = randomPackageData(
                random,
                testData.currentPackageDataList);

        final Set<PackageData> expectedPackagesDeleted = new TreeSet<PackageData>();
        expectedPackagesDeleted.add(packageData);
        addSubtreePackages(packageData, expectedPackagesDeleted);
        
        final long expectedNbrOfClassesDeleted = packageData.getSubtreeClassCount();
        
        final Set<PackageData> expectedPackagesDeletedOrModified = new TreeSet<PackageData>();
        expectedPackagesDeletedOrModified.addAll(expectedPackagesDeleted);
        addSuccPredPackages(
                expectedPackagesDeleted,
                expectedPackagesDeletedOrModified);
        
        /*
         * Call.
         */
        
        beforeCall(
                defaultP,
                "deletePackageData(" + packageData + ")");

        boolean didMod = false;
        IllegalArgumentException thrown = null; 
        try {
            didMod = PackageData.deletePackageData(packageData);
        } catch (IllegalArgumentException e) {
            // Was default package (things should still be consistent).
            thrown = e;
        }
        
        afterCall(defaultP);
        
        /*
         * Checks.
         */
        
        assertEquals((packageData == defaultP), (thrown != null));
        
        if (packageData != defaultP) {
            // Always true since we never use already deleted packages.
            assertTrue(didMod);
            
            for (PackageData expectedDeleted : expectedPackagesDeleted) {
                assertTrue(expectedDeleted.isDeleted());
            }
            
            // At least children and succ/pred are cleared.
            assertEquals(0, packageData.successors().size());
            assertEquals(0, packageData.predecessors().size());
            assertEquals(0, packageData.childClassDataByFileNameNoExt().size());
            assertEquals(0, packageData.childPackageDataByDirName().size());
            
            assertEquals(oldNbrOfClasses - expectedNbrOfClassesDeleted, defaultP.getSubtreeClassCount());
            
            checkSubtreeModCounts(
                    testData,
                    oldSubtreeModCountByPackageName,
                    new AbstractNameFilter() {
                        //@Override
                        public boolean accept(String name) {
                            for (PackageData packageData : expectedPackagesDeletedOrModified) {
                                if (NameUtils.startsWithName(packageData.name(), name)) {
                                    return true;
                                }
                            }
                            return false;
                        }
                    });
        }
        
        return true;
    }

    private static boolean callAndCheck_ensureDependency_2ClassData_boolean(
            Random random,
            MyTestData testData) {
        
        final PackageData defaultP = testData.defaultP;
        final long oldNbrOfClasses = defaultP.getSubtreeClassCount();
        final Map<String,Long> oldSubtreeModCountByPackageName = computeSubtreeModCountByPackageName(testData);
        
        final ClassData c1 = randomClassData(
                random,
                testData.currentClassDataList);
        if (c1 == null) {
            // Empty.
            return false;
        }
        
        final ClassData c2 = randomClassData(
                random,
                testData.currentClassDataList);
        
        final boolean asInverseDep = random.nextBoolean();
        
        final PackageData p1 = (PackageData) c1.parent();
        final PackageData p2 = (PackageData) c2.parent();

        // Did exist either as non-inverse or inverse dependency.
        final boolean didExistAsEither = c1.successors().contains(c2);
        final boolean expectedDidCreateDep = (!didExistAsEither) && (c1 != c2);
        
        final ClassData cause = (asInverseDep ? c2 : c1);
        final boolean expectedCausesAddedInP1P2;
        if (p1 == p2) {
            expectedCausesAddedInP1P2 = false;
        } else {
            if (didExistAsEither) {
                // Cause set not null since dependency exists.
                // Just need to check one set, since tree is consistent.
                expectedCausesAddedInP1P2 = !p1.causeSetBySuccessor().get(p2).contains(cause);
            } else {
                expectedCausesAddedInP1P2 = true;
            }
        }
        
        final boolean expectedDidMod = expectedDidCreateDep || expectedCausesAddedInP1P2;

        /*
         * Call.
         */
        
        beforeCall(
                defaultP,
                "ensureDependency(" + c1 + "," + c2 + "," + asInverseDep + ")");

        boolean didMod = false;
        IllegalArgumentException thrown = null; 
        try {
            didMod = PackageData.ensureDependency(c1, c2, asInverseDep);
        } catch (IllegalArgumentException e) {
            // Was dependency to self (things should still be consistent).
            thrown = e;
        }
        
        afterCall(defaultP);
        
        /*
         * Checks.
         */
        
        assertEquals((c1 == c2), (thrown != null));
        
        assertEquals(expectedDidMod, didMod);
        
        if (c1 != c2) {
            assertTrue(c1.successors().contains(c2));
        }
        
        assertEquals(oldNbrOfClasses, defaultP.getSubtreeClassCount());
        
        checkSubtreeModCounts(
                testData,
                oldSubtreeModCountByPackageName,
                new AbstractNameFilter() {
                    //@Override
                    public boolean accept(String name) {
                        return expectedDidMod
                                && (NameUtils.startsWithName(p1.name(), name)
                                        || NameUtils.startsWithName(p2.name(), name));
                    }
                });

        return true;
    }

    private static boolean callAndCheck_deleteDependency_2ClassData(
            Random random,
            MyTestData testData) {
        
        final PackageData defaultP = testData.defaultP;
        final long oldNbrOfClasses = defaultP.getSubtreeClassCount();
        final Map<String,Long> oldSubtreeModCountByPackageName = computeSubtreeModCountByPackageName(testData);
        
        final ClassData c1 = randomClassData(
                random,
                testData.currentClassDataList);
        if (c1 == null) {
            // Empty.
            return false;
        }
        
        final ClassData c2 = randomClassData(
                random,
                testData.currentClassDataList);
        
        final boolean didExist = c1.successors().contains(c2);
        
        /*
         * Call.
         */
        
        beforeCall(
                defaultP,
                "deleteDependency(" + c1 + "," + c2 + ")");

        boolean didMod = false;
        IllegalArgumentException thrown = null; 
        try {
            didMod = PackageData.deleteDependency(c1, c2);
        } catch (IllegalArgumentException e) {
            // Not expected.
            thrown = e;
        }
        
        afterCall(defaultP);
        
        /*
         * Checks.
         */
        
        assertNull(thrown);
        
        assertEquals(didExist, didMod);
        
        assertFalse(c1.successors().contains(c2));
        assertFalse(c2.predecessors().contains(c1));
        
        assertEquals(oldNbrOfClasses, defaultP.getSubtreeClassCount());
        
        final PackageData p1 = (PackageData) c1.parent();
        final PackageData p2 = (PackageData) c2.parent();
        
        checkSubtreeModCounts(
                testData,
                oldSubtreeModCountByPackageName,
                new AbstractNameFilter() {
                    //@Override
                    public boolean accept(String name) {
                        return didExist
                                && (NameUtils.startsWithName(p1.name(), name)
                                        || NameUtils.startsWithName(p2.name(), name));
                    }
                });

        return true;
    }

    private static boolean callAndCheck_setByteSizeForClassOrNested_ClassData_String_long(
            Random random,
            MyTestData testData) {
        
        final PackageData defaultP = testData.defaultP;
        final long oldNbrOfClasses = defaultP.getSubtreeClassCount();
        final Map<String,Long> oldSubtreeModCountByPackageName = computeSubtreeModCountByPackageName(testData);
        
        final ClassData classData = randomClassData(
                random,
                testData.currentClassDataList);
        if (classData == null) {
            // Empty.
            return false;
        }
        
        final ClassData topLevelClassData = classData.topLevelClassData();
        final String classFileNameNoExt = classData.fileNameNoExt();
        final long byteSize = 1 + random.nextInt(9);
        
        Map<String,Long> map = topLevelClassData.byteSizeByClassFileNameNoExt();
        final int oldMapSize = map.size();
        final Long oldByteSize = map.get(classFileNameNoExt);
        
        /*
         * Call.
         */
        
        beforeCall(
                defaultP,
                "setByteSizeForClassOrNested("
                        + topLevelClassData
                        + ","
                        + classFileNameNoExt
                        + ","
                        + byteSize
                        + ")");

        final boolean didMod = PackageData.setByteSizeForClassOrNested(
                topLevelClassData,
                classFileNameNoExt,
                byteSize);
        
        afterCall(defaultP);
        
        /*
         * Checks.
         */
        
        assertEquals((oldByteSize == null), didMod);
        
        // Map instance might have changed.
        map = topLevelClassData.byteSizeByClassFileNameNoExt();
        final Long newByteSize = map.get(classFileNameNoExt);
        assertNotNull(newByteSize);
        
        if (oldByteSize != null) {
            // Not changed.
            assertEquals(oldMapSize, map.size());
            assertEquals(oldByteSize.longValue(), newByteSize.longValue());
        } else {
            assertEquals(oldMapSize + 1, map.size());
            assertEquals(byteSize, newByteSize.longValue());
        }
        
        assertEquals(oldNbrOfClasses, defaultP.getSubtreeClassCount());
        
        checkSubtreeModCounts(
                testData,
                oldSubtreeModCountByPackageName,
                new AbstractNameFilter() {
                    //@Override
                    public boolean accept(String name) {
                        return (oldByteSize == null)
                                && NameUtils.startsWithName(topLevelClassData.parent().name(), name);
                    }
                });

        return true;
    }

    private static boolean callAndCheck_clear(
            Random random,
            MyTestData testData) {
        
        final PackageData defaultP = testData.defaultP;
        final long oldNbrOfClasses = defaultP.getSubtreeClassCount();
        final Map<String,Long> oldSubtreeModCountByPackageName = computeSubtreeModCountByPackageName(testData);
        
        final PackageData packageData = randomPackageData(
                random,
                testData.currentPackageDataList);
        
        final boolean wasEmpty =
                (packageData.childClassDataByFileNameNoExt().size() == 0)
                && (packageData.childPackageDataByDirName().size() == 0);

        final long oldSubtreeModCount = packageData.getSubtreeModCount();
        
        final Set<PackageData> expectedPackagesDeleted = new TreeSet<PackageData>();
        addSubtreePackages(packageData, expectedPackagesDeleted);

        final Set<PackageData> expectedPackagesCleared = new TreeSet<PackageData>();
        expectedPackagesCleared.addAll(expectedPackagesDeleted);
        if (!wasEmpty) {
            expectedPackagesCleared.add(packageData);
        }

        final long expectedNbrOfClassesDeleted = packageData.getSubtreeClassCount();
        
        final Set<PackageData> expectedPackagesDeletedOrModified = new TreeSet<PackageData>();
        expectedPackagesDeletedOrModified.addAll(expectedPackagesCleared);
        addSuccPredPackages(
                expectedPackagesCleared,
                expectedPackagesDeletedOrModified);

        final Set<ClassData> expectedClassesDeleted = new TreeSet<ClassData>();
        for (PackageData pd : expectedPackagesCleared) {
            expectedClassesDeleted.addAll(pd.childClassDataByFileNameNoExt().values());
        }

        /*
         * Call.
         */
        
        beforeCall(
                defaultP,
                packageData + ".clear()");

        packageData.clear();
        
        afterCall(defaultP);
        
        /*
         * Checks.
         */
        
        assertFalse(packageData.isDeleted());
        
        CodeTestUtils.checkedModCount(!wasEmpty, oldSubtreeModCount, packageData);

        for (ClassData cd : expectedClassesDeleted) {
            assertTrue(cd.isDeleted());
            
            assertEquals(0L, cd.byteSize());
            assertEquals(0, cd.successors().size());
            assertEquals(0, cd.predecessors().size());
        }
        
        for (PackageData pd : expectedPackagesDeleted) {
            assertTrue(pd.isDeleted());
            
            assertEquals(0L, pd.byteSize());
            assertEquals(0, pd.childClassDataByFileNameNoExt().size());
            assertEquals(0, pd.childPackageDataByDirName().size());
            assertEquals(0, pd.successors().size());
            assertEquals(0, pd.predecessors().size());
        }

        for (PackageData pd : expectedPackagesCleared) {
            assertEquals(0L, pd.byteSize());
            assertEquals(0, pd.childClassDataByFileNameNoExt().size());
            assertEquals(0, pd.childPackageDataByDirName().size());
            assertEquals(0, pd.successors().size());
            assertEquals(0, pd.predecessors().size());
        }

        assertEquals(oldNbrOfClasses - expectedNbrOfClassesDeleted, defaultP.getSubtreeClassCount());
        
        checkSubtreeModCounts(
                testData,
                oldSubtreeModCountByPackageName,
                new AbstractNameFilter() {
                    //@Override
                    public boolean accept(String name) {
                        for (PackageData packageData : expectedPackagesDeletedOrModified) {
                            if (NameUtils.startsWithName(packageData.name(), name)) {
                                return true;
                            }
                        }
                        return false;
                    }
                });
        
        return true;
    }

    /*
     * 
     */

    private static void computeSubtreeData(
            PackageData defaultP,
            List<ClassData> currentClassDataList,
            List<PackageData> currentPackageDataList) {
        if (defaultP.parent() != null) {
            throw new IllegalArgumentException();
        }

        currentClassDataList.clear();
        currentPackageDataList.clear();

        addSubtreeData(
                defaultP,
                currentClassDataList,
                currentPackageDataList);
    }

    /**
     * This method is recursive.
     * 
     * @param currentPackageDataList Always contains at least default package.
     */
    private static void addSubtreeData(
            PackageData packageData,
            List<ClassData> currentClassDataList,
            List<PackageData> currentPackageDataList) {

        currentPackageDataList.add(packageData);

        for (ClassData childClassData : packageData.childClassDataByFileNameNoExt().values()) {
            currentClassDataList.add(childClassData);
        }

        for (PackageData childPackageData : packageData.childPackageDataByDirName().values()) {
            addSubtreeData(
                    childPackageData,
                    currentClassDataList,
                    currentPackageDataList);
        }
    }

    private static int computeNbrOfNonExistingOuterClasses(
            PackageData refP,
            String relativeClassName) {
        
        int count = 0;
        
        while (true) {
            int index = relativeClassName.lastIndexOf('$');
            if (index < 0) {
                break;
            }
            
            // Outer class relative name.
            relativeClassName = relativeClassName.substring(0, index);
            if (refP.getClassData(relativeClassName) == null) {
                count++;
            }
        }
        
        return count;
    }

    /**
     * This method is recursive.
     */
    private static void addNestedClasses(
            ClassData classData,
            Collection<ClassData> nestedClasses) {
        
        for (ClassData nested : classData.nestedClassByFileNameNoExt().values()) {
            nestedClasses.add(nested);
            addNestedClasses(nested, nestedClasses);
        }
    }

    /**
     * This method is recursive.
     */
    private static void addSubtreePackages(
            PackageData packageData,
            Collection<PackageData> packageDataColl) {
        
        for (PackageData child : packageData.childPackageDataByDirName().values()) {
            packageDataColl.add(child);
            addSubtreePackages(child, packageDataColl);
        }
    }
    
    private static void addSuccPredClassesParents(
            Collection<ClassData> inColl,
            Collection<PackageData> outColl) {
        
        for (ClassData classData : inColl) {
            for (ClassData succ : classData.successors()) {
                outColl.add((PackageData) succ.parent());
            }
            for (ClassData pred : classData.predecessors()) {
                outColl.add((PackageData) pred.parent());
            }
        }
    }
    
    private static void addSuccPredPackages(
            Collection<PackageData> inColl,
            Collection<PackageData> outColl) {
        
        for (PackageData packageData : inColl) {
            for (PackageData succ : packageData.successors()) {
                outColl.add((PackageData) succ);
            }
            for (PackageData pred : packageData.predecessors()) {
                outColl.add((PackageData) pred);
            }
        }
    }
    
    private static Map<String,Long> computeSubtreeModCountByPackageName(MyTestData testData) {
        final Map<String,Long> subtreeModCountByPN = new TreeMap<String,Long>();
        for (PackageData packageData : testData.currentPackageDataList) {
            subtreeModCountByPN.put(packageData.name(), packageData.getSubtreeModCount());
        }
        return subtreeModCountByPN;
    }
    
    /**
     * @param modPackageNameFilter Filter to define packages which
     *        subtree mod count must have been modified.
     */
    private static void checkSubtreeModCounts(
            MyTestData testData,
            Map<String,Long> oldSubtreeModCountByPackageName,
            InterfaceNameFilter modPackageNameFilter) {
        for (Map.Entry<String,Long> entry : oldSubtreeModCountByPackageName.entrySet()) {
            final String packageName = entry.getKey();
            final PackageData packageData = testData.defaultP.getPackageData(packageName);
            if (packageData == null) {
                // Has been deleted.
            } else {
                final long oldCount = entry.getValue();
                if (modPackageNameFilter.accept(packageName)) {
                    if (oldCount >= packageData.getSubtreeModCount()) {
                        throw new AssertionError(packageData + " subtreeModCount didn't increase");
                    }
                } else {
                    assertEquals(oldCount, packageData.getSubtreeModCount());
                }
            }
        }
    }
    
    /*
     * 
     */
    
    private static void beforeCall(PackageData defaultP, String op) {
        if (DEBUG) {
            System.out.println();
            System.out.println("tree before op:");
            defaultP.printSubtree(System.out, true);
            
            System.out.println();
            System.out.println("op:");
            System.out.println(op);
        }
    }
    
    private static void afterCall(PackageData defaultP) {
        if (DEBUG) {
            System.out.println();
            System.out.println("tree after op:");
            defaultP.printSubtree(System.out, true);
        }

        /*
         * Consistency check.
         */
        
        checkConsistent_all(defaultP);
    }

    /*
     * 
     */

    /**
     * @return Null if list is empty.
     */
    private static ClassData randomClassData(
            Random random,
            List<ClassData> currentClassDataList) {
        if (currentClassDataList.size() == 0) {
            return null;
        }
        final int randomIndex = random.nextInt(currentClassDataList.size());
        return currentClassDataList.get(randomIndex);
    }

    private static PackageData randomPackageData(
            Random random,
            List<PackageData> currentPackageDataList) {
        final int randomIndex = random.nextInt(currentPackageDataList.size());
        return currentPackageDataList.get(randomIndex);
    }

    private static String randomRelativeName(
            Random random,
            String parentName,
            List<String> possibleNameList) {
        final ArrayList<String> relativeNameList = new ArrayList<String>();
        for (String name : possibleNameList) {
            if (NameUtils.startsWithName(name, parentName)) {
                final String relativeName;
                if (parentName.length() == 0) {
                    relativeName = name;
                } else if (parentName.length() == name.length()) {
                    relativeName = "";
                } else {
                    relativeName = name.substring(parentName.length() + 1);
                }
                relativeNameList.add(relativeName);
            }
        }
        final int randomIndex = random.nextInt(relativeNameList.size());
        return relativeNameList.get(randomIndex);
    }

    /*
     * 
     */

    private static void checkIsInItsParentIfAny(ClassData classData) {
        // Implicit null check.
        final PackageData parent = (PackageData) classData.parent();
        if (parent != null) {
            final ClassData forCheck = parent.childClassDataByFileNameNoExt().get(classData.fileNameNoExt());
            if (forCheck != classData) {
                throw new AssertionError(forCheck + " != " + classData);
            }
        }
        
        assertFalse(classData.isDeleted());
    }

    private static void checkIsInItsParentIfAny(PackageData packageData) {
        // Implicit null check.
        final PackageData parent = (PackageData) packageData.parent();
        if (parent != null) {
            final PackageData forCheck = parent.childPackageDataByDirName().get(packageData.fileNameNoExt());
            if (forCheck != packageData) {
                throw new AssertionError(forCheck + " != " + packageData);
            }
        }
        
        assertFalse(packageData.isDeleted());
    }

    private static boolean containsClassInPackage(
            Collection<ClassData> classDataColl,
            PackageData packageData) {
        for (ClassData classData : classDataColl) {
            if (classData.parent() == packageData) {
                return true;
            }
        }
        return false;
    }
    
    /*
     * Consistency check methods are recursive.
     * Recursion always done first, to avoid interleaving in logs.
     */

    private static void checkConsistent_all(PackageData packageData) {
        checkConsistent_names(packageData);
        checkConsistent_subtreeClassCount(packageData);
        checkConsistent_byteSize(packageData);
        checkConsistent_parentChildStructure(packageData);
        checkConsistent_dependencies(packageData);
    }

    private static void checkConsistent_names(PackageData packageData) {

        if (DEBUG) {
            System.out.println("checkConsistent_names(" + packageData + ")");
        }

        for (Map.Entry<String,PackageData> entry : packageData.childPackageDataByDirName().entrySet()) {
            final String dirName = entry.getKey();
            final PackageData childPackageData = entry.getValue();

            // Recursion.
            checkConsistent_names(childPackageData);

            // Same instance.
            assertSame(dirName, childPackageData.fileNameNoExt());
        }

        for (Map.Entry<String,ClassData> entry : packageData.childClassDataByFileNameNoExt().entrySet()) {
            final String fileNameNoExt = entry.getKey();
            final ClassData childClassData = entry.getValue();

            // Same instance.
            assertSame(fileNameNoExt, childClassData.fileNameNoExt());
        }
    }

    /**
     * @return Subtree class count.
     */
    private static long checkConsistent_subtreeClassCount(PackageData packageData) {

        if (DEBUG) {
            System.out.println("checkConsistent_subtreeClassCount(" + packageData + ")");
        }

        long expectedSubtreeClassCount = 0;

        for (PackageData childPackageData : packageData.childPackageDataByDirName().values()) {

            // Recursion.
            final long childSubtreeClassCount = checkConsistent_subtreeClassCount(childPackageData);

            expectedSubtreeClassCount += childSubtreeClassCount;
        }

        expectedSubtreeClassCount += packageData.childClassDataByFileNameNoExt().size();

        assertEquals(expectedSubtreeClassCount, packageData.getSubtreeClassCount());

        return expectedSubtreeClassCount;
    }

    private static void checkConsistent_byteSize(PackageData packageData) {

        if (DEBUG) {
            System.out.println("checkConsistent_byteSize(" + packageData + ")");
        }

        for (PackageData childPackageData : packageData.childPackageDataByDirName().values()) {

            // Recursion.
            checkConsistent_byteSize(childPackageData);
        }

        long expectedByteSize = 0;

        for (ClassData childClassData : packageData.childClassDataByFileNameNoExt().values()) {

            final long childByteSize = childClassData.byteSize();
            
            long childByteSizeInMap = 0;
            for (long fnneByteSize : childClassData.byteSizeByClassFileNameNoExt().values()) {
                childByteSizeInMap += fnneByteSize;
            }
            assertEquals(childByteSize, childByteSizeInMap);
            
            expectedByteSize += childByteSize;
        }

        assertEquals(expectedByteSize, packageData.byteSize());
    }

    private static void checkConsistent_parentChildStructure(PackageData packageData) {

        if (DEBUG) {
            System.out.println("checkConsistent_parentChildStructure(" + packageData + ")");
        }

        for (PackageData childPackageData : packageData.childPackageDataByDirName().values()) {

            // Recursion.
            checkConsistent_parentChildStructure(childPackageData);

            // Correct parent.
            assertSame(packageData, childPackageData.parent());
        }

        {
            checkIsInItsParentIfAny(packageData);

            for (PackageData succ : packageData.successors()) {
                checkIsInItsParentIfAny(succ);
                assertTrue(succ.predecessors().contains(packageData));
            }

            for (PackageData pred : packageData.predecessors()) {
                checkIsInItsParentIfAny(pred);
                assertTrue(pred.successors().contains(packageData));
            }
        }

        for (ClassData childClassData : packageData.childClassDataByFileNameNoExt().values()) {

            // Correct parent.
            assertSame(packageData, childClassData.parent());

            checkIsInItsParentIfAny(childClassData);

            for (ClassData succ : childClassData.successors()) {
                checkIsInItsParentIfAny(succ);
            }

            for (ClassData pred : childClassData.predecessors()) {
                checkIsInItsParentIfAny(pred);
            }
        }
    }

    private static void checkConsistent_dependencies(PackageData packageData) {

        if (DEBUG) {
            System.out.println("checkConsistent_dependencies(" + packageData + ")");
        }

        for (PackageData childPackageData : packageData.childPackageDataByDirName().values()) {

            // Recursion.
            checkConsistent_dependencies(childPackageData);
        }

        {
            for (PackageData succ : packageData.successors()) {
                assertTrue(succ.predecessors().contains(packageData));

                // Checking we have a corresponding cause set.
                final SortedSet<ClassData> causeSet = packageData.causeSetBySuccessor().get(succ);
                assertNotNull(causeSet);
                assertTrue(causeSet.size() != 0);
            }

            for (PackageData pred : packageData.predecessors()) {
                assertTrue(pred.successors().contains(packageData));

                // Checking we have a corresponding cause set.
                final SortedSet<ClassData> causeSet = packageData.causeSetByPredecessor().get(pred);
                assertNotNull(causeSet);
                assertTrue(causeSet.size() != 0);
            }

            assertEquals(packageData.successors().size(), packageData.causeSetBySuccessor().size());
            assertEquals(packageData.predecessors().size(), packageData.causeSetByPredecessor().size());

            for (Map.Entry<PackageData,SortedSet<ClassData>> entry : packageData.causeSetBySuccessor().entrySet()) {
                final PackageData succ = entry.getKey();
                final SortedSet<ClassData> causeSet = entry.getValue();

                assertTrue(packageData.successors().contains(succ));

                // Empty sets are removed.
                assertTrue(causeSet.size() != 0);

                for (ClassData cause : causeSet) {
                    final boolean isInverseDep = (cause.parent() == succ);
                    if (isInverseDep) {
                        assertTrue(containsClassInPackage(cause.predecessors(), packageData));
                    } else {
                        assertSame(packageData, cause.parent());
                        assertTrue(containsClassInPackage(cause.successors(), succ));
                    }
                }
            }

            for (Map.Entry<PackageData,SortedSet<ClassData>> entry : packageData.causeSetByPredecessor().entrySet()) {
                final PackageData pred = entry.getKey();
                final SortedSet<ClassData> causeSet = entry.getValue();

                assertTrue(packageData.predecessors().contains(pred));

                // Empty sets are removed.
                assertTrue(causeSet.size() != 0);

                for (ClassData cause : causeSet) {
                    final boolean isInverseDep = (cause.parent() == packageData);
                    if (isInverseDep) {
                        assertTrue(containsClassInPackage(cause.predecessors(), pred));
                    } else {
                        assertSame(pred, cause.parent());
                        assertTrue(containsClassInPackage(cause.successors(), packageData));
                    }
                }
            }
        }

        for (ClassData childClassData : packageData.childClassDataByFileNameNoExt().values()) {

            for (ClassData succ : childClassData.successors()) {
                assertTrue(succ.predecessors().contains(childClassData));
            }

            for (ClassData pred : childClassData.predecessors()) {
                assertTrue(pred.successors().contains(childClassData));
            }
        }
    }
}
