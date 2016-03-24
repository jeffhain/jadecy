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
package net.jadecy.code;

import java.util.ArrayList;

import net.jadecy.utils.ArgsUtils;

/**
 * Utilities for classes or packages names.
 */
public class NameUtils {

    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------
    
    static final boolean HANDLE_WEIRD_DOLLAR_SIGN_USAGES = true;
    
    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------

    /**
     * Is an empty string to keep name-related treatments simple.
     */
    public static final String DEFAULT_PACKAGE_NAME = "";

    public static final String DEFAULT_PACKAGE_DISPLAY_NAME = "[default package]";
    
    /*
     * 
     */
    
    private static final char CHAR_DOT = '.';
    private static final char CHAR_DOLLAR = '$';
    private static final char CHAR_SLASH = '/';
    
    private static final String STRING_DOT = ".";
    
    private static final String REGEX_DOT = "\\.";
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /**
     * @param name A string.
     * @return The specified string, with '/' replaced with '.'.
     * @throws NullPointerException if the specified string is null.
     */
    public static String doted(String name) {
        // Implicit null check.
        return name.replace(CHAR_SLASH, CHAR_DOT);
    }
    
    /**
     * @param name A string.
     * @return The specified string, with '.' replaced with '/'.
     * @throws NullPointerException if the specified string is null.
     */
    public static String slashed(String name) {
        // Implicit null check.
        return name.replace(CHAR_DOT, CHAR_SLASH);
    }

    /**
     * @param name A string.
     * @return The specified string, surrounded with double quotes ('"').
     * @throws NullPointerException if the specified string is null.
     */
    public static String quoted(String name) {
        return "\"" + ArgsUtils.requireNonNull(name) + "\"";
    }

    /**
     * If the specified class name is empty, returns an empty string.
     * 
     * @param className A class name.
     * @return The package name of the specified class name.
     * @throws NullPointerException if the specified class name is null.
     */
    public static String getPackageName(String className) {
        // Implicit null check.
        final int lastDotIndex = className.lastIndexOf(CHAR_DOT);
        if (lastDotIndex < 0) {
            return "";
        } else {
            return className.substring(0, lastDotIndex);
        }
    }
    
    /**
     * If the specified class name is empty, returns it.
     * 
     * @param className A class name.
     * @return The file name no ext of the specified class name.
     * @throws NullPointerException if the specified class name is null.
     */
    public static String getFileNameNoExt(String className) {
        // Implicit null check.
        final int lastDotIndex = className.lastIndexOf(CHAR_DOT);
        if (lastDotIndex < 0) {
            return className;
        } else {
            return className.substring(lastDotIndex+1, className.length());
        }
    }
    
    /**
     * If the specified class name is empty, returns it.
     * 
     * @param className A class name.
     * @return The top level class name, internal (with '/' instead of '.')
     *         or not depending on whether the specified class name is internal.
     * @throws NullPointerException if the specified class name is null.
     */
    public static String getTopLevelClassName(String className) {
        // Computing last dot index, both to avoid trouble with dollar sign
        // usage in package name, and because it should speed things up
        // in case of long package name (will only iterate over
        // class file name no ext).
        // Implicit null check.
        final int lastDotIndex = className.lastIndexOf(CHAR_DOT);
        
        // Works even if lastDotIndex < 0.
        final int firstDollarAfterDotIndex = className.indexOf(CHAR_DOLLAR, lastDotIndex+1);
        if (firstDollarAfterDotIndex < 0) {
            return className;
        } else {
            if (NameUtils.HANDLE_WEIRD_DOLLAR_SIGN_USAGES) {
                final String classFileNameNoExt;
                if (lastDotIndex < 0) {
                    classFileNameNoExt = className;
                } else {
                    classFileNameNoExt = className.substring(lastDotIndex+1);
                }
                if (isDollarPathologicalClassFileNameNoExt(classFileNameNoExt)) {
                    // Considering it's a top level class name.
                    return className;
                }
            }
            return className.substring(0, firstDollarAfterDotIndex);
        }
    }
    
