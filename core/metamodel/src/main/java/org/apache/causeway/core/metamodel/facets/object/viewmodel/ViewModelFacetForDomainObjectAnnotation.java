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
package org.apache.causeway.core.metamodel.facets.object.viewmodel;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.causeway.applib.annotation.DomainObject;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.metamodel.BeanSort;
import org.apache.causeway.applib.services.urlencoding.UrlEncodingService;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.memento._Mementos;
import org.apache.causeway.commons.internal.memento._Mementos.SerializingAdapter;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.properties.update.modify.PropertySetterFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.NonNull;
import lombok.val;

public class ViewModelFacetForDomainObjectAnnotation
extends ViewModelFacetAbstract {

    public static Optional<ViewModelFacetForDomainObjectAnnotation> create(
            final Optional<DomainObject> domainObjectIfAny,
            final FacetHolder holder) {

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

                        //[CAUSEWAY-3068] consider what the BeanTypeClassifier has come up with
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
                        return new ViewModelFacetForDomainObjectAnnotation(holder);
                    }
                    // shouldn't happen, the above switch should match all cases
                    throw new IllegalArgumentException("nature of '" + nature + "' not recognized");
                })
                .filter(Objects::nonNull);
    }

    private UrlEncodingService codec;
    private SerializingAdapter serializer;

    protected ViewModelFacetForDomainObjectAnnotation(
            final FacetHolder holder) {
        // is overruled by any other ViewModelFacet type
        super(holder, Precedence.LOW);
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

        streamPersistableProperties(viewmodelSpec)
        .forEach(property->{

            // we also explicitly set 'nulled' properties

            val propertyId = property.getId();
            val propertySpec = property.getElementType();
            val propertyType = propertySpec.getCorrespondingClass();
            val propertyPojo = memento.get(propertyId, propertyType);
            final ManagedObject propertyValue = propertyPojo!=null
                    ? objectManager.adapt(propertyPojo)
                    : ManagedObject.empty(propertySpec);

            property.set(viewmodel, propertyValue, InteractionInitiatedBy.PASS_THROUGH);
        });

        return viewmodel;
    }

    @Override
    public String serialize(final ManagedObject viewModel) {

        final _Mementos.Memento memento = newMemento();

        val viewmodelSpec = viewModel.getSpecification();

        streamPersistableProperties(viewmodelSpec)
        .forEach(property->{

            final ManagedObject propertyValue =
                    property.get(viewModel, InteractionInitiatedBy.PASS_THROUGH);

            if(propertyValue != null
                    && propertyValue.getPojo()!=null) {
                memento.put(property.getId(), propertyValue.getPojo());
            }
        });

        return memento.asString();
    }

    // -- HELPER

    private Stream<OneToOneAssociation> streamPersistableProperties(
            final @NonNull ObjectSpecification viewmodelSpec) {
        return viewmodelSpec.streamProperties(MixedIn.EXCLUDED)
                // ignore read-only
                .filter(property->property.containsNonFallbackFacet(PropertySetterFacet.class))
                // ignore those explicitly annotated as @Property(snapshot = Snapshot.EXCLUDED)
                .filter(property->property.isIncludedWithSnapshots());
    }

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
