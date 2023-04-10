/*
 * Copyright 2023 Jeff Hain
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

/**
 * Utilities for Jadecy tests.
 */
public class JdcTestUtils {
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    /**
     * @return 8 for 1.8.x.y, 9 for 9.x.y, etc.
     */
    public static int getJavaVersion() {
        String verStr = System.getProperty("java.version");
        
        // Removing "1.".
        if (verStr.startsWith("1.")) {
            verStr = verStr.substring(2);
        }
        
        // Removing ".*".
        final int di = verStr.indexOf(".");
        if (di > 0) {
            verStr = verStr.substring(0, di);
        }
        
        // Parsing major version.
        final int ver = Integer.parseInt(verStr);
        
        return ver;
    }
    
    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    private JdcTestUtils() {
    }
}
