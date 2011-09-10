package org.apache.isis.viewer.json.tck;

import java.io.IOException;

import org.apache.isis.runtimes.dflt.webserver.WebServer;
import org.apache.isis.viewer.json.applib.RestfulClient;
import org.apache.isis.viewer.json.applib.domainobjects.DomainObjectRepresentation;
import org.apache.isis.viewer.json.applib.domainobjects.DomainServiceResource;
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


    // adding this just to be able to locate a domain object directly.
    @Ignore
    @Test
    public void happyCase() throws Exception {
        DomainObjectRepresentation simplesService = givenRepresentation("simples");
        
        
    }


    private DomainObjectRepresentation givenRepresentation(String serviceId) throws JsonParseException, JsonMappingException, IOException {
        return RepresentationMatchers.entityOf(resource.service(serviceId), DomainObjectRepresentation.class);
    }


}
    