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
import java.util.Set;

import junit.framework.TestCase;

public class ClassDataTest extends TestCase {

    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------

    private static final boolean DEBUG = false;
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    public void test_successors() {
        final PackageData defaultP = new PackageData();
        final ClassData classData = defaultP.getOrCreateClassData("foo");
        
        // More tested in successors_internal() test.
        assertEquals(0, classData.successors().size());
        
        // Unmodifiable.
        try {
            classData.successors().add(defaultP.getOrCreateClassData("succ"));
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            // ok
        }
    }

    public void test_predecessors() {
        final PackageData defaultP = new PackageData();
        final ClassData classData = defaultP.getOrCreateClassData("foo");
        
        // More tested in predecessors_internal() test.
        assertEquals(0, classData.predecessors().size());
        
        // Unmodifiable.
        try {
            classData.predecessors().add(defaultP.getOrCreateClassData("pred"));
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            // ok
        }
    }

    public void test_topLevelClassData() {
        final PackageData defaultP = new PackageData();
        
        for (String className : new String[]{
                "foo.bar.A",
                "foo.bar.A$B",
                "foo.bar.A$B$C",
        }) {
            final ClassData classData = defaultP.getOrCreateClassData(className);
            
            if (DEBUG) {
                System.out.println("classData = " + classData);
            }
            
            final ClassData topLevelClassData = classData.topLevelClassData();
            
            if (DEBUG) {
                System.out.println("topLevelClassData = " + topLevelClassData);
            }
            
            assertEquals("foo.bar.A", topLevelClassData.name());
        }
    }

    public void test_outerClassData() {
        final PackageData defaultP = new PackageData();
        
        final ClassData ca = defaultP.getOrCreateClassData("a");
        final ClassData cb = defaultP.getOrCreateClassData("a$b");
        final ClassData cc = defaultP.getOrCreateClassData("a$b$c");
        
        assertNull(ca.outerClassData());
        assertSame(ca, cb.outerClassData());
        assertSame(cb, cc.outerClassData());
    }
    
    public void test_nestedClassByFileNameNoExt() {
        final PackageData defaultP = new PackageData();
        final ClassData classData = defaultP.getOrCreateClassData("foo");
        
        // More tested in nestedClassByFileNameNoExt_internal() test.
        assertEquals(0, classData.nestedClassByFileNameNoExt().size());
        
        // Unmodifiable.
        try {
            classData.nestedClassByFileNameNoExt().put("foo$a", defaultP.getOrCreateClassData("foo$a"));
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            // ok
        }
    }

    /*
     * 
     */
    
