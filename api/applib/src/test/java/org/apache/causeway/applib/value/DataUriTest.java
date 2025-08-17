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
package org.apache.causeway.applib.value;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.RequiredArgsConstructor;

class DataUriTest {

    @RequiredArgsConstructor
    enum Scenario {
        HALLO_WORLD("text/plain", null, DataUri.Encoding.BASE64, "Hello, World!".getBytes(StandardCharsets.UTF_8),
                "data:text/plain;base64,SGVsbG8sIFdvcmxkIQ=="),

//        TEXT_OTHER("text/vnd-example+xyz;foo=bar", "base64", "R0lGODdh".getBytes(StandardCharsets.UTF_8),
//                "data:text/vnd-example+xyz;foo=bar;base64,R0lGODdh"),
//
        TEXT_ADVANCED("text/plain", List.of("charset=UTF-8", "page=21"), DataUri.Encoding.NONE, "the data:1234,5678".getBytes(StandardCharsets.UTF_8),
                "data:text/plain;charset=UTF-8;page=21,the%20data%3A1234%2C5678"),
        ;
        final String mediaType;
        final List<String> parameters;
        final DataUri.Encoding encoding;
        final byte[] data;
        final String externalForm;
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    void rundtrip(Scenario scenario) {
        var ref = new DataUri(scenario.mediaType, scenario.parameters, scenario.encoding, scenario.data);
        var parsed = DataUri.parse(scenario.externalForm);
        switch (scenario) {
        case TEXT_ADVANCED: {
            assertEquals(scenario.externalForm, ref.toExternalForm());
            assertEquals(ref, parsed); // check equality relation
        }
        default:
            assertEquals(scenario.externalForm, ref.toExternalForm());
            assertEquals(ref, parsed); // check equality relation
        }

    }

}
