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
package org.apache.isis.viewer.json.tck.resources.service.invoke;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.apache.isis.runtimes.dflt.webserver.WebServer;
import org.apache.isis.viewer.json.applib.HttpMethod;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RestfulClient;
import org.apache.isis.viewer.json.applib.RestfulRequest;
import org.apache.isis.viewer.json.applib.RestfulRequest.QueryParameter;
import org.apache.isis.viewer.json.applib.RestfulResponse;
import org.apache.isis.viewer.json.applib.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.json.applib.blocks.LinkRepresentation;
import org.apache.isis.viewer.json.applib.domainobjects.DomainObjectRepresentation;
import org.apache.isis.viewer.json.applib.domainobjects.DomainServiceResource;
import org.apache.isis.viewer.json.applib.domainobjects.ListRepresentation;
import org.apache.isis.viewer.json.applib.domainobjects.ObjectActionRepresentation;
import org.apache.isis.viewer.json.applib.domainobjects.ScalarValueRepresentation;
import org.apache.isis.viewer.json.tck.IsisWebServerRule;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;


public class DomainServiceResourceTest_invokeAction {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();
    
    private RestfulClient client;
    
    @SuppressWarnings("unused")
    private DomainServiceResource resource;

    @Before
    public void setUp() throws Exception {
        WebServer webServer = webServerRule.getWebServer();
        client = new RestfulClient(webServer.getBase());
        
        resource = client.getDomainServiceResource();
    }


    @Test
    public void invokeQueryOnly_noArg_usingClientFollow() throws Exception {

        // given
        JsonRepresentation givenAction = givenAction("simples", "list");
        final ObjectActionRepresentation actionRepr = givenAction.as(ObjectActionRepresentation.class);
        
        // when
        final LinkRepresentation invokeLink = actionRepr.getInvoke();
        
        // then
        assertThat(invokeLink, is(not(nullValue())));
        final Response response = client.follow(invokeLink);
        RestfulResponse<ListRepresentation> restfulResponse = RestfulResponse.ofT(response);
        final ListRepresentation listRepr = restfulResponse.getEntity();
        
        assertThat(listRepr.getValues().size(), is(5));
    }

    @Test
    public void invokeIdempotent_withArgs_usingClientFollow() throws Exception {

        // given action
        JsonRepresentation givenAction = givenAction("simples", "newPersistentEntity");
        final ObjectActionRepresentation actionRepr = givenAction.as(ObjectActionRepresentation.class);
        
        // when
        final LinkRepresentation invokeLink = actionRepr.getInvoke();
        
        // then
        assertThat(invokeLink, is(not(nullValue())));
        
        final JsonRepresentation args = invokeLink.getArguments();
        assertThat(args.size(), is(2));
        assertThat(args.mapHas("name"), is(true));
        assertThat(args.mapHas("flag"), is(true));
        
        // when
        args.mapPut("name", "New Name");
        args.mapPut("flag", true);
        final Response response = client.follow(invokeLink, args);
        
        // then
        RestfulResponse<DomainObjectRepresentation> restfulResponse = RestfulResponse.ofT(response);
        final DomainObjectRepresentation objectRepr = restfulResponse.getEntity();
        
        assertThat(objectRepr.getProperty("name").getString("value"), is("New Name"));
        assertThat(objectRepr.getProperty("flag").getBoolean("value"), is(true));
    }

    @Test
    public void invoke_returningScalar_withReferenceArgs_usingClientFollow() throws Exception {
        
        // given action
        JsonRepresentation givenAction = givenAction("simples", "count");
        final ObjectActionRepresentation actionRepr = givenAction.as(ObjectActionRepresentation.class);
        
        // when
        final LinkRepresentation invokeLink = actionRepr.getInvoke();
        
        // then
        assertThat(invokeLink, is(not(nullValue())));
        final JsonRepresentation args = invokeLink.getArguments();
        assertThat(args.size(), is(0));
        
        // when
        final Response response = client.follow(invokeLink, args);
        
        // then
        RestfulResponse<ScalarValueRepresentation> restfulResponse = RestfulResponse.ofT(response);
        final ScalarValueRepresentation objectRepr = restfulResponse.getEntity();
        
        assertThat(objectRepr.getValue().asInt(), is(6));
    }


