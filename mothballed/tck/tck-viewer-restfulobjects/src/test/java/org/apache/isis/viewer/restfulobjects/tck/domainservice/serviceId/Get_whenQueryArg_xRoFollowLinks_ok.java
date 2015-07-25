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

import java.io.IOException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.core.webserver.WebServer;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RestfulHttpMethod;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulRequest;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulRequest.RequestParameter;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectRepresentation;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;
import org.apache.isis.viewer.restfulobjects.tck.Util;

import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.isMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class Get_whenQueryArg_xRoFollowLinks_ok {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();

    private RestfulClient client;

    @Before
    public void setUp() throws Exception {
        final WebServer webServer = webServerRule.getWebServer();
        client = new RestfulClient(webServer.getBase());
    }

    @Test
    public void self() throws Exception {
        final String href = givenHrefToService("WrapperValuedEntities");

        final RestfulRequest request = client.createRequest(RestfulHttpMethod.GET, href).withArg(RequestParameter.FOLLOW_LINKS, "links[rel=self]");
        final RestfulResponse<DomainObjectRepresentation> restfulResponse = request.executeT();

        assertThat(restfulResponse.getStatus(), is(HttpStatusCode.OK));
        final DomainObjectRepresentation repr = restfulResponse.getEntity();
        
        assertThat(repr.getSelf().getValue(), is(not(nullValue())));
    }

    @Test
    public void toDescribedBy() throws Exception {
        final String href = givenHrefToService("WrapperValuedEntities");

        final RestfulRequest request = client.createRequest(RestfulHttpMethod.GET, href).withArg(RequestParameter.FOLLOW_LINKS, "links[rel=describedby]");
        final RestfulResponse<DomainObjectRepresentation> restfulResponse = request.executeT();

        assertThat(restfulResponse.getStatus(), is(HttpStatusCode.OK));
        final DomainObjectRepresentation repr = restfulResponse.getEntity();
        
        assertThat(repr.getLinkWithRel(Rel.DESCRIBEDBY).getValue(), is(not(nullValue())));
    }

    @Test
    public void toMembersDetails() throws Exception {
        final String href = givenHrefToService("WrapperValuedEntities");

        final RestfulRequest request = client.createRequest(RestfulHttpMethod.GET, href).withArg(RequestParameter.FOLLOW_LINKS, "members.links[rel=%s]", Rel.DETAILS.getName());
        final RestfulResponse<DomainObjectRepresentation> restfulResponse = request.executeT();

        assertThat(restfulResponse.getStatus(), is(HttpStatusCode.OK));
        final DomainObjectRepresentation repr = restfulResponse.getEntity();

        final JsonRepresentation membersList = repr.getMembers();
        assertThat(membersList, isMap());

        JsonRepresentation actionRepr;

        actionRepr = membersList.getRepresentation("list");
        assertThat(actionRepr.getRepresentation("links[rel="+Rel.DETAILS.getName()+"]"), is(not(nullValue())));
        assertThat(actionRepr.getRepresentation("links[rel="+Rel.DETAILS.getName()+"].value"), is(not(nullValue()))); // followed

        actionRepr = membersList.getRepresentation("newEntity");
        assertThat(actionRepr.getRepresentation("links[rel="+Rel.DETAILS.getName()+"]"), is(not(nullValue())));
        assertThat(actionRepr.getRepresentation("links[rel="+Rel.DETAILS.getName()+"].value"), is(not(nullValue()))); // also followed
    }

    @Test
    public void singleMember_specified_by_criteria() throws Exception {

        final String href = givenHrefToService("WrapperValuedEntities");

        final RestfulRequest request = client.createRequest(RestfulHttpMethod.GET, href).withArg(RequestParameter.FOLLOW_LINKS, "members[%s].links[rel=%s]", "list", Rel.DETAILS.andParam("action", "list"));
        final RestfulResponse<DomainObjectRepresentation> restfulResponse = request.executeT();

        assertThat(restfulResponse.getStatus(), is(HttpStatusCode.OK));
        final DomainObjectRepresentation repr = restfulResponse.getEntity();

        final JsonRepresentation membersList = repr.getMembers();
        assertThat(membersList, isMap());

        JsonRepresentation actionRepr;

        actionRepr = membersList.getRepresentation("list");
        assertThat(actionRepr.getRepresentation("links[rel="+Rel.DETAILS.andParam("action", "list")+"]"), is(not(nullValue())));
        assertThat(actionRepr.getRepresentation("links[rel="+Rel.DETAILS.andParam("action", "list")+"].value"), is(not(nullValue()))); // followed

        actionRepr = membersList.getRepresentation("newEntity");
        assertThat(actionRepr.getRepresentation("links[rel="+Rel.DETAILS.andParam("action", "newEntity")+"]"), is(not(nullValue())));
        assertThat(actionRepr.getRepresentation("links[rel="+Rel.DETAILS.andParam("action", "newEntity")+"].value"), is(nullValue())); // not followed
    }

    @Test
    public void toMultipleMembersDetails() throws Exception {

        final String href = givenHrefToService("WrapperValuedEntities");

        final RestfulRequest request = client.createRequest(RestfulHttpMethod.GET, href).withArg(RequestParameter.FOLLOW_LINKS, "members[%s].links[rel=%s],members[%s].links[rel=%s]", "list", Rel.DETAILS.andParam("action", "list"), "newEntity", Rel.DETAILS.andParam("action", "newEntity"));
        final RestfulResponse<DomainObjectRepresentation> restfulResponse = request.executeT();

        assertThat(restfulResponse.getStatus(), is(HttpStatusCode.OK));
        final DomainObjectRepresentation repr = restfulResponse.getEntity();

        final JsonRepresentation membersList = repr.getMembers();
        assertThat(membersList, isMap());

        JsonRepresentation actionRepr;

        actionRepr = membersList.getRepresentation("list");
        assertThat(actionRepr.getRepresentation("links[rel="+Rel.DETAILS.andParam("action", "list")+"]"), is(not(nullValue())));
        assertThat(actionRepr.getRepresentation("links[rel="+Rel.DETAILS.andParam("action", "list")+"].value"), is(not(nullValue()))); // followed

        actionRepr = membersList.getRepresentation("newEntity");
        assertThat(actionRepr.getRepresentation("links[rel="+Rel.DETAILS.andParam("action", "newEntity")+"]"), is(not(nullValue())));
        assertThat(actionRepr.getRepresentation("links[rel="+Rel.DETAILS.andParam("action", "newEntity")+"].value"), is(not(nullValue()))); // also followed
    }
    
    private String givenHrefToService(final String serviceId) throws IOException {
        return Util.givenHrefToService(client, serviceId);
    }

}
