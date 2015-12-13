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

/**
 * Utilities to deal with arguments.
 */
public class ArgsUtils {

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /**
     * @param ref A reference.
     * @return The specified reference.
     * @throws NullPointerException if the specified reference is null.
     */
    public static <T> T requireNonNull(T ref) {
        if (ref == null) {
            throw new NullPointerException();
        }
        return ref;
    }

    /**
     * @param ref An array.
     * @return The specified array.
     * @throws NullPointerException if the specified array, of any of its
     *         elements, is null.
     */
    public static <T> T[] requireNonNull2(T[] ref) {
        requireNonNull(ref);
        for (T elem : ref) {
            requireNonNull(elem);
        }
        return ref;
    }

    /**
     * @param ref An array of arrays.
     * @return The specified array of arrays.
     * @throws NullPointerException if the specified array, of any of its
     *         arrays, or any of their elements, is null.
     */
    public static <T> T[][] requireNonNull3(T[][] ref) {
        requireNonNull(ref);
        for (T[] elem : ref) {
            requireNonNull2(elem);
        }
        return ref;
    }
    
    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------

    private ArgsUtils() {
    }
}
