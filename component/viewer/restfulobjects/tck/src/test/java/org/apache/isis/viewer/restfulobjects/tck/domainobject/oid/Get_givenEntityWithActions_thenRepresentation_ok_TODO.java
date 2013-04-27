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

import static org.apache.isis.core.commons.matchers.IsisMatchers.matches;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.tck.dom.scalars.ApplibValuedEntity;
import org.apache.isis.core.webserver.WebServer;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.LinkRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RestfulHttpMethod;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectResource;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;

public class Get_givenEntityWithActions_thenRepresentation_ok_TODO {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();

    protected RestfulClient client;

    private DomainObjectResource domainObjectResource;
    private DomainObjectRepresentation domainObjectRepr;

    @Before
    public void setUp() throws Exception {
        final WebServer webServer = webServerRule.getWebServer();
        client = new RestfulClient(webServer.getBase());
        domainObjectResource = client.getDomainObjectResource();
        
    }

    @Ignore("TODO")
    @Test
    public void thenMembers() throws Exception {

        // when
        final Response jaxrsResponse = domainObjectResource.object("PRMV","29");
        final RestfulResponse<DomainObjectRepresentation> restfulResponse = RestfulResponse.ofT(jaxrsResponse);
        assertThat(restfulResponse.getStatus(), is(HttpStatusCode.OK));

        // then
        domainObjectRepr = restfulResponse.getEntity();
        assertThat(domainObjectRepr, is(not(nullValue())));


        
        
        final LinkRepresentation self = domainObjectRepr.getSelf();

        // then actions
        final JsonRepresentation actions = domainObjectRepr.getActions();
        assertThat(actions.size(), is(2));

        final JsonRepresentation listAction = actions.getRepresentation("list");
        assertThat(listAction.getString("memberType"), is("action"));
        assertThat(listAction.getString("actionId"), is("list"));
        assertThat(listAction.getString("actionType"), is("USER"));
        assertThat(listAction.getInt("numParameters"), is(0));

        final LinkRepresentation listActionType = listAction.getLink("type");
        assertThat(listActionType.getRel(), is("type"));
        assertThat(listActionType.getHref(), matches(".+vnd\\.list\\+json"));
        assertThat(listActionType.getHttpMethod(), is(RestfulHttpMethod.GET));

        final LinkRepresentation listActionDetails = listAction.getLink("details");
        assertThat(listActionDetails.getRel(), is("action"));
        assertThat(listActionDetails.getHref(), is(self.getHref() + "/actions/list"));
        assertThat(listActionDetails.getHttpMethod(), is(RestfulHttpMethod.GET));

        final JsonRepresentation newEntityAction = actions.getRepresentation("newEntity");
        assertThat(newEntityAction.getString("memberType"), is("action"));
        assertThat(newEntityAction.getString("actionType"), is("USER"));
        assertThat(newEntityAction.getInt("numParameters"), is(0));

        final LinkRepresentation newEntityActionType = newEntityAction.getLink("type");
        assertThat(newEntityActionType.getRel(), is("type"));
        assertThat(newEntityActionType.getHref(), matches(".+vnd\\." + ApplibValuedEntity.class.getName() + "\\+json"));
        assertThat(newEntityActionType.getHttpMethod(), is(RestfulHttpMethod.GET));

        final LinkRepresentation newEntityActionDetails = newEntityAction.getLink("details");
        assertThat(newEntityActionDetails.getRel(), is("action"));
        assertThat(newEntityActionDetails.getHref(), is(self.getHref() + "/actions/newEntity"));
        assertThat(newEntityActionDetails.getHttpMethod(), is(RestfulHttpMethod.GET));
    }
    
}
