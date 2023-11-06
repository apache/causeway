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
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.graph.tree.TreeAdapter;
import org.apache.causeway.applib.graph.tree.TreePath;
import org.apache.causeway.commons.functional.IndexedFunction;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.wicket.model.util.WktContext;

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

    private transient TreeAdapter wrappedTreeAdapter;
    private transient MetaModelContext metaModelContext;

    _TreeModelTreeAdapter(
            final MetaModelContext metaModelContext,
            final Class<? extends TreeAdapter> treeAdapterClass) {
        this.metaModelContext = metaModelContext;
        this.treeAdapterClass = treeAdapterClass;
    }

    @Override
    public MetaModelContext getMetaModelContext() {
        return this.metaModelContext = WktContext.computeIfAbsent(metaModelContext);
    }

    @Override
    public Optional<_TreeNodeMemento> parentOf(final _TreeNodeMemento treeModel) {
        if(treeModel==null) {
            return Optional.empty();
        }
        val pojoNode = demementify(treeModel);
        if(pojoNode==null) {
            return Optional.empty();
        }
        return wrappedTreeAdapter().parentOf(pojoNode)
                .map(pojo->mementify(pojo, treeModel.getTreePath().getParentIfAny()));
    }

    @Override
    public int childCountOf(final _TreeNodeMemento treeModel) {
        if(treeModel==null) {
            return 0;
        }
        val pojoNode = demementify(treeModel);
        if(pojoNode==null) {
            return 0;
        }
        return wrappedTreeAdapter().childCountOf(pojoNode);
    }

    @Override
    public Stream<_TreeNodeMemento> childrenOf(final _TreeNodeMemento treeModel) {
        if(treeModel==null) {
            return Stream.empty();
        }
        val pojoNode = demementify(treeModel);
        if(pojoNode==null) {
            return Stream.empty();
        }
        return wrappedTreeAdapter().childrenOf(pojoNode)
                .map(newPojoToTreeModelMapper(treeModel));
    }

    _TreeNodeMemento mementify(final @NonNull Object pojo, final TreePath treePath) {
        return new _TreeNodeMemento(
                ManagedObject.adaptSingular(getSpecificationLoader(), pojo).getBookmark().orElseThrow(),
                treePath);
    }
    private @Nullable Object demementify(final _TreeNodeMemento model) {
        Objects.requireNonNull(model);
        return model.getPojo();
    }

    private Function<Object, _TreeNodeMemento> newPojoToTreeModelMapper(final _TreeNodeMemento parent) {
        return IndexedFunction.zeroBased((indexWithinSiblings, pojo)->
        mementify(pojo, parent.getTreePath().append(indexWithinSiblings)));
    }

    private TreeAdapter wrappedTreeAdapter() {
        if(wrappedTreeAdapter!=null) {
            return wrappedTreeAdapter;
        }
        try {
            return wrappedTreeAdapter = getFactoryService().getOrCreate(treeAdapterClass);
        } catch (Exception e) {
            throw new RuntimeException("failed to instantiate tree adapter", e);
        }
    }

}