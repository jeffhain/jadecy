package net.jadecy;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import net.jadecy.build.JadecyBuildConfig;
import net.jadecy.code.InterfaceNameFilter;
import net.jadecy.code.NameFilters;
import net.jadecy.code.NameUtils;
import net.jadecy.parsing.ParsingFilters;

/**
 * Computes split packages in JAVA_HOME jars.
 */
public class SplitPackageSample {

    //--------------------------------------------------------------------------
    // CONFIGURATION
    //--------------------------------------------------------------------------

    private static final String JAVA_HOME = JadecyBuildConfig.getJdkHome();

    //--------------------------------------------------------------------------
    // PRIVATE CLASSES
    //--------------------------------------------------------------------------

    private static class MyPackageData {
        /**
         * To store jar files and corresponding classes for the package.
         */
        final Map<File,Set<String>> packageClassNameSetByJarFile =
                new HashMap<File,Set<String>>();
    }
    
    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    public static void main(String[] args) {
        
        /*
         * Creating Jadecy.
         */
        
        final boolean mustMergeNestedClasses = true;
        final boolean apiOnly = false;
        final Jadecy jdc = new Jadecy(
                mustMergeNestedClasses,
                apiOnly);
        
        /*
         * Computing jar files.
         */
        
        final ArrayList<File> jarFileList = new ArrayList<File>();
        addJarFilesInto(
                new File(JAVA_HOME),
                jarFileList);
        
        /*
         * Computing jar files for each package.
         */
        
        final Set<String> encounteredJarFileNameSet = new HashSet<String>();
        
        final SortedMap<String,MyPackageData> packageDataByPackageName =
                new TreeMap<String,MyPackageData>();

        for (File jarFile : jarFileList) {
            if (!encounteredJarFileNameSet.add(jarFile.getName())) {
                // To avoid noise due to jars appearing at multiple places.
                continue;
            }
            
            // We only want class files found in this jar.
            jdc.parser().getDefaultPackageData().clear();
            // Parsing.
            jdc.parser().accumulateDependencies(
                    jarFile,
                    ParsingFilters.defaultInstance());
            
            // Extracting packages names and byte size of their classes.
            final Map<String,Long> byteSizeByPackageName =
                    jdc.computeMatches(
                            ElemType.PACKAGE,
                            NameFilters.any());

            // Removing packages just present due to dependencies.
            removeKeysWithZeroByteSize(byteSizeByPackageName);
            
            for (Map.Entry<String,Long> entry : byteSizeByPackageName.entrySet()) {
                final String packageName = entry.getKey();

                MyPackageData packageData = packageDataByPackageName.get(packageName);
                if (packageData == null) {
                    // jarFile is the first defining classes for that package.
                    packageData = new MyPackageData();
                    packageDataByPackageName.put(packageName, packageData);
                }

                final InterfaceNameFilter classNameFilter;
                final boolean isDefaultPackage = packageName.equals(NameUtils.DEFAULT_PACKAGE_DISPLAY_NAME);
                if (isDefaultPackage) {
                    classNameFilter = NameFilters.not(NameFilters.contains("."));
                } else {
                    classNameFilter = NameFilters.startsWithName(packageName);
                }
                
                // Classes defined in the jar for that package (plus eventually imported ones).
                final Map<String,Long> byteSizeByPackageClassName =
                        jdc.computeMatches(
                                ElemType.CLASS,
                                classNameFilter);
                
                // Removing classes just present due to dependencies.
                removeKeysWithZeroByteSize(byteSizeByPackageClassName);
                
                packageData.packageClassNameSetByJarFile.put(jarFile, byteSizeByPackageClassName.keySet());
            }
        }
        
        /*
         * Synthesis.
         */
        
        System.out.println();
        System.out.println("split packages:");
        
        for (Map.Entry<String,MyPackageData> entry : packageDataByPackageName.entrySet()) {
            final String packageName = entry.getKey();
            final MyPackageData packageData = entry.getValue();
            
            if (packageData.packageClassNameSetByJarFile.size() > 1) {
                System.out.println();
                System.out.println(packageName + ":");
                for (Map.Entry<File,Set<String>> e2 : packageData.packageClassNameSetByJarFile.entrySet()) {
                    final File file = e2.getKey();
                    final Set<String> classNameSet = e2.getValue();
                    
                    System.out.println("   " + file + ":");
                    for (String className : classNameSet) {
                        System.out.println("      " + className);
                    }
                }
            }
        }
    }
    
    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    /**
     * This method is recursive.
     * 
     * @param dirFrom (in)
     * @param jarFileList (in,out)
     */
    private static void addJarFilesInto(
            File dirFrom,
            List<File> jarFileList) {
        final File[] fileArr = dirFrom.listFiles();
        if (fileArr != null) {
            for (File file : fileArr) {
                if (file.isDirectory()) {
                    addJarFilesInto(file, jarFileList);
                } else {
                    if (file.getName().endsWith(".jar")) {
                        jarFileList.add(file);
                    }
                }
            }
        }
    }
    
    private static void removeKeysWithZeroByteSize(Map<String,Long> byteSizeByKey) {
        for (Iterator<Map.Entry<String,Long>> it = byteSizeByKey.entrySet().iterator(); it.hasNext();) {
            final Map.Entry<String,Long> entry = it.next();
            if (entry.getValue().longValue() == 0) {
                it.remove();
            }
        }
    }
}
