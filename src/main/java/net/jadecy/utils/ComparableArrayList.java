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

import java.util.ArrayList;
import java.util.Collection;

/**
 * An ArrayList that implements Comparable.
 * 
 * Lists are ordered by increasing size, and for lists of same size
 * according to the ordering of their elements.
 * 
 * Note: if comparable type has a natural ordering that is inconsistent
 * with equals, this class also does.
 */
public class ComparableArrayList<T extends Comparable<T>> extends ArrayList<T> implements Comparable<ComparableArrayList<T>> {
    
    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------
    
    private static final long serialVersionUID = 1L;
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    public ComparableArrayList() {
    }
    
    public ComparableArrayList(Collection<? extends T> c) {
        super(c);
    }
    
    //@Override
    public int compareTo(ComparableArrayList<T> other) {
        final int aSize = this.size();
        final int bSize = other.size();
        // No overflow since both >= 0.
        int cmp = aSize - bSize;
        if (cmp != 0) {
            return cmp;
        }
        final int size = aSize;
        for (int i = 0; i < size; i++) {
            final T av = this.get(i);
            final T bv = other.get(i);
            cmp = av.compareTo(bv);
            if (cmp != 0) {
                return cmp;
            }
        }
        return 0;
    }
}
