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
package net.jadecy.names;

import net.jadecy.utils.ArgsUtils;

/**
 * Utility to create name filters.
 */
public class NameFilters {
    
    //--------------------------------------------------------------------------
    // PRIVATE CLASSES
    //--------------------------------------------------------------------------

    /**
     * The opposite of a filter.
     */
    private static class MyFilterNot extends AbstractNameFilter {
        private final InterfaceNameFilter filter;
        public MyFilterNot(InterfaceNameFilter filter) {
            ArgsUtils.requireNonNull(filter);
            this.filter = filter;
        }
        @Override
        public String toString() {
            return "not(" + this.filter + ")";
        }
        @Override
        public boolean accept(String name) {
            return !this.filter.accept(name);
        }
    }

    /**
     * Intersection of filters.
     * If no filter, accepts nothing.
     */
    private static class MyFilterAnd extends AbstractNameFilter {
        private final InterfaceNameFilter[] filters;
        private final String prefix;
        private final boolean isNoneFilterForSure;
        public MyFilterAnd(InterfaceNameFilter[] filters) {
            ArgsUtils.requireNonNull2(filters);
            
            this.filters = filters;
            
            String optimisticPrefix = null;
            if ((filters.length != 0)
                    && ((optimisticPrefix = computeUnionPrefix(filters)) != null)) {
                this.prefix = optimisticPrefix;
                this.isNoneFilterForSure = false;
            } else {
                this.prefix = NONE_PREFIX;
                this.isNoneFilterForSure = true;
            }
        }
        @Override
        public String toString() {
            return "and(" + arrToString(this.filters) + ")";
        }
        @Override
        public String getPrefix() {
            return this.prefix;
        }
        @Override
        public boolean accept(String name) {
            if (this.isNoneFilterForSure) {
                // Easy.
                return false;
            } else {
                for (InterfaceNameFilter filter : this.filters) {
                    if (!filter.accept(name)) {
                        return false;
                    }
                }
                return true;
            }
        }
    }

    /**
     * Union of filters.
     * If no filter, accepts nothing.
     */
    private static class MyFilterOr extends AbstractNameFilter {
        private final InterfaceNameFilter[] filters;
        private final String prefix;
        public MyFilterOr(InterfaceNameFilter[] filters) {
            ArgsUtils.requireNonNull2(filters);
            this.filters = filters;
            this.prefix = computeIntersectionPrefix(filters);
        }
        @Override
        public String toString() {
            return "or(" + arrToString(this.filters) + ")";
        }
        @Override
        public String getPrefix() {
            return this.prefix;
        }
        @Override
        public boolean accept(String name) {
            for (InterfaceNameFilter filter : this.filters) {
                if (filter.accept(name)) {
                    return true;
                }
            }
            return false;
        }
    }

    /*
     * 
     */
    
    private static class MyFilterAny extends AbstractNameFilter {
        public MyFilterAny() {
        }
        @Override
        public String toString() {
            return "any";
        }
        @Override
        public boolean accept(String name) {
            return true;
        }
    }

    private static class MyFilterNone extends AbstractNameFilter {
        public MyFilterNone() {
        }
        @Override
        public String toString() {
            return "none";
        }
        @Override
        public String getPrefix() {
            return NONE_PREFIX;
        }
        @Override
        public boolean accept(String name) {
            return false;
        }
    }
    
    /*
     * 
     */

    private static class MyFilterEqualsName extends AbstractNameFilter {
        private final String name;
        public MyFilterEqualsName(String name) {
            ArgsUtils.requireNonNull(name);
            this.name = name;
        }
        @Override
        public String toString() {
            return "equalsName(" + this.name + ")";
        }
        @Override
        public String getPrefix() {
            return this.name;
        }
        @Override
        public boolean accept(String name) {
            return name.equals(this.name);
        }
    }

