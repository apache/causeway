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
package org.apache.causeway.viewer.restfulobjects.viewer.header;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.springframework.http.CacheControl;

import org.apache.causeway.viewer.restfulobjects.applib.RestfulMediaType;
import org.apache.causeway.viewer.restfulobjects.applib.util.MediaTypes;
import org.apache.causeway.viewer.restfulobjects.applib.util.Parser;

class ParserTest {

    @Test
    void forCacheControl() {
        final Parser<CacheControl> parser = Parser.forCacheControl();

        final CacheControl cc1 = CacheControl.maxAge(2000, TimeUnit.SECONDS);
        final CacheControl cc2 = CacheControl.noCache();
        for (final CacheControl v : new CacheControl[] { cc1, cc2 }) {
            var afterRoundtrip = parser.valueOf(parser.asString(v));
            assertEquals(v.getHeaderValue(), afterRoundtrip.getHeaderValue());
        }
    }

    @Test
    void forMediaType() {
        final Parser<org.springframework.http.MediaType> parser = Parser.forMediaType();

        for (final org.springframework.http.MediaType v : new org.springframework.http.MediaType[] {
                org.springframework.http.MediaType.APPLICATION_ATOM_XML,
                org.springframework.http.MediaType.APPLICATION_JSON,
                org.springframework.http.MediaType.APPLICATION_XHTML_XML,
                MediaTypes.parse(RestfulMediaType.APPLICATION_JSON_OBJECT)
        }) {
            final String asString = parser.asString(v);
            final org.springframework.http.MediaType valueOf = parser.valueOf(asString);
            assertThat(v, is(equalTo(valueOf)));
        }
    }

}
