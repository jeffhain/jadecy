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

import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A TreeMap that implements Comparable.
 * 
 * Maps are ordered by increasing size, and for maps of same size
 * according to the ordering of their keys.
 * 
 * Note: if comparable type has a natural ordering that is inconsistent
 * with equals, this class also does.
 */
public class ComparableTreeMap<K extends Comparable<K>,V> extends TreeMap<K,V> implements Comparable<ComparableTreeMap<K,V>> {
    
    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------
    
    private static final long serialVersionUID = 1L;
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    public ComparableTreeMap() {
    }
    
    public ComparableTreeMap(Map<? extends K, ? extends V> m) {
        super(m);
    }
    
    public ComparableTreeMap(SortedMap<K, ? extends V> m) {
        super(m);
    }
    
    //@Override
    public int compareTo(ComparableTreeMap<K,V> other) {
        final int aSize = this.size();
        final int bSize = other.size();
        if (aSize < bSize) {
            return -1;
        } else if (aSize > bSize) {
            return 1;
        }
        final Iterator<K> aIt = this.keySet().iterator();
        final Iterator<K> bIt = other.keySet().iterator();
        for (int i = 0; i < aSize; i++) {
            final K av = aIt.next();
            final K bv = bIt.next();
            final int cmp = av.compareTo(bv);
            if (cmp != 0) {
                return cmp;
            }
        }
        return 0;
    }
}