    private static class MyFilterStartsWith extends AbstractNameFilter {
        private final String prefix;
        public MyFilterStartsWith(String prefix) {
            ArgsUtils.requireNonNull(prefix);
            this.prefix = prefix;
        }
        @Override
        public String toString() {
            return "startsWith(" + this.prefix + ")";
        }
        @Override
        public String getPrefix() {
            return this.prefix;
        }
        @Override
        public boolean accept(String name) {
            return name.startsWith(this.prefix);
        }
    }

    private static class MyFilterEndsWith extends AbstractNameFilter {
        private final String suffix;
        public MyFilterEndsWith(String suffix) {
            ArgsUtils.requireNonNull(suffix);
            this.suffix = suffix;
        }
        @Override
        public String toString() {
            return "endsWith(" + this.suffix + ")";
        }
        @Override
        public boolean accept(String name) {
            return name.endsWith(this.suffix);
        }
    }

    private static class MyFilterContains extends AbstractNameFilter {
        private final String part;
        public MyFilterContains(String part) {
            ArgsUtils.requireNonNull(part);
            this.part = part;
        }
        @Override
        public String toString() {
            return "contains(" + this.part + ")";
        }
        @Override
        public boolean accept(String name) {
            return name.contains(this.part);
        }
    }
    
    private static class MyFilterMatches extends AbstractNameFilter {
        private final String regex;
        public MyFilterMatches(String regex) {
            ArgsUtils.requireNonNull(regex);
            this.regex = regex;
        }
        @Override
        public String toString() {
            return "matches(" + this.regex + ")";
        }
        @Override
        public boolean accept(String name) {
            return name.matches(this.regex);
        }
    }
    
    /*
     * Creating classes for xxxName filters, for simpler toString() output
     * and faster speed using NameUtils.xxxName(...) methods.
     */
    
    private static class MyFilterStartsWithName extends AbstractNameFilter {
        private final String beginName;
        public MyFilterStartsWithName(String beginName) {
            ArgsUtils.requireNonNull(beginName);
            this.beginName = beginName;
        }
        @Override
        public String toString() {
            return "startsWithName(" + this.beginName + ")";
        }
        @Override
        public String getPrefix() {
            return this.beginName;
        }
        @Override
        public boolean accept(String name) {
            return NameUtils.startsWithName(name, this.beginName);
        }
    }

    private static class MyFilterEndsWithName extends AbstractNameFilter {
        private final String endName;
        public MyFilterEndsWithName(String endName) {
            ArgsUtils.requireNonNull(endName);
            this.endName = endName;
        }
        @Override
        public String toString() {
            return "endsWithName(" + this.endName + ")";
        }
        @Override
        public String getPrefix() {
            return "";
        }
        @Override
        public boolean accept(String name) {
            return NameUtils.endsWithName(name, this.endName);
        }
    }

    private static class MyFilterContainsName extends AbstractNameFilter {
        private final String name;
        public MyFilterContainsName(String name) {
            ArgsUtils.requireNonNull(name);
            this.name = name;
        }
        @Override
        public String toString() {
            return "containsName(" + this.name + ")";
        }
        @Override
        public String getPrefix() {
            return "";
        }
        @Override
        public boolean accept(String name) {
            return NameUtils.containsName(name, this.name);
        }
    }

    //--------------------------------------------------------------------------
    // MEMBERS
    //--------------------------------------------------------------------------

    /**
     * Prefix to use when we know we don't accept anything
     * (no valid name can start with it).
     */
    static final String NONE_PREFIX = "0";
    
    private static final InterfaceNameFilter ANY = new MyFilterAny();

    private static final InterfaceNameFilter NONE = new MyFilterNone();

    //--------------------------------------------------------------------------
    // PUBLIC METHODS
    //--------------------------------------------------------------------------

    /**
     * NB: Uses String.startsWith(...), which looks at chars,
     * not at Unicode code points.
     *  
     * @return True if the specified name is longer than the specified prefix
     *         and starts with it, or if the prefix is longer and starts with
     *         the name.
     * @throws NullPointerException if any argument is null.
     */
    public static boolean areCompatible(
            String prefix,
            String name) {
        // Implicit null checks.
        if (name.length() >= prefix.length()) {
            return name.startsWith(prefix);
        } else {
            return prefix.startsWith(name);
        }
    }

