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
package org.apache.causeway.commons.internal.resources;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;

import org.jspecify.annotations.NonNull;

import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

/**
 * <h1>- internal use only -</h1>
 * <p>Utilities for marshalling {@link Serializable}.
 *
 * <p><b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 *
 * @apiNote Every code path within the framework, that requires {@link java.io.ObjectInputStream#readObject()}
 * should use this utility class to access it indirectly. This allows for easier security related code reviews.
 *
 * @since 2.0
 */
@UtilityClass
public class _Serializables {

    @SneakyThrows
    public byte[] write(
            final @NonNull Serializable object) {
        var bos = new ByteArrayOutputStream(16*4096); // 16k initial buffer size
        try(var oos = new ObjectOutputStream(bos)) {
            oos.writeObject(object);
            oos.flush();
        }
        return bos.toByteArray();
    }

    /**
     * This utility uses Java Object Serialization, which allows
     * arbitrary code to be run and is known for being the source of many Remote
     * Code Execution (RCE) vulnerabilities.
     */
    @SneakyThrows
    public <T extends Serializable> T read(
            final @NonNull Class<T> requiredClass,
            final @NonNull InputStream trustedContent) {
        try(var ois = new ObjectInputStream(trustedContent)){
            var pojo = ois.readObject();
            if(!(requiredClass.isAssignableFrom(pojo.getClass()))) {
                throw _Exceptions.unrecoverable(
                        "de-serializion of input stream did not yield an object of required type %s",
                        requiredClass.getName());
            }
            return _Casts.uncheckedCast(pojo);
        }
    }

    /**
     * This utility uses Java Object Serialization, which allows
     * arbitrary code to be run and is known for being the source of many Remote
     * Code Execution (RCE) vulnerabilities.
     */
    @SneakyThrows
    public <T extends Serializable> T read(
            final @NonNull Class<T> requiredClass,
            final @NonNull byte[] trustedBytes) {
        try(var bis = new ByteArrayInputStream(trustedBytes)) {
            return read(requiredClass, bis);
        }
    }

    /**
     * This utility uses Java Object Serialization, which allows
     * arbitrary code to be run and is known for being the source of many Remote
     * Code Execution (RCE) vulnerabilities.
     */
    @SneakyThrows
    public <T extends Serializable> T readWithCustomClassLoader(
        final @NonNull Class<T> requiredClass,
        final @NonNull ClassLoader classLoader,
        final @NonNull byte[] trustedBytes) {
        try(ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(trustedBytes)) {
            @Override
            protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                return Class.forName(desc.getName(), false, classLoader);
            }
        }) {
            var pojo = ois.readObject();
            if(!(requiredClass.isAssignableFrom(pojo.getClass()))) {
                throw _Exceptions.unrecoverable(
                        "de-serializion of input stream did not yield an object of required type %s",
                        requiredClass.getName());
            }
            return _Casts.uncheckedCast(pojo);
        }
    }

}
