package org.apache.isis.viewer.json.tck.stories;

import static org.apache.isis.core.commons.matchers.IsisMatchers.matches;
import static org.junit.Assert.assertThat;

import javax.ws.rs.core.Response;

import org.apache.isis.runtimes.dflt.webserver.WebServer;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RepresentationWalker;
import org.apache.isis.viewer.json.applib.RestfulClient;
import org.apache.isis.viewer.json.applib.homepage.HomePageResource;
import org.apache.isis.viewer.json.tck.IsisWebServerRule;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;


public class UserStoryTest {

    @Rule
    public IsisWebServerRule webServerRule = new IsisWebServerRule();
    
    protected RestfulClient client;

    @Before
    public void setUp() throws Exception {
        WebServer webServer = webServerRule.getWebServer();
        client = new RestfulClient(webServer.getBase());
    }
    

    @Ignore("to get working again")
    @Test
    public void walkResources() throws Exception {
    
        // given a response for an initial resource
        HomePageResource homePageResource = client.getHomePageResource();
        Response homePageResp = homePageResource.resources();
        
        // and given a walker starting from this response
        RepresentationWalker walker = client.createWalker(homePageResp);
        
        // when walk the home pages' 'services' link
        walker.walk("services");
        
        // and when locate the ApplibValues repo and walk the its 'object' link
        walker.walkXpath("/*[title='ApplibValues']/link[rel='object']");
        
        // and when locate the AppLibValues repo's "newEntity" action and walk to its details
        walker.walkXpath("/newEntity[memberType='action']/details");
        
        // and when find the invoke body for the "newEntity" action and then walk the action using the body 
        JsonRepresentation newEntityActionDetails = walker.getEntity();
        JsonRepresentation newEntityActionInvokeBody = newEntityActionDetails.getArray("invoke.body");
        walker.walkXpath("/invoke", newEntityActionInvokeBody);
        
        // and when walk the link to the returned object
        walker.walk("link");
        
        // then the returned object is created with its OID
        JsonRepresentation newEntityDomainObject = walker.getEntity();
        assertThat(newEntityDomainObject.getString("_self.link.href"), matches(".+/objects/OID:[\\d]+$")); 
    }

}
    