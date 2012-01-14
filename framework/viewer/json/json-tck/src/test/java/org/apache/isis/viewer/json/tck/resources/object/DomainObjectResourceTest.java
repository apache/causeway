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
package org.apache.isis.viewer.json.tck.resources.object;

import static org.apache.isis.core.commons.matchers.IsisMatchers.matches;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.assertThat;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.isLink;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.isis.runtimes.dflt.webserver.WebServer;
import org.apache.isis.tck.dom.assocs.ChildEntity;
import org.apache.isis.tck.dom.scalars.ApplibValuedEntity;
import org.apache.isis.viewer.json.applib.HttpMethod;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RestfulClient;
import org.apache.isis.viewer.json.applib.RestfulRequest;
import org.apache.isis.viewer.json.applib.RestfulResponse;
import org.apache.isis.viewer.json.applib.domainobjects.ActionResultRepresentation;
import org.apache.isis.viewer.json.applib.domainobjects.ActionResultRepresentation.ResultType;
import org.apache.isis.viewer.json.applib.domainobjects.DomainObjectRepresentation;
import org.apache.isis.viewer.json.applib.domainobjects.DomainObjectResource;
import org.apache.isis.viewer.json.applib.domainobjects.ObjectActionRepresentation;
import org.apache.isis.viewer.json.applib.domainobjects.ObjectPropertyRepresentation;
import org.apache.isis.viewer.json.applib.domainobjects.ScalarValueRepresentation;
import org.apache.isis.viewer.json.applib.links.LinkRepresentation;
import org.apache.isis.viewer.json.applib.links.Rel;
import org.apache.isis.viewer.json.tck.IsisWebServerRule;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;


public class DomainObjectResourceTest {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();
    
    protected RestfulClient client;

    @Before
    public void setUp() throws Exception {
        WebServer webServer = webServerRule.getWebServer();
        client = new RestfulClient(webServer.getBase());
    }


    @Test
    public void returnsDomainObjectRepresentation() throws Exception {
        
        // given
        DomainObjectResource domainObjectResource = client.getDomainObjectResource();
        
        // when
        Response domainObjectResp = domainObjectResource.object("OID:6");
        RestfulResponse<DomainObjectRepresentation> domainObjectJsonResp = RestfulResponse.ofT(domainObjectResp);
        assertThat(domainObjectJsonResp.getStatus().getFamily(), is(Family.SUCCESSFUL));
        
        // then 
        DomainObjectRepresentation domainObjectRepr = domainObjectJsonResp.getEntity();
        assertThat(domainObjectRepr, is(not(nullValue())));
    }

    @Test
    public void domainObjectRepresentationForPersistentObject_hasSelfAndOid() throws Exception {
        
        // given, when
        DomainObjectRepresentation domainObjectRepr = givenDomainObjectRepresentationFor("OID:32");

        // then
        LinkRepresentation self = domainObjectRepr.getSelf();
        assertThat(self, 
                isLink().rel(Rel.SELF).href(matches(".+objects/OID:32")).httpMethod(HttpMethod.GET).type(MediaType.APPLICATION_JSON_TYPE).typeParameter("profile", "urn:org.restfulobjects/domainobject"));
        assertThat(domainObjectRepr.getLinkWithRel(Rel.DESCRIBEDBY), 
                isLink().href(matches(".+" + ChildEntity.class.getName())).httpMethod(HttpMethod.GET).type(MediaType.APPLICATION_JSON_TYPE).typeParameter("profile", "urn:org.restfulobjects/domaintype"));
        
        assertThat(domainObjectRepr.getTitle(), is("parent 4 - child 2"));
        assertThat(domainObjectRepr.getOid(), is("OID:32"));

        // no icon
        LinkRepresentation selfIcon = domainObjectRepr.getLinkWithRel(Rel.ICON);
        assertThat(selfIcon, is(nullValue()));
    }

    @Test
    public void domainObjectRepresentationForTransient_hasNoSelf_andHasNoOid() throws Exception {
        
        // given, when
        final RestfulRequest request = this.client.createRequest(HttpMethod.POST, "services/simples/actions/newTransientEntity/invoke");
        final RestfulResponse<ActionResultRepresentation> response = request.executeT();
        final ActionResultRepresentation actionResultRepr = response.getEntity();
        assertThat(actionResultRepr.getResultType(), is(ResultType.DOMAIN_OBJECT));
        assertThat(actionResultRepr.getResult(), is(not(nullValue())));

        DomainObjectRepresentation domainObjectRepr = actionResultRepr.getResult().as(DomainObjectRepresentation.class);

        // then
        LinkRepresentation self = domainObjectRepr.getSelf();
        assertThat(self, is(nullValue()));
        
        assertThat(domainObjectRepr.getOid(), is(nullValue()));
    }

