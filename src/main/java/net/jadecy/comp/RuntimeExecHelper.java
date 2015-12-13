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
package net.jadecy.comp;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Helper for usage of Runtime.exec(...).
 */
public class RuntimeExecHelper {

    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------

    private static final boolean DEBUG = false;
    
    //--------------------------------------------------------------------------
    // PUBLIC CLASSES
    //--------------------------------------------------------------------------

    public interface InterfaceExecution {
        public InterfaceStreamReader getOutReader();
        public InterfaceStreamReader getErrReader();
        public int waitFor() throws InterruptedException;
        public int exitValue();
    }
    
    public interface InterfaceStreamReader {
        /**
         * Allowed to wait for stream termination, without waiting for the
         * whole subprocess termination.
         * 
         * @return True if reader started and then completed, false otherwise.
         */
        public boolean isDone();
        /**
         * Interrupts the thread running the reader, if any.
         */
        public void interruptRunner();
    }
    
    //--------------------------------------------------------------------------
    // PRIVATE CLASSES
    //--------------------------------------------------------------------------

    private static class MyStreamReader implements Runnable, InterfaceStreamReader {
        private final InputStream is;
        private final PrintStream ps;
        private final Object runnerNullificationMutex = new Object();
        private volatile Thread runner = null;
        private volatile boolean done = false;
        /**
         * @param is Must not be null.
         * @param ps Can be null.
         */
        public MyStreamReader(
                InputStream is,
                PrintStream ps) {
            this.is = is;
            this.ps = ps;
        }
        public void run() {
            if (this.runner != null) {
                // Guarding against concurrent calls.
                throw new IllegalStateException("still running");
            }
            final Thread runner = Thread.currentThread();
            this.runner = runner;
            if (DEBUG) {
                System.out.println("running with " + runner);
            }
            try {
                final byte[] chunkArr = new byte[STREAM_CHUNK_SIZE];

                boolean isEnd;

                do {
                    isEnd = false;

                    if (DEBUG) {
                        System.out.println("reading stream...");
                    }
                    isEnd = readSome(is, ps, chunkArr);

                } while (!isEnd);

                if (DEBUG) {
                    System.out.println("completed normally");
                }
            } catch (IOException e) {
                if (DEBUG) {
                    System.out.println("completed exceptionally");
                }
                throw new RuntimeException(e);
            } finally {
                synchronized (this.runnerNullificationMutex) {
                    // Clearing eventual interruption status, that could
                    // have been set by call to interruptRunner(),
                    // and is no longer needed since reader is now completing.
                    // Note: This might clear an interrupt done by external
                    // treatments. try/catch to make sure we go further.
                    try {
                        Thread.interrupted();
                    } catch (SecurityException se) {
                        // quiet
                    }
                    this.runner = null;
                }
                try {
                    this.is.close();
                } catch (IOException e) {
                    // quiet
                } finally {
                    this.done = true;
                }
            }
        }
        //@Override
        public boolean isDone() {
            return this.done;
        }
        //@Override
        public void interruptRunner() {
            synchronized (this.runnerNullificationMutex) {
                final Thread runner = this.runner;
                if (runner != null) {
                    if (DEBUG) {
                        System.out.println("interrupting " + runner);
                    }
                    // Since we are in runnerNullificationMutex,
                    // we are sure we don't interrupt thread
                    // after it exited run method.
                    try {
                        runner.interrupt();
                    } catch (SecurityException e) {
                        // quiet
                    }
                }
            }
        }
    }
    