    @Test
    public void invokeNonIdempotent_returningVoid_withReferenceArgs_usingClientFollow() throws Exception {

        // given simple entity with 'flag' property set to true
        final LinkRepresentation linkToSimpleEntity = givenLinkToSimpleEntity(0);
        final Response responseBefore = client.follow(linkToSimpleEntity);
        final RestfulResponse<DomainObjectRepresentation> restfulResponseBefore = RestfulResponse.ofT(responseBefore);
        final DomainObjectRepresentation simpleEntityBefore = restfulResponseBefore.getEntity();
        assertThat(simpleEntityBefore.getProperty("flag").getBoolean("value"), is(true));

        // and given 'toggle' action on repo
        JsonRepresentation givenAction = givenAction("simples", "toggle");
        final ObjectActionRepresentation actionRepr = givenAction.as(ObjectActionRepresentation.class);
        
        // when
        final LinkRepresentation invokeLink = actionRepr.getInvoke();
        
        // then
        assertThat(invokeLink, is(not(nullValue())));
        
        final JsonRepresentation args = invokeLink.getArguments();
        assertThat(args.size(), is(1));
        assertThat(args.mapHas("object"), is(true));
        
        // when
        args.mapPut("object", linkToSimpleEntity);
        final Response response = client.follow(invokeLink, args);
        
        // then
        RestfulResponse<JsonRepresentation> restfulResponse = RestfulResponse.ofT(response);
        assertThat(restfulResponse.getStatus(), is(HttpStatusCode.NO_CONTENT));

        // and then simple entity 'flag' property set to false
        final Response responseAfter = client.follow(linkToSimpleEntity);
        final RestfulResponse<DomainObjectRepresentation> restfulResponseAfter = RestfulResponse.ofT(responseAfter);
        final DomainObjectRepresentation simpleEntityAfter = restfulResponseAfter.getEntity();
        assertThat(simpleEntityAfter.getProperty("flag").getBoolean("value"), is(false));
    }


    
    @org.junit.Ignore("up to here")
    @Test
    public void invoke_withAllBuiltInArgs_usingClientFollow() throws Exception {

        // given
        JsonRepresentation givenAction = givenAction("simples", "newTransientEntity");
        final ObjectActionRepresentation actionRepr = givenAction.as(ObjectActionRepresentation.class);
        
        // when
        final LinkRepresentation invokeLink = actionRepr.getInvoke();
        
        // then
        assertThat(invokeLink, is(not(nullValue())));
        
        final JsonRepresentation args = invokeLink.getArguments();
        assertThat(args.size(), is(0));
        
        // when
        args.mapPut("name", "New Name");
        args.mapPut("flag", true);
        final Response response = client.follow(invokeLink, args);
        
        // then
        RestfulResponse<DomainObjectRepresentation> restfulResponse = RestfulResponse.ofT(response);
        final DomainObjectRepresentation objectRepr = restfulResponse.getEntity();
        
        assertThat(objectRepr.getRepresentation("members[propertyId=%s].value", "name").asString(), is("New Name"));
        assertThat(objectRepr.getRepresentation("members[propertyId=%s].value", "flag").asBoolean(), is(true));
    }


    private JsonRepresentation givenAction(final String serviceId, final String actionId) throws JsonParseException, JsonMappingException, IOException {
        final String href = givenHrefToService(serviceId);
        
        final RestfulRequest request = 
                client.createRequest(HttpMethod.GET, href).withArg(QueryParameter.FOLLOW_LINKS, "members[id=%s].details", actionId);
        final RestfulResponse<DomainObjectRepresentation> restfulResponse = request.executeT();

        assertThat(restfulResponse.getStatus(), is(HttpStatusCode.OK));
        final DomainObjectRepresentation repr = restfulResponse.getEntity();
        
        JsonRepresentation actionLinkRepr = repr.getAction(actionId);
        return actionLinkRepr.getRepresentation("details.value");
    }


    private String givenHrefToService(String serviceId) throws JsonParseException, JsonMappingException, IOException {
        final DomainServiceResource resource = client.getDomainServiceResource();
        final Response response = resource.services();
        final ListRepresentation services = RestfulResponse.<ListRepresentation>ofT(response).getEntity();

        return services.getRepresentation("values[key=%s]", serviceId).asLink().getHref();
    }

    private LinkRepresentation givenLinkToSimpleEntity(int num) throws JsonParseException, JsonMappingException, IOException, Exception {
        // given
        JsonRepresentation givenAction = givenAction("simples", "list");
        final ObjectActionRepresentation actionRepr = givenAction.as(ObjectActionRepresentation.class);
        
        // when
        final LinkRepresentation invokeLink = actionRepr.getInvoke();
        
        // then
        final Response response = client.follow(invokeLink);
        RestfulResponse<ListRepresentation> restfulResponse = RestfulResponse.ofT(response);
        final ListRepresentation listRepr = restfulResponse.getEntity();
        
        return listRepr.getValues().arrayGet(num).asLink();
    }


}
    