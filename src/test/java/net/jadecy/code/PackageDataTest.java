/*
 * Copyright 2015-2019 Jeff Hain
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

import java.util.List;
import java.util.SortedSet;

import net.jadecy.names.NameUtils;
import net.jadecy.tests.PrintTestUtils;
import net.jadecy.utils.MemPrintStream;
import junit.framework.TestCase;

/**
 * These tests can be light, for example only testing special cases, due to
 * behavior coverage being shared with CodeDataSturdinessTest.
 */
public class PackageDataTest extends TestCase {
    
    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------
    
    private static final boolean MUST_HANDLE_WEIRD_DOLLAR_SIGN_USAGES = true;

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    public void test_PackageData() {
        final PackageData defaultP = new PackageData();
        
        assertEquals(null, defaultP.parent());
        assertSame(defaultP, defaultP.root());
        assertEquals(null, defaultP.fileNameNoExt());
        assertEquals("", defaultP.name());
        assertEquals(NameUtils.DEFAULT_PACKAGE_DISPLAY_NAME, defaultP.displayName());
    }

    /*
     * 
     */
    
    public void test_getSubtreeClassCount() {
        final PackageData defaultP = new PackageData();
        assertEquals(0L, defaultP.getSubtreeClassCount());
    }
    
    public void test_getSubtreeModCount() {
        final PackageData defaultP = new PackageData();
        assertEquals(0L, defaultP.getSubtreeModCount());
    }
    
    /*
     * 
     */
    
    public void test_childClassDataByFileNameNoExt() {
        
        final PackageData defaultP = new PackageData();
        
        try {
            defaultP.childClassDataByFileNameNoExt().clear();
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            // ok
        }
    }
    
    public void test_childPackageDataByDirName() {
        
        final PackageData defaultP = new PackageData();
        
        try {
            defaultP.childPackageDataByDirName().clear();
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            // ok
        }
    }
    
    public void test_causeSetBySuccessor() {
        
        final PackageData defaultP = new PackageData();
        
        try {
            defaultP.causeSetBySuccessor().clear();
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            // ok
        }
        
        final ClassData c1 = defaultP.getOrCreateClassData("c1");
        final PackageData p1 = defaultP.getOrCreatePackageData("p1");
        final ClassData c11 = p1.getOrCreateClassData("c11");
        PackageData.ensureDependency(c1, c11);
        
        final SortedSet<ClassData> causeSet = defaultP.causeSetBySuccessor().get(p1);
        assertTrue(causeSet.contains(c1));
        
        try {
            causeSet.clear();
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            // ok
        }
    }
    
    public void test_successors() {
        
        final PackageData defaultP = new PackageData();
        
        try {
            defaultP.successors().clear();
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            // ok
        }
    }
    
    public void test_causeSetByPredecessor() {
        
        final PackageData defaultP = new PackageData();
        
        try {
            defaultP.causeSetByPredecessor().clear();
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            // ok
        }
        
        final ClassData c1 = defaultP.getOrCreateClassData("c1");
        final PackageData p1 = defaultP.getOrCreatePackageData("p1");
        final ClassData c11 = p1.getOrCreateClassData("c11");
        PackageData.ensureDependency(c1, c11);
        
        final SortedSet<ClassData> causeSet = p1.causeSetByPredecessor().get(defaultP);
        assertTrue(causeSet.contains(c1));
        
        try {
            causeSet.clear();
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            // ok
        }
    }
    
    public void test_predecessors() {
        
        final PackageData defaultP = new PackageData();
        
        try {
            defaultP.predecessors().clear();
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            // ok
        }
    }
    
    /*
     * 
     */

    public void test_clear() {
        final PackageData defaultP = new PackageData();

        final PackageData p1 = defaultP.getOrCreatePackageData("p1");
        p1.getOrCreateClassData("c11");
        p1.getOrCreatePackageData("p11");
        
        p1.clear();
        assertEquals(0, p1.childClassDataByFileNameNoExt().size());
        assertEquals(0, p1.childPackageDataByDirName().size());
        
        defaultP.clear();
        
        // Deleted.
        try {
            p1.clear();
            assertTrue(false);
        } catch (IllegalStateException e) {
            // ok
        }
    }
    
    /*
     * Class or package retrieval in subtree.
     */

