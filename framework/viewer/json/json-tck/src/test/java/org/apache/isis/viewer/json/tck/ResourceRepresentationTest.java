package org.apache.isis.viewer.json.tck;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.apache.isis.core.commons.matchers.IsisMatchers.*;

import javax.ws.rs.core.Response;

import org.apache.isis.runtimes.dflt.webserver.WebServer;
import org.apache.isis.tck.dom.scalars.ApplibValuesEntity;
import org.apache.isis.tck.objstore.dflt.scalars.ApplibValuesEntityRepositoryDefault;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RepresentationWalker;
import org.apache.isis.viewer.json.applib.RestfulClient;
import org.apache.isis.viewer.json.applib.blocks.Link;
import org.apache.isis.viewer.json.applib.blocks.Method;
import org.apache.isis.viewer.json.applib.domain.ActionInvocationRepresentation;
import org.apache.isis.viewer.json.applib.domain.ActionPromptRepresentation;
import org.apache.isis.viewer.json.applib.domain.DomainObjectRepresentation;
import org.apache.isis.viewer.json.applib.domain.DomainObjectResource;
import org.apache.isis.viewer.json.applib.domain.PropertyDetailsRepresentation;
import org.apache.isis.viewer.json.applib.domain.ServicesRepresentation;
import org.apache.isis.viewer.json.applib.domain.ServicesResource;
import org.apache.isis.viewer.json.applib.homepage.HomePageRepresentation;
import org.apache.isis.viewer.json.applib.homepage.HomePageResource;
import org.apache.isis.viewer.json.applib.user.UserRepresentation;
import org.apache.isis.viewer.json.applib.util.HttpStatusCode;
import org.apache.isis.viewer.json.applib.util.HttpStatusCode.Range;
import org.apache.isis.viewer.json.applib.util.JsonResponse;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;


