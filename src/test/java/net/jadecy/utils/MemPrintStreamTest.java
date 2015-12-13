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

import java.util.List;

import net.jadecy.utils.MemPrintStream;
import junit.framework.TestCase;

public class MemPrintStreamTest extends TestCase {

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    public void test_MemPrintStream() {
        final MemPrintStream stream = new MemPrintStream();
        try {
            assertTrue(stream.getMustIgnoreEmptyLastLine());
            assertEquals(0, stream.getLines().size());
        } finally {
            stream.close();
        }
    }

    public void test_MemPrintStream_boolean() {
        for (boolean mustIgnoreEmptyLastLine : new boolean[]{false,true}) {
            final MemPrintStream stream = new MemPrintStream(mustIgnoreEmptyLastLine);
            try {
                assertEquals(mustIgnoreEmptyLastLine, stream.getMustIgnoreEmptyLastLine());
                assertEquals(0, stream.getLines().size());
            } finally {
                stream.close();
            }
        }
    }

    public void test_getMustIgnoreEmptyLastLine() {
        // Already covered in constructors tests.
    }

    public void test_clear() {
        for (boolean mustIgnoreEmptyLastLine : new boolean[]{false,true}) {
            final MemPrintStream stream = new MemPrintStream(mustIgnoreEmptyLastLine);
            try {
                stream.println();
                stream.clear();
                assertEquals(0, stream.getLines().size());

                stream.print("a");
                stream.clear();
                assertEquals(0, stream.getLines().size());

                stream.print("a");
                stream.println();
                stream.clear();
                assertEquals(0, stream.getLines().size());
            } finally {
                stream.close();
            }
        }
    }

    public void test_getLines() {
        /*
         * Content test covered by other tests.
         * Just testing that it's a copy and mutable.
         */
        for (boolean mustIgnoreEmptyLastLine : new boolean[]{false,true}) {
            final MemPrintStream stream = new MemPrintStream(mustIgnoreEmptyLastLine);
            try {
                stream.println("a");
                stream.println("b");

                // Mutable.
                final List<String> lines1 = stream.getLines();
                lines1.set(0, "c");
                lines1.set(1, "d");
                lines1.add("e");

                // Not polluted by previous mutation.
                final List<String> lines2 = stream.getLines();
                assertEquals(vlc(stream, 2), lines2.size());
                assertEquals("a", lines2.get(0));
                assertEquals("b", lines2.get(1));
                checkLastIsEmptyLineIfNotIgnoring(stream);
            } finally {
                stream.close();
            }
        }
    }

    /*
     * 
     */

    public void test_print_boolean() {
        for (boolean mustIgnoreEmptyLastLine : new boolean[]{false,true}) {
            final MemPrintStream stream = new MemPrintStream(mustIgnoreEmptyLastLine);
            try {
                stream.print(false);
                assertEquals(1, stream.getLines().size());
                assertEquals("false", stream.getLines().get(0));

                stream.print(true);
                assertEquals(1, stream.getLines().size());
                assertEquals("falsetrue", stream.getLines().get(0));
            } finally {
                stream.close();
            }
        }
    }

    public void test_print_char() {
        for (boolean mustIgnoreEmptyLastLine : new boolean[]{false,true}) {
            final MemPrintStream stream = new MemPrintStream(mustIgnoreEmptyLastLine);
            try {
                stream.print((char) 'a');
                assertEquals(1, stream.getLines().size());
                assertEquals("a", stream.getLines().get(0));

                stream.print((char) 'b');
                assertEquals(1, stream.getLines().size());
                assertEquals("ab", stream.getLines().get(0));
            } finally {
                stream.close();
            }
        }
    }

    public void test_print_int() {
        for (boolean mustIgnoreEmptyLastLine : new boolean[]{false,true}) {
            final MemPrintStream stream = new MemPrintStream(mustIgnoreEmptyLastLine);
            try {
                stream.print(17);
                assertEquals(1, stream.getLines().size());
                assertEquals("17", stream.getLines().get(0));

                stream.print(Integer.MAX_VALUE);
                assertEquals(1, stream.getLines().size());
                assertEquals("17" + Integer.MAX_VALUE, stream.getLines().get(0));
            } finally {
                stream.close();
            }
        }
    }

    public void test_print_long() {
        for (boolean mustIgnoreEmptyLastLine : new boolean[]{false,true}) {
            final MemPrintStream stream = new MemPrintStream(mustIgnoreEmptyLastLine);
            try {
                stream.print(17L);
                assertEquals(1, stream.getLines().size());
                assertEquals("17", stream.getLines().get(0));

                stream.print(Long.MAX_VALUE);
                assertEquals(1, stream.getLines().size());
                assertEquals("17" + Long.MAX_VALUE, stream.getLines().get(0));
            } finally {
                stream.close();
            }
        }
    }

