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
package org.apache.isis.viewer.restfulobjects.applib;

import java.net.URI;

import javax.ws.rs.core.MediaType;

import org.apache.commons.collections.map.MultiValueMap;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.core.BaseClientResponse;
import org.jboss.resteasy.specimpl.UriBuilderImpl;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2.Mode;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RestfulClient;
import org.apache.isis.viewer.restfulobjects.applib.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.links.LinkRepresentation;

public class RestfulClientTest_follow {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    private final URI uri = URI.create("http://yadayada:8080");

    @Mock
    private ClientExecutor mockExecutor;

    @Mock
    private ClientRequest mockClientRequest;

    @Mock
    private BaseClientResponse<String> mockClientResponse;

    private JsonRepresentation jsonRepresentation;

    private RestfulClient client;

    @Before
    public void setUp() throws Exception {
        jsonRepresentation = new JsonRepresentation(JsonFixture.readJson("map.json"));
        client = new RestfulClient(uri, mockExecutor);
    }

    @Ignore("TODO")
    @Test
    public void follow_get() throws Exception {
        final LinkRepresentation link = jsonRepresentation.getLink("aLink");

        final String href = link.getHref();

        // when
        context.checking(new Expectations() {
            {
                one(mockExecutor).createRequest(with(any(UriBuilderImpl.class)));
                will(returnValue(mockClientRequest));

                one(mockExecutor).execute(mockClientRequest);
                will(returnValue(mockClientResponse));

                one(mockClientRequest).accept(MediaType.APPLICATION_JSON_TYPE);
                atLeast(1).of(mockClientRequest).setHttpMethod("GET");

                allowing(mockClientRequest).getHttpMethod();
                will(returnValue("GET"));

                one(mockClientRequest).execute();
                will(returnValue(mockClientResponse));

                one(mockClientResponse).setReturnType(String.class);
                allowing(mockClientResponse);

                final MultiValueMap result = new MultiValueMap();
                result.put("Content-Type", "application/json");
                allowing(mockClientResponse).getMetadata();
                will(returnValue(result));
            }

        });
        final RestfulResponse<JsonRepresentation> response = client.follow(link);

        // then
    }

    private static Matcher<ClientRequest> requestToHref(final String href) {
        return new TypeSafeMatcher<ClientRequest>() {

            @Override
            public void describeTo(final Description description) {
                description.appendText("request to href: " + href);
            }

            @Override
            public boolean matchesSafely(final ClientRequest clientRequest) {
                try {
                    return clientRequest.getUri().equals(href);
                } catch (final Exception e) {
                    return false;
                }
            }
        };
    }

}
