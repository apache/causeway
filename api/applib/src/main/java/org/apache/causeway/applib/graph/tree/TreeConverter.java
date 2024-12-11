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

/**
 * @param <U> underlying tree generic type
 * @param <T> converted tree generic type
 */
public interface TreeConverter<U, T> {

    /**
     * Creates a converted node value from the (underlying) node value, also providing context,
     * that is, passing in the resulting node's parent-node and the resulting node's sibling index.
     */
    T fromUnderlyingNode(U value, T parentNode, int siblingIndex);

    /**
     * Recovers the original (underlying) node value from given converted node value.
     */
    U toUnderlyingNode(T value);

}
