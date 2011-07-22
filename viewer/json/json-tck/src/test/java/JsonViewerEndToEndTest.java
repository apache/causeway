import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.isis.runtimes.dflt.webserver.WebServer;
import org.apache.isis.viewer.json.applib.blocks.Method;
import org.apache.isis.viewer.json.applib.domain.DomainObjectResource;
import org.apache.isis.viewer.json.applib.domain.ServicesResource;
import org.apache.isis.viewer.json.applib.homepage.HomePage;
import org.apache.isis.viewer.json.applib.homepage.HomePageResource;
import org.apache.isis.viewer.json.applib.user.UserResource;
import org.apache.isis.viewer.json.applib.util.JsonMapper;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.ProxyFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class JsonViewerEndToEndTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        webServer = new WebServer();
        webServer.run(39393);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        webServer.stop();
    }

    private static WebServer webServer;
    
    private final static JsonMapper jsonMapper = new JsonMapper();
    
    private HomePageResource homePageResource;
    private DomainObjectResource domainObjectResource;
    private ServicesResource servicesResource;
    private UserResource userResource;

    @Before
    public void setUp() throws Exception {
        URI base = webServer.getBase();
        ClientRequestFactory clientRequestFactory = new ClientRequestFactory(base);
        
        homePageResource = clientRequestFactory.createProxy(HomePageResource.class);
        domainObjectResource = clientRequestFactory.createProxy(DomainObjectResource.class);
        servicesResource = clientRequestFactory.createProxy(ServicesResource.class);
        userResource = clientRequestFactory.createProxy(UserResource.class);
    }
    
    @Test
    public void homePageResources() throws JsonParseException, JsonMappingException, IOException {
        Response response = homePageResource.resources();
        assertThat(response.getStatus(), is(200));
        HomePage homePage = jsonMapper.read(response, HomePage.class);
        assertThat(homePage, is(not(nullValue())));
        assertThat(homePage.getUser().getMethod(), is(Method.GET));
    }

}
