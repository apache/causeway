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

import org.jspecify.annotations.NonNull;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.util.hmac.Memento;
import org.apache.causeway.core.metamodel.util.hmac.MementoHmacContext;

import lombok.SneakyThrows;

/**
 * @since 3.0.0
 */
public final class ViewModelFacetForJavaRecord
extends SecureViewModelFacet {

    static Optional<ViewModelFacetForJavaRecord> create(
            final Class<?> cls,
            final MementoHmacContext mementoContext,
            final FacetHolder holder) {
        return mementoContext!=null
                && cls.isRecord()
            ? Optional.of(new ViewModelFacetForJavaRecord(cls, mementoContext, holder))
            : Optional.empty();
    }

    private final MementoHmacContext mementoContext;
    private final Constructor<?> canonicalConstructor;

    protected ViewModelFacetForJavaRecord(
            final Class<?> recordClass,
            final MementoHmacContext mementoContext,
            final FacetHolder holder) {
        // is overruled by ViewModel interface semantics
        super(mementoContext.hmacUrlCodec(), holder, Precedence.DEFAULT);
        this.mementoContext = mementoContext;
        this.canonicalConstructor = canonicalConstructor(recordClass);
    }

    @Override @SneakyThrows
    protected Object createViewmodelPojo(
            final @NonNull ObjectSpecification viewmodelSpec,
            final @NonNull byte[] trustedBookmarkIdAsBytes) {

        // throws on de-marshalling failure
        var memento = mementoContext.parseTrustedMemento(trustedBookmarkIdAsBytes);

        var recordComponentPojos = streamRecordComponents(viewmodelSpec)
        .map(association->{
            var associationPojo = association.isProperty()
                ? memento.get(association.getId(), association.getElementType().getCorrespondingClass())
                //TODO collection values not yet supported by memento (as workaround use Serializable record)
                : null;
            return associationPojo;
        }).toArray();

        return canonicalConstructor.newInstance(recordComponentPojos);
    }

    @Override
    public byte[] encodeState(final ManagedObject viewModel) {

        final Memento memento = mementoContext.newMemento();

        var viewmodelSpec = viewModel.objSpec();

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

        return memento.stateAsBytes();
    }

    // -- HELPER

    private Can<ObjectAssociation> recordComponentsAsAssociations;
    private Stream<ObjectAssociation> streamRecordComponents(
            final @NonNull ObjectSpecification viewmodelSpec) {
        if(recordComponentsAsAssociations==null) {
            this.recordComponentsAsAssociations = recordComponentsAsAssociations(viewmodelSpec);
        }
        return recordComponentsAsAssociations.stream();
    }

    private static Can<ObjectAssociation> recordComponentsAsAssociations(
            final @NonNull ObjectSpecification viewmodelSpec) {
        return Arrays.stream(viewmodelSpec.getCorrespondingClass().getRecordComponents())
                .map(RecordComponent::getName)
                .map(memberId->viewmodelSpec.getAssociationElseFail(memberId, MixedIn.EXCLUDED))
                .collect(Can.toCan());
    }

    @SneakyThrows
    private static <T> Constructor<T> canonicalConstructor(final @NonNull Class<T> recordClass) {
        var constructorParamTypes = Arrays.stream(recordClass.getRecordComponents())
                .map(RecordComponent::getType)
                .toArray(Class[]::new);
        return recordClass.getDeclaredConstructor(constructorParamTypes);
    }

}
