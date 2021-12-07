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
package org.apache.isis.viewer.restfulobjects.applib;

import java.io.IOException;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.isis.commons.functional.IndexedConsumer;

class JsonRepresentationTest_putXxx {

    private JsonRepresentation jsonRepresentation;

    @BeforeEach
    void setUp() throws Exception {
        jsonRepresentation = JsonRepresentation.newMap();
    }

    @Test
    void putInt() throws IOException {
        jsonRepresentation.mapPut("a", 123);
        assertThat(jsonRepresentation.getInt("a"), is(123));
    }

    @Test
    void putInt_multipart() throws IOException {
        jsonRepresentation.mapPut("a.b", 456);
        assertThat(jsonRepresentation.getInt("a.b"), is(456));
    }

    @Test
    void putInt_pathBlockedByValue() throws IOException {
        jsonRepresentation.mapPut("a", 123);
        assertThrows(IllegalArgumentException.class, ()->
            jsonRepresentation.mapPut("a.b", 456));
    }

    @Test
    void preserveMapOrder() throws IOException {
        final int[] values = new Random().ints(128, -4096, 4096).toArray();
        {
            int index = 0;
            for(int value : values) {
                jsonRepresentation.mapPut("i" + index, value);
                ++index;
            }
        }

        jsonRepresentation.streamMapEntries()
        .forEach(IndexedConsumer.zeroBased((index, entry)->{
            assertThat(entry.getKey(), is("i" + index));
        }));
    }

}
