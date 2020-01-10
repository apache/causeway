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
package org.apache.isis.viewer.restfulobjects.applib.client;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RestfulRequest;
import org.apache.isis.viewer.restfulobjects.applib.RestfulRequest.DomainModel;
import org.apache.isis.viewer.restfulobjects.applib.RestfulRequest.RequestParameter;

public class RestfulRequest_RequestParameterTest_valueOf_xrodomainmodel {

    private final RequestParameter<DomainModel> requestParameter = RestfulRequest.RequestParameter.DOMAIN_MODEL;

    private JsonRepresentation repr;

    @Before
    public void setUp() throws Exception {
        repr = JsonRepresentation.newMap();
    }

    @Test
    public void simple() {
        repr.mapPut("x-ro-domain-model", "simple");
        final DomainModel valueOf = requestParameter.valueOf(repr);

        assertThat(valueOf, is(DomainModel.SIMPLE));
    }

    @Test
    public void whenNone() {
        final DomainModel valueOf = requestParameter.valueOf(repr);

        assertThat(valueOf, is(DomainModel.FORMAL));
    }
}
