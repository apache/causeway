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
package org.apache.isis.core.metamodel.objectmanager;

import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.isis.core.metamodel.objectmanager.create.ObjectCreator;
import org.apache.isis.core.metamodel.objectmanager.detach.ObjectDetacher;
import org.apache.isis.core.metamodel.objectmanager.identify.ObjectBookmarker;
import org.apache.isis.core.metamodel.objectmanager.load.ObjectLoader;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMemorizer;
import org.apache.isis.core.metamodel.objectmanager.query.ObjectBulkLoader;
import org.apache.isis.core.metamodel.objectmanager.refresh.ObjectRefresher;
import org.apache.isis.core.metamodel.objectmanager.serialize.ObjectSerializer;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.PackedManagedObject;

import lombok.NonNull;
import lombok.val;

/**
 * Bundles all domain object state related responsibilities:<br>
 * - object creation ... init defaults <br>
 * - object loading ... given a specific object identifier (id) <br>
 * - object identification ... given a domain object (pojo) <br>
 * - object refreshing ... given a domain object (pojo) <br>
 *
 * @since 2.0
 */
public interface ObjectManager {

    MetaModelContext getMetaModelContext();

    ObjectCreator getObjectCreator();
    ObjectLoader getObjectLoader();
    ObjectBulkLoader getObjectBulkLoader();
    ObjectBookmarker getObjectBookmarker();
    ObjectRefresher getObjectRefresher();
    ObjectDetacher getObjectDetacher();
    ObjectSerializer getObjectSerializer();
    ObjectMemorizer getObjectMemorizer();

    // -- SHORTCUTS

    /**
     * Creates and initializes an instance conforming to given request parameters.
     * @param objectCreateRequest
     */
    public default ManagedObject createObject(final ObjectCreator.Request objectCreateRequest) {
        return getObjectCreator().createObject(objectCreateRequest);
    }

    /**
     * Loads an instance identified with given request parameters.
     * @param objectLoadRequest
     */
    public default ManagedObject loadObject(final ObjectLoader.Request objectLoadRequest) {
        return getObjectLoader().loadObject(objectLoadRequest);
    }

    public default Can<ManagedObject> queryObjects(final ObjectBulkLoader.Request objectQuery) {
        return getObjectBulkLoader().loadObject(objectQuery);
    }

    /**
     * Returns an object identifier for the instance.
     * @param managedObject
     */
    public default Bookmark bookmarkObject(final ManagedObject managedObject) {
        return getObjectBookmarker().bookmarkObject(managedObject);
    }

    /**
     * Reloads the state of the (entity) instance from the data store.
     * @param managedObject
     */
    public default void refreshObject(final ManagedObject managedObject) {
        getObjectRefresher().refreshObject(managedObject);
    }

    public default Optional<ObjectSpecification> specForPojo(final @Nullable Object pojo) {
        if(pojo==null) {
            return Optional.empty();
        }
        return specForType(pojo.getClass());
    }

    default Optional<ObjectSpecification> specForType(final @Nullable Class<?> domainType) {
        return getMetaModelContext().getSpecificationLoader().specForType(domainType);
    }

    // -- ADAPTING POJOS

    enum EntityAdaptingMode {
        MEMOIZE_BOOKMARK,
        SKIP_MEMOIZATION;
        public boolean isMemoize() { return this == MEMOIZE_BOOKMARK;}
    }

    /**
     * Not suitable for adapting a non-scalar
     * If {@code pojo} is an entity, automatically memoizes its bookmark.
     */
    public default ManagedObject adapt(final @Nullable Object pojo) {
        return adapt(pojo, ()->specForType(Object.class).orElseThrow(), EntityAdaptingMode.MEMOIZE_BOOKMARK);
    }

    /**
     * Not suitable for adapting a non-scalar.
     * If {@code pojo} is an entity, memoizes its bookmark based on policy {@code bookmarking}.
     */
    public default ManagedObject adapt(final @Nullable Object pojo, final EntityAdaptingMode bookmarking) {
        return adapt(pojo, ()->specForType(Object.class).orElseThrow(), bookmarking);
    }

    /**
     * Suitable for adapting a non-scalar.
     * If {@code pojo} is an entity, automatically memoizes its bookmark.
     */
    public default ManagedObject adapt(
            final @Nullable Object pojo,
            final @NonNull Supplier<ObjectSpecification> fallbackElementType,
            final EntityAdaptingMode bookmarking) {
        if(pojo==null) {
            return ManagedObject.unspecified();
        }
        // could be any pojo, even of a type, that is vetoed for introspection (spec==null)
        val spec = specForType(pojo.getClass()).orElse(null);
        if(spec==null) {
            return ManagedObject.unspecified();
        }
        return spec.isScalar()
                ? autoBookmarked(spec, pojo, bookmarking)
                : PackedManagedObject.pack(
                        spec.getElementSpecification().orElseGet(fallbackElementType),
                        _NullSafe.streamAutodetect(pojo)
                        .map(element->adapt(element, bookmarking))
                        .collect(Can.toCan()));
    }

    /**
     * Not suitable for adapting a non-scalar. Auto-memoizes the ManagedObject's bookmark, if applicable.
     * Optimized, for cases when the adpater's {@link ObjectSpecification} is known in advance.
     * If {@code pojo} is an entity, automatically memoizes its bookmark.
     */
    public default ManagedObject adapt(
            final @Nullable Object pojo,
            final @Nullable ObjectSpecification proposedSpec) {

        if(proposedSpec==null) {
            return adapt(pojo);
        }
        if(pojo==null) {
            return ManagedObject.empty(proposedSpec);
        }

        proposedSpec.assertPojoCompatible(pojo);

        final ManagedObject adapter =
        (proposedSpec.isValue()
                || pojo.getClass().equals(proposedSpec.getCorrespondingClass()))
            // if actual type matches spec's, we assume, that we don't need to reload,
            // so this is a shortcut for performance reasons
            ? autoBookmarked(proposedSpec, pojo, EntityAdaptingMode.MEMOIZE_BOOKMARK)
            // fallback, ignoring proposedSpec
            : adapt(pojo);
        return adapter;
    }

    // -- HELPER

    private static ManagedObject autoBookmarked(
            final ObjectSpecification spec,
            final Object pojo,
            final EntityAdaptingMode bookmarking) {

        if(bookmarking.isMemoize()
                && spec.isEntity()) {
            val entityFacet = spec.getFacet(EntityFacet.class);
            val state = entityFacet.getEntityState(pojo);
            if(state.isAttached()) {
                val id = entityFacet.identifierFor(spec, pojo);
                val bookmark = Bookmark.forLogicalTypeAndIdentifier(spec.getLogicalType(), id);
                return ManagedObject.bookmarked(spec, pojo, bookmark);
            }
        }
        return ManagedObject.notBookmarked(spec, pojo);
    }

}
