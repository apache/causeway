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

import java.io.IOException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.core.webserver.WebServer;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectResource;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class Get_givenDisabledMembers_thenRepresentation_ok {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();

    protected RestfulClient client;

    private DomainObjectResource domainObjectResource;

    @Before
    public void setUp() throws Exception {
        final WebServer webServer = webServerRule.getWebServer();
        client = new RestfulClient(webServer.getBase());
        domainObjectResource = client.getDomainObjectResource();
        
    }

    @Test
    public void domainObjectWithDisabledMembers() throws Exception {

        // given, when
        final DomainObjectRepresentation domainObjectRepr = givenDomainObjectRepresentationFor("BSRL","75");

        // property ('visibleButNotEditableProperty')
        final JsonRepresentation properties = domainObjectRepr.getProperties();
        final JsonRepresentation nameProperty = properties.getRepresentation("visibleButNotEditableProperty");
        assertThat(nameProperty.getString("disabledReason"), is("Always disabled"));
    }


    private DomainObjectRepresentation givenDomainObjectRepresentationFor(final String domainType, String instanceId) throws IOException {
        final DomainObjectResource domainObjectResource = client.getDomainObjectResource();

        final Response domainObjectResp = domainObjectResource.object(domainType, instanceId);
        final RestfulResponse<DomainObjectRepresentation> domainObjectJsonResp = RestfulResponse.ofT(domainObjectResp);
        assertThat(domainObjectJsonResp.getStatus().getFamily(), is(Family.SUCCESSFUL));

        return domainObjectJsonResp.getEntity();
    }

    
    
}
