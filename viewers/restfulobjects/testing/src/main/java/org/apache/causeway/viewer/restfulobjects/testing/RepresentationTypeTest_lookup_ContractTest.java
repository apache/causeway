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
package org.apache.causeway.viewer.restfulobjects.testing;

import javax.ws.rs.core.MediaType;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.apache.causeway.viewer.restfulobjects.applib.RepresentationType;


/**
 * contract test.
 */
public abstract class RepresentationTypeTest_lookup_ContractTest {

    @Test
    public void roundtrip() {
        for (final RepresentationType repType : RepresentationType.values()) {
            final String name = repType.getName();
            final RepresentationType lookup = RepresentationType.lookup(name);
            assertSame(repType, lookup);
        }
    }

    @Test
    public void roundtrip_overloaded() {
        for (final RepresentationType repType : RepresentationType.values()) {
            final MediaType mediaType = repType.getJsonMediaType();
            final RepresentationType lookup = RepresentationType.lookup(mediaType);
            assertSame(repType, lookup);
        }
    }

    @Test
    public void whenUnknown() {
        assertThat(RepresentationType.lookup(MediaType.APPLICATION_SVG_XML), is(RepresentationType.GENERIC));
        assertThat(RepresentationType.lookup("foobar"), is(RepresentationType.GENERIC));

    }

    @Test
    public void whenNull() {
        assertThat(RepresentationType.lookup((MediaType) null), is(RepresentationType.GENERIC));
        assertThat(RepresentationType.lookup((String) null), is(RepresentationType.GENERIC));
    }

    @Test
    public void whenDomainObjectWithXRoParameter() {
        MediaType toLookup = RepresentationType.DOMAIN_OBJECT
                .getMediaType("x-ro-domain-type", "http://mycompany.com:39393/domain-types/JdkValuedEntities");

        // ignores the parameter ...
        assertThat(
                RepresentationType.lookup(toLookup), is(RepresentationType.DOMAIN_OBJECT));
    }

}
