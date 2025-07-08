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
package org.apache.causeway.viewer.restfulobjects.applib.domainobjects;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;

import static org.apache.causeway.viewer.restfulobjects.applib.JsonFixture.readJson;

class DomainObjectRepresentationTest_getCollection {

    private DomainObjectRepresentation representation;

    @BeforeEach
    void setUp() throws Exception {
        representation = new DomainObjectRepresentation(readJson("domainObjectRepresentation.json"));
    }

    @Test
    void getCollection() throws IOException {
        final JsonRepresentation flag = representation.getCollection("someCollection");
        assertThat(flag, is(not(nullValue())));
        assertThat(flag.getBoolean("value"), is(true));
    }

}
