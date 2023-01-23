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
package org.apache.causeway.viewer.wicket.ui.components.tree;

import java.util.NoSuchElementException;
import java.util.Objects;

import org.apache.wicket.model.LoadableDetachableModel;

import org.apache.causeway.applib.graph.tree.TreePath;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.viewer.wicket.model.util.WktContext;

import lombok.val;

/**
 * Wicket's loadable/detachable model for TreeModel nodes.
 */
class _LoadableDetachableTreeModel
extends LoadableDetachableModel<_TreeModel>
implements HasMetaModelContext {
    private static final long serialVersionUID = 1L;

    private final Bookmark bookmark;
    private final TreePath treePath;
    private final int hashCode;

    private transient MetaModelContext metaModelContext;

    public _LoadableDetachableTreeModel(final _TreeModel tModel) {
        super(tModel);
        this.treePath = tModel.getTreePath();
        this.bookmark = ManagedObjects.bookmarkElseFail(tModel.getObject());

        this.hashCode = Objects.hash(bookmark.hashCode(), treePath.hashCode());
        this.metaModelContext = tModel.getMetaModelContext();
    }

    /*
     * loads EntityModel using Oid (id)
     */
    @Override
    protected _TreeModel load() {

        val objAdapter = getObjectManager()
                .loadObject(bookmark)
                .orElseThrow(()->new NoSuchElementException(
                        String.format("Tree creation: could not recreate TreeModel from Bookmark: '%s'", bookmark)));

        final Object pojo = objAdapter.getPojo();
        if(pojo==null) {
            throw new NoSuchElementException(
                    String.format("Tree creation: could not recreate Pojo from Oid: '%s'", bookmark));
        }

        return new _TreeModel(getMetaModelContext(), objAdapter, treePath);
    }

    @Override
    public MetaModelContext getMetaModelContext() {
        return this.metaModelContext = WktContext.computeIfAbsent(metaModelContext);
    }

    /*
     * Important! Models must be identifiable by their contained object. Also IDs must be
     * unique within a tree structure.
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof _LoadableDetachableTreeModel) {
            final _LoadableDetachableTreeModel other = (_LoadableDetachableTreeModel) obj;
            return treePath.equals(other.treePath) && bookmark.equals(other.bookmark);
        }
        return false;
    }

    /*
     * Important! Models must be identifiable by their contained object.
     */
    @Override
    public int hashCode() {
        return hashCode;
    }
}