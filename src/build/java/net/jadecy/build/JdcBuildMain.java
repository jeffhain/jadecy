/*
 * Copyright 2017-2019 Jeff Hain
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
package net.jadecy.build;

import java.io.File;
import java.util.Arrays;

import net.jadecy.comp.JavacHelper;
import net.jadecy.comp.JdcFsUtils;
import net.jadecy.names.InterfaceNameFilter;
import net.jadecy.names.NameFilters;

/**
 * Compiles sources and creates the jar.
 */
public class JdcBuildMain {

    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------
    
    private static final String JAR_VERSION = "2.0";
    private static final String JAR_FILE_NAME = "jadecy.jar";
    
    private static final String SOURCE_VERSION = "1.5";
    private static final String TARGET_VERSION = "1.5";

    private static final String MAIN_SRC_PATH = "src/main/java";

    private static final String COMP_DIR_PATH = "build_comp";

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    public static void main(String[] args) {
        
        final JavacHelper helper = new JavacHelper(
                SOURCE_VERSION,
                TARGET_VERSION,
                Arrays.asList(new String[]{}));
        
        final File compDir = new File(COMP_DIR_PATH);

        // Just to make sure it's empty,
        // since compilation ensures it anyway.
        JdcFsUtils.ensureEmptyDir(compDir);

        final InterfaceNameFilter relativeNameFilter = NameFilters.any();
        helper.compile(
                COMP_DIR_PATH,
                relativeNameFilter,
                Arrays.asList(MAIN_SRC_PATH));
        
        final String jarFilePath = "dist/" + JAR_FILE_NAME;
        JdcJarBuilder.createJarFile(
                compDir,
                JAR_VERSION,
                jarFilePath);
        
        System.out.println("generated " + jarFilePath);
    }
}
