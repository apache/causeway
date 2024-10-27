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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.annotation.Value;
import org.apache.causeway.commons.functional.IndexedConsumer;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Refs;
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
@Value
public class TreePath implements Serializable {
    
    // -- FACTORIES

    public static TreePath of(final int ... canonicalPath) {
        return new TreePath(canonicalPath);
    }

    public static TreePath root() {
        return of(0);
    }
    
    // -- CONSTRUCTION 
    
    private static final long serialVersionUID = 530511373409525896L;
    private final int[] canonicalPath;
    private final int hashCode;
    
    public TreePath(final int[] canonicalPath) {
        Objects.requireNonNull(canonicalPath, "canonicalPath is required");
        if(canonicalPath.length<1) {
            throw new IllegalArgumentException("canonicalPath must not be empty");
        }
        this.canonicalPath = canonicalPath;
        this.hashCode = Arrays.hashCode(canonicalPath);
    }

    /**
     * Number of path-elements.
     * @apiNote Root has size = 1.
     */
    public int size() {
        return canonicalPath.length;
    }
    
    /**
     * @param indexWithinSiblings
     * @return a new TreePath instance composed of this with one canonical path entry added
     */
    public TreePath append(int indexWithinSiblings) {
        final int[] newCanonicalPath = new int[canonicalPath.length+1];
        System.arraycopy(canonicalPath, 0, newCanonicalPath, 0, canonicalPath.length);
        newCanonicalPath[canonicalPath.length] = indexWithinSiblings;
        return new TreePath(newCanonicalPath);
    }
    
    /**
     * Returns a sub-path containing all the path-elements of this path, skipping
     * startIndex number of path elements at the start.
     * @apiNote The first element of the resulting path indicates the sibling index 
     *      of the tree-node the subPath corresponds to.
     */
    public TreePath subPath(int startIndex) {
        if(startIndex<=0) return this;
        if(startIndex>=size()) throw new IndexOutOfBoundsException(startIndex);
        final int newSize = size() - startIndex; 
        final int[] newCanonicalPath = new int[newSize];
        System.arraycopy(canonicalPath, startIndex, newCanonicalPath, 0, newSize);
        return new TreePath(newCanonicalPath);

    }
    
    /**
     * Returns a TreePath instance that represents the parent path of this TreePath,
     * if this is not the root.
     */
    public @Nullable TreePath getParentIfAny() {
        if(isRoot()) {
            return null;
        }
        final int[] newCanonicalPath = new int[canonicalPath.length-1];
        System.arraycopy(canonicalPath, 0, newCanonicalPath, 0, canonicalPath.length-1);
        return new TreePath(newCanonicalPath);

    }
    
    public boolean isRoot() {
        return canonicalPath.length==1;
    }
    
    public boolean startsWith(TreePath other) {
        if(other.size()>this.size()) return false;
        // optimization, not strictly required
        if(other instanceof TreePath) {
            final int lastIndexToCheck = other.size() - 1;
            return Arrays.equals(
                    this.canonicalPath, 0, lastIndexToCheck, 
                    ((TreePath)other).canonicalPath, 0, lastIndexToCheck);    
        }
        return this.stringify("/").startsWith(other.stringify("/"));
    }

    public IntStream streamPathElements() {
        return IntStream.of(canonicalPath);
    }
    
    /**
     * Optionally the 2nd path-element's value, based on presence.
     * It corresponds to the sibling index of the child node this tree-path 
     * (either directly references or) includes.
     */
    public OptionalInt childIndex() {
        return size()>=2
                ? OptionalInt.of(canonicalPath[1])
                : OptionalInt.empty();
    }

    public String stringify(String delimiter) {
        _Assert.assertTrue(_Strings.isNotEmpty(delimiter), ()->"non-empty delimiter required");
        return delimiter + streamPathElements()
            .mapToObj(i->""+i)
            .collect(Collectors.joining(delimiter));
    }

    public Stream<TreePath> streamUpTheHierarchyStartingAtSelf() {
        var hasMore = _Refs.booleanRef(true);
        return Stream.iterate((TreePath)this, __->hasMore.isTrue(), TreePath::getParentIfAny)
                .filter(x->{
                    if(x.isRoot()) {
                        hasMore.setValue(false); // stop the stream only after we have included the root
                    }
                    return true;
                });
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

    // -- OBJECT CONTRACTS

    @Override
    public boolean equals(final Object obj) {
        if(obj instanceof TreePath) {
            final TreePath other = (TreePath) obj;
            return Arrays.equals(canonicalPath, other.canonicalPath);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        return stringify("/");
    }

}
