package org.apache.isis.viewer.json.tck.resources.home;

import org.apache.isis.runtimes.dflt.webserver.WebServer;
import org.apache.isis.viewer.json.applib.HttpMethod;
import org.apache.isis.viewer.json.applib.RestfulClient;
import org.apache.isis.viewer.json.applib.RestfulRequest;
import org.apache.isis.viewer.json.applib.RestfulRequest.QueryParameter;
import org.apache.isis.viewer.json.applib.RestfulResponse;
import org.apache.isis.viewer.json.applib.homepage.HomePageRepresentation;
import org.apache.isis.viewer.json.applib.homepage.HomePageResource;
import org.apache.isis.viewer.json.tck.IsisWebServerRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;


public class HomePageResourceTest_xrofollowlinks {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();
    
    private RestfulClient client;
    private HomePageResource resource;

    @Before
    public void setUp() throws Exception {
        WebServer webServer = webServerRule.getWebServer();
        client = new RestfulClient(webServer.getBase());
        
        resource = client.getHomePageResource();
    }

    @Test
    public void xrofollowLinks() throws Exception {

        // TODO: this needs to be more generic
        final RestfulRequest request = client.createRequest(HttpMethod.GET, "/").withArg(QueryParameter.FOLLOW_LINKS, "user,services");
        final RestfulResponse<HomePageRepresentation> restfulResponse = request.executeT();
        
        
    }
    

}


    