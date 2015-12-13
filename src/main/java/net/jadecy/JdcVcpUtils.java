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
package net.jadecy;

import java.util.Collection;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import net.jadecy.code.AbstractCodeData;
import net.jadecy.code.ClassData;
import net.jadecy.code.PackageData;
import net.jadecy.graph.InterfaceVertex;

/**
 * Utils for Jadecy's vertex collection processors.
 */
class JdcVcpUtils {

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------
    
    public static String[] causesToStringArr(SortedSet<ClassData> depCauses) {
        final String[] causes = new String[depCauses.size()];
        int i = 0;
        for (ClassData depCause : depCauses) {
            causes[i++] = depCause.displayName();
        }
        return causes;
    }

    public static void addCausesInto(
            PackageData fromPackageData,
            PackageData toPackageData,
            Collection<String> causes) {
        // Causes names.
        final SortedSet<ClassData> depCauses =
                fromPackageData.causeSetBySuccessor().get(toPackageData);
        for (ClassData depCause : depCauses) {
            causes.add(depCause.displayName());
        }
    }

    /**
     * @param restrictionSet (in) Successors not in it are ignored
     *        (for not polluting results with dangling edges).
     * @param vertex (in)
     * @param causesByDepByName (in,out)
     */
    public static void addCausesByDepForVertex(
            ElemType elemType,
            Set<InterfaceVertex> restrictionSet,
            InterfaceVertex vertex,
            SortedMap<String,SortedMap<String,SortedSet<String>>> causesByDepByName) {

        final AbstractCodeData vertexD = (AbstractCodeData) vertex;

        final SortedMap<String,SortedSet<String>> causesByDep =
                new TreeMap<String,SortedSet<String>>();
        for (InterfaceVertex succ : vertex.successors()) {
            final AbstractCodeData succD = (AbstractCodeData) succ;
            if (restrictionSet.contains(succ)) {
                final SortedSet<String> causes = new TreeSet<String>();
                if (elemType == ElemType.PACKAGE) {
                    final PackageData fromPackageData = (PackageData) vertex;
                    final PackageData toPackageData = (PackageData) succ;
                    addCausesInto(fromPackageData, toPackageData, causes);
                }
                causesByDep.put(succD.displayName(), causes);
            }
        }
        final Object forCheck =
                causesByDepByName.put(vertexD.displayName(), causesByDep);
        if (forCheck != null) {
            throw new AssertionError();
        }
    }
    
    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    private JdcVcpUtils() {
    }
}
