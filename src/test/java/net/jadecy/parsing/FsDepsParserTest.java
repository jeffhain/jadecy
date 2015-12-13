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
package net.jadecy.parsing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.TestCase;
import net.jadecy.code.ClassData;
import net.jadecy.code.NameFilters;
import net.jadecy.code.NameUtils;
import net.jadecy.code.PackageData;
import net.jadecy.parsing.test1.A;
import net.jadecy.parsing.test2.B;
import net.jadecy.tests.JdcTestCompHelper;

public class FsDepsParserTest extends TestCase {

    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------

    private static final boolean DEBUG = false;

    private static final String COMPILATION_OUTPUT_DIR_PATH =
            JdcTestCompHelper.ensureCompiledAndGetOutputDirPath(
                    JdcTestCompHelper.MAIN_SRC_PATH,
                    JdcTestCompHelper.TEST_SRC_PATH);

    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------

    private static final boolean[] FALSE_TRUE = new boolean[]{false,true};

    /**
     * A jar that must contain at least one class.
     */
    private static final String DUMMY_JAR_NAME = "junit.jar";
    private static final String DUMMY_JAR_PATH = "lib/" + DUMMY_JAR_NAME;

    private static final File NON_EXISTING_CLASS = new File(COMPILATION_OUTPUT_DIR_PATH + "/" + "nonexisting.class");
    private static final File NON_EXISTING_JAR = new File(COMPILATION_OUTPUT_DIR_PATH + "/" + "nonexisting.jar");
    private static final File NON_EXISTING_DIR = new File(COMPILATION_OUTPUT_DIR_PATH + "/" + "nonexisting");

    private static final String CLASS_A_NAME = A.class.getName();
    private static final String PACKAGE_TEST1_NAME = CLASS_A_NAME.substring(0,CLASS_A_NAME.lastIndexOf('.'));
    
    private static final String CLASS_B_NAME = B.class.getName();
    private static final String CLASS_C_NAME = CLASS_B_NAME + "$C";
    private static final String PACKAGE_TEST2_NAME = CLASS_B_NAME.substring(0,CLASS_B_NAME.lastIndexOf('.'));
    
    private static final File CLASS_C_FILE = new File(COMPILATION_OUTPUT_DIR_PATH + "/" + slashed(CLASS_C_NAME) + ".class");
    private static final File PACKAGE_TEST1_FILE = new File(COMPILATION_OUTPUT_DIR_PATH + "/" + slashed(PACKAGE_TEST1_NAME));
    private static final File PACKAGE_TEST2_FILE = new File(COMPILATION_OUTPUT_DIR_PATH + "/" + slashed(PACKAGE_TEST2_NAME));

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    public void test_getMustMergeNestedClasses_and_getApiOnly() {
        for (boolean mustMergeNestedClasses : new boolean[]{false,true}) {
            for (boolean apiOnly : new boolean[]{false,true}) {
                final FsDepsParser parser = new FsDepsParser(
                        mustMergeNestedClasses,
                        apiOnly);
                
                assertEquals(mustMergeNestedClasses, parser.getMustMergeNestedClasses());
                assertEquals(apiOnly, parser.getApiOnly());
            }
        }
    }

    public void test_getDefaultPackageData() {
        final FsDepsParser parser = newDepsParser();
        final ParsingFilters filters = ParsingFilters.defaultInstance();
        
        final PackageData defaultP = parser.getDefaultPackageData();
        assertNull(defaultP.parent());
        assertEquals(0, defaultP.childClassDataByFileNameNoExt().size());
        assertEquals(0, defaultP.childPackageDataByDirName().size());
        
        parser.accumulateDependencies(
                PACKAGE_TEST1_FILE,
                filters);
        
        // Instance must not change on parsing.
        assertSame(defaultP, parser.getDefaultPackageData());
    }
    
    /*
     * 
     */
    
    public void test_accumulateDependencies_classFileNotExisting() {
        final FsDepsParser parser = newDepsParser();
        final ParsingFilters filters = ParsingFilters.defaultInstance();

        checkThrowsIAEFileNotFound(parser, NON_EXISTING_CLASS, filters);
    }

    public void test_accumulateDependencies_jarFileNotExisting() {
        final FsDepsParser parser = newDepsParser();
        final ParsingFilters filters = ParsingFilters.defaultInstance();

        checkThrowsIAEFileNotFound(parser, NON_EXISTING_JAR, filters);
    }

    public void test_accumulateDependencies_dirFileNotExisting() {
        final FsDepsParser parser = newDepsParser();
        final ParsingFilters filters = ParsingFilters.defaultInstance();

        checkThrowsIAEFileNotFound(parser, NON_EXISTING_DIR, filters);
    }
    
