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
package net.jadecy.comp;

public class PathHelper {

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    /**
     * Useful for example for javac, which doesn't like file paths with spaces
     * if they are not in double quotes.
     * 
     * @param path A path.
     * @return The specified path in double quotes, with eventual anti slashes
     *         and double quotes escaped.
     */
    public static String quoted(String path) {
        // NB: To get "\", must write "\\", or "\\\\" in a regex.
        
        // Replacing "\" with "\\".
        path = path.replaceAll("\\\\", "\\\\\\\\");
        
        // Replacing """ with "\"".
        path = path.replaceAll("\"", "\\\\\"");
        
        return "\"" + path + "\"";
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    private PathHelper() {
    }
}
