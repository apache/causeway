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
package org.apache.isis.viewer.restfulobjects.tck.domainobject.oid;

import javax.ws.rs.core.MediaType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulHttpMethod;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulRequest;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.version.VersionRepresentation;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class Get_whenRequestHeaders_Accept_ok {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();

    private RestfulClient client;

    private RestfulRequest request;

    @Before
    public void setUp() throws Exception {
        client = webServerRule.getClient();
        request = client.createRequest(RestfulHttpMethod.GET, "objects/BSRL/74");
    }

    @Test
    public void applicationJson_noProfile_returns200() throws Exception {

        request.withHeader(RestfulRequest.Header.ACCEPT, MediaType.APPLICATION_JSON_TYPE);
        final RestfulResponse<VersionRepresentation> restfulResponse = request.executeT();

        assertThat(restfulResponse.getStatus(), is(RestfulResponse.HttpStatusCode.OK));
    }


    @Test
    public void applicationJson_profileVersion_returns200() throws Exception {

        request.withHeader(RestfulRequest.Header.ACCEPT, RepresentationType.DOMAIN_OBJECT.getMediaType());
        final RestfulResponse<VersionRepresentation> restfulResponse = request.executeT();

        assertThat(restfulResponse.getStatus(), is(RestfulResponse.HttpStatusCode.OK));
    }

    @Test
    public void missingHeader_returns200() throws Exception {

        final RestfulResponse<VersionRepresentation> restfulResp = request.executeT();

        assertThat(restfulResp.getStatus(), is(RestfulResponse.HttpStatusCode.OK));
    }

}
