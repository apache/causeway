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

import java.io.Serializable;
import java.util.Optional;

import org.jspecify.annotations.NonNull;

import org.apache.causeway.applib.services.bookmark.HmacAuthority;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.resources._Serializables;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.util.hmac.HmacUrlCodec;
import lombok.SneakyThrows;

/**
 * Corresponds to {@link Serializable} interface.
 *
 * <p> requires a {@link HmacAuthority}, otherwise disabled.
 */
public final class ViewModelFacetForSerializableInterface
extends SecureViewModelFacet {

    static Optional<ViewModelFacet> create(
            final Class<?> cls,
            final HmacUrlCodec hmacUrlCodec,
            final FacetHolder holder) {

        return hmacUrlCodec!=null
            && Serializable.class.isAssignableFrom(cls)
                ? Optional.of(new ViewModelFacetForSerializableInterface(hmacUrlCodec, holder))
                : Optional.empty();
    }

    protected ViewModelFacetForSerializableInterface(
            final HmacUrlCodec hmacUrlCodec,
            final FacetHolder holder) {
        super(hmacUrlCodec, holder, Precedence.HIGH);
    }

    @SneakyThrows
    @Override
    protected Object createViewmodelPojo(
            final @NonNull ObjectSpecification viewmodelSpec,
            final @NonNull byte[] trustedBookmarkIdAsBytes) {

        Class<? extends Serializable> expectedType = _Casts.uncheckedCast(viewmodelSpec.getCorrespondingClass());
        return _Serializables.read(expectedType, trustedBookmarkIdAsBytes);
    }

    @SneakyThrows
    @Override
    public byte[] encodeState(final ManagedObject viewModel) {
        return _Serializables.write((Serializable) viewModel.getPojo());
    }

}
