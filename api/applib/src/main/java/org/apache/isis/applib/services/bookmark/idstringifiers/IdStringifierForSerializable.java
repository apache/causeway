/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.isis.applib.services.bookmark.idstringifiers;

import java.io.Serializable;

import javax.annotation.Priority;
import javax.inject.Inject;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.bookmark.IdStringifier;
import org.apache.isis.applib.services.urlencoding.UrlEncodingService;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.memento._Mementos;

import lombok.NonNull;

@Component
@Priority(PriorityPrecedence.LATE + 50) // after more specific stringifiers
public class IdStringifierForSerializable extends IdStringifier.Abstract<Serializable> {

    private final UrlEncodingService codec;
    private final _Mementos.SerializingAdapter serializer;

    @Inject
    public IdStringifierForSerializable(
            final @NonNull UrlEncodingService codec) {
        super(Serializable.class);
        this.codec = codec;
        this.serializer = new _Mementos.SerializingAdapter() {
            @Override
            public Serializable write(final Object value) {
                return _Casts.uncheckedCast(value);
            }

            @Override
            public <T> T read(final Class<T> cls, final Serializable value) {
                return _Casts.uncheckedCast(value);
            }
        };
    }

    @Override
    public boolean handles(final @NonNull Class<?> candidateValueClass) {
        return PredefinedSerializables.isPredefinedSerializable(candidateValueClass);
    }

    @Override
    public String enstring(final @NonNull Serializable id) {
        return newMemento().put("id", id).asString();
    }

    @Override
    public Serializable destring(
            final @NonNull String stringified,
            final @NonNull Class<?> targetEntityClass) {
        if (_Strings.isEmpty(stringified)) {
            return null;
        }
        return _Casts.uncheckedCast(parseMemento(stringified).get("id", Object.class));
    }

    // -- HELPER

    private _Mementos.Memento newMemento() {
        return _Mementos.create(codec, serializer);
    }

    private _Mementos.Memento parseMemento(final String input) {
        return _Mementos.parse(codec, serializer, input);
    }

}
