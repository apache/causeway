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
import java.util.function.Function;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.graph.tree.TreeAdapter;
import org.apache.causeway.applib.graph.tree.TreePath;
import org.apache.causeway.commons.functional.IndexedFunction;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;

import lombok.NonNull;
import lombok.val;

/**
 *  {@link TreeAdapter} for _TreeModel nodes.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
class _TreeModelTreeAdapter
implements
    TreeAdapter<_TreeNodeMemento>,
    HasMetaModelContext,
    Serializable {

    private static final long serialVersionUID = 1L;

    private final Class<? extends TreeAdapter> treeAdapterClass;

    /** non serializable delegate */
    private transient TreeAdapter delegate;

    _TreeModelTreeAdapter(
            final Class<? extends TreeAdapter> treeAdapterClass) {
        this.treeAdapterClass = treeAdapterClass;
    }

    @Override
    public int childCountOf(final @Nullable _TreeNodeMemento treeModel) {
        val pojoNode = demementify(treeModel);
        if(pojoNode==null) {
            return 0;
        }
        return delegateTreeAdapter().childCountOf(pojoNode);
    }

    @Override
    public Stream<_TreeNodeMemento> childrenOf(final @Nullable _TreeNodeMemento treeModel) {
        val pojoNode = demementify(treeModel);
        if(pojoNode==null) {
            return Stream.empty();
        }
        return delegateTreeAdapter().childrenOf(pojoNode)
                .map(newPojoToTreeModelMapper(treeModel));
    }

    // -- HELPER
    
    _TreeNodeMemento mementify(final @NonNull Object pojo, final @NonNull TreePath treePath) {
        return new _TreeNodeMemento(
                treePath,
                ManagedObject.adaptSingular(getSpecificationLoader(), pojo).getBookmark().orElseThrow());
    }
    
    private @Nullable Object demementify(final @Nullable _TreeNodeMemento model) {
        return model!=null
                ? model.getPojo()
                : null;
    }

    private Function<Object, _TreeNodeMemento> newPojoToTreeModelMapper(final _TreeNodeMemento parent) {
        return IndexedFunction.zeroBased((indexWithinSiblings, pojo)->
        mementify(pojo, parent.getTreePath().append(indexWithinSiblings)));
    }

    private TreeAdapter delegateTreeAdapter() {
        if(delegate!=null) {
            return delegate;
        }
        try {
            return delegate = getFactoryService().getOrCreate(treeAdapterClass);
        } catch (Exception e) {
            throw new RuntimeException("failed to instantiate tree adapter", e);
        }
    }

}