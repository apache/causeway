package org.apache.isis.viewer.json.tck.resources.service.invoke;

import static org.apache.isis.viewer.json.tck.RepresentationMatchers.isArray;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.apache.isis.runtimes.dflt.webserver.WebServer;
import org.apache.isis.viewer.json.applib.HttpMethod;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RestfulClient;
import org.apache.isis.viewer.json.applib.RestfulRequest;
import org.apache.isis.viewer.json.applib.RestfulResponse;
import org.apache.isis.viewer.json.applib.RestfulRequest.QueryParameter;
import org.apache.isis.viewer.json.applib.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.json.applib.blocks.Link;
import org.apache.isis.viewer.json.applib.domainobjects.DomainObjectRepresentation;
import org.apache.isis.viewer.json.applib.domainobjects.DomainServiceResource;
import org.apache.isis.viewer.json.applib.domainobjects.ListRepresentation;
import org.apache.isis.viewer.json.applib.domainobjects.ObjectActionRepresentation;
import org.apache.isis.viewer.json.tck.IsisWebServerRule;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;


public class DomainServiceResourceTest_invokeAction {

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
    public void invokeNoArg() throws Exception {
        
        JsonRepresentation givenAction = givenAction("simples", "list");
        final ObjectActionRepresentation actionRepr = givenAction.as(ObjectActionRepresentation.class);
        
        final Link invokeLink = actionRepr.getInvoke();
        assertThat(invokeLink, is(not(nullValue())));
        
        final Response response = client.follow(invokeLink);
        RestfulResponse<ListRepresentation> restfulResponse = RestfulResponse.ofT(response);
        final ListRepresentation listRepr = restfulResponse.getEntity();
        
        assertThat(listRepr.getValues().arraySize(), is(5));
    }


    private JsonRepresentation givenAction(final String serviceId, final String actionId) throws JsonParseException, JsonMappingException, IOException {
        final String href = givenHrefToService(serviceId);
        
        final RestfulRequest request = 
                client.createRequest(HttpMethod.GET, href).withArg(QueryParameter.FOLLOW_LINKS, "members[actionId=%s].actionDetails", actionId);
        final RestfulResponse<DomainObjectRepresentation> restfulResponse = request.executeT();

        assertThat(restfulResponse.getStatus(), is(HttpStatusCode.OK));
        final DomainObjectRepresentation repr = restfulResponse.getEntity();
        
        JsonRepresentation actionLinkRepr = repr.xpath("/members/e[actionId='%s']", actionId);
        return actionLinkRepr.getRepresentation("e.actionDetails.value");
    }


    private String givenHrefToService(String serviceId) throws JsonParseException, JsonMappingException, IOException {
        final DomainServiceResource resource = client.getDomainServiceResource();
        final Response response = resource.services();
        final ListRepresentation services = RestfulResponse.<ListRepresentation>ofT(response).getEntity();

        return services.xpath("//*[key='%s']", serviceId).getLink("e").getHref();
    }

}
    