    public void test_print_float() {
        for (boolean mustIgnoreEmptyLastLine : new boolean[]{false,true}) {
            final MemPrintStream stream = new MemPrintStream(mustIgnoreEmptyLastLine);
            try {
                stream.print(1.7f);
                assertEquals(1, stream.getLines().size());
                assertEquals("1.7", stream.getLines().get(0));

                stream.print(Float.MAX_VALUE);
                assertEquals(1, stream.getLines().size());
                assertEquals("1.7" + Float.MAX_VALUE, stream.getLines().get(0));
            } finally {
                stream.close();
            }
        }
    }

    public void test_print_double() {
        for (boolean mustIgnoreEmptyLastLine : new boolean[]{false,true}) {
            final MemPrintStream stream = new MemPrintStream(mustIgnoreEmptyLastLine);
            try {
                stream.print(1.7);
                assertEquals(1, stream.getLines().size());
                assertEquals("1.7", stream.getLines().get(0));

                stream.print(Double.MAX_VALUE);
                assertEquals(1, stream.getLines().size());
                assertEquals("1.7" + Double.MAX_VALUE, stream.getLines().get(0));
            } finally {
                stream.close();
            }
        }
    }

    public void test_print_Object() {
        for (boolean mustIgnoreEmptyLastLine : new boolean[]{false,true}) {
            final MemPrintStream stream = new MemPrintStream(mustIgnoreEmptyLastLine);
            try {
                final Object obj = new Object();

                stream.print(obj);
                assertEquals(1, stream.getLines().size());
                assertEquals(obj.toString(), stream.getLines().get(0));

                stream.print(obj);
                assertEquals(1, stream.getLines().size());
                assertEquals(obj.toString() + obj.toString(), stream.getLines().get(0));
            } finally {
                stream.close();
            }
        }
    }

    public void test_print_charArr() {
        for (boolean mustIgnoreEmptyLastLine : new boolean[]{false,true}) {
            final MemPrintStream stream = new MemPrintStream(mustIgnoreEmptyLastLine);
            try {
                final char[] arr = new char[]{'a', 'b'};

                stream.print(arr);
                assertEquals(1, stream.getLines().size());
                assertEquals("ab", stream.getLines().get(0));

                stream.print(arr);
                assertEquals(1, stream.getLines().size());
                assertEquals("abab", stream.getLines().get(0));
            } finally {
                stream.close();
            }
        }
    }

    public void test_print_String() {
        for (boolean mustIgnoreEmptyLastLine : new boolean[]{false,true}) {
            final MemPrintStream stream = new MemPrintStream(mustIgnoreEmptyLastLine);
            try {
                stream.print("ab");
                assertEquals(1, stream.getLines().size());
                assertEquals("ab", stream.getLines().get(0));

                stream.print("cd");
                assertEquals(1, stream.getLines().size());
                assertEquals("abcd", stream.getLines().get(0));
            } finally {
                stream.close();
            }
        }
    }

    /**
     * Tests "\n" and "\r" and "\r\n" handing.
     */
    public void test_print_String_nl_cr() {
        for (boolean mustIgnoreEmptyLastLine : new boolean[]{false,true}) {
            final MemPrintStream stream = new MemPrintStream(mustIgnoreEmptyLastLine);
            try {
                stream.print("a");
                assertEquals(1, stream.getLines().size());
                assertEquals("a", stream.getLines().get(0));

                stream.print("\n");
                assertEquals(vlc(stream, 1), stream.getLines().size());
                assertEquals("a", stream.getLines().get(0));
                checkLastIsEmptyLineIfNotIgnoring(stream);

                stream.print("\r");
                assertEquals(vlc(stream, 2), stream.getLines().size());
                assertEquals("a", stream.getLines().get(0));
                assertEquals("", stream.getLines().get(1));
                checkLastIsEmptyLineIfNotIgnoring(stream);

                // Only adds one line.
                stream.print("\r\n");
                assertEquals(vlc(stream, 3), stream.getLines().size());
                assertEquals("a", stream.getLines().get(0));
                assertEquals("", stream.getLines().get(1));
                assertEquals("", stream.getLines().get(2));
                checkLastIsEmptyLineIfNotIgnoring(stream);

                // Adds two lines.
                stream.print("\n\r");
                assertEquals(vlc(stream, 5), stream.getLines().size());
                assertEquals("a", stream.getLines().get(0));
                assertEquals("", stream.getLines().get(1));
                assertEquals("", stream.getLines().get(2));
                assertEquals("", stream.getLines().get(3));
                assertEquals("", stream.getLines().get(4));
                checkLastIsEmptyLineIfNotIgnoring(stream);
            } finally {
                stream.close();
            }
        }
    }

    /*
     * 
     */

