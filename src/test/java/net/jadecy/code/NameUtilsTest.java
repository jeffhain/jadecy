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
package net.jadecy.code;

import java.util.Arrays;

import junit.framework.TestCase;

public class NameUtilsTest extends TestCase {

    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------

    private static final boolean DEBUG = false;
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    public void test_doted_String() {
        
        try {
            NameUtils.doted(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        /*
         * 
         */
        
        assertEquals("", NameUtils.doted(""));
        assertEquals("a", NameUtils.doted("a"));
        assertEquals("a.", NameUtils.doted("a/"));
        assertEquals(".a", NameUtils.doted("/a"));
        assertEquals(".a.", NameUtils.doted("/a/"));
        assertEquals("..a$a..b$b..", NameUtils.doted("./a$a//b$b/."));
    }
    
    public void test_slashed_String() {
        
        try {
            NameUtils.slashed(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        /*
         * 
         */
        
        assertEquals("", NameUtils.slashed(""));
        assertEquals("a", NameUtils.slashed("a"));
        assertEquals("a/", NameUtils.slashed("a."));
        assertEquals("/a", NameUtils.slashed(".a"));
        assertEquals("/a/", NameUtils.slashed(".a."));
        assertEquals("//a$a//b$b//", NameUtils.slashed("/.a$a..b$b./"));
    }

    public void test_quoted_String() {
        
        try {
            NameUtils.quoted(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        /*
         * 
         */
        
        assertEquals("\"\"", NameUtils.quoted(""));
        assertEquals("\"a\"", NameUtils.quoted("a"));
    }
    
    public void test_getPackageName_String() {
        
        try {
            NameUtils.getPackageName(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        /*
         * 
         */
        
        assertEquals("", NameUtils.getPackageName(""));
        assertEquals("", NameUtils.getPackageName("a"));
        assertEquals("a", NameUtils.getPackageName("a.b"));
        assertEquals("a.b", NameUtils.getPackageName("a.b.c"));
        assertEquals("a.bb", NameUtils.getPackageName("a.bb.ccc$d"));
        assertEquals("aaa.bb", NameUtils.getPackageName("aaa.bb.c$d$e"));
    }
    
    public void test_getFileNameNoExt_String() {
        
        try {
            NameUtils.getFileNameNoExt(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        /*
         * 
         */
        
        assertEquals("", NameUtils.getFileNameNoExt(""));
        assertEquals("a", NameUtils.getFileNameNoExt("a"));
        assertEquals("b", NameUtils.getFileNameNoExt("a.b"));
        assertEquals("c", NameUtils.getFileNameNoExt("a.b.c"));
        assertEquals("ccc$d", NameUtils.getFileNameNoExt("a.bb.ccc$d"));
        assertEquals("c$d$e", NameUtils.getFileNameNoExt("aaa.bb.c$d$e"));
    }
    
    public void test_getTopLevelClassName_String() {
        
        try {
            NameUtils.getFileNameNoExt(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        /*
         * 
         */
        
        assertEquals("", NameUtils.getTopLevelClassName(""));
        assertEquals("a", NameUtils.getTopLevelClassName("a"));
        assertEquals("a.b", NameUtils.getTopLevelClassName("a.b"));
        assertEquals("a.b.c", NameUtils.getTopLevelClassName("a.b.c"));
        assertEquals("a.bb.ccc", NameUtils.getTopLevelClassName("a.bb.ccc$d"));
        assertEquals("aaa.bb.c", NameUtils.getTopLevelClassName("aaa.bb.c$d$e"));
        
        if (NameUtils.HANDLE_WEIRD_DOLLAR_SIGN_USAGES) {
            assertEquals("$", NameUtils.getTopLevelClassName("$"));
            assertEquals("$$", NameUtils.getTopLevelClassName("$$"));
            assertEquals("$$$", NameUtils.getTopLevelClassName("$$$"));
            //
            assertEquals("a.$", NameUtils.getTopLevelClassName("a.$"));
            assertEquals("a.$$", NameUtils.getTopLevelClassName("a.$$"));
            assertEquals("a.$$$", NameUtils.getTopLevelClassName("a.$$$"));
            //
            assertEquals("$a", NameUtils.getTopLevelClassName("$a"));
            assertEquals("a$", NameUtils.getTopLevelClassName("a$"));
            assertEquals("$a$", NameUtils.getTopLevelClassName("$a$"));
            assertEquals("$a$b", NameUtils.getTopLevelClassName("$a$b"));
            assertEquals("a$b$", NameUtils.getTopLevelClassName("a$b$"));
            assertEquals("$a$b$", NameUtils.getTopLevelClassName("$a$b$"));
            //
            assertEquals("a.$a", NameUtils.getTopLevelClassName("a.$a"));
            assertEquals("a.a$", NameUtils.getTopLevelClassName("a.a$"));
            assertEquals("a.$a$", NameUtils.getTopLevelClassName("a.$a$"));
            //
            assertEquals("a$$b", NameUtils.getTopLevelClassName("a$$b"));
            assertEquals("$a$$b", NameUtils.getTopLevelClassName("$a$$b"));
            assertEquals("a$$b$", NameUtils.getTopLevelClassName("a$$b$"));
            assertEquals("$a$$b$", NameUtils.getTopLevelClassName("$a$$b$"));
            //
            assertEquals("a.a$$b", NameUtils.getTopLevelClassName("a.a$$b"));
            assertEquals("a.$a$$b", NameUtils.getTopLevelClassName("a.$a$$b"));
            assertEquals("a.a$$b$", NameUtils.getTopLevelClassName("a.a$$b$"));
            assertEquals("a.$a$$b$", NameUtils.getTopLevelClassName("a.$a$$b$"));
            //
            assertEquals("a$$$b", NameUtils.getTopLevelClassName("a$$$b"));
            assertEquals("$a$$$b", NameUtils.getTopLevelClassName("$a$$$b"));
            assertEquals("a$$$b$", NameUtils.getTopLevelClassName("a$$$b$"));
            assertEquals("$a$$$b$", NameUtils.getTopLevelClassName("$a$$$b$"));
            //
            assertEquals("a.a$$$b", NameUtils.getTopLevelClassName("a.a$$$b"));
            assertEquals("a.$a$$$b", NameUtils.getTopLevelClassName("a.$a$$$b"));
            assertEquals("a.a$$$b$", NameUtils.getTopLevelClassName("a.a$$$b$"));
            assertEquals("a.$a$$$b$", NameUtils.getTopLevelClassName("a.$a$$$b$"));
            
            /*
             * Dollar sign in package name.
             */
            
            assertEquals("$.$", NameUtils.getTopLevelClassName("$.$"));
            assertEquals("$$.$", NameUtils.getTopLevelClassName("$$.$"));
            assertEquals("$a.$", NameUtils.getTopLevelClassName("$a.$"));
            assertEquals("a$.$", NameUtils.getTopLevelClassName("a$.$"));
            assertEquals("a$$b.$", NameUtils.getTopLevelClassName("a$$b.$"));
            //
            assertEquals("a.$.$", NameUtils.getTopLevelClassName("a.$.$"));
            assertEquals("$.a.$", NameUtils.getTopLevelClassName("$.a.$"));
        }
    }
    
    public void test_getOuterClassFileNameNoExt_String() {
        
        try {
            NameUtils.getOuterClassFileNameNoExt(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        /*
         * 
         */
        
        assertEquals(null, NameUtils.getOuterClassFileNameNoExt(""));
        assertEquals(null, NameUtils.getOuterClassFileNameNoExt("a"));
        assertEquals("a", NameUtils.getOuterClassFileNameNoExt("a$b"));
        assertEquals("aa$bb", NameUtils.getOuterClassFileNameNoExt("aa$bb$cc"));

        if (NameUtils.HANDLE_WEIRD_DOLLAR_SIGN_USAGES) {
            assertEquals(null, NameUtils.getOuterClassFileNameNoExt("$"));
            assertEquals(null, NameUtils.getOuterClassFileNameNoExt("$$"));
            assertEquals(null, NameUtils.getOuterClassFileNameNoExt("$$$"));
            //
            assertEquals(null, NameUtils.getOuterClassFileNameNoExt("$a"));
            assertEquals(null, NameUtils.getOuterClassFileNameNoExt("a$"));
            assertEquals(null, NameUtils.getOuterClassFileNameNoExt("$a$"));
            assertEquals(null, NameUtils.getOuterClassFileNameNoExt("$a$b"));
            assertEquals(null, NameUtils.getOuterClassFileNameNoExt("a$b$"));
            assertEquals(null, NameUtils.getOuterClassFileNameNoExt("$a$b$"));
            //
            assertEquals(null, NameUtils.getOuterClassFileNameNoExt("a$$b"));
            assertEquals(null, NameUtils.getOuterClassFileNameNoExt("$a$$b"));
            assertEquals(null, NameUtils.getOuterClassFileNameNoExt("a$$b$"));
            assertEquals(null, NameUtils.getOuterClassFileNameNoExt("$a$$b$"));
            //
            assertEquals(null, NameUtils.getOuterClassFileNameNoExt("a$$$b"));
            assertEquals(null, NameUtils.getOuterClassFileNameNoExt("$a$$$b"));
            assertEquals(null, NameUtils.getOuterClassFileNameNoExt("a$$$b$"));
            assertEquals(null, NameUtils.getOuterClassFileNameNoExt("$a$$$b$"));
        }
    }
    
    public void test_splitName_String() {
        
        try {
            NameUtils.splitName(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        for (String bad : CodeTestUtils.newBadNames()) {
            
            if (DEBUG) {
                System.out.println("bad = " + bad);
            }
            
            try {
                final String[] split = NameUtils.splitName(bad);
                
                if (DEBUG) {
                    System.out.println("split length = " + split.length);
                    for (String s : split) {
                        System.out.println(s + " (length = " + s.length() + ")");
                    }
                }
                
                assertTrue(false);
            } catch (IllegalArgumentException e) {
                // ok
            }
        }
        
        /*
         * 
         */
        
        checkEqual(new String[]{"a"}, NameUtils.splitName("a"));
        checkEqual(new String[]{"a","b"}, NameUtils.splitName("a.b"));
        checkEqual(new String[]{"a","b","c"}, NameUtils.splitName("a.b.c"));
        checkEqual(new String[]{"a","bb","ccc"}, NameUtils.splitName("a.bb.ccc"));
        checkEqual(new String[]{"aaa","bb","c"}, NameUtils.splitName("aaa.bb.c"));
        
        if (NameUtils.HANDLE_WEIRD_DOLLAR_SIGN_USAGES) {
            checkEqual(new String[]{"foo","bar","$"}, NameUtils.splitName("foo.bar.$"));
            checkEqual(new String[]{"foo","bar","$$"}, NameUtils.splitName("foo.bar.$$"));
            checkEqual(new String[]{"foo","bar","$a"}, NameUtils.splitName("foo.bar.$a"));
            checkEqual(new String[]{"foo","bar","a$"}, NameUtils.splitName("foo.bar.a$"));
            checkEqual(new String[]{"foo","bar","a$$b"}, NameUtils.splitName("foo.bar.a$$b"));
            
            /*
             * Dollar sign in package name.
             */
            
            checkEqual(new String[]{"$","bar","$"}, NameUtils.splitName("$.bar.$"));
            checkEqual(new String[]{"foo","$","$"}, NameUtils.splitName("foo.$.$"));
        }
    }

    /*
     * 
     */

    public void test_toRegex_String() {
        
        try {
            NameUtils.toRegex(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        /*
         * 
         */
        
        assertEquals("", NameUtils.toRegex(""));
        assertEquals("a", NameUtils.toRegex("a"));
        assertEquals("a\\.", NameUtils.toRegex("a."));
        assertEquals("\\.a", NameUtils.toRegex(".a"));
        assertEquals("\\.a\\.", NameUtils.toRegex(".a."));
        assertEquals("/\\.a$a\\.\\.b$b\\./\\", NameUtils.toRegex("/.a$a..b$b./\\"));
    }
    
    public void test_toDisplayName_String() {
        
        try {
            NameUtils.toDisplayName(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        /*
         * 
         */
        
        // Same instance.
        assertSame(NameUtils.DEFAULT_PACKAGE_DISPLAY_NAME, NameUtils.toDisplayName(""));
        
        for (String name : new String[]{
                "a",
                "a.b"
        }) {
            // Same instance.
            assertSame(name, NameUtils.toDisplayName(name));
        }
    }

    /*
     * 
     */
    
    public void test_startsWithName_2String() {
        
        try {
            NameUtils.startsWithName(null, "good");
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        try {
            NameUtils.startsWithName("good", null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        /*
         * 
         */
        
        {
            final String name = "";
            //
            assertTrue(NameUtils.startsWithName("", name));
            assertTrue(NameUtils.startsWithName("a", name));
            assertTrue(NameUtils.startsWithName("a.b", name));
        }
        {
            final String name = "a";
            //
            assertFalse(NameUtils.startsWithName("", name));
            //
            assertTrue(NameUtils.startsWithName("a", name));
            assertTrue(NameUtils.startsWithName("a.b", name));
            assertFalse(NameUtils.startsWithName("ab", name));
            assertFalse(NameUtils.startsWithName("ba", name));
            assertFalse(NameUtils.startsWithName("b", name));
            assertFalse(NameUtils.startsWithName("b.a", name));
        }
        {
            final String name = "a.b";
            //
            assertFalse(NameUtils.startsWithName("", name));
            //
            assertFalse(NameUtils.startsWithName("a", name));
            assertFalse(NameUtils.startsWithName("a.", name));
            assertTrue(NameUtils.startsWithName("a.b", name));
            assertTrue(NameUtils.startsWithName("a.b.", name));
            assertTrue(NameUtils.startsWithName("a.b$", name));
            assertTrue(NameUtils.startsWithName("a.b.c", name));
            assertTrue(NameUtils.startsWithName("a.b$c", name));
            assertFalse(NameUtils.startsWithName("a.bc", name));
            assertFalse(NameUtils.startsWithName("b.a", name));
        }
        {
            final String name = "a$b$c";
            //
            assertFalse(NameUtils.startsWithName("", name));
            //
            assertFalse(NameUtils.startsWithName("a", name));
            assertFalse(NameUtils.startsWithName("a$b$", name));
            assertTrue(NameUtils.startsWithName("a$b$c", name));
            // Illegal but still handled.
            assertTrue(NameUtils.startsWithName("a$b$c.", name));
            // Illegal but still handled.
            assertTrue(NameUtils.startsWithName("a$b$c$", name));
            // Illegal but still handled.
            assertTrue(NameUtils.startsWithName("a$b$c.d", name));
            assertTrue(NameUtils.startsWithName("a$b$c$d", name));
            assertFalse(NameUtils.startsWithName("a$b$cd", name));
            // Illegal but still handled.
            assertFalse(NameUtils.startsWithName(".a$b$c", name));
            // Illegal but still handled.
            assertFalse(NameUtils.startsWithName("$a$b$c", name));
            assertFalse(NameUtils.startsWithName("da$b$c", name));
        }
    }
    
    public void test_endsWithName_2String() {
        
        try {
            NameUtils.endsWithName(null, "good");
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        try {
            NameUtils.endsWithName("good", null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        /*
         * 
         */
        
        {
            final String name = "";
            //
            assertTrue(NameUtils.endsWithName("", name));
            assertTrue(NameUtils.endsWithName("a", name));
            assertTrue(NameUtils.endsWithName("a.b", name));
        }
        {
            final String name = "a";
            //
            assertFalse(NameUtils.endsWithName("", name));
            //
            assertTrue(NameUtils.endsWithName("a", name));
            // Illegal but still handled.
            assertTrue(NameUtils.endsWithName(".a", name));
            // Illegal but still handled.
            assertTrue(NameUtils.endsWithName("$a", name));
            assertTrue(NameUtils.endsWithName("b.a", name));
            assertTrue(NameUtils.endsWithName("b$a", name));
            assertFalse(NameUtils.endsWithName("ba", name));
        }
        {
            final String name = "ab";
            //
            assertFalse(NameUtils.endsWithName("", name));
            //
            assertFalse(NameUtils.endsWithName("b", name));
            assertTrue(NameUtils.endsWithName("ab", name));
            // Illegal but still handled.
            assertTrue(NameUtils.endsWithName(".ab", name));
            // Illegal but still handled.
            assertTrue(NameUtils.endsWithName("$ab", name));
            assertTrue(NameUtils.endsWithName("c.ab", name));
            assertTrue(NameUtils.endsWithName("c$ab", name));
            assertFalse(NameUtils.endsWithName("cab", name));
            assertFalse(NameUtils.endsWithName("ba", name));
        }
    }
    
    public void test_containsName_2String() {
        
        try {
            NameUtils.containsName(null, "");
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        try {
            NameUtils.containsName("", null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        /*
         * 
         */
        
        {
            final String name = "";
            //
            assertTrue(NameUtils.containsName("", name));
            assertTrue(NameUtils.containsName("a", name));
            assertTrue(NameUtils.containsName("a.b", name));
        }
        {
            final String name = "b";
            //
            assertFalse(NameUtils.containsName("", name));
            //
            assertTrue(NameUtils.containsName("b", name));
            // Illegal but still handled.
            assertTrue(NameUtils.containsName(".b", name));
            // Illegal but still handled.
            assertTrue(NameUtils.containsName("$b", name));
            // Illegal but still handled.
            assertTrue(NameUtils.containsName("b.", name));
            // Illegal but still handled.
            assertTrue(NameUtils.containsName("b$", name));
            // Illegal but still handled.
            assertTrue(NameUtils.containsName(".b.", name));
            // Illegal but still handled.
            assertTrue(NameUtils.containsName(".b$", name));
            // Illegal and still handled.
            assertTrue(NameUtils.containsName("$b.", name));
            //
            assertTrue(NameUtils.containsName("a.b", name));
            assertTrue(NameUtils.containsName("a$b", name));
            assertTrue(NameUtils.containsName("b.c", name));
            assertTrue(NameUtils.containsName("b$c", name));
            assertTrue(NameUtils.containsName("a.b.c", name));
            assertTrue(NameUtils.containsName("a.b$c", name));
            assertTrue(NameUtils.containsName("a$b$c", name));
            // Illegal and still handled.
            assertTrue(NameUtils.containsName("a$b.c", name));
            // Illegal and still handled.
            assertTrue(NameUtils.containsName("cb.b.c", name));
            //
            assertFalse(NameUtils.containsName("ab", name));
            assertFalse(NameUtils.containsName("bc", name));
            assertFalse(NameUtils.containsName("abc", name));
        }
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    private static void checkEqual(String[] expected, String[] actual) {
        assertEquals(Arrays.toString(expected), Arrays.toString(actual));
    }
}
