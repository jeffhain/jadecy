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

public class ArgsUtilsTest extends TestCase {
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    public void test_requireNonNull() {

        try {
            ArgsUtils.requireNonNull(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        {
            final Integer ref = 1;
            assertSame(ref, ArgsUtils.requireNonNull(ref));
        }
        {
            final Integer[] ref = new Integer[]{1, null};
            assertSame(ref, ArgsUtils.requireNonNull(ref));
        }
    }

    public void test_requireNonNull2() {

        try {
            ArgsUtils.requireNonNull2(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            ArgsUtils.requireNonNull2(new Integer[]{1, null});
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        {
            final Integer[] ref = new Integer[]{1, 2};
            assertSame(ref, ArgsUtils.requireNonNull2(ref));
        }
        {
            final Integer[][] ref = new Integer[][]{{1}, {null}};
            assertSame(ref, ArgsUtils.requireNonNull2(ref));
        }
    }

    public void test_requireNonNull3() {

        try {
            ArgsUtils.requireNonNull3(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            ArgsUtils.requireNonNull3(new Integer[][]{{1}, {null}});
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        {
            final Integer[][] ref = new Integer[][]{{1}, {2}};
            assertSame(ref, ArgsUtils.requireNonNull3(ref));
        }
        {
            final Integer[][][] ref = new Integer[][][]{{{1},{null}}};
            assertSame(ref, ArgsUtils.requireNonNull3(ref));
        }
    }
}
