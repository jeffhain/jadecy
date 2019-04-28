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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.jadecy.names.NameUtils;
import junit.framework.TestCase;

public class AbstractCodeDataTest extends TestCase {

    //--------------------------------------------------------------------------
    // PRIVATE CLASSES
    //--------------------------------------------------------------------------
    
    private static class MyData extends AbstractCodeData {
        final Set<AbstractCodeData> childSet = new HashSet<AbstractCodeData>();
        MyData() {
            super(null, null, null);
        }
        MyData(
                AbstractCodeData parent,
                String fileNameNoExt,
                AbstractCodeData original) {
            super(parent, fileNameNoExt, original);
        }
        @Override
        public Set<AbstractCodeData> successors() {
            throw new UnsupportedOperationException();
        }
        @Override
        public Set<AbstractCodeData> predecessors() {
            return null;
        }
        @Override
        boolean hasChild(AbstractCodeData data) {
            return this.childSet.contains(data);
        }
    }
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    public void test_toString() {
        final MyData d0 = new MyData();
        // Same instance.
        assertSame(d0.displayName(), d0.toString());

        final MyData d1 = new MyData(d0, "d1", null);
        // Same instance.
        assertSame(d1.displayName(), d1.toString());
        
        final MyData d2 = new MyData(d1, "d2", null);
        // Same instance.
        assertSame(d2.displayName(), d2.toString());
    }
    
    public void test_name() {
        final MyData d0 = new MyData();
        assertEquals("", d0.name());

        final MyData d1 = new MyData(d0, "d1", null);
        assertEquals("d1", d1.name());
        
        final MyData d2 = new MyData(d1, "d2", null);
        assertEquals("d1.d2", d2.name());
        
        // Same instance for each call.
        assertEquals(d2.name(), d2.name());
    }
    
    public void test_displayName() {
        final MyData d0 = new MyData();
        // Same instance.
        assertSame(NameUtils.DEFAULT_PACKAGE_DISPLAY_NAME, d0.displayName());

        final MyData d1 = new MyData(d0, "d1", null);
        assertEquals("d1", d1.displayName());
        
        final MyData d2 = new MyData(d1, "d2", null);
        assertEquals("d1.d2", d2.displayName());
        
        // Same instance for each call.
        assertEquals(d2.displayName(), d2.displayName());
    }
    
    public void test_compareTo_InterfaceVertex() {
        final List<MyData> dataList = new ArrayList<MyData>();
        
        final MyData d0 = new MyData();
        dataList.add(d0);
        
        final MyData d1 = new MyData(d0, "d1", null);
        dataList.add(d1);
        
        final MyData d11 = new MyData(d1, "d11", null);
        dataList.add(d11);
        
        final MyData d2 = new MyData(d0, "d2", null);
        dataList.add(d2);
        
        final MyData d22 = new MyData(d2, "d22", null);
        dataList.add(d22);
        
        for (MyData a : dataList) {
            for (MyData b : dataList) {
                assertEquals(a.name().compareTo(b.name()), a.compareTo(b));
            }
        }
    }

    public void test_isDeleted() {
        final MyData d0 = new MyData();
        assertFalse(d0.isDeleted());
        
        final MyData d1 = new MyData(d0, "d1", null);
        assertTrue(d1.isDeleted());
        d0.childSet.add(d1);
        assertFalse(d1.isDeleted());
        
        final MyData d11 = new MyData(d1, "d11", null);
        assertTrue(d11.isDeleted());
        d1.childSet.add(d11);
        assertFalse(d11.isDeleted());
        
        // Checking that is recursive.
        d0.childSet.remove(d1);
        assertTrue(d11.isDeleted());
    }

    public void test_parent() {
        final MyData d0 = new MyData();
        assertEquals(null, d0.parent());

        final MyData d1 = new MyData(d0, "d1", null);
        assertSame(d0, d1.parent());
        
        final MyData d2 = new MyData(d1, "d2", null);
        assertSame(d1, d2.parent());
    }

    public void test_root() {
        final MyData d0 = new MyData();
        assertSame(d0, d0.root());

        final MyData d1 = new MyData(d0, "d1", null);
        assertSame(d0, d1.root());
        
        final MyData d2 = new MyData(d1, "d2", null);
        assertSame(d0, d2.root());
    }
    
    public void test_fileNameNoExt() {
        final MyData d0 = new MyData();
        assertEquals(null, d0.fileNameNoExt());

        final MyData d1 = new MyData(d0, "d1", null);
        assertEquals("d1", d1.fileNameNoExt());
        
        final MyData d2 = new MyData(d1, "d2", null);
        assertEquals("d2", d2.fileNameNoExt());
   }
    
    public void test_byteSize_and_addByteSize_long() {
        final MyData d0 = new MyData();
        assertEquals(0L, d0.byteSize());
        
        d0.addByteSize(123);
        assertEquals(123L, d0.byteSize());
        
        d0.addByteSize(456);
        assertEquals(579L, d0.byteSize());
        
        d0.addByteSize(-500);
        assertEquals(79L, d0.byteSize());
        
        try {
            d0.addByteSize(-80L);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            // ok
        }
        assertEquals(79L, d0.byteSize());
        
        try {
            d0.addByteSize(-100);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            // ok
        }
        assertEquals(79L, d0.byteSize());
        
        d0.addByteSize(-79L);
        assertEquals(0L, d0.byteSize());
    }
    
    /*
     * 
     */
    
    public void test_AbstractCodeData_AbstractCodeData_String_AbstractCodeData() {
        
        final MyData d0 = new MyData();
        
        final MyData d1 = new MyData(d0, "d1", null);
        
        try {
            new MyData(null, "good", null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        try {
            new MyData(d0, null, null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        try {
            new MyData(d0, "bad..bad", null);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            // ok
        }

        // Original for default package is not default package.
        try {
            final MyData original = new MyData(d0, "foo", null);
            new MyData(null, null, original);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            // ok
        }

        // Original for non-default package is default package.
        try {
            final MyData original = new MyData();
            new MyData(d0, "foo", original);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            // ok
        }

        // Original parent name has wrong length.
        try {
            final MyData badNameLengthOriginalParent = new MyData(d0, "badNameLengthOriginalParent", null);
            final MyData original = new MyData(badNameLengthOriginalParent, "foo", null);
            new MyData(d1, "foo", original);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            // ok
        }

        // Original dir name has wrong length.
        try {
            final MyData original = new MyData(d0, "no_same_length_as_foo", null);
            new MyData(d0, "foo", original);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            // ok
        }

        /*
         * 
         */
        
        {
            final MyData original = new MyData();
            final MyData defaultP = new MyData(null, null, original);
            assertEquals(null, defaultP.parent());
            assertEquals(null, defaultP.fileNameNoExt());
        }
        
        // Original strings reuse.
        {
            final MyData d1Bis = new MyData(d0, "d1", d1);
            assertSame(d1.name(), d1Bis.name());
            assertSame(d1.fileNameNoExt(), d1Bis.fileNameNoExt());
        }
    }
}
