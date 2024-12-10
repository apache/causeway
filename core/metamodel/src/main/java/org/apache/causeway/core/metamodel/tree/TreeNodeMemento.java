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
package org.apache.causeway.core.metamodel.tree;

import java.io.Serializable;
import java.util.Objects;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.graph.tree.TreePath;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtils;

import lombok.NonNull;

/**
 * Memento for a pair of {@link Bookmark} and {@link TreePath}.
 */
@Programmatic
public record TreeNodeMemento(
    /**
     * If null, then only memoizes the treePath.
     * @see #isTreePathMemento
     */
    Bookmark bookmark,
    TreePath treePath,
    int hashCodePrecalc) implements Serializable {

    public static TreeNodeMemento mementify(final Object pojo, final TreePath treePath) {
        return new TreeNodeMemento(
            treePath,
            ManagedObject.adaptSingular(MetaModelContext.instanceElseFail().getSpecificationLoader(), pojo)
                .getBookmark()
                .orElseThrow());
    }

    public TreeNodeMemento(final @NonNull TreePath treePath) {
        this(null, treePath, Objects.hash(0, treePath.hashCode()));
    }

    public TreeNodeMemento(final @NonNull TreePath treePath, final @NonNull Bookmark bookmark) {
        this(bookmark, treePath, Objects.hash(bookmark.hashCode(), treePath.hashCode()));
    }

    public boolean isTreePathMemento() {
        return bookmark==null;
    }

    public @Nullable Object getPojo() {
        return MmUnwrapUtils.single(asManagedObject());
    }

    public ManagedObject asManagedObject() {
        _Assert.assertFalse(isTreePathMemento());
        return MetaModelContext.instanceElseFail().getObjectManager().loadObjectElseFail(bookmark);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof TreeNodeMemento other
            ? Objects.equals(this.treePath, other.treePath)
                    && Objects.equals(this.bookmark, other.bookmark)
            : false;
    }

    @Override
    public int hashCode() {
        return hashCodePrecalc;
    }

}