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
package net.jadecy.cmd;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 * Utilities for JadecyMain and associated classes.
 */
class JdcmUtils {

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    /**
     * Retains elements matching toKeepRegex in the specified map,
     * except if the also match exceptRegex.
     * 
     * @param map (in,out)
     * @param toKeepRegex Can be null, in which case nothing is removed.
     * @param exceptRegex Can be null, in which case there is no exception.
     */
    public static void keepOnlyMatchesInMap(
            SortedMap<String,Long> map,
            String toKeepRegex,
            String exceptRegex) {
        if (toKeepRegex == null) {
            return;
        }
        final Iterator<String> it = map.keySet().iterator();
        while (it.hasNext()) {
            final String name = it.next();
            if ((!name.matches(toKeepRegex))
                    && ((exceptRegex == null)
                            || (!name.matches(exceptRegex)))) {
                it.remove();
            }
        }
    }

    /**
     * Retains elements matching toKeepRegex in maps of the specified list,
     * except if the also match exceptRegex.
     * 
     * @param mapList (in,out)
     * @param toKeepRegex Can be null, in which case nothing is removed.
     * @param exceptRegex Can be null, in which case there is no exception.
     */
    public static void keepOnlyMatchesInMapList(
            List<SortedMap<String,Long>> mapList,
            String toKeepRegex,
            String exceptRegex) {
        if (toKeepRegex == null) {
            return;
        }
        for (SortedMap<String,Long> fromMap : mapList) {
            keepOnlyMatchesInMap(
                    fromMap,
                    toKeepRegex,
                    exceptRegex);
        }
    }

    /**
     * Removes from the maps of the specified list,
     * the keys of toRemoveSet, except if they match exceptRegex.
     * 
     * @param exceptRegex Can be null, in which case there is no exception.
     */
    public static void removeSetFromBulkLm(
            Set<String> toRemoveSet,
            List<SortedMap<String,Long>> byteSizeByNameList,
            String exceptRegex) {
        for (String key : toRemoveSet) {
            if ((exceptRegex == null) || (!key.matches(exceptRegex))) {
                for (SortedMap<String,Long> byteSizeByName : byteSizeByNameList) {
                    byteSizeByName.remove(key);
                }
            }
        }
    }

    /**
     * Removes from the maps of the specified list,
     * the keys of toRemoveSet, except if they match exceptRegex.
     * 
     * @param exceptRegex Can be null, in which case there is no exception.
     */
    public static void removeSetFromGraphLmml(
            Set<String> toRemoveSet,
            List<SortedMap<String,SortedMap<String,SortedSet<String>>>> causesByDepByNameList,
            String exceptRegex) {
        for (String key : toRemoveSet) {
            if ((exceptRegex == null) || (!key.matches(exceptRegex))) {
                for (SortedMap<String,SortedMap<String,SortedSet<String>>> causesByDepByName : causesByDepByNameList) {
                    causesByDepByName.remove(key);
                }
            }
        }
    }
    
    /**
     * Attempts to create a new empty file corresponding to the specified path.
     * If the file already exists, attempts to delete it, without right change.
     * If parent directories don't exist, attempts to create them.
     * Does not try anything else (like deleting a directory that would have
     * the same name, etc.).
     * 
     * @param stream Stream to report errors.
     * @return The newly created empty file, or null if could not create it.
     */
    public static File newEmptyFile(
            String filePath,
            PrintStream stream) {
        final File file = new File(filePath);
        if (file.exists()) {
            // File exists: attempting to delete it if it's not a directory.
            if (file.isDirectory()) {
                stream.println("ERROR: file already exists as a directory: " + file.getAbsolutePath());
                return null;
            }
            // TODO From Java 6 could abort if file not writable
            // (which is a weak guard on some OSes, i.e. does not prevent
            // the deletion that we try here).
            final boolean didIt = file.delete();
            if (!didIt) {
                stream.println("ERROR: could not delete file: " + file.getAbsolutePath());
                return null;
            }
        } else {
            // File does not exist: attempting to ensure parent directory.
            final File parentDir = file.getParentFile();
            if (parentDir != null) {
                if (!parentDir.exists()) {
                    final boolean didIt = parentDir.mkdirs();
                    if (!didIt) {
                        stream.println("ERROR: could not create parent directory: " + parentDir.getAbsolutePath());
                        return null;
                    }
                } else if (!parentDir.isDirectory()) {
                    stream.println("ERROR: parent directory already exists not as a directory: " + parentDir.getAbsolutePath());
                    return null;
                }
            }
        }
        
        try {
            final boolean didCreate = file.createNewFile();
            if (!didCreate) {
                stream.println("ERROR: could not create file: " + file.getAbsolutePath());
                return null;
            } else {
                return file;
            }
        } catch (IOException e) {
            stream.println("ERROR: could not create file " + file.getAbsolutePath() + "(" + e.getMessage() + ")");
            return null;
        }
    }
}
