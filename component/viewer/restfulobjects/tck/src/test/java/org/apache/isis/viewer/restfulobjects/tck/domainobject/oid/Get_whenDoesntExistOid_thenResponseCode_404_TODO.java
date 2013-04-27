package org.apache.isis.viewer.restfulobjects.tck.domainobject.oid;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.webserver.WebServer;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.LinkRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectResource;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainServiceResource;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;
import org.apache.isis.viewer.restfulobjects.tck.Util;

public class Get_whenDoesntExistOid_thenResponseCode_404_TODO {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();

    protected RestfulClient client;

    private DomainObjectRepresentation domainObjectRepr;

    @Before
    public void setUp() throws Exception {
        final WebServer webServer = webServerRule.getWebServer();
        client = new RestfulClient(webServer.getBase());
    }

    @Test
    public void usingClientFollow() throws Exception {

        // given
        final LinkRepresentation link = Util.domainObjectLink(client, "PrimitiveValuedEntities");
        link.mapPut("href", "http://localhost:39393/objects/PRMV/nonExistent");
        
        // when
        final RestfulResponse<JsonRepresentation> restfulResp = client.follow(link);
        
        // then
        then(restfulResp);
        
    }

    @Test
    public void usingResourceProxy() throws Exception {

        // when
        final DomainObjectResource objectResource = client.getDomainObjectResource();

        final Response response = objectResource.object("PRMV", "nonExistent");
        RestfulResponse<JsonRepresentation> restfulResp = RestfulResponse.of(response);

        then(restfulResp);
        
    }
    
    private void then(final RestfulResponse<JsonRepresentation> restfulResp) {
        assertThat(restfulResp.getStatus(), is(HttpStatusCode.NOT_FOUND));
    }


}
