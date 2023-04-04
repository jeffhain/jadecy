/*
 * Copyright 2015-2019 Jeff Hain
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
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import junit.framework.TestCase;
import net.jadecy.comp.JdcFsUtils;
import net.jadecy.parsing.test$.$;
import net.jadecy.parsing.test$.$$;
import net.jadecy.parsing.test$.$A;
import net.jadecy.parsing.test$.A$;
import net.jadecy.parsing.test$.A$$B;
import net.jadecy.parsing.testp.TestAnno1;
import net.jadecy.parsing.testp.TestAnno2;
import net.jadecy.tests.JdcTestCompHelper;
import net.jadecy.tests.JdcTestConfig;

public class ClassDepsParserTest extends TestCase {

    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------

    private static final boolean DEBUG = false;

    private static final String COMPILATION_OUTPUT_DIR_PATH =
            JdcTestCompHelper.ensureCompiledAndGetOutputDirPath(
                    Arrays.asList(
                            JdcTestCompHelper.MAIN_SRC_PATH,
                            JdcTestCompHelper.TEST_SRC_PATH));

    private static final boolean COMPARE_WITH_JDEPS = false;

    private static final String JAVA_HOME = JdcTestConfig.getJdk8Home();
    
    /**
     * We don't necessarily want to compute same dependencies than jdeps,
     * but jdeps output can give hints.
     */
    private static final String JDEPS = JAVA_HOME + "/bin/jdeps";
    
    /**
     * Cf. https://bugs.openjdk.java.net/browse/JDK-8136419.
     * Fixed in JDK 9.
     */
    private static final boolean BUG_JDK_8136419_FIXED = (getJavaVersion() >= 9);
    
    private static final boolean HANDLE_WEIRD_DOLLAR_SIGN_USAGES = true;

    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------

    private static final boolean[] FALSE_TRUE = new boolean[]{false,true};

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /*
     * Bad input.
     */

    /**
     * Tests that don't blow up when trying to read CAFEBABE.
     */
    public void test_computeDependencies_emptyFile() {
        final String className = "empty_class_file";

        final String classFilePath = getClassFilePath(className);
        final File emptyClassFile = new File(classFilePath);
        JdcFsUtils.ensureEmptyFile(emptyClassFile);

        /*
         * 
         */

        final boolean apiOnly = false;
        final SortedSet<String> actual = new TreeSet<String>();
        final String thisClassName;
        try {
            thisClassName = computeDependencies(
                    className,
                    apiOnly,
                    actual);
        } finally {
            // Cleanup.
            emptyClassFile.delete();
        }

        assertEquals(null, thisClassName);
        assertEquals(0, actual.size());
    }

    /*
     * package-info.
     */

    public void test_computeDependencies_packageInfo() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, TestAnno1.class);
                addSlashedName(expected, TestAnno2.class);
                addSlashedName(expected, Object.class);
                // String doesn't make it into the class file,
                // only types of other arguments.
                addSlashedName(expected, Integer.class);
                addSlashedName(expected, RoundingMode.class);
                addSlashedName(expected, Documented.class);
            }

            computeDepsAndCheck(TestAnno1.class.getPackage().getName() + ".package-info", apiOnly, expected);
        }
    }
    
    /*
     * Common specific location annotations definitions
     * (all but ElementType.PACKAGE, which we couldn't use here).
     */

    @Retention(RetentionPolicy.RUNTIME)@Target(ElementType.TYPE)
    public @interface A_TYPE_R {}
    @Retention(RetentionPolicy.CLASS)@Target(ElementType.TYPE)
    public @interface A_TYPE_C {}
    
    @Retention(RetentionPolicy.RUNTIME)@Target(ElementType.FIELD)
    public @interface A_FIELD_R {}
    @Retention(RetentionPolicy.CLASS)@Target(ElementType.FIELD)
    public @interface A_FIELD_C {}
    
    @Retention(RetentionPolicy.RUNTIME)@Target(ElementType.METHOD)
    public @interface A_METHOD_R {}
    @Retention(RetentionPolicy.CLASS)@Target(ElementType.METHOD)
    public @interface A_METHOD_C {}
    
    @Retention(RetentionPolicy.RUNTIME)@Target(ElementType.PARAMETER)
    public @interface A_PARAMETER_R {}
    @Retention(RetentionPolicy.CLASS)@Target(ElementType.PARAMETER)
    public @interface A_PARAMETER_C {}
    
    @Retention(RetentionPolicy.RUNTIME)@Target(ElementType.CONSTRUCTOR)
    public @interface A_CONSTRUCTOR_R {}
    @Retention(RetentionPolicy.CLASS)@Target(ElementType.CONSTRUCTOR)
    public @interface A_CONSTRUCTOR_C {}
    
    @Retention(RetentionPolicy.RUNTIME)@Target(ElementType.LOCAL_VARIABLE)
    public @interface A_LOCAL_VARIABLE_R {}
    @Retention(RetentionPolicy.CLASS)@Target(ElementType.LOCAL_VARIABLE)
    public @interface A_LOCAL_VARIABLE_C {}
    
    @Retention(RetentionPolicy.RUNTIME)@Target(ElementType.ANNOTATION_TYPE)
    public @interface A_ANNOTATION_TYPE_R {}
    @Retention(RetentionPolicy.CLASS)@Target(ElementType.ANNOTATION_TYPE)
    public @interface A_ANNOTATION_TYPE_C {}
    
    @Retention(RetentionPolicy.RUNTIME)@Target(ElementType.TYPE_PARAMETER)
    public @interface A_TYPE_PARAMETER_R {}
    @Retention(RetentionPolicy.CLASS)@Target(ElementType.TYPE_PARAMETER)
    public @interface A_TYPE_PARAMETER_C {}
    
    @Retention(RetentionPolicy.RUNTIME)@Target(ElementType.TYPE_USE)
    public @interface A_TYPE_USE_R {}
    @Retention(RetentionPolicy.CLASS)@Target(ElementType.TYPE_USE)
    public @interface A_TYPE_USE_C {}

    @Retention(RetentionPolicy.RUNTIME)@Target(ElementType.TYPE_USE)
    public @interface A_TYPE_USE_R_COMPLEX {
        Class<? extends Number>[] nbrClsArr() default {Byte.class};
        String tooStrong();
        RoundingMode rounding();
        Documented doc();
    }
    @Retention(RetentionPolicy.CLASS)@Target(ElementType.TYPE_USE)
    public @interface A_TYPE_USE_C_COMPLEX {
        Class<? extends Number>[] nbrClsArr() default {Byte.class};
        String tooStrong();
        RoundingMode rounding();
        Documented doc();
    }

    /*
     * Annotations (deps from them).
     */

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PACKAGE})
    public @interface MyTestAnno2_public {
        Class<? extends Number>[] nbrClsArr() default {Byte.class};
        String tooStrong();
        RoundingMode rounding();
        Documented doc();
    }
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PACKAGE})
    private @interface MyTestAnno2_private {
        Class<? extends Number>[] nbrClsArr() default {Byte.class};
        String tooStrong();
        RoundingMode rounding();
        Documented doc();
    }

    public void test_computeDependencies_fromAno_public() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            // Extends Object.
            addSlashedName(expected, Object.class);
            // Implements Annotation.
            addSlashedName(expected, Annotation.class);
            //
            addSlashedName(expected, Class.class);
            addSlashedName(expected, Number.class);
            addSlashedName(expected, Byte.class);
            addSlashedName(expected, String.class);
            addSlashedName(expected, RoundingMode.class);
            addSlashedName(expected, Documented.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, Retention.class);
                addSlashedName(expected, RetentionPolicy.class);
                addSlashedName(expected, Target.class);
                addSlashedName(expected, ElementType.class);
                //
                addSlashedName(expected, ClassDepsParserTest.class);
            }

            computeDepsAndCheck(MyTestAnno2_public.class.getName(), apiOnly, expected);
        }
    }
    public void test_computeDependencies_fromAno_private() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, Retention.class);
                addSlashedName(expected, RetentionPolicy.class);
                addSlashedName(expected, Target.class);
                addSlashedName(expected, ElementType.class);
                //
                addSlashedName(expected, ClassDepsParserTest.class);
                //
                addSlashedName(expected, Object.class);
                addSlashedName(expected, Annotation.class);
                //
                addSlashedName(expected, Class.class);
                addSlashedName(expected, Number.class);
                addSlashedName(expected, Byte.class);
                addSlashedName(expected, String.class);
                addSlashedName(expected, RoundingMode.class);
                addSlashedName(expected, Documented.class);
            }

            computeDepsAndCheck(MyTestAnno2_private.class.getName(), apiOnly, expected);
        }
    }

    /*
     * Class signature.
     */

    @TestAnno1
    public static class MyClassNonGenSig_public extends RuntimeException implements Runnable {
        private static final long serialVersionUID = 1L;
        @Override
        public void run() {
        }
    }
    @TestAnno1
    private static class MyClassNonGenSig_private extends RuntimeException implements Runnable {
        private static final long serialVersionUID = 1L;
        @Override
        public void run() {
        }
    }

    public void test_computeDependencies_classNonGenSig_public() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, RuntimeException.class);
            addSlashedName(expected, Runnable.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, TestAnno1.class);
                //
                addSlashedName(expected, ClassDepsParserTest.class);
            }

            computeDepsAndCheck(MyClassNonGenSig_public.class.getName(), apiOnly, expected);
        }
    }
    public void test_computeDependencies_classNonGenSigprivate() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, ClassDepsParserTest.class);
                //
                addSlashedName(expected, TestAnno1.class);
                addSlashedName(expected, RuntimeException.class);
                addSlashedName(expected, Runnable.class);
            }

            computeDepsAndCheck(MyClassNonGenSig_private.class.getName(), apiOnly, expected);
        }
    }

    @TestAnno1
    public static class MyClassGenSigStrict_public extends ThreadLocal<Object> implements GenericInterface<Number> {
    }
    @TestAnno1
    private static class MyClassGenSigStrict_private extends ThreadLocal<Object> implements GenericInterface<Number> {
    }

    public void test_computeDependencies_classGenSigStrict_public() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, ThreadLocal.class);
            addSlashedName(expected, Object.class);
            addSlashedName(expected, GenericInterface.class);
            addSlashedName(expected, Number.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, TestAnno1.class);
                //
                addSlashedName(expected, ClassDepsParserTest.class);
            }

            computeDepsAndCheck(MyClassGenSigStrict_public.class.getName(), apiOnly, expected);
        }
    }
    public void test_computeDependencies_classGenSigStrict_private() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, ClassDepsParserTest.class);
                //
                addSlashedName(expected, TestAnno1.class);
                addSlashedName(expected, ThreadLocal.class);
                addSlashedName(expected, Object.class);
                addSlashedName(expected, GenericInterface.class);
                addSlashedName(expected, Number.class);
            }

            computeDepsAndCheck(MyClassGenSigStrict_private.class.getName(), apiOnly, expected);
        }
    }

    @TestAnno1
    public static class MyClassGenSigLoose_public<E extends Object,F extends Number> extends ThreadLocal<E> implements GenericInterface<F> {
    }
    @TestAnno1
    private static class MyClassGenSigLoose_private<E extends Object,F extends Number> extends ThreadLocal<E> implements GenericInterface<F> {
    }

    public void test_computeDependencies_classGenSigLoose_public() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, ThreadLocal.class);
            addSlashedName(expected, Object.class);
            addSlashedName(expected, GenericInterface.class);
            addSlashedName(expected, Number.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, TestAnno1.class);
                //
                addSlashedName(expected, ClassDepsParserTest.class);
            }

            computeDepsAndCheck(MyClassGenSigLoose_public.class.getName(), apiOnly, expected);
        }
    }
    public void test_computeDependencies_classGenSigLoose_private() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, ClassDepsParserTest.class);
                //
                addSlashedName(expected, TestAnno1.class);
                addSlashedName(expected, ThreadLocal.class);
                addSlashedName(expected, Object.class);
                addSlashedName(expected, GenericInterface.class);
                addSlashedName(expected, Number.class);
            }

            computeDepsAndCheck(MyClassGenSigLoose_private.class.getName(), apiOnly, expected);
        }
    }

    /*
     * Fields signatures.
     */

    public static class MyFieldNonGenSig_public {
        @TestAnno1
        public Integer foo;
    }
    public static class MyFieldNonGenSig_private {
        @TestAnno1
        private Integer foo;
    }

    public void test_computeDependencies_fieldNonGenSig_public() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            addSlashedName(expected, Integer.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, TestAnno1.class);
                //
                addSlashedName(expected, ClassDepsParserTest.class);
            }

            computeDepsAndCheck(MyFieldNonGenSig_public.class.getName(), apiOnly, expected);
        }
    }
    public void test_computeDependencies_fieldNonGenSig_private() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, ClassDepsParserTest.class);
                //
                addSlashedName(expected, TestAnno1.class);
                addSlashedName(expected, Integer.class);
            }

            computeDepsAndCheck(MyFieldNonGenSig_private.class.getName(), apiOnly, expected);
        }
    }

    public static class MyFieldGenSigStrict_public {
        @TestAnno1
        public Comparable<Integer> foo;
    }
    public static class MyFieldGenSigStrict_private {
        @TestAnno1
        private Comparable<Integer> foo;
    }

    public void test_computeDependencies_fieldGenSigStrict_public() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            addSlashedName(expected, Comparable.class);
            addSlashedName(expected, Integer.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, TestAnno1.class);
                //
                addSlashedName(expected, ClassDepsParserTest.class);
            }

            computeDepsAndCheck(MyFieldGenSigStrict_public.class.getName(), apiOnly, expected);
        }
    }
    public void test_computeDependencies_fieldGenSigStrict_private() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, ClassDepsParserTest.class);
                //
                addSlashedName(expected, TestAnno1.class);
                addSlashedName(expected, Comparable.class);
                addSlashedName(expected, Integer.class);
            }

            computeDepsAndCheck(MyFieldGenSigStrict_private.class.getName(), apiOnly, expected);
        }
    }

    public static class MyFieldGenSigWildcard_public {
        @TestAnno1
        public Comparable<? extends Number> foo;
    }
    public static class MyFieldGenSigWildcard_private {
        @TestAnno1
        private Comparable<? extends Number> foo;
    }

    public void test_computeDependencies_fieldGenSigWildcard_public() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            addSlashedName(expected, Comparable.class);
            addSlashedName(expected, Number.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, TestAnno1.class);
                //
                addSlashedName(expected, ClassDepsParserTest.class);
            }

            computeDepsAndCheck(MyFieldGenSigWildcard_public.class.getName(), apiOnly, expected);
        }
    }
    public void test_computeDependencies_fieldGenSigWildcard_private() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, ClassDepsParserTest.class);
                //
                addSlashedName(expected, TestAnno1.class);
                addSlashedName(expected, Comparable.class);
                addSlashedName(expected, Number.class);
            }

            computeDepsAndCheck(MyFieldGenSigWildcard_private.class.getName(), apiOnly, expected);
        }
    }

    /*
     * Methods signatures.
     */

    /**
     * <init> method.
     */
    public static class MyMethodSigConstructor_public {
        @TestAnno1
        public MyMethodSigConstructor_public(Number arg) throws Exception {
        }
    }
    public static class MyMethodSigConstructor_private {
        @TestAnno1
        private MyMethodSigConstructor_private(Number arg) throws Exception {
        }
    }

    public void test_computeDependencies_methodSigConstructor_public() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            addSlashedName(expected, Number.class);
            addSlashedName(expected, Exception.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, TestAnno1.class);
                //
                addSlashedName(expected, ClassDepsParserTest.class);
            }

            computeDepsAndCheck(MyMethodSigConstructor_public.class.getName(), apiOnly, expected);
        }
    }
    public void test_computeDependencies_methodSigConstructor_private() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, ClassDepsParserTest.class);
                //
                addSlashedName(expected, TestAnno1.class);
                addSlashedName(expected, Number.class);
                addSlashedName(expected, Exception.class);
            }

            computeDepsAndCheck(MyMethodSigConstructor_private.class.getName(), apiOnly, expected);
        }
    }

    public static class MyMethodNonGenSigChecked_public {
        @TestAnno1
        public Integer foo(Number arg) throws Exception {
            return null;
        }
    }
    public static class MyMethodNonGenSigChecked_private {
        @TestAnno1
        private Integer foo(Number arg) throws Exception {
            return null;
        }
    }

    public void test_computeDependencies_methodNonGenSigChecked_public() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            addSlashedName(expected, Integer.class);
            addSlashedName(expected, Number.class);
            addSlashedName(expected, Exception.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, TestAnno1.class);
                //
                addSlashedName(expected, ClassDepsParserTest.class);
            }

            computeDepsAndCheck(MyMethodNonGenSigChecked_public.class.getName(), apiOnly, expected);
        }
    }
    public void test_computeDependencies_methodNonGenSigChecked_private() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, ClassDepsParserTest.class);
                //
                addSlashedName(expected, TestAnno1.class);
                addSlashedName(expected, Integer.class);
                addSlashedName(expected, Number.class);
                addSlashedName(expected, Exception.class);
            }

            computeDepsAndCheck(MyMethodNonGenSigChecked_private.class.getName(), apiOnly, expected);
        }
    }

    public static class MyMethodGenSigStrictUnchecked_public {
        @TestAnno1
        public Iterable<Integer> foo(Comparable<Number> arg) throws RuntimeException {
            return null;
        }
    }
    public static class MyMethodGenSigStrictUnchecked_private {
        @TestAnno1
        private Iterable<Integer> foo(Comparable<Number> arg) throws RuntimeException {
            return null;
        }
    }

    public void test_computeDependencies_methodGenSigStrictUnchecked_public() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            addSlashedName(expected, Iterable.class);
            addSlashedName(expected, Integer.class);
            addSlashedName(expected, Comparable.class);
            addSlashedName(expected, Number.class);
            addSlashedName(expected, RuntimeException.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, TestAnno1.class);
                //
                addSlashedName(expected, ClassDepsParserTest.class);
            }

            computeDepsAndCheck(MyMethodGenSigStrictUnchecked_public.class.getName(), apiOnly, expected);
        }
    }
    public void test_computeDependencies_methodGenSigStrictUnchecked_private() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, ClassDepsParserTest.class);
                //
                addSlashedName(expected, TestAnno1.class);
                addSlashedName(expected, Iterable.class);
                addSlashedName(expected, Integer.class);
                addSlashedName(expected, Comparable.class);
                addSlashedName(expected, Number.class);
                addSlashedName(expected, RuntimeException.class);
            }

            computeDepsAndCheck(MyMethodGenSigStrictUnchecked_private.class.getName(), apiOnly, expected);
        }
    }

    /*
     * Field init code (located in <init> method).
     */

    public static class MyFieldInitCode_public {
        @TestAnno1
        public Map<Integer,Long> foo = new HashMap<Integer,Long>();
    }
    public static class MyFieldInitCode_private {
        @TestAnno1
        private Map<Integer,Long> foo = new HashMap<Integer,Long>();
    }

    public void test_computeDependencies_fieldInitCode_public() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            addSlashedName(expected, Map.class);
            addSlashedName(expected, Integer.class);
            addSlashedName(expected, Long.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, TestAnno1.class);
                //
                addSlashedName(expected, ClassDepsParserTest.class);
                //
                addSlashedName(expected, HashMap.class);
            }

            computeDepsAndCheck(MyFieldInitCode_public.class.getName(), apiOnly, expected);
        }
    }
    public void test_computeDependencies_fieldInitCode_private() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, ClassDepsParserTest.class);
                //
                addSlashedName(expected, HashMap.class);
                //
                addSlashedName(expected, TestAnno1.class);
                addSlashedName(expected, Map.class);
                addSlashedName(expected, Integer.class);
                addSlashedName(expected, Long.class);
            }

            computeDepsAndCheck(MyFieldInitCode_private.class.getName(), apiOnly, expected);
        }
    }

    /*
     * Method code.
     * 
     * <clinit> method: one for class init.
     * <init> methods: one per constructor or init block.
     */

    /**
     * <clinit> method.
     */
    public static class MyMethodCodeStaticInitBlock {
        static {
            // String will not make it into the class file.
            @A_TYPE_USE_R_COMPLEX(nbrClsArr={Long.class},tooStrong="",rounding=RoundingMode.UNNECESSARY,doc=@Documented)
            Integer foo = 1;
            try {
                MyBlackHole.blackHole(foo);
            } catch (RuntimeException e) {
                throw e;
            }
        }
    }
    
    public void test_computeDependencies_methodCodeStaticInitBlock() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, ClassDepsParserTest.class);
                //
                if (BUG_JDK_8136419_FIXED) {
                    addSlashedName(expected, A_TYPE_USE_R_COMPLEX.class);
                    addSlashedName(expected, Long.class);
                    addSlashedName(expected, RoundingMode.class);
                    addSlashedName(expected, Documented.class);
                }
                addSlashedName(expected, Integer.class);
                addSlashedName(expected, MyBlackHole.class);
                addSlashedName(expected, RuntimeException.class);
            }

            computeDepsAndCheck(MyMethodCodeStaticInitBlock.class.getName(), apiOnly, expected);
        }
    }

    /**
     * <init> method.
     */
    public static class MyMethodCodeInitBlock {
        {
            // String will not make it into the class file.
            @A_TYPE_USE_R_COMPLEX(nbrClsArr={Long.class},tooStrong="",rounding=RoundingMode.UNNECESSARY,doc=@Documented)
            Integer foo = 1;
            try {
                MyBlackHole.blackHole(foo);
            } catch (RuntimeException e) {
                throw e;
            }
        }
    }

    public void test_computeDependencies_methodCodeInitBlock() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, ClassDepsParserTest.class);
                //
                if (BUG_JDK_8136419_FIXED) {
                    addSlashedName(expected, A_TYPE_USE_R_COMPLEX.class);
                    addSlashedName(expected, Long.class);
                    addSlashedName(expected, RoundingMode.class);
                    addSlashedName(expected, Documented.class);
                }
                addSlashedName(expected, Integer.class);
                addSlashedName(expected, MyBlackHole.class);
                addSlashedName(expected, RuntimeException.class);
            }

            computeDepsAndCheck(MyMethodCodeInitBlock.class.getName(), apiOnly, expected);
        }
    }

    /**
     * <init> method.
     */
    public static class MyMethodCodeConstructor {
        public MyMethodCodeConstructor() {
            // String will not make it into the class file.
            @A_TYPE_USE_R_COMPLEX(nbrClsArr={Long.class},tooStrong="",rounding=RoundingMode.UNNECESSARY,doc=@Documented)
            Integer foo = 1;
            try {
                MyBlackHole.blackHole(foo);
            } catch (RuntimeException e) {
                throw e;
            }
        }
    }

    public void test_computeDependencies_methodCodeConstructor() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, ClassDepsParserTest.class);
                //
                addSlashedName(expected, A_TYPE_USE_R_COMPLEX.class);
                addSlashedName(expected, Long.class);
                addSlashedName(expected, RoundingMode.class);
                addSlashedName(expected, Documented.class);
                addSlashedName(expected, Integer.class);
                addSlashedName(expected, MyBlackHole.class);
                addSlashedName(expected, RuntimeException.class);
            }

            computeDepsAndCheck(MyMethodCodeConstructor.class.getName(), apiOnly, expected);
        }
    }

    public static class MyMethodCodeNonGen {
        public void foo() {
            // String will not make it into the class file.
            @A_TYPE_USE_R_COMPLEX(nbrClsArr={Long.class},tooStrong="",rounding=RoundingMode.UNNECESSARY,doc=@Documented)
            Integer foo = 1;
            try {
                MyBlackHole.blackHole(foo);
            } catch (RuntimeException e) {
                throw e;
            }
        }
    }

    public void test_computeDependencies_methodCodeNonGen() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, ClassDepsParserTest.class);
                //
                addSlashedName(expected, A_TYPE_USE_R_COMPLEX.class);
                addSlashedName(expected, Long.class);
                addSlashedName(expected, RoundingMode.class);
                addSlashedName(expected, Documented.class);
                addSlashedName(expected, Integer.class);
                addSlashedName(expected, MyBlackHole.class);
                addSlashedName(expected, RuntimeException.class);
            }

            computeDepsAndCheck(MyMethodCodeNonGen.class.getName(), apiOnly, expected);
        }
    }

    public static class MyMethodCodeGenStrict {
        public void foo() {
            // String and Number will not make it into the class file.
            @A_TYPE_USE_R_COMPLEX(nbrClsArr={Long.class},tooStrong="",rounding=RoundingMode.UNNECESSARY,doc=@Documented)
            Comparable<Number> foo = (Comparable<Number>) new Object();
            try {
                MyBlackHole.blackHole(foo);
            } catch (RuntimeException e) {
                throw e;
            }
        }
    }

    public void test_computeDependencies_methodCodeGenStrict() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, ClassDepsParserTest.class);
                //
                addSlashedName(expected, A_TYPE_USE_R_COMPLEX.class);
                addSlashedName(expected, Long.class);
                addSlashedName(expected, RoundingMode.class);
                addSlashedName(expected, Documented.class);
                addSlashedName(expected, Comparable.class);
                addSlashedName(expected, MyBlackHole.class);
                addSlashedName(expected, RuntimeException.class);
            }

            computeDepsAndCheck(MyMethodCodeGenStrict.class.getName(), apiOnly, expected);
        }
    }

    public static class MyMethodCodeGenLoose {
        public void foo() {
            // String and Number will not make it into the class file.
            @A_TYPE_USE_R_COMPLEX(nbrClsArr={Long.class},tooStrong="",rounding=RoundingMode.UNNECESSARY,doc=@Documented)
            Comparable<? extends Number> foo = (Comparable<? extends Number>) new Object();
            try {
                MyBlackHole.blackHole(foo);
            } catch (RuntimeException e) {
                throw e;
            }
        }
    }

    public void test_computeDependencies_methodCodeGenLoose() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, ClassDepsParserTest.class);
                //
                addSlashedName(expected, A_TYPE_USE_R_COMPLEX.class);
                addSlashedName(expected, Long.class);
                addSlashedName(expected, RoundingMode.class);
                addSlashedName(expected, Documented.class);
                addSlashedName(expected, Comparable.class);
                addSlashedName(expected, MyBlackHole.class);
                addSlashedName(expected, RuntimeException.class);
            }

            computeDepsAndCheck(MyMethodCodeGenLoose.class.getName(), apiOnly, expected);
        }
    }

    /*
     * Nested classes.
     */

    public static class MyNestedClass_public {
        public static class MyNested1Ref {
            // Annotation belongs to the nested class, not to the API.
            @TestAnno1
            public static class MyNested2 {
                // Not direct nested class: not a dependency.
                public static class MyNested3 {
                }
            }
        }
    }
    public static class MyNestedClass_private {
        private static class MyNested1Ref {
            // Annotation belongs to the nested class, not to the API.
            @TestAnno1
            public static class MyNested2 {
                // Not direct nested class: not a dependency.
                public static class MyNested3 {
                }
            }
        }
    }

    public void test_computeDependencies_nestedClass_public() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            if (apiOnly) {
            } else {
                // Deps up to top level class.
                addSlashedName(expected, ClassDepsParserTest.class);
                addSlashedName(expected, MyNestedClass_public.class);
                // Deps down to direct nested classes.
                addSlashedName(expected, MyNestedClass_public.MyNested1Ref.MyNested2.class);
            }

            computeDepsAndCheck(MyNestedClass_public.MyNested1Ref.class.getName(), apiOnly, expected);
        }
    }
    public void test_computeDependencies_nestedClass_private() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, Object.class);
                //
                // Deps up to top level class.
                addSlashedName(expected, ClassDepsParserTest.class);
                addSlashedName(expected, MyNestedClass_private.class);
                // Deps down to direct nested classes.
                addSlashedName(expected, MyNestedClass_private.MyNested1Ref.MyNested2.class);
            }

            computeDepsAndCheck(MyNestedClass_private.MyNested1Ref.class.getName(), apiOnly, expected);
        }
    }

    public static class MyNestedClass_static {
        public static class MyNestedRef {
            public MyNestedRef() {
            }
        }
    }
    public static class MyNestedClass_nonStatic {
        public class MyNestedRef {
            // API constructor causes dependency to outer class,
            // due to outer class appearing in its signature.
            public MyNestedRef() {
            }
        }
    }
    public static class MyNestedClass_nonStatic_private {
        public class MyNestedRef {
            // non-API constructor so no API dependency to outer class.
            private MyNestedRef() {
            }
        }
    }
    
    public void test_computeDependencies_nestedClass_static() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, ClassDepsParserTest.class);
                addSlashedName(expected, MyNestedClass_static.class);
            }

            computeDepsAndCheck(MyNestedClass_static.MyNestedRef.class.getName(), apiOnly, expected);
        }
    }
    public void test_computeDependencies_nestedClass_nonStatic() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            addSlashedName(expected, MyNestedClass_nonStatic.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, ClassDepsParserTest.class);
            }

            computeDepsAndCheck(MyNestedClass_nonStatic.MyNestedRef.class.getName(), apiOnly, expected);
        }
    }
    public void test_computeDependencies_nestedClass_nonStatic_private() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, ClassDepsParserTest.class);
                //
                addSlashedName(expected, MyNestedClass_nonStatic_private.class);
            }

            computeDepsAndCheck(MyNestedClass_nonStatic_private.MyNestedRef.class.getName(), apiOnly, expected);
        }
    }

    private static class MyNestedClassApiIfOutersApi {
    }
    public static class MyNestedClassApiIfOutersApi_public {
        public static class MyNested {
            public static class MyNested2Ref {
                // Causing dependency to a non-API nested class with shorter
                // name, to make sure any appearing nested class with shorter
                // name is not considered as one of our outer classes, even
                // if it starts with the same string.
                private MyNestedClassApiIfOutersApi foo;
            }
        }
    }
    private static class MyNestedClassApiIfOutersApi_private {
        public static class MyNested {
            public static class MyNested2Ref {
            }
        }
    }

    public void test_computeDependencies_nestedClassApiIfOutersApi_public() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, ClassDepsParserTest.class);
                addSlashedName(expected, MyNestedClassApiIfOutersApi_public.class);
                addSlashedName(expected, MyNestedClassApiIfOutersApi_public.MyNested.class);
                //
                addSlashedName(expected, MyNestedClassApiIfOutersApi.class);
            }

            computeDepsAndCheck(MyNestedClassApiIfOutersApi_public.MyNested.MyNested2Ref.class.getName(), apiOnly, expected);
        }
    }
    public void test_computeDependencies_nestedClassApiIfOutersApi_private() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            if (apiOnly) {
                // At least one of outer classes up to top level class
                // (excluded because its access flags are not indicated)
                // is not public or protected, so the class is not an
                // API class.
            } else {
                addSlashedName(expected, Object.class);
                //
                addSlashedName(expected, ClassDepsParserTest.class);
                addSlashedName(expected, MyNestedClassApiIfOutersApi_private.class);
                addSlashedName(expected, MyNestedClassApiIfOutersApi_private.MyNested.class);
            }

            computeDepsAndCheck(MyNestedClassApiIfOutersApi_private.MyNested.MyNested2Ref.class.getName(), apiOnly, expected);
        }
    }

    public static class MyNestedOfOther1 {
        public static class MyNested1 {
            public static class MyNested2 {
                // Nested of nested depended on: won't have dependency to it.
                public static class MyNested3 {
                }
            }
        }
    }
    public static class MyNestedOfOther2 {
        public MyNestedOfOther1.MyNested1.MyNested2 foo;
    }

    /**
     * Dependency to a nested class of another class,
     * causes dependency to its outer class(es) up to its top level class
     * (due to the brutal dependencies computation that also causes
     * dependencies up to top level class of current class).
     */
    public void test_computeDependencies_nestedOfOther() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            addSlashedName(expected, MyNestedOfOther1.MyNested1.MyNested2.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, ClassDepsParserTest.class);
                //
                addSlashedName(expected, MyNestedOfOther1.class);
                addSlashedName(expected, MyNestedOfOther1.MyNested1.class);
            }

            computeDepsAndCheck(MyNestedOfOther2.class.getName(), apiOnly, expected);
        }
    }

    public static class MyNestedAndFlags1 {
        public static class Public1 {
        }
        public static class Public2 {
        }
        private static class Private1 {
        }
        private static class Private2 {
        }
    }
    public static class MyNestedAndFlags2 {
        public MyNestedAndFlags1.Public1 api_to_public;
        public MyNestedAndFlags1.Private1 api_to_private;
        //
        private MyNestedAndFlags1.Public2 nonApi_to_public;
        private MyNestedAndFlags1.Private2 nonApi_to_private;
    }

    /**
     * Testing that nested classes access flags (in InnerClasses attribute)
     * don't affect dependencies to these classes from fields, methods, etc.
     */
    public void test_computeDependencies_nestedAndFlags() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            addSlashedName(expected, MyNestedAndFlags1.Public1.class);
            addSlashedName(expected, MyNestedAndFlags1.Private1.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, ClassDepsParserTest.class);
                //
                addSlashedName(expected, MyNestedAndFlags1.class);
                addSlashedName(expected, MyNestedAndFlags1.Public2.class);
                addSlashedName(expected, MyNestedAndFlags1.Private2.class);
            }

            computeDepsAndCheck(MyNestedAndFlags2.class.getName(), apiOnly, expected);
        }
    }

    /*
     * Local classes.
     */

    public static class MyLocal {
        public void foo() {
            // $1Local
            @TestAnno1
            class Local {
            }
        }
    }

    public void test_computeDependencies_localClass_fromOuter() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, ClassDepsParserTest.class);
                //
                addSlashedName(expected, MyLocal.class.getName() + "$1Local");
            }

            computeDepsAndCheck(MyLocal.class.getName(), apiOnly, expected);
        }
    }
    public void test_computeDependencies_localClass_fromLocal() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            if (apiOnly) {
                // Class not API.
            } else {
                addSlashedName(expected, Object.class);
                //
                addSlashedName(expected, TestAnno1.class);
                addSlashedName(expected, ClassDepsParserTest.class);
                addSlashedName(expected, MyLocal.class);
            }

            computeDepsAndCheck(MyLocal.class.getName() + "$1Local", apiOnly, expected);
        }
    }

    /*
     * Anonymous inner classes.
     */

    public static class MyAnonInner {
        public Object foo() {
            // $1
            return new Runnable() {
                @Override
                public void run() {
                }
            };
        }
    }

    public void test_computeDependencies_anonInner_fromOuter() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, ClassDepsParserTest.class);
                //
                addSlashedName(expected, MyAnonInner.class.getName() + "$1");
            }

            computeDepsAndCheck(MyAnonInner.class.getName(), apiOnly, expected);
        }
    }

    public void test_computeDependencies_anonInner_fromAnonInner() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, Object.class);
                //
                addSlashedName(expected, ClassDepsParserTest.class);
                addSlashedName(expected, MyAnonInner.class);
                //
                addSlashedName(expected, Runnable.class);
            }

            computeDepsAndCheck(MyAnonInner.class.getName() + "$1", apiOnly, expected);
        }
    }
    
    /*
     * Advanced dependencies to annotations:
     * from each possible location, visible or not.
     * 
     * PACKAGE: already tested when testing package-info dependencies.
     */

    @A_TYPE_R
    public static class MyDepToAno_TYPE_R {
    }
    @A_TYPE_C
    public static class MyDepToAno_TYPE_C {
    }
    
    public void test_computeDependencies_toAno_TYPE_R() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, A_TYPE_R.class);
                //
                addSlashedName(expected, ClassDepsParserTest.class);
            }

            computeDepsAndCheck(MyDepToAno_TYPE_R.class.getName(), apiOnly, expected);
        }
    }
    public void test_computeDependencies_toAno_TYPE_C() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, A_TYPE_C.class);
                //
                addSlashedName(expected, ClassDepsParserTest.class);
            }

            computeDepsAndCheck(MyDepToAno_TYPE_C.class.getName(), apiOnly, expected);
        }
    }

    public static class MyDepToAno_FIELD_R {
        @A_FIELD_R
        public Object foo;
    }
    public static class MyDepToAno_FIELD_C {
        @A_FIELD_C
        public Object foo;
    }
    
    public void test_computeDependencies_toAno_FIELD_R() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, A_FIELD_R.class);
                //
                addSlashedName(expected, ClassDepsParserTest.class);
            }

            computeDepsAndCheck(MyDepToAno_FIELD_R.class.getName(), apiOnly, expected);
        }
    }
    public void test_computeDependencies_toAno_FIELD_C() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, A_FIELD_C.class);
                //
                addSlashedName(expected, ClassDepsParserTest.class);
            }

            computeDepsAndCheck(MyDepToAno_FIELD_C.class.getName(), apiOnly, expected);
        }
    }

    public static class MyDepToAno_METHOD_R {
        @A_METHOD_R
        public void foo() {
        }
    }
    public static class MyDepToAno_METHOD_C {
        @A_METHOD_C
        public void foo() {
        }
    }
    
    public void test_computeDependencies_toAno_METHOD_R() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, A_METHOD_R.class);
                //
                addSlashedName(expected, ClassDepsParserTest.class);
            }

            computeDepsAndCheck(MyDepToAno_METHOD_R.class.getName(), apiOnly, expected);
        }
    }
    public void test_computeDependencies_toAno_METHOD_C() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, A_METHOD_C.class);
                //
                addSlashedName(expected, ClassDepsParserTest.class);
            }

            computeDepsAndCheck(MyDepToAno_METHOD_C.class.getName(), apiOnly, expected);
        }
    }
    
    public static class MyDepToAno_PARAMETER_R {
        public void foo(@A_PARAMETER_R Object arg) {
        }
    }
    public static class MyDepToAno_PARAMETER_C {
        public void foo(@A_PARAMETER_C Object arg) {
        }
    }

    public void test_computeDependencies_toAno_PARAMETER_R() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, A_PARAMETER_R.class);
                //
                addSlashedName(expected, ClassDepsParserTest.class);
            }

            computeDepsAndCheck(MyDepToAno_PARAMETER_R.class.getName(), apiOnly, expected);
        }
    }
    public void test_computeDependencies_toAno_PARAMETER_C() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, A_PARAMETER_C.class);
                //
                addSlashedName(expected, ClassDepsParserTest.class);
            }

            computeDepsAndCheck(MyDepToAno_PARAMETER_C.class.getName(), apiOnly, expected);
        }
    }

    public static class MyDepToAno_CONSTRUCTOR_R {
        @A_CONSTRUCTOR_R
        public MyDepToAno_CONSTRUCTOR_R() {
        }
    }
    public static class MyDepToAno_CONSTRUCTOR_C {
        @A_CONSTRUCTOR_C
        public MyDepToAno_CONSTRUCTOR_C() {
        }
    }

    public void test_computeDependencies_toAno_CONSTRUCTOR_R() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, A_CONSTRUCTOR_R.class);
                //
                addSlashedName(expected, ClassDepsParserTest.class);
            }

            computeDepsAndCheck(MyDepToAno_CONSTRUCTOR_R.class.getName(), apiOnly, expected);
        }
    }
    public void test_computeDependencies_toAno_CONSTRUCTOR_C() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, A_CONSTRUCTOR_C.class);
                //
                addSlashedName(expected, ClassDepsParserTest.class);
            }

            computeDepsAndCheck(MyDepToAno_CONSTRUCTOR_C.class.getName(), apiOnly, expected);
        }
    }

    public static class MyDepToAno_LOCAL_VARIABLE_R {
        public void foo() {
            @A_LOCAL_VARIABLE_R
            Integer foo = 1;
            MyBlackHole.blackHole(foo);
        }
    }
    public static class MyDepToAno_LOCAL_VARIABLE_C {
        public void foo() {
            @A_LOCAL_VARIABLE_C
            Integer foo = 1;
            MyBlackHole.blackHole(foo);
        }
    }
    
    public void test_computeDependencies_toAno_LOCAL_VARIABLE_R() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, ClassDepsParserTest.class);
                //
                if (false) {
                    // The annotation doesn't make it into the class file
                    // (JSR 175: "it makes little sense to retain all annotations at run time").
                    addSlashedName(expected, A_LOCAL_VARIABLE_R.class);
                }
                addSlashedName(expected, Integer.class);
                addSlashedName(expected, MyBlackHole.class);
            }

            computeDepsAndCheck(MyDepToAno_LOCAL_VARIABLE_R.class.getName(), apiOnly, expected);
        }
    }
    public void test_computeDependencies_toAno_LOCAL_VARIABLE_C() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, ClassDepsParserTest.class);
                //
                if (false) {
                    // The annotation doesn't make it into the class file
                    // (JSR 175: "it makes little sense to retain all annotations at run time").
                    addSlashedName(expected, A_LOCAL_VARIABLE_C.class);
                }
                addSlashedName(expected, Integer.class);
                addSlashedName(expected, MyBlackHole.class);
            }

            computeDepsAndCheck(MyDepToAno_LOCAL_VARIABLE_C.class.getName(), apiOnly, expected);
        }
    }

    @A_ANNOTATION_TYPE_R
    public @interface MyDepToAno_ANNOTATION_TYPE_R {
    }
    @A_ANNOTATION_TYPE_C
    public @interface MyDepToAno_ANNOTATION_TYPE_C {
    }
    
    public void test_computeDependencies_toAno_ANNOTATION_TYPE_R() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            addSlashedName(expected, Annotation.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, A_ANNOTATION_TYPE_R.class);
                //
                addSlashedName(expected, ClassDepsParserTest.class);
            }

            computeDepsAndCheck(MyDepToAno_ANNOTATION_TYPE_R.class.getName(), apiOnly, expected);
        }
    }
    public void test_computeDependencies_toAno_ANNOTATION_TYPE_C() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            addSlashedName(expected, Annotation.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, A_ANNOTATION_TYPE_C.class);
                //
                addSlashedName(expected, ClassDepsParserTest.class);
            }

            computeDepsAndCheck(MyDepToAno_ANNOTATION_TYPE_C.class.getName(), apiOnly, expected);
        }
    }

    public static class MyDepToAno_TYPE_PARAMETER_R {
        public <@A_TYPE_PARAMETER_R E extends Number> void foo(E arg) {
        }
    }
    public static class MyDepToAno_TYPE_PARAMETER_C {
        public <@A_TYPE_PARAMETER_C E extends Number> void foo(E arg) {
        }
    }
    
    public void test_computeDependencies_toAno_TYPE_PARAMETER_R() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            addSlashedName(expected, Number.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, A_TYPE_PARAMETER_R.class);
                //
                addSlashedName(expected, ClassDepsParserTest.class);
            }

            computeDepsAndCheck(MyDepToAno_TYPE_PARAMETER_R.class.getName(), apiOnly, expected);
        }
    }
    public void test_computeDependencies_toAno_TYPE_PARAMETER_C() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            addSlashedName(expected, Number.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, A_TYPE_PARAMETER_C.class);
                //
                addSlashedName(expected, ClassDepsParserTest.class);
            }

            computeDepsAndCheck(MyDepToAno_TYPE_PARAMETER_C.class.getName(), apiOnly, expected);
        }
    }

    public static class MyDepToAno_TYPE_USE_R {
        public <E extends Number> void foo(@A_TYPE_USE_R E arg) {
        }
    }
    public static class MyDepToAno_TYPE_USE_C {
        public <E extends Number> void foo(@A_TYPE_USE_C E arg) {
        }
    }
    
    public void test_computeDependencies_toAno_TYPE_USE_R() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            addSlashedName(expected, Number.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, A_TYPE_USE_R.class);
                //
                addSlashedName(expected, ClassDepsParserTest.class);
            }

            computeDepsAndCheck(MyDepToAno_TYPE_USE_R.class.getName(), apiOnly, expected);
        }
    }
    public void test_computeDependencies_toAno_TYPE_USE_C() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            addSlashedName(expected, Number.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, A_TYPE_USE_C.class);
                //
                addSlashedName(expected, ClassDepsParserTest.class);
            }

            computeDepsAndCheck(MyDepToAno_TYPE_USE_C.class.getName(), apiOnly, expected);
        }
    }
    
    /*
     * Deprecated.
     * 
     * Tag is ignored, and if API only annotation is ignored
     * if not on an API.
     */

    /**
     * @deprecated
     */
    public static class MyDeprecatedPublicClassDoc {
    }
    /**
     * @deprecated
     */
    private static class MyDeprecatedPrivateClassDoc {
    }
    @Deprecated
    public static class MyDeprecatedPublicClass {
    }
    @Deprecated
    private static class MyDeprecatedPrivateClass {
    }
    //
    public static class MyDeprecatedPublicNestedClassDoc {
        /**
         * @deprecated
         */
        public static class MyNested {
        }
    }
    private static class MyDeprecatedPrivateNestedClassDoc {
        /**
         * @deprecated
         */
        private static class MyNested {
        }
    }
    public static class MyDeprecatedPublicNestedClass {
        @Deprecated
        public static class MyNested {
        }
    }
    private static class MyDeprecatedPrivateNestedClass {
        @Deprecated
        private static class MyNested {
        }
    }
    public static class MyDeprecatedPublicFieldDoc {
        /**
         * @deprecated
         */
        public int foo;
    }
    public static class MyDeprecatedPrivateFieldDoc {
        /**
         * @deprecated
         */
        private int foo;
    }
    //
    public static class MyDeprecatedPublicField {
        @Deprecated
        public int foo;
    }
    public static class MyDeprecatedPrivateField {
        @Deprecated
        private int foo;
    }
    public static class MyDeprecatedPublicMethodDoc {
        /**
         * @deprecated
         */
        public void foo() {
        }
    }
    public static class MyDeprecatedPrivateMethodDoc {
        /**
         * @deprecated
         */
        private void foo() {
        }
    }
    public static class MyDeprecatedPublicMethod {
        @Deprecated
        public void foo() {
        }
    }
    public static class MyDeprecatedPrivateMethod {
        @Deprecated
        private void foo() {
        }
    }

    public void test_computeDependencies_deprecated() {
        for (Class<?> clazz : new Class<?>[]{
                MyDeprecatedPublicClassDoc.class,
                MyDeprecatedPrivateClassDoc.class,
                MyDeprecatedPublicClass.class,
                MyDeprecatedPrivateClass.class,
                //
                MyDeprecatedPublicNestedClassDoc.MyNested.class,
                MyDeprecatedPrivateNestedClassDoc.MyNested.class,
                MyDeprecatedPublicNestedClass.MyNested.class,
                MyDeprecatedPrivateNestedClass.MyNested.class,
                //
                MyDeprecatedPublicFieldDoc.class,
                MyDeprecatedPrivateFieldDoc.class,
                MyDeprecatedPublicField.class,
                MyDeprecatedPrivateField.class,
                //
                MyDeprecatedPublicMethodDoc.class,
                MyDeprecatedPrivateMethodDoc.class,
                MyDeprecatedPublicMethod.class,
                MyDeprecatedPrivateMethod.class,
        }) {

            if (DEBUG) {
                System.out.println();
                System.out.println("clazz = " + clazz);
            }

            final String className = clazz.getName();

            for (boolean apiOnly : FALSE_TRUE) {

                final SortedSet<String> actualDeps = new TreeSet<String>();
                final String thisClassName = computeDependencies(
                        className,
                        apiOnly,
                        actualDeps);

                final boolean actualDeprecated = actualDeps.contains(slashed(Deprecated.class.getName()));

                final boolean expectedDeprecated;
                //
                if (apiOnly) {
                    expectedDeprecated = false;
                } else {
                    expectedDeprecated = (!clazz.getName().contains("Doc"));
                }
                
                if (DEBUG) {
                    System.out.println();
                    System.out.println("apiOnly = " + apiOnly);
                    System.out.println("class name: " + className);
                    System.out.println("expected deprecated: " + expectedDeprecated);
                    System.out.println("actual deprecated:   " + actualDeprecated);
                    System.out.println("actual deps: " + actualDeps);
                }

                assertEquals(slashed(className), thisClassName);
                assertEquals(expectedDeprecated, actualDeprecated);
            }
        }
    }

    /*
     * Class visibility.
     */

    @TestAnno1
    public static class MyClassVisibility_public extends RuntimeException implements Runnable {
        private static final long serialVersionUID = 1L;
        @TestAnno1
        public Integer foo;
        @TestAnno1
        public Integer foo() {
            return null;
        }
        @Override
        @TestAnno1
        public void run() {
            @TestAnno1
            Integer foo = 1;
            MyBlackHole.blackHole(foo);
        }
    }
    @TestAnno1
    private static class MyClassVisibility_private extends RuntimeException implements Runnable {
        private static final long serialVersionUID = 1L;
        @TestAnno1
        public Integer foo;
        @TestAnno1
        public Integer foo() {
            return null;
        }
        @Override
        @TestAnno1
        public void run() {
            @TestAnno1
            Integer foo = 1;
            MyBlackHole.blackHole(foo);
        }
    }

    public void test_computeDependencies_classVisibility_public() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, RuntimeException.class);
            addSlashedName(expected, Runnable.class);
            addSlashedName(expected, Integer.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, TestAnno1.class);
                //
                addSlashedName(expected, ClassDepsParserTest.class);
                //
                addSlashedName(expected, MyBlackHole.class);
                // Object comes from black hole API.
                addSlashedName(expected, Object.class);
            }

            computeDepsAndCheck(MyClassVisibility_public.class.getName(), apiOnly, expected);
        }
    }
    public void test_computeDependencies_classVisibility_private() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            if (apiOnly) {
                // No dependency (not even to Object!) if class is not API.
            } else {
                addSlashedName(expected, ClassDepsParserTest.class);
                //
                addSlashedName(expected, TestAnno1.class);
                addSlashedName(expected, RuntimeException.class);
                addSlashedName(expected, Runnable.class);
                addSlashedName(expected, Integer.class);
                //
                addSlashedName(expected, MyBlackHole.class);
                // Object comes from black hole API.
                addSlashedName(expected, Object.class);
            }

            computeDepsAndCheck(MyClassVisibility_private.class.getName(), apiOnly, expected);
        }
    }

    /*
     * To test that we correctly use 2 constant pool table indexes for 8-bytes
     * values.
     */

    public static class MyConstantPoolDoubleIndex {
        public double some_double = 1.1; // Inexact.
        public long some_long = -1L;
    }

    public void test_computeDependencies_constantPoolDoubleIndex() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, ClassDepsParserTest.class);
            }

            computeDepsAndCheck(MyConstantPoolDoubleIndex.class.getName(), apiOnly, expected);
        }
    }

    /*
     * Strings that look like classes.
     */

    public static class MyClassLikeStrings {
        public String notAClass_01 = "Lthis/is/not/a/class;";
        public String notAClass_02 = "this/is/not/a/class";
    }

    public void test_computeDependencies_classLikeStrings() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, ClassDepsParserTest.class);
            }
            //
            addSlashedName(expected, String.class);

            computeDepsAndCheck(MyClassLikeStrings.class.getName(), apiOnly, expected);
        }
    }

    /*
     * Advanced visibilities tests.
     * 
     * This test checks that protected is considered API,
     * and package-private not API.
     * Everywhere else, only public is used for API,
     * and private for non-API.
     */

    public static class MyVisibilityProtectedPackagePrivate {
        protected Integer foo;
        Long fool;
    }

    public void test_computeDependencies_visibilityProtectedPackagePrivate() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            addSlashedName(expected, Integer.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, ClassDepsParserTest.class);
                //
                addSlashedName(expected, Long.class);
            }

            computeDepsAndCheck(MyVisibilityProtectedPackagePrivate.class.getName(), apiOnly, expected);
        }
    }

    /*
     * Advanced signatures tests.
     * 
     * We have common code for all signatures, so not to bother
     * to test advanced ones for each place where they can appear,
     * we test them here.
     */

    public static class MyMethodGenSigWildcard {
        public Comparable<? extends Number> foo() {
            return null;
        }
    }

    public void test_computeDependencies_methodGenSigWildcard() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            addSlashedName(expected, Comparable.class);
            addSlashedName(expected, Number.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, ClassDepsParserTest.class);
            }

            computeDepsAndCheck(MyMethodGenSigWildcard.class.getName(), apiOnly, expected);
        }
    }

    public static class MyMethodGenSigLoose {
        public <E extends Number> Comparable<E> foo() {
            return null;
        }
    }

    public void test_computeDependencies_methodGenSigLoose() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            addSlashedName(expected, Comparable.class);
            addSlashedName(expected, Number.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, ClassDepsParserTest.class);
            }

            computeDepsAndCheck(MyMethodGenSigLoose.class.getName(), apiOnly, expected);
        }
    }

    /**
     * To test that we handle arrays types, here
     * "<E:Ljava/lang/Exception;>()[[TE;", "[[Ljava/lang/Error;",
     * and "[[[Z", by extracting the component type or ignoring it if
     * it's a primitive type (BaseType).
     */
    public static class MyArrayTypes {
        public <E extends Exception> E[][] toTab1() {
            return null;
        }
        public Error[][] toTab2() {
            return null;
        }
        public boolean[][][] toTab3() {
            return null;
        }
    }

    public void test_computeDependencies_arrayTypes() {
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            addSlashedName(expected, Exception.class);
            addSlashedName(expected, Error.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, ClassDepsParserTest.class);
            }

            computeDepsAndCheck(MyArrayTypes.class.getName(), apiOnly, expected);
        }
    }

    /*
     * 
     */

    public void test_dollarSign_$() {
        if (!HANDLE_WEIRD_DOLLAR_SIGN_USAGES) {
            return;
        }
        
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, $.$$.class);
            }

            computeDepsAndCheck($.class.getName(), apiOnly, expected);
        }
    }

    public void test_dollarSign_$$() {
        if (!HANDLE_WEIRD_DOLLAR_SIGN_USAGES) {
            return;
        }
        
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, $.class);
                addSlashedName(expected, $A.class);
                addSlashedName(expected, A$.class);
                addSlashedName(expected, A$$B.class);
            }

            computeDepsAndCheck($$.class.getName(), apiOnly, expected);
        }
    }

    public void test_dollarSign_$_$$() {
        if (!HANDLE_WEIRD_DOLLAR_SIGN_USAGES) {
            return;
        }
        
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            //
            if (apiOnly) {
            } else {
                addSlashedName(expected, $.class);
            }

            computeDepsAndCheck($.$$.class.getName(), apiOnly, expected);
        }
    }

    public void test_dollarSign_$A() {
        if (!HANDLE_WEIRD_DOLLAR_SIGN_USAGES) {
            return;
        }
        
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);
            
            computeDepsAndCheck($A.class.getName(), apiOnly, expected);
        }
    }

    public void test_dollarSign_A$() {
        if (!HANDLE_WEIRD_DOLLAR_SIGN_USAGES) {
            return;
        }
        
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);

            computeDepsAndCheck(A$.class.getName(), apiOnly, expected);
        }
    }

    public void test_dollarSign_A$$B() {
        if (!HANDLE_WEIRD_DOLLAR_SIGN_USAGES) {
            return;
        }
        
        for (boolean apiOnly : FALSE_TRUE) {

            final SortedSet<String> expected = new TreeSet<String>();
            //
            addSlashedName(expected, Object.class);

            computeDepsAndCheck(A$$B.class.getName(), apiOnly, expected);
        }
    }

    /*
     * Internal treatments.
     */

    public void test_addClassNameEventuallyFromArrayClassNameInto() {
        for (String className : new String[]{
                "L",
                "L/L",
                "a/L",
                "L/a",
                "a/b/C",
                "Z/L", // Z is for boolean primitive type.
        }) {
            for (int arrayLevel = 0; arrayLevel < 3; arrayLevel++) {
                String prefix = "";
                for (int i = 0; i < arrayLevel; i++) {
                    prefix += "[";
                }
                if (arrayLevel != 0) {
                    prefix += "L";
                }
                String suffix = (arrayLevel == 0) ? "" : ";";

                ArrayList<String> classNameList = new ArrayList<String>();
                ClassDepsParser.addClassNameEventuallyFromArrayClassNameInto(
                        prefix+className+suffix,
                        classNameList);

                assertEquals(1, classNameList.size());
                assertTrue(classNameList.contains(className));
            }
        }

        for (String primitiveName : new String[]{
                "B",
                "C",
                "D",
                "F",
                "I",
                "J",
                "S",
                "Z"
        }) {
            ArrayList<String> classNameList = new ArrayList<String>();
            ClassDepsParser.addClassNameEventuallyFromArrayClassNameInto(
                    "[" + primitiveName,
                    classNameList);

            assertEquals(0, classNameList.size());
        }
    }

    public void test_addClassNamesFromDescriptorInto() {
        for (String className : new String[]{
                "L",
                "L/L",
                "a/L",
                "L/a",
                "a/b/C",
        }) {
            ArrayList<String> classNameList = new ArrayList<String>();
            ClassDepsParser.addClassNamesFromDescriptorInto(
                    "L" + className + ";",
                    classNameList);

            assertEquals(1, classNameList.size());
            assertTrue(classNameList.contains(className));
        }
    }

    public void test_addClassNamesFromSignatureInto() {

        /*
         * Testing class names 'L' start, and ';' and '<' terminations.
         */

        for (String className : new String[]{
                "L",
                "L/L",
                "a/L",
                "L/a",
                "a/b/C",
        }) {
            for (String termination : new String[]{";","<"}) {
                ArrayList<String> classNameList = new ArrayList<String>();
                ClassDepsParser.addClassNamesFromSignatureInto(
                        "L" + className+termination,
                        classNameList);

                assertEquals(1, classNameList.size());
                assertTrue(classNameList.contains(className));
            }
        }

        /*
         * Testing generic type terminations.
         * 
         * When terminating with ';' or '<', generic type is preceded by 'T'.
         * When terminating with ':', it is not, but it might itself start with
         * 'T'.
         * It might also contain 'L', so when encountering ':' we must stop
         * considering we are parsing a class name.
         */

        for (String beforeTermination : new String[]{
                "L",
                "A",
                "T",
                "LL",
                "LA",
                "LT",
                "AL",
                "AA",
                "AT",
                "TL",
                "TA",
                "TT"
        }) {
            for (String termination : new String[]{";","<",":"}) {
                if ((!beforeTermination.startsWith("T"))
                        && beforeTermination.contains("L")
                        && (termination.equals(";")
                                || termination.equals("<"))) {
                    // Looks like a class name, not a generic type.
                    continue;
                }

                ArrayList<String> classNameList = new ArrayList<String>();
                ClassDepsParser.addClassNamesFromSignatureInto(
                        beforeTermination+termination + "Ljava/lang/Object;",
                        classNameList);

                assertEquals(1, classNameList.size());
                assertTrue(classNameList.contains("java/lang/Object"));
            }
        }

        /*
         * Testing class names '.' termination, and skipping of following
         * nested classes simple names.
         */

        for (String icsn : new String[]{
                "L",
                "A",
                "T",
                "LL",
                "LA",
                "LT",
                "AL",
                "AA",
                "AT",
                "TL",
                "TA",
                "TT"
        }) {
            for (boolean twice : FALSE_TRUE) {
                for (String termination : new String[]{";","<"}) {
                    ArrayList<String> classNameList = new ArrayList<String>();
                    ClassDepsParser.addClassNamesFromSignatureInto(
                            "Ljava/lang/Object." + icsn+(twice ? "." + icsn : "")+termination,
                            classNameList);

                    assertEquals(1, classNameList.size());
                    assertTrue(classNameList.contains("java/lang/Object"));
                }
            }
        }
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------

    /**
     * Black hole method in its own class,
     * for it not to cause direct dependency to test class,
     * which is also surrounding class of test classes.
     */
    private static class MyBlackHole {
        /**
         * Call that to ensure that the reference won't be optimized away by javac.
         * @return False.
         */
        public static boolean blackHole(Object obj) {
            // Always false, but javac doesn't know.
            if (StrictMath.cos((obj == null) ? 0 : obj.hashCode()) == 0.0) {
                System.out.println("can't happen");
                return true;
            } else {
                return false;
            }
        }
    }

    /*
     * 
     */

    private static boolean equal(Object a, Object b) {
        if (a == null) {
            return b == null;
        } else {
            return a.equals(b);
        }
    }

    private static String getClassFilePath(String className) {
        return COMPILATION_OUTPUT_DIR_PATH + "/" + slashed(className) + ".class";
    }

    private static String slashed(String className) {
        return className.replace('.','/');
    }

    private static void addSlashedName(Collection<String> coll, String className) {
        coll.add(slashed(className));
    }

    private static void addSlashedName(Collection<String> coll, Class<?> clazz) {
        addSlashedName(coll, clazz.getName());
    }

    /*
     * 
     */

    private void computeDepsAndCheck(
            String className,
            boolean apiOnly,
            SortedSet<String> expected) {

        final SortedSet<String> actual = new TreeSet<String>();
        final String thisClassName = computeDependencies(
                className,
                apiOnly,
                actual);

        // If class name is wrong, all hell breaks loose.
        assertEquals(slashed(className), thisClassName);

        final boolean depsOk = expected.equals(actual);

        if ((!depsOk) || DEBUG) {
            // Printing everything after computation,
            // which is handy in case of log spam during computation.
            System.out.println();
            System.out.println("apiOnly = " + apiOnly);
            System.out.println("class name: " + className);
            System.out.println("expected deps: " + expected);
            System.out.println("actual deps:   " + actual);
            printDiff(expected, actual);
        }

        assertTrue(depsOk);
    }

    private static String computeDependencies(
            String className,
            boolean apiOnly,
            Collection<String> jadecyDeps) {

        if (DEBUG) {
            System.out.println();
            System.out.println("apiOnly = " + apiOnly);
        }

        final String classFilePath = getClassFilePath(className);
        final String jadecyClassName = ClassDepsParser.computeDependencies(
                new File(classFilePath),
                apiOnly,
                jadecyDeps);

        if (DEBUG) {
            System.out.println();
            System.out.println("apiOnly = " + apiOnly);
        }

        // Comparing against jdeps only when debugging, for info,
        // in case it can help figuring out something.
        if (COMPARE_WITH_JDEPS) {
            final JdepsHelper jdepsHelper = new JdepsHelper(
                    JDEPS,
                    COMPILATION_OUTPUT_DIR_PATH);

            final SortedSet<String> jdepsDeps = new TreeSet<String>();
            final String jdepsClassName = jdepsHelper.computeDependencies(
                    className,
                    apiOnly,
                    jdepsDeps);

            final boolean ok =
                    equal(jdepsClassName, jadecyClassName)
                    && jdepsDeps.equals(jadecyDeps);

            System.out.println();
            if (classFilePath != null) {
                System.out.println("classFilePath =  " + classFilePath);
            }
            System.out.println("className =  " + className);
            System.out.println("jdepsClassName =  " + jdepsClassName);
            System.out.println("jadecyClassName = " + jadecyClassName);
            System.out.println("jdepsDeps =  " + jdepsDeps);
            System.out.println("jadecyDeps = " + jadecyDeps);
            // Repeating in case had a lot of logs.
            System.out.println("apiOnly = " + apiOnly);
            if (!ok) {
                printDiff(jdepsDeps, jadecyDeps);
            }

            if (ok) {
                System.out.println("same as jdeps");
            } else {
                System.out.println("not same as jdeps");
            }
        }

        return jadecyClassName;
    }

    private static void printDiff(
            Collection<String> expected,
            Collection<String> actual) {
        {
            final SortedSet<String> missing = new TreeSet<String>(expected);
            missing.removeAll(actual);
            if (missing.size() != 0) {
                System.out.println("missing : (" + missing.size() + ") " + missing);
            }
        }
        {
            final SortedSet<String> exceeding = new TreeSet<String>(actual);
            exceeding.removeAll(expected);
            if (exceeding.size() != 0) {
                System.out.println("exceeding : (" + exceeding.size() + ") " + exceeding);
            }
        }
    }
    
    /**
     * @return 8 for 1.8.x.y, 9 for 9.x.y, etc.
     */
    private static int getJavaVersion() {
        String verStr = System.getProperty("java.version");
        if (verStr.startsWith("1.")) {
            verStr = verStr.substring(2);
        }
        final int di = verStr.indexOf(".");
        if (di > 0) {
            verStr = verStr.substring(0, di);
        }
        final int ver = Integer.parseInt(verStr);
        return ver;
    }
}
