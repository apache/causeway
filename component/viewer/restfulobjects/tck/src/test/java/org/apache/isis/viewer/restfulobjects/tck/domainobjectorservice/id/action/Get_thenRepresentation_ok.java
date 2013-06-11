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

import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.assertThat;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.isLink;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.webserver.WebServer;
import org.apache.isis.viewer.restfulobjects.applib.LinkRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulHttpMethod;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectResource;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ObjectActionRepresentation;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;

public class Get_thenRepresentation_ok {

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

        // when
        final Response actionPromptResp = domainObjectResource.actionPrompt("RTNE", "67", "contains");
        final RestfulResponse<ObjectActionRepresentation> actionPromptJsonResp = RestfulResponse.ofT(actionPromptResp);
        assertThat(actionPromptJsonResp.getStatus().getFamily(), is(Family.SUCCESSFUL));

        // then
        final ObjectActionRepresentation actionPromptRepr = actionPromptJsonResp.getEntity();

        assertThat(actionPromptRepr.getString("memberType"), is("action"));

        // self link
        final LinkRepresentation selfLink = actionPromptRepr.getLinkWithRel(Rel.SELF);
        assertThat(selfLink, isLink(client)
                                .httpMethod(RestfulHttpMethod.GET)
                                .href(endsWith("/objects/RTNE/67/actions/contains"))
                                .returning(HttpStatusCode.OK));

        // up link
        final LinkRepresentation upLink = actionPromptRepr.getLinkWithRel(Rel.UP);
        assertThat(upLink, isLink(client)
                                .httpMethod(RestfulHttpMethod.GET)
                                .href(endsWith("/objects/RTNE/67"))
                                .returning(HttpStatusCode.OK)
                                .type(RepresentationType.DOMAIN_OBJECT.getMediaType())
                                .title("Untitled Actions Entity"));

        //invoke link
        final LinkRepresentation invokeLink = actionPromptRepr.getLinkWithRel(Rel.INVOKE);
        assertThat(invokeLink, isLink(client)
                                .httpMethod(RestfulHttpMethod.GET)
                                .href(endsWith("/services/ActionsEntities/actions/contains/invoke")));

        assertThat(invokeLink.getArguments(), is(not(nullValue())));
        assertThat(invokeLink.getArguments().isArray(), is(false));
        assertThat(invokeLink.getArguments().size(), is(3));

     // described by link
        final LinkRepresentation describedByLink = actionPromptRepr.getLinkWithRel(Rel.DESCRIBEDBY);
        assertThat(describedByLink, isLink(client)
                                .returning(HttpStatusCode.OK)
                                .responseEntityWithSelfHref(describedByLink.getHref()));

        assertThat(actionPromptRepr.getExtensions().getString("actionType"), is("user"));
        assertThat(actionPromptRepr.getExtensions().getString("actionSemantics"), is("safe"));
        assertThat(actionPromptRepr.getArray("parameters").size(), is(3));
    }
}
