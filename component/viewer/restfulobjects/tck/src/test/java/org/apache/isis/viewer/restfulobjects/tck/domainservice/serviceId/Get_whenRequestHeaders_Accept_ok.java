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
package org.apache.isis.viewer.restfulobjects.tck.domainservice.serviceId;

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
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ListRepresentation;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;
import org.apache.isis.viewer.restfulobjects.tck.Util;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class Get_whenRequestHeaders_Accept_ok {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();

    private RestfulClient client;
    private RestfulRequest request;

    private String href;

    @Before
    public void setUp() throws Exception {
        client = webServerRule.getClient();
        href = Util.givenLinkToService(client, "JdkValuedEntities");
        request = client.createRequest(RestfulHttpMethod.GET, href);
    }

    @Test
    public void applicationJson_noProfile_returns200() throws Exception {

        request.withHeader(RestfulRequest.Header.ACCEPT, MediaType.APPLICATION_JSON_TYPE);
        final RestfulResponse<DomainObjectRepresentation> restfulResponse = request.executeT();

        assertThat(restfulResponse.getStatus(), is(HttpStatusCode.OK));
    }

    @Test
    public void applicationJson_profileDomainObject_returns200() throws Exception {

        request.withHeader(RestfulRequest.Header.ACCEPT, RepresentationType.DOMAIN_OBJECT.getMediaType());
        final RestfulResponse<DomainObjectRepresentation> restfulResponse = request.executeT();

        assertThat(restfulResponse.getStatus(), is(HttpStatusCode.OK));
    }

    @Test
    public void missingHeader_returns200() throws Exception {

        final RestfulResponse<DomainObjectRepresentation> restfulResp = request.executeT();

        assertThat(restfulResp.getStatus(), is(HttpStatusCode.OK));
    }

    @Test
    public void applicationJson_profileIncorrect_returns406() throws Exception {

        request.withHeader(RestfulRequest.Header.ACCEPT, RepresentationType.VERSION.getMediaType());
        final RestfulResponse<ListRepresentation> restfulResponse = request.executeT();

        assertThat(restfulResponse.getStatus(), is(HttpStatusCode.NOT_ACCEPTABLE));
    }

    @Test
    public void incorrectMediaType_returnsNotAcceptable() throws Exception {

        // given
        final ClientRequest clientRequest = client.getClientRequestFactory().createRequest(href);
        clientRequest.accept(MediaType.APPLICATION_ATOM_XML_TYPE);

        // when
        final ClientResponse<?> resp = clientRequest.get();
        final RestfulResponse<JsonRepresentation> restfulResp = RestfulResponse.of(resp);
        
        // then
        assertThat(restfulResp.getStatus(), is(HttpStatusCode.NOT_ACCEPTABLE));
    }


}
