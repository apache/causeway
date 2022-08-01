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
package org.apache.isis.core.runtimeservices.serializing;

import java.io.Serializable;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.bookmark.idstringifiers.PredefinedSerializables;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.commons.internal.memento._Mementos.SerializingAdapter;
import org.apache.isis.core.runtime.idstringifier.IdStringifierLookupService;
import org.apache.isis.core.runtimeservices.IsisModuleCoreRuntimeServices;

import lombok.NonNull;
import lombok.Value;

/**
 * Default implementation of {@link SerializingAdapter}, intended as an 'internal' service.
 *
 * @implNote uses {@link Bookmark} or {@link StringifiedValueMemento}
 * for identifiable objects or any non {@link Serializable} objects,
 * while some predefined serializable types that implement {@link Serializable}
 * are written/read directly
 *
 * @see PredefinedSerializables
 */
@Service
@Named(IsisModuleCoreRuntimeServices.NAMESPACE + ".SerializingAdapterDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("Default")
public class SerializingAdapterDefault implements SerializingAdapter {

    @Inject private BookmarkService bookmarkService;
    @Inject private IdStringifierLookupService idStringifierLookupService;

    @Override
    public Serializable write(final @NonNull Object value) {
        if(PredefinedSerializables.isPredefinedSerializable(value.getClass())) {
            // the value can be stored/written directly without conversion to a bookmark
            return (Serializable) value;
        }

        // potentially not working for non serializable value-types
        return bookmarkService.bookmarkFor(value)
                .map(Serializable.class::cast)
                // so fallback to registered IdStringifieries
                .orElseGet(()->writeNonPredefinedSerializableValue(value));
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

        // otherwise, perhaps the value itself is a StringifiedValueMemento, in which case we treat it as a
        // memento for a non-predefined-serializable value to be reconstructed
        if(value instanceof StringifiedValueMemento) {
            return readNonPredefinedSerializableValue(valueClass, (StringifiedValueMemento) value);
        }

        // otherwise, the value was directly stored/written, so just recover as is
        return _Casts.uncheckedCast(value);
    }

    // -- HELPER

    @Value(staticConstructor = "of")
    private static final class StringifiedValueMemento implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String stringifiedValue;
    }

    private <T> StringifiedValueMemento writeNonPredefinedSerializableValue(
            final @NonNull T value) {

        final Class<T> valueClass = _Casts.uncheckedCast(value.getClass());

        return idStringifierLookupService.lookup(valueClass)
            .map(idStringifier->idStringifier.enstring(value))
            .map(stringifiedValue->StringifiedValueMemento.of(stringifiedValue))
            .orElseThrow(()->
                _Exceptions.unrecoverable("cannot create a memento for object of type %s", valueClass));
    }

    private <T> T readNonPredefinedSerializableValue(
            final @NonNull Class<T> valueClass,
            final @NonNull StringifiedValueMemento memento) {

        return idStringifierLookupService.lookup(valueClass)
                .map(idStringifier->idStringifier.destring(memento.getStringifiedValue(), null))
                .orElseThrow(()->
                    _Exceptions.unrecoverable("cannot restore object of type %s from memento '%s'",
                            valueClass, memento));
    }

}
