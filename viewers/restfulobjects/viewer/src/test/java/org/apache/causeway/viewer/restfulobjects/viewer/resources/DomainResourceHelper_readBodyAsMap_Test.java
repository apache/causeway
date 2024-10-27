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
package org.apache.causeway.viewer.restfulobjects.viewer.resources;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.causeway.viewer.restfulobjects.rendering.util.RequestParams;

class DomainResourceHelper_readBodyAsMap_Test {

    @Test
    void whenNull() throws Exception {
        var representation = representationFor(null);

        assertTrue(representation.isMap());
        assertEquals(0, representation.size());
    }

    @Test
    void whenEmptyString() throws Exception {
        var representation = representationFor("");

        assertTrue(representation.isMap());
        assertEquals(0, representation.size());
    }

    @Test
    void whenWhitespaceOnlyString() throws Exception {
        var representation = representationFor(" \t ");

        assertTrue(representation.isMap());
        assertEquals(0, representation.size());
    }

    @Test
    void emptyMap() throws Exception {
        var representation = representationFor("{}");

        assertTrue(representation.isMap());
        assertEquals(0, representation.size());
    }

    @Test
    void map() throws Exception {
        var representation = representationFor("{\"foo\":\"bar\"}");

        assertTrue(representation.isMap());
        assertEquals(1, representation.size());
    }

    @Test
    void whenArray() throws Exception {
        assertThrows(RestfulObjectsApplicationException.class, ()->{
            representationFor("[]");
        });
    }

    // -- HELPER

    private JsonRepresentation representationFor(final String input) {
        return RequestParams.ofQueryString(input).asMap();
    }

}