    /**
     * If the specified class name is empty, returns null.
     * 
     * @param classFileNameNoExt A class file name no ext.
     * @return The outer class file name no ext, or null if there is no outer class.
     * @throws NullPointerException if the specified class file name no ext is null.
     */
    public static String getOuterClassFileNameNoExt(String classFileNameNoExt) {
        // Implicit null check.
        final int lastDollarIndex = classFileNameNoExt.lastIndexOf(CHAR_DOLLAR);
        if (lastDollarIndex < 0) {
            // No outer class.
            return null;
        } else {
            if (NameUtils.HANDLE_WEIRD_DOLLAR_SIGN_USAGES) {
                if (isDollarPathologicalClassFileNameNoExt(classFileNameNoExt)) {
                    // Considering it's a top level class name.
                    return null;
                }
            }
            return classFileNameNoExt.substring(0, lastDollarIndex);
        }
    }

    /**
     * @param name A class name or non-empty package name.
     * @return New array of dot-separated (and non-empty) names.
     * @throws IllegalArgumentException if name has dots in wrong places
     *         (corresponding to empty name parts) or is empty.
     * @throws NullPointerException if the specified name is null.
     */
    public static String[] splitName(String name) {
        if (false) {
            /*
             * Also works, but a bit slower, and less elegant.
             */
            if (name.startsWith(STRING_DOT)
                    || name.endsWith(STRING_DOT)) {
                throwIAEEmptyNameIn(name);
            }
            // If name is empty, parts will contain a single empty string,
            // so we will throw in the for loop.
            final String[] parts = name.split(REGEX_DOT);
            for (String part : parts) {
                if (part.length() == 0) {
                    throwIAEEmptyNameIn(name);
                }
            }
            return parts;
        }

        final ArrayList<String> list = new ArrayList<String>();

        int i = 0;
        while (true) {
            // Implicit null check.
            final int j = name.indexOf(CHAR_DOT, i);
            if (j < 0) {
                if (i == name.length()) {
                    throwIAEEmptyNameIn(name);
                }
                list.add(name.substring(i));
                break;
            }

            if (j == i) {
                throwIAEEmptyNameIn(name);
            }

            list.add(name.substring(i, j));

            i = j + 1;
        }

        return list.toArray(new String[list.size()]);
    }

    /*
     * 
     */

    /**
     * Computes the regular expression corresponding to a package or class name,
     * by escaping dots.
     * 
     * @param name A string.
     * @return The specified string, with '.' replaced with "\.".
     * @throws NullPointerException if the specified name is null.
     */
    public static String toRegex(String name) {
        // Implicit null check.
        return name.replace(STRING_DOT, REGEX_DOT);
    }
    
    /**
     * @param name A class or package name.
     * @return The specified name if it is not empty, else DEFAULT_PACKAGE_DISPLAY_NAME.
     * @throws NullPointerException if the specified name is null.
     */
    public static String toDisplayName(String name) {
        // Implicit null check.
        if (name.length() == 0) {
            return DEFAULT_PACKAGE_DISPLAY_NAME;
        } else {
            return name;
        }
    }

    /*
     * 
     */
    
    /**
     * @param subject A string.
     * @param beginName A name.
     * @return True if subject starts with the specified name,
     *         i.e. always name is empty, else if name equals it,
     *         or starts with it followed by '.' or '$'.
     * @throws NullPointerException if subject or beginName is null.
     */
    public static boolean startsWithName(String subject, String beginName) {
        // Implicit null checks.
        if (!subject.startsWith(beginName) ) {
            return false;
        }
        if (beginName.length() == 0) {
            // Anything matches.
            return true;
        }
        if (subject.length() == beginName.length()) {
            // Equals.
            return true;
        }
        final char afterBeginNameChar = subject.charAt(beginName.length());
        return (afterBeginNameChar == CHAR_DOT) || (afterBeginNameChar == CHAR_DOLLAR);
    }

