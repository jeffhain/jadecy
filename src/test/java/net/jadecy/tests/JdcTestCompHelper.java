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

import net.jadecy.comp.CompHelper;

/**
 * Helper to compile code of this library for tests.
 */
public class JdcTestCompHelper {

    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------
    
    /**
     * NB: Can play with different versions for backward compatibility checks,
     * normally testing on the latest one should suffice.
     */
    public static final String JAVA_HOME = JdcTestConfig.getJdk8Home();
    
    private static final String SRC_V = "-source 1.8 -target 1.8";
    
    private static final String JAVAC_PATH = JAVA_HOME + "/bin/javac.exe";
    
    private static final String OUTPUT_DIR_PARENT_PATH = "test_comp";
    
    private static final CompHelper COMP_HELPER = new CompHelper(
            JAVAC_PATH,
            SRC_V,
            "lib/junit.jar",
            System.out,
            //
            OUTPUT_DIR_PARENT_PATH);

    /*
     * In compilation order.
     */
    
    public static final String MAIN_SRC_PATH = "src/main/java";
    public static final String TEST_SRC_PATH = "src/test/java";
    public static final String BUILD_SRC_PATH = "src/build/java";
    public static final String SAMPLES_SRC_PATH = "src/samples/java";
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    /**
     * @return A new array containing all sources directories paths,
     *         in compilation order.
     */
    public static String[] newAllSrcDirPathArr() {
        return new String[]{
                MAIN_SRC_PATH,
                TEST_SRC_PATH,
                BUILD_SRC_PATH,
                SAMPLES_SRC_PATH,
        };
    }
    
    /**
     * Uses CompHelper.ensureCompiledAndGetOutputDir(...) on a static instance,
     * to compile specified sources of this library.
     */
    public static String ensureCompiledAndGetOutputDirPath(String... srcDirPathArr) {
        return COMP_HELPER.ensureCompiledAndGetOutputDirPath(srcDirPathArr);
    }
}
