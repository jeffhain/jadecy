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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Ensures that the first specified runnable has been ran at least once
 * since creation of this instance.
 * Blocks until first specified runnable completed.
 */
public class RunOnceHelper {

    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------

    private static final boolean DEBUG = false;

    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------

    private static final int FLAG_PENDING = 0;
    private static final int FLAG_STARTED = 1;
    private static final int FLAG_DONE = 2;
    
    private final Runnable runnable;
    
    private final AtomicInteger ranFlag = new AtomicInteger(FLAG_PENDING);
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /**
     * @param runnable Runnable to run.
     * @throws NullPointerException if the specified runnable is null.
     */
    public RunOnceHelper(Runnable runnable) {
        if (runnable == null) {
            throw new NullPointerException();
        }
        this.runnable = runnable;
    }

    public void ensureRanOnceSync() {
        if (ranFlag.compareAndSet(FLAG_PENDING, FLAG_STARTED)) {
            // Doing it synchronously.
            if (DEBUG) {
                System.out.println("runnable started at " + System.currentTimeMillis() + " ms");
            }
            runnable.run();
            if (DEBUG) {
                System.out.println("runnable done at " + System.currentTimeMillis() + " ms");
            }
            ranFlag.set(FLAG_DONE);
            // Notifying eventual waiters.
            synchronized (ranFlag) {
                ranFlag.notifyAll();
            }
        } else {
            if (DEBUG) {
                System.out.println("runnable : already started...");
            }
            // Waiting for it to be done, if not done already.
            synchronized (ranFlag) {
                while (ranFlag.get() != FLAG_DONE) {
                    try {
                        ranFlag.wait();
                    } catch (InterruptedException e) {
                        // Bad, can't ensure our promise.
                        throw new RuntimeException(e);
                    }
                }
            }
            if (DEBUG) {
                System.out.println("runnable : ...and already done");
            }
        }
    }
}
