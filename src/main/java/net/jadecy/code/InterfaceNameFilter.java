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
package net.jadecy.code;

/**
 * Interface for filtering names, such as classes names.
 * 
 * getPrefix() method allow to eventually greatly speed up graphs constructions,
 * by filtering out non-prefix-matching subtrees of classes or packages,
 * instead of having to systematically check all names.
 * On the other hand, getPrefix() method doesn't complicate things much at usage
 * since it can be ignored, nor for usual filters creations if extending
 * AbstractNameFilter or using NameFilters utility treatments.
 */
public interface InterfaceNameFilter {
    
    /**
     * @return String names must start with, or be a start of, before usage
     *         of accept(...) method. Must not be null.
     *         Use empty string to allow for anything.
     *         Can use an illegal first char (like '0') to allow for nothing
     *         efficiently.
     */
    public String getPrefix();
    
    /**
     * @param name A name. Must not be null.
     * @return True if the specified name is accepted, false otherwise.
     */
    public boolean accept(String name);
}
