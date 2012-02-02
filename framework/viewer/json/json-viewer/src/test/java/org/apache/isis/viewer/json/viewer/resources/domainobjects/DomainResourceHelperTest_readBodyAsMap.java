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
package org.apache.isis.viewer.json.viewer.resources.domainobjects;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.viewer.JsonApplicationException;

public class DomainResourceHelperTest_readBodyAsMap {

    private JsonRepresentation representation;

    @Test
    public void whenNull() throws Exception {
        representation = DomainResourceHelper.readAsMap(null);

        assertThat(representation.isMap(), is(true));
        assertThat(representation.size(), is(0));
    }

    @Test
    public void whenEmptyString() throws Exception {
        representation = DomainResourceHelper.readAsMap("");

        assertThat(representation.isMap(), is(true));
        assertThat(representation.size(), is(0));
    }

    @Test
    public void whenWhitespaceOnlyString() throws Exception {
        representation = DomainResourceHelper.readAsMap(" \t ");

        assertThat(representation.isMap(), is(true));
        assertThat(representation.size(), is(0));
    }

    @Test
    public void emptyMap() throws Exception {
        representation = DomainResourceHelper.readAsMap("{}");

        assertThat(representation.isMap(), is(true));
        assertThat(representation.size(), is(0));
    }

    @Test
    public void map() throws Exception {
        representation = DomainResourceHelper.readAsMap("{\"foo\":\"bar\"}");

        assertThat(representation.isMap(), is(true));
        assertThat(representation.size(), is(1));
    }

    @Test(expected = JsonApplicationException.class)
    public void whenArray() throws Exception {
        DomainResourceHelper.readAsMap("[]");
    }

}