    /**
     * @param subject A string.
     * @param endName A name.
     * @return True if subject ends with the specified name,
     *         i.e. always name is empty, else if name equals it,
     *         or ends with it preceded by '.' or '$'.
     * @throws NullPointerException if subject or endName is null.
     */
    public static boolean endsWithName(String subject, String endName) {
        // Implicit null checks.
        if (!subject.endsWith(endName) ) {
            return false;
        }
        if (endName.length() == 0) {
            // Anything matches.
            return true;
        }
        final int deltaLength = subject.length() - endName.length();
        if (deltaLength == 0) {
            // Equals.
            return true;
        }
        final char beforeEndNameChar = subject.charAt(deltaLength - 1);
        return (beforeEndNameChar == CHAR_DOT) || (beforeEndNameChar == CHAR_DOLLAR);
    }

    /**
     * @param subject A string.
     * @param name A name.
     * @return True if subject contains the specified name,
     *         i.e. always if name is empty, else if name equals it,
     *         or starts with it followed by '.' or '$',
     *         or ends with it preceded by '.' or '$',
     *         or contains it preceded and followed by '.' or '$'.
     * @throws NullPointerException if subject or endName is null.
     */
    public static boolean containsName(String subject, String name) {
        
        ArgsUtils.requireNonNull(subject);
        
        // Implicit null check.
        if (name.length() == 0) {
            // Anything matches.
            return true;
        }

        int from = 0;
        while (true) {
            final int index = subject.indexOf(name, from);
            if (index < 0) {
                return false;
            }
            
            final boolean beforeOk;
            final int beforeIndex = index - 1;
            if (beforeIndex < 0) {
                // Nothing before.
                beforeOk = true;
            } else {
                final char beforeNameChar = subject.charAt(beforeIndex);
                beforeOk = (beforeNameChar == CHAR_DOT) || (beforeNameChar == CHAR_DOLLAR);
            }
            
            final boolean afterOk;
            final int afterIndex = index + name.length();
            if (afterIndex > subject.length() - 1) {
                // Nothing after.
                afterOk = true;
            } else {
                final char afterNameChar = subject.charAt(afterIndex);
                afterOk = (afterNameChar == CHAR_DOT) || (afterNameChar == CHAR_DOLLAR);
            }
            
            if (beforeOk && afterOk) {
                return true;
            }
            
            from = index + name.length() + 1;
        }
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    private NameUtils() {
    }

    /*
     * 
     */
    
    private static void throwIAEEmptyNameIn(String name) {
        throw new IllegalArgumentException("empty name in " + name);
    }
    
    /*
     * 
     */

    /**
     * @return True if the specified class file name no ext,
     *         uses dollar signs pathologically, false otherwise.
     */
    private static boolean isDollarPathologicalClassFileNameNoExt(
            String classFileNameNoExt) {
        
        final int firstDollarIndex = classFileNameNoExt.indexOf(CHAR_DOLLAR);
        if (firstDollarIndex < 0) {
            return false;
        }
        
        final int size = classFileNameNoExt.length();
        
        if ((firstDollarIndex == 0)
                || (classFileNameNoExt.charAt(size-1) == CHAR_DOLLAR)) {
            // Starts or ends with dollar sign.
            return true;
        }
        
        /*
         * Looking for consecutive dollar signs.
         */
        
        int index1 = firstDollarIndex;
        int index2;
        while (true) {
            final int i1p1 = index1 + 1;
            index2 = classFileNameNoExt.indexOf(CHAR_DOLLAR, i1p1);
            if (index2 < 0) {
                break;
            }
            if (index2 == i1p1) {
                // Consecutive dollar signs.
                return true;
            }
            index1 = index2;
        }
        
        return false;
    }
}
