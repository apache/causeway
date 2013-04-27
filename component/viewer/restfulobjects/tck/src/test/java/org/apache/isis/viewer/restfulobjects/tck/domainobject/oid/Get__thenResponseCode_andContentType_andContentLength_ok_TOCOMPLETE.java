package org.apache.isis.viewer.restfulobjects.tck.domainobject.oid;

import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.hasProfile;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.webserver.WebServer;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.LinkRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RestfulMediaType;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.Header;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectResource;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;
import org.apache.isis.viewer.restfulobjects.tck.Util;

public class Get__thenResponseCode_andContentType_andContentLength_ok_TOCOMPLETE {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();

    protected RestfulClient client;

    private DomainObjectResource domainObjectResource;

    @Before
    public void setUp() throws Exception {
        final WebServer webServer = webServerRule.getWebServer();
        client = new RestfulClient(webServer.getBase());
        domainObjectResource = client.getDomainObjectResource();
        
    }

    @Ignore("TODO")
    @Test
    public void usingClientFollow() throws Exception {

        // todo... same as test below
        final LinkRepresentation link = Util.domainObjectLink(client, "PrimitiveValuedEntities");
        final DomainObjectRepresentation objRepr = client.follow(link).getEntity().as(DomainObjectRepresentation.class);
        objRepr.getDomainType();
        objRepr.getInstanceId();
        
    }

    
    @Test
    public void usingResourceProxy() throws Exception {

        // when
        final LinkRepresentation link = Util.domainObjectLink(client, "PrimitiveValuedEntities");
        final DomainObjectRepresentation objRepr = client.follow(link).getEntity().as(DomainObjectRepresentation.class);
        final String domainType = objRepr.getDomainType();
        final String instanceId = objRepr.getInstanceId();
        
        final Response jaxrsResponse = domainObjectResource.object(domainType,instanceId);
        final RestfulResponse<DomainObjectRepresentation> restfulResponse = RestfulResponse.ofT(jaxrsResponse);
        assertThat(restfulResponse.getStatus(), is(HttpStatusCode.OK));

        // then
        assertThat(restfulResponse.getStatus(), is(HttpStatusCode.OK));
        assertThat(restfulResponse.getHeader(Header.CONTENT_TYPE), hasProfile(RestfulMediaType.APPLICATION_JSON_OBJECT));
        assertThat(restfulResponse.getHeader(Header.CONTENT_LENGTH), is(6382));
    }

}
