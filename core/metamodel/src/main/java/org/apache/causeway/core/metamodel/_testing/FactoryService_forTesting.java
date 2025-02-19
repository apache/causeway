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
package org.apache.causeway.core.metamodel._testing;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.graph.tree.TreeNode;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facets.object.navchild.ObjectTreeAdapter;

import org.jspecify.annotations.NonNull;
import lombok.SneakyThrows;

record FactoryService_forTesting(
    MetaModelContext metaModelContext) implements FactoryService {

    @SneakyThrows
    @Override
    public <T> T getOrCreate(final Class<T> requiredType) {
        return requiredType.getDeclaredConstructor().newInstance();
    }

    @SneakyThrows
    @Override
    public <T> T get(final Class<T> requiredType) {
        return requiredType.getDeclaredConstructor().newInstance();
    }

    @SneakyThrows
    @Override
    public <T> T detachedEntity(final Class<T> domainClass) {
        return domainClass.getDeclaredConstructor().newInstance();
    }

    @Override
    public <T> T detachedEntity(final @NonNull T entity) {
        return entity;
    }

    @Override
    public <T> T mixin(final Class<T> mixinClass, final Object mixedIn) {
        throw new IllegalArgumentException("Not yet supported");
    }

    @Override
    public <T> T viewModel(final Class<T> viewModelClass, final @Nullable Bookmark bookmark) {
        throw new IllegalArgumentException("Not yet supported");
    }

    @Override
    public <T> T viewModel(final T viewModel) {
        return viewModel;
    }

    @SneakyThrows
    @Override
    public <T> T create(final Class<T> domainClass) {
        return domainClass.getDeclaredConstructor().newInstance();
    }

    @Override
    public <T> TreeNode<T> treeNode(final T root) {
        return TreeNode.root(root, _Casts.uncheckedCast(new ObjectTreeAdapter(metaModelContext.getSpecificationLoader())));
    }

}
