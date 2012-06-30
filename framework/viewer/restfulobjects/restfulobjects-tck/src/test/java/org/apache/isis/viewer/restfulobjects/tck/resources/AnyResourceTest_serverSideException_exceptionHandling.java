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
package org.apache.isis.viewer.restfulobjects.tck.resources;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.runtimes.dflt.webserver.WebServer;
import org.apache.isis.viewer.restfulobjects.applib.HttpMethod;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.RestfulRequest;
import org.apache.isis.viewer.restfulobjects.applib.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.RestfulRequest.Header;
import org.apache.isis.viewer.restfulobjects.applib.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.homepage.HomePageRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.util.Parser;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;

public class AnyResourceTest_serverSideException_exceptionHandling {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();

    private RestfulClient client;

    @Before
    public void setUp() throws Exception {
        final WebServer webServer = webServerRule.getWebServer();
        client = new RestfulClient(webServer.getBase());
    }

    @Test
    public void runtimeException_isMapped() throws Exception {

        // given
        final RestfulRequest restfulReq = client.createRequest(HttpMethod.GET, "version");
        final Header<Boolean> header = new Header<Boolean>("X-FAIL", Parser.forBoolean());
        restfulReq.withHeader(header, true);

        // when
        final RestfulResponse<JsonRepresentation> jsonResp = restfulReq.execute();

        // then
        assertThat(jsonResp.getStatus(), is(HttpStatusCode.METHOD_FAILURE));
    }
}
