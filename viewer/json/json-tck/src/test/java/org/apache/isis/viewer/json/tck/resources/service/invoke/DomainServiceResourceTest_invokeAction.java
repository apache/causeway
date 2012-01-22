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

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.runtimes.dflt.webserver.WebServer;
import org.apache.isis.viewer.json.applib.HttpMethod;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RestfulClient;
import org.apache.isis.viewer.json.applib.RestfulRequest;
import org.apache.isis.viewer.json.applib.RestfulRequest.RequestParameter;
import org.apache.isis.viewer.json.applib.RestfulResponse;
import org.apache.isis.viewer.json.applib.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.json.applib.domainobjects.DomainObjectRepresentation;
import org.apache.isis.viewer.json.applib.domainobjects.DomainServiceResource;
import org.apache.isis.viewer.json.applib.domainobjects.ListRepresentation;
import org.apache.isis.viewer.json.applib.domainobjects.ObjectActionRepresentation;
import org.apache.isis.viewer.json.applib.domainobjects.ScalarValueRepresentation;
import org.apache.isis.viewer.json.applib.links.LinkRepresentation;
import org.apache.isis.viewer.json.tck.IsisWebServerRule;

public class DomainServiceResourceTest_invokeAction {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();

    private RestfulClient client;

    @SuppressWarnings("unused")
    private DomainServiceResource resource;

    @Before
    public void setUp() throws Exception {
        final WebServer webServer = webServerRule.getWebServer();
        client = new RestfulClient(webServer.getBase());

        resource = client.getDomainServiceResource();
    }

    @Ignore("TODO - fix broken test resulting from introduction of actionresult repr")
    @Test
    public void invokeQueryOnly_noArg_usingClientFollow() throws Exception {

        // given
        final JsonRepresentation givenAction = givenAction("simples", "list");
        final ObjectActionRepresentation actionRepr = givenAction.as(ObjectActionRepresentation.class);

        // when
        final LinkRepresentation invokeLink = actionRepr.getInvoke();

        // then
        assertThat(invokeLink, is(not(nullValue())));
        final RestfulResponse<ListRepresentation> restfulResponse = client.followT(invokeLink);
        final ListRepresentation listRepr = restfulResponse.getEntity();

        assertThat(listRepr.getValues().size(), is(5));
    }

    @Ignore("TODO - fix broken test resulting from introduction of actionresult repr")
    @Test
    public void invokeIdempotent_withArgs_usingClientFollow() throws Exception {

        // given action
        final JsonRepresentation givenAction = givenAction("simples", "newPersistentEntity");
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
        final RestfulResponse<DomainObjectRepresentation> restfulResponse = client.followT(invokeLink, args);

        // then
        final DomainObjectRepresentation objectRepr = restfulResponse.getEntity();

        assertThat(objectRepr.getProperty("name").getString("value"), is("New Name"));
        assertThat(objectRepr.getProperty("flag").getBoolean("value"), is(true));
    }

    @Ignore("TODO - fix broken test resulting from introduction of actionresult repr")
    @Test
    public void invoke_returningScalar_withReferenceArgs_usingClientFollow() throws Exception {

        // given action
        final JsonRepresentation givenAction = givenAction("simples", "count");
        final ObjectActionRepresentation actionRepr = givenAction.as(ObjectActionRepresentation.class);

        // when
        final LinkRepresentation invokeLink = actionRepr.getInvoke();

        // then
        assertThat(invokeLink, is(not(nullValue())));
        final JsonRepresentation args = invokeLink.getArguments();
        assertThat(args.size(), is(0));

        // when
        final RestfulResponse<ScalarValueRepresentation> restfulResponse = client.followT(invokeLink, args);

        // then
        final ScalarValueRepresentation objectRepr = restfulResponse.getEntity();

        assertThat(objectRepr.getValue().asInt(), is(6));
    }

