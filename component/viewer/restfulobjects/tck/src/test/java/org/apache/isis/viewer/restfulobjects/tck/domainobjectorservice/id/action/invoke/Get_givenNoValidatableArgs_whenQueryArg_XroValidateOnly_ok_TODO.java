package org.apache.isis.viewer.restfulobjects.tck.domainobjectorservice.id.action.invoke;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.webserver.WebServer;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;

public class Get_givenNoValidatableArgs_whenQueryArg_XroValidateOnly_ok_TODO {

    
    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();

    protected RestfulClient client;

    @Before
    public void setUp() throws Exception {
        final WebServer webServer = webServerRule.getWebServer();
        client = new RestfulClient(webServer.getBase());
    }

    @Ignore
    @Test
    public void success() throws Exception {
        // should return 204 (13.3)
    }

    @Ignore
    @Test
    public void failure() throws Exception {
        // should return 422, etc
    }

}
