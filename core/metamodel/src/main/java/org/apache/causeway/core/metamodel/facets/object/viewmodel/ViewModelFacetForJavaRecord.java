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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;

import org.springframework.util.Assert;

import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
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

        var recordComponentPojos = streamRecordComponents(viewmodelSpec.getCorrespondingClass())
            .map(recComp->memento.get(recComp.getName(), recComp.getType()))
            .toArray();

        return canonicalConstructor.newInstance(recordComponentPojos);
    }

    @Override
    public byte[] encodeState(final ManagedObject viewModel) {

        final Memento memento = mementoContext.newMemento();

        Arrays.stream(snapshotRecordComponents(viewModel.getPojo()))
            .forEach(arg->memento.put(arg.name(), arg.pojo()));

        return memento.stateAsBytes();
    }

    // -- HELPER

    @SneakyThrows
    private static Stream<RecordComponent> streamRecordComponents(final @NonNull Class<?> recordClass) {
        Assert.isTrue(recordClass.isRecord(), ()->"Illegal Argument: not a Java record");
        return Arrays.stream(recordClass.getRecordComponents());
    }

    @SneakyThrows
    private static <T> Constructor<T> canonicalConstructor(final @NonNull Class<T> recordClass) {
        var constructorParamTypes = streamRecordComponents(recordClass)
                .map(RecordComponent::getType)
                .toArray(Class[]::new);
        return recordClass.getDeclaredConstructor(constructorParamTypes);
    }

    private record NamedArg(String name, Object pojo) {
    }

    @SneakyThrows
    private static NamedArg[] snapshotRecordComponents(final Object recordInstance) {
        final @NonNull Class<?> recordClass = Objects.requireNonNull(recordInstance).getClass();
        Assert.isTrue(recordClass.isRecord(), ()->"Illegal Argument: not a Java record");

        RecordComponent[] components = recordClass.getRecordComponents();
        NamedArg[] result = new NamedArg[components.length];
        for (int i = 0; i < components.length; i++) {
            result[i] = new NamedArg(
                    components[i].getName(),
                    components[i].getAccessor().invoke(recordInstance));
        }
        return result;
    }

}
