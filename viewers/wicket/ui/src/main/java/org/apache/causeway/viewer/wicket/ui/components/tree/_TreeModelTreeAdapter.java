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

import java.io.Serializable;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.graph.tree.TreeAdapter;
import org.apache.causeway.applib.graph.tree.TreeAdapterWithConverter;
import org.apache.causeway.applib.graph.tree.TreeConverter;
import org.apache.causeway.applib.graph.tree.TreePath;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.tree.TreeAdapterRecord;
import org.apache.causeway.core.metamodel.tree.TreeNodeMemento;

/**
 *  {@link TreeAdapter} for _TreeModel nodes.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
record _TreeModelTreeAdapter(
    TreeAdapterRecord<Object> treeAdapterRecord)
implements
    TreeAdapter<TreeNodeMemento>,
    TreeConverter<Object, TreeNodeMemento>,
    HasMetaModelContext,
    Serializable {

    _TreeModelTreeAdapter(
            final TreeAdapter delegate) {
        this(new TreeAdapterRecord(delegate));
    }

    @Override
    public Stream<TreeNodeMemento> childrenOf(final TreeNodeMemento value) {
        return new TreeAdapterWithConverter<>(underlyingAdapter(), this)
            .childrenOf(value);
    }

    // -- TREE CONVERTER

    @Override
    public TreeNodeMemento fromUnderlyingNode(
            final Object pojoNode, final TreeNodeMemento parentNode, final int siblingIndex) {
        return mementify(pojoNode, parentNode.treePath().append(siblingIndex));
    }

    @Override
    public @Nullable Object toUnderlyingNode(final TreeNodeMemento node) {
        return node!=null
                ? node.getPojo()
                : null;
    }

    // -- TREE ADAPTER WITH CONVERTER

    protected TreeConverter<Object, TreeNodeMemento> converter() {
        return this;
    }

    protected TreeAdapter<Object> underlyingAdapter() {
        return treeAdapterRecord.treeAdapter();
    }

    // -- HELPER

    TreeNodeMemento mementify(final Object pojo, final TreePath treePath) {
        return new TreeNodeMemento(
                treePath,
                ManagedObject.adaptSingular(getSpecificationLoader(), pojo)
                    .getBookmark()
                    .orElseThrow());
    }

}