    public void test_println() {
        for (boolean mustIgnoreEmptyLastLine : new boolean[]{false,true}) {
            final MemPrintStream stream = new MemPrintStream(mustIgnoreEmptyLastLine);
            try {
                assertEquals(0, stream.getLines().size());

                stream.println();
                assertEquals(1, stream.getLines().size());
                assertEquals("", stream.getLines().get(0));

                stream.println();
                assertEquals(2, stream.getLines().size());
                assertEquals("", stream.getLines().get(0));
                assertEquals("", stream.getLines().get(1));

                stream.print("a");
                assertEquals(3, stream.getLines().size());
                assertEquals("", stream.getLines().get(0));
                assertEquals("", stream.getLines().get(1));
                assertEquals("a", stream.getLines().get(2));

                stream.println();
                assertEquals(vlc(stream, 3), stream.getLines().size());
                assertEquals("", stream.getLines().get(0));
                assertEquals("", stream.getLines().get(1));
                assertEquals("a", stream.getLines().get(2));
                checkLastIsEmptyLineIfNotIgnoring(stream);

                stream.print("a");
                assertEquals(4, stream.getLines().size());
                assertEquals("", stream.getLines().get(0));
                assertEquals("", stream.getLines().get(1));
                assertEquals("a", stream.getLines().get(2));
                assertEquals("a", stream.getLines().get(3));

                stream.print("a");
                assertEquals(4, stream.getLines().size());
                assertEquals("", stream.getLines().get(0));
                assertEquals("", stream.getLines().get(1));
                assertEquals("a", stream.getLines().get(2));
                assertEquals("aa", stream.getLines().get(3));
            } finally {
                stream.close();
            }
        }
    }

    public void test_println_boolean() {
        for (boolean mustIgnoreEmptyLastLine : new boolean[]{false,true}) {
            final MemPrintStream stream = new MemPrintStream(mustIgnoreEmptyLastLine);
            try {
                stream.println(false);
                assertEquals(vlc(stream, 1), stream.getLines().size());
                assertEquals("false", stream.getLines().get(0));
                checkLastIsEmptyLineIfNotIgnoring(stream);

                stream.println(true);
                assertEquals(vlc(stream, 2), stream.getLines().size());
                assertEquals("false", stream.getLines().get(0));
                assertEquals("true", stream.getLines().get(1));
                checkLastIsEmptyLineIfNotIgnoring(stream);
            } finally {
                stream.close();
            }
        }
    }

    public void test_println_char() {
        for (boolean mustIgnoreEmptyLastLine : new boolean[]{false,true}) {
            final MemPrintStream stream = new MemPrintStream(mustIgnoreEmptyLastLine);
            try {
                stream.println((char) 'a');
                assertEquals(vlc(stream, 1), stream.getLines().size());
                assertEquals("a", stream.getLines().get(0));
                checkLastIsEmptyLineIfNotIgnoring(stream);

                stream.println((char) 'b');
                assertEquals(vlc(stream, 2), stream.getLines().size());
                assertEquals("a", stream.getLines().get(0));
                assertEquals("b", stream.getLines().get(1));
                checkLastIsEmptyLineIfNotIgnoring(stream);
            } finally {
                stream.close();
            }
        }
    }

    public void test_println_int() {
        for (boolean mustIgnoreEmptyLastLine : new boolean[]{false,true}) {
            final MemPrintStream stream = new MemPrintStream(mustIgnoreEmptyLastLine);
            try {
                stream.println(17);
                assertEquals(vlc(stream, 1), stream.getLines().size());
                assertEquals("17", stream.getLines().get(0));
                checkLastIsEmptyLineIfNotIgnoring(stream);

                stream.println(Integer.MAX_VALUE);
                assertEquals(vlc(stream, 2), stream.getLines().size());
                assertEquals("17", stream.getLines().get(0));
                assertEquals("" + Integer.MAX_VALUE, stream.getLines().get(1));
                checkLastIsEmptyLineIfNotIgnoring(stream);
            } finally {
                stream.close();
            }
        }
    }

    public void test_println_long() {
        for (boolean mustIgnoreEmptyLastLine : new boolean[]{false,true}) {
            final MemPrintStream stream = new MemPrintStream(mustIgnoreEmptyLastLine);
            try {
                stream.println(17L);
                assertEquals(vlc(stream, 1), stream.getLines().size());
                assertEquals("17", stream.getLines().get(0));
                checkLastIsEmptyLineIfNotIgnoring(stream);

                stream.println(Long.MAX_VALUE);
                assertEquals(vlc(stream, 2), stream.getLines().size());
                assertEquals("17", stream.getLines().get(0));
                assertEquals("" + Long.MAX_VALUE, stream.getLines().get(1));
                checkLastIsEmptyLineIfNotIgnoring(stream);
            } finally {
                stream.close();
            }
        }
    }

