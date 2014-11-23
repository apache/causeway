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
package org.apache.isis.viewer.restfulobjects.tck.domainobject.oid.property;

import java.io.IOException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.core.webserver.WebServer;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.LinkRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectResource;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ObjectPropertyRepresentation;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

// for ISIS-797
public class Put_whenArgValidContainsPlus_thenStringPropertyUpdated_ok_TODO {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();

    protected RestfulClient client;

    private DomainObjectResource domainObjectResource;

    private LinkRepresentation modifyLink;
    private JsonRepresentation argRepr;

    @Before
    public void setUp() throws Exception {
        final WebServer webServer = webServerRule.getWebServer();
        client = new RestfulClient(webServer.getBase());
        domainObjectResource = client.getDomainObjectResource();
    }

    /**
     * Tests change state, so discard such that will be recreated by next test.
     */
    @After
    public void tearDown() throws Exception {
        webServerRule.discardWebApp();
    }


    @Test
    public void jdkPropertiesUpdated() throws Exception {

        // String
        final String s = "foo+bar";
        modifyLink = getObjectPropertyReprModifyLink("JDKV", "38", "stringProperty");
        argRepr = modifyLink.getArguments().mapPut("value", s);
        assertThat(followedRepr(modifyLink,argRepr).getString("value"), is(s));

    }


    private ObjectPropertyRepresentation getObjectPropertyRepr(final String domainType, final String instanceId, String propertyId) throws IOException {
        final Response domainObjectResp = domainObjectResource.propertyDetails(domainType, instanceId, propertyId);
        final RestfulResponse<ObjectPropertyRepresentation> domainObjectJsonResp = RestfulResponse.ofT(domainObjectResp);
        assertThat(domainObjectJsonResp.getStatus().getFamily(), is(Family.SUCCESSFUL));

        final ObjectPropertyRepresentation repr = domainObjectJsonResp.getEntity();
        return repr;
    }

    private LinkRepresentation getObjectPropertyReprModifyLink(String domainType, String instanceId, String propertyId) throws IOException {
        ObjectPropertyRepresentation objectPropertyRepr = getObjectPropertyRepr(domainType, instanceId, propertyId);
        return objectPropertyRepr.getLinkWithRel(Rel.MODIFY);
    }

    private JsonRepresentation followedRepr(LinkRepresentation modifyLink, JsonRepresentation argRepr) throws Exception {
        RestfulResponse<JsonRepresentation> result = client.follow(modifyLink, argRepr);
        assertThat(result.getStatus(), is(HttpStatusCode.OK));
        return result.getEntity().as(ObjectPropertyRepresentation.class);
    }

}
