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
package net.jadecy.comp;

import java.io.File;
import java.io.IOException;

public class JdcFsUtils {

    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------

    private static final boolean DEBUG = false;

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    /**
     * Deletes all files in the specified directory, recursively.
     * If the specified file is not a directory, does nothing.
     * 
     * @param dir Directory to clear. The directory itself is not deleted.
     */
    public static void clearDir(File dir) {
        final String dirPath = dir.getAbsolutePath();
        if (DEBUG) {
            System.out.println("clearDir(" + dirPath + ")");
        }
        final String[] childList = dir.list();
        if (childList == null) {
            return;
        }
        for (String childName : childList) {
            final File child = new File(dirPath + "/" + childName);
            if (child.isDirectory()) {
                clearDir(child);
            }
            final boolean didIt = child.delete();
            if (!didIt) {
                throw new RuntimeException("could not delete " + child.getAbsolutePath());
            }
        }
    }

    /**
     * Can attempt to create parent directories,
     * but does not try more brutal actions (such as deleting a file).
     * 
     * @param dir Directory to ensure exists.
     * @throws RuntimeException if could not ensure directory existence.
     */
    public static void ensureDir(File dir) {
        final String dirPath = dir.getAbsolutePath();
        if (DEBUG) {
            System.out.println("ensureEmptyDir(" + dirPath + ")");
        }
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                throw new RuntimeException("directory to ensure already exists as a file: " + dir.getAbsolutePath());
            }
        } else {
            final boolean didIt = dir.mkdirs();
            if (!didIt) {
                throw new RuntimeException("could not create directory: " + dirPath);
            }
        }
    }

    /**
     * Can attempt to create parent directories,
     * but does not try more brutal actions (such as deleting a file).
     * 
     * @param dir Directory to ensure exists and is empty.
     * @throws RuntimeException if could not ensure directory existence and emptiness.
     */
    public static void ensureEmptyDir(File dir) {
        ensureDir(dir);
        clearDir(dir);
    }

    /**
     * Can attempt to create parent directories or delete already existing file,
     * but does not try more brutal actions (such as deleting a directory).
     * 
     * @param file File which existence and emptiness must be ensured.
     * @throws RuntimeException if could not ensure file existence and emptiness.
     */
    public static void ensureEmptyFile(File file) {
        if (DEBUG) {
            System.out.println("ensureEmptyFile(" + file.getAbsolutePath() + ")");
        }
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new RuntimeException("file to create already exists as directory: " + file.getAbsolutePath());
            }
            if (DEBUG) {
                System.out.println("deleting " + file);
            }
            final boolean didIt = file.delete();
            if (!didIt) {
                throw new RuntimeException("could not delete file: " + file.getAbsolutePath());
            }
        } else {
            // Creating parent directory if any and it doesn't exist.
            final File parentDir = file.getParentFile();
            if (parentDir != null) {
                ensureDir(parentDir);
            }
        }

        final boolean didCreate;
        try {
            didCreate = file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (!didCreate) {
            throw new RuntimeException("could not create file: " + file.getAbsolutePath());
        }
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    private JdcFsUtils() {
    }
}
