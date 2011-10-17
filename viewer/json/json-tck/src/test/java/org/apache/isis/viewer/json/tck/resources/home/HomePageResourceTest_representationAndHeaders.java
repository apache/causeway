package org.apache.isis.viewer.json.tck.resources.home;

import static org.apache.isis.viewer.json.tck.RepresentationMatchers.assertThat;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.hasMaxAge;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.hasParameter;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.hasSubType;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.hasType;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.isArray;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.isFollowableLinkToSelf;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.isLink;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.isMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.isis.runtimes.dflt.webserver.WebServer;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.applib.RestfulClient;
import org.apache.isis.viewer.json.applib.RestfulResponse;
import org.apache.isis.viewer.json.applib.RestfulResponse.Header;
import org.apache.isis.viewer.json.applib.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.json.applib.blocks.Method;
import org.apache.isis.viewer.json.applib.homepage.HomePageRepresentation;
import org.apache.isis.viewer.json.applib.homepage.HomePageResource;
import org.apache.isis.viewer.json.tck.IsisWebServerRule;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;


public class HomePageResourceTest_representationAndHeaders {

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

    @Test
    public void representation() throws Exception {

        // given
        Response resp = resource.homePage();
        
        // when
        RestfulResponse<HomePageRepresentation> restfulResponse = RestfulResponse.ofT(resp);
        assertThat(restfulResponse.getStatus().getFamily(), is(Family.SUCCESSFUL));
        
        // then
        assertThat(restfulResponse.getStatus(), is(HttpStatusCode.OK));
        
        HomePageRepresentation repr = restfulResponse.getEntity();
        assertThat(repr, is(not(nullValue())));
        assertThat(repr, isMap());
        
        assertThat(repr.getSelf(), isLink(client).method(Method.GET));
        assertThat(repr.getUser(), isLink(client).method(Method.GET));
        assertThat(repr.getServices(), isLink(client).method(Method.GET));
        assertThat(repr.getCapabilities(), isLink(client).method(Method.GET));
        
        assertThat(repr.getLinks(), isArray());
        assertThat(repr.getExtensions(), isMap());
    }

    @Test
    public void headers() throws Exception {
        // given
        Response resp = resource.homePage();
        
        // when
        RestfulResponse<HomePageRepresentation> restfulResponse = RestfulResponse.ofT(resp);
        
        // then
        final MediaType contentType = restfulResponse.getHeader(Header.CONTENT_TYPE);
        assertThat(contentType, hasType("application"));
        assertThat(contentType, hasSubType("json"));
        assertThat(contentType, hasParameter("profile", "urn:org.restfulobjects/homepage"));
        assertThat(contentType, is(RepresentationType.HOME_PAGE.getMediaType()));
        
        // then
        final CacheControl cacheControl = restfulResponse.getHeader(Header.CACHE_CONTROL);
        assertThat(cacheControl, hasMaxAge(24*60*60));
        assertThat(cacheControl.getMaxAge(), is(24*60*60));
    }

    @Test
    public void self_isFollowable() throws Exception {
        // given
        HomePageRepresentation repr = givenRepresentation();

        // when, then
        assertThat(repr, isFollowableLinkToSelf(client));
    }
    
    @Test
    public void links() throws Exception {
        // given
        HomePageRepresentation repr = givenRepresentation();

        // when, then
        assertThat(repr.getServices(), isLink(client).returning(HttpStatusCode.OK));
        assertThat(repr.getUser(), isLink(client).returning(HttpStatusCode.OK));
        assertThat(repr.getCapabilities(), isLink(client).returning(HttpStatusCode.OK));
    }

    private HomePageRepresentation givenRepresentation() throws JsonParseException, JsonMappingException, IOException {
        RestfulResponse<HomePageRepresentation> response = RestfulResponse.ofT(resource.homePage());
        return response.getEntity();
    }

}


    