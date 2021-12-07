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
package org.apache.isis.core.metamodel.facets.object.entity;

import java.lang.reflect.Method;
import java.util.function.BiConsumer;

import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.repository.EntityState;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class _EntityFacetForTesting implements EntityFacet {

    @Getter private final PersistenceStandard persistenceStandard;
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
    public String identifierFor(final ObjectSpecification spec, final Object pojo) {
        throw _Exceptions.unsupportedOperation();
    }

    @Override
    public ManagedObject fetchByIdentifier(final ObjectSpecification spec, final Bookmark bookmark) {
        throw _Exceptions.unsupportedOperation();
    }

    @Override
    public Can<ManagedObject> fetchByQuery(final ObjectSpecification spec, final Query<?> query) {
        throw _Exceptions.unsupportedOperation();
    }

    @Override
    public void persist(final ObjectSpecification spec, final Object pojo) {
        throw _Exceptions.unsupportedOperation();
    }

    @Override
    public void refresh(final Object pojo) {
        throw _Exceptions.unsupportedOperation();
    }

    @Override
    public void delete(final ObjectSpecification spec, final Object pojo) {
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