    @Test
    public void domainObjectRepresentation_hasTitle() throws Exception {
        
        // given, when
        DomainObjectRepresentation domainObjectRepr = givenDomainObjectRepresentationFor("OID:32");

        // then
        assertThat(domainObjectRepr.getTitle(), is("parent 4 - child 2"));
    }

    @Test
    public void domainObjectRepresentation_hasDescribedByLink() throws Exception {
        
        // given, when
        DomainObjectRepresentation domainObjectRepr = givenDomainObjectRepresentationFor("OID:32");

        // then
        assertThat(domainObjectRepr.getLinkWithRel(Rel.DESCRIBEDBY), 
                isLink().href(matches(".+" + ChildEntity.class.getName())).httpMethod(HttpMethod.GET).type(MediaType.APPLICATION_JSON_TYPE).typeParameter("profile", "urn:org.restfulobjects/domaintype"));
    }

    @Test
    public void domainObjectRepresentation_noIcon() throws Exception {
        
        // given, when
        DomainObjectRepresentation domainObjectRepr = givenDomainObjectRepresentationFor("OID:32");

        // then 
        LinkRepresentation selfIcon = domainObjectRepr.getLinkWithRel(Rel.ICON);
        assertThat(selfIcon, is(nullValue()));
    }

    @Ignore("TODO")
    @Test
    public void domainObjectWithIcon() throws Exception {

        // given, when
        DomainObjectRepresentation domainObjectRepr = givenDomainObjectRepresentationFor("OID:xxx");

        // icon
        LinkRepresentation selfIcon = domainObjectRepr.getLinkWithRel(Rel.ICON);
        assertThat(selfIcon, 
                isLink().href(matches(".+" + "/images/" + "null\\.png")).httpMethod(HttpMethod.GET));

    }


    @Test
    public void domainObjectRepresentationContent_Properties() throws Exception {
        
        // given, when
        DomainObjectRepresentation domainObjectRepr = givenDomainObjectRepresentationFor("OID:32");
        LinkRepresentation self = domainObjectRepr.getSelf();

        // then properties
        JsonRepresentation properties = domainObjectRepr.getProperties();
        assertThat(properties.size(), is(2));
        
        // property ('name')
        JsonRepresentation nameProperty = properties.getRepresentation("[id=name]");
        assertThat(nameProperty.getString("memberType"), is("property"));
        assertThat(nameProperty.getString("value"), is("child 2"));
        assertThat(nameProperty.getString("disabledReason"), is(nullValue()));

        LinkRepresentation namePropertyDetails = nameProperty.getLink("links[rel=details]");
        assertThat(namePropertyDetails, isLink().rel("details").href(self.getHref() + "/properties/name").httpMethod(HttpMethod.GET));

    }

    @Ignore("todo")
    @Test
    public void domainObjectRepresentationContent_Collections() throws Exception {
        
        // given, when
        DomainObjectRepresentation domainObjectRepr = givenDomainObjectRepresentationFor("OID:32");

        // then collections
        

    }

    @Ignore("TODO")
    @Test
    public void domainObjectRepresentationContent() throws Exception {
        
        // given, when
        DomainObjectRepresentation domainObjectRepr = givenDomainObjectRepresentationFor("OID:32");
        LinkRepresentation self = domainObjectRepr.getSelf();

        // then actions
        JsonRepresentation actions = domainObjectRepr.getActions();
        assertThat(actions.size(), is(2));

        JsonRepresentation listAction = actions.getRepresentation("list");
        assertThat(listAction.getString("memberType"), is("action"));
        assertThat(listAction.getString("actionId"), is("list"));
        assertThat(listAction.getString("actionType"), is("USER"));
        assertThat(listAction.getInt("numParameters"), is(0));

        LinkRepresentation listActionType = listAction.getLink("type");
        assertThat(listActionType.getRel(), is("type"));
        assertThat(listActionType.getHref(), matches(".+vnd\\.list\\+json"));
        assertThat(listActionType.getHttpMethod(), is(HttpMethod.GET));

        LinkRepresentation listActionDetails = listAction.getLink("details");
        assertThat(listActionDetails.getRel(), is("action"));
        assertThat(listActionDetails.getHref(), is(self.getHref() + "/actions/list"));
        assertThat(listActionDetails.getHttpMethod(), is(HttpMethod.GET));

        JsonRepresentation newEntityAction = actions.getRepresentation("newEntity");
        assertThat(newEntityAction.getString("memberType"), is("action"));
        assertThat(newEntityAction.getString("actionType"), is("USER"));
        assertThat(newEntityAction.getInt("numParameters"), is(0));

        LinkRepresentation newEntityActionType = newEntityAction.getLink("type");
        assertThat(newEntityActionType.getRel(), is("type"));
        assertThat(newEntityActionType.getHref(), matches(".+vnd\\." +
                ApplibValuedEntity.class.getName() +
                "\\+json"));
        assertThat(newEntityActionType.getHttpMethod(), is(HttpMethod.GET));

        LinkRepresentation newEntityActionDetails = newEntityAction.getLink("details");
        assertThat(newEntityActionDetails.getRel(), is("action"));
        assertThat(newEntityActionDetails.getHref(), is(self.getHref() + "/actions/newEntity"));
        assertThat(newEntityActionDetails.getHttpMethod(), is(HttpMethod.GET));
    }

