package org.apache.isis.viewer.restfulobjects.tck.domainobjectorservice.id.action.invoke;

import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.assertThat;
import static org.apache.isis.viewer.restfulobjects.tck.RestfulMatchers.isLink;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.Response;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.LinkRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulHttpMethod;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ActionResultRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainServiceResource;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ObjectActionRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.util.JsonNodeUtils;
import org.apache.isis.viewer.restfulobjects.applib.util.UrlEncodingUtils;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;
import org.apache.isis.viewer.restfulobjects.tck.Util;

public class Post_thenRepresentation_doesNotContainSelfLink_ok {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();

    private RestfulClient client;

    private DomainServiceResource serviceResource;

    @Before
    public void setUp() throws Exception {
        client = webServerRule.getClient();

        serviceResource = client.getDomainServiceResource();
    }

    @Test
    public void usingClientFollow() throws Exception {

        // given
        final JsonRepresentation givenAction = Util.givenAction(client, "ActionsEntities", "findByIdNotIdempotent");
        final ObjectActionRepresentation actionRepr = givenAction.as(ObjectActionRepresentation.class);

        final LinkRepresentation invokeLink = actionRepr.getInvoke();
        final JsonRepresentation args =invokeLink.getArguments();
        
        // when
        args.mapPut("id.value", 1);

        // when
        final RestfulResponse<ActionResultRepresentation> restfulResponse = client.followT(invokeLink, args);
        
        // then
        then(restfulResponse);
    }

    

    @Test
    public void usingResourceProxy() throws Exception {

        // given, when
        final JsonRepresentation args = JsonRepresentation.newMap();
        args.mapPut("id.value", 1);

        final Response response = serviceResource.invokeAction("ActionsEntities", "findByIdNotIdempotent", JsonNodeUtils.asInputStream(args));
        final RestfulResponse<ActionResultRepresentation> restfulResponse = RestfulResponse.ofT(response);
        
        // then
        then(restfulResponse);
        
    }

    private void then(RestfulResponse<ActionResultRepresentation> restfulResponse) throws Exception {
        final ActionResultRepresentation actionResultRepr = restfulResponse.getEntity();
        assertThat(actionResultRepr.mapHas("links[rel=self]"), is(false));
    }

}
