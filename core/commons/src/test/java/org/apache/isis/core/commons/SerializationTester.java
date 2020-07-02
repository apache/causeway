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
package org.apache.isis.core.commons;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.junit.jupiter.api.Assertions;

import lombok.val;

/**
 * in-memory serialization round-tripping
 * @since Jul 2, 2020
 *
 */
public class SerializationTester {

    public static byte[] marshall(Serializable object) throws IOException {
        val bos = new ByteArrayOutputStream(16*4096); // 16k initial buffer size
        val oos = new ObjectOutputStream(bos);
        oos.writeObject(object);
        oos.flush();
        oos.close();
        return bos.toByteArray();
    }

    public static <T> T unmarshall(byte[] input) throws IOException, ClassNotFoundException {
        val bis = new ByteArrayInputStream(input);
        val ois = new ObjectInputStream(bis);
        @SuppressWarnings("unchecked")
        val t = (T) ois.readObject();
        bis.close(); 
        return t;
    }

    public static <T extends Serializable> T roundtrip(T object) throws IOException, ClassNotFoundException {
        val bytes = marshall(object);
        return unmarshall(bytes);
    }

    public static <T extends Serializable> void assertEqualsOnRoundtrip(T object) {
        T afterRoundtrip;
        try {
            afterRoundtrip = roundtrip(object);
        } catch (ClassNotFoundException | IOException e) {
            Assertions.fail(e);
            return;
        }
        Assertions.assertEquals(object, afterRoundtrip);
    }

}
