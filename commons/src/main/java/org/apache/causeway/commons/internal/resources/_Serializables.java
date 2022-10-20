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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Utilities for marshalling Serializable.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 * @since 2.0
 */
public class _Serializables {

    @SneakyThrows
    public static byte[] write(
            final @NonNull Serializable object) {
        val bos = new ByteArrayOutputStream(16*4096); // 16k initial buffer size
        try(val oos = new ObjectOutputStream(bos)) {
            oos.writeObject(object);
            oos.flush();
        }
        return bos.toByteArray();

    }

    @SneakyThrows
    public static <T extends Serializable> T read(
            final @NonNull Class<T> requiredClass,
            final @NonNull InputStream content) {
        try(val ois = new ObjectInputStream(content)){
            val pojo = ois.readObject();
            if(!(requiredClass.isAssignableFrom(pojo.getClass()))) {
                throw _Exceptions.unrecoverable(
                        "de-serializion of input stream did not yield an object of required type %s",
                        requiredClass.getName());
            }
            return _Casts.uncheckedCast(pojo);
        }
    }

    @SneakyThrows
    public static <T extends Serializable> T read(
            final @NonNull Class<T> requiredClass,
            final @NonNull byte[] input) {
        try(val bis = new ByteArrayInputStream(input)) {
            return read(requiredClass, bis);
        }
    }

}
