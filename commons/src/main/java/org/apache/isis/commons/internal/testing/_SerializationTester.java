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
package org.apache.isis.commons.internal.testing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.base._Casts;

import lombok.SneakyThrows;
import lombok.val;

/**
 * in-memory serialization round-tripping
 * @since 2.0
 *
 */
public class _SerializationTester {

    @SneakyThrows
    public static byte[] marshall(final Serializable object) {
        val bos = new ByteArrayOutputStream(16*4096); // 16k initial buffer size
        try(val oos = new ObjectOutputStream(bos)) {
            oos.writeObject(object);
            oos.flush();
        }
        return bos.toByteArray();

    }

    @SneakyThrows
    public static <T> T unmarshall(final byte[] input) {
        val bis = new ByteArrayInputStream(input);
        try(val ois = new ObjectInputStream(bis)){
            return _Casts.uncheckedCast(ois.readObject());
        }
    }

    @SneakyThrows
    public static <T extends Serializable> T roundtrip(final T object) {
        val bytes = marshall(object);
        return unmarshall(bytes);
    }

    @SneakyThrows
    public static <T extends Serializable> void assertEqualsOnRoundtrip(final T object) {
        T afterRoundtrip = roundtrip(object);
        _Assert.assertEquals(object, afterRoundtrip);
    }

    @SneakyThrows
    public static void selftest() {
        String afterRoundtrip = roundtrip("Hello World!");
        _Assert.assertEquals("Hello World!", afterRoundtrip);
        assertEqualsOnRoundtrip("Hello World!");
    }

}
