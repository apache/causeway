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

import jakarta.inject.Named;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.exceptions.unrecoverable.BookmarkNotFoundException;
import org.apache.causeway.applib.query.Query;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.annotations.BeanInternal;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.CausewayModuleCoreMetamodel;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.PackedManagedObject;
import org.apache.causeway.core.metamodel.object.ProtoObject;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMementoCollection;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMementoForEmpty;
import org.apache.causeway.core.metamodel.objectmanager.memento.ObjectMementoForScalar;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.NonNull;

/**
 * Bundles all domain object state related responsibilities:<br>
 * - object creation ... init defaults <br>
 * - object loading ... given a specific object identifier (id) <br>
 * - object identification ... given a domain object (pojo) <br>
 * - object refreshing ... given a domain object (pojo) <br>
 *
 * @since 2.0
 */
@SuppressWarnings("exports")
@BeanInternal
@Named(CausewayModuleCoreMetamodel.NAMESPACE + ".ObjectManager")
public record ObjectManager(
        MetaModelContext mmc,
        ObjectCreator objectCreator,
        ObjectLoader objectLoader,
        ObjectBulkLoader objectBulkLoader) implements HasMetaModelContext {

    // -- REQUEST (VALUE) TYPE

    public record BulkLoadRequest(
        ObjectSpecification objectSpecification,
        Query<?> query) {
    }

    public ObjectManager(final MetaModelContext mmc) {
        this(mmc,
                new ObjectCreator(mmc),
                new ObjectLoader(),
                new ObjectBulkLoader());
    }

    /**
     * Creates and initializes an instance conforming to given request parameters.
     * <p>
     * Resolves injection-points for the result. (Handles service injection.)
     */
    public ManagedObject createObject(final ObjectSpecification objectCreateRequest) {
        return objectCreator().createObject(objectCreateRequest);
    }

    /**
     * Loads an instance identified with given request parameters.
     * <p>
     * Resolves injection-points for the result. (Handles service injection.)
     */
    public ManagedObject loadObject(final ProtoObject objectLoadRequest) {
        return objectLoader().loadObject(objectLoadRequest);
    }

    /**
     * Recovers an object (graph) from given {@code bookmark}.
     * <p>
     * Resolves injection-points for the result. (Handles service injection.)
     * <p>
     * Supports alias lookup.
     */
    public Optional<ManagedObject> loadObject(final @Nullable Bookmark bookmark) {
        if(bookmark==null) {
            return Optional.empty();
        }
        var specLoader = getMetaModelContext().getSpecificationLoader();
        return ProtoObject.resolve(specLoader, bookmark)
                .map(this::loadObject);
    }

    /**
     * Introduced for serializing action parameter values to bookmarks and vice versa.
     * <p>
     * Does NOT handle {@link PackedManagedObject}. (Needs to be handled by the caller.)
     * @see #debookmark(Bookmark)
     */
    public Bookmark bookmark(final @NonNull ManagedObject managedObj) {
        return ManagedObjects.bookmark(managedObj)
                .orElseGet(()->Bookmark.empty(managedObj.getLogicalType()));
    }
    /**
     * Introduced for de-serializing action parameter values from bookmarks and vice versa.
     * <p>
     * Does NOT handle {@link PackedManagedObject}. (Needs to be handled by the caller.)
     * @see #bookmark(ManagedObject)
     */
    public ManagedObject debookmark(final @NonNull Bookmark bookmark) {
        return bookmark.isEmpty()
            ? ManagedObject.empty(getSpecificationLoader().specForBookmarkElseFail(bookmark))
            : loadObjectElseFail(bookmark);
    }

    /**
     * @see #loadObject(Bookmark)
     */
    public ManagedObject loadObjectElseFail(final @NonNull Bookmark bookmark) {
        var adapter = loadObject(bookmark)
                .orElseThrow(() -> new BookmarkNotFoundException(String.format("Bookmark %s was not found.", bookmark)));
        if(adapter.getSpecialization().isEntity()) {
            _Assert.assertEquals(bookmark, adapter.getBookmark().orElse(null),
                    ()->"object loaded from bookmark must itself return an equal bookmark");
        }
        return adapter;
    }

    /**
     * Resolves injection-points for the result. (Handles service injection.)
     */
    public Can<ManagedObject> queryObjects(final BulkLoadRequest objectQuery) {
        return objectBulkLoader().loadObject(objectQuery);
    }

    public Optional<ObjectSpecification> specForPojo(final @Nullable Object pojo) {
        if(pojo==null) {
            return Optional.empty();
        }
        return specForType(pojo.getClass());
    }

    @Override
    public Optional<ObjectSpecification> specForType(final @Nullable Class<?> domainType) {
        return getMetaModelContext().getSpecificationLoader().specForType(domainType);
    }

    // -- ADAPTING POJOS

    /**
     * Not suitable for adapting a plural.
     * If {@code pojo} is an entity, automatically memoizes its bookmark.
     * <p>
     * Resolves injection-points for the result. (Handles service injection.)
     * <p>
     * see also {@link #adapt(Object, Supplier)},
     *      where the 2nd arg supplies the {@link ObjectSpecification} if known (eg for null args).
     *
     * @see ManagedObject#adaptSingular(ObjectSpecification, Object)
     * @see ManagedObject#adaptParameter(ObjectActionParameter, Object)
     * @see ManagedObject#adaptProperty(OneToOneAssociation, Object)
     */
    public ManagedObject adapt(final @Nullable Object pojo) {
        return adapt(pojo, ()->specForType(Object.class).orElseThrow());
    }

    /**
     * Suitable for adapting a plural.
     * If {@code pojo} is an entity, automatically memoizes its bookmark.
     * <p>
     * Resolves injection-points for the result. (Handles service injection.)
     */
    public ManagedObject adapt(
            final @Nullable Object pojo,
            final @NonNull Supplier<ObjectSpecification> fallbackElementType) {
        if(pojo==null) {
            ObjectSpecification objectSpecification = fallbackElementType.get();
            if (objectSpecification.isSingular()) {
                return ManagedObject.empty(objectSpecification);
            }
            // best we can do?
            return ManagedObject.unspecified();
        }
        if(pojo instanceof ManagedObject) {
            // yet ignoring any bookmarking policy, assuming this is not required here
            return (ManagedObject) pojo;
        }
        // could be any pojo, even of a type, that is vetoed for introspection (spec==null)
        var spec = specForType(pojo.getClass()).orElse(null);
        if(spec==null) {
            // best we can do?
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

    // -- OBJECT MEMENTOS

    public Optional<ObjectMemento> mementify(final @Nullable ManagedObject object) {
        return Optional.ofNullable(object)
        .flatMap(ManagedObject::getMemento);
    }

    public ObjectMemento mementifyElseFail(final @NonNull ManagedObject object) {
        return object.getMemento()
                .orElseThrow(()->
                    _Exceptions.unrecoverable("failed to create memento for  %s", object.getSpecification()));
    }


    //TODO why not use loadObject(bookmark) instead
    public ManagedObject demementify(final ObjectSpecification spec, final ObjectMemento memento) {
        return demementify(memento);
    }

    //TODO why not use loadObject(bookmark) instead
    public ManagedObject demementify(final @Nullable ObjectMemento memento) {

        if(memento==null) {
            return null;
        }

        if(memento instanceof ObjectMementoForEmpty) {
            var objectMementoForEmpty = (ObjectMementoForEmpty) memento;
            var logicalType = objectMementoForEmpty.getLogicalType();
            /* note: we recover from (corresponding) class not logical-type-name,
             * as the latter can be ambiguous, when shared in a type hierarchy*/
            var spec = getSpecificationLoader().specForLogicalType(logicalType);
            return spec.isPresent()
                    ? ManagedObject.empty(spec.get())
                    : ManagedObject.unspecified();
        }

        if(memento instanceof ObjectMementoCollection) {
            var objectMementoCollection = (ObjectMementoCollection) memento;

            var logicalType = objectMementoCollection.getLogicalType();
            /* note: we recover from (corresponding) class not logical-type-name,
             * as the latter can be ambiguous, when shared in a type hierarchy*/
            var elementSpec = getSpecificationLoader().specForLogicalTypeElseFail(logicalType);

            var objects = objectMementoCollection.unwrapList().stream()
                    .map(this::demementify)
                    .collect(Can.toCan());

            return ManagedObject.packed(elementSpec, objects);
        }

        if(memento instanceof ObjectMementoForScalar) {
            var objectMementoAdapter = (ObjectMementoForScalar) memento;
            return objectMementoAdapter.reconstructObject(getMetaModelContext());
        }

        throw _Exceptions.unrecoverable("unsupported ObjectMemento type %s", memento.getClass());
    }

    @Override
    public MetaModelContext getMetaModelContext() {
        return mmc;
    }

}
