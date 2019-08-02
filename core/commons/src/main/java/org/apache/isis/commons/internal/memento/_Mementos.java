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

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.exceptions._Exceptions;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Provides framework internal memento support.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
public final class _Mementos {

    private _Mementos(){}

    // -- ENCODE-DECODER INTERFACE

    public static interface EncoderDecoder {
        public String encode(final byte[] bytes);
        public byte[] decode(String str);
    }

    // -- MEMENTO INTERFACE

    /**
     * Similar to a {@link Map}&lt;String, Object&gt; for key/value pairs,
     * but in addition allows to-String <em>serialization</em> and
     * from-String <em>de-serialization</em> of the entire map.
     */
    public static interface Memento {

        /**
         * Returns the Object associated with {@code name}
         * @param name
         * @param cls the expected type which to cast the retrieved value to (required)
         * @return
         */
        public <T> T get(String name, Class<T> cls);

        /**
         * Behaves like a {@link HashMap}, but returns the Memento itself.
         * @param name
         * @param value
         * @return self
         */
        public Memento put(String name, Object value);

        /**
         * @return an unmodifiable key-set of this map
         */
        public Set<String> keySet();

        /**
         * @return to-String <em>serialization</em> of this map
         */
        public String asString();
    }

    // -- SERIALIZER INTERFACE

    /**
     * Coder/Decoder from {@link Object} to {@link Serializable}
     */
    public static interface SerializingAdapter {

        /**
         * Converts the value into a {@link Serializable} that is write-able to an {@link ObjectOutput}.<br/>
         * Note: write and read are complementary operators.
         * @param value
         * @return
         */
        public Serializable write(Object value);

        /**
         * Converts the {@link Serializable} {@code value} as read from an {@link ObjectInput} back into its
         * original (typically a Pojo).<br/>
         * Note: write and read are complementary operators.
         * @param cls the expected type which to cast the {@code value} to (required)
         * @param value
         * @return
         */
        public <T> T read(Class<T> cls, Serializable value);
    }

    // -- MEMENTO CONSTRUCTION

    /**
     * Creates an empty {@link Memento}.
     *
     * <p>
     * Typically followed by {@link Memento#put(String, Object)} for each of the data values to
     * add to the {@link Memento}, then {@link Memento#asString()} to convert to a string format.
     * </p>
     *
     * @param codec (required)
     * @param serializer (required)
     * @return non-null
     */
    public static Memento create(EncoderDecoder codec, SerializingAdapter serializer) {
        return new _Mementos_MementoDefault(codec, serializer);
    }

    /**
     * Parse string returned from {@link Memento#asString()}
     *
     * <p>
     * Typically followed by {@link Memento#get(String, Class)} for each of the data values held
     * in the {@link Memento}.
     * </p>
     *
     * @param codec (required)
     * @param serializer (required)
     * @param input
     * @return {@code empty()} if {@code input} is empty
     *
     * @throws IllegalArgumentException if parsing fails
     *
     */
    public static Memento parse(
            final EncoderDecoder codec,
            final SerializingAdapter serializer,
            final String input) {

        if(_Strings.isNullOrEmpty(input)) {
            return empty();
        }
        return _Mementos_MementoDefault.parse(codec, serializer, input);
    }

    // -- EMPTY MEMENTO

    private static final class EmptyMemento implements Memento {

        @Override
        public <T> T get(String name, Class<T> cls) {
            return null;
        }

        @Override
        public Memento put(String name, Object value) {
            throw _Exceptions.notImplemented();
        }

        @Override
        public Set<String> keySet() {
            return Collections.emptySet();
        }

        @Override
        public String asString() {
            return "EmptyMemento";
        }

    }

    private final static Memento EMPTY_MEMENTO = new EmptyMemento();

    public static Memento empty() {
        return EMPTY_MEMENTO;
    }

}
