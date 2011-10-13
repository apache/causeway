package org.apache.isis.viewer.json.tck.resources.service.serviceId;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.isis.runtimes.dflt.webserver.WebServer;
import org.apache.isis.viewer.json.applib.HttpMethod;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.applib.RestfulClient;
import org.apache.isis.viewer.json.applib.RestfulRequest;
import org.apache.isis.viewer.json.applib.RestfulResponse;
import org.apache.isis.viewer.json.applib.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.json.applib.domainobjects.DomainObjectRepresentation;
import org.apache.isis.viewer.json.applib.domainobjects.DomainServiceResource;
import org.apache.isis.viewer.json.applib.domainobjects.ListRepresentation;
import org.apache.isis.viewer.json.applib.homepage.HomePageRepresentation;
import org.apache.isis.viewer.json.tck.IsisWebServerRule;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.jboss.resteasy.spi.Link;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class DomainServiceResourceTest_serviceId_accept {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();

    private RestfulClient client;

    @Before
    public void setUp() throws Exception {
        WebServer webServer = webServerRule.getWebServer();
        client = new RestfulClient(webServer.getBase());
    }

    @Test
    public void applicationJson() throws Exception {

        final String href = givenLinkToService();
        
        final RestfulRequest request = 
                client.createRequest(HttpMethod.GET, href).withHeader(RestfulRequest.Header.ACCEPT, MediaType.APPLICATION_JSON_TYPE);
        final RestfulResponse<DomainObjectRepresentation> restfulResponse = request.executeT();

        assertThat(restfulResponse.getStatus(), is(HttpStatusCode.OK));
    }

    @Test
    public void applicationJson_profileList() throws Exception {

        final String href = givenLinkToService();

        final RestfulRequest request = 
                client.createRequest(HttpMethod.GET, href).withHeader(RestfulRequest.Header.ACCEPT, RepresentationType.DOMAIN_OBJECT.getMediaType());
        final RestfulResponse<DomainObjectRepresentation> restfulResponse = request.executeT();

        assertThat(restfulResponse.getStatus(), is(HttpStatusCode.OK));
    }

    @Test
    public void applicationJson_invalid() throws Exception {

        final String href = givenLinkToService();

        final RestfulRequest request = 
                client.createRequest(HttpMethod.GET, href).withHeader(RestfulRequest.Header.ACCEPT, RepresentationType.USER.getMediaType());
        final RestfulResponse<DomainObjectRepresentation> restfulResponse = request.executeT();

        assertThat(restfulResponse.getStatus(), is(HttpStatusCode.NOT_ACCEPTABLE));
    }

    private String givenLinkToService() throws JsonParseException, JsonMappingException, IOException {
        final DomainServiceResource resource = client.getDomainServiceResource();
        final Response response = resource.services();
        final ListRepresentation services = RestfulResponse.<ListRepresentation>ofT(response).getEntity();

        final String href = services.xpath("//*[key='%s']", "simples").getLink("e").getHref();
        return href;
    }


}
