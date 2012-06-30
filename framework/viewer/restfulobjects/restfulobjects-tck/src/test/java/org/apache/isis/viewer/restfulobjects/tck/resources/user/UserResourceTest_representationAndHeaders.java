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
package org.apache.isis.viewer.restfulobjects.tck.resources.user;

import static org.apache.isis.viewer.restfulobjects.tck.RepresentationMatchers.assertThat;
import static org.apache.isis.viewer.restfulobjects.tck.RepresentationMatchers.hasMaxAge;
import static org.apache.isis.viewer.restfulobjects.tck.RepresentationMatchers.hasParameter;
import static org.apache.isis.viewer.restfulobjects.tck.RepresentationMatchers.hasSubType;
import static org.apache.isis.viewer.restfulobjects.tck.RepresentationMatchers.hasType;
import static org.apache.isis.viewer.restfulobjects.tck.RepresentationMatchers.isArray;
import static org.apache.isis.viewer.restfulobjects.tck.RepresentationMatchers.isFollowableLinkToSelf;
import static org.apache.isis.viewer.restfulobjects.tck.RepresentationMatchers.isLink;
import static org.apache.isis.viewer.restfulobjects.tck.RepresentationMatchers.isMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.runtimes.dflt.webserver.WebServer;
import org.apache.isis.viewer.restfulobjects.applib.HttpMethod;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.RestfulResponse.Header;
import org.apache.isis.viewer.restfulobjects.applib.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.user.UserRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.user.UserResource;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;

public class UserResourceTest_representationAndHeaders {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();

    private RestfulClient client;
    private UserResource resource;

    @Before
    public void setUp() throws Exception {
        final WebServer webServer = webServerRule.getWebServer();
        client = new RestfulClient(webServer.getBase());

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

        assertThat(repr.getSelf(), isLink(client).httpMethod(HttpMethod.GET));
        assertThat(repr.getUserName(), is(not(nullValue())));
        assertThat(repr.getFriendlyName(), is(nullValue())); // TODO: change
                                                             // fixture so
                                                             // populated
        assertThat(repr.getEmail(), is(nullValue())); // TODO: change fixture so
                                                      // populated
        assertThat(repr.getRoles(), is(not(nullValue()))); // TODO: change
                                                           // fixture so have
                                                           // non-empty list

        assertThat(repr.getLinks(), isArray());
        assertThat(repr.getExtensions(), isMap());
    }

    @Test
    public void headers() throws Exception {

        // given
        final Response resp = resource.user();

        // when
        final RestfulResponse<UserRepresentation> restfulResponse = RestfulResponse.ofT(resp);

        // then
        final MediaType contentType = restfulResponse.getHeader(Header.CONTENT_TYPE);
        assertThat(contentType, hasType("application"));
        assertThat(contentType, hasSubType("json"));
        assertThat(contentType, hasParameter("profile", "urn:org.restfulobjects/user"));
        assertThat(contentType, is(RepresentationType.USER.getMediaType()));

        // then
        final CacheControl cacheControl = restfulResponse.getHeader(Header.CACHE_CONTROL);
        assertThat(cacheControl, hasMaxAge(60 * 60));
        assertThat(cacheControl.getMaxAge(), is(60 * 60));
    }

    @Test
    public void self_isFollowable() throws Exception {
        // given
        final UserRepresentation repr = givenRepresentation();

        // when, then
        assertThat(repr, isFollowableLinkToSelf(client));
    }

    private UserRepresentation givenRepresentation() throws JsonParseException, JsonMappingException, IOException {
        final RestfulResponse<UserRepresentation> jsonResp = RestfulResponse.ofT(resource.user());
        return jsonResp.getEntity();
    }

}
