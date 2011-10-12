package org.apache.isis.viewer.json.tck.resources.home;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.apache.isis.runtimes.dflt.webserver.WebServer;
import org.apache.isis.viewer.json.applib.HttpMethod;
import org.apache.isis.viewer.json.applib.RestfulClient;
import org.apache.isis.viewer.json.applib.RestfulRequest;
import org.apache.isis.viewer.json.applib.RestfulRequest.QueryParameter;
import org.apache.isis.viewer.json.applib.RestfulResponse;
import org.apache.isis.viewer.json.applib.homepage.HomePageRepresentation;
import org.apache.isis.viewer.json.tck.IsisWebServerRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;


public class HomePageResourceTest_xrofollowlinks {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();
    
    private RestfulClient client;

    @Before
    public void setUp() throws Exception {
        WebServer webServer = webServerRule.getWebServer();
        client = new RestfulClient(webServer.getBase());
    }

    @Test
    public void xrofollowLinks() throws Exception {

        RestfulRequest request;
        RestfulResponse<HomePageRepresentation> restfulResponse;
        HomePageRepresentation repr;
        
        request = client.createRequest(HttpMethod.GET, "/");
        restfulResponse = request.executeT();
        repr = restfulResponse.getEntity();
        
        assertThat(repr.getUser().getValue(), is(nullValue()));
        assertThat(repr.getCapabilities().getValue(), is(nullValue()));
        assertThat(repr.getServices().getValue(), is(nullValue()));

        request = client.createRequest(HttpMethod.GET, "/").withArg(QueryParameter.FOLLOW_LINKS, "user,services,capabilities");
        restfulResponse = request.executeT();
        repr = restfulResponse.getEntity();

        assertThat(repr.getUser().getValue(), is(not(nullValue())));
        assertThat(repr.getCapabilities().getValue(), is(not(nullValue())));
        assertThat(repr.getServices().getValue(), is(not(nullValue())));
    }
    

}


    