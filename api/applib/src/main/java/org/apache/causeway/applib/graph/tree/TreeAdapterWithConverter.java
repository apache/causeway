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
import java.util.function.Function;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.functional.IndexedFunction;

/**
 * Acts as a TreeAdapter facade by wrapping an underlying {@link TreeAdapter}
 * and translating the node type back and forth using a {@link TreeConverter}.
 * @param <U> underlying tree node generic type
 * @param <T> tree node generic type of this facade
 */
record TreeAdapterWithConverter<U, T>(
        TreeAdapter<U> underlyingAdapter,
        TreeConverter<U, T> converter)
implements TreeAdapter<T>{

    @Override
    public final int childCountOf(final @Nullable T t) {
        if(t==null) return 0;
        var underlyingNode = converter().toUnderlyingNode(t);
        return underlyingNode!=null
                ? underlyingAdapter().childCountOf(underlyingNode)
                : 0;
    }

    @Override
    public final Stream<T> childrenOf(final @Nullable T t) {
        if(t==null) return Stream.empty();
        var underlyingNode = converter().toUnderlyingNode(t);
        return underlyingNode!=null
                ? underlyingAdapter().childrenOf(underlyingNode)
                        .map(childFactoryForParentNode(t))
                : Stream.empty();
    }

    @Override
    public final Optional<T> resolveRelative(final @Nullable T t, final @Nullable TreePath relativePath) {
        if(t==null) return Optional.empty();
        var underlyingNode = converter().toUnderlyingNode(t);
        return underlyingNode!=null
                ? underlyingAdapter().resolveRelative(underlyingNode, relativePath)
                        .map(childFactoryForParentNode(t))
                : Optional.empty();
    }

    // -- HELPER

    private Function<U, T> childFactoryForParentNode(final T parentNode) {
        return IndexedFunction.zeroBased((indexWithinSiblings, pojo)->
            converter().fromUnderlyingNode(pojo, parentNode, indexWithinSiblings));
    }

}
