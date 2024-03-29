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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Installation related tests configuration.
 */
public class JdcTestConfig {

    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------

    private static final String CONFIG_FILE_PATH = "src/test/resources/test_config.properties";
    
    private static final Properties PROPERTIES = new Properties();
    static {
        try {
            final FileInputStream fis = new FileInputStream(CONFIG_FILE_PATH);
            try {
                PROPERTIES.load(fis);
            } finally {
                fis.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    /**
     * Used for tests compilation and jdeps.
     */
    public static String getJdkHome() {
        return PROPERTIES.getProperty("JDK_HOME");
    }
    
    /**
     * Used for rt.jar, for benches.
     */
    public static String getJdk8Home() {
        return PROPERTIES.getProperty("JDK8_HOME");
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    private JdcTestConfig() {
    }
}
