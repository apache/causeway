package org.apache.isis.viewer.json.tck.resources.service.invoke;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.apache.isis.applib.annotation.Ignore;
import org.apache.isis.runtimes.dflt.webserver.WebServer;
import org.apache.isis.viewer.json.applib.HttpMethod;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RestfulClient;
import org.apache.isis.viewer.json.applib.RestfulRequest;
import org.apache.isis.viewer.json.applib.RestfulRequest.QueryParameter;
import org.apache.isis.viewer.json.applib.RestfulResponse;
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
    public void invokeQueryOnly_noArg_usingClientFollow() throws Exception {

        // given
        JsonRepresentation givenAction = givenAction("simples", "list");
        final ObjectActionRepresentation actionRepr = givenAction.as(ObjectActionRepresentation.class);
        
        // when
        final Link invokeLink = actionRepr.getInvoke();
        
        // then
        assertThat(invokeLink, is(not(nullValue())));
        final Response response = client.follow(invokeLink);
        RestfulResponse<ListRepresentation> restfulResponse = RestfulResponse.ofT(response);
        final ListRepresentation listRepr = restfulResponse.getEntity();
        
        assertThat(listRepr.getValues().size(), is(5));
    }

    @Test
    public void invokeIdempotent_withArgs_usingClientFollow() throws Exception {

        // given
        JsonRepresentation givenAction = givenAction("simples", "newPersistentEntity");
        final ObjectActionRepresentation actionRepr = givenAction.as(ObjectActionRepresentation.class);
        
        // when
        final Link invokeLink = actionRepr.getInvoke();
        
        // then
        assertThat(invokeLink, is(not(nullValue())));
        
        final JsonRepresentation args = invokeLink.getArguments();
        assertThat(args.size(), is(2));
        assertThat(args.mapHas("name"), is(true));
        assertThat(args.mapHas("flag"), is(true));
        
        // when
        args.mapPut("name", "New Name");
        args.mapPut("flag", true);
        final Response response = client.follow(invokeLink, args);
        
        // then
        RestfulResponse<DomainObjectRepresentation> restfulResponse = RestfulResponse.ofT(response);
        final DomainObjectRepresentation objectRepr = restfulResponse.getEntity();
        
        assertThat(objectRepr.xpath("//members/e[propertyId='%s']/value", "name").getString("value"), is("New Name"));
        assertThat(objectRepr.xpath("//members/e[propertyId='%s']/value", "flag").getBoolean("value"), is(true));
    }

    @org.junit.Ignore("up to here")
    @Test
    public void invoke_withAllBuiltInArgs_usingClientFollow() throws Exception {

        // given
        JsonRepresentation givenAction = givenAction("simples", "newTransientEntity");
        final ObjectActionRepresentation actionRepr = givenAction.as(ObjectActionRepresentation.class);
        
        // when
        final Link invokeLink = actionRepr.getInvoke();
        
        // then
        assertThat(invokeLink, is(not(nullValue())));
        
        final JsonRepresentation args = invokeLink.getArguments();
        assertThat(args.size(), is(0));
        
        // when
        args.mapPut("name", "New Name");
        args.mapPut("flag", true);
        final Response response = client.follow(invokeLink, args);
        
        // then
        RestfulResponse<DomainObjectRepresentation> restfulResponse = RestfulResponse.ofT(response);
        final DomainObjectRepresentation objectRepr = restfulResponse.getEntity();
        
        assertThat(objectRepr.xpath("//members/e[propertyId='%s']/value", "name").getString("value"), is("New Name"));
        assertThat(objectRepr.xpath("//members/e[propertyId='%s']/value", "flag").getBoolean("value"), is(true));
    }


    private JsonRepresentation givenAction(final String serviceId, final String actionId) throws JsonParseException, JsonMappingException, IOException {
        final String href = givenHrefToService(serviceId);
        
        final RestfulRequest request = 
                client.createRequest(HttpMethod.GET, href).withArg(QueryParameter.FOLLOW_LINKS, "members[actionId=%s].details", actionId);
        final RestfulResponse<DomainObjectRepresentation> restfulResponse = request.executeT();

        assertThat(restfulResponse.getStatus(), is(HttpStatusCode.OK));
        final DomainObjectRepresentation repr = restfulResponse.getEntity();
        
        JsonRepresentation actionLinkRepr = repr.xpath("/members/e[actionId='%s']", actionId);
        return actionLinkRepr.getRepresentation("e.details.value");
    }


    private String givenHrefToService(String serviceId) throws JsonParseException, JsonMappingException, IOException {
        final DomainServiceResource resource = client.getDomainServiceResource();
        final Response response = resource.services();
        final ListRepresentation services = RestfulResponse.<ListRepresentation>ofT(response).getEntity();

        return services.xpath("//*[key='%s']", serviceId).getLink("e").getHref();
    }

}
    