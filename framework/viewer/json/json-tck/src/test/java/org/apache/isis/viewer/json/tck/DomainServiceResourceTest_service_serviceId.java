package org.apache.isis.viewer.json.tck;

import static org.apache.isis.viewer.json.tck.RepresentationMatchers.assertThat;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.isArray;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.isFollowableLinkToSelf;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.isLink;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.isMap;
import static org.apache.isis.core.commons.matchers.IsisMatchers.*;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.isis.runtimes.dflt.webserver.WebServer;
import org.apache.isis.viewer.json.applib.HttpStatusCode;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.applib.RestfulClient;
import org.apache.isis.viewer.json.applib.RestfulResponse;
import org.apache.isis.viewer.json.applib.blocks.Link;
import org.apache.isis.viewer.json.applib.blocks.Method;
import org.apache.isis.viewer.json.applib.domainobjects.DomainObjectRepresentation;
import org.apache.isis.viewer.json.applib.domainobjects.DomainServiceResource;
import org.apache.isis.viewer.json.applib.domainobjects.DomainServicesRepresentation;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;


public class DomainServiceResourceTest_service_serviceId {

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

        // given
        Response resp = resource.service("simples");
        
        // when
        RestfulResponse<DomainObjectRepresentation> jsonResp = RestfulResponse.of(resp, DomainObjectRepresentation.class);
        
        
        
        // then
        assertThat(jsonResp.getStatus(), is(HttpStatusCode.OK));
        assertThat(jsonResp.getHeader(RestfulResponse.Header.MEDIA_TYPE), is(MediaType.APPLICATION_JSON_TYPE));
        assertThat(jsonResp.getHeader(RestfulResponse.Header.CACHE_CONTROL).isNoCache(), is(true));
        assertThat(jsonResp.getHeader(RestfulResponse.Header.X_REPRESENTATION_TYPE), is(RepresentationType.DOMAIN_OBJECT));

        DomainObjectRepresentation repr = jsonResp.getEntity();

        assertThat(repr, isMap());

        assertThat(repr.getSelf(), isLink().method(Method.GET));
        assertThat(repr.getOid(), matches("OID[:].+"));
        assertThat(repr.getTitle(), matches("Simples"));

        assertThat(repr.getMembers(), isArray());
        
        assertThat(repr.getLinks(), isArray());
        assertThat(repr.getExtensions(), isMap());
    }


    @Test
    public void linksToSelf() throws Exception {
        // given
        DomainObjectRepresentation repr = givenRepresentation("simples");

        // when, then
        assertThat(repr, isFollowableLinkToSelf(client));
    }


    @Ignore("up to here")
    @Test
    public void linksToDomainServiceResources() throws Exception {
        
        // given
        DomainObjectRepresentation repr = givenRepresentation("simples");

    }

    @Test
    public void notFound() throws Exception {

        // when
        Response resp = resource.service("nonExistentServiceId");
        RestfulResponse<DomainObjectRepresentation> jsonResp = RestfulResponse.of(resp, DomainObjectRepresentation.class);
        
        // then
        assertThat(jsonResp.getStatus(), is(HttpStatusCode.NOT_FOUND));
    }


    private DomainObjectRepresentation givenRepresentation(String serviceId) throws JsonParseException, JsonMappingException, IOException {
        return RepresentationMatchers.entityOf(resource.service(serviceId), DomainObjectRepresentation.class);
    }


}
    