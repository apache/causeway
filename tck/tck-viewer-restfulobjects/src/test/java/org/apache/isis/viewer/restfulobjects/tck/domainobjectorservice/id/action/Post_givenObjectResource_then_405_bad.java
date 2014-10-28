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
package org.apache.isis.viewer.restfulobjects.tck.domainobjectorservice.id.action;

import javax.ws.rs.core.Response;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.core.webserver.WebServer;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.LinkRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RestfulHttpMethod;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectResource;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ObjectActionRepresentation;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class Post_givenObjectResource_then_405_bad {

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
    public void representation() throws Exception {

        // given
        final Response actionPromptResp = domainObjectResource.actionPrompt("RTNE", "78", "contains");
        final RestfulResponse<ObjectActionRepresentation> actionPromptJsonResp = RestfulResponse.ofT(actionPromptResp);
        final ObjectActionRepresentation actionPromptRepr = actionPromptJsonResp.getEntity();
        final LinkRepresentation selfLink = actionPromptRepr.getLinkWithRel(Rel.SELF);
        final LinkRepresentation deleteLink = selfLink.withMethod(RestfulHttpMethod.POST);

        // when
        final RestfulResponse<JsonRepresentation> restfulResponse = client.follow(deleteLink);

        // then
        assertThat(restfulResponse.getStatus(), is(RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED));
        assertThat(restfulResponse.getHeader(RestfulResponse.Header.WARNING), is("Posting to an action prompt resource is not allowed."));
    }

}
