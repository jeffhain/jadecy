/*
 * Copyright 2015-2016 Jeff Hain
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

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import net.jadecy.ElemType;
import net.jadecy.InterfaceCycleProcessor;
import net.jadecy.JadecyUtils;

class JdcmCycleProcessor implements InterfaceCycleProcessor {

    //--------------------------------------------------------------------------
    // PRIVATE CLASSES
    //--------------------------------------------------------------------------

    /**
     * Ordered by increasing count, and for equal counts by increasing name.
     */
    private static class MyEntry implements Comparable<MyEntry> {
        final String name;
        final long count;
        public MyEntry(
                String name,
                long count) {
            this.name = name;
            this.count = count;
        }
        //@Override
        public int compareTo(MyEntry other) {
            if (other == this) {
                return 0;
            }
            {
                final long cmp = this.count - other.count;
                if (cmp != 0) {
                    return (cmp < 0) ? -1 : 1;
                }
            }
            {
                final int cmp = this.name.compareTo(other.name);
                if (cmp != 0) {
                    return cmp;
                }
            }
            // Compare to same, but is not this: tolerating that case.
            return 0;
        }
    }
    
    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------
    
    private final ElemType elemType;
    
    private final boolean mustPrintOnProcess;
    
    private final boolean mustPrintCauses;
    
    private final boolean mustUseDotFormat;
    
    private final PrintStream stream;
    
    private final TreeMap<Integer,Long> countBySize = new TreeMap<Integer,Long>();

    /**
     * Not using an identity hash map, in case this class would be used with
     * a cycles computer not ensuring a single String instance per package name.
     */
    private final HashMap<String,Long> countByElemName = new HashMap<String,Long>();

    private final HashMap<String,Long> countByCauseName = new HashMap<String,Long>();

    private final int minSize;
    
    /**
     * No limit if < 0.
     */
    private final long maxCount;
    
    private long count = 0;
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /**
     * @param minSize Cycles of inferior size are ignored.
     * @param maxCount Max number of cycles to process. If < 0, no limit.
     */
    public JdcmCycleProcessor(
            ElemType elemType,
            boolean mustPrintOnProcess,
            boolean mustPrintCauses,
            boolean mustUseDotFormat,
            PrintStream stream,
            int minSize,
            long maxCount) {
        this.elemType = elemType;
        this.mustPrintOnProcess = mustPrintOnProcess;
        this.mustPrintCauses = mustPrintCauses;
        this.mustUseDotFormat = mustUseDotFormat;
        this.stream = stream;
        this.minSize = minSize;
        this.maxCount = maxCount;
    }
    
    /**
     * @return The number of cycles processed.
     */
    public long getCount() {
        return this.count;
    }

    //@Override
    public boolean processCycle(
            String[] names,
            String[][] causesArr) {
        if (this.maxCount == 0) {
            return true;
        }
        if (names.length < this.minSize) {
            return false;
        }
        this.count++;
        increment(this.countBySize, names.length);
        for (int i = 0; i < names.length; i++) {
            final String name = names[i];
            increment(this.countByElemName, name);
            
            if (causesArr != null) {
                final String[] causes = causesArr[i];
                for (String cause : causes) {
                    increment(this.countByCauseName, cause);
                }
            }
        }
        if (this.mustPrintOnProcess) {
            if (this.mustUseDotFormat) {
                JadecyUtils.printGraphInDOTFormatMs(
                        JadecyUtils.computeGraphFromCycle(names),
                        "cycle_" + this.count,
                        stream);
            } else {
                this.stream.println();
                this.stream.println("CYCLE " + this.count + ":");
                JadecyUtils.printCycle(
                        names,
                        (this.mustPrintCauses ? causesArr : null),
                        this.stream);
            }
        }
        return (this.maxCount >= 0) && (this.count >= this.maxCount);
    }
    
    public void printStats() {
        if (this.countByCauseName.size() != 0) {
            this.stream.println();
            this.stream.println("number of cycles by cause name:");
            {
                final TreeSet<MyEntry> sortedEntrySet = new TreeSet<MyEntry>();
                for (Map.Entry<String,Long> entry : this.countByCauseName.entrySet()) {
                    sortedEntrySet.add(new MyEntry(entry.getKey(), entry.getValue().longValue()));
                }
                for (MyEntry entry : sortedEntrySet) {
                    this.stream.println(entry.name + ": " + entry.count);
                }
            }
        }
        
        /*
         * 
         */
        
        this.stream.println();
        this.stream.println("number of cycles by " + this.elemType.toStringSingularLC() + " name:");
        {
            final TreeSet<MyEntry> sortedEntrySet = new TreeSet<MyEntry>();
            for (Map.Entry<String,Long> entry : this.countByElemName.entrySet()) {
                sortedEntrySet.add(new MyEntry(entry.getKey(), entry.getValue().longValue()));
            }
            for (MyEntry entry : sortedEntrySet) {
                this.stream.println(entry.name + ": " + entry.count);
            }
        }
        
        /*
         * 
         */
        
        this.stream.println();
        this.stream.println("number of cycles by size:");
        for (Map.Entry<Integer, Long> entry : this.countBySize.entrySet()) {
            this.stream.println(entry.getKey() + " : " + entry.getValue());
        }
        
        /*
         * 
         */
        
        this.stream.println();
        this.stream.println("number of cycles found: " + this.count);
    }
    
    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------

    private static <K> void increment(
            Map<K, Long> countByKey,
            K key) {
        Long prev = countByKey.get(key);
        if (prev == null) {
            countByKey.put(key, 1L);
        } else {
            countByKey.put(key, prev + 1L);
        }
    }
}
