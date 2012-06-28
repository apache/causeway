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
package org.apache.isis.viewer.json.tck.resources.service.serviceId;

import static org.apache.isis.core.commons.matchers.IsisMatchers.matches;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.assertThat;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.isArray;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.isFollowableLinkToSelf;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.isLink;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.isMap;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.runtimes.dflt.webserver.WebServer;
import org.apache.isis.viewer.json.applib.HttpMethod;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.applib.RestfulClient;
import org.apache.isis.viewer.json.applib.RestfulResponse;
import org.apache.isis.viewer.json.applib.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.json.applib.domainobjects.DomainObjectRepresentation;
import org.apache.isis.viewer.json.applib.domainobjects.DomainServiceResource;
import org.apache.isis.viewer.json.tck.IsisWebServerRule;

public class DomainServiceResourceTest_serviceId_representationAndHeaders {

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

        // given
        final Response resp = resource.service("simples");

        // when
        final RestfulResponse<DomainObjectRepresentation> jsonResp = RestfulResponse.ofT(resp);

        // then
        assertThat(jsonResp.getStatus(), is(HttpStatusCode.OK));

        final DomainObjectRepresentation repr = jsonResp.getEntity();

        assertThat(repr, isMap());

        assertThat(repr.getSelf(), isLink().httpMethod(HttpMethod.GET));
        assertThat(repr.getOid(), matches("OID[:].+"));
        assertThat(repr.getTitle(), matches("Simples"));

        assertThat(repr.getMembers(), isArray());

        assertThat(repr.getLinks(), isArray());
        assertThat(repr.getExtensions(), isMap());
    }

    @Test
    public void headers() throws Exception {
        // given
        final Response resp = resource.service("simples");

        // when
        final RestfulResponse<DomainObjectRepresentation> jsonResp = RestfulResponse.ofT(resp);

        // then
        assertThat(jsonResp.getStatus(), is(HttpStatusCode.OK));
        assertThat(jsonResp.getHeader(RestfulResponse.Header.CONTENT_TYPE), is(RepresentationType.DOMAIN_OBJECT.getMediaType()));
        assertThat(jsonResp.getHeader(RestfulResponse.Header.CACHE_CONTROL).getMaxAge(), is(24 * 60 * 60));
    }

    @Test
    public void self_isFollowable() throws Exception {
        // given
        final DomainObjectRepresentation repr = givenRepresentation("simples");

        // when, then
        assertThat(repr, isFollowableLinkToSelf(client));
    }

    @Test
    public void links() throws Exception {
        // given, when
        final DomainObjectRepresentation repr = givenRepresentation("simples");

        // then
        assertThat(repr.getLinks().size(), is(3));
    }

    private DomainObjectRepresentation givenRepresentation(final String serviceId) throws JsonParseException, JsonMappingException, IOException {
        final RestfulResponse<DomainObjectRepresentation> jsonResp = RestfulResponse.ofT(resource.service(serviceId));
        return jsonResp.getEntity();
    }

}