    public void test_ClassData_AbstractCodeData_String_ClassData() {
        final PackageData defaultP = new PackageData();

        final ClassData c1 = new ClassData(defaultP, "c1", null, null);

        /*
         * Exceptions.
         */
        
        try {
            new ClassData(null, "good", null, null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        try {
            new ClassData(defaultP, null, null, null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        try {
            new ClassData(c1, "good", null, null);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            // ok
        }
        
        for (String bad : CodeTestUtils.newBadNames()) {
            try {
                new ClassData(defaultP, bad, null, null);
                assertTrue(false);
            } catch (IllegalArgumentException e) {
                // ok
            }
        }
    }
    
    public void test_successors_internal() {
        final PackageData defaultP = new PackageData();

        final ClassData ca = defaultP.getOrCreateClassData("a");
        final ClassData cb = defaultP.getOrCreateClassData("b");
        final ClassData cc = defaultP.getOrCreateClassData("c");
        
        ca.successors_internal().add(cb);
        ca.successors_internal().add(cc);
        cb.successors_internal().add(cc);
        
        for (boolean internal : new boolean[]{false,true}) {
            
            if (DEBUG) {
                System.out.println("internal = " + internal);
            }
            
            assertEquals(2, successors(ca,internal).size());
            assertTrue(successors(ca,internal).contains(cb));
            assertTrue(successors(ca,internal).contains(cc));
            
            assertEquals(1, successors(cb,internal).size());
            assertTrue(successors(cb,internal).contains(cc));
            
            assertEquals(0, successors(cc,internal).size());
        }
        
        /*
         * 
         */
        
        cb.successors_internal().add(cb);

        for (boolean internal : new boolean[]{false,true}) {
            
            if (DEBUG) {
                System.out.println("internal = " + internal);
            }
            
            assertEquals(2, successors(ca,internal).size());
            assertTrue(successors(ca,internal).contains(cb));
            assertTrue(successors(ca,internal).contains(cc));
            
            assertEquals(2, successors(cb,internal).size());
            assertTrue(successors(cb,internal).contains(cc));
            assertTrue(successors(cb,internal).contains(cb));
            
            assertEquals(0, successors(cc,internal).size());
        }
    }
    
    public void test_predecessors_internal() {
        final PackageData defaultP = new PackageData();

        final ClassData ca = defaultP.getOrCreateClassData("a");
        final ClassData cb = defaultP.getOrCreateClassData("b");
        final ClassData cc = defaultP.getOrCreateClassData("c");
        
        ca.predecessors_internal().add(cb);
        ca.predecessors_internal().add(cc);
        cb.predecessors_internal().add(cc);
        
        for (boolean internal : new boolean[]{false,true}) {
            
            if (DEBUG) {
                System.out.println("internal = " + internal);
            }
            
            assertEquals(2, predecessors(ca,internal).size());
            assertTrue(predecessors(ca,internal).contains(cb));
            assertTrue(predecessors(ca,internal).contains(cc));
            
            assertEquals(1, predecessors(cb,internal).size());
            assertTrue(predecessors(cb,internal).contains(cc));
            
            assertEquals(0, predecessors(cc,internal).size());
        }
        
        /*
         * 
         */
        
        cb.predecessors_internal().add(cb);

        for (boolean internal : new boolean[]{false,true}) {
            
            if (DEBUG) {
                System.out.println("internal = " + internal);
            }
            
            assertEquals(2, predecessors(ca,internal).size());
            assertTrue(predecessors(ca,internal).contains(cb));
            assertTrue(predecessors(ca,internal).contains(cc));
            
            assertEquals(2, predecessors(cb,internal).size());
            assertTrue(predecessors(cb,internal).contains(cc));
            assertTrue(predecessors(cb,internal).contains(cb));
            
            assertEquals(0, predecessors(cc,internal).size());
        }
    }
    
    public void test_nestedClassByFileNameNoExt_internal() {
        final PackageData defaultP = new PackageData();

        final ClassData ca = defaultP.getOrCreateClassData("a");
        final ClassData cb = defaultP.getOrCreateClassData("a$b");
        final ClassData cc = defaultP.getOrCreateClassData("a$b$c");
        
        for (boolean internal : new boolean[]{false,true}) {
            
            if (DEBUG) {
                System.out.println("internal = " + internal);
            }
            
            assertEquals(1, nestedClassByFileNameNoExt(ca,internal).size());
            assertSame(cb, nestedClassByFileNameNoExt(ca,internal).get("a$b"));
            
            assertEquals(1, nestedClassByFileNameNoExt(cb,internal).size());
            assertSame(cc, nestedClassByFileNameNoExt(cb,internal).get("a$b$c"));
            
            assertEquals(0, nestedClassByFileNameNoExt(cc,internal).size());
        }
        
        /*
         * 
         */
        
        cb.nestedClassByFileNameNoExt_internal().put("selfie", cb);

        for (boolean internal : new boolean[]{false,true}) {
            
            if (DEBUG) {
                System.out.println("internal = " + internal);
            }
            
            assertEquals(1, nestedClassByFileNameNoExt(ca,internal).size());
            assertSame(cb, nestedClassByFileNameNoExt(ca,internal).get("a$b"));
            
            assertEquals(2, nestedClassByFileNameNoExt(cb,internal).size());
            assertSame(cc, nestedClassByFileNameNoExt(cb,internal).get("a$b$c"));
            assertSame(cb, nestedClassByFileNameNoExt(cb,internal).get("selfie"));
            
            assertEquals(0, nestedClassByFileNameNoExt(cc,internal).size());
        }
    }
    
    /**
     * Tests byteSizeByClassFileNameNoExt(),
     * setByteSizeForClassFileNameNoExt(...)
     * and clearByteSizeByClassFileNameNoExt().
     */
    public void test_byteSizeForClassFileNameNoExt_stuffs() {
        final PackageData defaultP = new PackageData();
        
        final ClassData classData = defaultP.getOrCreateClassData("foo");
        
        try {
            classData.byteSizeByClassFileNameNoExt().clear();
            assertTrue(false);
        } catch (UnsupportedOperationException e) {
            // ok
        }

        assertFalse(classData.byteSizeByClassFileNameNoExt().containsKey("bar"));
        assertFalse(classData.byteSizeByClassFileNameNoExt().containsKey("boo"));
        
        for (int k = 0; k < 2; k++) {
            classData.setByteSizeForClassFileNameNoExt("bar", 13L);
            assertEquals((Long) 13L, classData.byteSizeByClassFileNameNoExt().get("bar"));
            assertFalse(classData.byteSizeByClassFileNameNoExt().containsKey("boo"));
            // Not updated.
            assertEquals(0L, classData.byteSize());
        }
        
        for (int k = 0; k < 2; k++) {
            classData.setByteSizeForClassFileNameNoExt("boo", 17L);
            assertEquals((Long) 13L, classData.byteSizeByClassFileNameNoExt().get("bar"));
            assertEquals((Long) 17L, classData.byteSizeByClassFileNameNoExt().get("boo"));
            // Not updated.
            assertEquals(0L, classData.byteSize());
        }
        
        {
            classData.clearByteSizeByClassFileNameNoExt();
            assertEquals(0, classData.byteSizeByClassFileNameNoExt().size());
            // Not updated.
            assertEquals(0L, classData.byteSize());
        }
    }
    
    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    private static Set<ClassData> successors(
            ClassData classData,
            boolean internal) {
        if (internal) {
            return classData.successors_internal();
        } else {
            return classData.successors();
        }
    }
    
    private static Set<ClassData> predecessors(
            ClassData classData,
            boolean internal) {
        if (internal) {
            return classData.predecessors_internal();
        } else {
            return classData.predecessors();
        }
    }
    
    private static Map<String,ClassData> nestedClassByFileNameNoExt(
            ClassData classData,
            boolean internal) {
        if (internal) {
            return classData.nestedClassByFileNameNoExt_internal();
        } else {
            return classData.nestedClassByFileNameNoExt();
        }
    }
}
