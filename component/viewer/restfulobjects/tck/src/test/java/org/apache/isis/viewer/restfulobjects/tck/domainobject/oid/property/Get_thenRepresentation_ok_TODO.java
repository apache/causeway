package org.apache.isis.viewer.restfulobjects.tck.domainobject.oid.property;

import static org.apache.isis.core.commons.matchers.IsisMatchers.matches;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.tck.dom.scalars.ApplibValuedEntityRepository;
import org.apache.isis.core.webserver.WebServer;
import org.apache.isis.viewer.restfulobjects.applib.LinkRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RestfulHttpMethod;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectResource;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ObjectPropertyRepresentation;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;

public class Get_thenRepresentation_ok_TODO {

    
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

    
    @Ignore("to get working again")
    @Test
    public void propertyDetails() throws Exception {

        // when
        final Response idPropertyResp = domainObjectResource.propertyDetails("OID","1", "id");
        final RestfulResponse<ObjectPropertyRepresentation> idPropertyJsonResp = RestfulResponse.ofT(idPropertyResp);
        assertThat(idPropertyJsonResp.getStatus().getFamily(), is(Family.SUCCESSFUL));

        // then
        final ObjectPropertyRepresentation propertyDetailsRepr = idPropertyJsonResp.getEntity();

        // _self.link
        final LinkRepresentation selfLink = propertyDetailsRepr.getLink("_self.link");
        assertThat(selfLink.getRel(), is("member"));
        assertThat(selfLink.getHref(), matches(".+objects/OID:1/properties/id"));
        assertThat(selfLink.getHttpMethod(), is(RestfulHttpMethod.GET));

        // _self.object
        final LinkRepresentation selfObject = propertyDetailsRepr.getLink("_self.object");
        assertThat(selfObject.getRel(), is("object"));
        assertThat(selfObject.getHref(), matches(".+objects/OID:1"));
        assertThat(selfObject.getHttpMethod(), is(RestfulHttpMethod.GET));

        // type
        final LinkRepresentation type = propertyDetailsRepr.getLink("type");
        assertThat(type.getRel(), is("type"));
        assertThat(type.getHref(), matches(".+vnd\\.string\\+json"));
        assertThat(type.getHttpMethod(), is(RestfulHttpMethod.GET));

        assertThat(propertyDetailsRepr.getString("memberType"), is("property"));
        assertThat(propertyDetailsRepr.getString("value"), is(ApplibValuedEntityRepository.class.getName()));
        assertThat(propertyDetailsRepr.getString("disabledReason"), is(not(nullValue())));
    }
}
