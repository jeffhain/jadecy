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
package net.jadecy.parsing;

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Computes class dependencies of a class, based on its class file.
 * As a result, dependencies might differ from what appears in source files:
 * - Due to javac optimizations, such as:
 *   - Unused imports removal.
 *   - Add of StringBuilder for Strings construction.
 *   - Add of AssertionError for assert statements.
 *   - Some types used in the code might not make it into the class file.
 * - Due to class file containing possibly a lot of nested classes not
 *   actually depended on.
 * - Due to nested classes being defined in their own ".class" files.
 * - Due to bytecode generated for records.
 * 
 * Handles class files of major version <= 64 (Java 20), and does best effort
 * if major version is higher.
 * 
 * Theoretically, we would like to consider a class to be API, and have some
 * dependencies when apiOnly is true, when it is neither public nor protected,
 * or has a neither public nor protected outer class up to top level class
 * included.
 * Though, when computing the APIness of a nested class, since the access flags
 * of the top level class are not indicated in the class file, and it would be
 * too restrictive to suppose the top level class to be package-private, we
 * suppose it to be public.
 * 
 * Dependencies and annotations:
 * Annotations used as annotations (i.e. with '@' prefix), whether on regular
 * classes, methods, etc., or on other annotations, are not API dependencies.
 * 
 * Dependencies and nested classes:
 * A class has non-API dependencies on its direct nested classes, on its outer
 * classes up to top level class, and on the outer classes of the nested classes
 * it depends on, up to their top level classes.
 * An API constructor of a non-static nested class causes an API dependency
 * of its class to its outer class.
 * 
 * Dependencies and modules:
 * Computes dependency from a module to its main class,
 * but not dependencies between modules (out of scope for this library).
 */
public class ClassDepsParser {

    /*
     * ".class" file format definition in jvms20.pdf, ch. 4.
     * 
     * NB:
     * The format uses "inner" word for data related to nested classes
     * (inner classes as defined by ch. 8 being non-static nested classes),
     * and "outer" word for data related to surrounding class.
     */
    
    /*
     * Based on DataInput, and not on RandomAccessFile/FileChannel/ByteBuffer:
     * - Faster (with buffered input stream).
     * - Allows for usage with ZipFile.getInputStream(ZipEntry).
     */
    
    /*
     * The Code attribute doesn't contain indexes of classes used in the
     * corresponding bytecode, which we don't want to bother parsing
     * (these treatments are already far too complex for what they are doing).
     * As a result, when computing all dependencies (not only the API ones),
     * we consider every class name present in the constant pool to be a
     * dependency.
     * That includes direct nested classes and outer classes of current class
     * up to the top level class, and outer classes of nested classes actually
     * depended on, up to their top level classes as well.
     */
    
    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------
    
    private static final boolean DEBUG = false;

    private static final boolean ANNOS_NOT_IN_API = true;
    
    //--------------------------------------------------------------------------
    // PRIVATE CLASSES
    //--------------------------------------------------------------------------
    
    /**
     * Contains some input and some output.
     * 
     * Not optimizing by using primitive collections or taking advantage
     * of our keys being relatively small integers (i.e. indexes),
     * gains not looking to be worth it.
     */
    private static class MyData {
        final boolean apiOnly;
        boolean foundNonApiClassAbove = false;
        /*
         * Data always filled, from constants pool.
         */
        /**
         * key = index for a CONSTANT_Utf8
         * value = the String
         */
        final Map<Integer,String> stringByUtf8Index = new HashMap<Integer,String>();
        /**
         * Must only be called after UTF8 strings of constant pool
         * have been parsed.
         * 
         * @throws IllegalArgumentException if the string
         *         has not been read from constant pool yet.
         */
        String stringOfUtf8Index(Integer index) {
            final String ret = this.stringByUtf8Index.get(index);
            if (ret == null) {
                throw new IllegalArgumentException("no value in stringByUtf8Index for key " + index);
            }
            return ret;
        }
        /**
         * Need for a map so that we can retrieve class name of the class, using "thisClassIndex",
         * else a "classNamesUtf8Indexes" list would be enough (since two classes
         * must not have a same name, hence not a same Utf8 index for their names).
         */
        final Map<Integer,Integer> classNamesUtf8IndexByClassIndex = new HashMap<Integer,Integer>();
        String classNameOfClassIndex(Integer index) {
            final Integer utf8Index = this.classNamesUtf8IndexByClassIndex.get(index);
            return this.stringOfUtf8Index(utf8Index);
        }
        /*
         * Classes indexes, descriptors indexes, and signatures indexes,
         * corresponding to class names to take into account for dependencies.
         */
        /**
         * element = index of a CONSTANT_Class_info structure.
         */
        final Set<Integer> depClassesIndexes = new HashSet<Integer>();
        /**
         * Descriptor: without generic typing.
         * 
         * Need for a set because multiple stuffs might have identical descriptors.
         */
        final Set<Integer> depDescriptorsUtf8Indexes = new HashSet<Integer>();
        /**
         * Signature: with generic typing.
         * 
         * Need for a set because multiple stuffs might have identical signatures.
         */
        final Set<Integer> depSignaturesUtf8Indexes = new HashSet<Integer>();
        /*
         * 
         */
        /**
         * Computed by classVersion(...) method.
         */
        long classVersion = 0;
        int class_access_flags;
        /**
         * Index for CONSTANT_Class of the class.
         */
        int thisClassIndex;
        /**
         * Where names of classes depended on are put.
         * Using a set ensures unicity of adds into the specified collection,
         * and makes it easy to ensure that we don't add "this class name" into it.
         */
        final Set<String> dependencySet = new HashSet<String>();
        MyData(boolean apiOnly) {
            this.apiOnly = apiOnly;
        }
    }
    
    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------
    
