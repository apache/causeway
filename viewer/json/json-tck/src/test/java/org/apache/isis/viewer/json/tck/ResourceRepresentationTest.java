package org.apache.isis.viewer.json.tck;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import javax.ws.rs.core.Response;

import org.apache.isis.runtimes.dflt.webserver.WebServer;
import org.apache.isis.viewer.json.applib.RestfulClient;
import org.apache.isis.viewer.json.applib.blocks.Method;
import org.apache.isis.viewer.json.applib.domain.DomainObjectRepresentation;
import org.apache.isis.viewer.json.applib.domain.ServicesRepresentation;
import org.apache.isis.viewer.json.applib.domain.ServicesResource;
import org.apache.isis.viewer.json.applib.homepage.HomePageRepresentation;
import org.apache.isis.viewer.json.applib.homepage.HomePageResource;
import org.apache.isis.viewer.json.applib.util.HttpStatusCode;
import org.apache.isis.viewer.json.applib.util.JsonResponse;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class ResourceRepresentationTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        webServer = new WebServer();
        webServer.run(39393);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        webServer.stop();
    }

    protected static WebServer webServer;
    protected RestfulClient client;

    @Before
    public void setUp() throws Exception {
        client = new RestfulClient(webServer.getBase());
    }


    @Test
    public void homePageResource_returnsHomePageRepresentation() throws Exception {
        // given
        HomePageResource homePageResource = client.getHomePageResource();
        
        // when
        Response resourcesResp = homePageResource.resources();
        JsonResponse<HomePageRepresentation> homePageJsonResp = JsonResponse.of(resourcesResp, HomePageRepresentation.class);
        
        // then
        assertThat(homePageJsonResp.getStatus(), is(HttpStatusCode.OK));
        
        HomePageRepresentation homePageRep = homePageJsonResp.getEntity();
        assertThat(homePageRep, is(not(nullValue())));
        assertThat(homePageRep.getUser().getMethod(), is(Method.GET));
        
        assertThat(homePageRep.getServices(), is(not(nullValue())));
        assertThat(homePageRep.getServices().getMethod(), is(Method.GET));
    }

    @Test
    public void homePageResource_linksToServicesResource() throws Exception {
        
        // given
        HomePageResource homePageResource = client.getHomePageResource();

        // when
        Response resourcesResp = homePageResource.resources();
        JsonResponse<HomePageRepresentation> homePageJsonResp = JsonResponse.of(resourcesResp, HomePageRepresentation.class);
        
        // then
        HomePageRepresentation homePageRepr = homePageJsonResp.getEntity();

        // and when
        Response servicesResp = client.follow(homePageRepr.getServices());
        JsonResponse<ServicesRepresentation> servicesJsonResp = JsonResponse.of(servicesResp, ServicesRepresentation.class);
        
        // then
        ServicesRepresentation servicesRepr = servicesJsonResp.getEntity();
        
        DomainObjectRepresentation applibValuesEntityRepoRep = servicesRepr.get(0);
        assertThat(applibValuesEntityRepoRep, is(not(nullValue())));
    }

    @Test
    public void servicesResource_returnsServicesRepresentation() throws Exception {
        // given
        ServicesResource servicesResource = client.getServicesResource();
        
        // when
        Response servicesResp = servicesResource.services();
        JsonResponse<ServicesRepresentation> servicesJsonResp = JsonResponse.of(servicesResp, ServicesRepresentation.class);
        
        // then
        ServicesRepresentation servicesRep = servicesJsonResp.getEntity();
        
        DomainObjectRepresentation applibValuesEntityRepoRep = servicesRep.get(0);
        assertThat(applibValuesEntityRepoRep, is(not(nullValue())));
    }


}
