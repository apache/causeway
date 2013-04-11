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
package org.apache.isis.viewer.restfulobjects.tck.homepage.root;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.ws.rs.core.MediaType;

import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulHttpMethod;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulRequest;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.homepage.HomePageRepresentation;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class Get_thenRequestHeaders_Accept_ok {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();

    private RestfulClient client;

    private RestfulRequest request;

    @Before
    public void setUp() throws Exception {
        client = webServerRule.getClient();
        request = client.createRequest(RestfulHttpMethod.GET, "/");
    }

    @Test
    public void applicationJson_noProfile_returns200() throws Exception {

        request.withHeader(RestfulRequest.Header.ACCEPT, MediaType.APPLICATION_JSON_TYPE);
        final RestfulResponse<HomePageRepresentation> restfulResponse = request.executeT();

        assertThat(restfulResponse.getStatus(), is(HttpStatusCode.OK));
        assertThat(restfulResponse.getHeader(RestfulResponse.Header.CONTENT_TYPE), is(RepresentationType.HOME_PAGE.getMediaType()));
    }

    @Test
    public void applicationJson_profileHomePage_returns200() throws Exception {

        request.withHeader(RestfulRequest.Header.ACCEPT, RepresentationType.HOME_PAGE.getMediaType());
        final RestfulResponse<HomePageRepresentation> restfulResponse = request.executeT();

        assertThat(restfulResponse.getStatus(), is(HttpStatusCode.OK));
    }

    @Test
    public void missingHeader_returns200() throws Exception {

        final RestfulResponse<HomePageRepresentation> restfulResp = request.executeT();

        assertThat(restfulResp.getStatus(), is(HttpStatusCode.OK));
    }

    @Test
    public void applicationJson_profileIncorrect_returns406() throws Exception {

        request.withHeader(RestfulRequest.Header.ACCEPT, RepresentationType.USER.getMediaType());
        final RestfulResponse<HomePageRepresentation> restfulResponse = request.executeT();

        assertThat(restfulResponse.getStatus(), is(HttpStatusCode.NOT_ACCEPTABLE));
    }

    @Test
    public void incorrectMediaType_returnsNotAcceptable() throws Exception {

        // given
        final ClientRequest clientRequest = client.getClientRequestFactory().createRelativeRequest("/");
        clientRequest.accept(MediaType.APPLICATION_ATOM_XML_TYPE);

        // when
        final ClientResponse<?> resp = clientRequest.get();
        final RestfulResponse<JsonRepresentation> restfulResp = RestfulResponse.of(resp);
        
        // then
        assertThat(restfulResp.getStatus(), is(HttpStatusCode.NOT_ACCEPTABLE));
    }
}
