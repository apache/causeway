package org.apache.isis.viewer.json.tck;

import static org.apache.isis.viewer.json.tck.RepresentationMatchers.assertThat;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.isArray;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.isFollowableLinkToSelf;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.isLink;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.isMap;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.isis.runtimes.dflt.webserver.WebServer;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.applib.RestfulClient;
import org.apache.isis.viewer.json.applib.RestfulResponse;
import org.apache.isis.viewer.json.applib.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.json.applib.blocks.Link;
import org.apache.isis.viewer.json.applib.blocks.Method;
import org.apache.isis.viewer.json.applib.domainobjects.DomainServiceResource;
import org.apache.isis.viewer.json.applib.domainobjects.ListRepresentation;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;


public class DomainServiceResourceTest_services {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();
    
    private RestfulClient client;
    private DomainServiceResource resource;

    @Before
    public void setUp() throws Exception {
        WebServer webServer = webServerRule.getWebServer();
        client = new RestfulClient(webServer.getBase());
        
        resource = client.getDomainServiceResource();
    }


    @Test
    public void representation() throws Exception {
        
        // when
        Response resp = resource.services();
        RestfulResponse<ListRepresentation> jsonResp = RestfulResponse.ofT(resp);
        
        // then
        assertThat(jsonResp.getStatus(), is(HttpStatusCode.OK));
        assertThat(jsonResp.getHeader(RestfulResponse.Header.CONTENT_TYPE), is(RepresentationType.LIST.getMediaType()));
        assertThat(jsonResp.getHeader(RestfulResponse.Header.CACHE_CONTROL).getMaxAge(), is(24*60*60));

        ListRepresentation repr = jsonResp.getEntity();

        assertThat(repr, isMap());

        assertThat(repr.getSelf(), isLink().method(Method.GET));
        
        assertThat(repr.getValues(), isArray());
        
        assertThat(repr.getLinks(), isArray());
        assertThat(repr.getExtensions(), isMap());
    }


    @Test
    public void self_isFollowable() throws Exception {
        // given
        ListRepresentation repr = givenRepresentation();

        // when, then
        assertThat(repr, isFollowableLinkToSelf(client));
    }


    @Test
    public void linksToDomainServiceResources() throws Exception {
        
        // given
        ListRepresentation repr = givenRepresentation();
        
        // when
        JsonRepresentation values = repr.getValues();
        
        // then
        for (Link link : values.arrayIterable(Link.class)) {
            Response followResp = client.follow(link);
            RestfulResponse<JsonRepresentation> followJsonResp = RestfulResponse.of(followResp);
            assertThat(followJsonResp.getStatus().getFamily(), is(Family.SUCCESSFUL));
            
            JsonRepresentation followRepr = followJsonResp.getEntity();
            Link self = followRepr.getLink("self");
            
            assertThat(self.getHref(), is(link.getHref()));
        }
    }


    private ListRepresentation givenRepresentation() throws JsonParseException, JsonMappingException, IOException {
        return RepresentationMatchers.entityOf(resource.services(), ListRepresentation.class);
    }


}
    