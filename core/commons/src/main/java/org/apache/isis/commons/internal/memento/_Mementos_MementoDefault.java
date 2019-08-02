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

package org.apache.isis.commons.internal.memento;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.memento._Mementos.EncoderDecoder;
import org.apache.isis.commons.internal.memento._Mementos.Memento;
import org.apache.isis.commons.internal.memento._Mementos.SerializingAdapter;

import static org.apache.isis.commons.internal.base._With.requires;

/**
 *
 * package private mixin for utility class {@link _Mementos}
 *
 * Memento default implementation.
 *
 */
class _Mementos_MementoDefault implements _Mementos.Memento {

    private final EncoderDecoder codec;
    private final SerializingAdapter serializer;

    private final Map<String, Serializable> valuesByKey;

    _Mementos_MementoDefault(EncoderDecoder codec, SerializingAdapter serializer) {
        this(codec, serializer, _Maps.newHashMap());
    }

    private _Mementos_MementoDefault(
            EncoderDecoder codec,
            SerializingAdapter serializer,
            Map<String, Serializable> valuesByKey) {

        this.codec = requires(codec, "codec");
        this.serializer = requires(serializer, "serializer");
        this.valuesByKey = requires(valuesByKey, "valuesByKey");
    }

    @Override
    public Memento put(String name, Object value) {
        if(value==null) {
            return this; //no-op, there is no point in storing null values
        }
        requires(name, "name");
        valuesByKey.put(name, serializer.write(value));
        return this;
    }

    @Override
    public <T> T get(String name, Class<T> cls) {
        final Serializable value = valuesByKey.get(name);
        if(value==null) {
            return null;
        }
        return serializer.read(cls, value);
    }

    @Override
    public Set<String> keySet() {
        return _Sets.unmodifiable(valuesByKey.keySet());
    }

    @Override
    public String asString() {
        final ByteArrayOutputStream os = new ByteArrayOutputStream(16*1024); // 16k initial size
        try(ObjectOutputStream oos = new ObjectOutputStream(os)){
            oos.writeObject(valuesByKey); // write the entire map to the byte-buffer
        } catch (Exception e) {
            throw new IllegalArgumentException("failed to serialize memento", e);
        }
        return codec.encode(os.toByteArray()); // convert bytes from byte-buffer to encoded string
    }

    // -- PARSER

    static Memento parse(EncoderDecoder codec, SerializingAdapter serializer, @Nullable String str) {
        requires(codec, "codec");
        if(_NullSafe.isEmpty(str)) {
            return null;
        }
        try(ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(codec.decode(str))) {
            //override ObjectInputStream's class-loading
            @Override
            protected Class<?> resolveClass(ObjectStreamClass desc)
                    throws IOException, ClassNotFoundException
            {
                String name = desc.getName();
                return Class.forName(name, false, _Context.getDefaultClassLoader());
            }
        }) {
            // read in the entire map
            final Map<String, Serializable> valuesByKey = _Casts.uncheckedCast(ois.readObject());
            return new _Mementos_MementoDefault(codec, serializer, valuesByKey);
        } catch (Exception e) {
            throw new IllegalArgumentException("failed to parse memento from serialized string", e);
        }
    }

}
