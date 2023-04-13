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

import java.util.Set;
import java.util.stream.Collectors;

import org.apache.wicket.model.IModel;

import org.apache.causeway.applib.graph.tree.TreePath;

/**
 * Wicket's model for collapse/expand state
 */
class _TreeExpansionModel implements IModel<Set<_TreeNodeMemento>> {
    private static final long serialVersionUID = 648152234030889164L;

    public static _TreeExpansionModel of(
            final Set<TreePath> expandedTreePaths) {
        return new _TreeExpansionModel( expandedTreePaths);
    }

    /**
     * Happens on user interaction via UI.
     * @param t
     */
    public void onExpand(final _TreeNodeMemento t) {
        expandedTreePaths.add(t.getTreePath());
    }

    /**
     * Happens on user interaction via UI.
     * @param t
     */
    public void onCollapse(final _TreeNodeMemento t) {
        expandedTreePaths.remove(t.getTreePath());
    }

    public boolean contains(final TreePath treePath) {
        return expandedTreePaths.contains(treePath);
    }

    private final Set<TreePath> expandedTreePaths;
    private final Set<_TreeNodeMemento> expandedNodes;

    private _TreeExpansionModel(
            final Set<TreePath> expandedTreePaths) {

        this.expandedTreePaths = expandedTreePaths;
        this.expandedNodes = expandedTreePaths.stream()
                .map(tPath->new _TreeNodeMemento(tPath))
                .collect(Collectors.toSet());
    }

    @Override
    public Set<_TreeNodeMemento> getObject() {
        return expandedNodes;
    }

    @Override
    public String toString() {
        return "{" + expandedTreePaths.stream()
        .map(TreePath::toString)
        .collect(Collectors.joining(", ")) + "}";
    }

}