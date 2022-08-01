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
import java.util.Optional;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.bookmark.IdStringifier;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.memento._Mementos.SerializingAdapter;
import org.apache.isis.core.runtime.idstringifier.IdStringifierLookupService;
import org.apache.isis.core.runtimeservices.IsisModuleCoreRuntimeServices;

import lombok.NonNull;
import lombok.val;

/**
 * Special implementation of {@link SerializingAdapter}, intended as an 'internal' service.
 *
 * @implNote uses {@link Bookmark} for any non {@link Serializable} objects, while
 * any {@link Serializable} objects are written/read directly
 */
@Service
@Named(IsisModuleCoreRuntimeServices.NAMESPACE + ".SerializingAdapterWithIdStringifier")
@Priority(PriorityPrecedence.MIDPOINT)
@Deprecated
public class SerializingAdapterWithIdStringifier implements SerializingAdapter {

    @Inject private IdStringifierLookupService idStringifierLookupService;
    @Inject private BookmarkService bookmarkService;

    @Override
    public <T> T read(final @NonNull Class<T> valueClass, final @NonNull Serializable value) {

        val idStringifierIfAny = idStringifierLookupService.lookup(valueClass);
        if(idStringifierIfAny.isPresent()) {
            final IdStringifier<T> idStringifier = idStringifierIfAny.get();
            return idStringifier.destring((String)value, null);
        }

        // see if required/desired value-class is Bookmark, then just cast
        if(Bookmark.class.equals(valueClass)) {
            return _Casts.uncheckedCast(value);
        }

        // otherwise, perhaps the value itself is a Bookmark, in which case we treat it as a
        // reference to an Object (probably an entity) to be looked up
        if(Bookmark.class.isAssignableFrom(value.getClass())) {
            final Bookmark valueBookmark = (Bookmark) value;
            //TODO[ISIS-3103] ... potentially not working for non serializable value-types
            return _Casts.uncheckedCast(bookmarkService.lookup(valueBookmark).orElse(null));
        }

        // otherwise, the value was directly stored/written, so just recover as is
        return _Casts.uncheckedCast(value);
    }

    @Override
    public Serializable write(final Object value) {
        return write(_Casts.uncheckedCast(value), value.getClass());
    }

    // -- HELPER

    private <T> Serializable write(final T value, final Class<T> aClass) {

        Optional<IdStringifier<T>> idStringifierIfAny = idStringifierLookupService.lookup(aClass);
        if(idStringifierIfAny.isPresent()) {
            final IdStringifier<T> idStringifier = idStringifierIfAny.get();
            return idStringifier.enstring(value);
        }

        //TODO[ISIS-3103] ... potentially not working for non serializable value-types
        return bookmarkService.bookmarkForElseFail(value);
    }

}
