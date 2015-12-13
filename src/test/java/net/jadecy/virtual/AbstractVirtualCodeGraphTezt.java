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
package net.jadecy.virtual;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import junit.framework.TestCase;
import net.jadecy.code.ClassData;
import net.jadecy.code.CodeDataUtils;
import net.jadecy.code.InterfaceNameFilter;
import net.jadecy.code.NameFilters;
import net.jadecy.code.PackageData;
import net.jadecy.graph.InterfaceVertex;
import net.jadecy.parsing.InterfaceDepsParser;
import net.jadecy.parsing.InterfaceDepsParserFactory;
import net.jadecy.parsing.ParsingFilters;
import net.jadecy.tests.PrintTestUtils;
import net.jadecy.utils.MemPrintStream;

/**
 * Abstract class for tests using the following virtual code dependency graph:
 * 
 *    2---------->5
 *   ^ ^         / \
 *  /   \       /   v
 * 1     \     /     7
 *  \     \   /     ^
 *   v     \ v     v
 *    3---->4<--->6
 * p1: {1,2,3,4}
 * p2: {5,6,7}
 * 
 * Optionally, through constructor arg, adding:
 * 8<->9
 * p3: {8,9}
 * so that we can have two SCCs (whether for classes of packages),
 * of different sizes, and an SCC of size 1 (p3 alone).
 * 
 * Plus dependency of each class to java.lang.Object,
 * which is not needed since these dependencies are virtual,
 * but makes things realistic, and allows to check for dangling edges
 * removal in some cases.
 * 
 * Also, dependency from C1 to C2 is non-API (i.e. ignored if API only),
 * and dependency from C1 to C3 is through a nested class C1$CX
 * (which appears only if not merging nested classes :
 * if only API dependencies taken into account: C1 -> C1$CX -> C3
 * if non-API dependencies taken into account: C1 <-> C1$CX -> C3).
 * 
 * Virtual parsing is limited to classes which start with File.getPath()
 * for the specified file, modulo the separators.
 * As a result, using (new File("")) means any class is accepted,
 * and using (new File("foo/bar")) means only classes which name starts
 * with "foo.bar" are used.
 * 
 * Using virtual dependencies instead of dependencies parsed from actual class
 * files make tests technically lighter, but also in practice much faster since
 * it removes the compilation overhead.
 */
public abstract class AbstractVirtualCodeGraphTezt extends TestCase {

    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------

    protected static final String JLN = Object.class.getPackage().getName();
    // These packages are purely virtual (in-memory).
    protected static final String P1N = "net.jadecy.virtual.p1";
    protected static final String P2N = "net.jadecy.virtual.p2";
    protected static final String P3N = "net.jadecy.virtual.p3";

    private static final String C1CXFNNE = "C1$CX";
    
    protected static final String ObjectN = Object.class.getName();
    //
    protected static final String C1N = P1N + ".C1";
    protected static final String C1CXN = P1N + "." + C1CXFNNE;
    protected static final String C2N = P1N + ".C2";
    protected static final String C3N = P1N + ".C3";
    protected static final String C4N = P1N + ".C4";
    //
    protected static final String C5N = P2N + ".C5";
    protected static final String C6N = P2N + ".C6";
    protected static final String C7N = P2N + ".C7";
    //
    protected static final String C8N = P3N + ".C8";
    protected static final String C9N = P3N + ".C9";
    
    protected static final long C1BS_NOMERGE = 120;
    protected static final long C1CXBS = 3;
    protected static final long C1BS = C1BS_NOMERGE + C1CXBS;
    protected static final long C2BS = 234;
    protected static final long C3BS = 345;
    protected static final long C4BS = 456;
    protected static final long C5BS = 567;
    protected static final long C6BS = 678;
    protected static final long C7BS = 789;
    protected static final long C8BS = 890;
    protected static final long C9BS = 901;

    protected static final long P1BS = C1BS + C2BS + C3BS + C4BS;
    protected static final long P2BS = C5BS + C6BS + C7BS;
    protected static final long P3BS = C8BS + C9BS;

    /*
     * 
     */

    private final PackageData refDefaultP_noMerge_notApiOnly;
    private final PackageData refDefaultP_noMerge_apiOnly;
    private final PackageData refDefaultP_merge_notApiOnly;
    private final PackageData refDefaultP_merge_apiOnly;

