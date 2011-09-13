package org.apache.isis.viewer.json.tck;

import static org.apache.isis.core.commons.matchers.IsisMatchers.matches;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.assertThat;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.isLink;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.isis.runtimes.dflt.webserver.WebServer;
import org.apache.isis.tck.dom.scalars.ApplibValuedEntity;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RestfulClient;
import org.apache.isis.viewer.json.applib.RestfulResponse;
import org.apache.isis.viewer.json.applib.blocks.Link;
import org.apache.isis.viewer.json.applib.blocks.Method;
import org.apache.isis.viewer.json.applib.domainobjects.DomainObjectRepresentation;
import org.apache.isis.viewer.json.applib.domainobjects.DomainObjectResource;
import org.apache.isis.viewer.json.applib.domainobjects.ObjectActionRepresentation;
import org.apache.isis.viewer.json.applib.domainobjects.ObjectPropertyRepresentation;
import org.apache.isis.viewer.json.applib.domainobjects.ScalarValueRepresentation;
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


    @Ignore
    @Test
    public void returnsDomainObjectRepresentation() throws Exception {
        
        // given
        DomainObjectResource domainObjectResource = client.getDomainObjectResource();
        
        // when
        Response domainObjectResp = domainObjectResource.object("OID:6");
        RestfulResponse<DomainObjectRepresentation> domainObjectJsonResp = RestfulResponse.of(domainObjectResp, DomainObjectRepresentation.class);
        assertThat(domainObjectJsonResp.getStatus().getFamily(), is(Family.SUCCESSFUL));
        
        // then 
        DomainObjectRepresentation domainObjectRepr = domainObjectJsonResp.getEntity();

        Link self = domainObjectRepr.getSelf();
        assertThat(self, 
                isLink().rel("object").href(matches(".+objects/OID:1")).method(Method.GET));
//        assertThat(domainObjectRepr.getDomainType(), 
//                isLink().rel("type").href(matches(".+" + ApplibValuedEntityRepositoryDefault.class.getName() + ".+")).method(Method.GET));
        
        assertThat(domainObjectRepr.getTitle(), is("ApplibValues"));
        assertThat(domainObjectRepr.getOid(), is("OID:1"));

        // self.icon
        Link selfIcon = domainObjectRepr.getLink("_self.icon");
        // TODO: shouldn't really be present since no icon available; or should point to a default, perhaps
        assertThat(selfIcon, isLink().rel("icon").href(matches(".+" + "/images/" + "null\\.png")).method(Method.GET));

        // properties
        JsonRepresentation properties = domainObjectRepr.getProperties();
        assertThat(properties.mapSize(), is(1));
        
        // id property
        JsonRepresentation idProperty = properties.getRepresentation("id");
        assertThat(idProperty.getString("memberType"), is("property"));
        assertThat(idProperty.getString("propertyId"), is("id"));
        assertThat(idProperty.getString("value"), is(org.apache.isis.tck.objstore.dflt.scalars.ApplibValuedEntityRepositoryDefault.class.getName()));
        assertThat(idProperty.getString("disabledReason"), is(not(nullValue())));

        Link idPropertyType = idProperty.getLink("type");
        assertThat(idPropertyType, isLink().rel("type").href(matches(".+vnd\\.string\\+json")).method(Method.GET));

        Link idPropertyDetails = idProperty.getLink("details");
        assertThat(idPropertyDetails, isLink().rel("property").href(self.getHref() + "/properties/id").method(Method.GET));

        // actions
        JsonRepresentation actions = domainObjectRepr.getActions();
        assertThat(actions.mapSize(), is(2));

        JsonRepresentation listAction = actions.getRepresentation("list");
        assertThat(listAction.getString("memberType"), is("action"));
        assertThat(listAction.getString("actionId"), is("list"));
        assertThat(listAction.getString("actionType"), is("USER"));
        assertThat(listAction.getInt("numParameters"), is(0));

        Link listActionType = listAction.getLink("type");
        assertThat(listActionType.getRel(), is("type"));
        assertThat(listActionType.getHref(), matches(".+vnd\\.list\\+json"));
        assertThat(listActionType.getMethod(), is(Method.GET));

        Link listActionDetails = listAction.getLink("details");
        assertThat(listActionDetails.getRel(), is("action"));
        assertThat(listActionDetails.getHref(), is(self.getHref() + "/actions/list"));
        assertThat(listActionDetails.getMethod(), is(Method.GET));

        JsonRepresentation newEntityAction = actions.getRepresentation("newEntity");
        assertThat(newEntityAction.getString("memberType"), is("action"));
        assertThat(newEntityAction.getString("actionType"), is("USER"));
        assertThat(newEntityAction.getInt("numParameters"), is(0));

        Link newEntityActionType = newEntityAction.getLink("type");
        assertThat(newEntityActionType.getRel(), is("type"));
        assertThat(newEntityActionType.getHref(), matches(".+vnd\\." +
                ApplibValuedEntity.class.getName() +
        		"\\+json"));
        assertThat(newEntityActionType.getMethod(), is(Method.GET));

        Link newEntityActionDetails = newEntityAction.getLink("details");
        assertThat(newEntityActionDetails.getRel(), is("action"));
        assertThat(newEntityActionDetails.getHref(), is(self.getHref() + "/actions/newEntity"));
        assertThat(newEntityActionDetails.getMethod(), is(Method.GET));
    }

    @Ignore("to get working again")
    @Test
    public void propertyDetails() throws Exception {
        // given
        DomainObjectResource domainObjectResource = client.getDomainObjectResource();
        
        // when
        Response idPropertyResp = domainObjectResource.propertyDetails("OID:1", "id");
        RestfulResponse<ObjectPropertyRepresentation> idPropertyJsonResp = RestfulResponse.of(idPropertyResp, ObjectPropertyRepresentation.class);
        assertThat(idPropertyJsonResp.getStatus().getFamily(), is(Family.SUCCESSFUL));
        
        // then 
        ObjectPropertyRepresentation propertyDetailsRepr = idPropertyJsonResp.getEntity();

        // _self.link
        Link selfLink = propertyDetailsRepr.getLink("_self.link");
        assertThat(selfLink.getRel(), is("member"));
        assertThat(selfLink.getHref(), matches(".+objects/OID:1/properties/id"));
        assertThat(selfLink.getMethod(), is(Method.GET));

        // _self.object
        Link selfObject = propertyDetailsRepr.getLink("_self.object");
        assertThat(selfObject.getRel(), is("object"));
        assertThat(selfObject.getHref(), matches(".+objects/OID:1"));
        assertThat(selfObject.getMethod(), is(Method.GET));

        // type
        Link type = propertyDetailsRepr.getLink("type");
        assertThat(type.getRel(), is("type"));
        assertThat(type.getHref(), matches(".+vnd\\.string\\+json"));
        assertThat(type.getMethod(), is(Method.GET));

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
        RestfulResponse<ObjectActionRepresentation> actionPromptJsonResp = RestfulResponse.of(actionPromptResp, ObjectActionRepresentation.class);
        assertThat(actionPromptJsonResp.getStatus().getFamily(), is(Family.SUCCESSFUL));
        
        // then 
        ObjectActionRepresentation actionPromptRepr = actionPromptJsonResp.getEntity();

        // _self.link
        Link selfLink = actionPromptRepr.getLink("_self.link");
        assertThat(selfLink.getRel(), is("member"));
        assertThat(selfLink.getHref(), matches(".+objects/OID:1/actions/list"));
        assertThat(selfLink.getMethod(), is(Method.GET));

        // _self.object
        Link selfObject = actionPromptRepr.getLink("_self.object");
        assertThat(selfObject.getRel(), is("object"));
        assertThat(selfObject.getHref(), matches(".+objects/OID:1"));
        assertThat(selfObject.getMethod(), is(Method.GET));

        // type
        Link type = actionPromptRepr.getLink("type");
        assertThat(type.getRel(), is("type"));
        assertThat(type.getHref(), matches(".+vnd\\.list\\+json"));
        assertThat(type.getMethod(), is(Method.GET));

        assertThat(actionPromptRepr.getString("memberType"), is("action"));
        assertThat(actionPromptRepr.getString("actionType"), is("USER"));
        assertThat(actionPromptRepr.getInt("numParameters"), is(0));
        assertThat(actionPromptRepr.getArray("parameters").arraySize(), is(0));
        
        Link invokeLink = actionPromptRepr.getLink("invoke");
        assertThat(invokeLink.getRel(), is("invoke"));
        assertThat(invokeLink.getHref(), matches(".+objects/OID:1/actions/list/invoke"));
        assertThat(invokeLink.getMethod(), is(Method.POST));
        assertThat(invokeLink.getArguments(), is(not(nullValue())));
        assertThat(invokeLink.getArguments().isArray(), is(true));
        assertThat(invokeLink.getArguments().arraySize(), is(0));
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
        RestfulResponse<ScalarValueRepresentation> actionInvokeJsonResp = RestfulResponse.of(actionInvokeResp, ScalarValueRepresentation.class);
        assertThat(actionInvokeJsonResp.getStatus().getFamily(), is(Family.SUCCESSFUL));
        
        // then 
        ScalarValueRepresentation actionInvokeRepr = actionInvokeJsonResp.getEntity();
        assertThat(actionInvokeRepr.isArray(), is(true));
        assertThat(actionInvokeRepr.arraySize(), is(5));
        
        JsonRepresentation domainObjectRefRepr = actionInvokeRepr.arrayGet(0);

        assertThat(domainObjectRefRepr, is(not(nullValue())));
        assertThat(domainObjectRefRepr.getString("title"), is("Untitled Applib Values Entity")); // TODO
        
        Link domainObjectLink = domainObjectRefRepr.getLink("link");
        assertThat(domainObjectLink.getRel(), is("object"));
        assertThat(domainObjectLink.getHref(), matches("http://localhost:\\d+/objects/OID:7"));
        
        Link domainObjectTypeLink = domainObjectRefRepr.getLink("type");
        assertThat(domainObjectTypeLink.getRel(), is("type"));
        assertThat(domainObjectTypeLink.getHref(), matches("http://localhost:\\d+/types/application/vnd." +
        		org.apache.isis.tck.dom.scalars.ApplibValuedEntity.class.getName() +
        		"\\+json"));

        Link domainObjectIconLink = domainObjectRefRepr.getLink("icon");
        assertThat(domainObjectIconLink.getRel(), is("icon"));
        assertThat(domainObjectIconLink.getHref(), matches("http://localhost:\\d+/images/null.png")); // TODO
    }

}
    