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
package net.jadecy.build;

import java.io.File;

import net.jadecy.comp.CompHelper;
import net.jadecy.comp.JavacHelper;
import net.jadecy.comp.RuntimeExecHelper;

/**
 * Class to build the jar of this library.
 */
public class JadecyJarBuilder {

    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------
    
    private static final String JAVA_HOME = JadecyBuildConfig.getJdkHome();
    
    private static final String SRC_V = "-source 1.5 -target 1.5";
    
    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------
    
    private static final String JAVAC_PATH = JAVA_HOME + "/bin/javac.exe";
    private static final String JAR_PATH = JAVA_HOME + "/bin/jar.exe";
    
    private static final String MAIN_SRC_PATH = "src/main/java";
    
    private static final String OUTPUT_DIR_PARENT_PATH = "build_comp";
    
    private static final String JAR_FILE_NAME = "jadecy.jar";

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    public static void main(String[] args) {
        final CompHelper compHelper = new CompHelper(
                JAVAC_PATH,
                SRC_V,
                "",
                System.out,
                //
                OUTPUT_DIR_PARENT_PATH);
        
        final String compDirPath = compHelper.ensureCompiledAndGetOutputDirPath(MAIN_SRC_PATH);
        
        /*
         * generating jar
         */
        
        // Not wanting the list in the jar.
        final String srcToCompFilePath = compDirPath + "/" + JavacHelper.SRC_TO_COMP_FILE_NAME;
        new File(srcToCompFilePath).delete();

        RuntimeExecHelper.execSyncNoIE(
                JAR_PATH + " -cf dist/" + JAR_FILE_NAME + " -C " + compDirPath + " .",
                System.out);
    }
}
