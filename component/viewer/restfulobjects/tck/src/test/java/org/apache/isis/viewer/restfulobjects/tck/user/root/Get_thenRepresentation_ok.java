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

import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.assertThat;
import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.isArray;
import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.isLink;
import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.isMap;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulHttpMethod;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.user.UserRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.user.UserResource;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;

public class Get_thenRepresentation_ok {

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
    public void representation() throws Exception {

        // given
        final Response resp = resource.user();

        // when
        final RestfulResponse<UserRepresentation> jsonResp = RestfulResponse.ofT(resp);
        assertThat(jsonResp.getStatus().getFamily(), is(Family.SUCCESSFUL));

        // then
        assertThat(jsonResp.getStatus(), is(HttpStatusCode.OK));

        final UserRepresentation repr = jsonResp.getEntity();
        assertThat(repr, is(not(nullValue())));
        assertThat(repr.isMap(), is(true));

        assertThat(repr.getSelf(), isLink(client)
                                    .rel(Rel.SELF)
                                    .href(endsWith(":39393/user"))
                                    .httpMethod(RestfulHttpMethod.GET)
                                    .type(RepresentationType.USER.getMediaType())
                                    .returning(HttpStatusCode.OK)
                                    );
        
        assertThat(repr.getUp(), isLink(client)
                                    .rel(Rel.UP)
                                    .href(endsWith(":39393/"))
                                    .httpMethod(RestfulHttpMethod.GET)
                                    .type(RepresentationType.HOME_PAGE.getMediaType())
                                    .returning(HttpStatusCode.OK)
                                    );
        assertThat(repr.getUserName(), is(not(nullValue())));
        
        // TODO: change fixture so populated
        assertThat(repr.getFriendlyName(), is(nullValue())); 
        assertThat(repr.getEmail(), is(nullValue())); 
        assertThat(repr.getRoles(), is(not(nullValue()))); 

        assertThat(repr.getLinks(), isArray());
        assertThat(repr.getExtensions(), isMap());
    }
}


