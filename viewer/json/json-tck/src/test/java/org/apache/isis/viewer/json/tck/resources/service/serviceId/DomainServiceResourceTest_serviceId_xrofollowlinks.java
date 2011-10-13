package org.apache.isis.viewer.json.tck.resources.service.serviceId;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.apache.isis.runtimes.dflt.webserver.WebServer;
import org.apache.isis.viewer.json.applib.HttpMethod;
import org.apache.isis.viewer.json.applib.RestfulClient;
import org.apache.isis.viewer.json.applib.RestfulRequest;
import org.apache.isis.viewer.json.applib.RestfulRequest.QueryParameter;
import org.apache.isis.viewer.json.applib.RestfulResponse;
import org.apache.isis.viewer.json.applib.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.json.applib.domainobjects.DomainServiceResource;
import org.apache.isis.viewer.json.applib.domainobjects.ListRepresentation;
import org.apache.isis.viewer.json.applib.domainobjects.ObjectActionRepresentation;
import org.apache.isis.viewer.json.tck.IsisWebServerRule;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class DomainServiceResourceTest_serviceId_xrofollowlinks {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();

    private RestfulClient client;

    @Before
    public void setUp() throws Exception {
        WebServer webServer = webServerRule.getWebServer();
        client = new RestfulClient(webServer.getBase());
    }

    @Test
    public void usingXpath() throws Exception {

        final String href = givenLinkToService();
        
        final RestfulRequest request = 
                client.createRequest(HttpMethod.GET, href).withArg(QueryParameter.FOLLOW_LINKS, "members[actionId='%s'].actionDetails", "list");
        final RestfulResponse<ObjectActionRepresentation> restfulResponse = request.executeT();

        assertThat(restfulResponse.getStatus(), is(HttpStatusCode.OK));
    }


    private String givenLinkToService() throws JsonParseException, JsonMappingException, IOException {
        final DomainServiceResource resource = client.getDomainServiceResource();
        final Response response = resource.services();
        final ListRepresentation services = RestfulResponse.<ListRepresentation>ofT(response).getEntity();

        return services.xpath("//*[key='%s']", "simples").getLink("e").getHref();
    }


}
