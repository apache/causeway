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
package org.apache.causeway.core.runtimeservices.serializing;

import java.io.Serializable;
import java.util.Optional;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.bookmark.idstringifiers.PredefinedSerializables;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsResolver;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.memento._Mementos.SerializingAdapter;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;

import lombok.NonNull;
import lombok.val;

/**
 * Default implementation of {@link SerializingAdapter}, intended as an 'internal' service.
 *
 * @implNote uses {@link Bookmark} or {@link ValueDecomposition}
 * for identifiable objects or value types,
 * while some predefined serializable types that implement {@link Serializable}
 * are written/read directly
 *
 * @see PredefinedSerializables
 */
@Service
@Named(CausewayModuleCoreRuntimeServices.NAMESPACE + ".SerializingAdapterDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public class SerializingAdapterDefault implements SerializingAdapter {

    @Inject private BookmarkService bookmarkService;

    @Lazy
    @Inject private ValueSemanticsResolver valueSemanticsResolver;

    @Override
    public Serializable write(final @NonNull Object value) {
        if(PredefinedSerializables.isPredefinedSerializable(value.getClass())) {
            // the value can be stored/written directly without conversion to a bookmark
            return (Serializable) value;
        }

        // potentially is a value with value semantics
        return toValueDecomposition(value)
                .map(Serializable.class::cast)
                // if not fallback to bookmarkService
                .or(()->bookmarkService.bookmarkFor(value))
                .orElseThrow(()->
                    _Exceptions.unrecoverable("cannot create a memento for object of type %s", value.getClass()));
    }

    @Override
    public <T> T read(final @NonNull Class<T> valueClass, final @NonNull Serializable value) {

        // see if required/desired value-class is Bookmark, then just cast
        if(Bookmark.class.equals(valueClass)) {
            return _Casts.uncheckedCast(value);
        }

        // otherwise, perhaps the value itself is a Bookmark, in which case we treat it as a
        // reference to an Object (probably an entity) to be looked up
        if(Bookmark.class.isAssignableFrom(value.getClass())) {
            final Bookmark valueBookmark = (Bookmark) value;
            return _Casts.uncheckedCast(bookmarkService.lookup(valueBookmark)
                        .orElse(null));
        }

        // otherwise, perhaps the value itself is a ValueDecomposition, in which case we
        // re-compose the original value
        if(value instanceof ValueDecomposition) {
            val decomposition = (ValueDecomposition) value;
            return fromValueDecomposition(valueClass, (ValueDecomposition) value)
                    .orElseThrow(()->
                      _Exceptions.unrecoverable("cannot restore object of type %s from decomposition '%s'",
                              valueClass, decomposition.toJson()));
        }

        // otherwise, the value was directly stored/written, so just recover as is
        return _Casts.uncheckedCast(value);
    }

    // -- HELPER

    private <T> Optional<ValueDecomposition> toValueDecomposition(
            final @NonNull T value) {

        final Class<T> valueClass = _Casts.uncheckedCast(value.getClass());

        return valueSemanticsResolver.streamValueSemantics(valueClass)
                .findFirst()
                .map(vs->vs.decompose(value));
    }

    private <T> Optional<T> fromValueDecomposition(
            final @NonNull Class<T> valueClass,
            final @NonNull ValueDecomposition decomposition) {

        return valueSemanticsResolver.streamValueSemantics(valueClass)
                .findFirst()
                .map(vs->vs.compose(decomposition));
    }

}
