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
package org.apache.causeway.core.metamodel.inspect.model;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.ViewModel;
import org.apache.causeway.applib.annotation.LabelPosition;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.graph.tree.TreeAdapter;
import org.apache.causeway.applib.graph.tree.TreeConverter;
import org.apache.causeway.applib.graph.tree.TreeNode;
import org.apache.causeway.applib.graph.tree.TreePath;

import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
abstract class TreeNodeVm<T, V extends TreeNodeVm<T, V>> implements ViewModel {

    @Programmatic
    protected final Class<T> nodeType;

    @Programmatic
    protected final T rootNode;

    @Programmatic
    protected final T activeNode;

    @Programmatic
    protected final TreePath activeTreePath;

    protected TreeNodeVm(final Class<T> nodeType, final T rootNode, final TreePath activeTreePath) {
        this.nodeType = nodeType;
        this.rootNode = rootNode;
        this.activeTreePath = activeTreePath;
        this.activeNode = getTreeAdapter().resolveRelative(rootNode, activeTreePath).orElseThrow();
    }

    @Property
    @PropertyLayout(labelPosition = LabelPosition.NONE, fieldSetId = "tree", sequence = "1")
    public TreeNode<V> getTree() {
        final TreeNode<V> tree = TreeNode.root(getViewModel(rootNode, null, 0), getTreeAdapterV());

        // expand the current node
        activeTreePath.streamUpTheHierarchyStartingAtSelf()
            .forEach(tree::expand);

        // mark active node as selected
        tree.select(activeTreePath);

        return tree;
    }

    private @NonNull TreeAdapter<V> getTreeAdapterV() {
        return getTreeAdapter().convert(new TreeConverter<T, V>() {
            @Override
            public V fromUnderlyingNode(final T value, final V parentNode, final int siblingIndex) {
                return getViewModel(value, parentNode, siblingIndex);
            }
            @Override
            public T toUnderlyingNode(final V value) {
                return value.activeNode;
            }
        });
    }

    protected abstract V getViewModel(T node, @Nullable V parentNode, int siblingIndex);
    protected abstract TreeAdapter<T> getTreeAdapter();

}