    @Ignore("TODO")
    @Test
    public void domainObjectWithDisabledMembers() throws Exception {

        // given, when
        DomainObjectRepresentation domainObjectRepr = givenDomainObjectRepresentationFor("OID:xxx");

        // property ('name')
        JsonRepresentation properties = domainObjectRepr.getProperties();
        JsonRepresentation nameProperty = properties.getRepresentation("name");
        assertThat(nameProperty.getString("disabledReason"), is(not(nullValue())));
    }


    @Ignore("to get working again")
    @Test
    public void propertyDetails() throws Exception {
        // given
        DomainObjectResource domainObjectResource = client.getDomainObjectResource();
        
        // when
        Response idPropertyResp = domainObjectResource.propertyDetails("OID:1", "id");
        RestfulResponse<ObjectPropertyRepresentation> idPropertyJsonResp = RestfulResponse.ofT(idPropertyResp);
        assertThat(idPropertyJsonResp.getStatus().getFamily(), is(Family.SUCCESSFUL));
        
        // then 
        ObjectPropertyRepresentation propertyDetailsRepr = idPropertyJsonResp.getEntity();

        // _self.link
        LinkRepresentation selfLink = propertyDetailsRepr.getLink("_self.link");
        assertThat(selfLink.getRel(), is("member"));
        assertThat(selfLink.getHref(), matches(".+objects/OID:1/properties/id"));
        assertThat(selfLink.getHttpMethod(), is(HttpMethod.GET));

        // _self.object
        LinkRepresentation selfObject = propertyDetailsRepr.getLink("_self.object");
        assertThat(selfObject.getRel(), is("object"));
        assertThat(selfObject.getHref(), matches(".+objects/OID:1"));
        assertThat(selfObject.getHttpMethod(), is(HttpMethod.GET));

        // type
        LinkRepresentation type = propertyDetailsRepr.getLink("type");
        assertThat(type.getRel(), is("type"));
        assertThat(type.getHref(), matches(".+vnd\\.string\\+json"));
        assertThat(type.getHttpMethod(), is(HttpMethod.GET));

        assertThat(propertyDetailsRepr.getString("memberType"), is("property"));
        assertThat(propertyDetailsRepr.getString("value"), is(org.apache.isis.tck.objstore.dflt.scalars.ApplibValuedEntityRepositoryDefault.class.getName()));
        assertThat(propertyDetailsRepr.getString("disabledReason"), is(not(nullValue())));
    }

    
    @Ignore("to get working again")
    @Test
    public void actionPrompt() throws Exception {
        // given
        DomainObjectResource domainObjectResource = client.getDomainObjectResource();
        
        // when
        Response actionPromptResp = domainObjectResource.actionPrompt("OID:1", "list");
        RestfulResponse<ObjectActionRepresentation> actionPromptJsonResp = RestfulResponse.ofT(actionPromptResp);
        assertThat(actionPromptJsonResp.getStatus().getFamily(), is(Family.SUCCESSFUL));
        
        // then 
        ObjectActionRepresentation actionPromptRepr = actionPromptJsonResp.getEntity();

        // _self.link
        LinkRepresentation selfLink = actionPromptRepr.getLink("_self.link");
        assertThat(selfLink.getRel(), is("member"));
        assertThat(selfLink.getHref(), matches(".+objects/OID:1/actions/list"));
        assertThat(selfLink.getHttpMethod(), is(HttpMethod.GET));

        // _self.object
        LinkRepresentation selfObject = actionPromptRepr.getLink("_self.object");
        assertThat(selfObject.getRel(), is("object"));
        assertThat(selfObject.getHref(), matches(".+objects/OID:1"));
        assertThat(selfObject.getHttpMethod(), is(HttpMethod.GET));

        // type
        LinkRepresentation type = actionPromptRepr.getLink("type");
        assertThat(type.getRel(), is("type"));
        assertThat(type.getHref(), matches(".+vnd\\.list\\+json"));
        assertThat(type.getHttpMethod(), is(HttpMethod.GET));

        assertThat(actionPromptRepr.getString("memberType"), is("action"));
        assertThat(actionPromptRepr.getString("actionType"), is("USER"));
        assertThat(actionPromptRepr.getInt("numParameters"), is(0));
        assertThat(actionPromptRepr.getArray("parameters").size(), is(0));
        
        LinkRepresentation invokeLink = actionPromptRepr.getLink("invoke");
        assertThat(invokeLink.getRel(), is("invoke"));
        assertThat(invokeLink.getHref(), matches(".+objects/OID:1/actions/list/invoke"));
        assertThat(invokeLink.getHttpMethod(), is(HttpMethod.POST));
        assertThat(invokeLink.getArguments(), is(not(nullValue())));
        assertThat(invokeLink.getArguments().isArray(), is(true));
        assertThat(invokeLink.getArguments().size(), is(0));
    }

