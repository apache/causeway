/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.viewer.json.applib;

import java.net.URI;

import javax.ws.rs.core.MediaType;

import org.apache.isis.viewer.json.applib.blocks.LinkRepresentation;
import org.apache.isis.viewer.json.applib.blocks.Method;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ClientRequest;
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
        LinkRepresentation getLink = jsonRepresentation.getLink("aLink");

        // when
        context.checking(new Expectations() {
            {
                one(mockExecutor).createRequest("http://foo/bar");
                will(returnValue(mockClientRequest));
                
                ignoring(mockClientRequest);

                one(mockExecutor).execute(mockClientRequest);
                will(returnValue(mockClientResponse));
                
                one(mockClientResponse).setReturnType(String.class);
            }
        });
        client.follow(getLink);
        
        // then
    }

}
