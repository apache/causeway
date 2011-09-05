package org.apache.isis.viewer.json.tck;

import static org.apache.isis.core.commons.matchers.IsisMatchers.greaterThan;
import static org.apache.isis.core.commons.matchers.IsisMatchers.matches;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.isis.runtimes.dflt.webserver.WebServer;
import org.apache.isis.tck.dom.scalars.ApplibValuedEntity;
import org.apache.isis.tck.objstore.dflt.scalars.ApplibValuedEntityRepositoryDefault;
import org.apache.isis.viewer.json.applib.HttpStatusCode;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RepresentationWalker;
import org.apache.isis.viewer.json.applib.RestfulClient;
import org.apache.isis.viewer.json.applib.RestfulResponse;
import org.apache.isis.viewer.json.applib.blocks.Link;
import org.apache.isis.viewer.json.applib.blocks.Method;
import org.apache.isis.viewer.json.applib.domain.ActionInvocationRepresentation;
import org.apache.isis.viewer.json.applib.domain.ActionPromptRepresentation;
import org.apache.isis.viewer.json.applib.domain.DomainObjectRepresentation;
import org.apache.isis.viewer.json.applib.domain.DomainObjectResource;
import org.apache.isis.viewer.json.applib.domain.DomainServicesResource;
import org.apache.isis.viewer.json.applib.domain.PropertyDetailsRepresentation;
import org.apache.isis.viewer.json.applib.domain.ServicesRepresentation;
import org.apache.isis.viewer.json.applib.homepage.HomePageRepresentation;
import org.apache.isis.viewer.json.applib.homepage.HomePageResource;
import org.apache.isis.viewer.json.applib.reptypes.RepresentationTypeRepresentation;
import org.apache.isis.viewer.json.applib.user.UserRepresentation;
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


    @Ignore("to get working again")
    @Test
    public void homePageResource_returnsHomePageRepresentation() throws Exception {

        // given
        HomePageResource homePageResource = client.getHomePageResource();
        
        // when
        Response resourcesResp = homePageResource.resources();
        RestfulResponse<HomePageRepresentation> homePageJsonResp = RestfulResponse.of(resourcesResp, HomePageRepresentation.class);
        assertThat(homePageJsonResp.getStatus().getFamily(), is(Family.SUCCESSFUL));
        
        // then
        assertThat(homePageJsonResp.getStatus(), is(HttpStatusCode.OK));
        
        HomePageRepresentation homePageRepr = homePageJsonResp.getEntity();
        assertThat(homePageRepr, is(not(nullValue())));
        assertThat(homePageRepr.isMap(), is(true));
        
        assertThat(homePageRepr.getRepresentationType(), is(not(nullValue())));
        assertThat(homePageRepr.getRepresentationType().getMethod(), is(Method.GET));
        
        assertThat(homePageRepr.getSelf(), is(not(nullValue())));
        assertThat(homePageRepr.getSelf().getMethod(), is(Method.GET));
        
        assertThat(homePageRepr.getUser(), is(not(nullValue())));
        assertThat(homePageRepr.getUser().getMethod(), is(Method.GET));
        
        assertThat(homePageRepr.getServices(), is(not(nullValue())));
        assertThat(homePageRepr.getServices().getMethod(), is(Method.GET));

        assertThat(homePageRepr.getLinks(), is(not(nullValue())));
        assertThat(homePageRepr.getMetadata(), is(not(nullValue())));
    }

    @Test
    public void homePageResource_linksToSelf() throws Exception {
        // given
        HomePageResource homePageResource = client.getHomePageResource();

        // when
        Response resourcesResp = homePageResource.resources();
        RestfulResponse<HomePageRepresentation> homePageJsonResp = RestfulResponse.of(resourcesResp, HomePageRepresentation.class);
        
        // then
        HomePageRepresentation homePageRepr = homePageJsonResp.getEntity();

        // and when
        Response servicesResp = client.follow(homePageRepr.getSelf());
        RestfulResponse<HomePageRepresentation> homePageJsonResp2 = RestfulResponse.of(servicesResp, HomePageRepresentation.class);
        
        // then
        HomePageRepresentation homePageRepr2 = homePageJsonResp2.getEntity();
        assertThat(homePageRepr.getSelf(), is(homePageRepr2.getSelf()));
    }

    @Test
    public void homePageResource_linksToItsRepresentationType() throws Exception {
        // given
        HomePageResource homePageResource = client.getHomePageResource();

        // when
        Response resourcesResp = homePageResource.resources();
        RestfulResponse<HomePageRepresentation> homePageJsonResp = RestfulResponse.of(resourcesResp, HomePageRepresentation.class);
        
        // then
        HomePageRepresentation homePageRepr = homePageJsonResp.getEntity();

        // and when
        Response representationTypeResp = client.follow(homePageRepr.getRepresentationType());
        RestfulResponse<RepresentationTypeRepresentation> representationTypeJsonResp = RestfulResponse.of(representationTypeResp, RepresentationTypeRepresentation.class);
        
        // then
        assertThat(representationTypeJsonResp.getStatus().getFamily(), is (Family.SUCCESSFUL));
    }

    @Ignore("to get working again")
    @Test
    public void homePageResource_linksToServicesResource() throws Exception {
        
        // given
        HomePageResource homePageResource = client.getHomePageResource();

        // when
        Response resourcesResp = homePageResource.resources();
        RestfulResponse<HomePageRepresentation> homePageJsonResp = RestfulResponse.of(resourcesResp, HomePageRepresentation.class);
        
        // then
        HomePageRepresentation homePageRepr = homePageJsonResp.getEntity();

        // and when
        Response servicesResp = client.follow(homePageRepr.getServices());
        RestfulResponse<ServicesRepresentation> servicesJsonResp = RestfulResponse.of(servicesResp, ServicesRepresentation.class);
        
        // then
        assertThat(servicesJsonResp.getStatus().getFamily(), is (Family.SUCCESSFUL));
        ServicesRepresentation servicesRepr = servicesJsonResp.getEntity();
        
        Link serviceReprRepTypeLink = servicesRepr.getRepresentationType();
        assertThat(serviceReprRepTypeLink.getHref(), matches(".*/representationTypes/list:object$"));

        Link serviceReprSelfLink = servicesRepr.getSelf();
        assertThat(serviceReprSelfLink, is(not(nullValue())));

        JsonRepresentation serviceValues = servicesRepr.xpath("/value/e[rel='service']");
        assertThat(serviceValues, is(not(nullValue())));
        assertThat(serviceValues.arraySize(), is(greaterThan(0)));

        assertThat(homePageRepr.getLinks(), is(not(nullValue())));
        assertThat(homePageRepr.getMetadata(), is(not(nullValue())));
    }

    @Test
    public void homePageResource_linksToUserResource() throws Exception {
        
        // given
        HomePageResource homePageResource = client.getHomePageResource();

        // when
        Response resourcesResp = homePageResource.resources();
        RestfulResponse<HomePageRepresentation> homePageJsonResp = RestfulResponse.of(resourcesResp, HomePageRepresentation.class);
        
        // then
        HomePageRepresentation homePageRepr = homePageJsonResp.getEntity();

        // and when
        Response userResp = client.follow(homePageRepr.getUser());
        RestfulResponse<UserRepresentation> userJsonResp = RestfulResponse.of(userResp, UserRepresentation.class);
        
        // then
        assertThat(userJsonResp.getStatus().getFamily(), is(Family.SUCCESSFUL));
        UserRepresentation userRepr = userJsonResp.getEntity();

        Link userReprRepTypeLink = userRepr.getRepresentationType();
        assertThat(userReprRepTypeLink.getHref(), matches(".*/representationTypes/user$"));

        assertThat(userRepr.getUserName(), is(not(nullValue())));
    }

    @Ignore("to get working again")
    @Test
    public void servicesResource_returnsServicesRepresentation() throws Exception {
        
        // given
        DomainServicesResource servicesResource = client.getServicesResource();
        
        // when
        Response servicesResp = servicesResource.services();
        RestfulResponse<ServicesRepresentation> servicesJsonResp = RestfulResponse.of(servicesResp, ServicesRepresentation.class);
        assertThat(servicesJsonResp.getStatus().getFamily(), is(Family.SUCCESSFUL));
        
        // then
        ServicesRepresentation servicesRepr = servicesJsonResp.getEntity();

        assertThat(servicesRepr, is(not(nullValue())));
        assertThat(servicesRepr.isMap(), is(true));

        assertThat(servicesRepr.getRepresentationType(), is(not(notNullValue())));
        // assertThat(servicesRepr.getSelf(), is(not(notNullValue()))); // TODO

        assertThat(servicesRepr.getString("title"), is("ApplibValues"));

        JsonRepresentation serviceValues = servicesRepr.xpath("/value/e[rel='service']");
        assertThat(serviceValues, is(not(nullValue())));
        assertThat(serviceValues.arraySize(), is(greaterThan(0)));

        Link serviceLink = serviceValues.elementAt(0).asLink();
        
        assertThat(serviceLink.getRel(), is("service"));
        assertThat(serviceLink.getHref(), matches("http://localhost:\\d+/services/.*$"));
    }


    @Ignore
    @Test
    public void servicesResource_linksToRepresentationType() throws Exception {
        
    }

    @Ignore
    @Test
    public void servicesResource_linksToSelf() throws Exception {
        
    }


    @Ignore("to get working again")
    @Test
    public void servicesResource_linksToDomainObjectResourceForService() throws Exception {
        
        // given
        DomainServicesResource servicesResource = client.getServicesResource();
        
        // when
        Response servicesResp = servicesResource.services();
        RestfulResponse<ServicesRepresentation> servicesJsonResp = RestfulResponse.of(servicesResp, ServicesRepresentation.class);
        
        // then
        ServicesRepresentation servicesRepr = servicesJsonResp.getEntity();

        JsonRepresentation repoRepr = servicesRepr.elementAt(0);
        Link repoObjLink = repoRepr.getLink("link");

        // and when
        Response repoFollowResp = client.follow(repoObjLink);
        RestfulResponse<DomainObjectRepresentation> repoFollowJsonResp = RestfulResponse.of(repoFollowResp, DomainObjectRepresentation.class);
        
        // then
        DomainObjectRepresentation domainObjectRepr = repoFollowJsonResp.getEntity();
        Link domainObjectReprLink = domainObjectRepr.getLink("_self.link");
        assertThat(domainObjectReprLink, is(repoObjLink));
    }


    @Ignore("to get working again")
    @Test
    public void domainObjectResource_returnsDomainObjectRepresentation() throws Exception {
        
        // given
        DomainObjectResource domainObjectResource = client.getDomainObjectResource();
        
        // when
        Response domainObjectResp = domainObjectResource.object("OID:1");
        RestfulResponse<DomainObjectRepresentation> domainObjectJsonResp = RestfulResponse.of(domainObjectResp, DomainObjectRepresentation.class);
        assertThat(domainObjectJsonResp.getStatus().getFamily(), is(Family.SUCCESSFUL));
        
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
        assertThat(selfType.getHref(), matches(".+" + ApplibValuedEntityRepositoryDefault.class.getName() + ".+"));
        assertThat(selfType.getMethod(), is(Method.GET));
        
        assertThat(domainObjectRepr.getString("_self.title"), is("ApplibValues"));
        assertThat(domainObjectRepr.getString("_self.oid"), is("OID:1"));

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
        assertThat(idProperty.getString("propertyId"), is("id"));
        assertThat(idProperty.getString("value"), is(org.apache.isis.tck.objstore.dflt.scalars.ApplibValuedEntityRepositoryDefault.class.getName()));
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
        assertThat(listAction.getString("actionId"), is("list"));
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
                ApplibValuedEntity.class.getName() +
        		"\\+json"));
        assertThat(newEntityActionType.getMethod(), is(Method.GET));

        Link newEntityActionDetails = newEntityAction.getLink("details");
        assertThat(newEntityActionDetails.getRel(), is("action"));
        assertThat(newEntityActionDetails.getHref(), is(selfLink.getHref() + "/actions/newEntity"));
        assertThat(newEntityActionDetails.getMethod(), is(Method.GET));
    }

    @Ignore("to get working again")
    @Test
    public void domainObjectResource_propertyDetails() throws Exception {
        // given
        DomainObjectResource domainObjectResource = client.getDomainObjectResource();
        
        // when
        Response idPropertyResp = domainObjectResource.propertyDetails("OID:1", "id");
        RestfulResponse<PropertyDetailsRepresentation> idPropertyJsonResp = RestfulResponse.of(idPropertyResp, PropertyDetailsRepresentation.class);
        assertThat(idPropertyJsonResp.getStatus().getFamily(), is(Family.SUCCESSFUL));
        
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
        assertThat(propertyDetailsRepr.getString("value"), is(org.apache.isis.tck.objstore.dflt.scalars.ApplibValuedEntityRepositoryDefault.class.getName()));
        assertThat(propertyDetailsRepr.getString("disabledReason"), is(not(nullValue())));
    }

    
    @Ignore("to get working again")
    @Test
    public void domainObjectResource_actionPrompt() throws Exception {
        // given
        DomainObjectResource domainObjectResource = client.getDomainObjectResource();
        
        // when
        Response actionPromptResp = domainObjectResource.actionPrompt("OID:1", "list");
        RestfulResponse<ActionPromptRepresentation> actionPromptJsonResp = RestfulResponse.of(actionPromptResp, ActionPromptRepresentation.class);
        assertThat(actionPromptJsonResp.getStatus().getFamily(), is(Family.SUCCESSFUL));
        
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
        assertThat(invokeLink.getArguments(), is(not(nullValue())));
        assertThat(invokeLink.getArguments().isArray(), is(true));
        assertThat(invokeLink.getArguments().arraySize(), is(0));
    }

    @Ignore("TODO")
    @Test
    public void domainObjectResource_collectionDetails() throws Exception {
        fail();
    }

    
    @Ignore("to get working again")
    @Test
    public void domainObjectResource_actionPostInvoke_returningList() throws Exception {
        
        // given
        DomainObjectResource domainObjectResource = client.getDomainObjectResource();
        
        JsonRepresentation body = JsonRepresentation.newArray();
        
        // when
        Response actionInvokeResp = domainObjectResource.invokeAction("OID:1", "list", body.asInputStream());
        RestfulResponse<ActionInvocationRepresentation> actionInvokeJsonResp = RestfulResponse.of(actionInvokeResp, ActionInvocationRepresentation.class);
        assertThat(actionInvokeJsonResp.getStatus().getFamily(), is(Family.SUCCESSFUL));
        
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
        		org.apache.isis.tck.dom.scalars.ApplibValuedEntity.class.getName() +
        		"\\+json"));

        Link domainObjectIconLink = domainObjectRefRepr.getLink("icon");
        assertThat(domainObjectIconLink.getRel(), is("icon"));
        assertThat(domainObjectIconLink.getHref(), matches("http://localhost:\\d+/images/null.png")); // TODO
    }
    

    @Ignore("to get working again")
    @Test
    public void walkResources() throws Exception {
    
        // given a response for an initial resource
        HomePageResource homePageResource = client.getHomePageResource();
        Response homePageResp = homePageResource.resources();
        
        // and given a walker starting from this response
        RepresentationWalker walker = client.createWalker(homePageResp);
        
        // when walk the home pages' 'services' link
        walker.walk("services");
        
        // and when locate the ApplibValues repo and walk the its 'object' link
        walker.walkXpath("/*[title='ApplibValues']/link[rel='object']");
        
        // and when locate the AppLibValues repo's "newEntity" action and walk to its details
        walker.walkXpath("/newEntity[memberType='action']/details");
        
        // and when find the invoke body for the "newEntity" action and then walk the action using the body 
        JsonRepresentation newEntityActionDetails = walker.getEntity();
        JsonRepresentation newEntityActionInvokeBody = newEntityActionDetails.getArray("invoke.body");
        walker.walkXpath("/invoke", newEntityActionInvokeBody);
        
        // and when walk the link to the returned object
        walker.walk("link");
        
        // then the returned object is created with its OID
        JsonRepresentation newEntityDomainObject = walker.getEntity();
        assertThat(newEntityDomainObject.getString("_self.link.href"), matches(".+/objects/OID:[\\d]+$")); 
    }

}
    