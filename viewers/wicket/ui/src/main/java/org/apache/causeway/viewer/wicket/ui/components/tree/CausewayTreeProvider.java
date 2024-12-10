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

import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.graph.tree.TreeAdapter;
import org.apache.causeway.applib.graph.tree.TreeAdapterWithConverter;
import org.apache.causeway.applib.graph.tree.TreeConverter;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.metamodel.tree.TreeAdapterRecord;
import org.apache.causeway.core.metamodel.tree.TreeNodeMemento;

/**
 * Wicket's {@link ITreeProvider} implemented for Causeway.
 */
record CausewayTreeProvider(
    /** tree's single root */
    TreeNodeMemento primaryValue,
    TreeAdapterRecord<Object> treeAdapterRecord)
implements
    ITreeProvider<TreeNodeMemento>,
    TreeConverter<Object, TreeNodeMemento> {

    private static final long serialVersionUID = 1L;

    CausewayTreeProvider(final TreeNodeMemento primaryValue, final TreeAdapter<?> treeAdapter) {
        this(primaryValue,_Casts.<TreeAdapterRecord<Object>>uncheckedCast(new TreeAdapterRecord<>(treeAdapter)));
    }

    @Override
    public void detach() {
    }

    @Override
    public Iterator<TreeNodeMemento> getRoots() {
        return List.of(primaryValue).iterator();
    }

    @Override
    public boolean hasChildren(final TreeNodeMemento node) {
        return new TreeAdapterWithConverter<Object, TreeNodeMemento>(treeAdapterRecord.treeAdapter(), this)
            .childCountOf(node)>0;
    }

    @Override
    public Iterator<? extends TreeNodeMemento> getChildren(final TreeNodeMemento node) {
        var children = new TreeAdapterWithConverter<Object, TreeNodeMemento>(treeAdapterRecord.treeAdapter(), this)
            .childrenOf(node)
            .toList();
        return children.iterator();
    }

    @Override
    public IModel<TreeNodeMemento> model(final TreeNodeMemento treeModel) {
        return Model.of(treeModel);
    }

    // -- TREE CONVERTER

    @Override
    public TreeNodeMemento fromUnderlyingNode(
            final Object pojoNode, final TreeNodeMemento parentNode, final int siblingIndex) {
        return TreeNodeMemento.mementify(pojoNode, parentNode.treePath().append(siblingIndex));
    }

    @Override
    @Nullable
    public Object toUnderlyingNode(final TreeNodeMemento node) {
        return node!=null
                ? node.getPojo()
                : null;
    }

}