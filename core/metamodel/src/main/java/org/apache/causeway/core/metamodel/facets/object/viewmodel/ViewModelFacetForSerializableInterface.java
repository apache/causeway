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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.internal.base._Bytes;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import org.jspecify.annotations.NonNull;
import lombok.SneakyThrows;

/**
 * Corresponds to {@link Serializable} interface.
 */
public class ViewModelFacetForSerializableInterface
extends ViewModelFacetAbstract {

    public static Optional<ViewModelFacet> create(
            final Class<?> cls,
            final FacetHolder holder) {

        return Serializable.class.isAssignableFrom(cls)
                ? Optional.of(new ViewModelFacetForSerializableInterface(holder))
                : Optional.empty();
    }

    protected ViewModelFacetForSerializableInterface(
            final FacetHolder holder) {
        super(holder, Precedence.HIGH);
    }

    @SneakyThrows
    @Override
    protected ManagedObject createViewmodel(
            final @NonNull ObjectSpecification viewmodelSpec,
            final @NonNull Bookmark bookmark) {
        return ManagedObject.bookmarked(
                        viewmodelSpec,
                        deserialize(viewmodelSpec, bookmark.identifier()),
                        bookmark);
    }

    @SneakyThrows
    @Override
    public String serialize(final ManagedObject viewModel) {
        var baos = new ByteArrayOutputStream();
        try(var oos = new ObjectOutputStream(baos)) {
            oos.writeObject(viewModel.getPojo());
            var mementoStr = _Strings.ofBytes(
                    _Bytes.asUrlBase64.apply(baos.toByteArray()),
                    StandardCharsets.UTF_8);
            return mementoStr;
        }
    }

    // -- HELPER

    @SneakyThrows
    private Object deserialize(
            final @NonNull ObjectSpecification viewmodelSpec,
            final @NonNull String memento) {
        var bytes = _Bytes.ofUrlBase64.apply(_Strings.toBytes(memento, StandardCharsets.UTF_8));
        try(var ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            var viewModelPojo = ois.readObject();
            return viewModelPojo;
        }
    }

}
