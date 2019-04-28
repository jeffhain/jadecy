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

import junit.framework.TestCase;
import net.jadecy.graph.InterfaceVertex;
import net.jadecy.names.NameFilters;

public class CodeDataUtilsTest extends TestCase {

    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------
    
    private static final boolean DEBUG = false;

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    public void test_newClassDataList_PackageData_InterfaceNameFilter() {
        final PackageData defaultP = new PackageData();

        /*
         * Exceptions.
         */
        
        try {
            CodeDataUtils.newClassDataList(
                    null,
                    NameFilters.any());
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            CodeDataUtils.newClassDataList(
                    defaultP,
                    null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        /*
         * Creating classes deps.
         */
        
        final int nbrOfClasses = 5;
        final String baseClassName = "foo.bar.A";
        
        final List<String> classNameList = new ArrayList<String>();
        // Reverse looping for creation, to check ordering.
        for (int i = nbrOfClasses; --i >= 0;) {
            classNameList.add(baseClassName + (i+1));
        }
        for (int i = 0; i < nbrOfClasses; i++) {
            final String from = classNameList.get(i);
            for (int j = 0; j < i; j++) {
                final String to = classNameList.get(j);
                final ClassData fromC = defaultP.getOrCreateClassData(from);
                final ClassData toC = defaultP.getOrCreateClassData(to);
                PackageData.ensureDependency(fromC, toC);
            }
        }
        
        /*
         * 
         */
        
        final List<InterfaceVertex> listAll =
                CodeDataUtils.newClassDataList(
                        defaultP,
                        NameFilters.any());

        final List<InterfaceVertex> listFirst =
                CodeDataUtils.newClassDataList(
                        defaultP,
                        NameFilters.equalsName(baseClassName + 1));

        for (Object _list : new Object[]{
                listAll,
                listFirst
        }) {
            @SuppressWarnings("unchecked")
            final List<InterfaceVertex> list = (List<InterfaceVertex>) _list;
            
            if (DEBUG) {
                for (InterfaceVertex v : list) {
                    System.out.println(v + " : " + v.successors());
                }
            }

            if (list == listAll) {
                assertEquals(nbrOfClasses, list.size());
            } else {
                assertEquals(1, list.size());
            }
            
            for (int i = 0; i < list.size(); i++) {
                final ClassData classData = (ClassData) list.get(i);
                
                // Checking ordering.
                assertEquals(baseClassName + (i+1), classData.name());
                
                // Checking that dependencies are preserved.
                for (int j = 0; j < nbrOfClasses; j++) {
                    final ClassData depClassData = (ClassData) listAll.get(j);
                    assertEquals((i < j), classData.successors().contains(depClassData));
                }
            }
        }
    }
    
    public void test_newPackageDataList_PackageData_InterfaceNameFilter() {
        final PackageData defaultP = new PackageData();

        /*
         * Exceptions.
         */
        
        try {
            CodeDataUtils.newPackageDataList(
                    null,
                    NameFilters.any());
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            CodeDataUtils.newPackageDataList(
                    defaultP,
                    null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        /*
         * Creating classes deps.
         */
        
        final int nbrOfPackages = 5;
        final String basePackageName = "foo.bar";

        // "foo" and default package.
        final int nbrOfBonusPackages = 2;

        final List<String> packageNameList = new ArrayList<String>();
        // Reverse looping for creation, to check ordering.
        for (int i = nbrOfPackages; --i >= 0;) {
            packageNameList.add(basePackageName + (i+1));
        }
        for (int i = 0; i < nbrOfPackages; i++) {
            final String from = packageNameList.get(i);
            PackageData fromP = defaultP.getPackageData(from);
            if (fromP == null) {
                fromP = defaultP.getOrCreatePackageData(from);
            }
            for (int j = 0; j < i; j++) {
                final String to = packageNameList.get(j);
                final PackageData toP = defaultP.getOrCreatePackageData(to);
                final ClassData fromC = fromP.getOrCreateClassData("c");
                final ClassData toC = toP.getOrCreateClassData("c");
                PackageData.ensureDependency(fromC, toC);
            }
        }
        
        /*
         * 
         */
        
        final List<InterfaceVertex> listAll =
                CodeDataUtils.newPackageDataList(
                        defaultP,
                        NameFilters.any());

        final List<InterfaceVertex> listFirst =
                CodeDataUtils.newPackageDataList(
                        defaultP,
                        NameFilters.equalsName(basePackageName + 1));

        @SuppressWarnings("unchecked")
        final List<InterfaceVertex>[] listArr = new List[] {
                listAll,
                listFirst
        };
        for (List<InterfaceVertex> list : listArr) {
            
            if (DEBUG) {
                for (InterfaceVertex v : list) {
                    System.out.println(v + " : " + v.successors());
                }
            }

            final int myNbrOfPackages;
            final int myNbrOfBonusPackages;
            if (list == listAll) {
                myNbrOfPackages = nbrOfPackages;
                myNbrOfBonusPackages = nbrOfBonusPackages;
            } else {
                myNbrOfPackages = 1;
                myNbrOfBonusPackages = 0;
            }
            assertEquals(myNbrOfPackages + myNbrOfBonusPackages, list.size());
            
            for (int i = 0; i < myNbrOfPackages; i++) {
                final PackageData packageData = (PackageData) list.get(myNbrOfBonusPackages + i);
                
                // Checking ordering.
                assertEquals(basePackageName + (i+1), packageData.name());
                
                // Checking that dependencies are preserved.
                for (int j = 0; j < nbrOfPackages; j++) {
                    final PackageData depPackageData = (PackageData) listAll.get(nbrOfBonusPackages + j);
                    assertEquals((i < j), packageData.successors().contains(depPackageData));
                }
            }
        }
    }
    
    public void test_haveSameTopLevelClass_CollectionOfClassData() {
        final PackageData defaultP = new PackageData();
        
        /*
         * Exceptions.
         */
        
        try {
            CodeDataUtils.haveSameTopLevelClass(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        /*
         * Classes data for test.
         */
        
        final ClassData classDataA = defaultP.getOrCreateClassData("foo.bar.A");
        final ClassData classDataB = defaultP.getOrCreateClassData("foo.bar.B");
        final ClassData classDataAA = defaultP.getOrCreateClassData("foo.bar.A$A");
        final ClassData classDataAB = defaultP.getOrCreateClassData("foo.bar.A$B");
        final ClassData classDataABC = defaultP.getOrCreateClassData("foo.bar.A$B$C");
        
        /*
         * Empty.
         */
        
        {
            final Collection<ClassData> coll = new ArrayList<ClassData>();
            assertTrue(CodeDataUtils.haveSameTopLevelClass(coll));
        }
        
        /*
         * Not same top level.
         */
        
        {
            final Collection<ClassData> coll = new ArrayList<ClassData>();
            coll.add(classDataA);
            coll.add(classDataB);
            assertFalse(CodeDataUtils.haveSameTopLevelClass(coll));
        }
        
        {
            final Collection<ClassData> coll = new ArrayList<ClassData>();
            coll.add(classDataAB);
            coll.add(classDataB);
            assertFalse(CodeDataUtils.haveSameTopLevelClass(coll));
        }
        
        /*
         * Same top level.
         */
        
        {
            final Collection<ClassData> coll = new ArrayList<ClassData>();
            coll.add(classDataA);
            assertTrue(CodeDataUtils.haveSameTopLevelClass(coll));
        }
        
        {
            final Collection<ClassData> coll = new ArrayList<ClassData>();
            coll.add(classDataAB);
            assertTrue(CodeDataUtils.haveSameTopLevelClass(coll));
        }
        
        {
            final Collection<ClassData> coll = new ArrayList<ClassData>();
            coll.add(classDataA);
            coll.add(classDataA);
            assertTrue(CodeDataUtils.haveSameTopLevelClass(coll));
        }
        
        {
            final Collection<ClassData> coll = new ArrayList<ClassData>();
            coll.add(classDataA);
            coll.add(classDataABC);
            coll.add(classDataAA);
            coll.add(classDataAB);
            assertTrue(CodeDataUtils.haveSameTopLevelClass(coll));
        }
    }
}
