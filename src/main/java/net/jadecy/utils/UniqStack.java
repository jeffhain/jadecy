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

import java.util.HashMap;

/**
 * Stack which elements must only be contained once in it,
 * but with O(1) retrieval of their index in the stack.
 */
public class UniqStack<E> {
    
    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------
    
    /**
     * Makes things faster.
     * Each element only appears once in this stack, so we don't need to
     * store a list of indexes.
     */
    private final HashMap<E,Integer> indexByElement = new HashMap<E,Integer>();
    
    private Object[] a = new Object[4];
    
    private int size;
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    public UniqStack() {
    }
    
    public int size() {
        return this.size;
    }
    
    /**
     * @param e Must not be already in this stack.
     * @throws NullPointerException if e is null.
     * @throws IllegalArgumentException if e is already in this stack.
     */
    public void push(E e) {
        ArgsUtils.requireNonNull(e);
        Object[] a = this.a;
        if (a.length == this.size) {
            a = this.grownArray();
        }
        final Object forCheck = this.indexByElement.put(e, this.size);
        if (forCheck != null) {
            // Was already in.
            throw new IllegalArgumentException();
        }
        a[this.size++] = e;
    }
    
    /**
     * @param forCheck The element to be poped,
     *        i.e. the last of this stack.
     * @throws IllegalArgumentException if e is not the element to be popped,
     *         possibly due to e being null.
     * @throws ArrayIndexOutOfBoundsException if this stack is empty.
     */
    public void pop(E forCheck) {
        final Object[] a = this.a;
        final int index = this.size-1;
        Object toPop = a[index];
        if (toPop != forCheck) {
            throw new IllegalArgumentException();
        }
        a[index] = null;
        this.size--;
        this.indexByElement.remove(toPop);
    }

    /**
     * @return The element at the specified index.
     * @throws ArrayIndexOutOfBoundsException if index is out of range.
     */
    public E get(int index) {
        if (index >= this.size) {
            throw new ArrayIndexOutOfBoundsException();
        }
        @SuppressWarnings("unchecked")
        final E e = (E) this.a[index];
        return e;
    }
    
    /**
     * @return The last element.
     * @throws ArrayIndexOutOfBoundsException if this stack is empty.
     */
    public E getLast() {
        @SuppressWarnings("unchecked")
        final E e = (E) this.a[this.size-1];
        return e;
    }
    
    /**
     * Executes in constant time (O(1)).
     * 
     * @return The index of the specified element in this stack, else -1,
     *         possibly due to e being null.
     */
    public int indexOf(E e) {
        final Integer intgr = this.indexByElement.get(e);
        if (intgr == null) {
            return -1;
        }
        return intgr.intValue();
    }
    
    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    private Object[] grownArray() {
        if (this.a.length == Integer.MAX_VALUE) {
            throw new ArithmeticException();
        }
        final int newLength = (int)(1.2 * this.a.length + 1.0);
        final Object[] b = new Object[newLength];
        System.arraycopy(this.a, 0, b, 0, this.size);
        this.a = b;
        return b;
    }
}
