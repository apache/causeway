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

import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.annotation.Domain;

/**
 * Provides the parent/child relationship information between pojos
 * to derive a tree-structure.
 *
 * @param <T> type of the tree nodes that make up the tree structure
 *
 * @since 2.0 {@index}
 */
public interface TreeAdapter<T> {

    /**
     * @param value - tree-node (pojo)
     * @return number of child tree-nodes of the specified {@code value} tree-node (pojo)
     */
    @Domain.Exclude
    default int childCountOf(final T value) {
        return Math.toIntExact(childrenOf(value).count());
    }

    /**
     * @param value - tree-node (pojo)
     * @return stream of child tree-nodes of the specified {@code value} tree-node (pojo)
     */
    @Domain.Exclude
    Stream<T> childrenOf(T value);

    /**
     * Resolves given {@link TreePath} to its corresponding sub-node relative to given node if possible.
     * <p>
     * E.g. starting from root, '/0' will return the root;<br>
     * starting from root, '/0/2' will return the 3rd child of root;<br>
     * starting from sub-node '/0/2', '/2/9' will resolve the 10th child ('/0/2/9') of this sub-node
     */
    @Domain.Exclude
    default Optional<T> resolveRelative(final @Nullable T node, final @Nullable TreePath relativePath) {
        if(node==null
                || relativePath==null
                || relativePath.size()<1) {
            return Optional.empty();
        }

        // if relativePath is of size 1, then the path points to given start node
        // (in this case we simply ignore the sibling index)
        if(relativePath.size()==1) return Optional.of(node);

        // at this point relativePath is of size >= 2, so we should have a child index
        final int childIndex = relativePath.childIndex().orElse(-1);
        if(childIndex<0) return Optional.empty();

        final Optional<T> childNode = childrenOf(node)
                .skip(childIndex)
                .findFirst();
        if(!childNode.isPresent()) return Optional.empty();

        // recursively calls itself, if there are still children left to resolve (when size of path > 2)
        return relativePath.size()>2
                ? resolveRelative(childNode.get(), relativePath.subPath(1))
                : childNode;
    }

}
