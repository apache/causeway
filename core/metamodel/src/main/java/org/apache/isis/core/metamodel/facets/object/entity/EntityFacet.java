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
import java.util.Optional;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.repository.EntityState;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.config.beans.PersistenceStack;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.object.ManagedObject;
import org.apache.isis.core.metamodel.object.MmSpecUtil;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

/**
 * Indicates that this class is managed by a persistence context.
 * @since 2.0
 */
public interface EntityFacet extends Facet {

    /**
     * The {@link ObjectSpecification} of the entity type this
     * facet is associated with.
     */
    default ObjectSpecification getEntitySpecification() {
        return (ObjectSpecification)getFacetHolder();
    }

    /**
     * Optionally the stringified OID,
     * based on whether the entity has one associated.
     * @throws IllegalArgumentException if the pojo's class is not recognized
     *      by the persistence layer
     */
    Optional<String> identifierFor(@Nullable Object pojo);

    /**
     * Optionally the {@link Bookmark},
     * based on whether the entity has an OID associated.
     * eg. it has not if not persisted yet
     * @throws IllegalArgumentException if the pojo's class is not recognized
     *      by the persistence layer or does not exactly match the expected
     */
    default Optional<Bookmark> bookmarkFor(final @Nullable Object pojo) {
        return identifierFor(pojo)
                .map(id->Bookmark.forLogicalTypeAndIdentifier(
                        MmSpecUtil.quicklyResolveObjectSpecificationFor(
                                getEntitySpecification(),
                                pojo.getClass())
                        .getLogicalType(),
                        id));
    }

    default Bookmark bookmarkForElseFail(final @Nullable Object pojo) {
        return bookmarkFor(pojo)
                .orElseThrow(()->_Exceptions.noSuchElement("entity has no OID: %s",
                        getEntitySpecification().getLogicalType()));
    }


    /**
     * Optionally the entity pojo corresponding to given {@link Bookmark},
     * based on whether could be found.
     */
    Optional<Object> fetchByBookmark(Bookmark bookmark);

    Can<ManagedObject> fetchByQuery(Query<?> query);

    void persist(Object pojo);

    void refresh(Object pojo);

    void delete(Object pojo);

    EntityState getEntityState(Object pojo);

    /**
     * Whether given method originates from byte code mangling.
     * @param method
     */
    boolean isProxyEnhancement(Method method);

    <T> T detach(T pojo);

    PersistenceStack getPersistenceStack();

    // -- JUNIT SUPPORT

    static EntityFacet forTesting(
            final PersistenceStack persistenceStandard,
            final FacetHolder facetHolder) {
        return new _EntityFacetForTesting(persistenceStandard, facetHolder);
    }



}
