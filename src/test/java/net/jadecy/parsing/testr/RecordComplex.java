/*
 * Copyright 2023 Jeff Hain
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
package net.jadecy.parsing.testr;

import net.jadecy.parsing.testp.TestAnno1;
import net.jadecy.parsing.testp.TestAnno2;
import net.jadecy.parsing.testp.TestAnno3;
import net.jadecy.parsing.testp.TestAnno4;
import net.jadecy.parsing.testp.TestAnno5;

@TestAnno1
public record RecordComplex<T>(
    @TestAnno2 Byte a,
    Short[] b,
    T c) {
    
    @TestAnno3
    public static final Integer D = 2; 
    
    @TestAnno4
    public void add(Long e) {
        @TestAnno5
        final Double f = e + 0.5;
        if (f.doubleValue() < 0) {
            throw new IllegalArgumentException();
        }
    }
}