    @Ignore("TODO")
    @Test
    public void collectionDetails() throws Exception {
        fail();
    }

    
    @Ignore("to get working again")
    @Test
    public void actionPostInvoke_returningList() throws Exception {
        
        // given
        DomainObjectResource domainObjectResource = client.getDomainObjectResource();
        
        JsonRepresentation body = JsonRepresentation.newArray();
        
        // when
        Response actionInvokeResp = domainObjectResource.invokeAction("OID:1", "list", body.asInputStream());
        RestfulResponse<ScalarValueRepresentation> actionInvokeJsonResp = RestfulResponse.ofT(actionInvokeResp);
        assertThat(actionInvokeJsonResp.getStatus().getFamily(), is(Family.SUCCESSFUL));
        
        // then 
        ScalarValueRepresentation actionInvokeRepr = actionInvokeJsonResp.getEntity();
        assertThat(actionInvokeRepr.isArray(), is(true));
        assertThat(actionInvokeRepr.size(), is(5));
        
        JsonRepresentation domainObjectRefRepr = actionInvokeRepr.arrayGet(0);

        assertThat(domainObjectRefRepr, is(not(nullValue())));
        assertThat(domainObjectRefRepr.getString("title"), is("Untitled Applib Values Entity")); // TODO
        
        LinkRepresentation domainObjectLink = domainObjectRefRepr.getLink("link");
        assertThat(domainObjectLink.getRel(), is("object"));
        assertThat(domainObjectLink.getHref(), matches("http://localhost:\\d+/objects/OID:7"));
        
        LinkRepresentation domainObjectTypeLink = domainObjectRefRepr.getLink("type");
        assertThat(domainObjectTypeLink.getRel(), is("type"));
        assertThat(domainObjectTypeLink.getHref(), matches("http://localhost:\\d+/types/application/vnd." +
        		org.apache.isis.tck.dom.scalars.ApplibValuedEntity.class.getName() +
        		"\\+json"));

        LinkRepresentation domainObjectIconLink = domainObjectRefRepr.getLink("icon");
        assertThat(domainObjectIconLink.getRel(), is("icon"));
        assertThat(domainObjectIconLink.getHref(), matches("http://localhost:\\d+/images/null.png")); // TODO
    }

    private DomainObjectRepresentation givenDomainObjectRepresentationFor(final String oidStr) throws JsonParseException, JsonMappingException, IOException {
        DomainObjectResource domainObjectResource = client.getDomainObjectResource();
        
        Response domainObjectResp = domainObjectResource.object(oidStr);
        RestfulResponse<DomainObjectRepresentation> domainObjectJsonResp = RestfulResponse.ofT(domainObjectResp);
        assertThat(domainObjectJsonResp.getStatus().getFamily(), is(Family.SUCCESSFUL));
        
        DomainObjectRepresentation domainObjectRepr = domainObjectJsonResp.getEntity();
        return domainObjectRepr;
    }


}
    