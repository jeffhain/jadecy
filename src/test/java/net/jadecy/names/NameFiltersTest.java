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
package net.jadecy.names;

import net.jadecy.names.InterfaceNameFilter;
import net.jadecy.names.NameFilters;
import junit.framework.TestCase;

public class NameFiltersTest extends TestCase {

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    public void test_areCompatible_2String() {
        
        try {
            NameFilters.areCompatible(null, "");
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        try {
            NameFilters.areCompatible("", null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        for (String[] pair : new String[][]{
                {"", ""},
                {"", "a"},
                {"a", ""},
                {"a", "a"},
                {"a", "ab"},
                {"ab", "a"},
                {"ab", "ab"},
                {"foo", "foo.bar"},
        }) {
            assertTrue(NameFilters.areCompatible(pair[0], pair[1]));
        }
        
        for (String[] pair : new String[][]{
                {"a", "b"},
                {"a", "ba"},
                {"ab", "b"},
                {"ab", "ba"},
                {"foo", "bar"},
        }) {
            assertFalse(NameFilters.areCompatible(pair[0], pair[1]));
        }
    }
    
    /*
     * 
     */
    
    public void test_any() {
        InterfaceNameFilter filter = NameFilters.any();
        checkAccepts_anyBasic(filter);
        
        assertEquals("any", filter.toString());
    }

    public void test_none() {
        InterfaceNameFilter filter = NameFilters.none();
        checkAccepts_noneBasic(filter);
        checkRecognizedAs_none(filter);
        
        assertEquals("none", filter.toString());
    }
    
    /*
     * 
     */
    
    public void test_not_InterfaceNameFilter() {
        try {
            NameFilters.not(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        {
            InterfaceNameFilter filter = NameFilters.not(NameFilters.any());
            checkAccepts_noneBasic(filter);
            check_NOT_RecognizedAs_none(filter);
            
            assertEquals("not(any)", filter.toString());
        }
        {
            InterfaceNameFilter filter = NameFilters.not(NameFilters.none());
            checkAccepts_anyBasic(filter);
            
            assertEquals("not(none)", filter.toString());
        }
    }
    
    /*
     * 
     */

    public void test_and_arrayOfInterfaceNameFilter() {
        try {
            NameFilters.and((InterfaceNameFilter[]) null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        try {
            NameFilters.and(new InterfaceNameFilter[]{null});
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        {
            InterfaceNameFilter filter = NameFilters.and();
            checkAccepts_noneBasic(filter);
            checkRecognizedAs_none(filter);
            
            assertEquals("and()", filter.toString());
        }
        {
            InterfaceNameFilter filter = NameFilters.and(
                    NameFilters.startsWith("a"),
                    NameFilters.startsWith("b"));
            checkAccepts_noneBasic(filter);
            checkRecognizedAs_none(filter);
            
            assertEquals("and(startsWith(a),startsWith(b))", filter.toString());
        }
        {
            InterfaceNameFilter filter = NameFilters.and(
                    NameFilters.startsWith("ab"),
                    NameFilters.startsWith("bc"));
            checkAccepts_noneBasic(filter);
            checkRecognizedAs_none(filter);
        }
        {
            InterfaceNameFilter filter = NameFilters.and(
                    NameFilters.none(),
                    NameFilters.startsWith("a"));
            checkAccepts_noneBasic(filter);
            // Recognized due to incompatibility between prefixes.
            checkRecognizedAs_none(filter);
        }
        
        /*
         * 
         */
        
        {
            InterfaceNameFilter filter = NameFilters.and(
                    NameFilters.startsWith("a"));
            //
            assertEquals("a", filter.getPrefix());
            //
            assertFalse(filter.accept(""));
            //
            assertTrue(filter.accept("a"));
            assertTrue(filter.accept("ab"));
            
            assertEquals("and(startsWith(a))", filter.toString());
        }
        {
            InterfaceNameFilter filter = NameFilters.and(
                    NameFilters.any(),
                    NameFilters.startsWith("a"));
            //
            assertEquals("a", filter.getPrefix());
            //
            assertFalse(filter.accept(""));
            //
            assertTrue(filter.accept("a"));
            assertTrue(filter.accept("ab"));
        }
        {
            InterfaceNameFilter filter = NameFilters.and(
                    NameFilters.startsWith("a"),
                    NameFilters.startsWith("ab"));
            //
            assertEquals("ab", filter.getPrefix());
            //
            assertFalse(filter.accept(""));
            //
            assertFalse(filter.accept("a"));
            assertTrue(filter.accept("ab"));
            assertTrue(filter.accept("abc"));
        }
        {
            InterfaceNameFilter filter = NameFilters.and(
                    NameFilters.startsWith("abcd"),
                    NameFilters.startsWith("ab"),
                    NameFilters.startsWith("abc"));
            //
            assertEquals("abcd", filter.getPrefix());
            //
            assertFalse(filter.accept(""));
            //
            assertFalse(filter.accept("a"));
            assertFalse(filter.accept("ab"));
            assertFalse(filter.accept("abc"));
            assertTrue(filter.accept("abcd"));
            assertTrue(filter.accept("abcde"));
        }
    }

    public void test_or_arrayOfInterfaceNameFilter() {
        try {
            NameFilters.or((InterfaceNameFilter[]) null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        try {
            NameFilters.or(new InterfaceNameFilter[]{null});
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        {
            InterfaceNameFilter filter = NameFilters.or();
            checkAccepts_noneBasic(filter);
            checkRecognizedAs_none(filter);
            
            assertEquals("or()", filter.toString());
        }
        {
            InterfaceNameFilter filter = NameFilters.or(
                    NameFilters.none());
            checkAccepts_noneBasic(filter);
            checkRecognizedAs_none(filter);
            
            assertEquals("or(none)", filter.toString());
        }

        /*
         * 
         */
        
        {
            InterfaceNameFilter filter = NameFilters.or(
                    NameFilters.startsWith("a"));
            //
            assertEquals("a", filter.getPrefix());
            //
            assertFalse(filter.accept(""));
            //
            assertTrue(filter.accept("a"));
            assertTrue(filter.accept("ab"));
            
            assertEquals("or(startsWith(a))", filter.toString());
        }
        {
            InterfaceNameFilter filter = NameFilters.or(
                    NameFilters.none(),
                    NameFilters.startsWith("a"));
            //
            assertEquals("", filter.getPrefix());
            //
            assertFalse(filter.accept(""));
            //
            assertTrue(filter.accept("a"));
            assertTrue(filter.accept("ab"));
            
            assertEquals("or(none,startsWith(a))", filter.toString());
        }
        {
            InterfaceNameFilter filter = NameFilters.or(
                    NameFilters.any(),
                    NameFilters.startsWith("a"));
            checkAccepts_anyBasic(filter);
        }
        {
            InterfaceNameFilter filter = NameFilters.or(
                    NameFilters.startsWith("a"),
                    NameFilters.startsWith("ab"));
            //
            assertEquals("a", filter.getPrefix());
            //
            assertFalse(filter.accept(""));
            //
            assertTrue(filter.accept("a"));
            assertTrue(filter.accept("ab"));
            assertTrue(filter.accept("abc"));
        }
        {
            InterfaceNameFilter filter = NameFilters.or(
                    NameFilters.startsWith("abd"),
                    NameFilters.startsWith("ab"),
                    NameFilters.startsWith("abc"));
            //
            assertEquals("ab", filter.getPrefix());
            //
            assertFalse(filter.accept(""));
            //
            assertFalse(filter.accept("a"));
            assertTrue(filter.accept("ab"));
            assertTrue(filter.accept("abc"));
            assertTrue(filter.accept("abd"));
            assertTrue(filter.accept("abcd"));
        }
        {
            InterfaceNameFilter filter = NameFilters.or(
                    NameFilters.startsWith("abc"),
                    NameFilters.startsWith("de"),
                    NameFilters.startsWith("abh"));
            //
            assertEquals("", filter.getPrefix());
            //
            assertFalse(filter.accept(""));
            //
            assertFalse(filter.accept("a"));
            assertFalse(filter.accept("ab"));
            assertTrue(filter.accept("abc"));
            assertTrue(filter.accept("abcd"));
            //
            assertFalse(filter.accept("d"));
            assertTrue(filter.accept("de"));
            assertTrue(filter.accept("dea"));
            //
            assertFalse(filter.accept("a"));
            assertFalse(filter.accept("ab"));
            assertTrue(filter.accept("abh"));
            assertTrue(filter.accept("abhd"));
        }
    }

    public void test_equalsName_String() {
        try {
            NameFilters.equalsName(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        {
            InterfaceNameFilter filter = NameFilters.equalsName("");
            //
            assertEquals("", filter.getPrefix());
            //
            assertTrue(filter.accept(""));
            //
            assertFalse(filter.accept("a"));
            assertFalse(filter.accept("b"));
            
            assertEquals("equalsName()", filter.toString());
        }
        {
            InterfaceNameFilter filter = NameFilters.equalsName("a");
            //
            assertEquals("a", filter.getPrefix());
            //
            assertFalse(filter.accept(""));
            //
            assertTrue(filter.accept("a"));
            assertFalse(filter.accept("ab"));
            assertFalse(filter.accept("b"));
            
            assertEquals("equalsName(a)", filter.toString());
        }
        {
            InterfaceNameFilter filter = NameFilters.equalsName("ab");
            //
            assertEquals("ab", filter.getPrefix());
            //
            assertFalse(filter.accept(""));
            //
            assertFalse(filter.accept("a"));
            assertTrue(filter.accept("ab"));
            assertFalse(filter.accept("abc"));
            assertFalse(filter.accept("ba"));
        }
    }

    public void test_startsWith_String() {
        try {
            NameFilters.startsWith(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        {
            InterfaceNameFilter filter = NameFilters.startsWith("");
            checkAccepts_anyBasic(filter);
            
            assertEquals("startsWith()", filter.toString());
        }
        {
            InterfaceNameFilter filter = NameFilters.startsWith("a");
            //
            assertEquals("a", filter.getPrefix());
            //
            assertFalse(filter.accept(""));
            //
            assertTrue(filter.accept("a"));
            assertTrue(filter.accept("ab"));
            assertFalse(filter.accept("ba"));
            assertFalse(filter.accept("b"));
            
            assertEquals("startsWith(a)", filter.toString());
        }
        {
            InterfaceNameFilter filter = NameFilters.startsWith("ab");
            //
            assertEquals("ab", filter.getPrefix());
            //
            assertFalse(filter.accept(""));
            //
            assertFalse(filter.accept("a"));
            assertTrue(filter.accept("ab"));
            assertTrue(filter.accept("abc"));
            assertFalse(filter.accept("ba"));
        }
    }

    public void test_startsWithName_String() {
        try {
            NameFilters.startsWithName(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        {
            InterfaceNameFilter filter = NameFilters.startsWithName("");
            checkAccepts_anyBasic(filter);
            
            assertEquals("startsWithName()", filter.toString());
        }
        {
            InterfaceNameFilter filter = NameFilters.startsWithName("a");
            //
            assertEquals("a", filter.getPrefix());
            //
            assertFalse(filter.accept(""));
            //
            assertTrue(filter.accept("a"));
            assertTrue(filter.accept("a.b"));
            assertFalse(filter.accept("ab"));
            assertFalse(filter.accept("ba"));
            assertFalse(filter.accept("b"));
            assertFalse(filter.accept("b.a"));
            
            assertEquals("startsWithName(a)", filter.toString());
        }
        {
            InterfaceNameFilter filter = NameFilters.startsWithName("a.b");
            //
            assertEquals("a.b", filter.getPrefix());
            //
            assertFalse(filter.accept(""));
            //
            assertFalse(filter.accept("a"));
            assertFalse(filter.accept("a."));
            assertTrue(filter.accept("a.b"));
            assertTrue(filter.accept("a.b."));
            assertTrue(filter.accept("a.b$"));
            assertTrue(filter.accept("a.b.c"));
            assertTrue(filter.accept("a.b$c"));
            assertFalse(filter.accept("a.bc"));
            assertFalse(filter.accept("b.a"));
        }
        {
            InterfaceNameFilter filter = NameFilters.startsWithName("a$b$c");
            //
            assertEquals("a$b$c", filter.getPrefix());
            //
            assertFalse(filter.accept(""));
            //
            assertFalse(filter.accept("a"));
            assertFalse(filter.accept("a$b$"));
            assertTrue(filter.accept("a$b$c"));
            // Illegal but still handled.
            assertTrue(filter.accept("a$b$c."));
            // Illegal but still handled.
            assertTrue(filter.accept("a$b$c$"));
            // Illegal but still handled.
            assertTrue(filter.accept("a$b$c.d"));
            assertTrue(filter.accept("a$b$c$d"));
            assertFalse(filter.accept("a$b$cd"));
            // Illegal but still handled.
            assertFalse(filter.accept(".a$b$c"));
            // Illegal but still handled.
            assertFalse(filter.accept("$a$b$c"));
            assertFalse(filter.accept("da$b$c"));
        }
    }
    
    public void test_endsWith_String() {
        try {
            NameFilters.endsWith(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        {
            InterfaceNameFilter filter = NameFilters.endsWith("");
            checkAccepts_anyBasic(filter);
            
            assertEquals("endsWith()", filter.toString());
        }
        {
            InterfaceNameFilter filter = NameFilters.endsWith("a");
            //
            assertEquals("", filter.getPrefix());
            //
            assertFalse(filter.accept(""));
            //
            assertTrue(filter.accept("a"));
            assertTrue(filter.accept("ba"));
            assertFalse(filter.accept("ab"));
            
            assertEquals("endsWith(a)", filter.toString());
        }
        {
            InterfaceNameFilter filter = NameFilters.endsWith("ab");
            //
            assertEquals("", filter.getPrefix());
            //
            assertFalse(filter.accept(""));
            //
            assertFalse(filter.accept("a"));
            assertTrue(filter.accept("ab"));
            assertTrue(filter.accept("cab"));
            assertFalse(filter.accept("ba"));
        }
    }
    
    public void test_endsWithName_String() {
        try {
            NameFilters.endsWithName(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        {
            InterfaceNameFilter filter = NameFilters.endsWithName("");
            checkAccepts_anyBasic(filter);
            
            assertEquals("endsWithName()", filter.toString());
        }
        {
            InterfaceNameFilter filter = NameFilters.endsWithName("a");
            //
            assertEquals("", filter.getPrefix());
            //
            assertFalse(filter.accept(""));
            //
            assertTrue(filter.accept("a"));
            // Illegal but still handled.
            assertTrue(filter.accept(".a"));
            // Illegal but still handled.
            assertTrue(filter.accept("$a"));
            assertTrue(filter.accept("b.a"));
            assertTrue(filter.accept("b$a"));
            assertFalse(filter.accept("ba"));
            
            assertEquals("endsWithName(a)", filter.toString());
        }
        {
            InterfaceNameFilter filter = NameFilters.endsWithName("ab");
            //
            assertEquals("", filter.getPrefix());
            //
            assertFalse(filter.accept(""));
            //
            assertFalse(filter.accept("b"));
            assertTrue(filter.accept("ab"));
            // Illegal but still handled.
            assertTrue(filter.accept(".ab"));
            // Illegal but still handled.
            assertTrue(filter.accept("$ab"));
            assertTrue(filter.accept("c.ab"));
            assertTrue(filter.accept("c$ab"));
            assertFalse(filter.accept("cab"));
            assertFalse(filter.accept("ba"));
        }
    }

    public void test_contains_String() {
        try {
            NameFilters.contains(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        {
            InterfaceNameFilter filter = NameFilters.contains("");
            checkAccepts_anyBasic(filter);
            
            assertEquals("contains()", filter.toString());
        }
        {
            InterfaceNameFilter filter = NameFilters.contains("a");
            //
            assertEquals("", filter.getPrefix());
            //
            assertFalse(filter.accept(""));
            //
            assertTrue(filter.accept("a"));
            assertTrue(filter.accept("ab"));
            assertTrue(filter.accept("ba"));
            assertFalse(filter.accept("b"));
            
            assertEquals("contains(a)", filter.toString());
        }
        {
            InterfaceNameFilter filter = NameFilters.contains("ab");
            //
            assertEquals("", filter.getPrefix());
            //
            assertFalse(filter.accept(""));
            //
            assertFalse(filter.accept("a"));
            assertTrue(filter.accept("ab"));
            assertTrue(filter.accept("abc"));
            assertTrue(filter.accept("cab"));
            assertFalse(filter.accept("ba"));
        }
    }
    
    public void test_containsName_String() {
        try {
            NameFilters.containsName(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        {
            InterfaceNameFilter filter = NameFilters.containsName("");
            checkAccepts_anyBasic(filter);
            
            assertEquals("containsName()", filter.toString());
        }
        {
            InterfaceNameFilter filter = NameFilters.containsName("b");
            //
            assertEquals("", filter.getPrefix());
            //
            assertFalse(filter.accept(""));
            //
            assertTrue(filter.accept("b"));
            // Illegal but still handled.
            assertTrue(filter.accept(".b"));
            // Illegal but still handled.
            assertTrue(filter.accept("$b"));
            // Illegal but still handled.
            assertTrue(filter.accept("b."));
            // Illegal but still handled.
            assertTrue(filter.accept("b$"));
            // Illegal but still handled.
            assertTrue(filter.accept(".b."));
            // Illegal but still handled.
            assertTrue(filter.accept(".b$"));
            // Illegal and still handled.
            assertTrue(filter.accept("$b."));
            //
            assertTrue(filter.accept("a.b"));
            assertTrue(filter.accept("a$b"));
            assertTrue(filter.accept("b.c"));
            assertTrue(filter.accept("b$c"));
            assertTrue(filter.accept("a.b.c"));
            assertTrue(filter.accept("a.b$c"));
            assertTrue(filter.accept("a$b$c"));
            // Illegal and still handled.
            assertTrue(filter.accept("a$b.c"));
            // Illegal and still handled.
            assertTrue(filter.accept("cb.b.c"));
            //
            assertFalse(filter.accept("ab"));
            assertFalse(filter.accept("bc"));
            assertFalse(filter.accept("abc"));
            
            assertEquals("containsName(b)", filter.toString());
        }
    }

    public void test_matches_String() {
        try {
            NameFilters.matches(null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
        
        {
            InterfaceNameFilter filter = NameFilters.matches("");
            //
            assertEquals("", filter.getPrefix());
            //
            assertTrue(filter.accept(""));
            //
            assertFalse(filter.accept("a"));
            
            assertEquals("matches()", filter.toString());
        }
        {
            InterfaceNameFilter filter = NameFilters.matches("a");
            //
            // We don't bother to compute a prefix
            // (can always use newAnd(startsWithNameFilter,matchesFilter)).
            assertEquals("", filter.getPrefix());
            //
            assertFalse(filter.accept(""));
            //
            assertTrue(filter.accept("a"));
            assertFalse(filter.accept("aa"));
            
            assertEquals("matches(a)", filter.toString());
        }
        {
            InterfaceNameFilter filter = NameFilters.matches("[a-z]+");
            //
            assertEquals("", filter.getPrefix());
            //
            assertFalse(filter.accept(""));
            //
            assertTrue(filter.accept("a"));
            assertTrue(filter.accept("ab"));
            assertTrue(filter.accept("abz"));
            assertTrue(filter.accept("aa"));
            assertFalse(filter.accept("a0a"));
            
            assertEquals("matches([a-z]+)", filter.toString());
        }
        {
            InterfaceNameFilter filter = NameFilters.matches("a.b");
            //
            assertEquals("", filter.getPrefix());
            //
            assertFalse(filter.accept(""));
            //
            assertFalse(filter.accept("a"));
            assertFalse(filter.accept("ab"));
            assertTrue(filter.accept("a.b"));
            // '.' part of regex stuffs, and matches "any char".
            assertTrue(filter.accept("aab"));
        }
        {
            InterfaceNameFilter filter = NameFilters.matches("a\\.b");
            //
            assertEquals("", filter.getPrefix());
            //
            assertFalse(filter.accept(""));
            //
            assertFalse(filter.accept("a"));
            assertFalse(filter.accept("ab"));
            assertTrue(filter.accept("a.b"));
            // "\\" to de-regex '.'.
            assertFalse(filter.accept("aab"));
        }
    }
    
    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------

    /**
     * Checks that treatments recognized it as filter accepting anything
     * we use in basic tests.
     */
    private static void checkAccepts_anyBasic(InterfaceNameFilter filter) {
        assertEquals("", filter.getPrefix());
        assertTrue(filter.accept(""));
        assertTrue(filter.accept("a"));
        assertTrue(filter.accept("b"));
        assertTrue(filter.accept("ab"));
        assertTrue(filter.accept("ba"));
        assertTrue(filter.accept("foo"));
        assertTrue(filter.accept("bar"));
    }

    /**
     * Checks that treatments recognized it as filter accepting nothing.
     */
    private static void checkAccepts_noneBasic(InterfaceNameFilter filter) {
        assertTrue(
                filter.getPrefix().equals("")
                || filter.getPrefix().equals(NameFilters.NONE_PREFIX));
        assertFalse(filter.accept(""));
        assertFalse(filter.accept("a"));
        assertFalse(filter.accept("b"));
        assertFalse(filter.accept("ab"));
        assertFalse(filter.accept("ba"));
        assertFalse(filter.accept("foo"));
        assertFalse(filter.accept("bar"));
    }

    private static void checkRecognizedAs_none(InterfaceNameFilter filter) {
        assertEquals(NameFilters.NONE_PREFIX, filter.getPrefix());
    }

    private static void check_NOT_RecognizedAs_none(InterfaceNameFilter filter) {
        assertFalse(filter.getPrefix().equals(NameFilters.NONE_PREFIX));
    }
}
