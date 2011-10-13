package org.apache.isis.viewer.json.tck.resources.service.services;

import static org.apache.isis.viewer.json.tck.RepresentationMatchers.assertThat;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.isArray;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.isLink;
import static org.apache.isis.viewer.json.tck.RepresentationMatchers.isMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import org.apache.isis.runtimes.dflt.webserver.WebServer;
import org.apache.isis.viewer.json.applib.HttpMethod;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RestfulClient;
import org.apache.isis.viewer.json.applib.RestfulRequest;
import org.apache.isis.viewer.json.applib.RestfulRequest.QueryParameter;
import org.apache.isis.viewer.json.applib.RestfulResponse;
import org.apache.isis.viewer.json.applib.domainobjects.ListRepresentation;
import org.apache.isis.viewer.json.tck.IsisWebServerRule;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;


public class DomainServiceResourceTest_services_xrofollowlinks {

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
        RestfulResponse<ListRepresentation> restfulResponse;
        ListRepresentation repr;
        
        request = client.createRequest(HttpMethod.GET, "services");
        restfulResponse = request.executeT();
        repr = restfulResponse.getEntity();
        
        assertThat(repr.getValues(), isArray());
        assertThat(repr.getValues().arraySize(), is(greaterThan(0)));
        assertThat(repr.getValues().arrayGet(0), isLink().novalue());

        request = client.createRequest(HttpMethod.GET, "services").withArg(QueryParameter.FOLLOW_LINKS, "values");
        restfulResponse = request.executeT();
        repr = restfulResponse.getEntity();

        assertThat(repr.getValues().arrayGet(0), isLink().value(is(not(Matchers.<JsonRepresentation>nullValue()))));
        assertThat(repr.getValues().arrayGet(0).getRepresentation("value"), isMap());
    }
    

}


    