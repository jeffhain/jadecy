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
package net.jadecy.parsing.testp;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.RoundingMode;

/**
 * Complex annotation, to check computation of dependencies from annotations,
 * or whether dependencies to annotations parameters and defaults are detected.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE})
public @interface TestAnno2 {
    Class<? extends Number>[] nbrClsArr() default {Byte.class};
    String tooStrong();
    RoundingMode rounding();
    Documented doc();
}
