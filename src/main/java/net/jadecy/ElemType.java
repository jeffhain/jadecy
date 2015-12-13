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
package net.jadecy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Type of data for which dependencies or such are computed.
 */
public enum ElemType {
    CLASS("class","classes"),
    PACKAGE("package","packages");
    
    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------
    
    private static final List<ElemType> VALUES_LIST;
    static {
        final ElemType[] values = ElemType.values();
        final ArrayList<ElemType> list = new ArrayList<ElemType>(values.length);
        for (ElemType value : values) {
            list.add(value);
        }
        VALUES_LIST = Collections.unmodifiableList(list);
    }

    private final String nameSingularLC;
    private final String namePluralLC;
    
    private final String nameSingularUC;
    private final String namePluralUC;
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    /**
     * @return An unmodifiable list of all values.
     */
    public static List<ElemType> valuesList() {
        return VALUES_LIST;
    }

    public String toStringSingularLC() {
        return this.nameSingularLC;
    }
    
    public String toStringPluralLC() {
        return this.namePluralLC;
    }
    
    public String toStringSingularUC() {
        return this.nameSingularUC;
    }
    
    public String toStringPluralUC() {
        return this.namePluralUC;
    }
    
    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    private ElemType(
            String nameSingularLC,
            String namePluralLC) {
        this.nameSingularLC = nameSingularLC;
        this.namePluralLC = namePluralLC;
        
        this.nameSingularUC = nameSingularLC.toUpperCase();
        this.namePluralUC = namePluralLC.toUpperCase();
    }
}