    //--------------------------------------------------------------------------
    // PRIVATE CLASSES
    //--------------------------------------------------------------------------
    
    private class MyDepsParserFactory implements InterfaceDepsParserFactory {
        //@Override
        public InterfaceDepsParser newInstance(
                boolean mustMergeNestedClasses,
                boolean apiOnly) {
            return new MyDepsParser(
                    mustMergeNestedClasses,
                    apiOnly);
        }
    }

    private class MyDepsParser implements InterfaceDepsParser {
        
        private final boolean mustMergeNestedClasses;
        private final boolean apiOnly;
        
        private final PackageData defaultP = new PackageData();
        
        public MyDepsParser(
                boolean mustMergeNestedClasses,
                boolean apiOnly) {
            this.mustMergeNestedClasses = mustMergeNestedClasses;
            this.apiOnly = apiOnly;
        }
        //@Override
        public boolean getMustMergeNestedClasses() {
            return this.mustMergeNestedClasses;
        }
        //@Override
        public boolean getApiOnly() {
            return this.apiOnly;
        }
        //@Override
        public PackageData getDefaultPackageData() {
            return this.defaultP;
        }
        //@Override
        public boolean accumulateDependencies(
                File file,
                ParsingFilters filters) {
            
            final String dotNameRoot = file.getPath();
            
            if (dotNameRoot.equals(NON_EXISTING_FILE_PATH)) {
                throw new IllegalArgumentException(
                        "file to parse not found",
                        new FileNotFoundException());
            }
            
            // JadecyMain only uses this filter.
            final InterfaceNameFilter classNameFilter = filters.getClassNameFilter();
            
            final PackageData refDefaultP = this.getRefDefaultP();
            
            final List<InterfaceVertex> refClassDataList =
                    CodeDataUtils.newClassDataList(
                            refDefaultP,
                            NameFilters.any());
            
            final long mc1 = this.defaultP.getSubtreeModCount();
            
            for (InterfaceVertex v : refClassDataList) {
                final ClassData refClassData = (ClassData) v;
                final String className = refClassData.name();
                
                if (className.startsWith(dotNameRoot)
                        && classNameFilter.accept(className)) {
                    final ClassData classData = this.defaultP.getOrCreateClassData(className);

                    if (refClassData.byteSize() > 0) {
                        PackageData.setByteSizeForClassOrNested(
                                classData,
                                classData.fileNameNoExt(),
                                refClassData.byteSize());
                    }

                    for (ClassData refSucc : refClassData.successors()) {
                        final ClassData succ = this.defaultP.getOrCreateClassData(refSucc.name());
                        PackageData.ensureDependency(classData, succ);
                    }
                }
            }
            
            final long mc2 = this.defaultP.getSubtreeModCount();
            
            return (mc2 != mc1);
        }
        private PackageData getRefDefaultP() {
            if (this.mustMergeNestedClasses) {
                if (this.apiOnly) {
                    return refDefaultP_merge_apiOnly;
                } else {
                    return refDefaultP_merge_notApiOnly;
                }
            } else {
                if (this.apiOnly) {
                    return refDefaultP_noMerge_apiOnly;
                } else {
                    return refDefaultP_noMerge_notApiOnly;
                }
            }
        }
    }
    
    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------
    
    /**
     * Causes parser to consider that the file to parse does not exist.
     */
    public static final String NON_EXISTING_FILE_PATH = "NON_EXISTING_FILE_PATH";

    protected final InterfaceDepsParserFactory virtualDepsParserFactory =
            new MyDepsParserFactory();
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /**
     * Creates without p3.
     */
    public AbstractVirtualCodeGraphTezt() {
        this(false);
    }

    /**
     * @param withP3 If true, adds a p3 package, containing an strongly
     *        connected component
     */
    public AbstractVirtualCodeGraphTezt(boolean withP3) {
        this.refDefaultP_noMerge_notApiOnly = computeRefDefaultP(withP3, false, false);
        this.refDefaultP_noMerge_apiOnly = computeRefDefaultP(withP3, false, true);
        this.refDefaultP_merge_notApiOnly = computeRefDefaultP(withP3, true, false);
        this.refDefaultP_merge_apiOnly = computeRefDefaultP(withP3, true, true);
    }
    
    //--------------------------------------------------------------------------
    // PROTECTED METHODS
    //--------------------------------------------------------------------------
    
