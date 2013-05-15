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

import static org.apache.isis.core.commons.matchers.IsisMatchers.matches;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.webserver.WebServer;
import org.apache.isis.viewer.restfulobjects.applib.LinkRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RestfulHttpMethod;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectResource;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ObjectActionRepresentation;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;

public class Get_thenRepresentation_ok_TODO {

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

    @Ignore("to get working again")
    @Test
    public void actionPrompt() throws Exception {

        // when
        final Response actionPromptResp = domainObjectResource.actionPrompt("OID","1", "list");
        final RestfulResponse<ObjectActionRepresentation> actionPromptJsonResp = RestfulResponse.ofT(actionPromptResp);
        assertThat(actionPromptJsonResp.getStatus().getFamily(), is(Family.SUCCESSFUL));

        // then
        final ObjectActionRepresentation actionPromptRepr = actionPromptJsonResp.getEntity();

        // _self.link
        final LinkRepresentation selfLink = actionPromptRepr.getLink("_self.link");
        assertThat(selfLink.getRel(), is("member"));
        assertThat(selfLink.getHref(), matches(".+objects/OID:1/actions/list"));
        assertThat(selfLink.getHttpMethod(), is(RestfulHttpMethod.GET));

        // _self.object
        final LinkRepresentation selfObject = actionPromptRepr.getLink("_self.object");
        assertThat(selfObject.getRel(), is("object"));
        assertThat(selfObject.getHref(), matches(".+objects/OID:1"));
        assertThat(selfObject.getHttpMethod(), is(RestfulHttpMethod.GET));

        // type
        final LinkRepresentation type = actionPromptRepr.getLink("type");
        assertThat(type.getRel(), is("type"));
        assertThat(type.getHref(), matches(".+vnd\\.list\\+json"));
        assertThat(type.getHttpMethod(), is(RestfulHttpMethod.GET));

        assertThat(actionPromptRepr.getString("memberType"), is("action"));
        assertThat(actionPromptRepr.getString("actionType"), is("USER"));
        assertThat(actionPromptRepr.getInt("numParameters"), is(0));
        assertThat(actionPromptRepr.getArray("parameters").size(), is(0));

        final LinkRepresentation invokeLink = actionPromptRepr.getLink("invoke");
        assertThat(invokeLink.getRel(), is("invoke"));
        assertThat(invokeLink.getHref(), matches(".+objects/OID:1/actions/list/invoke"));
        assertThat(invokeLink.getHttpMethod(), is(RestfulHttpMethod.POST));
        assertThat(invokeLink.getArguments(), is(not(nullValue())));
        assertThat(invokeLink.getArguments().isArray(), is(true));
        assertThat(invokeLink.getArguments().size(), is(0));
    }


}
