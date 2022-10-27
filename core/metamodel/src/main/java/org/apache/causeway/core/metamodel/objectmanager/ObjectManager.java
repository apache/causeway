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
package org.apache.causeway.core.metamodel.objectmanager;

import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ProtoObject;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

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
public interface ObjectManager extends HasMetaModelContext {

    ObjectCreator getObjectCreator();
    ObjectLoader getObjectLoader();
    ObjectBulkLoader getObjectBulkLoader();
    ObjectBookmarker getObjectBookmarker();

    // -- OBJECT MEMENTOS

    default Optional<ObjectMemento> mementify(final @Nullable ManagedObject object) {
        return Optional.ofNullable(object)
        .flatMap(ManagedObject::getMemento);
    }

    default ObjectMemento mementifyElseFail(final @NonNull ManagedObject object) {
        return object.getMemento()
                .orElseThrow(()->
                    _Exceptions.unrecoverable("failed to create memento for  %s", object.getSpecification()));
    }

    //TODO why not use loadObject(bookmark) instead
    ManagedObject demementify(final ObjectMemento memento);
    //TODO why not use loadObject(bookmark) instead
    default ManagedObject demementify(final ObjectSpecification spec, final ObjectMemento memento) {
        return demementify(memento);
    }

    // -- SHORTCUTS

    /**
     * Creates and initializes an instance conforming to given request parameters.
     * <p>
     * Resolves injection-points for the result. (Handles service injection.)
     */
    public default ManagedObject createObject(final ObjectSpecification objectCreateRequest) {
        return getObjectCreator().createObject(objectCreateRequest);
    }

    /**
     * Loads an instance identified with given request parameters.
     * <p>
     * Resolves injection-points for the result. (Handles service injection.)
     */
    public default ManagedObject loadObject(final ProtoObject objectLoadRequest) {
        return getObjectLoader().loadObject(objectLoadRequest);
    }

    /**
     * Recovers an object (graph) from given {@code bookmark}.
     * <p>
     * Resolves injection-points for the result. (Handles service injection.)
     * <p>
     * Supports alias lookup.
     */
    default Optional<ManagedObject> loadObject(final @Nullable Bookmark bookmark) {
        if(bookmark==null) {
            return Optional.empty();
        }
        val specLoader = getMetaModelContext().getSpecificationLoader();
        return ProtoObject.resolve(specLoader, bookmark)
                .map(this::loadObject);
    }

    /**
     * @see #loadObject(Bookmark)
     */
    default ManagedObject loadObjectElseFail(final @NonNull Bookmark bookmark) {
        val adapter = loadObject(bookmark)
                .orElseThrow(()->
                    _Exceptions.unrecoverable("failed to restore object from bookmark %s", bookmark));
        if(adapter.getSpecialization().isEntity()) {
            _Assert.assertEquals(bookmark, adapter.getBookmark().orElse(null),
                    ()->"object loaded from bookmark must itself return an equal bookmark");
        }
        return adapter;
    }

    /**
     * Resolves injection-points for the result. (Handles service injection.)
     */
    public default Can<ManagedObject> queryObjects(final ObjectBulkLoader.Request objectQuery) {
        return getObjectBulkLoader().loadObject(objectQuery);
    }

    /**
     * Returns an object identifier for the instance.
     * @param managedObject
     */
    public default Optional<Bookmark> bookmarkObject(final ManagedObject managedObject) {
        return getObjectBookmarker().bookmarkObject(managedObject);
    }

    public default Bookmark bookmarkObjectElseFail(final ManagedObject managedObject) {
        return bookmarkObject(managedObject)
                .orElseThrow(()->
                    _Exceptions.unrecoverable("failed to bookmark %s", managedObject.getSpecification()));
    }

    public default Optional<ObjectSpecification> specForPojo(final @Nullable Object pojo) {
        if(pojo==null) {
            return Optional.empty();
        }
        return specForType(pojo.getClass());
    }

    @Override
    default Optional<ObjectSpecification> specForType(final @Nullable Class<?> domainType) {
        return getMetaModelContext().getSpecificationLoader().specForType(domainType);
    }

    // -- ADAPTING POJOS

    /**
     * Not suitable for adapting a non-scalar.
     * If {@code pojo} is an entity, automatically memoizes its bookmark.
     * <p>
     * Resolves injection-points for the result. (Handles service injection.)
     */
    public default ManagedObject adapt(final @Nullable Object pojo) {
        return adapt(pojo, ()->specForType(Object.class).orElseThrow());
    }

    /**
     * Suitable for adapting a non-scalar.
     * If {@code pojo} is an entity, automatically memoizes its bookmark.
     * <p>
     * Resolves injection-points for the result. (Handles service injection.)
     */
    public default ManagedObject adapt(
            final @Nullable Object pojo,
            final @NonNull Supplier<ObjectSpecification> fallbackElementType) {
        if(pojo==null) {
            return ManagedObject.unspecified();
        }
        if(pojo instanceof ManagedObject) {
            // yet ignoring any bookmarking policy, assuming this is not required here
            return (ManagedObject) pojo;
        }
        // could be any pojo, even of a type, that is vetoed for introspection (spec==null)
        val spec = specForType(pojo.getClass()).orElse(null);
        if(spec==null) {
            return ManagedObject.unspecified();
        }
        return spec.isSingular()
                ? ManagedObject.adaptSingular(spec, pojo)
                : ManagedObject.packed(
                        spec.getElementSpecification().orElseGet(fallbackElementType),
                        _NullSafe.streamAutodetect(pojo)
                        .map(element->adapt(element))
                        .collect(Can.toCan()));
    }


}