    public void test_println_float() {
        for (boolean mustIgnoreEmptyLastLine : new boolean[]{false,true}) {
            final MemPrintStream stream = new MemPrintStream(mustIgnoreEmptyLastLine);
            try {
                stream.println(1.7f);
                assertEquals(vlc(stream, 1), stream.getLines().size());
                assertEquals("1.7", stream.getLines().get(0));
                checkLastIsEmptyLineIfNotIgnoring(stream);

                stream.println(Float.MAX_VALUE);
                assertEquals(vlc(stream, 2), stream.getLines().size());
                assertEquals("1.7", stream.getLines().get(0));
                assertEquals("" + Float.MAX_VALUE, stream.getLines().get(1));
                checkLastIsEmptyLineIfNotIgnoring(stream);
            } finally {
                stream.close();
            }
        }
    }

    public void test_println_double() {
        for (boolean mustIgnoreEmptyLastLine : new boolean[]{false,true}) {
            final MemPrintStream stream = new MemPrintStream(mustIgnoreEmptyLastLine);
            try {
                stream.println(1.7);
                assertEquals(vlc(stream, 1), stream.getLines().size());
                assertEquals("1.7", stream.getLines().get(0));
                checkLastIsEmptyLineIfNotIgnoring(stream);

                stream.println(Double.MAX_VALUE);
                assertEquals(vlc(stream, 2), stream.getLines().size());
                assertEquals("1.7", stream.getLines().get(0));
                assertEquals("" + Double.MAX_VALUE, stream.getLines().get(1));
                checkLastIsEmptyLineIfNotIgnoring(stream);
            } finally {
                stream.close();
            }
        }
    }

    public void test_println_Object() {
        for (boolean mustIgnoreEmptyLastLine : new boolean[]{false,true}) {
            final MemPrintStream stream = new MemPrintStream(mustIgnoreEmptyLastLine);
            try {
                final Object obj = new Object();

                stream.println(obj);
                assertEquals(vlc(stream, 1), stream.getLines().size());
                assertEquals(obj.toString(), stream.getLines().get(0));
                checkLastIsEmptyLineIfNotIgnoring(stream);

                stream.println(obj);
                assertEquals(vlc(stream, 2), stream.getLines().size());
                assertEquals(obj.toString(), stream.getLines().get(0));
                assertEquals(obj.toString(), stream.getLines().get(1));
                checkLastIsEmptyLineIfNotIgnoring(stream);
            } finally {
                stream.close();
            }
        }
    }

    public void test_println_charArr() {
        for (boolean mustIgnoreEmptyLastLine : new boolean[]{false,true}) {
            final MemPrintStream stream = new MemPrintStream(mustIgnoreEmptyLastLine);
            try {
                final char[] arr = new char[]{'a', 'b'};

                stream.println(arr);
                assertEquals(vlc(stream, 1), stream.getLines().size());
                assertEquals("ab", stream.getLines().get(0));
                checkLastIsEmptyLineIfNotIgnoring(stream);

                stream.println(arr);
                assertEquals(vlc(stream, 2), stream.getLines().size());
                assertEquals("ab", stream.getLines().get(0));
                assertEquals("ab", stream.getLines().get(1));
                checkLastIsEmptyLineIfNotIgnoring(stream);
            } finally {
                stream.close();
            }
        }
    }

    public void test_println_String() {
        for (boolean mustIgnoreEmptyLastLine : new boolean[]{false,true}) {
            final MemPrintStream stream = new MemPrintStream(mustIgnoreEmptyLastLine);
            try {
                stream.println("ab");
                assertEquals(vlc(stream, 1), stream.getLines().size());
                assertEquals("ab", stream.getLines().get(0));
                checkLastIsEmptyLineIfNotIgnoring(stream);

                stream.println("cd");
                assertEquals(vlc(stream, 2), stream.getLines().size());
                assertEquals("ab", stream.getLines().get(0));
                assertEquals("cd", stream.getLines().get(1));
                checkLastIsEmptyLineIfNotIgnoring(stream);
            } finally {
                stream.close();
            }
        }
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------

    /**
     * Return varying line count.
     * 
     * @param stream The stream.
     * @param lineCount A number of lines.
     * @return The specified number of lines if ignoring empty last lines,
     *         else its value plus one.
     */
    private int vlc(MemPrintStream stream, int lineCount) {
        if (stream.getMustIgnoreEmptyLastLine()) {
            return lineCount;
        } else {
            return lineCount + 1;
        }
    }

    private void checkLastIsEmptyLineIfNotIgnoring(MemPrintStream stream) {
        if (!stream.getMustIgnoreEmptyLastLine()) {
            final List<String> lines = stream.getLines();
            assertEquals("", lines.get(lines.size() - 1));
        }
    }
}
