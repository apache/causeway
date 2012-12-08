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
package org.apache.isis.viewer.restfulobjects.tck.resources.service.services;

import static org.apache.isis.viewer.restfulobjects.tck.RepresentationMatchers.assertThat;
import static org.apache.isis.viewer.restfulobjects.tck.RepresentationMatchers.isArray;
import static org.apache.isis.viewer.restfulobjects.tck.RepresentationMatchers.isFollowableLinkToSelf;
import static org.apache.isis.viewer.restfulobjects.tck.RepresentationMatchers.isLink;
import static org.apache.isis.viewer.restfulobjects.tck.RepresentationMatchers.isMap;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.runtimes.dflt.webserver.WebServer;
import org.apache.isis.viewer.restfulobjects.applib.HttpMethod;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.links.LinkRepresentation;
import org.apache.isis.viewer.restfulobjects.domainobjects.DomainServiceResource;
import org.apache.isis.viewer.restfulobjects.domainobjects.ListRepresentation;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;

public class DomainServiceResourceTest_services_representationAndHeaders {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();

    private RestfulClient client;
    private DomainServiceResource resource;

    @Before
    public void setUp() throws Exception {
        final WebServer webServer = webServerRule.getWebServer();
        client = new RestfulClient(webServer.getBase());

        resource = client.getDomainServiceResource();
    }

    @Test
    public void representation() throws Exception {

        // when
        final Response response = resource.services();
        final RestfulResponse<ListRepresentation> restfulResponse = RestfulResponse.ofT(response);

        // then
        assertThat(restfulResponse.getStatus(), is(HttpStatusCode.OK));
        assertThat(restfulResponse.getHeader(RestfulResponse.Header.CONTENT_TYPE), is(RepresentationType.LIST.getMediaType()));
        assertThat(restfulResponse.getHeader(RestfulResponse.Header.CACHE_CONTROL).getMaxAge(), is(24 * 60 * 60));

        final ListRepresentation repr = restfulResponse.getEntity();

        assertThat(repr, isMap());

        assertThat(repr.getSelf(), isLink().httpMethod(HttpMethod.GET));

        assertThat(repr.getValues(), isArray());

        assertThat(repr.getLinks(), isArray());
        assertThat(repr.getExtensions(), isMap());
    }

    @Test
    public void self_isFollowable() throws Exception {
        // given
        final ListRepresentation repr = givenRepresentation();

        // when, then
        assertThat(repr, isFollowableLinkToSelf(client));
    }

    @Test
    public void linksToDomainServiceResources() throws Exception {

        // given
        final ListRepresentation repr = givenRepresentation();

        // when
        final JsonRepresentation values = repr.getValues();

        // then
        for (final LinkRepresentation link : values.arrayIterable(LinkRepresentation.class)) {
            assertThat("HiddenRepository should not show up in services list", false, is(link.getHref().endsWith("HiddenRepository")));

            final RestfulResponse<JsonRepresentation> followJsonResp = client.follow(link);
            assertThat(followJsonResp.getStatus().getFamily(), is(Family.SUCCESSFUL));

            final JsonRepresentation followRepr = followJsonResp.getEntity();
            final LinkRepresentation self = followRepr.getLink("links[rel=self]");

            assertThat(self.getHref(), is(link.getHref()));
        }
    }

    private ListRepresentation givenRepresentation() throws JsonParseException, JsonMappingException, IOException {
        final RestfulResponse<ListRepresentation> jsonResp = RestfulResponse.ofT(resource.services());
        return jsonResp.getEntity();
    }

}
