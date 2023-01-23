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

import org.apache.causeway.applib.graph.tree.TreeAdapter;
import org.apache.causeway.applib.graph.tree.TreePath;
import org.apache.causeway.commons.functional.IndexedFunction;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.wicket.model.util.WktContext;

import lombok.NonNull;

/**
 *  {@link TreeAdapter} for _TreeModel nodes.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
class _TreeModelTreeAdapter
implements
    TreeAdapter<_TreeModel>,
    HasMetaModelContext,
    Serializable {

    private static final long serialVersionUID = 1L;

    private final Class<? extends TreeAdapter> treeAdapterClass;

    private transient TreeAdapter wrappedTreeAdapter;
    private transient MetaModelContext metaModelContext;

    _TreeModelTreeAdapter(
            final MetaModelContext mmc,
            final Class<? extends TreeAdapter> treeAdapterClass) {
        this.metaModelContext = mmc;
        this.treeAdapterClass = treeAdapterClass;
    }

    @Override
    public MetaModelContext getMetaModelContext() {
        return this.metaModelContext = WktContext.computeIfAbsent(metaModelContext);
    }

    @Override
    public Optional<_TreeModel> parentOf(final _TreeModel treeModel) {
        if(treeModel==null) {
            return Optional.empty();
        }
        return wrappedTreeAdapter().parentOf(unwrap(treeModel))
                .map(pojo->wrap(pojo, treeModel.getTreePath().getParentIfAny()));
    }

    @Override
    public int childCountOf(final _TreeModel treeModel) {
        if(treeModel==null) {
            return 0;
        }
        return wrappedTreeAdapter().childCountOf(unwrap(treeModel));
    }

    @Override
    public Stream<_TreeModel> childrenOf(final _TreeModel treeModel) {
        if(treeModel==null) {
            return Stream.empty();
        }
        return wrappedTreeAdapter().childrenOf(unwrap(treeModel))
                .map(newPojoToTreeModelMapper(treeModel));
    }

    _TreeModel wrap(final @NonNull Object pojo, final TreePath treePath) {
        return new _TreeModel(
                getMetaModelContext(),
                ManagedObject.adaptSingular(getSpecificationLoader(), pojo),
                treePath);
    }

    private Object unwrap(final _TreeModel model) {
        Objects.requireNonNull(model);
        return model.getObject().getPojo();
    }

    private Function<Object, _TreeModel> newPojoToTreeModelMapper(final _TreeModel parent) {
        return IndexedFunction.zeroBased((indexWithinSiblings, pojo)->
        wrap(pojo, parent.getTreePath().append(indexWithinSiblings)));
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