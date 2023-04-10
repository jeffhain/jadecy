/*
 * Copyright 2015-2023 Jeff Hain
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.jadecy.comp.CompHelper;

/**
 * Helper to compile code of this library for tests.
 */
public class JdcTestCompHelper {

    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------
    
    private static final int EXECUTING_JAVA_VERSION = JdcTestUtils.getJavaVersion();
    
    /*
     * Using highest possible source and target version.
     * To change them, either change them here,
     * or change the executing JVM.
     */
    
    private static final String SOURCE_VERSION = Integer.toString(EXECUTING_JAVA_VERSION);
    private static final String TARGET_VERSION = SOURCE_VERSION;
    
    /*
     * 
     */
    
    private static final String OUTPUT_DIR_PARENT_PATH = "test_comp";
    
    private static final CompHelper COMP_HELPER = new CompHelper(
            SOURCE_VERSION,
            TARGET_VERSION,
            Arrays.asList("lib/junit.jar"),
            //
            OUTPUT_DIR_PARENT_PATH);

    /*
     * In compilation order.
     */
    
    public static final String MAIN_SRC_PATH = "src/main/java";
    public static final String TEST_SRC_PATH = "src/test/java";
    public static final String BUILD_SRC_PATH = "src/build/java";
    public static final String SAMPLES_SRC_PATH = "src/samples/java";
    
    /**
     * Unmodifiable list containing all sources directories paths,
     * in compilation order.
     */
    public static final List<String> ALL_SRC_PATH_LIST =
            Collections.unmodifiableList(
                    Arrays.asList(
                            MAIN_SRC_PATH,
                            TEST_SRC_PATH,
                            BUILD_SRC_PATH,
                            SAMPLES_SRC_PATH));
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    /**
     * Uses CompHelper.ensureCompiledAndGetOutputDir(...) on a static instance,
     * to compile specified sources of this library.
     */
    public static String ensureCompiledAndGetOutputDirPath(List<String> srcDirPathList) {
        return COMP_HELPER.ensureCompiledAndGetOutputDirPath(srcDirPathList);
    }
}
