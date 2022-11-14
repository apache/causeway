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
package org.apache.causeway.viewer.restfulobjects.applib;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class JsonRepresentationTest_urlEncoding {

    @Test
    public void test() throws UnsupportedEncodingException {
        final LinkRepresentation l = new LinkRepresentation().withRel("object").withHref("http://localhost:8080/objects/ABC:123").withMethod(RestfulHttpMethod.GET);

        final String expectedValue = "%7B%22method%22%3A%22GET%22%2C%22rel%22%3A%22object%22%2C%22href%22%3A%22http%3A%2F%2Flocalhost%3A8080%2Fobjects%2FABC%3A123%22%7D";
        @SuppressWarnings("unused")
        final String decoded = URLDecoder.decode(expectedValue, StandardCharsets.UTF_8.name());

        assertThat(l.asUrlEncoded(), is(expectedValue));

    }
}