    public void test_getClassData_String() {
        final PackageData defaultP = new PackageData();

        try {
            defaultP.getClassData(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        for (String bad : new String[]{
                "",
                "bad..dots"
        }) {
            try {
                defaultP.getClassData(bad);
                assertTrue(false);
            } catch (IllegalArgumentException e) {
                // ok
            }
        }

        final ClassData c1 = defaultP.getOrCreateClassData("c1");
        assertSame(c1, defaultP.getClassData("c1"));

        final PackageData p1 = defaultP.getOrCreatePackageData("p1");
        final ClassData c11 = p1.getOrCreateClassData("c11");
        assertSame(c11, defaultP.getClassData("p1.c11"));
        assertSame(c11, p1.getClassData("c11"));
        
        // Not a class.
        assertEquals(null, defaultP.getClassData("p2"));

        defaultP.getOrCreatePackageData("p2");
        // Still not a class.
        assertEquals(null, defaultP.getClassData("p2"));
    }
    
    public void test_getPackageData_String() {
        final PackageData defaultP = new PackageData();
        
        try {
            defaultP.getPackageData(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            defaultP.getPackageData("bad..dots");
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            // ok
        }

        assertSame(defaultP, defaultP.getPackageData(""));
        
        final PackageData p1 = defaultP.getOrCreatePackageData("p1");
        assertSame(p1, defaultP.getPackageData("p1"));
        assertSame(p1, p1.getPackageData(""));

        final PackageData p11 = p1.getOrCreatePackageData("p11");
        assertSame(p11, defaultP.getPackageData("p1.p11"));
        assertSame(p11, p1.getPackageData("p11"));
        assertSame(p11, p11.getPackageData(""));
        
        // Not a package.
        assertEquals(null, defaultP.getPackageData("c1"));

        defaultP.getOrCreateClassData("c1");
        // Still not a package.
        assertEquals(null, defaultP.getPackageData("c1"));
    }

    /*
     * Class or package retrieval or creation in subtree.
     */

    public void test_getOrCreateClassData_String() {
        
        final PackageData defaultP = new PackageData();

        {
            try {
                defaultP.getOrCreateClassData(null);
                assertTrue(false);
            } catch (NullPointerException e) {
                // ok
            }

            for (String bad : new String[]{
                    "",
                    "bad..dots"
            }) {
                try {
                    defaultP.getOrCreateClassData(bad);
                    assertTrue(false);
                } catch (IllegalArgumentException e) {
                    // ok
                }
            }
            
            final ClassData c1 = defaultP.getOrCreateClassData("c1");
            assertSame(c1, defaultP.getClassData("c1"));
            assertSame(defaultP, c1.parent());
            assertSame(defaultP, c1.root());
            assertEquals("c1", c1.fileNameNoExt());
            assertEquals("c1", c1.name());
            assertSame(c1.name(), c1.displayName());
            assertEquals(0L, c1.byteSize());
            assertEquals(0, c1.successors().size());
            assertEquals(0, c1.predecessors().size());

            final PackageData p1 = defaultP.getOrCreatePackageData("p1");
            final ClassData c111 = p1.getOrCreateClassData("p11.c111");
            assertSame(c111, p1.getClassData("p11.c111"));
        }

        if (MUST_HANDLE_WEIRD_DOLLAR_SIGN_USAGES) {
            for (String className : new String[]{
                    "foo.bar.$",
                    "foo.bar.$$",
                    "foo.bar.$A",
                    "foo.bar.A$",
                    "foo.bar.A$$B",
            }) {
                final ClassData classData = defaultP.getOrCreateClassData(className);

                // Weird name -> considered as top level.
                assertSame(classData, classData.topLevelClassData());
                assertEquals(className, classData.name());
                assertEquals("foo.bar", classData.parent().name());
                assertEquals(null, classData.outerClassData());
                assertEquals(0, classData.nestedClassByFileNameNoExt_internal().size());
                assertEquals(0, classData.nestedClassByFileNameNoExt().size());
            }
            
            /*
             * Dollar sign in package name.
             */
            
            for (String n1 : new String[]{"$","a"}) {
                for (String n2 : new String[]{"$","a"}) {
                    final String className = n1 + "." + n2 + ".$";
                    
                    final ClassData classData = defaultP.getOrCreateClassData(className);

                    // Weird name -> considered as top level.
                    assertSame(classData, classData.topLevelClassData());
                    assertEquals(className, classData.name());
                    assertEquals(n1 + "." + n2, classData.parent().name());
                    assertEquals(null, classData.outerClassData());
                    assertEquals(0, classData.nestedClassByFileNameNoExt_internal().size());
                    assertEquals(0, classData.nestedClassByFileNameNoExt().size());
                }
            }
        }
    }
    
    public void test_getOrCreatePackageData_String() {
        
        final PackageData defaultP = new PackageData();
        
        {
            try {
                defaultP.getOrCreatePackageData(null);
                assertTrue(false);
            } catch (NullPointerException e) {
                // ok
            }

            try {
                defaultP.getOrCreatePackageData("bad..dots");
                assertTrue(false);
            } catch (IllegalArgumentException e) {
                // ok
            }

            assertSame(defaultP, defaultP.getPackageData(""));
            
            final PackageData p1 = defaultP.getOrCreatePackageData("p1");
            assertSame(p1, defaultP.getPackageData("p1"));
            assertSame(defaultP, p1.parent());
            assertSame(defaultP, p1.root());
            assertEquals("p1", p1.fileNameNoExt());
            assertEquals("p1", p1.name());
            assertSame(p1.name(), p1.displayName());
            assertEquals(0L, p1.byteSize());
            assertEquals(0, p1.successors().size());
            assertEquals(0, p1.predecessors().size());
            assertEquals(0, p1.causeSetBySuccessor().size());
            assertEquals(0, p1.causeSetByPredecessor().size());

            final PackageData p11 = p1.getOrCreatePackageData("p11");
            assertSame(p11, p1.getPackageData("p11"));
        }
        
        if (MUST_HANDLE_WEIRD_DOLLAR_SIGN_USAGES) {
            for (String n1 : new String[]{"$","a"}) {
                for (String n2 : new String[]{"$","a"}) {
                    final String p1Name = n1;
                    final String p2Name = n1 + "." + n2;
                    
                    final PackageData p1 = defaultP.getOrCreatePackageData(p1Name);
                    final PackageData p2 = defaultP.getOrCreatePackageData(p2Name);
                    
                    assertSame(p1, defaultP.getPackageData(p1Name));
                    assertSame(p2, defaultP.getPackageData(p2Name));
                    
                    assertSame(p1, p2.parent());
                    assertSame(p2, p1.getPackageData(n2));
                }
            }
        }
    }
    
    public void test_setByteSizeForClassOrNested_ClassData_String_long() {
        
        final PackageData defaultP = new PackageData();
        
        final PackageData p1 = defaultP.getOrCreatePackageData("p1");
        final ClassData c11 = p1.getOrCreateClassData("c11");
        
        try {
            PackageData.setByteSizeForClassOrNested(null, "c11", 1L);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        try {
            PackageData.setByteSizeForClassOrNested(c11, null, 1L);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        for (String badName : new String[]{
                "",
                "c",
                "a",
                "a11",
                "c11a",
                "c1$1",
                "p1.c11"
        }) {
            try {
                PackageData.setByteSizeForClassOrNested(c11, badName, 1L);
                assertTrue(false);
            } catch (IllegalArgumentException e) {
                // ok
            }
        }
        {
            // Outer class file name no ext is not good either.
            final ClassData c11b = p1.getOrCreateClassData("c11$b");
            try {
                PackageData.setByteSizeForClassOrNested(c11b, "c11", 1L);
                assertTrue(false);
            } catch (IllegalArgumentException e) {
                // ok
            }
        }
        
        for (long badSize : new long[]{
                Long.MIN_VALUE,
                -1L,
                0L
        }) {
            try {
                PackageData.setByteSizeForClassOrNested(c11, "c11", badSize);
                assertTrue(false);
            } catch (IllegalArgumentException e) {
                // ok
            }
        }
        
        // Deleted.
        {
            final ClassData cd = defaultP.getOrCreateClassData("cd");
            PackageData.deleteClassData(cd);
            
            try {
                PackageData.setByteSizeForClassOrNested(cd, "cd", 1L);
                assertTrue(false);
            } catch (IllegalArgumentException e) {
                // ok
            }
        }
        
        assertFalse(c11.byteSizeByClassFileNameNoExt().containsKey("c11"));
        assertTrue(PackageData.setByteSizeForClassOrNested(c11, "c11", 1L));
        assertTrue(c11.byteSizeByClassFileNameNoExt().containsKey("c11"));
        assertFalse(PackageData.setByteSizeForClassOrNested(c11, "c11", 100L));
        assertEquals(0L, defaultP.byteSize());
        assertEquals(1L, p1.byteSize());
        assertEquals(1L, c11.byteSize());
        
        assertFalse(c11.byteSizeByClassFileNameNoExt().containsKey("c11$a"));
        assertTrue(PackageData.setByteSizeForClassOrNested(c11, "c11$a", 2L));
        assertTrue(c11.byteSizeByClassFileNameNoExt().containsKey("c11"));
        assertTrue(c11.byteSizeByClassFileNameNoExt().containsKey("c11$a"));
        assertFalse(PackageData.setByteSizeForClassOrNested(c11, "c11$a", 100L));
        assertEquals(0L, defaultP.byteSize());
        assertEquals(3L, p1.byteSize());
        assertEquals(3L, c11.byteSize());
        
        ClassData c11a = p1.getClassData("c11$a");
        // Setting byte size for it did not create it.
        assertEquals(null, c11a);
        c11a = p1.getOrCreateClassData("c11$a");
        // Byte site was set for it in c11, not anywhere else.
        assertFalse(c11a.byteSizeByClassFileNameNoExt().containsKey("c11$a"));
        assertEquals(0L, defaultP.byteSize());
        assertEquals(3L, p1.byteSize());
        assertEquals(3L, c11.byteSize());
        assertEquals(0L, c11a.byteSize());
        // Can set another byte size for it in itself.
        assertTrue(PackageData.setByteSizeForClassOrNested(c11a, "c11$a", 5L));
        assertTrue(c11a.byteSizeByClassFileNameNoExt().containsKey("c11$a"));
        assertFalse(PackageData.setByteSizeForClassOrNested(c11a, "c11$a", 100L));
        assertEquals(0L, defaultP.byteSize());
        assertEquals(8L, p1.byteSize());
        assertEquals(3L, c11.byteSize());
        assertEquals(5L, c11a.byteSize());
        
        final ClassData c12 = p1.getOrCreateClassData("c12");
        assertFalse(c12.byteSizeByClassFileNameNoExt().containsKey("c12"));
        assertTrue(PackageData.setByteSizeForClassOrNested(c12, "c12", 7L));
        assertTrue(c12.byteSizeByClassFileNameNoExt().containsKey("c12"));
        assertFalse(PackageData.setByteSizeForClassOrNested(c12, "c12", 100L));
        assertEquals(0L, defaultP.byteSize());
        assertEquals(15L, p1.byteSize());
        assertEquals(3L, c11.byteSize());
        assertEquals(5L, c11a.byteSize());
        assertEquals(7L, c12.byteSize());
    }

    /*
     * Class or package deletion.
     */
    
    public void test_deleteClassData_ClassData() {
        
        final PackageData defaultP = new PackageData();
        
        try {
            PackageData.deleteClassData(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        final ClassData a = defaultP.getOrCreateClassData("a");
        final ClassData ab = defaultP.getOrCreateClassData("a$b");
        final ClassData abc = defaultP.getOrCreateClassData("a$b$c");
        
        // Some byte sizes.
        PackageData.setByteSizeForClassOrNested(a, "a", 2L);
        PackageData.setByteSizeForClassOrNested(a, "a$b", 3L);
        PackageData.setByteSizeForClassOrNested(a, "a$b$c", 5L);
        PackageData.setByteSizeForClassOrNested(ab, "a$b", 20L);
        PackageData.setByteSizeForClassOrNested(ab, "a$b$c", 30L);
        PackageData.setByteSizeForClassOrNested(abc, "a$b$c", 200L);
        assertEquals(260L, defaultP.byteSize());
        assertEquals(10L, a.byteSize());
        assertEquals(50L, ab.byteSize());
        assertEquals(200L, abc.byteSize());
        
        // All kind of deps.
        PackageData.ensureDependency(a, ab);
        PackageData.ensureDependency(a, abc);
        PackageData.ensureDependency(ab, a);
        PackageData.ensureDependency(ab, abc);
        PackageData.ensureDependency(abc, a);
        PackageData.ensureDependency(abc, ab);
        
        assertTrue(PackageData.deleteClassData(ab));
        // Deleted "a$b" and "a$b$c".
        assertFalse(a.isDeleted());
        assertTrue(ab.isDeleted());
        assertTrue(abc.isDeleted());
        assertTrue(defaultP.childClassDataByFileNameNoExt().containsKey("a"));
        assertFalse(defaultP.childClassDataByFileNameNoExt().containsKey("a$b"));
        assertFalse(defaultP.childClassDataByFileNameNoExt().containsKey("a$b$c"));
        assertEquals(10L, defaultP.byteSize());
        assertEquals(10L, a.byteSize());
        assertEquals(0L, ab.byteSize());
        assertEquals(0L, abc.byteSize());
        
        assertEquals(0, a.successors().size());
        assertEquals(0, a.predecessors().size());
        // "a$b" and "a$b$c" not supposed to be used anymore (except for isDeleted()),
        // but their dependencies were nevertheless deleted.
        assertEquals(0, ab.successors().size());
        assertEquals(0, ab.predecessors().size());
        assertEquals(0, abc.successors().size());
        assertEquals(0, abc.predecessors().size());
        
        // Already done.
        assertFalse(PackageData.deleteClassData(ab));
        
        final ClassData newAb = defaultP.getOrCreateClassData("a$b");
        assertNotNull(newAb);
        assertNotSame(ab, newAb);
    }

    public void test_deletePackageData_PackageData() {
        
        final PackageData defaultP = new PackageData();
        
        try {
            PackageData.deletePackageData(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            PackageData.deletePackageData(defaultP);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            // ok
        }

        final PackageData p1 = defaultP.getOrCreatePackageData("p1");
        final ClassData c1 = defaultP.getOrCreateClassData("c1");
        
        final PackageData p11 = p1.getOrCreatePackageData("p11");
        final ClassData c11 = p1.getOrCreateClassData("c11");
        
        final PackageData p111 = p11.getOrCreatePackageData("p111");
        final ClassData c111 = p11.getOrCreateClassData("c111");
        
        assertEquals(3L, defaultP.getSubtreeClassCount());
        
        // Some byte sizes.
        PackageData.setByteSizeForClassOrNested(c11, "c11", 2L);
        PackageData.setByteSizeForClassOrNested(c111, "c111", 3L);
        assertEquals(0L, defaultP.byteSize());
        assertEquals(2L, p1.byteSize());
        assertEquals(3L, p11.byteSize());
        assertEquals(0L, p111.byteSize());
        
        // All kind of deps.
        PackageData.ensureDependency(c1, c11);
        PackageData.ensureDependency(c1, c111);
        PackageData.ensureDependency(c11, c1);
        PackageData.ensureDependency(c11, c111);
        PackageData.ensureDependency(c111, c1);
        PackageData.ensureDependency(c111, c11);

        assertTrue(PackageData.deletePackageData(p1));
        assertEquals(1L, defaultP.getSubtreeClassCount());
        assertEquals(1, defaultP.childClassDataByFileNameNoExt().size());
        assertEquals(0, defaultP.childPackageDataByDirName().size());
        assertEquals(0, defaultP.successors().size());
        assertEquals(0, defaultP.predecessors().size());
        // p1, p11 and p11 not supposed to be used anymore (except for isDeleted()),
        // but their children and dependencies were nevertheless deleted.
        assertEquals(0L, p1.byteSize());
        assertEquals(0L, p11.byteSize());
        assertEquals(0, p1.childClassDataByFileNameNoExt().size());
        assertEquals(0, p1.childPackageDataByDirName().size());
        assertEquals(0, p11.childClassDataByFileNameNoExt().size());
        assertEquals(0, p11.childPackageDataByDirName().size());
        assertEquals(0, p1.successors().size());
        assertEquals(0, p1.predecessors().size());
        assertEquals(0, p11.successors().size());
        assertEquals(0, p11.predecessors().size());
        
        // Already done.
        assertFalse(PackageData.deletePackageData(p1));
        
        final PackageData newP1 = defaultP.getOrCreatePackageData("p1");
        assertNotNull(newP1);
        assertNotSame(p1, newP1);
    }

    /*
     * Dependency ensuring.
     */
    
    public void test_ensureDependency_2ClassData() {
        
        final PackageData defaultP = new PackageData();
        final ClassData c1 = defaultP.getOrCreateClassData("c1");
        
        try {
            PackageData.ensureDependency(null, c1);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        try {
            PackageData.ensureDependency(c1, null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        // Dependency to self.
        try {
            PackageData.ensureDependency(c1, c1);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            // ok
        }
        
        // Deleted.
        {
            final ClassData cd = defaultP.getOrCreateClassData("cd");
            PackageData.deleteClassData(cd);
            
            try {
                PackageData.ensureDependency(cd, c1);
                assertTrue(false);
            } catch (IllegalArgumentException e) {
                // ok
            }
            try {
                PackageData.ensureDependency(c1, cd);
                assertTrue(false);
            } catch (IllegalArgumentException e) {
                // ok
            }
        }

        // Not same root.
        {
            final PackageData alienP = new PackageData();
            final ClassData alienC = alienP.getOrCreateClassData("z1");
            
            try {
                PackageData.ensureDependency(alienC, c1);
                assertTrue(false);
            } catch (IllegalArgumentException e) {
                // ok
            }
            try {
                PackageData.ensureDependency(c1, alienC);
                assertTrue(false);
            } catch (IllegalArgumentException e) {
                // ok
            }
        }
        
        final PackageData p1 = defaultP.getOrCreatePackageData("p1");
        final PackageData p2 = defaultP.getOrCreatePackageData("p2");
        final ClassData c11 = p1.getOrCreateClassData("c11");
        final ClassData c22 = p2.getOrCreateClassData("c22");
        
        assertTrue(PackageData.ensureDependency(c11, c22));
        assertFalse(PackageData.ensureDependency(c11, c22));
        //
        checkDep(c11, c22, true, false);
        checkDep(c22, c11, false, false);
        //
        checkDepCausesCount(p1, p1, 0, 0);
        checkDepCausesCount(p2, p2, 0, 0);
        checkDepCausesCount(p1, p2, 1, 0);
        checkDepCausesCount(p2, p1, 0, 0);
        
        assertTrue(PackageData.ensureDependency(c22, c11));
        assertFalse(PackageData.ensureDependency(c22, c11));
        //
        checkDep(c11, c22, true, false);
        checkDep(c22, c11, true, false);
        //
        checkDepCausesCount(p1, p1, 0, 0);
        checkDepCausesCount(p2, p2, 0, 0);
        checkDepCausesCount(p1, p2, 1, 0);
        checkDepCausesCount(p2, p1, 1, 0);
    }
    
    public void test_ensureDependency_2ClassData_boolean() {
        
        final PackageData defaultP = new PackageData();
        final ClassData c1 = defaultP.getOrCreateClassData("c1");
        
        try {
            PackageData.ensureDependency(null, c1, false);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        try {
            PackageData.ensureDependency(c1, null, false);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        // Dependency to self.
        try {
            PackageData.ensureDependency(c1, c1, false);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            // ok
        }
        
        // Deleted.
        {
            final ClassData cd = defaultP.getOrCreateClassData("cd");
            PackageData.deleteClassData(cd);
            
            try {
                PackageData.ensureDependency(cd, c1, false);
                assertTrue(false);
            } catch (IllegalArgumentException e) {
                // ok
            }
            try {
                PackageData.ensureDependency(c1, cd, false);
                assertTrue(false);
            } catch (IllegalArgumentException e) {
                // ok
            }
        }

        // Not same root.
        {
            final PackageData alienP = new PackageData();
            final ClassData alienC = alienP.getOrCreateClassData("z1");
            
            try {
                PackageData.ensureDependency(alienC, c1, false);
                assertTrue(false);
            } catch (IllegalArgumentException e) {
                // ok
            }
            try {
                PackageData.ensureDependency(c1, alienC, false);
                assertTrue(false);
            } catch (IllegalArgumentException e) {
                // ok
            }
        }
        
        final PackageData p1 = defaultP.getOrCreatePackageData("p1");
        final PackageData p2 = defaultP.getOrCreatePackageData("p2");
        final ClassData c11 = p1.getOrCreateClassData("c11");
        final ClassData c22 = p2.getOrCreateClassData("c22");
        
        assertTrue(PackageData.ensureDependency(c11, c22, false));
        assertFalse(PackageData.ensureDependency(c11, c22, false));
        //
        checkDep(c11, c22, true, false);
        checkDep(c22, c11, false, false);
        //
        checkDepCausesCount(p1, p1, 0, 0);
        checkDepCausesCount(p2, p2, 0, 0);
        checkDepCausesCount(p1, p2, 1, 0);
        checkDepCausesCount(p2, p1, 0, 0);
        
        assertTrue(PackageData.ensureDependency(c11, c22, true));
        assertFalse(PackageData.ensureDependency(c11, c22, true));
        //
        checkDep(c11, c22, true, true);
        checkDep(c22, c11, false, false);
        //
        checkDepCausesCount(p1, p1, 0, 0);
        checkDepCausesCount(p2, p2, 0, 0);
        checkDepCausesCount(p1, p2, 1, 1);
        checkDepCausesCount(p2, p1, 0, 0);
        
        assertTrue(PackageData.ensureDependency(c22, c11, false));
        assertFalse(PackageData.ensureDependency(c22, c11, false));
        //
        checkDep(c11, c22, true, true);
        checkDep(c22, c11, true, false);
        //
        checkDepCausesCount(p1, p1, 0, 0);
        checkDepCausesCount(p2, p2, 0, 0);
        checkDepCausesCount(p1, p2, 1, 1);
        checkDepCausesCount(p2, p1, 1, 0);
        
        assertTrue(PackageData.ensureDependency(c22, c11, true));
        assertFalse(PackageData.ensureDependency(c22, c11, true));
        //
        checkDep(c11, c22, true, true);
        checkDep(c22, c11, true, true);
        //
        checkDepCausesCount(p1, p1, 0, 0);
        checkDepCausesCount(p2, p2, 0, 0);
        checkDepCausesCount(p1, p2, 1, 1);
        checkDepCausesCount(p2, p1, 1, 1);
    }

    /*
     * Dependency deletion.
     */
    
    public void test_deleteDependency_2ClassData() {
        
        final PackageData defaultP = new PackageData();
        final ClassData c1 = defaultP.getOrCreateClassData("c1");
        
        try {
            PackageData.deleteDependency(null, c1);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        try {
            PackageData.deleteDependency(c1, null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        // Deleted.
        {
            final ClassData cd = defaultP.getOrCreateClassData("cd");
            PackageData.deleteClassData(cd);
            
            try {
                PackageData.deleteDependency(cd, c1);
                assertTrue(false);
            } catch (IllegalArgumentException e) {
                // ok
            }
            try {
                PackageData.deleteDependency(c1, cd);
                assertTrue(false);
            } catch (IllegalArgumentException e) {
                // ok
            }
        }

        // Not same root.
        {
            final PackageData alienP = new PackageData();
            final ClassData alienC = alienP.getOrCreateClassData("z1");
            
            try {
                PackageData.deleteDependency(alienC, c1);
                assertTrue(false);
            } catch (IllegalArgumentException e) {
                // ok
            }
            try {
                PackageData.deleteDependency(c1, alienC);
                assertTrue(false);
            } catch (IllegalArgumentException e) {
                // ok
            }
        }
        
        // Deleting (necessarily non-existing) dependency to self: accepted.
        assertFalse(PackageData.deleteDependency(c1, c1));

        final PackageData p1 = defaultP.getOrCreatePackageData("p1");
        final PackageData p2 = defaultP.getOrCreatePackageData("p2");
        final ClassData c11 = p1.getOrCreateClassData("c11");
        final ClassData c12 = p1.getOrCreateClassData("c12");
        final ClassData c22 = p2.getOrCreateClassData("c22");
        
        // No dependency to delete.
        assertFalse(PackageData.deleteDependency(c11, c12));
        assertFalse(PackageData.deleteDependency(c11, c22));
        
        // Creating dependencies in both directions between
        // c11 and c12 (i.e. within a same package), and between
        // c11 and c22 (i.e. involving two packages), as both
        // non-inverse and inverse dependency.
        for (boolean asInverseDep : new boolean[]{false,true}) {
            PackageData.ensureDependency(c11, c12, asInverseDep);
            PackageData.ensureDependency(c12, c11, asInverseDep);
            PackageData.ensureDependency(c11, c22, asInverseDep);
            PackageData.ensureDependency(c22, c11, asInverseDep);
        }
        //
        checkDep(c11, c12, true, true);
        checkDep(c12, c11, true, true);
        checkDep(c11, c22, true, true);
        checkDep(c22, c11, true, true);
        //
        checkDepCausesCount(p1, p1, 0, 0);
        checkDepCausesCount(p2, p2, 0, 0);
        checkDepCausesCount(p1, p2, 1, 1);
        checkDepCausesCount(p2, p1, 1, 1);
        
        assertTrue(PackageData.deleteDependency(c11, c12));
        assertFalse(PackageData.deleteDependency(c11, c12));
        //
        checkDep(c11, c12, false, false);
        checkDep(c12, c11, true, true);
        checkDep(c11, c22, true, true);
        checkDep(c22, c11, true, true);
        //
        checkDepCausesCount(p1, p1, 0, 0);
        checkDepCausesCount(p2, p2, 0, 0);
        checkDepCausesCount(p1, p2, 1, 1);
        checkDepCausesCount(p2, p1, 1, 1);
        
        assertTrue(PackageData.deleteDependency(c12, c11));
        assertFalse(PackageData.deleteDependency(c12, c11));
        //
        checkDep(c11, c12, false, false);
        checkDep(c12, c11, false, false);
        checkDep(c11, c22, true, true);
        checkDep(c22, c11, true, true);
        //
        checkDepCausesCount(p1, p1, 0, 0);
        checkDepCausesCount(p2, p2, 0, 0);
        checkDepCausesCount(p1, p2, 1, 1);
        checkDepCausesCount(p2, p1, 1, 1);
        
        assertTrue(PackageData.deleteDependency(c11, c22));
        assertFalse(PackageData.deleteDependency(c11, c22));
        //
        checkDep(c11, c12, false, false);
        checkDep(c12, c11, false, false);
        checkDep(c11, c22, false, false);
        checkDep(c22, c11, true, true);
        //
        checkDepCausesCount(p1, p1, 0, 0);
        checkDepCausesCount(p2, p2, 0, 0);
        checkDepCausesCount(p1, p2, 0, 0);
        checkDepCausesCount(p2, p1, 1, 1);
        
        assertTrue(PackageData.deleteDependency(c22, c11));
        assertFalse(PackageData.deleteDependency(c22, c11));
        //
        checkDep(c11, c12, false, false);
        checkDep(c12, c11, false, false);
        checkDep(c11, c22, false, false);
        checkDep(c22, c11, false, false);
        //
        checkDepCausesCount(p1, p1, 0, 0);
        checkDepCausesCount(p2, p2, 0, 0);
        checkDepCausesCount(p1, p2, 0, 0);
        checkDepCausesCount(p2, p1, 0, 0);
    }

    /*
     * Print.
     */

    public void test_printSubtree() {
        // Eye tested.
    }

    public void test_printSubtree_PrintStream_boolean() {
        
        final PackageData defaultP = new PackageData();
        final PackageData p1 = defaultP.getOrCreatePackageData("p1");
        final ClassData c11 = p1.getOrCreateClassData("c11");
        PackageData.setByteSizeForClassOrNested(c11, "c11", 2L);
        PackageData.setByteSizeForClassOrNested(c11, "c11$a", 3L);
        final PackageData p11 = p1.getOrCreatePackageData("p11");
        final ClassData c111 = p11.getOrCreateClassData("c111");
        PackageData.ensureDependency(c11, c111);

        for (boolean verbose : new boolean[]{false,true}) {
            
            final MemPrintStream stream = new MemPrintStream();
            
            p1.printSubtree(stream, verbose);
            
            if (verbose) {
                final String[] expectedLines = new String[]{
                        "p1 (5 bytes)",
                        " causeSetBySuccessor = {p1.p11=[p1.c11]}",
                        " causeSetByPredecessor = {}",
                        " p1.c11 (5 bytes: {c11=2, c11$a=3})",
                        "   -> p1.p11.c111",
                        "p1.p11 (0 bytes)",
                        " causeSetBySuccessor = {}",
                        " causeSetByPredecessor = {p1=[p1.c11]}",
                        " p1.p11.c111 (0 bytes: {})"
                };
                checkEqual(expectedLines, toStringTab(stream.getLines()));
            } else {
                final String[] expectedLines = new String[]{
                        "p1",
                        " p1.c11",
                        "   -> p1.p11.c111",
                        "p1.p11",
                        " p1.p11.c111"
                };
                checkEqual(expectedLines, toStringTab(stream.getLines()));
            }
        }
    }

    /*
     * 
     */

    public void test_getOrCreateClassData_String_ClassData() {
        
        final PackageData defaultP = new PackageData();
        final PackageData derDefaultP = new PackageData();

        final ClassData c1 = defaultP.getOrCreateClassData("c1");
        final ClassData derC1 = derDefaultP.getOrCreateClassData("c1", c1);
        checkSameStrings(c1, derC1);
        
        /*
         * Creating parent packages and outer classes
         * along with the class data: all must properly
         * reuse original strings.
         */
        
        final ClassData abcdef = defaultP.getOrCreateClassData("a.b.c$d$e$f");
        final ClassData abcde = abcdef.outerClassData();
        final ClassData abcd = abcde.outerClassData();
        final ClassData abc = abcd.outerClassData();
        final PackageData ab = (PackageData) abc.parent();
        final PackageData a = (PackageData) ab.parent();
        
        final ClassData derAbcde = derDefaultP.getOrCreateClassData("a.b.c$d$e", abcde);
        final ClassData derAbcd = derAbcde.outerClassData();
        final ClassData derAbc = derAbcd.outerClassData();
        final PackageData derAb = (PackageData) derAbc.parent();
        final PackageData derA = (PackageData) derAb.parent();
        
        checkSameStrings(abcde, derAbcde);
        checkSameStrings(abcd, derAbcd);
        checkSameStrings(abc, derAbc);
        checkSameStrings(ab, derAb);
        checkSameStrings(a, derA);
    }
    
    public void test_getOrCreatePackageData_String_PackageData() {
        
        final PackageData defaultP = new PackageData();
        final PackageData derDefaultP = new PackageData();

        final PackageData p1 = defaultP.getOrCreatePackageData("p1");
        final PackageData derP1 = derDefaultP.getOrCreatePackageData("p1", p1);
        checkSameStrings(p1, derP1);
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    private static void checkSameStrings(AbstractCodeData original, AbstractCodeData derived) {
        assertSame(original.fileNameNoExt(), derived.fileNameNoExt());
        assertSame(original.name(), derived.name());
        assertSame(original.displayName(), derived.displayName());
    }
    
    private static int computeCauseCountIn(
            SortedSet<ClassData> causeSet,
            PackageData packageData) {
        
        int count = 0;
        
        if (causeSet != null) {
            for (ClassData cause : causeSet) {
                if (cause.parent() == packageData) {
                    count++;
                }
            }
        }
        
        return count;
    }
    
    private static void checkNotEmptyIfNotNull(SortedSet<ClassData> causeSet) {
        if (causeSet != null) {
            assertTrue(causeSet.size() != 0);
        }
    }
    
    /*
     * 
     */
    
    private static void checkDepCausesCount(
            PackageData fromP,
            PackageData toP,
            int nonInverseDepCausesCount,
            int inverseDepCausesCount) {
        
        final boolean nonInverseDep = (nonInverseDepCausesCount != 0);
        final boolean inverseDep = (inverseDepCausesCount != 0);
        
        assertEquals((nonInverseDep || inverseDep), fromP.successors().contains(toP));
        assertEquals((nonInverseDep || inverseDep), toP.predecessors().contains(fromP));
        
        final SortedSet<ClassData> succCauseSet = fromP.causeSetBySuccessor().get(toP);
        final SortedSet<ClassData> predCauseSet = toP.causeSetByPredecessor().get(fromP);

        checkNotEmptyIfNotNull(succCauseSet);
        checkNotEmptyIfNotNull(predCauseSet);

        assertEquals(nonInverseDepCausesCount, computeCauseCountIn(succCauseSet, fromP));
        assertEquals(nonInverseDepCausesCount, computeCauseCountIn(predCauseSet, fromP));
        
        assertEquals(inverseDepCausesCount, computeCauseCountIn(succCauseSet, toP));
        assertEquals(inverseDepCausesCount, computeCauseCountIn(predCauseSet, toP));
    }
    
    private static void checkDep(
            ClassData from,
            ClassData to,
            boolean existsAsNonInverseDep,
            boolean existsAsInverseDep) {
        
        assertEquals((existsAsNonInverseDep || existsAsInverseDep), from.successors().contains(to));
        assertEquals((existsAsNonInverseDep || existsAsInverseDep), to.predecessors().contains(from));
        
        final PackageData fromP = (PackageData) from.parent();
        final PackageData toP = (PackageData) to.parent();

        final SortedSet<ClassData> succCauseSet = fromP.causeSetBySuccessor().get(toP);
        final SortedSet<ClassData> predCauseSet = toP.causeSetByPredecessor().get(fromP);

        checkNotEmptyIfNotNull(succCauseSet);
        checkNotEmptyIfNotNull(predCauseSet);

        final boolean differentPackages = (fromP != toP);
        
        assertEquals(differentPackages && existsAsNonInverseDep, (predCauseSet != null) && succCauseSet.contains(from));
        assertEquals(differentPackages && existsAsNonInverseDep, (predCauseSet != null) && predCauseSet.contains(from));

        assertEquals(differentPackages && existsAsInverseDep, (succCauseSet != null) && succCauseSet.contains(to));
        assertEquals(differentPackages && existsAsInverseDep, (predCauseSet != null) && predCauseSet.contains(to));
    }
    
    /*
     * 
     */
    
    private static String[] toStringTab(List<String> lineList) {
        return PrintTestUtils.toStringTab(lineList);
    }
    
    private static void checkEqual(String[] expected, String[] actual) {
        PrintTestUtils.checkEqual(expected, actual);
    }
}