    private static class MyExecution implements InterfaceExecution {
        private final Process process;
        private final InterfaceStreamReader outReader;
        private final InterfaceStreamReader errReader;
        public MyExecution(
                Process process,
                InterfaceStreamReader outReader,
                InterfaceStreamReader errReader) {
            this.process = process;
            this.outReader = outReader;
            this.errReader = errReader;
        }
        //@Override
        public InterfaceStreamReader getOutReader() {
            return this.outReader;
        }
        //@Override
        public InterfaceStreamReader getErrReader() {
            return this.errReader;
        }
        /**
         * Causes the current thread to wait, if necessary, until the process
         * has terminated.
         * This method returns immediately if the subprocess has already
         * terminated.
         *
         * @return The exit value of the subprocess. By convention, zero
         *         indicates normal termination.
         * @throws InterruptedException if the wait is interrupted.
         */
        public int waitFor() throws InterruptedException {
            return this.process.waitFor();
        }
        /**
         * Returns the exit value for the subprocess.
         *
         * @return The exit value of the subprocess.
         * @throws IllegalThreadStateException if the subprocess has not yet
         *         terminated.
         */
        public int exitValue() {
            return this.process.exitValue();
        }
    }
    
    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------

    /**
     * Zero keep alive time so that VM shuts down quickly after run.
     */
    private static final Executor DEFAULT_EXECUTOR = new ThreadPoolExecutor(
            0,
            Integer.MAX_VALUE,
            0L,
            TimeUnit.MICROSECONDS,
            new SynchronousQueue<Runnable>());
    
    private static final int STREAM_CHUNK_SIZE = 1024 * 8;
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /**
     * Uses System.out and System.err.
     * 
     * @param cmd Command to execute.
     */
    public static InterfaceExecution execAsync(String cmd) {
        return execAsync(
                cmd,
                System.out,
                System.err);
    }
    
    public static InterfaceExecution execAsync(
            String cmd,
            PrintStream outStream,
            PrintStream errStream) {
        return execAsync(
                cmd,
                outStream,
                errStream,
                DEFAULT_EXECUTOR);
    }

    /**
     * @param cmd Command to execute.
     * @param outStream Can be null, in which case output stream
     *        is read but not printed.
     * @param errStream Can be null, in which case error stream
     *        is read but not printed.
     * @param executor Executor to run streams readers in.
     *        Must provide at least as many threads as read streams.
     */
    public static InterfaceExecution execAsync(
            String cmd,
            PrintStream outStream,
            PrintStream errStream,
            Executor executor) {
        
        if (DEBUG) {
            System.out.println("cmd = " + cmd);
        }
        
        final Process process;
        try {
            process = Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        final InputStream processOut = process.getInputStream();
        final InputStream processErr = process.getErrorStream();
        
        final MyStreamReader outReader = new MyStreamReader(
                processOut,
                outStream);
        executor.execute(outReader);
        
        final MyStreamReader errReader = new MyStreamReader(
                processErr,
                errStream);
        executor.execute(errReader);
        
        return new MyExecution(
                process,
                outReader,
                errReader);
    }
    
    /*
     * Convenience methods.
     */
    
    /**
     * Can be interrupted, in which case interrupt status is set.
     * 
     * @return The exist value (possible Integer.MIN_VALUE),
     *         or Integer.MIN_VALUE if the wait has been interrupted.
     */
    public static int execSyncNoIE(String cmd, PrintStream stream) {
        return waitForNoIE(execAsync(cmd, stream, stream));
    }
    
    /**
     * Can be interrupted, in which case interrupt status is set.
     * 
     * @return The exist value (possible Integer.MIN_VALUE),
     *         or Integer.MIN_VALUE if the wait has been interrupted.
     */
    public static int waitForNoIE(InterfaceExecution execution) {
        int value = Integer.MIN_VALUE;
        try {
            value = execution.waitFor();
        } catch (InterruptedException e) {
            // OK we stop.
            Thread.currentThread().interrupt();
        }
        return value;
    }
    
    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------

    private RuntimeExecHelper() {
    }

    /**
     * @return True if read did return a value < 0, false otherwise.
     */
    private static boolean readSome(
            InputStream is,
            PrintStream ps,
            byte[] chunkArr) throws IOException {
        final int ret = is.read(chunkArr, 0, chunkArr.length);
        if (DEBUG) {
            System.out.println("ret = " + ret);
        }
        if ((ret > 0) && (ps != null)) {
            final String string = new String(chunkArr, 0, ret);
            if (DEBUG) {
                System.out.println("string = " + string);
            }
            ps.print(string);
        }
        return (ret < 0);
    }
}