    /**
     * Ordinal is the corresponding value of the tag.
     * Easier for debug than using int values.
     */
    private enum MyTag {
        unused_0,
        CONSTANT_Utf8, // 1 (Java 1.0.2+)
        unused_2,
        CONSTANT_Integer, // 3 (Java 1.0.2+)
        CONSTANT_Float, // 4 (Java 1.0.2+)
        CONSTANT_Long, // 5 (Java 1.0.2+)
        CONSTANT_Double, // 6 (Java 1.0.2+)
        CONSTANT_Class, // 7 (Java 1.0.2+)
        CONSTANT_String, // 8 (Java 1.0.2+)
        CONSTANT_Fieldref, // 9 (Java 1.0.2+)
        CONSTANT_Methodref, // 10 (Java 1.0.2+)
        CONSTANT_InterfaceMethodref, // 11 (Java 1.0.2+)
        CONSTANT_NameAndType, // 12 (Java 1.0.2+)
        unused_13,
        unused_14,
        CONSTANT_MethodHandle, // 15 (Java 7+)
        CONSTANT_MethodType, // 16 (Java 7+)
        CONSTANT_Dynamic, // 17 (Java 11+)
        CONSTANT_InvokeDynamic, // 18 (Java 7+)
        CONSTANT_Module, // 19 (Java 9+)
        CONSTANT_Package; // 20 (Java 9+)
        private static final MyTag[] VALUES = MyTag.values();
        public static MyTag valueOf(int ordinal) {
            return VALUES[ordinal];
        }
    }
    
    private static final int ACC_PUBLIC = 0x0001;
    private static final int ACC_PROTECTED = 0x0004;

    /*
     * Attributes by version, to ignore them if they are not
     * predefined ones but user defined ones.
     */
    
    private static final String ATTR_ConstantValue = "ConstantValue";
    private static final String ATTR_Code = "Code";
    private static final String ATTR_Exceptions = "Exceptions";
    private static final String ATTR_SourceFile = "SourceFile";
    private static final String ATTR_LineNumberTable = "LineNumberTable";
    private static final String ATTR_LocalVariableTable = "LocalVariableTable";
    private static final String ATTR_InnerClasses = "InnerClasses";
    private static final String ATTR_Synthetic = "Synthetic";
    private static final String ATTR_Deprecated = "Deprecated";
    private static final String ATTR_EnclosingMethod = "EnclosingMethod";
    private static final String ATTR_Signature = "Signature";
    private static final String ATTR_SourceDebugExtension = "SourceDebugExtension";
    private static final String ATTR_LocalVariableTypeTable = "LocalVariableTypeTable";
    private static final String ATTR_RuntimeVisibleAnnotations = "RuntimeVisibleAnnotations";
    private static final String ATTR_RuntimeInvisibleAnnotations = "RuntimeInvisibleAnnotations";
    private static final String ATTR_RuntimeVisibleParameterAnnotations = "RuntimeVisibleParameterAnnotations";
    private static final String ATTR_RuntimeInvisibleParameterAnnotations = "RuntimeInvisibleParameterAnnotations";
    private static final String ATTR_AnnotationDefault = "AnnotationDefault";
    private static final String ATTR_StackMapTable = "StackMapTable";
    private static final String ATTR_BootstrapMethods = "BootstrapMethods";
    private static final String ATTR_RuntimeVisibleTypeAnnotations = "RuntimeVisibleTypeAnnotations";
    private static final String ATTR_RuntimeInvisibleTypeAnnotations = "RuntimeInvisibleTypeAnnotations";
    private static final String ATTR_MethodParameters = "MethodParameters";
    private static final String ATTR_Module = "Module";
    private static final String ATTR_ModulePackages = "ModulePackages";
    private static final String ATTR_ModuleMainClass = "ModuleMainClass";
    private static final String ATTR_NestHost = "NestHost";
    private static final String ATTR_NestMembers = "NestMembers";
    private static final String ATTR_Record = "Record";
    private static final String ATTR_PermittedSubclasses = "PermittedSubclasses";
    
