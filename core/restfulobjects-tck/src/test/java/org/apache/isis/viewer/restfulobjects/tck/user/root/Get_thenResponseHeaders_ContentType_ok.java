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
package org.apache.isis.viewer.restfulobjects.tck.user.root;

import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.hasParameter;
import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.hasSubType;
import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.hasMediaType;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.Header;
import org.apache.isis.viewer.restfulobjects.applib.user.UserRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.user.UserResource;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;

public class Get_thenResponseHeaders_ContentType_ok {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();

    private RestfulClient client;
    private UserResource resource;

    @Before
    public void setUp() throws Exception {
        client = webServerRule.getClient();
        resource = client.getUserResource();
    }


    @Test
    public void ok() throws Exception {

        // given
        final Response resp = resource.user();

        // when
        final RestfulResponse<UserRepresentation> restfulResponse = RestfulResponse.ofT(resp);

        // then
        final MediaType contentType = restfulResponse.getHeader(Header.CONTENT_TYPE);
        assertThat(contentType, hasMediaType("application"));
        assertThat(contentType, hasSubType("json"));
        assertThat(contentType, hasParameter("profile", "urn:org.restfulobjects:repr-types/user"));
        assertThat(contentType, is(RepresentationType.USER.getMediaType()));
    }
}
