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
package org.apache.causeway.core.metamodel.util.hmac;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import org.apache.causeway.applib.exceptions.unrecoverable.DigitalVerificationException;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.commons.internal.context._Context;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.resources._Serializables;

record SecureMemento(
        MementoHmacContext context,
        // we need a Serializable Map
        Map<String, Serializable> valuesByKey
        ) implements Memento {

    SecureMemento(final MementoHmacContext context) {
        this(context, new HashMap<>());
    }

    SecureMemento {
        Objects.requireNonNull(context);
        Objects.requireNonNull(valuesByKey);
        Assert.isTrue(Serializable.class.isInstance(valuesByKey), ()->"map is expected to be serializable");
    }

    @Override
    public Memento put(final @NonNull String name, final Object value) {
        if(value==null) return this; //no-op, there is no point in storing null values

        valuesByKey.put(name, context.valueCodec().encode(value));
        return this;
    }

    @Override
    public <T> T get(final String name, final Class<T> cls) {
        final Serializable value = valuesByKey.get(name);
        if(value==null) return null;

        return context.valueCodec().decode(cls, value);
    }

    @Override
    public Set<String> keySet() {
        return _Sets.unmodifiable(valuesByKey.keySet());
    }

    @Override
    public byte[] stateAsBytes() {
        return _Serializables.write((Serializable) valuesByKey);
    }

    @Override
    public String toExternalForm() {
        try {
            return context.hmacUrlCodec().encodeForUrl(stateAsBytes());
        } catch (Exception e) {
            throw new IllegalArgumentException("failed to serialize memento", e);
        }
    }

    // -- PARSER

    static Memento parseDigitallySignedMemento(
            final MementoHmacContext context,
            final @Nullable String untrustedEncodedString) {
        if(!StringUtils.hasText(untrustedEncodedString)) throw new DigitalVerificationException("invalid memento data");

        var trustedBytes = context.hmacUrlCodec().decodeFromUrl(untrustedEncodedString).orElse(null);
        if(trustedBytes==null) throw new DigitalVerificationException("invalid memento data");

        return parseTrustedMemento(context, trustedBytes);
    }

    static Memento parseTrustedMemento(
        final MementoHmacContext context,
        final byte[] trustedBytes) {
        try {
            final HashMap<String, Serializable> valuesByKey = _Casts.uncheckedCast(
                _Serializables.readWithCustomClassLoader(HashMap.class, _Context.getDefaultClassLoader(), trustedBytes));
            return new SecureMemento(context, valuesByKey);
        } catch (Exception e) {
            throw _Exceptions.illegalArgument(e,
                    "failed to parse memento from serialized string '%s'",
                    _Strings.ellipsifyAtEnd(new String(trustedBytes, StandardCharsets.UTF_8) , 200, "..."));
        }
    }
}
