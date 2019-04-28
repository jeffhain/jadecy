/*
 * Copyright 2015-2019 Jeff Hain
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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Prints into a list of String, one per line,
 * considering '\n', '\r', and '\r' followed by '\n' (CR+LF, for Windows)
 * as line separators.
 * 
 * Overridden and added methods are thread-safe.
 * 
 * Only all of PrintStream's print(...) and println(...)
 * printing methods are supported.
 */
public class MemPrintStream extends PrintStream {
    
    /*
     * Overriding all methods to support, for they usually don't
     * delegate to each other but to some private methods.
     */

    //--------------------------------------------------------------------------
    // PRIVATE CLASSES
    //--------------------------------------------------------------------------
    
    private static class MyFakeOS extends OutputStream {
        @Override
        public void write(int b) throws IOException {
            // Should never get there if only overridden methods are used.
            throw new UnsupportedOperationException();
        }
    }
    
    /**
     * To avoid creating a String for each char array.
     */
    private static class MyCharSeq implements CharSequence {
        private final char[] array;
        public MyCharSeq(char[] array) {
            this.array = array;
        }
        //@Override
        public CharSequence subSequence(int start, int end) {
            throw new UnsupportedOperationException();
        }
        //@Override
        public int length() {
            return this.array.length;
        }
        //@Override
        public char charAt(int index) {
            return this.array[index];
        }
    };
    
    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------

    /**
     * True not to pollute getLines() result with this pesky empty last line.
     */
    private final boolean mustIgnoreEmptyLastLine;
    
    /**
     * Guards all internal states.
     */
    private final Object mutex = new Object();
    
    private final ArrayList<String> lineList = new ArrayList<String>();
    
    /**
     * Possibly null.
     * 
     * Invariant: never empty (nulled if so).
     */
    private StringBuilder currentLine;
    
    /*
     * temps
     */

    private final StringBuilder tmpCurrentLine = new StringBuilder();
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    /**
     * Creates an instance that ignores empty last lines.
     */
    public MemPrintStream() {
        this(true);
    }

    /**
     * @param mustIgnoreEmptyLastLine True if empty last line must not appear
     *        when retrieving lines (as with getLines()).
     */
    public MemPrintStream(boolean mustIgnoreEmptyLastLine) {
        super(new MyFakeOS(), true);
        this.mustIgnoreEmptyLastLine = mustIgnoreEmptyLastLine;
    }

    /**
     * @return True if eventual empty last line does not appear when retrieving
     *         lines, false otherwise.
     */
    public boolean getMustIgnoreEmptyLastLine() {
        return this.mustIgnoreEmptyLastLine;
    }
    
    /**
     * Removes any already printed character.
     */
    public void clear() {
        synchronized (this.mutex) {
            this.lineList.clear();
            this.currentLine = null;
        }
    }
    
    /**
     * The returned lines contain neither '\n' nor '\r',
     * which are automatically replaced with line separations.
     * 
     * @return A new mutable copy of the printed lines.
     */
    public List<String> getLines() {
        synchronized (this.mutex) {
            @SuppressWarnings("unchecked")
            final List<String> result = (List<String>) this.lineList.clone();
            if (this.currentLine != null) {
                result.add(this.currentLine.toString());
            }
            return result;
        }
    }
    
    /*
     * Methods that do not terminate lines.
     */

    @Override
    public void print(boolean x) {
        this.print_charSeq(String.valueOf(x));
    }

    @Override
    public void print(char x) {
        this.print_charSeq(String.valueOf(x));
    }

    @Override
    public void print(int x) {
        this.print_charSeq(String.valueOf(x));
    }

    @Override
    public void print(long x) {
        this.print_charSeq(String.valueOf(x));
    }

    @Override
    public void print(float x) {
        this.print_charSeq(String.valueOf(x));
    }

    @Override
    public void print(double x) {
        this.print_charSeq(String.valueOf(x));
    }

    @Override
    public void print(Object x) {
        this.print_charSeq(String.valueOf(x));
    }

    @Override
    public void print(char[] x) {
        this.print_charSeq(new MyCharSeq(x));
    }

    @Override
    public void print(String x) {
        this.print_charSeq(x);
    }
    
    /*
     * Methods that do terminate lines.
     */

    @Override
    public void println() {
        synchronized (this.mutex) {
            this.println_unsync();
        }
    }

    @Override
    public void println(boolean x) {
        this.println_charSeq(String.valueOf(x));
    }

    @Override
    public void println(char x) {
        this.println_charSeq(String.valueOf(x));
    }

    @Override
    public void println(int x) {
        this.println_charSeq(String.valueOf(x));
    }

    @Override
    public void println(long x) {
        this.println_charSeq(String.valueOf(x));
    }

    @Override
    public void println(float x) {
        this.println_charSeq(String.valueOf(x));
    }

    @Override
    public void println(double x) {
        this.println_charSeq(String.valueOf(x));
    }

    @Override
    public void println(Object x) {
        this.println_charSeq(String.valueOf(x));
    }

    @Override
    public void println(char[] x) {
        this.println_charSeq(new MyCharSeq(x));
    }

    @Override
    public void println(String x) {
        this.println_charSeq(x);
    }

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    private void print_charSeq(CharSequence seq) {
        synchronized (this.mutex) {
            this.print_unsync(seq);
        }
    }
    
    private void println_charSeq(CharSequence seq) {
        // One synchronized block to avoid interleaving.
        synchronized (this.mutex) {
            this.print_unsync(seq);
            this.println_unsync();
        }
    }
    
    /*
     * 
     */
    
    private void println_unsync() {
        if (this.currentLine != null) {
            this.lineList.add(this.currentLine.toString());
            if (this.mustIgnoreEmptyLastLine) {
                this.currentLine = null;
            } else {
                this.clearCurrentLine();
            }
        } else {
            // No current line being built:
            // directly adding the empty line.
            this.lineList.add("");
        }
    }
    
    private void print_unsync(CharSequence seq) {
        for (int i = 0; i < seq.length(); i++) {
            final char c = seq.charAt(i);
            if (c == '\n') {
                this.println_unsync();
            } else if (c == '\r') {
                if ((i < seq.length() - 1)
                        && (seq.charAt(i+1) == '\n')) {
                    // CR+LF: eating LF not to cause two new lines.
                    i++;
                }
                this.println_unsync();
            } else {
                if (this.currentLine == null) {
                    this.initCurrentLine();
                }
                this.currentLine.append(c);
            }
        }
    }
    
    /*
     * 
     */
    
    private void initCurrentLine() {
        this.currentLine = this.tmpCurrentLine;
        this.clearCurrentLine();
    }
    
    private void clearCurrentLine() {
        this.currentLine.setLength(0);
    }
}