    public void test_accumulateDependencies_classFileNotExistingNotMatchingFilters() {
        final FsDepsParser parser = newDepsParser();
        ParsingFilters filters = ParsingFilters.defaultInstance();

        filters = filters.withClassFilenameFilter(new FilenameFilter() {
            //@Override
            public boolean accept(File dir, String name) {
                return false;
            }
        });

        checkThrowsIAEFileNotFound(parser, NON_EXISTING_CLASS, filters);
    }

    public void test_accumulateDependencies_jarFileNotExistingNotMatchingFilters() {
        final FsDepsParser parser = newDepsParser();
        ParsingFilters filters = ParsingFilters.defaultInstance();

        filters = filters.withJarFilenameFilter(new FilenameFilter() {
            //@Override
            public boolean accept(File dir, String name) {
                return false;
            }
        });

        checkThrowsIAEFileNotFound(parser, NON_EXISTING_JAR, filters);
    }

    public void test_accumulateDependencies_dirFileNotExistingNotMatchingFilters() {
        final FsDepsParser parser = newDepsParser();
        ParsingFilters filters = ParsingFilters.defaultInstance();

        filters = filters.withDirFilenameFilter(new FilenameFilter() {
            //@Override
            public boolean accept(File dir, String name) {
                return false;
            }
        });

        checkThrowsIAEFileNotFound(parser, NON_EXISTING_DIR, filters);
    }
    
    /*
     * 
     */

    public void test_accumulateDependencies_useOf_mustMergeNestedClasses_and_apiOnly() {
        for (boolean mustMergeNestedClasses : FALSE_TRUE) {
            for (boolean apiOnly : FALSE_TRUE) {

                if (DEBUG) {
                    System.out.println();
                    System.out.println("mustMergeNestedClasses = " + mustMergeNestedClasses);
                    System.out.println("apiOnly = " + apiOnly);
                }
                
                final FsDepsParser parser = new FsDepsParser(
                        mustMergeNestedClasses,
                        apiOnly);
                final PackageData defaultP = parser.getDefaultPackageData();
                final ParsingFilters filters = ParsingFilters.defaultInstance();

                parser.accumulateDependencies(
                        PACKAGE_TEST1_FILE,
                        filters);
                parser.accumulateDependencies(
                        PACKAGE_TEST2_FILE,
                        filters);

                final PackageData p1 = defaultP.getPackageData(PACKAGE_TEST1_NAME);
                final PackageData p2 = defaultP.getPackageData(PACKAGE_TEST2_NAME);

                if (DEBUG) {
                    defaultP.printSubtree();
                }

                assertTrue(p1.causeSetBySuccessor().get(p2).contains(p1.getClassData("A")));
                
                assertEquals(!mustMergeNestedClasses, p2.childClassDataByFileNameNoExt().containsKey("B$C"));
                
                if (apiOnly) {
                    assertFalse(p2.successors().contains(p1));
                } else {
                    if (mustMergeNestedClasses) {
                        assertTrue(p2.causeSetBySuccessor().get(p1).contains(p2.getClassData("B")));
                    } else {
                        assertFalse(p2.causeSetBySuccessor().get(p1).contains(p2.getClassData("B")));
                        assertTrue(p2.causeSetBySuccessor().get(p1).contains(p2.getClassData("B$C")));
                    }
                }
            }
        }
    }

    /*
     * Filters usage.
     */
    