    protected static void checkEqual(String[] expected, MemPrintStream stream) {
        PrintTestUtils.checkEqual(expected, PrintTestUtils.toStringTab(stream.getLines()));
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------

    private static PackageData computeRefDefaultP(
            boolean withP3,
            boolean mustMergeNestedClasses,
            boolean apiOnly) {
        
        final PackageData refDefaultP = new PackageData();
        
        /*
         * Classes.
         */
        
        final ClassData obj = refDefaultP.getOrCreateClassData(Object.class.getName());
        //
        final ClassData c1 = refDefaultP.getOrCreateClassData(C1N);
        final ClassData c1cx;
        if (mustMergeNestedClasses) {
            c1cx = null;
        } else {
            c1cx = refDefaultP.getOrCreateClassData(C1CXN);
        }
        final ClassData c2 = refDefaultP.getOrCreateClassData(C2N);
        final ClassData c3 = refDefaultP.getOrCreateClassData(C3N);
        final ClassData c4 = refDefaultP.getOrCreateClassData(C4N);
        //
        final ClassData c5 = refDefaultP.getOrCreateClassData(C5N);
        final ClassData c6 = refDefaultP.getOrCreateClassData(C6N);
        final ClassData c7 = refDefaultP.getOrCreateClassData(C7N);
        //
        final ClassData c8;
        final ClassData c9;
        if (withP3) {
            c8 = refDefaultP.getOrCreateClassData(C8N);
            c9 = refDefaultP.getOrCreateClassData(C9N);
        } else {
            c8 = null;
            c9 = null;
        }
        
        /*
         * Byte sizes (no byte size for Object class, as if we didn't parse it).
         */
        
        PackageData.setByteSizeForClassOrNested(c1, c1.fileNameNoExt(), C1BS_NOMERGE);
        PackageData.setByteSizeForClassOrNested((mustMergeNestedClasses ? c1 : c1cx), C1CXFNNE, C1CXBS);
        PackageData.setByteSizeForClassOrNested(c2, c2.fileNameNoExt(), C2BS);
        PackageData.setByteSizeForClassOrNested(c3, c3.fileNameNoExt(), C3BS);
        PackageData.setByteSizeForClassOrNested(c4, c4.fileNameNoExt(), C4BS);
        PackageData.setByteSizeForClassOrNested(c5, c5.fileNameNoExt(), C5BS);
        PackageData.setByteSizeForClassOrNested(c6, c6.fileNameNoExt(), C6BS);
        PackageData.setByteSizeForClassOrNested(c7, c7.fileNameNoExt(), C7BS);
        if (withP3) {
            PackageData.setByteSizeForClassOrNested(c8, c8.fileNameNoExt(), C8BS);
            PackageData.setByteSizeForClassOrNested(c9, c9.fileNameNoExt(), C9BS);
        }
        
        /*
         * Dependencies.
         */
        
        PackageData.ensureDependency(c1, obj);
        if (!apiOnly) {
            PackageData.ensureDependency(c1, c2);
        }
        if (mustMergeNestedClasses) {
            PackageData.ensureDependency(c1, c3);
        } else {
            if (!apiOnly) {
                // Doesn't really matters, but makes things realistic.
                PackageData.ensureDependency(c1cx, c1);
            }
            PackageData.ensureDependency(c1, c1cx);
            PackageData.ensureDependency(c1cx, c3);
        }
        //
        PackageData.ensureDependency(c2, obj);
        PackageData.ensureDependency(c2, c5);
        //
        PackageData.ensureDependency(c3, obj);
        PackageData.ensureDependency(c3, c4);
        //
        PackageData.ensureDependency(c4, obj);
        PackageData.ensureDependency(c4, c2);
        PackageData.ensureDependency(c4, c6);
        //
        PackageData.ensureDependency(c5, obj);
        PackageData.ensureDependency(c5, c4);
        PackageData.ensureDependency(c5, c7);
        //
        PackageData.ensureDependency(c6, obj);
        PackageData.ensureDependency(c6, c4);
        PackageData.ensureDependency(c6, c7);
        //
        PackageData.ensureDependency(c7, obj);
        PackageData.ensureDependency(c7, c6);
        //
        if (withP3) {
            PackageData.ensureDependency(c8, obj);
            PackageData.ensureDependency(c8, c9);
            //
            PackageData.ensureDependency(c9, obj);
            PackageData.ensureDependency(c9, c8);
        }
        
        return refDefaultP;
    }
}
