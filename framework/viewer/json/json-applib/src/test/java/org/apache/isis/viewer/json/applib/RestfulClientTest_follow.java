package org.apache.isis.viewer.json.applib;
import java.net.URI;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.isis.viewer.json.applib.blocks.Link;
import org.apache.isis.viewer.json.applib.blocks.Method;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.BaseClientResponse;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(JMock.class)
public class RestfulClientTest_follow {

    private Mockery context = new JUnit4Mockery() {{
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    private RestfulClient client;
    private URI uri;
    private ClientExecutor mockExecutor;
    private ClientRequest mockClientRequest;
    private BaseClientResponse<String> mockClientResponse;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        mockExecutor = context.mock(ClientExecutor.class);
        mockClientRequest = context.mock(ClientRequest.class);
        mockClientResponse = context.mock(BaseClientResponse.class);
        
        uri = URI.create("http://yadayada:8080");
        client = new RestfulClient(uri, mockExecutor);
    }
    
    
    @Test
    public void follow_get() throws Exception {
        // given
        JsonRepresentation jsonRepresentation = new JsonRepresentation(JsonUtils.readJson("map.json"));
        Link getLink = jsonRepresentation.getLink("aLink");

        // when
        final Sequence sequence = context.sequence("execution");
        context.checking(new Expectations() {
            {
                one(mockExecutor).createRequest("http://foo/bar");
                inSequence(sequence);
                will(returnValue(mockClientRequest));
                
                one(mockClientRequest).setHttpMethod(Method.GET.name());
                inSequence(sequence);
                one(mockClientRequest).accept(MediaType.APPLICATION_JSON_TYPE);
                inSequence(sequence);

                one(mockExecutor).execute(mockClientRequest);
                inSequence(sequence);
                will(returnValue(mockClientResponse));
                
                one(mockClientResponse).setReturnType(String.class);
                inSequence(sequence);
            }
        });
        client.follow(getLink);
        
        // then
    }

}
