package org.apache.isis.viewer.json.tck;

import static org.apache.isis.core.commons.matchers.IsisMatchers.greaterThan;
import static org.apache.isis.core.commons.matchers.IsisMatchers.matches;
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
import org.apache.isis.viewer.json.applib.RestfulClient;
import org.apache.isis.viewer.json.applib.RestfulResponse;
import org.apache.isis.viewer.json.applib.blocks.Link;
import org.apache.isis.viewer.json.applib.blocks.Method;
import org.apache.isis.viewer.json.applib.domainobjects.DomainObjectRepresentation;
import org.apache.isis.viewer.json.applib.domainobjects.DomainServicesRepresentation;
import org.apache.isis.viewer.json.applib.domainobjects.DomainServicesResource;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;


public class DomainServiceResourceTest {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();
    
    private RestfulClient client;
    private DomainServicesResource resource;

    @Before
    public void setUp() throws Exception {
        WebServer webServer = webServerRule.getWebServer();
        client = new RestfulClient(webServer.getBase());
        
        resource = client.getDomainServicesResource();
    }


    @Ignore("to get working again")
    @Test
    public void returnsServicesRepresentation() throws Exception {
        

        // when
        Response servicesResp = resource.services();
        RestfulResponse<DomainServicesRepresentation> servicesJsonResp = RestfulResponse.of(servicesResp, DomainServicesRepresentation.class);
        assertThat(servicesJsonResp.getStatus().getFamily(), is(Family.SUCCESSFUL));
        
        // then
        DomainServicesRepresentation servicesRepr = servicesJsonResp.getEntity();

        assertThat(servicesRepr, isMap());

        assertThat(servicesRepr.getSelf(), isLink().method(Method.GET));

        assertThat(servicesRepr.getString("title"), is("ApplibValues"));
        
        JsonRepresentation serviceValues = servicesRepr.xpath("/value/e[rel='service']");
        assertThat(serviceValues, isArray());
        assertThat(serviceValues.arraySize(), is(greaterThan(0)));

        Link serviceLink = serviceValues.elementAt(0).asLink();
        assertThat(serviceLink, isLink().rel("service").href(matches("http://localhost:\\d+/services/.*$")).method(Method.GET));
    }



    @org.junit.Ignore("to get working")
    @Test
    public void linksToSelf() throws Exception {
        // given
        DomainServicesRepresentation servicesRepr = givenRepresentation();

        // when, then
        assertThat(servicesRepr, isFollowableLinkToSelf(client));
    }


    @Ignore("to get working again")
    @Test
    public void linksToDomainServiceResources() throws Exception {
        
        // given
        DomainServicesRepresentation servicesRepr = givenRepresentation();

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


    private DomainServicesRepresentation givenRepresentation() throws JsonParseException, JsonMappingException, IOException {
        return RepresentationMatchers.entityOf(resource.services(), DomainServicesRepresentation.class);
    }


}
    