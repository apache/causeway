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

import java.io.Serializable;

import javax.annotation.Nullable;

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
     * @param indexWithinSiblings
     * @return a new TreePath instance composed of this with one canonical path entry added
     */
    public TreePath append(int indexWithinSiblings);

    /**
     *
     * @return a new TreePath instance that represents the parent path of this
     */
    public @Nullable TreePath getParentIfAny();

    public boolean isRoot();

    // -- CONSTRUCTION

    public static TreePath of(final int ... canonicalPath) {
        return new TreePath_Default(canonicalPath);
    }

    public static TreePath root() {
        return of(0);
    }

}
