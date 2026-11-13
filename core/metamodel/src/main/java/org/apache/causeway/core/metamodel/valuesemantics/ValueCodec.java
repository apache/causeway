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
package org.apache.causeway.core.metamodel.valuesemantics;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Optional;

import jakarta.inject.Provider;

import org.jspecify.annotations.NonNull;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.bookmark.idstringifiers.PredefinedSerializables;
import org.apache.causeway.applib.value.semantics.ValueDecomposition;
import org.apache.causeway.applib.value.semantics.ValueSemanticsResolver;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

/**
 * Coder/Decoder from {@link Object} to {@link Serializable}
 * As used to encode values to bookmarks.
 *
 * @implNote uses {@link Bookmark} or {@link ValueDecomposition}
 * for identifiable objects or value types,
 * while some predefined serializable types that implement {@link Serializable}
 * are written/read directly
 *
 * @see PredefinedSerializables
 *
 * @since 3.5 (refactored from SerializingAdapter)
 */
public record ValueCodec(
    BookmarkService bookmarkService,
    Provider<ValueSemanticsResolver> valueSemanticsResolverProvider) {

    /** JUnit testing default */
    public static ValueCodec forTesting() {
        return new ValueCodec(null, () -> null);
    }

    /**
     * Converts the value into a {@link Serializable} that is write-able to an {@link ObjectOutput}.
     *
     * <p>Note: write and read are complementary operations
     */
    public Serializable encode(final @NonNull Object value) {
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

    /**
     * Converts the {@link Serializable} {@code value} as read from an {@link ObjectInput} back into its
     * original (typically a Pojo).
     *
     * <p>Note: write and read are complementary operations
     *
     * @param valueClass the expected type which to cast the {@code value} to (required)
     */
    public <T> T decode(final @NonNull Class<T> valueClass, final @NonNull Serializable value) {

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
            var decomposition = (ValueDecomposition) value;
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

        return valueSemanticsResolverProvider().get().streamValueSemantics(valueClass)
                .findFirst()
                .map(vs->vs.decompose(value));
    }

    private <T> Optional<T> fromValueDecomposition(
            final @NonNull Class<T> valueClass,
            final @NonNull ValueDecomposition decomposition) {

        return valueSemanticsResolverProvider().get().streamValueSemantics(valueClass)
                .findFirst()
                .map(vs->vs.compose(decomposition));
    }

}