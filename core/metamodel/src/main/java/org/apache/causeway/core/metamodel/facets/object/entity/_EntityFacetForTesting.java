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
package org.apache.causeway.core.metamodel.facets.object.entity;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.causeway.applib.query.Query;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.repository.EntityState;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.config.beans.PersistenceStack;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.object.ManagedObject;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class _EntityFacetForTesting implements EntityFacet {

    @Getter private final PersistenceStack persistenceStack;
    @Getter private final FacetHolder facetHolder;

    @Override
    public Class<? extends Facet> facetType() {
        return EntityFacet.class;
    }

    @Override
    public Precedence getPrecedence() {
        return Precedence.DEFAULT;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
    }

    @Override
    public Optional<String> identifierFor(final Object pojo) {
        throw _Exceptions.unsupportedOperation();
    }

    @Override
    public Bookmark validateBookmark(@NonNull final Bookmark bookmark) {
        return bookmark;
    }

    @Override
    public Optional<Object> fetchByBookmark(final Bookmark bookmark) {
        throw _Exceptions.unsupportedOperation();
    }

    @Override
    public Can<ManagedObject> fetchByQuery(final Query<?> query) {
        throw _Exceptions.unsupportedOperation();
    }

    @Override
    public void persist(final Object pojo) {
        throw _Exceptions.unsupportedOperation();
    }

    @Override
    public void refresh(final Object pojo) {
        throw _Exceptions.unsupportedOperation();
    }

    @Override
    public void delete(final Object pojo) {
        throw _Exceptions.unsupportedOperation();
    }

    @Override
    public EntityState getEntityState(final Object pojo) {
        throw _Exceptions.unsupportedOperation();
    }

    @Override
    public boolean isProxyEnhancement(final Method method) {
        throw _Exceptions.unsupportedOperation();
    }

    @Override
    public <T> T detach(final T pojo) {
        throw _Exceptions.unsupportedOperation();
    }

}
