package org.apache.isis.viewer.json.tck;

import static org.apache.isis.viewer.json.tck.RepresentationMatchers.entityOf;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.isLink;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.isFollowableLinkToSelf;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.isis.runtimes.dflt.webserver.WebServer;
import org.apache.isis.viewer.json.applib.HttpStatusCode;
import org.apache.isis.viewer.json.applib.RestfulClient;
import org.apache.isis.viewer.json.applib.RestfulResponse;
import org.apache.isis.viewer.json.applib.blocks.Method;
import org.apache.isis.viewer.json.applib.homepage.HomePageRepresentation;
import org.apache.isis.viewer.json.applib.homepage.HomePageResource;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;


public class HomePageResourceTest {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();
    
    private RestfulClient client;
    private HomePageResource resource;

    @Before
    public void setUp() throws Exception {
        WebServer webServer = webServerRule.getWebServer();
        client = new RestfulClient(webServer.getBase());
        
        resource = client.getHomePageResource();
    }

    @org.junit.Ignore("to get working")
    @Test
    public void returnsHomePageRepresentation() throws Exception {

        // given
        Response resourcesResp = resource.resources();
        
        // when
        RestfulResponse<HomePageRepresentation> homePageJsonResp = RestfulResponse.of(resourcesResp, HomePageRepresentation.class);
        assertThat(homePageJsonResp.getStatus().getFamily(), is(Family.SUCCESSFUL));
        
        // then
        assertThat(homePageJsonResp.getStatus(), is(HttpStatusCode.OK));
        
        HomePageRepresentation homePageRepr = homePageJsonResp.getEntity();
        assertThat(homePageRepr, is(not(nullValue())));
        assertThat(homePageRepr.isMap(), is(true));
        
        assertThat(homePageRepr.getSelf(), isLink(client).method(Method.GET));
        assertThat(homePageRepr.getUser(), isLink(client).method(Method.GET));
        assertThat(homePageRepr.getServices(), isLink(client).method(Method.GET));
        assertThat(homePageRepr.getCapabilities(), isLink(client).method(Method.GET));
        
        assertThat(homePageRepr.getLinks(), is(not(nullValue())));
        assertThat(homePageRepr.getMetadata(), is(not(nullValue())));
    }

    @Test
    public void linksToSelf() throws Exception {
        // given
        HomePageRepresentation homePageRepr = givenRepresentation();

        // when, then
        assertThat(homePageRepr, isFollowableLinkToSelf(client));
    }
    
    @org.junit.Ignore("to get working")
    @Test
    public void links() throws Exception {
        
        HomePageRepresentation homePageRepr = givenRepresentation();

        // when, then
        assertThat(homePageRepr.getServices(), isLink(client).returning(HttpStatusCode.OK));
        assertThat(homePageRepr.getUser(), isLink(client).returning(HttpStatusCode.OK));
        assertThat(homePageRepr.getCapabilities(), isLink(client).returning(HttpStatusCode.OK));
    }

    private HomePageRepresentation givenRepresentation() throws JsonParseException, JsonMappingException, IOException {
        return entityOf(resource.resources(), HomePageRepresentation.class);
    }

    //
}


    