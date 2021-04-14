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
package org.apache.isis.applib.graph.tree;

import java.util.Optional;
import java.util.stream.Stream;

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
     * @return the parent tree-node (pojo) of the specified {@code value} tree-node (pojo)
     */
    public Optional<T> parentOf(T value);

    /**
     * @param value - tree-node (pojo)
     * @return number of child tree-nodes of the specified {@code value} tree-node (pojo)
     */
    public int childCountOf(T value);

    /**
     * @param value - tree-node (pojo)
     * @return stream of child tree-nodes of the specified {@code value} tree-node (pojo)
     */
    public Stream<T> childrenOf(T value);

}
