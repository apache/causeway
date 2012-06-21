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
package org.apache.isis.viewer.json.tck.resources;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.runtimes.dflt.webserver.WebServer;
import org.apache.isis.viewer.json.applib.HttpMethod;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.applib.RestfulClient;
import org.apache.isis.viewer.json.applib.RestfulRequest;
import org.apache.isis.viewer.json.applib.RestfulRequest.Header;
import org.apache.isis.viewer.json.applib.RestfulResponse;
import org.apache.isis.viewer.json.applib.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.json.applib.homepage.HomePageRepresentation;
import org.apache.isis.viewer.json.applib.util.Parser;
import org.apache.isis.viewer.json.tck.IsisWebServerRule;

public class AnyResourceTest_clientAcceptHeader_exceptionHandling {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();

    private RestfulClient client;

    @Before
    public void setUp() throws Exception {
        final WebServer webServer = webServerRule.getWebServer();
        client = new RestfulClient(webServer.getBase());
    }

    @Test
    public void whenSetsNoAcceptHeader_isOk() throws Exception {
        // given
        final RestfulRequest restfulReq = client.createRequest(HttpMethod.GET, "/");

        // when
        final RestfulResponse<HomePageRepresentation> restfulResp = restfulReq.executeT();

        // then
        assertThat(restfulResp.getStatus(), is(HttpStatusCode.OK));
        assertThat(restfulResp.getHeader(RestfulResponse.Header.CONTENT_TYPE), is(RepresentationType.HOME_PAGE.getMediaType()));
    }

    @Test
    public void whenSetsAcceptHeaderOfApplicationJson_isOk() throws Exception {

        // given
        final RestfulRequest restfulReq = client.createRequest(HttpMethod.GET, "/");
        restfulReq.withHeader(Header.ACCEPT, MediaType.APPLICATION_JSON_TYPE);

        // when
        final RestfulResponse<HomePageRepresentation> restfulResp = restfulReq.executeT();

        // then
        assertThat(restfulResp.getStatus(), is(HttpStatusCode.OK));
        assertThat(restfulResp.getHeader(RestfulResponse.Header.CONTENT_TYPE), is(RepresentationType.HOME_PAGE.getMediaType()));
    }

    @Ignore("RestEasy seems to reject with a 500, 'No match for accept header', rather than a 405.")
    @Test
    public void whenSetsIncorrectMediaType_returnsNotAcceptable() throws Exception {

        // given
        final ClientRequest clientRequest = client.getClientRequestFactory().createRelativeRequest("/");
        clientRequest.accept(MediaType.APPLICATION_ATOM_XML_TYPE);

        // when
        final ClientResponse<?> resp = clientRequest.get();
        final RestfulResponse<JsonRepresentation> restfulResp = RestfulResponse.of(resp);
        
        final String entity = restfulResp.getEntity().toString();

        // then
        assertThat(restfulResp.getStatus(), is(HttpStatusCode.NOT_ACCEPTABLE));
    }

}
