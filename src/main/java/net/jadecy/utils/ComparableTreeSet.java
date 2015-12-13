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

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

/**
 * A TreeSet that implements Comparable.
 * 
 * Sets are ordered by increasing size, and for sets of same size
 * according to the ordering of their elements.
 * 
 * Note: if comparable type has a natural ordering that is inconsistent
 * with equals, this class also does.
 */
public class ComparableTreeSet<T extends Comparable<T>> extends TreeSet<T> implements Comparable<ComparableTreeSet<T>> {
    
    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------
    
    private static final long serialVersionUID = 1L;
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    public ComparableTreeSet() {
    }
    
    public ComparableTreeSet(Collection<? extends T> c) {
        super(c);
    }
    
    //@Override
    public int compareTo(ComparableTreeSet<T> other) {
        final int aSize = this.size();
        final int bSize = other.size();
        if (aSize < bSize) {
            return -1;
        } else if (aSize > bSize) {
            return 1;
        }
        final Iterator<T> aIt = this.iterator();
        final Iterator<T> bIt = other.iterator();
        for (int i = 0; i < aSize; i++) {
            final T av = aIt.next();
            final T bv = bIt.next();
            final int cmp = av.compareTo(bv);
            if (cmp != 0) {
                return cmp;
            }
        }
        return 0;
    }
}
