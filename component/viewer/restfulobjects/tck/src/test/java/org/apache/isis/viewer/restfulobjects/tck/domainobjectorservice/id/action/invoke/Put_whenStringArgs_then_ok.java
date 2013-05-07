package org.apache.isis.viewer.restfulobjects.tck.domainobjectorservice.id.action.invoke;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.LinkRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ActionResultRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainServiceResource;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ObjectActionRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ScalarValueRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.errors.ErrorRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.util.JsonNodeUtils;
import org.apache.isis.viewer.restfulobjects.tck.IsisWebServerRule;
import org.apache.isis.viewer.restfulobjects.tck.Util;

public class Put_whenStringArgs_then_ok {

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
        final JsonRepresentation givenAction = Util.givenAction(client, "ActionsEntities", "concatenate");
        final ObjectActionRepresentation actionRepr = givenAction.as(ObjectActionRepresentation.class);

        final LinkRepresentation invokeLink = actionRepr.getInvoke();
        final JsonRepresentation args =invokeLink.getArguments();
        
        // when
        args.mapPut("str1.value", "IVA VENDITE 21%");
        args.mapPut("str2.value", "AAA");

        // when
        final RestfulResponse<ActionResultRepresentation> restfulResponse = client.followT(invokeLink, args);
        
        // then
        then(restfulResponse);
    }

    

    @Test
    public void usingResourceProxy() throws Exception {

        // given, when
        final JsonRepresentation args = JsonRepresentation.newMap();
        args.mapPut("str1.value", "IVA VENDITE 21%");
        args.mapPut("str2.value", "AAA");

        final Response response = serviceResource.invokeActionIdempotent("ActionsEntities", "concatenate", JsonNodeUtils.asInputStream(args));
        final RestfulResponse<ActionResultRepresentation> restfulResponse = RestfulResponse.ofT(response);
        
        // then
        then(restfulResponse);
    }

    private void then(RestfulResponse<ActionResultRepresentation> restfulResponse) throws Exception {
        assertThat(restfulResponse.getStatus(), is(HttpStatusCode.OK));
        final ActionResultRepresentation entity = restfulResponse.getEntity();
        final ScalarValueRepresentation svr = entity.getResult().as(ScalarValueRepresentation.class);
        assertThat(svr.getValue().asString(), is("IVA VENDITE 21%AAA"));
    }

}
