package org.apache.isis.viewer.json.tck.resources.service.serviceId;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.ws.rs.core.Response;

import org.apache.isis.runtimes.dflt.webserver.WebServer;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RestfulClient;
import org.apache.isis.viewer.json.applib.RestfulResponse;
import org.apache.isis.viewer.json.applib.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.json.applib.domainobjects.DomainServiceResource;
import org.apache.isis.viewer.json.tck.IsisWebServerRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;


public class DomainServiceResourceTest_serviceId_notFound {

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
    public void notFound() throws Exception {

        // when
        Response resp = resource.service("nonExistentServiceId");
        RestfulResponse<JsonRepresentation> jsonResp = RestfulResponse.of(resp);
        
        // then
        assertThat(jsonResp.getStatus(), is(HttpStatusCode.NOT_FOUND));
    }


}
    