    public void test_accumulateDependencies_useOf_dirFilenameFilter() {
        final FsDepsParser parser = newDepsParser();
        final PackageData defaultP = parser.getDefaultPackageData();
        ParsingFilters filters = ParsingFilters.defaultInstance();

        filters = filters.withDirFilenameFilter(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                assertNotNull(dir);
                return (!name.equals("test2"));
            }
        });
        
        parser.accumulateDependencies(PACKAGE_TEST1_FILE, filters);
        parser.accumulateDependencies(PACKAGE_TEST2_FILE, filters);

        if (DEBUG) {
            defaultP.printSubtree();
        }

        final PackageData p2 = defaultP.getPackageData(PACKAGE_TEST2_NAME);
        
        // p2 exists, as dependency of p1, and even contains some classes,
        // which p1 classes depend on, but corresponding class data are empty.
        assertEquals(0, p2.successors().size());
        final ClassData b = p2.getClassData("B");
        final ClassData c = p2.getClassData("B$C");
        // Class data for B exists due to dependencies from p1,
        // but has not been parsed so its byte size is 0.
        assertEquals(0, b.byteSize());
        // Nothing in p1 depends on C, so class data not created.
        assertNull(c);
    }

    public void test_accumulateDependencies_useOf_classFilenameFilter() {
        final FsDepsParser parser = newDepsParser();
        final PackageData defaultP = parser.getDefaultPackageData();
        ParsingFilters filters = ParsingFilters.defaultInstance();

        // Won't parse C.
        filters = filters.withClassFilenameFilter(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                assertNotNull(dir);
                return name.endsWith(".class")
                        && (!name.endsWith("C.class"));
            }
        });

        parser.accumulateDependencies(PACKAGE_TEST2_FILE, filters);

        if (DEBUG) {
            defaultP.printSubtree();
        }

        /*
         * Class data for C exists as dependency from B,
         * but has not been parsed so is empty, and there is no
         * dependency to p1 which data has therefore not been created.
         */
        
        final PackageData p1 = defaultP.getPackageData(PACKAGE_TEST1_NAME);
        final PackageData p2 = defaultP.getPackageData(PACKAGE_TEST2_NAME);

        assertNull(p1);
        assertNotNull(p2);

        final ClassData b = p2.getClassData("B");
        final ClassData c = p2.getClassData("B$C");
        
        assertNotNull(b);
        assertNotNull(c);
        assertEquals(0, c.byteSize());
    }

    public void test_accumulateDependencies_useOf_jarFilenameFilter() {
        final FsDepsParser parser = newDepsParser();
        final PackageData defaultP = parser.getDefaultPackageData();
        ParsingFilters filters = ParsingFilters.defaultInstance();

        // To check that jar file was found and not empty.
        final AtomicBoolean acceptCalled = new AtomicBoolean();

        // Won't parse anything.
        filters = filters.withJarFilenameFilter(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                assertNotNull(dir);
                assertNotNull(name);
                acceptCalled.set(true);
                return false;
            }
        });

        parser.accumulateDependencies(new File(DUMMY_JAR_PATH), filters);

        if (DEBUG) {
            defaultP.printSubtree();
        }

        assertTrue(acceptCalled.get());

        // Nothing accepted.
        assertEquals(0, defaultP.childClassDataByFileNameNoExt().size());
        assertEquals(0, defaultP.childPackageDataByDirName().size());
    }

    public void test_accumulateDependencies_useOf_jarEntryFilenameFilter() {
        final FsDepsParser parser = newDepsParser();
        final PackageData defaultP = parser.getDefaultPackageData();
        ParsingFilters filters = ParsingFilters.defaultInstance();

        // To check that jar file was found and not empty.
        final AtomicBoolean acceptCalled = new AtomicBoolean();

        // Won't parse anything.
        filters = filters.withJarEntryFilenameFilter(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                assertNotNull(dir);
                assertNotNull(name);
                acceptCalled.set(true);
                return false;
            }
        });

        parser.accumulateDependencies(new File(DUMMY_JAR_PATH), filters);

        if (DEBUG) {
            defaultP.printSubtree();
        }

        assertTrue(acceptCalled.get());

        // Nothing accepted.
        assertEquals(0, defaultP.childClassDataByFileNameNoExt().size());
        assertEquals(0, defaultP.childPackageDataByDirName().size());
    }

    public void test_accumulateDependencies_useOf_classNameFilter() {
        final FsDepsParser parser = newDepsParser();
        final PackageData defaultP = parser.getDefaultPackageData();
        ParsingFilters filters = ParsingFilters.defaultInstance();

        // Won't parse C.
        filters = filters.withClassNameFilter(
                NameFilters.not(
                        NameFilters.equalsName(CLASS_C_NAME)));

        parser.accumulateDependencies(PACKAGE_TEST2_FILE, filters);

        if (DEBUG) {
            defaultP.printSubtree();
        }

        /*
         * Class data for C exists as dependency from B,
         * but has not been parsed so is empty, and there is no
         * dependency to p1 which data has therefore not been created.
         */
        
        final PackageData p1 = defaultP.getPackageData(PACKAGE_TEST1_NAME);
        final PackageData p2 = defaultP.getPackageData(PACKAGE_TEST2_NAME);

        assertNull(p1);
        assertNotNull(p2);
        
        final ClassData b = p2.getClassData("B");
        final ClassData c = p2.getClassData("B$C");
        
        assertNotNull(b);
        assertTrue(b.successors().contains(c));
        assertTrue(b.byteSize() != 0);
        
        assertNotNull(c);
        assertEquals(0, c.successors().size());
        assertEquals(0, c.byteSize());
    }

    /*
     * Parsing.
     */

    public void test_accumulateDependencies_exceptions() {
        final FsDepsParser parser = newDepsParser();
        ParsingFilters filters = ParsingFilters.defaultInstance();

        try {
            parser.accumulateDependencies(null, filters);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }

        try {
            parser.accumulateDependencies(new File(""), null);
            assertTrue(false);
        } catch (NullPointerException e) {
            // ok
        }
    }

    public void test_accumulateDependencies_classFile() {
        final FsDepsParser parser = newDepsParser();
        final PackageData defaultP = parser.getDefaultPackageData();
        ParsingFilters filters = ParsingFilters.defaultInstance();

        final File toParse = CLASS_C_FILE;

        assertTrue(parser.accumulateDependencies(toParse, filters));

        if (DEBUG) {
            defaultP.printSubtree();
        }
        
        /*
         * Could only parse C, so see p1 from p2, but nothing from p1.
         */
        
        final PackageData p1 = defaultP.getPackageData(PACKAGE_TEST1_NAME);
        final PackageData p2 = defaultP.getPackageData(PACKAGE_TEST2_NAME);
        
        assertNotNull(p1);
        assertNotNull(p2);

        final ClassData a = p1.getClassData("A");
        final ClassData b = p2.getClassData("B");
        final ClassData c = p2.getClassData("B$C");
        
        assertNotNull(a);
        assertEquals(0, a.byteSize());
        assertEquals(0, a.successors().size());
        
        assertNotNull(b);
        assertEquals(0, b.byteSize());
        assertEquals(0, b.successors().size());
        
        assertNotNull(c);
        assertTrue(c.byteSize() != 0);
        assertTrue(c.successors().contains(a));

        // Nothing new parsed.
        assertFalse(parser.accumulateDependencies(toParse, filters));
    }

    public void test_accumulateDependencies_jarFile() {
        final FsDepsParser parser = newDepsParser();
        final PackageData defaultP = parser.getDefaultPackageData();
        final ParsingFilters filters = ParsingFilters.defaultInstance();

        final File toParse = new File(DUMMY_JAR_PATH);

        assertTrue(parser.accumulateDependencies(toParse, filters));
        
        if (DEBUG) {
            defaultP.printSubtree();
        }

        // Could parse something (all, normally).
        assertTrue(defaultP.getSubtreeClassCount() != 0);

        // Nothing new parsed.
        assertFalse(parser.accumulateDependencies(toParse, filters));
    }

    public void test_accumulateDependencies_dirWithClassFiles() {
        final FsDepsParser parser = newDepsParser();
        final PackageData defaultP = parser.getDefaultPackageData();
        final ParsingFilters filters = ParsingFilters.defaultInstance();

        final File toParse = new File(COMPILATION_OUTPUT_DIR_PATH);

        assertTrue(parser.accumulateDependencies(toParse, filters));
        
        if (DEBUG) {
            defaultP.printSubtree();
        }

        /*
         * Could parse all classes.
         */
        
        final PackageData p1 = defaultP.getPackageData(PACKAGE_TEST1_NAME);
        final PackageData p2 = defaultP.getPackageData(PACKAGE_TEST2_NAME);

        assertTrue(p1.successors().contains(p2));
        assertTrue(p2.successors().contains(p1));

        // Nothing new parsed.
        assertFalse(parser.accumulateDependencies(toParse, filters));
    }

    public void test_accumulateDependencies_dirWithJarFiles() {
        final FsDepsParser parser = newDepsParser();
        final PackageData defaultP = parser.getDefaultPackageData();
        final ParsingFilters filters = ParsingFilters.defaultInstance();

        final File toParse = new File(DUMMY_JAR_PATH).getParentFile();

        assertTrue(parser.accumulateDependencies(toParse, filters));

        if (DEBUG) {
            defaultP.printSubtree();
        }
        
        // Could parse something (all, normally).
        assertTrue(defaultP.getSubtreeClassCount() != 0);

        // Nothing new parsed.
        assertFalse(parser.accumulateDependencies(toParse, filters));
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------

    private static FsDepsParser newDepsParser() {
        return new FsDepsParser(false, false);
    }

    private static String slashed(String name) {
        return NameUtils.slashed(name);
    }
    
    /*
     * 
     */
    
    private static void checkThrowsIAEFileNotFound(
            FsDepsParser parser,
            File badFile,
            ParsingFilters filters) {
        try {
            parser.accumulateDependencies(
                    badFile,
                    filters);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof FileNotFoundException) {
                // ok
            } else {
                assertTrue(false);
            }
        }
    }
}