    /**
     * Java version for major versions:
     * Java 4..20 : javaVersion + 44
     * ex.:
     * 49: Java 5
     * (...)
     * 54: Java 10
     * (...)
     * 59: Java 15
     * (...)
     * 64: Java 20
     */
    private static final Map<String,Long> FIRST_VERSION_BY_ATTR_NAME;
    static {
        final Map<String,Long> map = new HashMap<String,Long>();
        map.put(ATTR_ConstantValue, classVersion(45,3));
        map.put(ATTR_Code, classVersion(45,3));
        map.put(ATTR_Exceptions, classVersion(45,3));
        map.put(ATTR_SourceFile, classVersion(45,3));
        map.put(ATTR_LineNumberTable, classVersion(45,3));
        map.put(ATTR_LocalVariableTable, classVersion(45,3));
        map.put(ATTR_InnerClasses, classVersion(45,3));
        map.put(ATTR_Synthetic, classVersion(45,3));
        map.put(ATTR_Deprecated, classVersion(45,3));
        map.put(ATTR_EnclosingMethod, classVersion(49,0));
        map.put(ATTR_Signature, classVersion(49,0));
        map.put(ATTR_SourceDebugExtension, classVersion(49,0));
        map.put(ATTR_LocalVariableTypeTable, classVersion(49,0));
        map.put(ATTR_RuntimeVisibleAnnotations, classVersion(49,0));
        map.put(ATTR_RuntimeInvisibleAnnotations, classVersion(49,0));
        map.put(ATTR_RuntimeVisibleParameterAnnotations, classVersion(49,0));
        map.put(ATTR_RuntimeInvisibleParameterAnnotations, classVersion(49,0));
        map.put(ATTR_AnnotationDefault, classVersion(49,0));
        map.put(ATTR_StackMapTable, classVersion(50,0));
        map.put(ATTR_BootstrapMethods, classVersion(51,0));
        map.put(ATTR_RuntimeVisibleTypeAnnotations, classVersion(52,0));
        map.put(ATTR_RuntimeInvisibleTypeAnnotations, classVersion(52,0));
        map.put(ATTR_MethodParameters, classVersion(52,0));
        map.put(ATTR_Module, classVersion(53,0));
        map.put(ATTR_ModulePackages, classVersion(53,0));
        map.put(ATTR_ModuleMainClass, classVersion(53,0));
        map.put(ATTR_NestHost, classVersion(55,0));
        map.put(ATTR_NestMembers, classVersion(55,0));
        map.put(ATTR_Record, classVersion(60,0));
        map.put(ATTR_PermittedSubclasses, classVersion(61,0));

        FIRST_VERSION_BY_ATTR_NAME = Collections.unmodifiableMap(map);
    }

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /**
     * Computes dependencies and class name of the specified class file.
     * 
     * Provided class names are in their internal form, e.g. "java/lang/Object".
     * 
     * If the specified collection implements java.util.Set interface, then a
     * same class name might be added multiple times into it, else this method
     * takes care to only add each class name once (but even if it was already
     * in the specified collection before this call).
     * 
     * The order in which names are added in the specified collection is
     * undefined, to allow for maximum performances.
     * If wanting deterministic results, you can use a TreeSet.
     * 
     * @param classFile File object corresponding to a class file.
     * @param apiOnly If true, only takes into account dependencies from API,
     *        i.e. essentially public and protected fields, methods and
     *        class signatures.
     * @param depInternalClassNameColl Collection where to add internal class
     *        names of classes the specified class depends on.
     * @return The internal class name of the specified class, or null if the
     *         specified stream did not start with 0xCAFEBABE.
     * @throws RuntimeException wrapping an IOException if any is thrown.
     */
    public static String computeDependencies(
            File classFile,
            boolean apiOnly,
            Collection<String> depInternalClassNameColl) {
        final FileInputStream fis;
        try {
            fis = new FileInputStream(classFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            return computeDependencies(
                    fis,
                    apiOnly,
                    depInternalClassNameColl);
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    /**
     * Computes dependencies and class name of the specified class file.
     * 
     * Provided class names are in their internal form, e.g. "java/lang/Object".
     * 
     * If the specified collection implements java.util.Set interface, then a
     * same class name might be added multiple times into it, else this method
     * takes care to only add each class name once (but even if it was already
     * in the specified collection before this call).
     * 
     * The order in which names are added in the specified collection is
     * undefined, to allow for maximum performances.
     * If wanting deterministic results, you can use a TreeSet.
     * 
     * If the specified input stream is not an instance of BufferedInputStream,
     * adds intermediary buffering.
     * 
     * @param classFileInputStream Input stream corresponding to a class file.
     * @param apiOnly If true, only takes into account dependencies from API,
     *        i.e. essentially public and protected fields, methods and
     *        class signatures.
     * @param depInternalClassNameColl Collection where to add internal class
     *        names of classes the specified class depends on.
     * @return The internal class name of the specified class, or null if the
     *         specified stream did not start with 0xCAFEBABE.
     * @throws RuntimeException wrapping an IOException if any is thrown.
     */
    public static String computeDependencies(
            InputStream classFileInputStream,
            boolean apiOnly,
            Collection<String> depInternalClassNameColl) {
        final BufferedInputStream bis;
        if (classFileInputStream instanceof BufferedInputStream) {
            bis = (BufferedInputStream)classFileInputStream;
        } else {
            bis = new BufferedInputStream(classFileInputStream);
        }
        try {
            final DataInputStream dis = new DataInputStream(bis);
            try {
                return computeDependencies(
                        (DataInput)dis,
                        apiOnly,
                        depInternalClassNameColl);
            } finally {
                try {
                    dis.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } finally {
            try {
                // Only closing if it's ours.
                if (bis != classFileInputStream) {
                    bis.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Computes dependencies and class name of the specified class file.
     * 
     * Provided class names are in their internal form, e.g. "java/lang/Object".
     * 
     * If the specified collection implements java.util.Set interface, then a
     * same class name might be added multiple times into it, else this method
     * takes care to only add each class name once (but even if it was already
     * in the specified collection before this call).
     * 
     * The order in which names are added in the specified collection is
     * undefined, to allow for maximum performances.
     * If wanting deterministic results, you can use a TreeSet.
     * 
     * Reads the specified DataInput directly, without adding buffering.
     * 
     * @param classFileDataInput Data input corresponding to a class file.
     * @param apiOnly If true, only takes into account dependencies from API,
     *        i.e. essentially public and protected fields, methods and
     *        class signatures.
     * @param depInternalClassNameColl Collection where to add internal class
     *        names of classes the specified class depends on.
     * @return The internal class name of the specified class, or null if the
     *         specified stream did not start with 0xCAFEBABE.
     * @throws RuntimeException wrapping an IOException if any is thrown.
     */
    public static String computeDependencies(
            DataInput classFileDataInput,
            boolean apiOnly,
            Collection<String> depInternalClassNameColl) {
        final MyData data = new MyData(apiOnly);
        final boolean okSoFar = parseClassFile(
                classFileDataInput,
                data);
        if (DEBUG) {
            System.out.println("ok after parsing = " + okSoFar);
        }
        if (!okSoFar) {
            return null;
        }
        
        // Already logged when parsed index.
        final String thisClassName = data.classNameOfClassIndex(data.thisClassIndex);
        if (thisClassName == null) {
            throw new IllegalArgumentException("null class name for this");
        }
        
        if (data.apiOnly
                && ((!isApi(data.class_access_flags))
                        || data.foundNonApiClassAbove)) {
            if (DEBUG) {
                System.out.println("not an API class : no API dependencies");
            }
            return thisClassName;
        }

        for (int classIndex : data.depClassesIndexes) {
            if (classIndex == data.thisClassIndex) {
                // Optional, since we remove "this class name"
                // at the end.
                continue;
            }
            if (DEBUG) {
                System.out.print("dep classIndex = " + classIndex);
            }
            final String depClassName = data.classNameOfClassIndex(classIndex);
            if (DEBUG) {
                System.out.println(", className = " + depClassName);
            }
            addClassNameEventuallyFromArrayClassNameInto(
                    depClassName,
                    data.dependencySet);
        }

        for (int utf8Index : data.depDescriptorsUtf8Indexes) {
            if (DEBUG) {
                System.out.print("dep utf8Index = " + utf8Index);
            }
            final String descriptor = data.stringOfUtf8Index(utf8Index);
            if (DEBUG) {
                System.out.println(", descriptor = " + descriptor);
            }
            addClassNamesFromDescriptorInto(
                    descriptor,
                    data.dependencySet);
        }
        
        for (int utf8Index : data.depSignaturesUtf8Indexes) {
            if (DEBUG) {
                System.out.print("dep utf8Index = " + utf8Index);
            }
            final String signature = data.stringOfUtf8Index(utf8Index);
            if (DEBUG) {
                System.out.println(", signature = " + signature);
            }
            addClassNamesFromSignatureInto(
                    signature,
                    data.dependencySet);
        }

        /*
         * Making sure we don't pretend to depend on ourselves.
         */
        
        data.dependencySet.remove(thisClassName);

        /*
         * 
         */

        depInternalClassNameColl.addAll(data.dependencySet);
        
        return thisClassName;
    }

    //--------------------------------------------------------------------------
    // PACKAGE-PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    /**
     * We don't consider arrays classes names as dependencies, only the class
     * name of their element type if it's not primitive.
     */
    static void addClassNameEventuallyFromArrayClassNameInto(
            String classNameOrArrayClassName,
            Collection<String> classNameColl) {
        if (classNameOrArrayClassName.startsWith("[")) {
            // An array class name.
            // If does not contain 'L', must be an array of primitive type.
            final int index = classNameOrArrayClassName.indexOf('L');
            if (index >= 0) {
                // Removing "[[*L" prefix and ";" suffix.
                final String extracted = classNameOrArrayClassName.substring(index+1, classNameOrArrayClassName.length()-1);
                if (extracted.length() == 0) {
                    throw new IllegalArgumentException("empty class name in array class name " + classNameOrArrayClassName);
                }
                addClassName(
                        classNameColl,
                        extracted);
            }
        } else {
            addClassName(
                    classNameColl,
                    classNameOrArrayClassName);
        }
    }

    /**
     * Can add a same class name multiple times.
     * 
     * @param descriptor Similar to signature, but does not contain generic
     *        types.
     */
    static void addClassNamesFromDescriptorInto(
            String descriptor,
            Collection<String> classNameColl) {
        int mark = 0;
        while ((mark = descriptor.indexOf('L',mark)) >= 0) {
            final int from = mark+1;
            final int toExcl = descriptor.indexOf(';',from+1);
            if (toExcl < 0) {
                throw new IllegalArgumentException(
                        "type name starting at "
                        + from
                        + " but not ending : "
                        + descriptor);
            }
            final String className = descriptor.substring(from, toExcl);
            if (className.length() == 0) {
                throw new IllegalArgumentException("empty class name in " + descriptor);
            }
            addClassName(
                    classNameColl,
                    className);
            mark = toExcl+1;
        }
    }

    /**
     * Can add a same class name multiple times.
     * 
     * @param signature Similar to descriptor, but contains generic types.
     */
    static void addClassNamesFromSignatureInto(
            String signature,
            Collection<String> classNameColl) {
        /*
         * A bit more complicated than the descriptor parsing, because:
         * - Classes names can end with '<' if generic.
         * - 'L' might appear in generic types.
         * - Generic types are either preceded by 'T', or followed by ':'.
         * - '.' might separate a top level class signature from one of its
         *   nested classes signatures.
         */
        final int length = signature.length();
        // Reading class name (or maybe some generic type) if cnFrom >= 0.
        int cnFrom = -1;
        // Skipping generic type if gtFrom >= 0.
        int gtFrom = -1;
        // Skipping nested class simple name if icsnFrom >= 0.
        int icsnFrom = -1;
        for (int i = 0; i < length; i++) {
            final char c = signature.charAt(i);
            if (cnFrom >= 0) {
                if (c == ':') {
                    // We were actually reading some generic type, that
                    // contains 'L' (which is why we thought it was a class name).
                    cnFrom = -1;
                } else {
                    // If a nested class simple name appears, we stop, since
                    // nested classes names appear in classes descriptions
                    // anyway.
                    final boolean stop = (c == ';') || (c == '<') || (c == '.');
                    if (stop) {
                        final String className = signature.substring(cnFrom, i);
                        if (className.length() == 0) {
                            throw new IllegalArgumentException("empty class name in " + signature);
                        }
                        addClassName(
                                classNameColl,
                                className);
                        cnFrom = -1;
                        // If we stopped on a nested class simple name, we need
                        // to take care to skip it, for it might contain 'L' and
                        // we could start parsing what we would think is a class
                        // name.
                        if (c == '.') {
                            icsnFrom = i+1;
                        }
                    }
                }
            } else if (gtFrom >= 0) {
                // Generic types can only end with ':' when they are not
                // preceded by 'T', and we initiate this case when we encounter
                // a 'T', but a generic type might itself start with 'T', so
                // trigger this case, so we need to check for terminating ':'.
                final boolean stop = (c == ';') || (c == '<') || (c == ':');
                if (stop) {
                    // We are done skipping generic type.
                    gtFrom = -1;
                }
            } else if (icsnFrom >= 0) {
                final boolean stop = (c == ';') || (c == '<');
                if (stop) {
                    // We are done skipping nested class simple name.
                    icsnFrom = -1;
                }
            } else {
                if (c == 'L') {
                    cnFrom = i+1;
                } else if (c == 'T') {
                    gtFrom = i+1;
                } else if (c == '.') {
                    icsnFrom = i+1;
                }
            }
        }
        if (cnFrom >= 0) {
            throw new IllegalArgumentException(
                    "class name starting at "
                    + cnFrom
                    + " but not ending : "
                    + signature);
        }
        if (gtFrom >= 0) {
            throw new IllegalArgumentException(
                    "generic type name starting at "
                    + gtFrom
                    + " but not ending : "
                    + signature);
        }
        if (icsnFrom >= 0) {
            throw new IllegalArgumentException(
                    "nested class simple name starting at "
                    + icsnFrom
                    + " but not ending : "
                    + signature);
        }
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    private ClassDepsParser() {
    }
    
    /*
     * 
     */
    
    /**
     * @param data (in,out)
     * @return True if the input started with 0xCAFEBABE, i.e. corresponds to a
     *         ".class" file, false otherwise.
     */
    private static boolean parseClassFile(
            DataInput dataInput,
            MyData data) {
        
        final long magic = getU4ElseZero(dataInput);
        if ((int)magic != 0xCAFEBABE) {
            if (DEBUG) {
                System.out.println();
                System.out.println("parseClassFile : magic = 0x" + Long.toHexString(magic));
            }
            return false;
        }

        if (DEBUG) {
            System.out.println();
            System.out.println("parseClassFile : apiOnly = " + data.apiOnly);
        }
        
        final int minor_version = getU2(dataInput);
        final int major_version = getU2(dataInput);
        if (DEBUG) {
            System.out.println("major_version = " + major_version);
            System.out.println("minor_version = " + minor_version);
        }
        data.classVersion = classVersion(major_version, minor_version);
        
        process_constant_pool(
                dataInput,
                data);

        final int access_flags = getU2(dataInput);
        data.class_access_flags = access_flags;

        // this_class
        data.thisClassIndex = getU2(dataInput);
        
        if (DEBUG) {
            System.out.println("thisClassIndex = " + data.thisClassIndex + " (" + data.classNameOfClassIndex(data.thisClassIndex) + ")");
        }

        if (data.apiOnly && (!isApi(access_flags))) {
            // No API dependency for non-API classes.
            // Need to be checked again upon return.
            return true;
        }
        
        if (data.apiOnly) {
            if (DEBUG) {
                System.out.println("parsing super class...");
            }
            final int super_class = getU2(dataInput);
            if (super_class == 0) {
                // Must be Object class (not checking that).
            } else {
                addDepClassIndex(data, super_class);
            }
        } else {
            if (DEBUG) {
                System.out.println("skipping super class...");
            }
            // Can skip because is added by brutal add.
            // Skipping super_class.
            skip(dataInput, 2);
        }

        final int interfaces_count = getU2(dataInput);
        if (data.apiOnly) {
            if (DEBUG) {
                System.out.println("parsing interfaces...");
            }
            // Indexes of CONSTANT_Class_info structures.
            for (int i = 0; i < interfaces_count; i++) {
                final int interfaceClassIndex = getU2(dataInput);
                addDepClassIndex(data, interfaceClassIndex);
            }
        } else {
            if (DEBUG) {
                System.out.println("skipping interfaces...");
            }
            // Can skip because is added by brutal add.
            // Skipping interfaces = interfaces_count * (interface [u2]).
            skip(dataInput, interfaces_count * 2);
        }

        if (DEBUG) {
            System.out.println("parsing fields...");
        }

        final int field_count = getU2(dataInput);
        
        for (int i  =0; i < field_count; i++) {
            process_field_info_or_method_info(
                    dataInput,
                    data);
        }
        
        if (DEBUG) {
            System.out.println("parsing methods...");
        }
        
        final int method_count = getU2(dataInput);
        
        for (int i = 0; i < method_count; i++) {
            process_field_info_or_method_info(
                    dataInput,
                    data);
        }

        if (DEBUG) {
            System.out.println("parsing attributes...");
        }

        final int attribute_count = getU2(dataInput);

        for (int i = 0; i < attribute_count; i++) {
            process_attribute_info(
                    dataInput,
                    data);
        }
        
        return true;
    }

    /*
     * Constant pool.
     */

    private static void process_constant_pool(
            DataInput dataInput,
            MyData data) {
        final int constant_pool_count = getU2(dataInput);
        // Starts at 1, but must still be inferior to count.
        for (int index = 1; index < constant_pool_count; index++) {
            final int tagOrdinal = getU1(dataInput);
            if (tagOrdinal >= MyTag.VALUES.length) {
                throw new RuntimeException("unknown tag : " + tagOrdinal);
            }
            
            final MyTag tag = MyTag.valueOf(tagOrdinal);
            if (DEBUG) {
                System.out.println("tag = " + tagOrdinal + " = " + tag);
            }
            switch (tag) {
            case CONSTANT_Utf8: {
                process_CONSTANT_Utf8(
                        dataInput,
                        data,
                        index);
            } break;
            
            case CONSTANT_Integer:
            case CONSTANT_Float: {
                skip(dataInput, 4);
            } break;
            
            case CONSTANT_Long:
            case CONSTANT_Double: {
                skip(dataInput, 8);
                // 8-byte entries take two indexes.
                index++;
            } break;

            case CONSTANT_Class: {
                process_CONSTANT_Class(
                        dataInput,
                        data,
                        index);
            } break;
            
            case CONSTANT_String: {
                skip(dataInput, 2);
            } break;
            
            case CONSTANT_Fieldref:
            case CONSTANT_Methodref:
            case CONSTANT_InterfaceMethodref: {
                skip(dataInput, 4);
            } break;
            
            case CONSTANT_NameAndType: {
                process_CONSTANT_TypeAndName(
                        dataInput,
                        data,
                        index);
            } break;
            
            case CONSTANT_MethodHandle: {
                skip(dataInput, 3);
            } break;
            
            case CONSTANT_MethodType: {
                process_CONSTANT_MethodType(
                        dataInput,
                        data,
                        index);
            } break;
            
            case CONSTANT_Dynamic:
            case CONSTANT_InvokeDynamic: {
                skip(dataInput, 4);
            } break;
            
            case CONSTANT_Module:
            case CONSTANT_Package: {
                skip(dataInput, 2);
            } break;

            default:
                // Can happen, for unused_XXX enum values.
                throw new RuntimeException("unknown tag : " + tag);
            }
        }
    }
    
    private static void process_CONSTANT_Utf8(
            DataInput dataInput,
            MyData data,
            int index) {
        // Reading in modified UTF-8.
        String string;
        try {
            string = dataInput.readUTF();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (DEBUG) {
            System.out.println("pool[" + index + "] : utf8 = " + string);
        }
        data.stringByUtf8Index.put(index, string);
    }
    
    private static void process_CONSTANT_Class(
            DataInput dataInput,
            MyData data,
            int index) {
        final int name_index = getU2(dataInput);
        if (DEBUG) {
            System.out.println("pool[" + index + "] : class name utf8 index = " + name_index);
        }
        data.classNamesUtf8IndexByClassIndex.put(index, name_index);
        if (!data.apiOnly) {
            // Brutal add.
            addDepClassIndex(data, index);
        }
    }
    
    private static void process_CONSTANT_TypeAndName(
            DataInput dataInput,
            MyData data,
            int index) {
        // Skipping name_index.
        skip(dataInput, 2);
        
        final int descriptor_index = getU2(dataInput);
        if (DEBUG) {
            System.out.println("pool[" + index + "] : descriptor (TAN) utf8 index = " + descriptor_index);
        }
        if (!data.apiOnly) {
            // Brutal add.
            addDepDescriptorIndex(data, descriptor_index);
        }
    }

    private static void process_CONSTANT_MethodType(
            DataInput dataInput,
            MyData data,
            int index) {
        final int descriptor_index = getU2(dataInput);
        if (DEBUG) {
            System.out.println("pool[" + index + "] : descriptor (MT) utf8 index = " + descriptor_index);
        }
        if (!data.apiOnly) {
            // Brutal add.
            addDepDescriptorIndex(data, descriptor_index);
        }
    }
    
    /*
     * 
     */

    /**
     * field_info and method_info have same structure, except
     * for the types of attributes they can contain: this method
     * handles any.
     */
    private static void process_field_info_or_method_info(
            DataInput dataInput,
            MyData data) {
        final int access_flags = getU2(dataInput);
        
        final boolean useIt = (!data.apiOnly) || isApi(access_flags);
        
        if (DEBUG) {
            final int name_index = getU2(dataInput);
            System.out.println("name_index = " + name_index + " (useIt = " + useIt + ")");
        } else {
            // Skipping name_index [u2].
            skip(dataInput, 2);
        }
        
        final int descriptor_index = getU2(dataInput);
        if (DEBUG) {
            System.out.println("descriptor (fOrM) utf8 index = " + descriptor_index);
        }
        if (useIt) {
            addDepDescriptorIndex(data, descriptor_index);
        }
        
        final int attributes_count = getU2(dataInput);
        for (int i = 0; i < attributes_count; i++) {
            if (useIt) {
                process_attribute_info(
                        dataInput,
                        data);
            } else {
                skip_attribute_info(dataInput);
            }
        }
    }
    
    /*
     * Attributes.
     */

    private static void process_attribute_info(
        DataInput dataInput,
        MyData data) {
        
        // These two are read here (common for all attributes),
        // so need not to read them again while reading attributes.
        final int attribute_name_index = getU2(dataInput);
        if (DEBUG) {
            System.out.println("attribute_name_index = " + attribute_name_index);
        }
        
        final long attribute_length = getU4(dataInput);
        if (DEBUG) {
            System.out.println("attribute_length = " + attribute_length);
        }

        // Utf8 constants have all been read at this point,
        // so name must not be null.
        final String name = data.stringOfUtf8Index(attribute_name_index);

        final boolean isPredef = isPredefAttr(data.classVersion, name);
        
        if (DEBUG) {
            if (isPredef) {
                System.out.println("attribute name = " + name);
            } else {
                System.out.println("attribute name = " + name + " (not predefined : will ignore)");
            }
        }
        
        boolean attrProcessed = false;

        if (!isPredef) {
            // User attribute: skipping.
            
        } else if (name.equals(ATTR_Signature)) {
            process_Signature_attributeBody(
                    dataInput,
                    data);
            attrProcessed = true;

        } else if (name.equals(ATTR_InnerClasses)) {
            if (data.apiOnly) {
                process_InnerClasses_attributeBody(
                        dataInput,
                        data);
                attrProcessed = true;
            } else {
                // Can skip, no need to check outer classes access flags.
            }
            
        } else if (name.equals(ATTR_Exceptions)) {
            if (data.apiOnly) {
                // Needed to detect thrown exceptions in method signatures.
                process_Exceptions_attributeBody(
                        dataInput,
                        data);
                attrProcessed = true;
            } else {
                // Can skip, already detected when parsing constant pool.
            }
            
        } else if (name.equals(ATTR_Code)) {
            if (data.apiOnly) {
                // Code is never API.
            } else {
                // Needed to detect class names of parameters of code annotations
                // that make it into the class file, such as annotations of
                // ElementType.TYPE_USE target.
                process_Code_attributeBody(
                        dataInput,
                        data);
                attrProcessed = true;
            }
            
        } else if (name.equals(ATTR_RuntimeVisibleAnnotations)
                || name.equals(ATTR_RuntimeInvisibleAnnotations)) {
            if (ANNOS_NOT_IN_API && data.apiOnly) {
                // No dep to annotations (when used as annotations).
            } else {
                // Needed to detect annotations in signatures
                // or other basic usages.
                process_annotations(dataInput, data);
                attrProcessed = true;
            }

        } else if (name.equals(ATTR_RuntimeVisibleParameterAnnotations)
                || name.equals(ATTR_RuntimeInvisibleParameterAnnotations)) {
            if (ANNOS_NOT_IN_API && data.apiOnly) {
                // No dep to annotations (when used as annotations).
            } else {
                // Needed to detect annotations of ElementType.PARAMETER target.
                process_parameter_annotations(dataInput, data);
                attrProcessed = true;
            }
            
        } else if (name.equals(ATTR_RuntimeVisibleTypeAnnotations)
                || name.equals(ATTR_RuntimeInvisibleTypeAnnotations)) {
            if (ANNOS_NOT_IN_API && data.apiOnly) {
                // No dep to annotations (when used as annotations).
            } else {
                // Needed to detect annotations of ElementType.TYPE_PARAMETER
                // and ElementType.TYPE_USE targets.
                process_type_annotations(dataInput, data);
                attrProcessed = true;
            }

        } else if (name.equals(ATTR_AnnotationDefault)) {
            // Needed to detect default parameters values
            // when parsing annotations.
            process_element_value(dataInput, data);
            attrProcessed = true;
            
        } else if (name.equals(ATTR_ModuleMainClass)) {
            /*
             * Someone might be interested in this dependency.
             */
            process_ModuleMainClass_attributeBody(
                dataInput,
                data);
            attrProcessed = true;
            
        } else if (name.equals(ATTR_Record)) {
            /*
             * We actually don't seem to need to parse Record attribute,
             * since it can only contain (predefined) attributes
             * RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations,
             * RuntimeVisibleTypeAnnotations, RuntimeInvisibleTypeAnnotations,
             * and Signature (cf. "Table 4.7-C"),
             * all of them being already parsed aside,
             * but we still parse the record,
             * in case there could be an actual reason to.
             */
            process_Record_attributeBody(
                dataInput,
                data);
            attrProcessed = true;
            
        } else {
            /*
             * Not of interest.
             * 
             * Not considering PermittedSubclasses
             * as a dependency (else it would cause cycles
             * whenever sealed classes are used,
             * not allowing for clean cycleless code).
             */
        }
        
        if (!attrProcessed) {
            if (DEBUG) {
                System.out.println("skipping attribute : " + name);
            }
            // Skipping attribute.
            skip(dataInput, attribute_length);
        }
    }

    private static void skip_attribute_info(DataInput dataInput) {
        final int attribute_name_index = getU2(dataInput);
        final long attribute_length = getU4(dataInput);
        if (DEBUG) {
            System.out.println("skipping attribute : attribute_name_index = " + attribute_name_index);
        }
        // Skipping attribute.
        skip(dataInput, attribute_length);
    }

    private static void process_element_value(
            DataInput dataInput,
            MyData data) {
        
        final char tag = (char) getU1(dataInput);
        if (tag == '[') {
            // array_value
            process_array_value(dataInput, data);
        } else if (tag == '@') {
            // annotation_value
            process_annotation(dataInput, data);
        } else if (tag == 'c') {
            final int class_info_index = getU2(dataInput);
            addDepDescriptorIndex(data, class_info_index);
        } else if (tag == 'e') {
            // enum_const_value
            process_enum_const_value(dataInput, data);
        } else {
            // const_value_index [u2]: for a primitive type
            // or String type (if tag == 's') constant.
            // Dependency to String is taken care of already
            // in process_field_info_or_method_info(...),
            // so we don't do anything special about that here
            // (same for ConstantValue_attribute).
            skip(dataInput, 2);
        }
    }
    
    private static void process_array_value(
            DataInput dataInput,
            MyData data) {
        final int num_values = getU2(dataInput);
        for (int i = 0; i < num_values; i++) {
            process_element_value(
                    dataInput,
                    data);
        }
    }
    
    private static void process_annotations(
            DataInput dataInput,
            MyData data) {
        final int num_annotations = getU2(dataInput);
        for (int i = 0; i < num_annotations; i++) {
            process_annotation(dataInput, data);
        }
    }
    
    private static void process_annotation(
            DataInput dataInput,
            MyData data) {
        final int type_index = getU2(dataInput);
        addDepDescriptorIndex(data, type_index);
        
        final int num_element_value_pairs = getU2(dataInput);
        for (int i = 0; i < num_element_value_pairs; i++) {
            // Skipping element_name_index [u2].
            skip(dataInput, 2);
            process_element_value(dataInput, data);
        }
    }
    
    private static void process_parameter_annotations(
            DataInput dataInput,
            MyData data) {
        final int num_parameters = getU1(dataInput);
        for (int i = 0; i < num_parameters; i++) {
            process_annotations(dataInput, data);
        }
    }
    
    private static void process_type_annotations(
            DataInput dataInput,
            MyData data) {
        final int num_annotations = getU2(dataInput);
        for (int i = 0; i < num_annotations; i++) {
            process_type_annotation(dataInput, data);
        }
    }
    
    private static void process_type_annotation(
            DataInput dataInput,
            MyData data) {
        
        final int target_type = getU1(dataInput);
        if ((target_type >= 0x00) && (target_type <= 0x01)) {
            // type_parameter_target
            // Skipping type_parameter_index [u1].
            skip(dataInput, 1);
            
        } else if (target_type == 0x10) {
            // supertype_target
            // Skipping supertype_index [u2].
            skip(dataInput, 2);
            
        } else if ((target_type >= 0x11) && (target_type <= 0x12)) {
            // type_parameter_bound_target
            // Skipping type_parameter_index [u1]
            // and bound_index [u1].
            skip(dataInput, 2);
            
        } else if ((target_type >= 0x13) && (target_type <= 0x15)) {
            // empty_target (really empty!)
            
        } else if (target_type == 0x16) {
            // method_formal_parameter_target / formal_parameter_target
            // Skipping formal_parameter_index [u1].
            skip(dataInput, 1);
            
        } else if (target_type == 0x17) {
            // throws_target
            // Skipping throws_type_index [u2].
            skip(dataInput, 2);
            
        } else if ((target_type >= 0x40) && (target_type <= 0x41)) {
            // localvar_target
            final int table_length = getU2(dataInput);
            // Skipping
            // table_length
            // * (start_pc [u2]
            //    + length [u2]
            //    + index [u2]).
            skip(dataInput, table_length * 6L);
            
        } else if (target_type == 0x42) {
            // catch_target
            // Skipping exception_table_index [u2].
            skip(dataInput, 2);
            
        } else if ((target_type >= 0x43) && (target_type <= 0x46)) {
            // offset_target
            // Skipping offset [u2]
            // and type_argument_index [u1].
            skip(dataInput, 3);
            
        } else if ((target_type >= 0x47) && (target_type <= 0x4B)) {
            // type_argument_target
            final int path_length = getU1(dataInput);
            // Skipping
            // path_length
            // * (type_path_kind [u1]
            //    + type_argument_index [u1]).
            skip(dataInput, path_length * 2);
            
        } else {
            // We would like to ignore it, but don't know how much to skip.
            throw new IllegalArgumentException("unknown target_type: " + target_type);
        }
        
        // type_path target_path
        // Indicates what is annotated: we don't care.
        {
            final int path_length = getU1(dataInput);
            // Skipping
            // path_length
            // * (type_path_kind [u1]
            //    + type_argument_index [u1]).
            skip(dataInput, path_length * 2);
        }
        
        process_annotation(dataInput, data);
    }
    
    /*
     * 
     */
    
    private static void process_enum_const_value(
            DataInput dataInput,
            MyData data) {
        // Descriptor for name of the enum type.
        final int type_name_index = getU2(dataInput);
        addDepDescriptorIndex(data, type_name_index);
        
        // Skipping const_name_index [u2].
        skip(dataInput, 2);
    }
    
    private static void process_Signature_attributeBody(
            DataInput dataInput,
            MyData data) {
        final int signature_index = getU2(dataInput);
        if (DEBUG) {
            System.out.println("Signature attr : signature utf8 index = " + signature_index);
        }
        addDepSignatureIndex(data, signature_index);
    }
    
    /**
     * Never need to add dependency to a nested class from here, since:
     * - if API only, dependency is added when needed when parsing
     *   API signatures etc.
     * - if not API only, dependency is added while parsing constant pool.
     * 
     * But if API only we still need to parse this attribute
     * to check access flags of outer classes.
     */
    private static void process_InnerClasses_attributeBody(
            DataInput dataInput,
            MyData data) {
        final int number_of_classes = getU2(dataInput);
        
        final String thisClassName = data.classNameOfClassIndex(data.thisClassIndex);
        
        for (int i = 0; i < number_of_classes; i++) {
            final int inner_class_info_index = getU2(dataInput);
            final int outer_class_info_index = getU2(dataInput);
            // 0 if anonymous inner class, so we don't count
            // on it to retrieve the name.
            final int inner_name_index = getU2(dataInput);
            final int inner_class_access_flags = getU2(dataInput);
            
            if (DEBUG) {
                System.out.println("inner_class_info_index = " + inner_class_info_index);
                System.out.println("outer_class_info_index = " + outer_class_info_index);
                System.out.println("inner_name_index = " + inner_name_index);
                System.out.println("inner_class_access_flags = " + inner_class_access_flags + " (isApi = " + isApi(inner_class_access_flags) + ")");
            }

            final String innerClassName = data.classNameOfClassIndex(inner_class_info_index);
            
            final boolean innerIsAnOuterOfThis =
                    (innerClassName.length() < thisClassName.length())
                    && thisClassName.startsWith(innerClassName)
                    && (thisClassName.charAt(innerClassName.length()) == '$');
            
            if (!data.foundNonApiClassAbove) {
                if (innerIsAnOuterOfThis) {
                    // The inner class is one of our outer classes
                    // (but the top level class never appears here,
                    // so it's always assumed public for APIness computation).
                    if (!isApi(inner_class_access_flags)) {
                        data.foundNonApiClassAbove = true;
                    }
                }
            }
        }
    }
    
    private static void process_ModuleMainClass_attributeBody(
        DataInput dataInput,
        MyData data) {
        
        final int main_class_index = getU2(dataInput);
        if (DEBUG) {
            System.out.println("main_class_index = " + main_class_index);
        }

        addDepClassIndex(data, main_class_index);
    }
    
    private static void process_Record_attributeBody(
        DataInput dataInput,
        MyData data) {
        
        final int components_count = getU2(dataInput);
        if (DEBUG) {
            System.out.println("components_count = " + components_count);
        }
        
        for (int i = 0; i < components_count; i++) {
            process_record_component_info(
                dataInput, data);
        }
    }
    
    private static void process_record_component_info(
        DataInput dataInput,
        MyData data) {
        
        if (DEBUG) {
            final int name_index = getU2(dataInput);
            System.out.println("name_index = " + name_index);
            final int descriptor_index = getU2(dataInput);
            System.out.println("descriptor_index = " + descriptor_index);
        } else {
            skip(dataInput, 4);
        }
        
        final int attributes_count = getU2(dataInput);
        if (DEBUG) {
            System.out.println("attributes_count = " + attributes_count);
        }
        
        for (int i = 0; i < attributes_count; i++) {
            process_attribute_info(dataInput, data);
        }
    }
    
    /**
     * NB: Format spec says:
     * "The Exceptions attribute indicates which checked exceptions a method may throw."
     * but experimentally one can find that unchecked exceptions in throws clause
     * also appear here, and we count on it for dependencies computation of API methods.
     */
    private static void process_Exceptions_attributeBody(
            DataInput dataInput,
            MyData data) {
        final int number_of_exceptions = getU2(dataInput);
        for (int i = 0; i < number_of_exceptions; i++) {
            final int exception_class_index = getU2(dataInput);
            addDepClassIndex(data, exception_class_index);
        }
    }
    
    private static void process_Code_attributeBody(
            DataInput dataInput,
            MyData data) {
        
        // Skipping max_stack [u2]
        // and max_locals [u2].
        skip(dataInput, 4);
        
        final long code_length = getU4(dataInput);
        // Skipping code[code_length] ([u1] per element).
        skip(dataInput, code_length);
        
        final int exception_table_length = getU2(dataInput);
        // Skipping
        // exception_table_length
        // * (start_pc [u2]
        //    + end_pc [u2]
        //    + handler_pc [u2]
        //    + catch_type [u2]).
        // (catch_type, when != 0, contains an exception class index,
        // but these are already taken care of when parsing constant pool).
        skip(dataInput, exception_table_length * 8L);
        
        final int attributes_count = getU2(dataInput);
        for (int i = 0; i < attributes_count; i++) {
            process_attribute_info(
                    dataInput,
                    data);
        }
    }

    /*
     * Access flags.
     */
    
    private static boolean isApi(int access_flags) {
        return ((access_flags & (ACC_PUBLIC | ACC_PROTECTED)) != 0);
    }

    /*
     * 
     */
    
    private static void addDepClassIndex(
            MyData data,
            Integer classIndex) {
        if (DEBUG) {
            System.out.println("adding dep to classIndex = " + classIndex);
        }
        data.depClassesIndexes.add(classIndex);
    }
    
    private static void addDepDescriptorIndex(
            MyData data,
            Integer descriptorIndex) {
        if (DEBUG) {
            System.out.println("adding dep to descriptorIndex = " + descriptorIndex);
        }
        data.depDescriptorsUtf8Indexes.add(descriptorIndex);
    }
    
    private static void addDepSignatureIndex(
            MyData data,
            Integer signatureIndex) {
        if (DEBUG) {
            System.out.println("adding dep to signatureIndex = " + signatureIndex);
        }
        data.depSignaturesUtf8Indexes.add(signatureIndex);
    }
    
    /*
     * 
     */

    /**
     * Method for adding dependencies classes names.
     */
    private static void addClassName(
            Collection<String> classNameColl,
            String className) {
        if (DEBUG) {
            System.out.println("adding dependency to " + className);
        }
        classNameColl.add(className);
    }
    
    /*
     * Misc.
     */
    
    private static int getU1(DataInput dataInput) {
        int value;
        try {
            value = dataInput.readByte();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (value < 0) {
            value += (1<<8);
        }
        return value;
    }
    
    private static int getU2(DataInput dataInput) {
        int value;
        try {
            value = dataInput.readShort();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (value < 0) {
            value += (1<<16);
        }
        return value;
    }

    /**
     * @return An u4 value, or 0 if reached EOF before it could be read.
     */
    private static long getU4ElseZero(DataInput dataInput) {
        long value;
        try {
            value = dataInput.readInt();
        } catch (EOFException e) {
            return 0;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (value < 0) {
            value += (1L<<32);
        }
        return value;
    }

    private static long getU4(DataInput dataInput) {
        long value;
        try {
            value = dataInput.readInt();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (value < 0) {
            value += (1L<<32);
        }
        return value;
    }

    private static void skip(DataInput dataInput, int count) {
        final int forCheck;
        try {
            forCheck = dataInput.skipBytes(count);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (forCheck != count) {
            throw new AssertionError(forCheck + " != " + count);
        }
    }

    private static void skip(DataInput dataInput, long count) {
        while (count > 0L) {
            final int toSkip;
            if (count > Integer.MAX_VALUE) {
                toSkip = Integer.MAX_VALUE;
            } else {
                toSkip = (int) count;
            }
            count -= toSkip;
            skip(dataInput, toSkip);
        }
    }
    
    /*
     * 
     */
    
    /**
     * @return A long representation of class version,
     *         increasing as major.minor increases.
     */
    private static long classVersion(
            int major_version,
            int minor_version) {
        return (((long) major_version)<<32) + minor_version;
    }
    
    private static boolean isPredefAttr(
            long classVersion,
            String attrName) {
        final Long attrVersion = FIRST_VERSION_BY_ATTR_NAME.get(attrName);
        return (attrVersion != null)
                && (classVersion >= attrVersion.longValue());
    }
}
