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
package net.jadecy.utils;

import junit.framework.TestCase;

public class UniqStackTest extends TestCase {
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    public void test_UniqStack() {
        final UniqStack<Integer> stack = new UniqStack<Integer>();
        assertEquals(0, stack.size());
    }
    
    public void test_size() {
        final UniqStack<Integer> stack = new UniqStack<Integer>();
        assertEquals(0, stack.size());
        
        stack.push(17);
        assertEquals(1, stack.size());
        
        stack.push(21);
        assertEquals(2, stack.size());
        
        stack.pop(21);
        assertEquals(1, stack.size());
        
        stack.pop(17);
        assertEquals(0, stack.size());
    }
    
    public void test_push_E() {
        final UniqStack<Integer> stack = new UniqStack<Integer>();
        
        try {
            stack.push(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        assertEquals(0, stack.size());
        
        stack.push(17);
        assertEquals(1, stack.size());
        
        stack.push(21);
        assertEquals(2, stack.size());

        // Already in.
        try {
            stack.push(17);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            // ok
        }
        assertEquals(2, stack.size());
    }
    
    public void test_pop_E() {
        final UniqStack<Integer> stack = new UniqStack<Integer>();
        
        // Empty.
        try {
            stack.pop(null);
            assertTrue(false);
        } catch (ArrayIndexOutOfBoundsException e) {
            // ok
        }
        assertEquals(0, stack.size());
        
        stack.push(17);
        assertEquals(1, stack.size());
        
        stack.push(21);
        assertEquals(2, stack.size());

        // Not the element to pop (not null).
        try {
            stack.pop(17);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            // ok
        }
        assertEquals(2, stack.size());

        // Not the element to pop (null).
        try {
            stack.pop(null);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            // ok
        }
        assertEquals(2, stack.size());
        
        stack.pop(21);
        assertEquals(1, stack.size());
        
        stack.pop(17);
        assertEquals(0, stack.size());
    }
    
    public void test_get_int() {
        final UniqStack<Integer> stack = new UniqStack<Integer>();
        
        // Empty.
        try {
            stack.get(0);
            assertTrue(false);
        } catch (ArrayIndexOutOfBoundsException e) {
            // ok
        }
        
        stack.push(17);
        for (int badIndex : new int[]{-1, 1}) {
            try {
                stack.get(badIndex);
                assertTrue(false);
            } catch (ArrayIndexOutOfBoundsException e) {
                // ok
            }
        }
        assertEquals((Integer) 17, stack.get(0));
        
        stack.push(21);
        for (int badIndex : new int[]{-1, 2}) {
            try {
                stack.get(badIndex);
                assertTrue(false);
            } catch (ArrayIndexOutOfBoundsException e) {
                // ok
            }
        }
        assertEquals((Integer) 17, stack.get(0));
        assertEquals((Integer) 21, stack.get(1));
    }
    
    public void test_getLast() {
        final UniqStack<Integer> stack = new UniqStack<Integer>();
        
        // Empty.
        try {
            stack.getLast();
            assertTrue(false);
        } catch (ArrayIndexOutOfBoundsException e) {
            // ok
        }
        
        stack.push(17);
        assertEquals((Integer) 17, stack.getLast());
        
        stack.push(21);
        assertEquals((Integer) 21, stack.getLast());
        
        stack.pop(21);
        assertEquals((Integer) 17, stack.getLast());
    }
    
    public void test_indexOf_E() {
        final UniqStack<Integer> stack = new UniqStack<Integer>();
        
        // Empty.
        assertEquals(-1, stack.indexOf(null));
        assertEquals(-1, stack.indexOf(17));
        assertEquals(-1, stack.indexOf(21));
        
        stack.push(17);
        assertEquals(-1, stack.indexOf(null));
        assertEquals(0, stack.indexOf(17));
        assertEquals(-1, stack.indexOf(21));
        
        stack.push(21);
        assertEquals(-1, stack.indexOf(null));
        assertEquals(0, stack.indexOf(17));
        assertEquals(1, stack.indexOf(21));
        
        stack.pop(21);
        assertEquals(-1, stack.indexOf(null));
        assertEquals(0, stack.indexOf(17));
        assertEquals(-1, stack.indexOf(21));
    }
}
