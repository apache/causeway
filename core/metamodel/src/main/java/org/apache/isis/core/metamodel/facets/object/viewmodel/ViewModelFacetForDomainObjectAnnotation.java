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
package org.apache.isis.core.metamodel.facets.object.viewmodel;

import java.util.Objects;
import java.util.Optional;

import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.metamodel.BeanSort;
import org.apache.isis.applib.services.urlencoding.UrlEncodingService;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.memento._Mementos;
import org.apache.isis.commons.internal.memento._Mementos.SerializingAdapter;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.HasPostConstructMethodCache;
import org.apache.isis.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.object.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;

import lombok.NonNull;
import lombok.val;

public class ViewModelFacetForDomainObjectAnnotation
extends ViewModelFacetAbstract {

    public static Optional<ViewModelFacetForDomainObjectAnnotation> create(
            final Optional<DomainObject> domainObjectIfAny,
            final FacetHolder holder,
            final HasPostConstructMethodCache postConstructMethodCache) {

        return domainObjectIfAny
                .map(DomainObject::nature)
                .map(nature -> {
                    switch (nature) {
                    case BEAN:
                    case ENTITY:
                    case MIXIN:
                        // not a ViewModel, so no ViewModelFacet
                        return null;
                    case NOT_SPECIFIED:

                        //[ISIS-3068] consider what the BeanTypeClassifier has come up with
                        final boolean isClassifiedAsViewModel =
                            _Casts.castTo(ObjectSpecification.class, holder)
                            .map(ObjectSpecification::getBeanSort)
                            .map(BeanSort::isViewModel)
                            .orElse(false);

                        if(!isClassifiedAsViewModel) {
                            // not a ViewModel, so no ViewModelFacet
                            return null;
                        }
                        // else fall through
                    case VIEW_MODEL:
                        return new ViewModelFacetForDomainObjectAnnotation(
                                holder, postConstructMethodCache);
                    }
                    // shouldn't happen, the above switch should match all cases
                    throw new IllegalArgumentException("nature of '" + nature + "' not recognized");
                })
                .filter(Objects::nonNull);
    }

    private UrlEncodingService codec;
    private SerializingAdapter serializer;

    protected ViewModelFacetForDomainObjectAnnotation(
            final FacetHolder holder,
            final HasPostConstructMethodCache postConstructMethodCache) {

        super(holder, postConstructMethodCache, Precedence.LOW); // is overruled by any other ViewModelFacet type
    }

    @Override
    protected ManagedObject createViewmodel(
            @NonNull final ObjectSpecification viewmodelSpec,
            @NonNull final Bookmark bookmark) {

        val viewmodel = viewmodelSpec.createObject();

        val memento = parseMemento(bookmark);
        val mementoKeys = memento.keySet();

        if(mementoKeys.isEmpty()) {
            return viewmodel;
        }

        val objectManager = super.getObjectManager();

        viewmodelSpec.streamProperties(MixedIn.EXCLUDED)
        .filter(property->mementoKeys.contains(property.getId()))
        .forEach(property->{

            val propertyId = property.getId();
            val propertyType = property.getElementType().getCorrespondingClass();
            val propertyValue = memento.get(propertyId, propertyType);

            if(propertyValue != null) {
                property.set(viewmodel, objectManager.adapt(propertyValue), InteractionInitiatedBy.FRAMEWORK);
            }
        });

        return viewmodel;
    }

    @Override
    public String serialize(final ManagedObject viewModel) {

        final _Mementos.Memento memento = newMemento();

        /*
         * ManagedObject that holds the ObjectSpecification used for
         * interrogating the domain object's metadata.
         *
         * Does _not_ perform dependency injection on the domain object. Also bypasses
         * caching (if any), that is each call to this method creates a new instance.
         */
        val spec = viewModel.getSpecification();

        spec.streamProperties(MixedIn.EXCLUDED)
        // ignore read-only
        .filter(property->property.containsNonFallbackFacet(PropertySetterFacet.class))
        // ignore those explicitly annotated as @NotPersisted
        .filter(property->!property.isNotPersisted())
        .forEach(property->{
            final ManagedObject propertyValue =
                    property.get(viewModel, InteractionInitiatedBy.FRAMEWORK);
            if(propertyValue != null
                    && propertyValue.getPojo()!=null) {
                memento.put(property.getId(), propertyValue.getPojo());
            }
        });

        return memento.asString();
    }

    // -- HELPER

    private void initDependencies() {
        val serviceRegistry = getServiceRegistry();
        this.codec = serviceRegistry.lookupServiceElseFail(UrlEncodingService.class);
        this.serializer = serviceRegistry.lookupServiceElseFail(SerializingAdapter.class);
    }

    private void ensureDependenciesInited() {
        if(codec==null) {
            initDependencies();
        }
    }

    private _Mementos.Memento newMemento() {
        ensureDependenciesInited();
        return _Mementos.create(codec, serializer);
    }

    private _Mementos.Memento parseMemento(final Bookmark bookmark) {
        ensureDependenciesInited();
        return _Mementos.parse(codec, serializer, bookmark.getIdentifier());
    }

}
