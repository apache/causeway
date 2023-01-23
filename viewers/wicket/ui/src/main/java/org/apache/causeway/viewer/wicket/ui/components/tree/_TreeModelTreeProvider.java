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

import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.apache.causeway.commons.internal.collections._Lists;

/**
 * Wicket's {@link ITreeProvider} implemented for Causeway
 */
class _TreeModelTreeProvider implements ITreeProvider<_TreeNodeMemento> {

    private static final long serialVersionUID = 1L;

    /**
     * tree's root
     */
    private final _TreeNodeMemento primaryValue;
    private final _TreeModelTreeAdapter treeAdapter;

    _TreeModelTreeProvider(final _TreeNodeMemento primaryValue, final _TreeModelTreeAdapter treeAdapter) {
        this.primaryValue = primaryValue;
        this.treeAdapter = treeAdapter;
    }

    @Override
    public void detach() {
    }

    @Override
    public Iterator<? extends _TreeNodeMemento> getRoots() {
        return _Lists.singleton(primaryValue).iterator();
    }

    @Override
    public boolean hasChildren(final _TreeNodeMemento node) {
        return treeAdapter.childCountOf(node)>0;
    }

    @Override
    public Iterator<? extends _TreeNodeMemento> getChildren(final _TreeNodeMemento node) {
        return treeAdapter.childrenOf(node).iterator();
    }

    @Override
    public IModel<_TreeNodeMemento> model(final _TreeNodeMemento treeModel) {
        return Model.of(treeModel);
    }

}