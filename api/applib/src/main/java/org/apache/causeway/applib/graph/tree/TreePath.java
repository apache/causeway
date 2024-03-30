/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.applib.graph.tree;

import java.io.Serializable;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.functional.IndexedConsumer;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.primitives._Ints;

/**
 * Provides an unambiguous way to address nodes by position within a tree-structure. Examples:
 * <ul>
 * <li>/0 ... the tree root</li>
 * <li>/0/1 ... the second child of root</li>
 * <li>/0/0/0 ... the first child of first child of root</li>
 * </ul>
 * @since 2.0 {@index}
 */
public interface TreePath extends Serializable {

    /**
     * Number of path-elements.
     * @apiNote Root has size = 1.
     */
    public int size();
    
    /**
     * @param indexWithinSiblings
     * @return a new TreePath instance composed of this with one canonical path entry added
     */
    public TreePath append(int indexWithinSiblings);
    
    /**
     * Returns a sub-path containing all the path-elements of this path, skipping
     * startIndex number of path elements at the start.
     * @apiNote The first element of the resulting path indicates the sibling index 
     *      of the tree-node the subPath corresponds to.
     */
    public TreePath subPath(int startIndex);
    
    /**
     * Returns a TreePath instance that represents the parent path of this TreePath,
     * if this is not the root.
     */
    public @Nullable TreePath getParentIfAny();
    
    public boolean isRoot();
    public boolean startsWith(TreePath other);

    public IntStream streamPathElements();
    
    /**
     * Optionally the 2nd path-element's value, based on presence.
     * It corresponds to the sibling index of the child node this tree-path 
     * (either directly references or) includes.
     */
    public OptionalInt childIndex();

    public String stringify(String delimiter);

    public Stream<TreePath> streamUpTheHierarchyStartingAtSelf();

    // -- CONSTRUCTION

    public static TreePath of(final int ... canonicalPath) {
        return new TreePath_Default(canonicalPath);
    }

    public static TreePath root() {
        return of(0);
    }

    /**
     * Parses stringified tree path of format {@code <delimiter>0<delimiter>3<delimiter>1} ...,
     * as returned by {@link TreePath#stringify(String)}.
     * <p>
     * For null or empty input the root is returned.
     * @throws IllegalArgumentException if parsing fails
     */
    public static TreePath parse(final @Nullable String treePathStringified, final String delimiter) {
        if(_Strings.isNullOrEmpty(treePathStringified)) {
            return root();
        }
        _Assert.assertTrue(_Strings.isNotEmpty(delimiter), ()->"non-empty delimiter required");

        // parse the input String into a list of integers
        final List<Integer> pathElementsAsList =
            _Strings.splitThenStream(treePathStringified, delimiter)
            .filter(_Strings::isNotEmpty)
            .map(pathElement->
                _Ints.parseInt(pathElement, 10)
                .orElseThrow(()->
                    _Exceptions.illegalArgument("illformed treePath '%s' while parsing element '%s' using delimiter '%s'",
                            treePathStringified, pathElement, delimiter)))
            .collect(Collectors.toList());

        // convert the list of integers into an array of int
        final int[] canonicalPath = new int[pathElementsAsList.size()];
        pathElementsAsList.forEach(IndexedConsumer.zeroBased((index, value)->canonicalPath[index] = value));

        return of(canonicalPath);
    }

}
