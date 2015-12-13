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
package net.jadecy.cmd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The requested type of computation.
 */
enum JdcmCompType {
    DEPSOF(false),
    DEPSTO(true),
    /*
     * Not calling it DEPSGOF, because it would look too much like a graph
     * version of DEPSOF computation, whereas it's doesn't exactly compute
     * the same thing (the form impacts the content).
     */
    GDEPSOF(false),
    GDEPSTO(true),
    SPATH(false),
    PATHSG(false),
    SCCS(false),
    CYCLES(false),
    SOMECYCLES(false);
    
    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------
    
    private static final List<JdcmCompType> VALUES_LIST;
    static {
        final JdcmCompType[] values = JdcmCompType.values();
        final ArrayList<JdcmCompType> list = new ArrayList<JdcmCompType>(values.length);
        for (JdcmCompType value : values) {
            list.add(value);
        }
        VALUES_LIST = Collections.unmodifiableList(list);
    }
    
    private final boolean usesInverseDeps;
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    /**
     * @return An unmodifiable list of all values.
     */
    public static List<JdcmCompType> valuesList() {
        return VALUES_LIST;
    }

    /**
     * @return True if the computation uses inverse dependencies,
     *         false otherwise.
     */
    public boolean usesInverseDeps() {
        return this.usesInverseDeps;
    }
    
    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    /**
     * @param usesInverseDeps True if the computation uses inverse dependencies,
     *        false otherwise.
     */
    private JdcmCompType(boolean usesInverseDeps) {
        this.usesInverseDeps = usesInverseDeps;
    }
}
