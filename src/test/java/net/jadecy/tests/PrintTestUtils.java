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
package net.jadecy.tests;

import java.util.List;

import junit.framework.TestCase;

/**
 * For use with MemPrintStream.
 */
public class PrintTestUtils {

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    public static String[] toStringTab(List<String> lineList) {
        return lineList.toArray(new String[lineList.size()]);
    }
    
    public static void checkEqual(String[] expected, String[] actual) {
        if (expected.length != actual.length) {
            printLines(expected, actual);
            TestCase.assertEquals(expected.length, actual.length);
        }
        for (int i = 0; i < expected.length; i++) {
            if (!expected[i].equals(actual[i])) {
                printLines(expected, actual);
                TestCase.assertEquals(expected[i], actual[i]);
            }
        }
    }
    
    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------

    private PrintTestUtils() {
    }
    
    /*
     * 
     */
    
    private static void printLines(String[] expected, String[] actual) {
        System.out.println();
        System.out.println("expected:");
        System.out.println("#########");
        for (String line : expected) {
            System.out.println(line);
        }
        System.out.println("#########");
        System.out.println();
        System.out.println("actual:");
        System.out.println("#######");
        for (String line : actual) {
            System.out.println(line);
        }
        System.out.println("#######");
    }
}
