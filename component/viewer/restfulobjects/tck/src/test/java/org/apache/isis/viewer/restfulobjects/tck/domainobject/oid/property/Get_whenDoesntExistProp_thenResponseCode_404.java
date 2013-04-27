package org.apache.isis.viewer.restfulobjects.tck.domainobject.oid.property;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.webserver.WebServer;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.LinkRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;
import org.apache.isis.viewer.restfulobjects.tck.Util;

public class Get_whenDoesntExistProp_thenResponseCode_404 {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();

    protected RestfulClient client;

    private LinkRepresentation link;

    @Before
    public void setUp() throws Exception {
        final WebServer webServer = webServerRule.getWebServer();
        client = new RestfulClient(webServer.getBase());
        
        link = new LinkRepresentation();
    }

    @Test
    public void whenPropertyDoesntExist() throws Exception {

        // given
        final LinkRepresentation linkToExistingObject = Util.domainObjectLink(client, "PrimitiveValuedEntities");
        link.withHref(linkToExistingObject.getHref() + "/properties/nonExistentProperty");
        
        // when
        final RestfulResponse<JsonRepresentation> restfulResp = client.follow(link);
        
        // then
        assertThat(restfulResp.getStatus(), is(HttpStatusCode.NOT_FOUND));
    }

}