    @Ignore("TODO - fix broken test resulting from introduction of actionresult repr")
    @Test
    public void invokeNonIdempotent_returningVoid_withReferenceArgs_usingClientFollow() throws Exception {

        // given simple entity with 'flag' property set to true
        final LinkRepresentation linkToSimpleEntity = givenLinkToSimpleEntity(0);
        final RestfulResponse<DomainObjectRepresentation> restfulResponseBefore = client.followT(linkToSimpleEntity);
        final DomainObjectRepresentation simpleEntityBefore = restfulResponseBefore.getEntity();
        final Boolean before = simpleEntityBefore.getProperty("flag").getBoolean("value");

        // and given 'toggle' action on repo
        final JsonRepresentation givenAction = givenAction("simples", "toggle");
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
        final RestfulResponse<JsonRepresentation> restfulResponse = client.followT(invokeLink, args);

        // then
        assertThat(restfulResponse.getStatus(), is(HttpStatusCode.NO_CONTENT));

        // and then simple entity 'flag' property set to false
        final RestfulResponse<DomainObjectRepresentation> restfulResponseAfter = client.followT(linkToSimpleEntity);
        final DomainObjectRepresentation simpleEntityAfter = restfulResponseAfter.getEntity();

        final Boolean after = simpleEntityAfter.getProperty("flag").getBoolean("value");
        assertThat(after, is(!before)); // ie has been toggled
    }

    @org.junit.Ignore("up to here")
    @Test
    public void invoke_withAllBuiltInArgs_usingClientFollow() throws Exception {

        // given simple entity with 'flag' property set to true
        final LinkRepresentation linkToSimpleEntity = givenLinkToSimpleEntity(0);

        // given
        final JsonRepresentation givenAction = givenAction("simples", "update");
        final ObjectActionRepresentation actionRepr = givenAction.as(ObjectActionRepresentation.class);

        // when
        final LinkRepresentation invokeLink = actionRepr.getInvoke();

        // then
        assertThat(invokeLink, is(not(nullValue())));

        final JsonRepresentation args = invokeLink.getArguments();
        assertThat(args.size(), is(0));
        assertThat(args.mapHas("object"), is(true));
        assertThat(args.mapHas("name"), is(true));
        assertThat(args.mapHas("flag"), is(true));
        assertThat(args.mapHas("Boolean"), is(true));
        assertThat(args.mapHas("int"), is(true));
        assertThat(args.mapHas("integer"), is(true));
        assertThat(args.mapHas("long1"), is(true));
        assertThat(args.mapHas("long2"), is(true));
        assertThat(args.mapHas("double1"), is(true));
        assertThat(args.mapHas("double2"), is(true));
        assertThat(args.mapHas("bigInteger"), is(true));
        assertThat(args.mapHas("bigDecimal"), is(true));

        // when
        args.mapPut("name", "New Name");
        args.mapPut("flag", true);
        final RestfulResponse<DomainObjectRepresentation> restfulResponse = client.followT(invokeLink, args);

        // then
        final DomainObjectRepresentation objectRepr = restfulResponse.getEntity();

        assertThat(objectRepr.getRepresentation("members[propertyId=%s].value", "name").asString(), is("New Name"));
        assertThat(objectRepr.getRepresentation("members[propertyId=%s].value", "flag").asBoolean(), is(true));
    }

    private JsonRepresentation givenAction(final String serviceId, final String actionId) throws JsonParseException, JsonMappingException, IOException {
        final String href = givenHrefToService(serviceId);

        final RestfulRequest request = client.createRequest(HttpMethod.GET, href).withArg(RequestParameter.FOLLOW_LINKS, "members[id=%s].links[rel=details]", actionId);
        final RestfulResponse<DomainObjectRepresentation> restfulResponse = request.executeT();

        assertThat(restfulResponse.getStatus(), is(HttpStatusCode.OK));
        final DomainObjectRepresentation repr = restfulResponse.getEntity();

        final JsonRepresentation actionLinkRepr = repr.getAction(actionId);
        return actionLinkRepr.getRepresentation("links[rel=details].value");
    }

    private String givenHrefToService(final String serviceId) throws JsonParseException, JsonMappingException, IOException {
        final DomainServiceResource resource = client.getDomainServiceResource();
        final Response response = resource.services();
        final ListRepresentation services = RestfulResponse.<ListRepresentation> ofT(response).getEntity();

        return services.getRepresentation("values[id=%s]", serviceId).asLink().getHref();
    }

    private LinkRepresentation givenLinkToSimpleEntity(final int num) throws JsonParseException, JsonMappingException, IOException, Exception {
        // given
        final JsonRepresentation givenAction = givenAction("simples", "list");
        final ObjectActionRepresentation actionRepr = givenAction.as(ObjectActionRepresentation.class);

        // when
        final LinkRepresentation invokeLink = actionRepr.getInvoke();

        // then
        final RestfulResponse<ListRepresentation> restfulResponse = client.followT(invokeLink);
        final ListRepresentation listRepr = restfulResponse.getEntity();

        return listRepr.getValues().arrayGet(num).asLink();
    }

}
