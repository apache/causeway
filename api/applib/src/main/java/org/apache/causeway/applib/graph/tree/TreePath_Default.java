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

import java.util.Arrays;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Refs;
import org.apache.causeway.commons.internal.base._Strings;

import lombok.NonNull;
import lombok.val;

/**
 * Package private mixin for TreePath.
 */
class TreePath_Default implements TreePath {

    private static final long serialVersionUID = 530511373409525896L;
    private final int[] canonicalPath;
    private final int hashCode;

    TreePath_Default(final int[] canonicalPath) {
        Objects.requireNonNull(canonicalPath, "canonicalPath is required");
        if(canonicalPath.length<1) {
            throw new IllegalArgumentException("canonicalPath must not be empty");
        }
        this.canonicalPath = canonicalPath;
        this.hashCode = Arrays.hashCode(canonicalPath);
    }

    @Override
    public int size() {
        return canonicalPath.length;
    }
    
    @Override
    public TreePath append(final int indexWithinSiblings) {
        final int[] newCanonicalPath = new int[canonicalPath.length+1];
        System.arraycopy(canonicalPath, 0, newCanonicalPath, 0, canonicalPath.length);
        newCanonicalPath[canonicalPath.length] = indexWithinSiblings;
        return new TreePath_Default(newCanonicalPath);
    }

    @Override
    public TreePath getParentIfAny() {
        if(isRoot()) {
            return null;
        }
        final int[] newCanonicalPath = new int[canonicalPath.length-1];
        System.arraycopy(canonicalPath, 0, newCanonicalPath, 0, canonicalPath.length-1);
        return new TreePath_Default(newCanonicalPath);
    }

    @Override
    public boolean isRoot() {
        return canonicalPath.length==1;
    }
    
    @Override
    public boolean startsWith(TreePath other) {
        if(other.size()>this.size()) return false;
        // optimization, not strictly required
        if(other instanceof TreePath_Default) {
            final int lastIndexToCheck = other.size() - 1;
            return Arrays.equals(
                    this.canonicalPath, 0, lastIndexToCheck, 
                    ((TreePath_Default)other).canonicalPath, 0, lastIndexToCheck);    
        }
        return this.stringify("/").startsWith(other.stringify("/"));
    }
    
    @Override
    public OptionalInt childIndex() {
        return size()>=2
                ? OptionalInt.of(canonicalPath[1])
                : OptionalInt.empty();
    }
    
    @Override
    public TreePath subPath(int startIndex) {
        if(startIndex<=0) return this;
        if(startIndex>=size()) throw new IndexOutOfBoundsException(startIndex);
        final int newSize = size() - startIndex; 
        final int[] newCanonicalPath = new int[newSize];
        System.arraycopy(canonicalPath, startIndex, newCanonicalPath, 0, newSize);
        return new TreePath_Default(newCanonicalPath);
    }

    @Override
    public String stringify(@NonNull final String delimiter) {
        _Assert.assertTrue(_Strings.isNotEmpty(delimiter), ()->"non-empty delimiter required");
        return delimiter + streamPathElements()
            .mapToObj(i->""+i)
            .collect(Collectors.joining(delimiter));
    }

    @Override
    public IntStream streamPathElements() {
        return IntStream.of(canonicalPath);
    }

    @Override
    public Stream<TreePath> streamUpTheHierarchyStartingAtSelf() {
        val hasMore = _Refs.booleanRef(true);
        return Stream.iterate((TreePath)this, __->hasMore.isTrue(), TreePath::getParentIfAny)
                .filter(x->{
                    if(x.isRoot()) {
                        hasMore.setValue(false); // stop the stream only after we have included the root
                    }
                    return true;
                });
    }

    // -- OBJECT CONTRACTS

    @Override
    public boolean equals(final Object obj) {
        if(obj instanceof TreePath_Default) {
            final TreePath_Default other = (TreePath_Default) obj;
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