    /*
     * 
     */
    
    /**
     * Always returns the same filter.
     * 
     * @return A filter accepting any name.
     */
    public static InterfaceNameFilter any() {
        return ANY;
    }
    
    /**
     * Always returns the same filter.
     * 
     * @return A filter accepting no name.
     */
    public static InterfaceNameFilter none() {
        return NONE;
    }
    
    /*
     * 
     */
    
    /**
     * @return A filter accepting names not accepted by the specified filter.
     * @throws NullPointerException if argument is null.
     */
    public static InterfaceNameFilter not(InterfaceNameFilter filter) {
        return new MyFilterNot(filter);
    }
    
    /**
     * If no filter, accepts nothing.
     * 
     * @return A filter accepting names accepted by both of the specified filters.
     * @throws NullPointerException if argument or any filter is null.
     */
    public static InterfaceNameFilter and(InterfaceNameFilter... filters) {
        return new MyFilterAnd(filters);
    }
    
    /**
     * If no filter, accepts nothing.
     * 
     * @return A filter accepting names accepted by either of the specified filters.
     * @throws NullPointerException if argument or any filter is null.
     */
    public static InterfaceNameFilter or(InterfaceNameFilter... filters) {
        return new MyFilterOr(filters);
    }
    
    /*
     * 
     */

    /**
     * @param name A name. Must not be null.
     * @return A filter only accepting the specified name.
     * @throws NullPointerException if argument is null.
     */
    public static InterfaceNameFilter equalsName(String name) {
        return new MyFilterEqualsName(name);
    }

    /*
     * 
     */
    
    /**
     * @param prefix A prefix. Must not be null.
     * @return A filter only accepting names starting with the specified prefix.
     * @throws NullPointerException if argument is null.
     */
    public static InterfaceNameFilter startsWith(String prefix) {
        return new MyFilterStartsWith(prefix);
    }

    /**
     * @param beginName A package or class name, like "a", "a.b.D$E", or an empty string.
     * @return A filter only accepting names starting with the specified name,
     *         i.e. all names if it is empty, else names equal to it,
     *         or starting with it followed by '.' or '$'.
     * @throws NullPointerException if argument is null.
     */
    public static InterfaceNameFilter startsWithName(String beginName) {
        return new MyFilterStartsWithName(beginName);
    }

    /*
     * 
     */
    
    /**
     * @param suffix A suffix. Must not be null.
     * @return A filter only accepting names ending with the specified suffix.
     * @throws NullPointerException if argument is null.
     */
    public static InterfaceNameFilter endsWith(String suffix) {
        return new MyFilterEndsWith(suffix);
    }
    
    /**
     * @param endName The eventually dot-truncated end of a package or class name,
     *        like "a", "a.b.D$E", or an empty string.
     * @return A filter only accepting names ending with the specified name part,
     *         i.e. all names if it is empty, else names equal to it,
     *         or ending with it preceded by '.' or '$'.
     * @throws NullPointerException if argument is null.
     */
    public static InterfaceNameFilter endsWithName(String endName) {
        return new MyFilterEndsWithName(endName);
    }

    /*
     * 
     */
    
    /**
     * @param part A part of a name. Must not be null.
     * @return A filter only accepting names containing the specified part.
     * @throws NullPointerException if argument is null.
     */
    public static InterfaceNameFilter contains(String part) {
        return new MyFilterContains(part);
    }

    /**
     * @param name The eventually dot-truncated part of a package or class name,
     *        like "a", "a.b.D$E", or an empty string.
     * @return A filter only accepting names containing the specified name part,
     *         i.e. all names if it is empty, else names equal to it,
     *         or starting with it followed by '.' or '$',
     *         or ending with it preceded by '.' or '$',
     *         or containing it preceded and followed by '.' or '$'.
     * @throws NullPointerException if argument is null.
     */
    public static InterfaceNameFilter containsName(String name) {
        return new MyFilterContainsName(name);
    }

