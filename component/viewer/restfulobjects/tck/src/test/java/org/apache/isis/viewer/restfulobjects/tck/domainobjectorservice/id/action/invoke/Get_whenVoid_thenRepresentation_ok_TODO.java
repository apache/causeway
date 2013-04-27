package org.apache.isis.viewer.restfulobjects.tck.domainobjectorservice.id.action.invoke;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.webserver.WebServer;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;

public class Get_whenVoid_thenRepresentation_ok_TODO {

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
    public void todo() throws Exception {
        
    }
}