public class ResourceRepresentationTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        webServer = new WebServer();
        webServer.run(39393);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        webServer.stop();
    }

    protected static WebServer webServer;
    protected RestfulClient client;

    @Before
    public void setUp() throws Exception {
        client = new RestfulClient(webServer.getBase());
    }


    @Test
    public void homePageResource_returnsHomePageRepresentation() throws Exception {
        // given
        HomePageResource homePageResource = client.getHomePageResource();
        
        // when
        Response resourcesResp = homePageResource.resources();
        JsonResponse<HomePageRepresentation> homePageJsonResp = JsonResponse.of(resourcesResp, HomePageRepresentation.class);
        assertThat(homePageJsonResp.getStatus().getRange(), is(Range.SUCCESS));
        
        // then
        assertThat(homePageJsonResp.getStatus(), is(HttpStatusCode.OK));
        
        HomePageRepresentation homePageRepr = homePageJsonResp.getEntity();
        assertThat(homePageRepr, is(not(nullValue())));
        assertThat(homePageRepr.isMap(), is(true));
        
        assertThat(homePageRepr.getUser(), is(not(nullValue())));
        assertThat(homePageRepr.getUser().getMethod(), is(Method.GET));
        
        assertThat(homePageRepr.getServices(), is(not(nullValue())));
        assertThat(homePageRepr.getServices().getMethod(), is(Method.GET));
    }

    @Test
    public void homePageResource_linksToServicesResource() throws Exception {
        
        // given
        HomePageResource homePageResource = client.getHomePageResource();

        // when
        Response resourcesResp = homePageResource.resources();
        JsonResponse<HomePageRepresentation> homePageJsonResp = JsonResponse.of(resourcesResp, HomePageRepresentation.class);
        
        // then
        HomePageRepresentation homePageRepr = homePageJsonResp.getEntity();

        // and when
        Response servicesResp = client.follow(homePageRepr.getServices());
        JsonResponse<ServicesRepresentation> servicesJsonResp = JsonResponse.of(servicesResp, ServicesRepresentation.class);
        
        // then
        ServicesRepresentation servicesRepr = servicesJsonResp.getEntity();
        
        JsonRepresentation applibValuesEntityRepoRep = servicesRepr.elementAt(0);
        assertThat(applibValuesEntityRepoRep, is(not(nullValue())));
    }

    @Ignore("not yet implemented")
    @Test
    public void homePageResource_linksToUserResource() throws Exception {
        
        // given
        HomePageResource homePageResource = client.getHomePageResource();

        // when
        Response resourcesResp = homePageResource.resources();
        JsonResponse<HomePageRepresentation> homePageJsonResp = JsonResponse.of(resourcesResp, HomePageRepresentation.class);
        
        // then
        HomePageRepresentation homePageRepr = homePageJsonResp.getEntity();

        // and when
        Response userResp = client.follow(homePageRepr.getUser());
        JsonResponse<UserRepresentation> userJsonResp = JsonResponse.of(userResp, UserRepresentation.class);
        
        // then
        UserRepresentation userRepr = userJsonResp.getEntity();

        assertThat(userRepr, is(not(nullValue())));
        assertThat(userRepr.isMap(), is(true));
    }

    
    @Test
    public void servicesResource_returnsServicesRepresentation() throws Exception {
        
        // given
        ServicesResource servicesResource = client.getServicesResource();
        
        // when
        Response servicesResp = servicesResource.services();
        JsonResponse<ServicesRepresentation> servicesJsonResp = JsonResponse.of(servicesResp, ServicesRepresentation.class);
        assertThat(servicesJsonResp.getStatus().getRange(), is(Range.SUCCESS));
        
        // then
        ServicesRepresentation servicesRepr = servicesJsonResp.getEntity();

        assertThat(servicesRepr, is(not(nullValue())));
        assertThat(servicesRepr.isArray(), is(true));
        assertThat(servicesRepr.arraySize(), is(4));

        JsonRepresentation repoRepr = servicesRepr.elementAt(0);
        assertThat(repoRepr, is(not(nullValue())));
        
        assertThat(repoRepr.getString("title"), is("ApplibValues"));
        
        Link repoObjLink = repoRepr.getLink("link");
        assertThat(repoObjLink.getRel(), is("object"));
        assertThat(repoObjLink.getHref(), matches("http://localhost:\\d+/objects/OID:1"));
        
        Link repoTypeLink = repoRepr.getLink("type");
        assertThat(repoTypeLink.getRel(), is("type"));
        assertThat(repoTypeLink.getHref(), matches("http://localhost:\\d+/types/application/vnd." +
        		org.apache.isis.tck.objstore.dflt.scalars.ApplibValuesEntityRepositoryDefault.class.getName() +
        		"\\+json"));

        Link repoIconLink = repoRepr.getLink("icon");
        assertThat(repoIconLink.getRel(), is("icon"));
        assertThat(repoIconLink.getHref(), matches("http://localhost:\\d+/images/null.png"));
    }


    @Test
    public void servicesResource_linksToDomainObjectResourceForService() throws Exception {
        
        // given
        ServicesResource servicesResource = client.getServicesResource();
        
        // when
        Response servicesResp = servicesResource.services();
        JsonResponse<ServicesRepresentation> servicesJsonResp = JsonResponse.of(servicesResp, ServicesRepresentation.class);
        
        // then
        ServicesRepresentation servicesRepr = servicesJsonResp.getEntity();

        JsonRepresentation repoRepr = servicesRepr.elementAt(0);
        Link repoObjLink = repoRepr.getLink("link");

        // and when
        Response repoFollowResp = client.follow(repoObjLink);
        JsonResponse<DomainObjectRepresentation> repoFollowJsonResp = JsonResponse.of(repoFollowResp, DomainObjectRepresentation.class);
        
        // then
        DomainObjectRepresentation domainObjectRepr = repoFollowJsonResp.getEntity();
        Link domainObjectReprLink = domainObjectRepr.getLink("_self.link");
        assertThat(domainObjectReprLink, is(repoObjLink));
    }


    @Test
    public void domainObjectResource_returnsDomainObjectRepresentation() throws Exception {
        
        // given
        DomainObjectResource domainObjectResource = client.getDomainObjectResource();
        
        // when
        Response domainObjectResp = domainObjectResource.object("OID:1");
        JsonResponse<DomainObjectRepresentation> domainObjectJsonResp = JsonResponse.of(domainObjectResp, DomainObjectRepresentation.class);
        assertThat(domainObjectJsonResp.getStatus().getRange(), is(Range.SUCCESS));
        
        // then 
        DomainObjectRepresentation domainObjectRepr = domainObjectJsonResp.getEntity();

        // _self.link
        Link selfLink = domainObjectRepr.getLink("_self.link");
        assertThat(selfLink.getRel(), is("object"));
        assertThat(selfLink.getHref(), matches(".+objects/OID:1"));
        assertThat(selfLink.getMethod(), is(Method.GET));

        // _self.type
        Link selfType = domainObjectRepr.getLink("_self.type");
        assertThat(selfType.getRel(), is("type"));
        assertThat(selfType.getHref(), matches(".+" + ApplibValuesEntityRepositoryDefault.class.getName() + ".+"));
        assertThat(selfType.getMethod(), is(Method.GET));
        
        assertThat(domainObjectRepr.getString("_self.title"), is("ApplibValues"));

        // _self.icon
        Link selfIcon = domainObjectRepr.getLink("_self.icon");
        assertThat(selfIcon.getRel(), is("icon"));
        assertThat(selfIcon.getHref(), matches(".+" + "/images/" + "null\\.png"));  // TODO: shouldn't really be present
        assertThat(selfIcon.getMethod(), is(Method.GET));

        // properties
        JsonRepresentation properties = domainObjectRepr.getProperties();
        assertThat(properties.mapSize(), is(1));
        
        // id property
        JsonRepresentation idProperty = properties.getRepresentation("id");
        assertThat(idProperty.getString("memberType"), is("property"));
        assertThat(idProperty.getString("value"), is(org.apache.isis.tck.objstore.dflt.scalars.ApplibValuesEntityRepositoryDefault.class.getName()));
        assertThat(idProperty.getString("disabledReason"), is(not(nullValue())));

        Link idPropertyType = idProperty.getLink("type");
        assertThat(idPropertyType.getRel(), is("type"));
        assertThat(idPropertyType.getHref(), matches(".+vnd\\.string\\+json"));
        assertThat(idPropertyType.getMethod(), is(Method.GET));

        Link idPropertyDetails = idProperty.getLink("details");
        assertThat(idPropertyDetails.getRel(), is("property"));
        assertThat(idPropertyDetails.getHref(), is(selfLink.getHref() + "/properties/id"));
        assertThat(idPropertyDetails.getMethod(), is(Method.GET));

        // actions
        JsonRepresentation actions = domainObjectRepr.getActions();
        assertThat(actions.mapSize(), is(2));

        JsonRepresentation listAction = actions.getRepresentation("list");
        assertThat(listAction.getString("memberType"), is("action"));
        assertThat(listAction.getString("actionType"), is("USER"));
        assertThat(listAction.getInt("numParameters"), is(0));

        Link listActionType = listAction.getLink("type");
        assertThat(listActionType.getRel(), is("type"));
        assertThat(listActionType.getHref(), matches(".+vnd\\.list\\+json"));
        assertThat(listActionType.getMethod(), is(Method.GET));

        Link listActionDetails = listAction.getLink("details");
        assertThat(listActionDetails.getRel(), is("action"));
        assertThat(listActionDetails.getHref(), is(selfLink.getHref() + "/actions/list"));
        assertThat(listActionDetails.getMethod(), is(Method.GET));

        JsonRepresentation newEntityAction = actions.getRepresentation("newEntity");
        assertThat(newEntityAction.getString("memberType"), is("action"));
        assertThat(newEntityAction.getString("actionType"), is("USER"));
        assertThat(newEntityAction.getInt("numParameters"), is(0));

        Link newEntityActionType = newEntityAction.getLink("type");
        assertThat(newEntityActionType.getRel(), is("type"));
        assertThat(newEntityActionType.getHref(), matches(".+vnd\\." +
                ApplibValuesEntity.class.getName() +
        		"\\+json"));
        assertThat(newEntityActionType.getMethod(), is(Method.GET));

        Link newEntityActionDetails = newEntityAction.getLink("details");
        assertThat(newEntityActionDetails.getRel(), is("action"));
        assertThat(newEntityActionDetails.getHref(), is(selfLink.getHref() + "/actions/newEntity"));
        assertThat(newEntityActionDetails.getMethod(), is(Method.GET));
    }

    @Test
    public void domainObjectResource_propertyDetails() throws Exception {
        // given
        DomainObjectResource domainObjectResource = client.getDomainObjectResource();
        
        // when
        Response idPropertyResp = domainObjectResource.propertyDetails("OID:1", "id");
        JsonResponse<PropertyDetailsRepresentation> idPropertyJsonResp = JsonResponse.of(idPropertyResp, PropertyDetailsRepresentation.class);
        assertThat(idPropertyJsonResp.getStatus().getRange(), is(Range.SUCCESS));
        
        // then 
        PropertyDetailsRepresentation propertyDetailsRepr = idPropertyJsonResp.getEntity();

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
        assertThat(propertyDetailsRepr.getString("value"), is(org.apache.isis.tck.objstore.dflt.scalars.ApplibValuesEntityRepositoryDefault.class.getName()));
        assertThat(propertyDetailsRepr.getString("disabledReason"), is(not(nullValue())));
    }

    
    @Test
    public void domainObjectResource_actionPrompt() throws Exception {
        // given
        DomainObjectResource domainObjectResource = client.getDomainObjectResource();
        
        // when
        Response actionPromptResp = domainObjectResource.actionPrompt("OID:1", "list");
        JsonResponse<ActionPromptRepresentation> actionPromptJsonResp = JsonResponse.of(actionPromptResp, ActionPromptRepresentation.class);
        assertThat(actionPromptJsonResp.getStatus().getRange(), is(Range.SUCCESS));
        
        // then 
        ActionPromptRepresentation actionPromptRepr = actionPromptJsonResp.getEntity();

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
        assertThat(invokeLink.getBody(), is(not(nullValue())));
        assertThat(invokeLink.getBody().isArray(), is(true));
        assertThat(invokeLink.getBody().arraySize(), is(0));
    }

    @Ignore("TODO")
    @Test
    public void domainObjectResource_collectionDetails() throws Exception {
        fail();
    }

    
    @Test
    public void domainObjectResource_actionPostInvoke_returningList() throws Exception {
        
        // given
        DomainObjectResource domainObjectResource = client.getDomainObjectResource();
        
        JsonRepresentation body = JsonRepresentation.newArray();
        
        // when
        Response actionInvokeResp = domainObjectResource.invokeAction("OID:1", "list", body.asInputStream());
        JsonResponse<ActionInvocationRepresentation> actionInvokeJsonResp = JsonResponse.of(actionInvokeResp, ActionInvocationRepresentation.class);
        assertThat(actionInvokeJsonResp.getStatus().getRange(), is(Range.SUCCESS));
        
        // then 
        ActionInvocationRepresentation actionInvokeRepr = actionInvokeJsonResp.getEntity();
        assertThat(actionInvokeRepr.isArray(), is(true));
        assertThat(actionInvokeRepr.arraySize(), is(5));
        
        JsonRepresentation domainObjectRefRepr = actionInvokeRepr.elementAt(0);

        assertThat(domainObjectRefRepr, is(not(nullValue())));
        assertThat(domainObjectRefRepr.getString("title"), is("Untitled Applib Values Entity")); // TODO
        
        Link domainObjectLink = domainObjectRefRepr.getLink("link");
        assertThat(domainObjectLink.getRel(), is("object"));
        assertThat(domainObjectLink.getHref(), matches("http://localhost:\\d+/objects/OID:7"));
        
        Link domainObjectTypeLink = domainObjectRefRepr.getLink("type");
        assertThat(domainObjectTypeLink.getRel(), is("type"));
        assertThat(domainObjectTypeLink.getHref(), matches("http://localhost:\\d+/types/application/vnd." +
        		org.apache.isis.tck.dom.scalars.ApplibValuesEntity.class.getName() +
        		"\\+json"));

        Link domainObjectIconLink = domainObjectRefRepr.getLink("icon");
        assertThat(domainObjectIconLink.getRel(), is("icon"));
        assertThat(domainObjectIconLink.getHref(), matches("http://localhost:\\d+/images/null.png")); // TODO
    }
    

    @Test
    public void walkResources() throws Exception {
    
        // given an initial representation
        HomePageResource homePageResource = client.getHomePageResource();

        RepresentationWalker walker = client.createWalker(homePageResource.resources());
        walker.walk("services");
        walker.walkXpath("/*[title='ApplibValues']/link[rel='object']");
        walker.walkXpath("/newEntity[memberType='action']/details");
        JsonRepresentation jsonRepresentation = walker.getEntity();
        
        assertThat(jsonRepresentation.getString("_self.link.href"), matches(".+/objects/OID:1/actions/newEntity")); 
    }

}
    