    /*
     * 
     */
    
    /**
     * @param regex A regular expression. Must not be null.
     * @return A filter only accepting names matching the specified regex.
     * @throws NullPointerException if argument is null.
     */
    public static InterfaceNameFilter matches(String regex) {
        return new MyFilterMatches(regex);
    }

    //--------------------------------------------------------------------------
    // PRIVATE METHODS
    //--------------------------------------------------------------------------
    
    private NameFilters() {
    }

    /*
     * 
     */

    /**
     * Returns Integer.MAX_VALUE if no filter.
     */
    private static int computePrefixMinLength(InterfaceNameFilter[] filters) {
        int minLength = Integer.MAX_VALUE;
        
        for (InterfaceNameFilter filter : filters) {
            final String prefix = filter.getPrefix();
            if (prefix.length() < minLength) {
                minLength = prefix.length();
                if (minLength == 0) {
                    break;
                }
            }
        }
        
        return minLength;
    }
    
    /*
     * 
     */
    
    /**
     * Returns null if there is no filter or at least one incompatibility.
     * 
     * @return The smallest prefix which starts with each of the prefixes,
     *         or null if there is no such prefix.
     */
    private static String computeUnionPrefix(InterfaceNameFilter[] filters) {
        
        if (filters.length == 0) {
            return null;
        }

        /*
         * Cache-friendly memory access pattern.
         */
        
        String currentLongestP = filters[0].getPrefix();
        
        // For each other prefix...
        for (int i = 1; i < filters.length; i++) {
            final String pi = filters[i].getPrefix();
            final int commonLength = Math.min(
                    currentLongestP.length(),
                    pi.length());
            // ...we check equality on the common part...
            for (int j = 0; j < commonLength; j++) {
                if (pi.charAt(j) != currentLongestP.charAt(j)) {
                    // Divergence.
                    return null;
                }
            }
            // ...and if the other prefix is longer, using it as new reference.
            if (pi.length() > currentLongestP.length()) {
                currentLongestP = pi;
            }
        }
        
        // No divergence.
        return currentLongestP;
    }

    /**
     * Returns NONE_PREFIX if there is no filter.
     * 
     * @return The longest prefix each of the prefixes starts with.
     */
    private static String computeIntersectionPrefix(InterfaceNameFilter[] filters) {
        
        if (filters.length == 0) {
            return NONE_PREFIX;
        }
        
        if (filters.length == 1) {
            // Easy.
            return filters[0].getPrefix();
        }
        
        final int maxCommonPrefixLength = computePrefixMinLength(filters);
        if (maxCommonPrefixLength == 0) {
            // Easy.
            return "";
        }
        
        /*
         * Cache-friendly memory access pattern.
         */
        
        final String p0 = filters[0].getPrefix();
        int currentEndIndexExcl = maxCommonPrefixLength;
        
        // For each other prefix...
        for (int i = 1; i < filters.length; i++) {
            final String pi = filters[i].getPrefix();
            // ...computing the first index where we find a divergence...
            for (int j = 0; j < currentEndIndexExcl; j++) {
                if (pi.charAt(j) != p0.charAt(j)) {
                    // ...which we use as new first excluded index
                    // for the common prefix.
                    currentEndIndexExcl = j;
                    break;
                }
            }
            if (currentEndIndexExcl == 0) {
                // Can't get lower.
                break;
            }
        }
        
        if (currentEndIndexExcl == 0) {
            return "";
        } else {
            // Returns p0 if full length so no need to check that.
            return p0.substring(0, currentEndIndexExcl);
        }
    }
    
    /*
     * 
     */
    
    private static String arrToString(InterfaceNameFilter[] filterArr) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < filterArr.length; i++) {
            if (i != 0) {
                sb.append(",");
            }
            sb.append(filterArr[i]);
        }
        return sb.toString();
    }
}
