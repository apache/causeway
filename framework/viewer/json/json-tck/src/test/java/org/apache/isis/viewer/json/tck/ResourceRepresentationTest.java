package org.apache.isis.viewer.json.tck;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import javax.ws.rs.core.Response;

import org.apache.isis.runtimes.dflt.webserver.WebServer;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RestfulClient;
import org.apache.isis.viewer.json.applib.blocks.Method;
import org.apache.isis.viewer.json.applib.domain.DomainObjectRepresentation;
import org.apache.isis.viewer.json.applib.domain.DomainObjectResource;
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
        
        HomePageRepresentation homePageRepr = homePageJsonResp.getEntity();
        assertThat(homePageRepr, is(not(nullValue())));
        assertThat(homePageRepr.getUser().getMethod(), is(Method.GET));
        
        assertThat(homePageRepr.getServices(), is(not(nullValue())));
        assertThat(homePageRepr.getServices().getMethod(), is(Method.GET));
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
        
        JsonRepresentation applibValuesEntityRepoRep = servicesRepr.elementAt(0);
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
        ServicesRepresentation servicesRepr = servicesJsonResp.getEntity();
        
        JsonRepresentation applibValuesEntityRepoRep = servicesRepr.elementAt(0);
        assertThat(applibValuesEntityRepoRep, is(not(nullValue())));
    }

    @Test
    public void domainObjectResource_returnsDomainObjectRepresentation() throws Exception {
        // given
        DomainObjectResource domainObjectResource = client.getDomainObjectResource();
        
        // when
        Response domainObjectResp = domainObjectResource.object("OID:1");
        JsonResponse<DomainObjectRepresentation> domainObjectJsonResp = JsonResponse.of(domainObjectResp, DomainObjectRepresentation.class);
        
        // then
        DomainObjectRepresentation domainObjectRepr = domainObjectJsonResp.getEntity();
    }

    
    
    
}
//{
//    "_self" : {
//      "link" : {
//        "rel" : "object",
//        "href" : "http://localhost:8080/objects/OID:1",
//        "method" : "GET"
//      },
//      "type" : {
//        "rel" : "type",
//        "href" : "http://localhost:8080/types/application/vnd.org.apache.isis.tck.objstore.dflt.scalars.ApplibValuesEntityRepositoryDefault+json",
//        "method" : "GET"
//      },
//      "title" : "ApplibValues",
//      "icon" : {
//        "rel" : "icon",
//        "href" : "http://localhost:8080/images/null.png",
//        "method" : "GET"
//      }
//    },
//    "id" : {
//      "type" : {
//        "rel" : "type",
//        "href" : "http://localhost:8080/types/application/vnd.string+json",
//        "method" : "GET"
//      },
//      "memberType" : "property",
//      "value" : "org.apache.isis.tck.objstore.dflt.scalars.ApplibValuesEntityRepositoryDefault",
//      "disabledReason" : "Always disabled; Derived",
//      "details" : {
//        "rel" : "property",
//        "href" : "http://localhost:8080/objects/OID:1/properties/id",
//        "method" : "GET"
//      }
//    },
//    "list" : {
//      "type" : {
//        "rel" : "type",
//        "href" : "http://localhost:8080/types/application/vnd.java.util.List+json",
//        "method" : "GET"
//      },
//      "memberType" : "action",
//      "actionType" : "USER",
//      "numParameters" : 0,
//      "details" : {
//        "rel" : "action",
//        "href" : "http://localhost:8080/objects/OID:1/actions/list",
//        "method" : "GET"
//      }
//    },
//    "newEntity" : {
//      "type" : {
//        "rel" : "type",
//        "href" : "http://localhost:8080/types/application/vnd.java.lang.Object+json",
//        "method" : "GET"
//      },
//      "memberType" : "action",
//      "actionType" : "USER",
//      "numParameters" : 0,
//      "details" : {
//        "rel" : "action",
//        "href" : "http://localhost:8080/objects/OID:1/actions/newEntity",
//        "method" : "GET"
//      }
//    }
//  }
