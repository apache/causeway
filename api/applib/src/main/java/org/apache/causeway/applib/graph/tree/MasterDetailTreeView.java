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

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.ViewModel;
import org.apache.causeway.applib.annotation.LabelPosition;
import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.annotation.PropertyLayout;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

/**
 * Base class for a master/detail viewmodel, with a tree property acting as the master,
 * whereas the details need to be provided be the subclass.
 * <p>
 * No layout is provided, which requires for the implementing viewmodel subclass to provide its own.
 *
 * @param <T> common node type of the tree
 * @param <V> type of the implementing viewmodel subclass
 * @since 3.2
 */
@AllArgsConstructor
public abstract class MasterDetailTreeView<T, V extends MasterDetailTreeView<T, V>> implements ViewModel {

    /**
     * Common underlying node type of the tree.
     */
    @Programmatic @Getter(value = AccessLevel.PROTECTED) @Accessors(fluent=true)
    private final Class<T> nodeType;

    /**
     * Root node of the underlying tree.
     */
    @Programmatic @Getter(value = AccessLevel.PROTECTED) @Accessors(fluent=true) 
    private final T rootNode;

    /**
     * Active/selected node of the underlying tree, to which the view's details should be bound.
     */
    @Programmatic @Getter(value = AccessLevel.PROTECTED) @Accessors(fluent=true) 
    private final T activeNode;

    /**
     * {@link TreePath} of the active node.
     * @see #activeNode
     */
    @Programmatic @Getter(value = AccessLevel.PROTECTED) @Accessors(fluent=true) 
    private final TreePath activeTreePath;

    /**
     * Constructs the viewmodel with given root-node and details activated/selected for given tree-path.
     */
    protected MasterDetailTreeView(final Class<T> nodeType, final T rootNode, final TreePath activeTreePath) {
        this.nodeType = nodeType;
        this.rootNode = rootNode;
        this.activeTreePath = activeTreePath;
        this.activeNode = treeAdapter().resolveRelative(rootNode, activeTreePath).orElseThrow();
    }

    /**
     * Tree that acts as the master to this master detail view.
     */
    @Property
    @PropertyLayout(labelPosition = LabelPosition.NONE, fieldSetId = "tree", sequence = "1")
    public TreeNode<V> getTree() {
        final TreeNode<V> tree = TreeNode.root(viewModel(rootNode, null, 0), treeAdapterV());

        // expand the current node
        activeTreePath.streamUpTheHierarchyStartingAtSelf()
            .forEach(tree::expand);

        // mark active node as selected
        tree.select(activeTreePath);

        return tree;
    }

    /**
     * Creates or does lookup the viewmodel for given underlying tree-node.
     * @param node tree-node of the underlying tree-model
     * @param parentNode viewmodel that is the logical parent to the returned viewmodel
     * @param siblingIndex index of the returned viewmodel in relation to its siblings, that share the same logical parent
     */
    @Programmatic
    protected abstract V viewModel(T node, @Nullable V parentNode, int siblingIndex);

    /**
     * {@link TreeAdapter} for the underlying tree model.
     */
    @Programmatic
    protected abstract TreeAdapter<T> treeAdapter();

    // -- HELPER

    private @NonNull TreeAdapter<V> treeAdapterV() {
        return treeAdapter().convert(new TreeConverter<T, V>() {
            @Override
            public V fromUnderlyingNode(final T value, final V parentNode, final int siblingIndex) {
                return viewModel(value, parentNode, siblingIndex);
            }
            @Override
            public T toUnderlyingNode(final V value) {
                return value.activeNode();
            }
        });
    }

}
