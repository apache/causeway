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

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.urlencoding.UrlEncodingService;
import org.apache.causeway.commons.internal.memento._Mementos;
import org.apache.causeway.commons.internal.memento._Mementos.SerializingAdapter;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

/**
 * @since 3.0.0
 */
public class ViewModelFacetForJavaRecord
extends ViewModelFacetAbstract {

    public static Optional<ViewModelFacetForJavaRecord> create(
            final Class<?> cls,
            final FacetHolder holder) {
        return cls.isRecord()
                ? Optional.of(new ViewModelFacetForJavaRecord(cls, holder))
                : Optional.empty();
    }

    private UrlEncodingService codec;
    private SerializingAdapter serializer;
    private Constructor<?> canonicalConstructor;

    protected ViewModelFacetForJavaRecord(
            final Class<?> recordClass,
            final FacetHolder holder) {
        // is overruled by ViewModel interface semantics
        super(holder, Precedence.DEFAULT);
        this.canonicalConstructor = canonicalConstructor(recordClass);
    }

    @Override @SneakyThrows
    protected ManagedObject createViewmodel(
            @NonNull final ObjectSpecification viewmodelSpec,
            @NonNull final Bookmark bookmark) {

        val memento = parseMemento(bookmark);

        var recordComponentPojos = streamRecordComponents(viewmodelSpec)
        .map(association->{
            val associationId = association.getId();
            val elementType = association.getElementType();
            val elementClass = elementType.getCorrespondingClass();
            val associationPojo = association.isProperty()
                    ? memento.get(associationId, elementClass)
                    //TODO collection values not yet supported by memento (as workaround use Serializable record)
                    : null;
            return associationPojo;
        }).toArray();

        return getObjectManager().adapt(canonicalConstructor.newInstance(recordComponentPojos));
    }

    @Override
    public String serialize(final ManagedObject viewModel) {

        final _Mementos.Memento memento = newMemento();

        val viewmodelSpec = viewModel.getSpecification();

        streamRecordComponents(viewmodelSpec)
        .forEach(association->{

            final ManagedObject associationValue =
                    association.get(viewModel, InteractionInitiatedBy.PASS_THROUGH);

            if(association != null
                    //TODO collection values not yet supported by memento (as workaround use Serializable record)
                    && association.isProperty()
                    && associationValue.getPojo()!=null) {
                memento.put(association.getId(), associationValue.getPojo());
            }
        });

        return memento.asString();
    }

    // -- HELPER

    private Stream<ObjectAssociation> streamRecordComponents(
            final @NonNull ObjectSpecification viewmodelSpec) {
        return Stream.of(viewmodelSpec.getCorrespondingClass().getRecordComponents())
            .map(RecordComponent::getName)
            .map(memberId->viewmodelSpec.getAssociationElseFail(memberId, MixedIn.EXCLUDED));
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

    @SneakyThrows
    private static <T> Constructor<T> canonicalConstructor(final @NonNull Class<T> recordClass) {
        val constructorParamTypes = Arrays.stream(recordClass.getRecordComponents())
                .map(RecordComponent::getType)
                .toArray(Class[]::new);
        return recordClass.getDeclaredConstructor(constructorParamTypes);
    }

}
