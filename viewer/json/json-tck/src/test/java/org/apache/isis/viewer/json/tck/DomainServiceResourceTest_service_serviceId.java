package org.apache.isis.viewer.json.tck;

import static org.apache.isis.core.commons.matchers.IsisMatchers.matches;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.assertThat;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.isArray;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.isFollowableLinkToSelf;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.isLink;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.isMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.apache.isis.runtimes.dflt.webserver.WebServer;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.applib.RestfulClient;
import org.apache.isis.viewer.json.applib.RestfulResponse;
import org.apache.isis.viewer.json.applib.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.json.applib.blocks.Link;
import org.apache.isis.viewer.json.applib.blocks.Method;
import org.apache.isis.viewer.json.applib.domainobjects.DomainObjectRepresentation;
import org.apache.isis.viewer.json.applib.domainobjects.DomainServiceResource;
import org.apache.isis.viewer.json.applib.domainobjects.ObjectActionRepresentation;
import org.apache.isis.viewer.json.viewer.resources.domainobjects.MemberType;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;


public class DomainServiceResourceTest_service_serviceId {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();
    
    private RestfulClient client;
    private DomainServiceResource resource;

    @Before
    public void setUp() throws Exception {
        WebServer webServer = webServerRule.getWebServer();
        client = new RestfulClient(webServer.getBase());
        
        resource = client.getDomainServiceResource();
    }


    @Test
    public void representation() throws Exception {

        // given
        Response resp = resource.service("simples");
        
        // when
        RestfulResponse<DomainObjectRepresentation> jsonResp = RestfulResponse.ofT(resp);
        
        // then
        assertThat(jsonResp.getStatus(), is(HttpStatusCode.OK));
        assertThat(jsonResp.getHeader(RestfulResponse.Header.CONTENT_TYPE), is(RepresentationType.DOMAIN_OBJECT.getMediaType()));
        assertThat(jsonResp.getHeader(RestfulResponse.Header.CACHE_CONTROL).getMaxAge(), is(24*60*60));

        DomainObjectRepresentation repr = jsonResp.getEntity();

        assertThat(repr, isMap());

        assertThat(repr.getSelf(), isLink().method(Method.GET));
        assertThat(repr.getOid(), matches("OID[:].+"));
        assertThat(repr.getTitle(), matches("Simples"));

        assertThat(repr.getMembers(), isArray());
        
        assertThat(repr.getLinks(), isArray());
        assertThat(repr.getExtensions(), isMap());
    }


    @Test
    public void self_isFollowable() throws Exception {
        // given
        DomainObjectRepresentation repr = givenRepresentation("simples");

        // when, then
        assertThat(repr, isFollowableLinkToSelf(client));
    }


    @Ignore("broken - need to duplicate logic in DSRS")
    @Test
    public void members_actions() throws Exception {
        // given
        DomainObjectRepresentation repr = givenRepresentation("simples");

        JsonRepresentation actions = repr.xpath("/members/e[memberType='objectAction']");
        assertThat(actions.arraySize(), is(3));
        for (ObjectActionRepresentation memberRepr : actions.arrayIterable(ObjectActionRepresentation.class)) {
            assertThat(memberRepr.getMemberType(), is(not(nullValue())));
            assertThat(MemberType.lookup(memberRepr.getMemberType()), is(MemberType.OBJECT_ACTION));
            assertThat(memberRepr.getActionId(), is(not(nullValue())));
            assertThat(memberRepr.getActionDetails(), isLink());
            assertThat(memberRepr.getDisabledReason(), is(nullValue()));
        }
        
        JsonRepresentation listActionRepr = repr.xpath("/members/e[memberType='objectAction' and actionId='list']").getRepresentation("e");
        Link listActionDetailsLink = listActionRepr.getLink("actionDetails");
        
        // when
        Response listActionDetailsResp = client.follow(listActionDetailsLink);
        
        // then
        RestfulResponse<ObjectActionRepresentation> listActionDetailsJsonResp = RestfulResponse.ofT(listActionDetailsResp);
        assertThat(listActionDetailsJsonResp.getStatus(), is(HttpStatusCode.OK));

        ObjectActionRepresentation listActionDetailsRepr = listActionDetailsJsonResp.getEntity();
        
        
    }

    @Ignore("TODO - need to add fixture data")
    @Test
    public void members_actions_disabled() throws Exception {
        
    }

    @Test
    public void links_noIcons() throws Exception {
        // given, when
        DomainObjectRepresentation repr = givenRepresentation("simples");

        // then
        assertThat(repr.getLinks().arraySize(), is(0));
    }
    

    @Ignore("TODO - need to add fixture data")
    @Test
    public void links_icons() throws Exception {
        
    }
    

    @Test
    public void notFound() throws Exception {

        // when
        Response resp = resource.service("nonExistentServiceId");
        RestfulResponse<JsonRepresentation> jsonResp = RestfulResponse.of(resp);
        
        // then
        assertThat(jsonResp.getStatus(), is(HttpStatusCode.NOT_FOUND));
    }


    private DomainObjectRepresentation givenRepresentation(String serviceId) throws JsonParseException, JsonMappingException, IOException {
        return RepresentationMatchers.entityOf(resource.service(serviceId), DomainObjectRepresentation.class);
    }


}
    