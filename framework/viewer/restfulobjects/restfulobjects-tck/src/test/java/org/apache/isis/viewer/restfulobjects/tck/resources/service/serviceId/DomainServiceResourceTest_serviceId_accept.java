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
package org.apache.isis.viewer.restfulobjects.tck.resources.service.serviceId;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.runtimes.dflt.webserver.WebServer;
import org.apache.isis.viewer.restfulobjects.applib.HttpMethod;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.RestfulRequest;
import org.apache.isis.viewer.restfulobjects.applib.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.domainobjects.DomainObjectRepresentation;
import org.apache.isis.viewer.restfulobjects.domainobjects.DomainServiceResource;
import org.apache.isis.viewer.restfulobjects.domainobjects.ListRepresentation;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;

public class DomainServiceResourceTest_serviceId_accept {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();

    private RestfulClient client;

    @Before
    public void setUp() throws Exception {
        final WebServer webServer = webServerRule.getWebServer();
        client = new RestfulClient(webServer.getBase());
    }

    @Test
    public void applicationJson() throws Exception {

        final String href = givenLinkToService();

        final RestfulRequest request = client.createRequest(HttpMethod.GET, href).withHeader(RestfulRequest.Header.ACCEPT, MediaType.APPLICATION_JSON_TYPE);
        final RestfulResponse<DomainObjectRepresentation> restfulResponse = request.executeT();

        assertThat(restfulResponse.getStatus(), is(HttpStatusCode.OK));
    }

    @Test
    public void applicationJson_profileList() throws Exception {

        final String href = givenLinkToService();

        final RestfulRequest request = client.createRequest(HttpMethod.GET, href).withHeader(RestfulRequest.Header.ACCEPT, RepresentationType.DOMAIN_OBJECT.getMediaType());
        final RestfulResponse<DomainObjectRepresentation> restfulResponse = request.executeT();

        assertThat(restfulResponse.getStatus(), is(HttpStatusCode.OK));
    }

    @Test
    public void applicationJson_invalid() throws Exception {

        final String href = givenLinkToService();

        final RestfulRequest request = client.createRequest(HttpMethod.GET, href).withHeader(RestfulRequest.Header.ACCEPT, RepresentationType.USER.getMediaType());
        final RestfulResponse<DomainObjectRepresentation> restfulResponse = request.executeT();

        assertThat(restfulResponse.getStatus(), is(HttpStatusCode.NOT_ACCEPTABLE));
    }

    private String givenLinkToService() throws JsonParseException, JsonMappingException, IOException {
        final DomainServiceResource resource = client.getDomainServiceResource();
        final Response response = resource.services();
        final ListRepresentation services = RestfulResponse.<ListRepresentation> ofT(response).getEntity();

        final String href = services.getRepresentation("values[id=%s]", "simples").asLink().getHref();
        return href;
    }

}
