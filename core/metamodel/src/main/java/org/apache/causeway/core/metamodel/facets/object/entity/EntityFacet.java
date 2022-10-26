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
import java.util.function.Function;

import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import org.apache.causeway.applib.query.Query;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.IdStringifier;
import org.apache.causeway.applib.services.repository.EntityState;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.config.beans.PersistenceStack;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmSpecUtil;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.NonNull;

/**
 * Indicates that this class is managed by a persistence context.
 * @since 2.0
 */
public interface EntityFacet extends Facet {

    @lombok.Value(staticConstructor = "of")
    static class PrimaryKeyType<T> {
        private final @NonNull Class<?> owningEntityClass;
        private final @NonNull IdStringifier<T> idStringifier;
        private final @NonNull Class<T> primaryKeyClass;
        public String enstring(final T primaryKey) {
            return idStringifier.enstring(primaryKey);
        }
        public String enstringWithCast(final Object primaryKey) {
            return _Casts.castTo(primaryKeyClass, primaryKey)
            .map(idStringifier::enstring)
            .orElseThrow(()->_Exceptions.illegalArgument(
                    "failed to cast primary-key '%s' to expected type %s",
                        ""+primaryKey,
                        primaryKeyClass.getName()))
            ;
        }
        public T destring(final String stringifiedPrimaryKey) {
            return idStringifier.destring(owningEntityClass, stringifiedPrimaryKey);
        }
        /** shallow PK detection */
        public boolean isValid(final @NonNull Object primaryKey) {
            return _Casts.castTo(primaryKeyClass, primaryKey)
                    .map(idStringifier::isValid)
                    .orElse(false);
        }
        public static <T> PrimaryKeyType<T> getInstance(
                final @NonNull Class<?> owningEntityClass,
                final @NonNull Function<Class<T>, IdStringifier<T>> stringifierLookup,
                final @NonNull Class<T> primaryKeyClass){
            return of(
                    owningEntityClass,
                    stringifierLookup.apply(primaryKeyClass),
                    _Casts.uncheckedCast(ClassUtils.resolvePrimitiveIfNecessary(primaryKeyClass)));
        }
    }

    /**
     * The {@link ObjectSpecification} of the entity type this
     * facet is associated with.
     */
    default ObjectSpecification getEntitySpecification() {
        return (ObjectSpecification)getFacetHolder();
    }

    /**
     * Introduced purely for optimization purposes.
     * @implNote if possible memoizes the fact as to whether
     *      services were already injected into given pojo,
     *      and if so allows to skip any consecutive injection attempts
     */
    default boolean isInjectionPointsResolved(final @Nullable Object pojo) {
        return pojo==null;
    }

    /**
     * Optionally the stringified OID,
     * based on whether the entity has one associated.
     * @throws IllegalArgumentException if the pojo's class is not recognized
     *      by the persistence layer
     */
    Optional<String> identifierFor(@Nullable Object pojo);

    Bookmark validateBookmark(@NonNull Bookmark bookmark);

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
                        MmSpecUtil.quicklyResolveObjectSpecification(
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
