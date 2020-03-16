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
package org.apache.isis.persistence.jdo.datanucleus5.metamodel.facets.object.persistencecapable;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import javax.jdo.annotations.EmbeddedOnly;

import org.apache.isis.applib.services.urlencoding.UrlEncodingService;
import org.apache.isis.core.commons.internal.base._Bytes;
import org.apache.isis.core.commons.internal.base._Casts;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.commons.internal.memento._Mementos;
import org.apache.isis.core.commons.internal.memento._Mementos.Memento;
import org.apache.isis.core.commons.internal.memento._Mementos.SerializingAdapter;

import lombok.val;

/**
 * Used package locally, for serializable types, that are persistence-capable and embedded-only.
 * @see {@link EmbeddedOnly}
 * @since 2.0
 *
 */
final class MementoUtil {

    private final static UrlEncodingService codec = new UrlEncodingService() {
        @Override
        public String encode(final byte[] bytes) {
            return _Strings.ofBytes(_Bytes.asCompressedUrlBase64.apply(bytes), StandardCharsets.UTF_8);
        }
        @Override
        public byte[] decode(final String str) {
            return _Bytes.ofCompressedUrlBase64.apply(_Strings.toBytes(str, StandardCharsets.UTF_8));
        }
    };
    
    private final static SerializingAdapter serializer = new SerializingAdapter() {
        @Override
        public Serializable write(Object value) {
            return (Serializable) value;
        }
        @Override
        public <T> T read(Class<T> cls, Serializable value) {
            return _Casts.castToOrElseNull(value, cls);
        }
    };

    public static Memento createMemento(Object pojo) {
        val memento = _Mementos.create(codec, serializer);
        memento.put("payload", pojo);
        return memento;
    }

    public static <T> T parse(Class<T> type, String identifier) {
        val memento = _Mementos.parse(codec, serializer, identifier);
        return memento.get("payload", type);
    }
    
}
