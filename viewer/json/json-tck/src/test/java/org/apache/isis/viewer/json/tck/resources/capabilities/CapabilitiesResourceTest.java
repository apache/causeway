package org.apache.isis.viewer.json.tck.resources.capabilities;

import static org.apache.isis.viewer.json.tck.RepresentationMatchers.assertThat;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.isFollowableLinkToSelf;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.isLink;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.isMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.apache.isis.runtimes.dflt.webserver.WebServer;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RestfulClient;
import org.apache.isis.viewer.json.applib.RestfulResponse;
import org.apache.isis.viewer.json.applib.blocks.Method;
import org.apache.isis.viewer.json.applib.capabilities.CapabilitiesRepresentation;
import org.apache.isis.viewer.json.applib.capabilities.CapabilitiesResource;
import org.apache.isis.viewer.json.tck.IsisWebServerRule;
import org.apache.isis.viewer.json.tck.RepresentationMatchers;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;


public class CapabilitiesResourceTest {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();
    
    private RestfulClient client;
    private CapabilitiesResource resource;

    @Before
    public void setUp() throws Exception {
        WebServer webServer = webServerRule.getWebServer();
        client = new RestfulClient(webServer.getBase());
        
        resource = client.getCapabilitiesResource();
    }


    @Test
    public void representation() throws Exception {
        
        // when
        Response servicesResp = resource.capabilities();
        RestfulResponse<CapabilitiesRepresentation> jsonResp = RestfulResponse.ofT(servicesResp);
        assertThat(jsonResp.getStatus().getFamily(), is(Family.SUCCESSFUL));
        
        // then
        CapabilitiesRepresentation repr = jsonResp.getEntity();
        assertThat(repr, isMap());

        assertThat(repr.getSelf(), isLink().method(Method.GET));

        JsonRepresentation capabilities = repr.getCapabilities();
        assertThat(capabilities, isMap());
        
        assertThat(capabilities.getString("concurrencyChecking"), is("no"));
        assertThat(capabilities.getString("transientObjects"), is("no"));
        assertThat(capabilities.getString("deleteObjects"), is("no"));
        assertThat(capabilities.getString("simpleArguments"), is("no"));
        assertThat(capabilities.getString("partialArguments"), is("no"));
        assertThat(capabilities.getString("followLinks"), is("no"));
        assertThat(capabilities.getString("validateOnly"), is("no"));
        assertThat(capabilities.getString("pagination"), is("no"));
        assertThat(capabilities.getString("sorting"), is("no"));
        assertThat(capabilities.getString("domainModel"), is("rich"));
        
        assertThat(repr.getLinks(), is(not(nullValue())));
        assertThat(repr.getExtensions(), is(not(nullValue())));
    }


    @Test
    public void selfIsFollowable() throws Exception {
        // given
        CapabilitiesRepresentation repr = givenRepresentation();

        // when, then
        assertThat(repr, isFollowableLinkToSelf(client));
    }


    private CapabilitiesRepresentation givenRepresentation() throws JsonParseException, JsonMappingException, IOException {
        RestfulResponse<CapabilitiesRepresentation> jsonResp = RestfulResponse.ofT(resource.capabilities());
        return jsonResp.getEntity();
    }


}
    